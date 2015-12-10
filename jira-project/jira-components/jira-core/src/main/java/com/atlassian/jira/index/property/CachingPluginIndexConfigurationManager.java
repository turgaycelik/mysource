package com.atlassian.jira.index.property;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheLoader;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CacheSettingsBuilder;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.event.ComponentManagerStartedEvent;
import com.atlassian.jira.index.IndexDocumentConfiguration;
import com.atlassian.jira.plugin.index.EntityPropertyIndexDocumentModuleDescriptor;
import com.atlassian.ozymandias.SafePluginPointAccess;
import com.atlassian.plugin.event.PluginEventListener;
import com.atlassian.plugin.event.events.PluginRefreshedEvent;
import com.atlassian.plugin.event.events.PluginUninstalledEvent;
import com.atlassian.plugin.event.events.PluginUpgradedEvent;

import com.google.common.base.Function;

/**
 * @since v6.2
 */
@EventComponent
public class CachingPluginIndexConfigurationManager implements PluginIndexConfigurationManager
{
    private final OfBizPluginIndexConfigurationManager delegate;
    private final Cache<String, Iterable<PluginIndexConfiguration>> byEntityKeyCache;

    public CachingPluginIndexConfigurationManager(final OfBizPluginIndexConfigurationManager delegate, final CacheManager cacheManager)
    {
        this.delegate = delegate;
        this.byEntityKeyCache = cacheManager.getCache(CachingPluginIndexConfigurationManager.class.getName() + ".cacheByEntityKey",
                new ByEntityKeyCacheLoader(),
                new CacheSettingsBuilder().expireAfterAccess(10, TimeUnit.MINUTES).maxEntries(1000).build());
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        byEntityKeyCache.removeAll();
    }

    @PluginEventListener
    public void onPluginUninstalled(final PluginUninstalledEvent event)
    {
        remove(event.getPlugin().getKey());
    }

    @PluginEventListener
    public void onPluginRefreshed(final PluginRefreshedEvent event)
    {
        remove(event.getPlugin().getKey());
    }

    @PluginEventListener
    public void onPluginUpgraded(final PluginUpgradedEvent event)
    {
        remove(event.getPlugin().getKey());
    }

    @EventListener
    public void onComponentManagerStartedEvent(final ComponentManagerStartedEvent ignore)
    {
        //Note to future developers changing when ComponentManagerStartedEvent is triggered
        //You need to invoke this method in the state when
        // plugins system was started
        // and all JIRA components were initialized
        // but *before* JIRA attempts to do any reindex.
        final List<EntityPropertyIndexDocumentModuleDescriptor> epIndexModules = ComponentAccessor.getPluginAccessor().getEnabledModuleDescriptorsByClass(EntityPropertyIndexDocumentModuleDescriptor.class);
        for (final EntityPropertyIndexDocumentModuleDescriptor epIndexModule : epIndexModules)
        {
            SafePluginPointAccess.safe(new Function<EntityPropertyIndexDocumentModuleDescriptor, Void>()
            {
                @Override
                public Void apply(final EntityPropertyIndexDocumentModuleDescriptor input)
                {
                    //those modules may have not initialized before now
                    input.init();
                    return null;
                }
            }).apply(epIndexModule);
        }
    }
    @Override
    public Iterable<PluginIndexConfiguration> getDocumentsForEntity(@Nonnull final String entityKey)
    {
        return byEntityKeyCache.get(entityKey);
    }

    @Override
    public void put(@Nonnull final String pluginKey, @Nonnull final String moduleKey, @Nonnull final IndexDocumentConfiguration document)
    {
        delegate.put(pluginKey, moduleKey, document);
        byEntityKeyCache.remove(document.getEntityKey());
    }

    @Override
    public void remove(@Nonnull final String pluginKey)
    {
        delegate.remove(pluginKey);
        byEntityKeyCache.removeAll();
    }

    private class ByEntityKeyCacheLoader implements CacheLoader<String, Iterable<PluginIndexConfiguration>>
    {
        @Nonnull
        @Override
        public Iterable<PluginIndexConfiguration> load(@Nonnull final String entityKey)
        {
            return delegate.getDocumentsForEntity(entityKey);
        }
    }
}
