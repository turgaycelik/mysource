package com.atlassian.jira.plugin.keyboardshortcut;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.easymock.EasyMockAnnotations;
import com.atlassian.jira.easymock.Mock;
import com.atlassian.jira.plugin.webfragment.descriptors.ConditionDescriptorFactoryImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.webresource.WebResourceIntegration;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.junit.Before;
import org.junit.Test;

import static java.util.Collections.singletonList;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class TestCachingKeyboardShortcutManager
{
    private static final Map<String,Object> USER_CONTEXT = Collections.emptyMap();
    private static final KeyboardShortcut GLOBAL_SHORTCUT_100 = new KeyboardShortcut("key", KeyboardShortcutManager.Context.global, KeyboardShortcutManager.Operation.execute, "alert('hidden shortcut');", 100, keys("g", "f"), "i18n_key1", false);
    private static final KeyboardShortcut GLOBAL_SHORTCUT_200 = new KeyboardShortcut("key", KeyboardShortcutManager.Context.global, KeyboardShortcutManager.Operation.execute, "alert('give us the func!');", 200, keys("g", "f"), "i18n_key2", false);
    private static final KeyboardShortcut GLOBAL_SHORTCUT_300 = new KeyboardShortcut("key", KeyboardShortcutManager.Context.global, KeyboardShortcutManager.Operation.execute, "alert('super g');", 300, keys("g"), "i18n_key3", false);
    private static final KeyboardShortcut GLOBAL_SHORTCUT_400 = new KeyboardShortcut("key", KeyboardShortcutManager.Context.global, KeyboardShortcutManager.Operation.execute, "alert('typed p');", 400, keys("p"), "i18n_key_4", false);
    private static final KeyboardShortcut GLOBAL_SHORTCUT_500 = new KeyboardShortcut("key", KeyboardShortcutManager.Context.global, KeyboardShortcutManager.Operation.execute, "alert('typed ps');", 500, keys("ps"), "i18n_key_5", false);
    private static final KeyboardShortcut GREENHOPPER_SHORTCUT_200 = new KeyboardShortcut("key", KeyboardShortcutManager.Context.greenhopper, KeyboardShortcutManager.Operation.execute, "alert('give us the func!');", 200, keys("g", "f"), "i18n_key2", false);

    @Mock
    BuildUtilsInfo mockBuildUtilsInfo;

    @Mock
    EventPublisher mockEventPublisher;

    @Mock
    PluginAccessor mockPluginAccessor;

    @Mock
    JiraAuthenticationContext mockAuthenticationContext;

    @Mock
    UserPreferencesManager mockUserPrefsManager;

    @Mock
    WebResourceIntegration mockWebResourceIntegration;

    @Mock
    Condition mockCondition;

    private CachingKeyboardShortcutManager manager;

    @Before
    public void setUp() throws Exception
    {
        EasyMockAnnotations.initMocks(this);

        manager = new CachingKeyboardShortcutManager(mockBuildUtilsInfo, mockPluginAccessor, mockWebResourceIntegration, mockAuthenticationContext, mockUserPrefsManager)
        {
            @Override
            Map<String, Object> getWebFragmentContext()
            {
                return Collections.emptyMap();
            }
        };
    }

    @Test
    public void testClearCacheEvent() throws Exception
    {
        expect(mockBuildUtilsInfo.getCurrentBuildNumber()).andReturn("500").anyTimes();

        final MockKeyboardShortcutModuleDescriptor mockKeyboardShortcutModuleDescriptor = new MockKeyboardShortcutModuleDescriptor(null, null, null);
        expect(mockPluginAccessor.getEnabledModuleDescriptorsByClass(KeyboardShortcutModuleDescriptor.class)).
                andReturn(CollectionBuilder.<KeyboardShortcutModuleDescriptor>newBuilder(mockKeyboardShortcutModuleDescriptor).asList());

        replay(mockBuildUtilsInfo, mockEventPublisher, mockPluginAccessor);

        List<KeyboardShortcut> list = manager.getAllShortcuts();
        assertEquals(0, list.size());
        final Set<List<String>> keys = new HashSet<List<String>>();
        keys.add(Arrays.asList("g", "h"));
        final KeyboardShortcut shortcut = new KeyboardShortcut("key", KeyboardShortcutManager.Context.global,
                KeyboardShortcutManager.Operation.click, "#some_link", 30, keys, "blah", false);
        manager.registerShortcut("some.key", shortcut);
        final List<KeyboardShortcut> shortcuts = manager.getAllShortcuts();
        assertEquals(1, shortcuts.size());
        assertEquals(shortcut, shortcuts.get(0));

        //now clear the cache
        manager.onClearCache(null);

        final List<KeyboardShortcut> shortcuts2 = manager.getAllShortcuts();
        assertEquals(1, shortcuts2.size());
        assertEquals(mockKeyboardShortcutModuleDescriptor.getSecondShortcut(), shortcuts2.get(0));

        verify(mockBuildUtilsInfo, mockEventPublisher, mockPluginAccessor);
    }

    /**
     * Test registering and unregistering of shortcuts and that this action also updates the hashcode for the URL used
     * to include shortcuts.
     */
    @Test
    public void testAddAndRemoveShortcuts()
    {
        prepare();

        List<KeyboardShortcut> list = manager.getAllShortcuts();
        assertEquals(0, list.size());
        final Set<List<String>> keys = new HashSet<List<String>>();
        keys.add(Arrays.asList("g", "h"));
        final KeyboardShortcut shortcut = new KeyboardShortcut("key", KeyboardShortcutManager.Context.global,
                KeyboardShortcutManager.Operation.click, "#some_link", 30, keys, "blah", false);
        final KeyboardShortcut shortcut2 = new KeyboardShortcut("key", KeyboardShortcutManager.Context.global,
                KeyboardShortcutManager.Operation.click, "#some_link2", 10, keys, "blah2", false);
        final String url = manager.includeShortcuts();
        assertNotNull(url);
        manager.registerShortcut("some.key", shortcut);
        final String url2 = manager.includeShortcuts();
        //url should have changed after registering another shortcut
        assertFalse(url.equals(url2));
        manager.registerShortcut("some.key2", shortcut2);
        final String url3 = manager.includeShortcuts();
        //url should have changed after registering another shortcut
        assertFalse(url2.equals(url3));

        final List<KeyboardShortcut> sortedList = manager.getAllShortcuts();
        assertEquals(2, sortedList.size());
        //due to the order attribute, shortcut 2 should come first
        assertEquals(shortcut2, sortedList.get(0));
        assertEquals(shortcut, sortedList.get(1));

        manager.unregisterShortcut("some.key");
        final String url4 = manager.includeShortcuts();
        //url should have changed after registering another shortcut
        assertFalse(url3.equals(url4));

        final List<KeyboardShortcut> allShortcuts = manager.getAllShortcuts();
        assertEquals(1, allShortcuts.size());
        assertEquals(shortcut2, allShortcuts.get(0));

        verify(mockBuildUtilsInfo, mockWebResourceIntegration);
    }

    @Test
    public void getActiveShortcutsDoesNotReturnDuplicateShortcuts() throws Exception
    {
        manager.registerShortcut("key1", GLOBAL_SHORTCUT_100);
        manager.registerShortcut("key2", GLOBAL_SHORTCUT_200);

        List<KeyboardShortcut> shortcuts = manager.getActiveShortcuts();
        assertThat(shortcuts, equalTo(singletonList(GLOBAL_SHORTCUT_200)));
    }

    @Test
    public void getActiveShortcutsDoesNotReturnShadowedShortcuts() throws Exception
    {
        manager.registerShortcut("module1", GLOBAL_SHORTCUT_100);
        manager.registerShortcut("module3", GLOBAL_SHORTCUT_300);
        manager.registerShortcut("module4", GLOBAL_SHORTCUT_400);
        manager.registerShortcut("module5", GLOBAL_SHORTCUT_500);

        List<KeyboardShortcut> shortcuts = manager.getActiveShortcuts();
        assertThat("shortcut 'g' should shadow shortcut 'gf'", shortcuts, hasItem(GLOBAL_SHORTCUT_300));
        assertThat("shortcut 'ps' should shadow shortcut 'p'", shortcuts, hasItem(GLOBAL_SHORTCUT_500));
        assertThat(shortcuts.size(), equalTo(2));
    }

    @Test
    public void getActiveShortcutsReturnsDuplicateShortcutsAcrossContexts() throws Exception
    {
        manager.registerShortcut("key1", GLOBAL_SHORTCUT_100);
        manager.registerShortcut("key2", GREENHOPPER_SHORTCUT_200);

        List<KeyboardShortcut> shortcuts = manager.getActiveShortcuts();
        assertThat(shortcuts.size(), is(2));
        assertThat(shortcuts, hasItem(GLOBAL_SHORTCUT_100));
        assertThat(shortcuts, hasItem(GREENHOPPER_SHORTCUT_200));
    }

    @Test
    public void listActiveShortcutsWithContextDoesNotReturnDuplicateShortcuts() throws Exception
    {
        manager.registerShortcut("key1", GLOBAL_SHORTCUT_100);
        manager.registerShortcut("key2", GLOBAL_SHORTCUT_200);

        List<KeyboardShortcut> shortcuts = manager.listActiveShortcutsUniquePerContext(USER_CONTEXT);
        assertThat(shortcuts, equalTo(singletonList(GLOBAL_SHORTCUT_200)));
    }

    @Test
    public void listActiveShortcutsReturnsDuplicateShortcutsAcrossContexts() throws Exception
    {
        manager.registerShortcut("key1", GLOBAL_SHORTCUT_100);
        manager.registerShortcut("key2", GREENHOPPER_SHORTCUT_200);

        List<KeyboardShortcut> shortcuts = manager.listActiveShortcutsUniquePerContext(USER_CONTEXT);
        assertThat(shortcuts.size(), is(2));
        assertThat(shortcuts, hasItem(GLOBAL_SHORTCUT_100));
        assertThat(shortcuts, hasItem(GREENHOPPER_SHORTCUT_200));
    }

    @Test
    public void listActiveShortcutsWithContextConditionEvaluationThrowsException() throws Exception
    {
        KeyboardShortcut globalShortcut100 = new KeyboardShortcut("key", KeyboardShortcutManager.Context.global, KeyboardShortcutManager.Operation.execute, "alert('hidden shortcut');", 100, keys("g", "f"), "i18n_key1", false, mockCondition);
        manager.registerShortcut("key1", globalShortcut100);
        manager.registerShortcut("key2", GLOBAL_SHORTCUT_200);

        expect(mockCondition.shouldDisplay(USER_CONTEXT)).andThrow(new RuntimeException("Faulty condition: catch me, if you can"));
        replay(mockCondition);

        List<KeyboardShortcut> shortcuts = manager.listActiveShortcutsUniquePerContext(USER_CONTEXT);
        assertThat(shortcuts, equalTo(singletonList(GLOBAL_SHORTCUT_200)));

        verify(mockCondition);
    }

    @Test
    public void testRequiringContextsDoesntChangeUrlHash()
    {
        prepare();

        final Set<List<String>> keys = new HashSet<List<String>>();
        keys.add(Arrays.asList("g", "h"));
        final KeyboardShortcut shortcut = new KeyboardShortcut("key", KeyboardShortcutManager.Context.global,
                KeyboardShortcutManager.Operation.click, "#some_link", 30, keys, "blah", false);

        final String urlWithHash = manager.includeShortcuts();
        assertFalse(urlWithHash.contains("?"));

        manager.requireShortcutsForContext(KeyboardShortcutManager.Context.issueaction);
        assertTrue(manager.includeShortcuts().startsWith(urlWithHash));
        assertTrue(manager.includeShortcuts().contains("context=" + KeyboardShortcutManager.Context.issueaction.toString()));

        manager.requireShortcutsForContext(KeyboardShortcutManager.Context.issuenavigation);
        assertTrue(manager.includeShortcuts().startsWith(urlWithHash));
        assertTrue(manager.includeShortcuts().contains("context=" + KeyboardShortcutManager.Context.issueaction.toString()));
        assertTrue(manager.includeShortcuts().contains("context=" + KeyboardShortcutManager.Context.issuenavigation.toString()));

        // Now make the hash change.
        manager.registerShortcut("some.key", shortcut);
        assertFalse(manager.includeShortcuts().startsWith(urlWithHash));
        assertTrue(manager.includeShortcuts().contains("context=" + KeyboardShortcutManager.Context.issueaction.toString()));
        assertTrue(manager.includeShortcuts().contains("context=" + KeyboardShortcutManager.Context.issuenavigation.toString()));

        verify(mockBuildUtilsInfo, mockWebResourceIntegration);
    }

    @Test
    public void changingConditionResultsChangesUrlHash()
    {
        prepare();

        final Set<List<String>> keys = new HashSet<List<String>>();
        keys.add(Arrays.asList("g", "h"));

        final AtomicBoolean conditionResult = new AtomicBoolean(false);

        Condition condition = new Condition()
        {
            @Override
            public void init(final Map<String, String> stringStringMap) throws PluginParseException
            {
            }

            @Override
            public boolean shouldDisplay(Map<String, Object> context)
            {
                return conditionResult.get();
            }
        };

        KeyboardShortcut shortcut = new KeyboardShortcut("key", KeyboardShortcutManager.Context.global,
                KeyboardShortcutManager.Operation.click, "#some_link", 30, keys, "blah", false, condition);

        manager.registerShortcut("some.key", shortcut);

        String urlWhenConditionIsFalse = manager.includeShortcuts();

        conditionResult.set(true);
        assertFalse("URL-s must be different since condition result changed", urlWhenConditionIsFalse.equals(manager.includeShortcuts()));

        // Change condition result value back, URL must be the same.
        conditionResult.set(false);
        assertEquals(urlWhenConditionIsFalse, manager.includeShortcuts());

        verify(mockBuildUtilsInfo, mockWebResourceIntegration);
    }

    private void prepare()
    {
        HashMap<String, Object> requestCache = new HashMap<String, Object>();

        expect(mockBuildUtilsInfo.getCurrentBuildNumber()).andReturn("500").anyTimes();
        expect(mockWebResourceIntegration.getRequestCache())
                .andReturn(requestCache)
                .anyTimes();

        replay(mockBuildUtilsInfo, mockWebResourceIntegration);
    }

    private static class MockKeyboardShortcutModuleDescriptor extends KeyboardShortcutModuleDescriptor
    {
        private KeyboardShortcut secondShortcut;

        public MockKeyboardShortcutModuleDescriptor(final JiraAuthenticationContext authenticationContext, final KeyboardShortcutManager keyboardShortcutManager, final HostContainer hostContainer)
        {
            super(authenticationContext, keyboardShortcutManager, ModuleFactory.LEGACY_MODULE_FACTORY, new ConditionDescriptorFactoryImpl(hostContainer));
            final Set<List<String>> keys = new HashSet<List<String>>();
            keys.add(Arrays.asList("c"));
            secondShortcut = new KeyboardShortcut("key", KeyboardShortcutManager.Context.issueaction,
                    KeyboardShortcutManager.Operation.click, "#create_issue", 10, keys, "dude", false);
        }

        @Override
        public String getCompleteKey()
        {
            return "some.plugin.module.key";
        }

        @Override
        protected KeyboardShortcut createModule()
        {
            return secondShortcut;
        }

        public KeyboardShortcut getSecondShortcut()
        {
            return secondShortcut;
        }
    }

    static Set<List<String>> keys(String... keys)
    {
        List<String> strings = Lists.newArrayList(keys);

        //noinspection unchecked
        return Sets.<List<String>>newHashSet(strings);
    }
}
