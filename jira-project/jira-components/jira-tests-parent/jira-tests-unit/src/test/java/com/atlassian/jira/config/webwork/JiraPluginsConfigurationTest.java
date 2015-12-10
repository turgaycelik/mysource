package com.atlassian.jira.config.webwork;

import java.util.Collection;

import com.atlassian.jira.mock.plugin.MockPlugin;
import com.atlassian.jira.plugin.webwork.WebworkModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.module.ModuleFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JiraPluginsConfigurationTest
{

    private PluginAccessor pluginAccessor;
    private PluginEventManager pluginEventManager;
    private JiraPluginsConfiguration jiraPluginsConfiguration;

    @Before
    public void setUp() throws Exception
    {
        pluginEventManager = mock(PluginEventManager.class);
        pluginAccessor = mock(PluginAccessor.class);

        WebworkModuleDescriptor webworkModuleDescriptor = new WebworkModuleDescriptor(null, null, ModuleFactory.LEGACY_MODULE_FACTORY)
        {
            @Override
            public Object getImpl(String aName) throws IllegalArgumentException
            {
                return aName;
            }
        };

        when(pluginAccessor.getEnabledModuleDescriptorsByClass(WebworkModuleDescriptor.class)).thenReturn(ImmutableList.of(webworkModuleDescriptor));

        final Collection<Plugin> mockPlugins = Lists.<Plugin>newArrayList(new MockPlugin("name", "key", null));
        when(pluginAccessor.getPlugins()).thenReturn(mockPlugins);

        jiraPluginsConfiguration = new JiraPluginsConfiguration()
        {
            @Override
            PluginAccessor getPluginAccessor()
            {
                return pluginAccessor;
            }

            @Override
            PluginEventManager getPluginEventManager()
            {
                return pluginEventManager;
            }
        };
    }

    @Test
    public void testGetImpl() throws Exception
    {
        try
        {
            jiraPluginsConfiguration.getImpl("webwork.anything");
            Assert.fail("Should not except webwork prefixed parameters");
        }
        catch (IllegalArgumentException expected)
        {
            assertThat(expected.getMessage(), containsString("No such setting"));
        }

        Object value = jiraPluginsConfiguration.getImpl("something.else");
        assertEquals("something.else", value);
    }

}
