package com.atlassian.jira.i18n;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.instrumentation.InstrumentRegistry;
import com.atlassian.jira.junit.rules.MockComponentContainer;
import com.atlassian.jira.plugin.language.LanguageModuleDescriptor;
import com.atlassian.jira.plugin.language.TranslationTransform;
import com.atlassian.jira.plugin.language.TranslationTransformModuleDescriptor;
import com.atlassian.jira.plugin.util.PluginsTracker;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.MockUserLocaleStore;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MockComponentLocator;
import com.atlassian.jira.web.util.JiraLocaleUtils;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.plugin.event.events.PluginModuleDisabledEvent;
import com.atlassian.plugin.event.events.PluginModuleEnabledEvent;
import com.atlassian.plugin.event.events.PluginRefreshedEvent;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.when;

/**
 * @since v6.2.3
 */
@RunWith(MockitoJUnitRunner.class)
public class TestCachingI18nFactory
{
    @Rule
    public MockComponentContainer container = new MockComponentContainer(this);

    @Mock
    private JiraLocaleUtils jiraLocaleUtils;
    private MockUserLocaleStore userLocaleStore = new MockUserLocaleStore(Locale.ENGLISH);
    private BackingI18nFactory backingI18nFactory = new MockBackingI18nFactory();

    @Mock
    private InstrumentRegistry registry;

    @Mock
    private EventPublisher publisher;

    @Mock
    private PluginsTracker tracker;

    @Mock
    private PluginAccessor pluginAccessor;

    private MockComponentLocator locator = new MockComponentLocator();

    private CachingI18nFactory i18nFactory;
    private int pluginKeyCount;

    @Before
    public void setup()
    {
        container.addMock(InstrumentRegistry.class, registry);
        locator.register(PluginAccessor.class, pluginAccessor);

        i18nFactory = new CachingI18nFactory(jiraLocaleUtils, publisher, backingI18nFactory, userLocaleStore, locator, tracker);
    }

    @Test
    public void getHelperForLocaleCached()
    {
        final I18nHelper englishCall = i18nFactory.getInstance(Locale.ENGLISH);
        final I18nHelper englishCall2 = i18nFactory.getInstance(Locale.ENGLISH);

        assertThat(englishCall.getLocale(), equalTo(Locale.ENGLISH));
        assertThat(englishCall2.getLocale(), equalTo(Locale.ENGLISH));
        assertThat(englishCall, Matchers.sameInstance(englishCall2));

        final I18nHelper frenchCall = i18nFactory.getInstance(Locale.FRENCH);
        assertThat(frenchCall.getLocale(), equalTo(Locale.FRENCH));

        final I18nHelper englishCall3 = i18nFactory.getInstance(Locale.ENGLISH);
        assertThat(englishCall3.getLocale(), equalTo(Locale.ENGLISH));
        assertThat(englishCall3, Matchers.sameInstance(englishCall2));
    }

    @Test
    public void getHelperForUserCached()
    {
        User frenchUser = new MockUser("fr_FR");
        ApplicationUser frenchUserApp = new MockApplicationUser("fr_FR");
        User german = new MockUser("de_DE");

        userLocaleStore
                .setLocale(frenchUser, Locale.FRANCE)
                .setLocale(frenchUserApp, Locale.FRANCE)
                .setLocale(german, Locale.GERMANY);

        final I18nHelper french1 = i18nFactory.getInstance(frenchUser);
        final I18nHelper french2 = i18nFactory.getInstance(frenchUser);

        assertThat(french1.getLocale(), equalTo(Locale.FRANCE));
        assertThat(french2.getLocale(), equalTo(Locale.FRANCE));
        assertThat(french1, Matchers.sameInstance(french2));

        final I18nHelper german1 = i18nFactory.getInstance(german);
        assertThat(german1.getLocale(), equalTo(Locale.GERMANY));

        final I18nHelper french3 = i18nFactory.getInstance(frenchUserApp);
        assertThat(french3.getLocale(), equalTo(Locale.FRANCE));
        assertThat(french3, Matchers.sameInstance(french2));
    }

    @Test
    public void getHelperContainsCorrectTransforms()
    {
        final TranslationTransformModuleDescriptor moduleDescriptor1 = createTransformModule();
        final TranslationTransformModuleDescriptor moduleDescriptor2 = createTransformModule();

        when(pluginAccessor.getEnabledModuleDescriptorsByClass(TranslationTransformModuleDescriptor.class))
                .thenReturn(asList(moduleDescriptor1, moduleDescriptor2));
        assertTransforms(i18nFactory.getInstance(Locale.CANADA), moduleDescriptor1, moduleDescriptor2);
    }

    @Test
    public void getHelperContainsCorrectTransformsInCorrectOrder()
    {
        final TranslationTransformModuleDescriptor moduleDescriptor1 = createTransformModule(2);
        final TranslationTransformModuleDescriptor moduleDescriptor2 = createTransformModule(1);

        when(pluginAccessor.getEnabledModuleDescriptorsByClass(TranslationTransformModuleDescriptor.class))
                .thenReturn(asList(moduleDescriptor1, moduleDescriptor2));

        //Make sure we get the correct transforms.
        assertTransforms(i18nFactory.getInstance(Locale.CANADA), moduleDescriptor2, moduleDescriptor1);
    }


    @Test
    public void getHelperContainsCorrectTransformsAfterCacheRefresh()
    {
        final TranslationTransformModuleDescriptor moduleDescriptor1 = createTransformModule();
        final TranslationTransformModuleDescriptor moduleDescriptor2 = createTransformModule();
        final TranslationTransformModuleDescriptor moduleDescriptor3 = createTransformModule();
        final TranslationTransformModuleDescriptor moduleDescriptor4 = createTransformModule();

        when(pluginAccessor.getEnabledModuleDescriptorsByClass(TranslationTransformModuleDescriptor.class))
                .thenReturn(asList(moduleDescriptor1, moduleDescriptor2));

        //Make sure we get the correct transforms.
        assertTransforms(i18nFactory.getInstance(Locale.CANADA), moduleDescriptor1, moduleDescriptor2);

        //Check that we cache the transforms.
        when(pluginAccessor.getEnabledModuleDescriptorsByClass(TranslationTransformModuleDescriptor.class))
                .thenReturn(asList(moduleDescriptor4, moduleDescriptor3));
        assertTransforms(i18nFactory.getInstance(Locale.CANADA), moduleDescriptor1, moduleDescriptor2);


        //Trigger a cache reset. We will not get new transforms.
        i18nFactory.start();
        assertTransforms(i18nFactory.getInstance(Locale.CANADA), moduleDescriptor4, moduleDescriptor3);
    }

    @Test
    public void registeredAsEventListene()
    {
        i18nFactory.afterInstantiation();
        Mockito.verify(publisher).register(i18nFactory);
    }

    @Test
    public void pluginEnableWillClearCacheDuringStartup()
    {
        final Plugin plugin = createPlugin();

        I18nHelper canada = i18nFactory.getInstance(Locale.CANADA);
        canada = assertCached(canada, i18nFactory.getInstance(Locale.CANADA));

        //At startup, this event will trigger a cache clear.
        i18nFactory.pluginEnabled(new PluginEnabledEvent(plugin));
        canada = assertNotCached(canada, i18nFactory.getInstance(Locale.CANADA));

        //Trigger a start. Cache will be cleared *now* but wont be cleared on plugin enabled any more.
        i18nFactory.start();

        //Cache was cleared on start.
        canada = assertNotCached(canada, i18nFactory.getInstance(Locale.CANADA));

        //This will no longer trigger cached clear.
        i18nFactory.pluginEnabled(new PluginEnabledEvent(plugin));
        assertCached(canada, i18nFactory.getInstance(Locale.CANADA));
    }

    @Test
    public void pluginModuleDisabledWillClearCacheIfRelevant()
    {
        final TranslationTransformModuleDescriptor transformModule = createTransformModule();

        I18nHelper canada = i18nFactory.getInstance(Locale.CANADA);
        canada = assertCached(canada, i18nFactory.getInstance(Locale.CANADA));

        //At startup, this event will trigger cache clear when relevant.
        when(tracker.isPluginInvolved(transformModule)).thenReturn(true);
        i18nFactory.pluginModuleDisabled(new PluginModuleDisabledEvent(transformModule, false));
        canada = assertNotCached(canada, i18nFactory.getInstance(Locale.CANADA));

        //Now lets say the module is not relevant and as such the disable should not clear the cache.
        when(tracker.isPluginInvolved(transformModule)).thenReturn(false);
        i18nFactory.pluginModuleDisabled(new PluginModuleDisabledEvent(transformModule, false));
        canada = assertCached(canada, i18nFactory.getInstance(Locale.CANADA));

        //Trigger a start. Cache will be cleared *now* and always for this event.
        i18nFactory.start();

        //Cache was cleared on start.
        canada = assertNotCached(canada, i18nFactory.getInstance(Locale.CANADA));

        //This will always trigger a cache clear even if not relevant (WTF?)
        when(tracker.isPluginInvolved(transformModule)).thenReturn(false);
        i18nFactory.pluginModuleDisabled(new PluginModuleDisabledEvent(transformModule, false));
        assertNotCached(canada, i18nFactory.getInstance(Locale.CANADA));
    }

    @Test
    public void pluginRefreshedWillClearCacheIfRelevant()
    {
        final TranslationTransformModuleDescriptor transformModule = createTransformModule();

        //Initially cached.
        I18nHelper canada = i18nFactory.getInstance(Locale.CANADA);
        canada = assertCached(canada, i18nFactory.getInstance(Locale.CANADA));

        //When not started even relevant events wont clear the cached.
        when(tracker.isPluginWithModuleDescriptor(transformModule, LanguageModuleDescriptor.class)).thenReturn(true);
        i18nFactory.pluginModuleEnabled(new PluginModuleEnabledEvent(transformModule));
        canada = assertCached(canada, i18nFactory.getInstance(Locale.CANADA));

        //Trigger a start. Cache will be cleared *now*.
        i18nFactory.start();
        canada = assertNotCached(canada, i18nFactory.getInstance(Locale.CANADA));

        //Once started, enabling a LanguageModuleDescriptor will cause a cache refresh.
        when(tracker.isPluginWithModuleDescriptor(transformModule, LanguageModuleDescriptor.class)).thenReturn(true);
        i18nFactory.pluginModuleEnabled(new PluginModuleEnabledEvent(transformModule));
        canada = assertNotCached(canada, i18nFactory.getInstance(Locale.CANADA));

        //Once started, enabling a TranslationTransformModuleDescriptor will cause a cache refresh.
        Mockito.reset(tracker);
        when(tracker.isPluginWithModuleDescriptor(transformModule, TranslationTransformModuleDescriptor.class)).thenReturn(true);
        i18nFactory.pluginModuleEnabled(new PluginModuleEnabledEvent(transformModule));
        canada = assertNotCached(canada, i18nFactory.getInstance(Locale.CANADA));

        //Once started, enabling a plugin with I18N_RESOURCE_TYPE will cause a cache refresh.
        Mockito.reset(tracker);
        when(tracker.isPluginWithResourceType(transformModule, "i18n")).thenReturn(true);
        i18nFactory.pluginModuleEnabled(new PluginModuleEnabledEvent(transformModule));
        canada = assertNotCached(canada, i18nFactory.getInstance(Locale.CANADA));

        //Other events will not cause cache reset.
        Mockito.reset(tracker);
        i18nFactory.pluginModuleEnabled(new PluginModuleEnabledEvent(transformModule));
        assertCached(canada, i18nFactory.getInstance(Locale.CANADA));
    }

    @Test
    public void pluginModuleEnabledWillClearCacheIfRelevant()
    {
        final TranslationTransformModuleDescriptor transformModule = createTransformModule();

        //Initially cached.
        I18nHelper canada = i18nFactory.getInstance(Locale.CANADA);
        canada = assertCached(canada, i18nFactory.getInstance(Locale.CANADA));

        //Relevant event will always clear the cache.
        when(tracker.isPluginInvolved(transformModule.getPlugin())).thenReturn(true);
        i18nFactory.pluginRefreshed(new PluginRefreshedEvent(transformModule.getPlugin()));
        canada = assertNotCached(canada, i18nFactory.getInstance(Locale.CANADA));

        //Not lets say the module is not relevant and as such the refresh should not clear the cache.
        when(tracker.isPluginInvolved(transformModule.getPlugin())).thenReturn(false);
        i18nFactory.pluginRefreshed(new PluginRefreshedEvent(transformModule.getPlugin()));
        canada = assertCached(canada, i18nFactory.getInstance(Locale.CANADA));

        //Trigger a start. Cache will be cleared *now*.
        i18nFactory.start();
        canada = assertNotCached(canada, i18nFactory.getInstance(Locale.CANADA));

        //Cache will not be cleared unless relevant
        when(tracker.isPluginInvolved(transformModule.getPlugin())).thenReturn(false);
        i18nFactory.pluginRefreshed(new PluginRefreshedEvent(transformModule.getPlugin()));
        canada = assertCached(canada, i18nFactory.getInstance(Locale.CANADA));

        //Cache will not be cleared when relevant
        when(tracker.isPluginInvolved(transformModule.getPlugin())).thenReturn(true);
        i18nFactory.pluginRefreshed(new PluginRefreshedEvent(transformModule.getPlugin()));
        assertNotCached(canada, i18nFactory.getInstance(Locale.CANADA));
    }

    @Test
    public void getStateHashCodeTracksPlugins()
    {
        //Hash should remain the same when no changes made.
        final String stateHashCode = i18nFactory.getStateHashCode();
        assertThat(stateHashCode, equalTo(i18nFactory.getStateHashCode()));
    }

    private static I18nHelper assertCached(final I18nHelper old, final I18nHelper next)
    {
        //Initially cached.
        assertThat(old, sameInstance(next));
        return next;
    }

    private static I18nHelper assertNotCached(final I18nHelper old, final I18nHelper next)
    {
        //Initially cached.
        assertThat(old, not(sameInstance(next)));
        return next;
    }

    private TranslationTransformModuleDescriptor createTransformModule()
    {
        return createTransformModule(0);
    }

    private TranslationTransformModuleDescriptor createTransformModule(final int order)
    {
        final TranslationTransformModuleDescriptor descriptor = Mockito.mock(TranslationTransformModuleDescriptor.class);
        final TranslationTransform transform = Mockito.mock(TranslationTransform.class);
        final Plugin plugin = createPlugin();

        when(descriptor.getModule()).thenReturn(transform);
        when(descriptor.getPlugin()).thenReturn(plugin);
        when(descriptor.getOrder()).thenReturn(order);

        return descriptor;
    }

    private void assertTransforms(final I18nHelper i18nHelper, final TranslationTransformModuleDescriptor...descriptors)
    {
        List<Matcher<? super TranslationTransform>> transformMatchers = Lists.newArrayList();
        for (TranslationTransformModuleDescriptor descriptor : descriptors)
        {
            transformMatchers.add(equalTo(descriptor.getModule()));
        }
        //Make sure we get the correct transforms.
        assertThat(((MockI18nHelper)i18nHelper).getTransforms(), contains(transformMatchers));
    }

    private Plugin createPlugin()
    {
        final Plugin plugin  = Mockito.mock(Plugin.class);
        when(plugin.getKey()).thenReturn(createPluginKey());

        return plugin;
    }

    private String createPluginKey()
    {
        return String.format("pluign:key:%d", pluginKeyCount++);
    }

    private static class MockBackingI18nFactory implements BackingI18nFactory
    {
        @Override
        public I18nHelper create(final Locale locale, final PluginsTracker involvedPluginsTracker,
                final Iterable<? extends TranslationTransform> translationTransforms)
        {
            return new MockI18nHelper(locale, translationTransforms);
        }
    }

    private static class MockI18nHelper implements I18nHelper
    {

        private final Locale locale;
        private final List<TranslationTransform> transforms;

        private MockI18nHelper(final Locale locale, final Iterable<? extends TranslationTransform> transforms)
        {
            this.locale = locale;
            this.transforms = ImmutableList.copyOf(transforms);
        }

        public List<TranslationTransform> getTransforms()
        {
            return transforms;
        }

        @Override
        public Locale getLocale()
        {
            return locale;
        }

        @Override
        public ResourceBundle getDefaultResourceBundle()
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public String getUnescapedText(final String key)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public String getUntransformedRawText(final String key)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public boolean isKeyDefined(final String key)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public String getText(final String key)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public String getText(final String key, final String value1)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public String getText(final String key, final String value1, final String value2)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public String getText(final String key, final String value1, final String value2, final String value3)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public String getText(final String key, final String value1, final String value2, final String value3, final String value4)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public String getText(final String key, final Object value1, final Object value2, final Object value3)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public String getText(final String key, final Object value1, final Object value2, final Object value3, final Object value4)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public String getText(final String key, final Object value1, final Object value2, final Object value3, final Object value4, final Object value5)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public String getText(final String key, final Object value1, final Object value2, final Object value3, final Object value4, final Object value5, final Object value6)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public String getText(final String key, final Object value1, final Object value2, final Object value3, final Object value4, final Object value5, final Object value6, final Object value7)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public String getText(final String key, final String value1, final String value2, final String value3, final String value4, final String value5, final String value6, final String value7)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public String getText(final String key, final Object value1, final Object value2, final Object value3, final Object value4, final Object value5, final Object value6, final Object value7, final Object value8)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public String getText(final String key, final String value1, final String value2, final String value3, final String value4, final String value5, final String value6, final String value7, final String value8, final String value9)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public String getText(final String key, final Object parameters)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public Set<String> getKeysForPrefix(final String prefix)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public ResourceBundle getResourceBundle()
        {
            throw new UnsupportedOperationException("Not implemented");
        }
    }
}
