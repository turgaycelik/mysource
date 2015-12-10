package com.atlassian.jira.config.webwork;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.webwork.WebworkModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.event.PluginEventListener;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.event.events.PluginFrameworkShutdownEvent;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import webwork.config.ConfigurationInterface;
import webwork.config.DelegatingConfiguration;
import webwork.config.WebworkConfigurationNotFoundException;

import java.util.Iterator;
import java.util.List;

/**
 * Delegating configuration that checks each WebworkModuleDescriptor for action names and action view mappings
 */
public class JiraPluginsConfiguration implements ConfigurationInterface
{
    private static final Logger log = Logger.getLogger(JiraPluginsConfiguration.class);

    private final PluginSystemAccessor pluginSystemAccessor = new PluginSystemAccessor();

    /**
     * Get a named setting.
     */
    public Object getImpl(final String aName) throws IllegalArgumentException
    {
        if (isBlackListedKey(StringUtils.defaultString(aName)))
        {
            throw new WebworkConfigurationNotFoundException(this.getClass(), "No such setting", aName);
        }
        return getConfig().getImpl(aName);
    }

    /**
     * We will be asked for keys such as webwork.action.extension, webwork.configuration.xml.reload,
     * webwork.ui.templateDir, webwork.ui.theme and so on and we are now stating that plugin webwork1 modules cannot
     * supply them.
     * <p/>
     * This will break things if some one has an action aliases as webwork.MyAction.jspa however we are willing to wear
     * this for Unicorn performance reasons.
     * <p/>
     * The reason things are so expensive is that the webwork "registry" is very flat and instantiated
     * ConfigurationInterface classes are put to the front of the list so they can override anything something else may
     * provide.  The downside is that they get called for secret webwork business keys such as
     * 'webwork.action.extension' and so on.  So we need to shortcut the lookup from running over the plugin module
     * descriptors to save cpu.
     *
     * @param aName the name of thr webwork config key
     * @return true if we are black listing that key and hence not calling onto WebworkModuleDescriptors
     */
    private boolean isBlackListedKey(String aName)
    {
        return aName.startsWith("webwork.");
    }


    /**
     * We don't support this and in fact no where in JIRA does
     */
    public void setImpl(final String aName, final Object aValue)
            throws IllegalArgumentException, UnsupportedOperationException
    {
        throw new UnsupportedOperationException("This configuration does not support updating a setting");
    }

    /**
     * List setting names
     */
    public Iterator listImpl()
    {
        return getConfig().listImpl();
    }

    private ConfigurationInterface getConfig()
    {
        return pluginSystemAccessor.getConfig();
    }

    PluginAccessor getPluginAccessor()
    {
        return ComponentAccessor.getComponentOfType(PluginAccessor.class);
    }

    PluginEventManager getPluginEventManager()
    {
        return ComponentAccessor.getComponentOfType(PluginEventManager.class);
    }

    private static class NullConfig implements ConfigurationInterface
    {
        private final String message;

        public NullConfig(final String message)
        {
            this.message = message;
        }

        public Object getImpl(final String aName) throws IllegalArgumentException
        {
            throw new IllegalArgumentException(message);
        }

        public void setImpl(final String aName, final Object aValue)
                throws IllegalArgumentException, UnsupportedOperationException
        {
            throw new UnsupportedOperationException("This configuration does not support updating a setting");
        }

        public Iterator listImpl()
        {
            throw new UnsupportedOperationException("This configuration does not support listing the settings");
        }
    }

    class PluginSystemAccessor
    {
        public class Listener
        {
            @PluginEventListener
            public void onShutdown(final PluginFrameworkShutdownEvent event)
            {
                config = null;
            }
        }

        final Listener shutdownListener = new Listener();
        volatile ConfigurationInterface config;

        PluginSystemAccessor()
        {
            getPluginEventManager().register(shutdownListener);
        }

        @ClusterSafe("This is purely a local concern")
        ConfigurationInterface getConfig()
        {
            synchronized (this)
            {
                if (config == null)
                {
                    final PluginAccessor pluginAccessor = getPluginAccessor();
                    //Orion 2.0.1 bug - the servlet is init'ed before the ServletContext has been initialised
                    //Therefore plugin configurations are null.  This bug appears to have been fixed in Orion 2.0.2
                    //To work around this bug - if there are no plugins available, then we probably aren't initialised yet.
                    if ((pluginAccessor.getPlugins() == null) || pluginAccessor.getPlugins().isEmpty())
                    {
                        if (config == null)
                        {
                            config = new NullConfig("JIRA plugin has not loaded yet - no properties available");
                        }
                    }
                    else
                    {
                        final List<WebworkModuleDescriptor> configurations = getWebworkPluginConfigurations();
                        if (configurations.isEmpty())
                        {
                            config = new NullConfig("No webwork plugins.");
                        }
                        else
                        {
                            config = new DelegatingConfiguration(configurations.toArray(new ConfigurationInterface[configurations.size()]));
                        }
                    }
                }
            }
            return config;
        }

        List<WebworkModuleDescriptor> getWebworkPluginConfigurations()
        {
            try
            {
                return getPluginAccessor().getEnabledModuleDescriptorsByClass(WebworkModuleDescriptor.class);
            }
            catch (final PluginParseException e)
            {
                log.error("Problem getting webwork module descriptors" + e, e);
                return null;
            }
        }
    }
}
