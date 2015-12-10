package com.atlassian.jira.plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.jira.startup.JiraStartupPluginSystemListener;
import com.atlassian.jira.tenancy.JiraTenantAccessor;
import com.atlassian.jira.tenancy.PluginKeyPredicateLoader;
import com.atlassian.plugin.ModuleDescriptorFactory;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginInformation;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.plugin.event.events.PluginUninstalledEvent;
import com.atlassian.plugin.loaders.PluginLoader;
import com.atlassian.plugin.manager.PluginPersistentStateStore;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/** @since v3.13 */
public class TestJiraPluginManager
{
    private static void assertNewDate(final long time)
    {
        assertThat(time, Matchers.greaterThan(System.currentTimeMillis() - 1000));
    }

    private static Plugin getMockPlugin(final String key, final String name, final String version)
    {
        final PluginInformation pluginInfo = mock(PluginInformation.class);
        when(pluginInfo.getVersion()).thenReturn(version);

        final Plugin mockPlugin = mock(Plugin.class);
        when(mockPlugin.getKey()).thenReturn(key);
        when(mockPlugin.getName()).thenReturn(name);
        when(mockPlugin.getPluginInformation()).thenReturn(pluginInfo);
        return mockPlugin;
    }

    private File installedPluginsDirectory;
    private JiraPluginManager jiraPluginManager;

    @Mock private JiraStartupPluginSystemListener mockPluginSystemListener;
    @Mock private ModuleDescriptorFactory mockModuleDescriptorFactory;
    @Mock private PluginEventManager mockPluginEventManager;
    @Mock private PluginLoaderFactory mockPluginLoaderFactory;
    @Mock private PluginPath mockPluginPath;
    @Mock private PluginPersistentStateStore mockPluginStateStore;
    @Mock private PluginVersionStore mockPluginVersionStore;
    @Mock private JiraFailedPluginTracker failedPluginTracker;
    @Mock private PluginKeyPredicateLoader mockPluginKeyPredicateLoader;

    @Before
    public void setUp() throws IOException
    {
        MockitoAnnotations.initMocks(this);
        when(mockPluginLoaderFactory.getPluginLoaders()).thenReturn(Collections.<PluginLoader>emptyList());
        setUpInstalledPluginsDirectory();
        jiraPluginManager = new JiraPluginManager(mockPluginStateStore, mockPluginLoaderFactory, mockModuleDescriptorFactory,
                mockPluginVersionStore, mockPluginEventManager, mockPluginPath, mockPluginKeyPredicateLoader,
                mockPluginSystemListener, failedPluginTracker);
    }

    private void setUpInstalledPluginsDirectory() throws IOException
    {
        installedPluginsDirectory = new File(System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + "installed-plugins");
        //noinspection ResultOfMethodCallIgnored
        installedPluginsDirectory.createNewFile();
        when(mockPluginPath.getInstalledPluginsDirectory()).thenReturn(installedPluginsDirectory);
    }

    @After
    public void tearDown() throws Exception
    {
        //noinspection ResultOfMethodCallIgnored
        installedPluginsDirectory.delete();
    }

    @Test
    public void testGetVersionsByKey()
    {
        // Set up
        final PluginVersion pluginVersion1 = new PluginVersionImpl("key1", "name1", "version1", new Date());
        final PluginVersion pluginVersion2 = new PluginVersionImpl("key2", "name2", "version2", new Date());
        when(mockPluginVersionStore.getAll()).thenReturn(Arrays.asList(pluginVersion1, pluginVersion2));

        // Invoke
        final Map versionsByKey = jiraPluginManager.getPluginVersionsByKey();

        // Check
        assertEquals(2, versionsByKey.size());
        assertEquals(pluginVersion1, versionsByKey.get("key1"));
        assertEquals(pluginVersion2, versionsByKey.get("key2"));
    }

    @Test
    public void testDeletePluginVersions()
    {
        // Set up
        final long id1 = 1;
        final long id2 = 2;
        when(mockPluginVersionStore.delete(id1)).thenReturn(true);
        when(mockPluginVersionStore.delete(id2)).thenReturn(true);
        final List<PluginVersion> versionsToDelete = new ArrayList<PluginVersion>();
        versionsToDelete.add(new PluginVersionImpl(id1, "key1", "name1", "version1", new Date()));
        versionsToDelete.add(new PluginVersionImpl(id2, "key2", "name2", "version2", new Date()));

        // Invoke
        jiraPluginManager.deletePluginVersions(versionsToDelete);

        // Check
        verify(mockPluginVersionStore).delete(id1);
        verify(mockPluginVersionStore).delete(id2);
        verifyNoMoreInteractions(mockPluginVersionStore);
    }

    @Test
    public void testStorePluginVersionCreateVersion()
    {
        // Set up
        final Plugin mockPlugin = getMockPlugin("key2", "name2", "version2");

        // Invoke
        jiraPluginManager.storePluginVersion(mockPlugin, null);

        // Check
        final ArgumentCaptor<PluginVersion> pluginVersionCaptor = ArgumentCaptor.forClass(PluginVersion.class);
        verify(mockPluginVersionStore).create(pluginVersionCaptor.capture());
        final PluginVersion pluginVersion = pluginVersionCaptor.getValue();
        assertEquals("key2", pluginVersion.getKey());
        assertEquals("name2", pluginVersion.getName());
        assertEquals("version2", pluginVersion.getVersion());
        assertNewDate(pluginVersion.getCreated().getTime());
    }

    @Test
    public void testStorePluginVersionUpdateVersion()
    {
        // Set up
        final PluginVersionImpl pluginVersion1 = new PluginVersionImpl(1L, "key1", "name1", "version1", new Date());
        final Plugin mockPlugin = getMockPlugin("key1", "name1", "version3");

        // Invoke
        jiraPluginManager.storePluginVersion(mockPlugin, pluginVersion1);

        // Check
        final ArgumentCaptor<PluginVersion> pluginVersionCaptor = ArgumentCaptor.forClass(PluginVersion.class);
        verify(mockPluginVersionStore).update(pluginVersionCaptor.capture());
        final PluginVersion pluginVersion = pluginVersionCaptor.getValue();
        assertEquals("key1", pluginVersion.getKey());
        assertEquals("name1", pluginVersion.getName());
        assertEquals("version3", pluginVersion.getVersion());
    }

    @Test
    public void testStorePluginVersions()
    {
        // Set up
        final AtomicBoolean deletePluginVersionsCalled = new AtomicBoolean(false);
        final AtomicBoolean getPluginsCalled = new AtomicBoolean(false);
        final AtomicBoolean getPluginsVersionsByKeyCalled = new AtomicBoolean(false);
        final AtomicBoolean storePluginVersionCalled = new AtomicBoolean(false);

        final JiraPluginManager jiraPluginManager = new JiraPluginManager(mockPluginStateStore,
            mockPluginLoaderFactory, mockModuleDescriptorFactory, null, mockPluginEventManager, mockPluginPath,
                mockPluginKeyPredicateLoader, mockPluginSystemListener, failedPluginTracker)
        {
            @Override
            void deletePluginVersions(final Collection pluginVersionsToDelete)
            {
                deletePluginVersionsCalled.set(true);
            }

            @Override
            public Collection<Plugin> getPlugins()
            {
                getPluginsCalled.set(true);
                final Plugin mockPlugin = getMockPlugin("key2", "name2", "version3");
                return Collections.singleton(mockPlugin);
            }

            @Override
            Map<String, PluginVersion> getPluginVersionsByKey()
            {
                getPluginsVersionsByKeyCalled.set(true);
                return new LinkedHashMap<String, PluginVersion>()
                {{
                        put("key1", new PluginVersionImpl(1L, "key1", "name1", "version1", new Date()));
                        put("key2", new PluginVersionImpl(1L, "key2", "name2", "version2", new Date()));

                }};
            }

            @Override
            void storePluginVersion(final Plugin plugin, final PluginVersion pluginVersion)
            {
                assertEquals("key2", plugin.getKey());
                storePluginVersionCalled.set(true);
            }
        };

        // Invoke
        jiraPluginManager.storePluginVersions();

        // Check
        assertTrue((deletePluginVersionsCalled.get()));
        assertTrue(getPluginsCalled.get());
        assertTrue(getPluginsVersionsByKeyCalled.get());
        assertTrue(storePluginVersionCalled.get());
    }

    @Test
    public void handlingPluginEnabledEventShouldCausePluginVersionToBeStored()
    {
        // Set up
        final PluginEnabledEvent mockEvent = mock(PluginEnabledEvent.class);
        final String key = "key4";
        final String name = "name4";
        final String version = "version4";
        final Plugin mockPlugin = getMockPlugin(key, name, version);
        when(mockEvent.getPlugin()).thenReturn(mockPlugin);

        // Invoke
        jiraPluginManager.onPluginEnabledEvent(mockEvent);

        // Check
        final ArgumentCaptor<PluginVersion> pluginVersionCaptor = ArgumentCaptor.forClass(PluginVersion.class);
        verify(mockPluginVersionStore).save(pluginVersionCaptor.capture());
        final PluginVersion pluginVersion = pluginVersionCaptor.getValue();
        assertEquals(key, pluginVersion.getKey());
        assertEquals(name, pluginVersion.getName());
        assertEquals(version, pluginVersion.getVersion());
        assertNewDate(pluginVersion.getCreated().getTime());
    }

    @Test
    public void handlingPluginUninstalledEventShouldCausePluginVersionToBeDeleted()
    {
        // Set up
        final PluginUninstalledEvent mockEvent = mock(PluginUninstalledEvent.class);
        final String key = "key5";
        final Plugin mockPlugin = getMockPlugin(key, "anyName", "anyVersion");
        when(mockEvent.getPlugin()).thenReturn(mockPlugin);

        // Invoke
        jiraPluginManager.onPluginUninstalledEvent(mockEvent);

        // Check
        verify(mockPluginVersionStore).deleteByKey(key);
    }
}
