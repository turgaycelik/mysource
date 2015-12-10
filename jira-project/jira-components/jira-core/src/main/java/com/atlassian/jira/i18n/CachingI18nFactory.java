package com.atlassian.jira.i18n;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.InitializingComponent;
import com.atlassian.jira.cache.GoogleCacheInstruments;
import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.plugin.language.LanguageModuleDescriptor;
import com.atlassian.jira.plugin.language.TranslationTransform;
import com.atlassian.jira.plugin.language.TranslationTransformModuleDescriptor;
import com.atlassian.jira.plugin.util.ModuleDescriptorComparator;
import com.atlassian.jira.plugin.util.PluginsTracker;
import com.atlassian.jira.plugin.util.SimplePluginsTracker;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserLocaleStore;
import com.atlassian.jira.util.ComponentLocator;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.resourcebundle.DefaultResourceBundle;
import com.atlassian.jira.web.util.JiraLocaleUtils;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.plugin.event.events.PluginModuleDisabledEvent;
import com.atlassian.plugin.event.events.PluginModuleEnabledEvent;
import com.atlassian.plugin.event.events.PluginRefreshedEvent;
import com.atlassian.util.concurrent.ResettableLazyReference;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Supplier;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.UncheckedExecutionException;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Collections.sort;

/**
 * As the name implies: a factory that caches for different locales {@link com.atlassian.jira.util.I18nHelper}.

 * With the advent of Plugins-2 we need to iterate through all enabled plugins and get their i18n resources when
 * an {@code I18nHelper} is required.
 */
public class CachingI18nFactory implements I18nHelper.BeanFactory, InitializingComponent, Startable
{
    private static final String I18N_RESOURCE_TYPE = "i18n";

    @ClusterSafe
    private final Cache<Locale, I18nHelper> cache;

    private final JiraLocaleUtils jiraLocaleUtils;
    private final UserLocaleStore userLocaleStore;
    private final Supplier<PluginAccessor> pluginAccessor;
    private final PluginsTracker involvedPluginsTracker;
    private final EventPublisher eventPublisher;
    private final AtomicBoolean jiraStarted = new AtomicBoolean(false);

    @ClusterSafe("Driven by plugin state, which is kept in synch across the cluster")
    private final ResettableLazyReference<List<TranslationTransform>> translationTransformsRef = new ResettableLazyReference<List<TranslationTransform>>()
    {
        @Override
        protected List<TranslationTransform> create() throws Exception
        {
            return loadTranslationTransforms();
        }
    };

    public CachingI18nFactory(
            final JiraLocaleUtils jiraLocaleUtils,
            final EventPublisher eventPublisher,
            final BackingI18nFactory i18nBackingFactory,
            final UserLocaleStore userLocaleStore,
            final ComponentLocator locator)
    {
        this (jiraLocaleUtils, eventPublisher, i18nBackingFactory, userLocaleStore, locator, new SimplePluginsTracker());
    }

    @VisibleForTesting
    CachingI18nFactory(
            final JiraLocaleUtils jiraLocaleUtils,
            final EventPublisher eventPublisher,
            final BackingI18nFactory i18nBackingFactory,
            final UserLocaleStore userLocaleStore,
            final ComponentLocator locator, PluginsTracker tracker)
    {
        this.jiraLocaleUtils = jiraLocaleUtils;
        this.userLocaleStore = userLocaleStore;
        this.pluginAccessor = locator.getComponentSupplier(PluginAccessor.class);
        this.involvedPluginsTracker = tracker;
        this.eventPublisher = eventPublisher;

        final CacheLoader<Locale, I18nHelper> cacheLoader = new CacheLoader<Locale, I18nHelper>()
        {
            @Override
            public I18nHelper load(Locale locale)
            {
                return i18nBackingFactory.create(locale, involvedPluginsTracker, translationTransformsRef.get());
            }
        };

        this.cache = CacheBuilder.newBuilder()
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .build(cacheLoader);
    }

    // Note: We can not use @EventComponent to take care of this for us because we
    // need to clear the cache after we've registered to make sure we haven't missed
    // anything.
    public void start()
    {
        jiraStarted.set(true);
        clearCaches();
        new GoogleCacheInstruments("i18n." + getClass().getSimpleName()).addCache(cache).install();
    }

    @Override
    public void afterInstantiation()
    {
        eventPublisher.register(this);
    }

    @SuppressWarnings ( { "UnusedDeclaration" })
    @EventListener
    public void pluginEnabled(final PluginEnabledEvent event)
    {
        //Only clear caches during framework starting
        if (!jiraStarted.get())
        {
            clearCaches();
        }
    }

    @SuppressWarnings ( { "UnusedDeclaration" })
    @EventListener
    public void pluginModuleDisabled(final PluginModuleDisabledEvent event)
    {
        if (jiraStarted.get() || involvedPluginsTracker.isPluginInvolved(event.getModule()))
        {
            clearCaches();
        }
    }

    @SuppressWarnings ( { "UnusedDeclaration" })
    @EventListener
    public void pluginModuleEnabled(final PluginModuleEnabledEvent event)
    {
        if (jiraStarted.get() && (
                involvedPluginsTracker.isPluginWithModuleDescriptor(event.getModule(), LanguageModuleDescriptor.class) ||
                involvedPluginsTracker.isPluginWithResourceType(event.getModule(), I18N_RESOURCE_TYPE) ||
                involvedPluginsTracker.isPluginWithModuleDescriptor(event.getModule(), TranslationTransformModuleDescriptor.class)))
        {
            clearCaches();
        }
    }

    @SuppressWarnings ( { "UnusedDeclaration" })
    @EventListener
    public void pluginRefreshed(final PluginRefreshedEvent event)
    {
        if (involvedPluginsTracker.isPluginInvolved(event.getPlugin()))
        {
            clearCaches();
        }
    }

    private void clearCaches()
    {
        involvedPluginsTracker.clear();             // <-- copy on write set under the covers
        cache.invalidateAll();
        jiraLocaleUtils.resetInstalledLocales();    // <-- synchronised method
        translationTransformsRef.reset();
    }

    private RuntimeException unwind(RuntimeException re)
    {
        final Throwable cause = re.getCause();
        if (cause instanceof RuntimeException)
        {
            return (RuntimeException)cause;
        }

        if (cause instanceof Error)
        {
            throw (Error)cause;
        }

        return re;
    }

    public I18nHelper getInstance(final Locale locale)
    {
        if (DefaultResourceBundle.isDefaultResourceBundleStale(locale))
        {
            clearCaches();
        }
        try
        {
            return cache.getUnchecked(locale);
        }
        catch (UncheckedExecutionException ex)
        {
            throw unwind(ex);
        }
    }

    public I18nHelper getInstance(final User user)
    {
        return getInstance(userLocaleStore.getLocale(user));
    }

    public I18nHelper getInstance(final ApplicationUser user)
    {
        return getInstance(userLocaleStore.getLocale(user));
    }

    /**
     * An opaque string that changes whenever the underlying i18n bundles change
     * (e.g. when a new translation pack is installed)
     */
    public String getStateHashCode()
    {
        return involvedPluginsTracker.getStateHashCode();
    }

    private List<TranslationTransform> loadTranslationTransforms()
    {
        final List<TranslationTransform> translationTransforms = Lists.newArrayList();
        final List<TranslationTransformModuleDescriptor> descriptors = Lists.newArrayList(
                pluginAccessor.get().getEnabledModuleDescriptorsByClass(TranslationTransformModuleDescriptor.class));
        sort(descriptors, ModuleDescriptorComparator.COMPARATOR);

        for (TranslationTransformModuleDescriptor descriptor : descriptors)
        {
            final TranslationTransform translationTransform = descriptor.getModule();
            involvedPluginsTracker.trackInvolvedPlugin(descriptor);
            translationTransforms.add(translationTransform);
        }
        return ImmutableList.copyOf(translationTransforms);
    }
}
