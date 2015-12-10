package com.atlassian.jira.startup;

import java.util.ArrayList;
import java.util.List;

import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.plugin.MockPlugin;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginInformation;
import com.atlassian.plugin.PluginState;
import com.atlassian.plugin.impl.UnloadablePlugin;
import com.atlassian.plugin.metadata.PluginMetadataManager;

import com.google.common.collect.Iterables;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestPluginInfoProviderImpl extends MockControllerTestCase
{
    private List<Plugin> plugins;
    private PluginAccessor pluginAccessor;
    private PluginMetadataManager pluginMetadataManager;
    private PluginInformation pluginInformation;

    @Before
    public void setUp()
    {
        pluginInformation = new PluginInformation();
        plugins = new ArrayList<Plugin>();
        plugins.add(new MockPlugin("zplugin1", "com.atlassian.jira.plugin1", pluginInformation, PluginState.DISABLED));
        plugins.add(new MockPlugin("aplugin2", "com.atlassian.jira.plugin2", pluginInformation, PluginState.ENABLED));
        plugins.add(new UnloadablePlugin("unloadReasonText"));

        pluginAccessor = getMock(PluginAccessor.class);
        pluginMetadataManager = getMock(PluginMetadataManager.class);
    }

    @Test
    public void testGetSystemPlugins() throws Exception
    {
        expect(pluginMetadataManager.isUserInstalled(this.<Plugin>anyObject())).andStubReturn(false);
        expect(pluginAccessor.getPlugins()).andStubReturn(plugins);
        final PluginInfoProviderImpl infoProvider = instantiate(PluginInfoProviderImpl.class);

        final PluginInfos systemPlugins = infoProvider.getSystemPlugins();

        standardAsserts(systemPlugins, true);
    }

    @Test
    public void testGetUserPlugins() throws Exception
    {
        expect(pluginMetadataManager.isUserInstalled(this.<Plugin>anyObject())).andStubReturn(true);
        expect(pluginAccessor.getPlugins()).andStubReturn(plugins);
        final PluginInfoProviderImpl infoProvider = instantiate(PluginInfoProviderImpl.class);

        final PluginInfos userPlugins = infoProvider.getUserPlugins();

        standardAsserts(userPlugins, false);
    }

    private void standardAsserts(PluginInfos plugins, final boolean isSystemPlugin)
    {
        assertNotNull(plugins);
        assertEquals(3, plugins.size());

        PluginInfo info;

        info = Iterables.get(plugins,0);
        assertNotNull(info);
        assertEquals(true, info.getName().startsWith("Unknown"));
        assertEquals(true, info.getKey().startsWith("Unknown"));
        assertEquals(isSystemPlugin, info.isSystemPlugin());
        assertEquals(false, info.isEnabled());

        info = Iterables.get(plugins,1);
        assertNotNull(info);
        assertEquals("aplugin2", info.getName());
        assertEquals("com.atlassian.jira.plugin2", info.getKey());
        assertEquals(isSystemPlugin, info.isSystemPlugin());
        assertEquals(true, info.isEnabled());
        assertEquals(pluginInformation, info.getPluginInformation());

        info = Iterables.get(plugins,2);
        assertNotNull(info);
        assertEquals("zplugin1", info.getName());
        assertEquals("com.atlassian.jira.plugin1", info.getKey());
        assertEquals(isSystemPlugin, info.isSystemPlugin());
        assertEquals(false, info.isEnabled());
        assertEquals(pluginInformation, info.getPluginInformation());
    }

}
