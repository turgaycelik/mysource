package com.atlassian.jira.plugin;

import com.atlassian.plugin.AutowireCapablePlugin;
import com.atlassian.plugin.Plugin;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @since v4.4.4
 */
public class TestPluginInjector
{
    private AutowireablePlugin autowireCapablePlugin;

    @Before
    public void setUp()
    {
        autowireCapablePlugin = mock(AutowireablePlugin.class);
    }

    @Test
    public void testAutowireCapablePlugin()
    {
        Object answer = new Object();

        when(autowireCapablePlugin.autowire(eq(Object.class))).thenReturn(answer);

        Object o = PluginInjector.newInstance(Object.class, autowireCapablePlugin);

        assertSame(answer, o);

        verify(autowireCapablePlugin).autowire(eq(Object.class));
    }

    private interface AutowireablePlugin extends AutowireCapablePlugin, Plugin
    {
    }

}
