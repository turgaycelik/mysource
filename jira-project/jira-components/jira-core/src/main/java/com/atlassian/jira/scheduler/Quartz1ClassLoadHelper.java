package com.atlassian.jira.scheduler;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.plugin.PluginAccessor;

import org.quartz.simpl.CascadingClassLoadHelper;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * So quartz can load plugin classes.
 *
 * @since v6.2
 */
public class Quartz1ClassLoadHelper extends CascadingClassLoadHelper
{
    @Override
    public void initialize()
    {
        final PluginAccessor pluginAccessor = notNull("pluginAccessor", ComponentAccessor.getComponent(PluginAccessor.class));
        final Thread thd = Thread.currentThread();
        final ClassLoader original = thd.getContextClassLoader();
        try
        {
            thd.setContextClassLoader(pluginAccessor.getClassLoader());
            super.initialize();
        }
        finally
        {
            thd.setContextClassLoader(original);
        }
    }
}
