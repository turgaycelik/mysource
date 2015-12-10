package com.atlassian.jira.config.webwork;

import com.atlassian.event.api.EventListener;
import com.atlassian.jira.InfrastructureException;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.plugin.webwork.WebworkModuleDescriptor;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.event.events.PluginModuleDisabledEvent;
import com.atlassian.plugin.event.events.PluginModuleEnabledEvent;
import webwork.action.factory.ActionFactory;
import webwork.config.Configuration;
import webwork.config.ConfigurationInterface;
import webwork.config.DefaultConfiguration;
import webwork.util.WebworkCacheControl;

/**
 * Provides a way for JIRA to configure Webwork to lookup action classes by setting the <code>TypeResolver</code>.
 *
 * @since v3.13
 */
public class WebworkConfigurator implements Startable
{
    private final ClassLoader applicationClassLoader;
    private final PluginEventManager pluginEventManager;
    private final ActionConfigResetListener configResetListener = new ActionConfigResetListener();

    public WebworkConfigurator(final ClassLoader applicationClassLoader, final PluginEventManager pluginEventManager)
    {
        this.applicationClassLoader = applicationClassLoader;
        this.pluginEventManager = pluginEventManager;
    }

    public void start() throws Exception
    {
        setupConfiguration();
        setupActionFactory();
        /*
         * Static control of whether webwork things are cached or not.  We had problems in webwork
         * where strings such as "com.atlassian.jira.ViewIssue@34128" would be cached as
         * webwork EL.  Profiling has revealed that out of 2000ms of stack.findValue() calls,
         * then only 18ms of that was parsing the EL strings.  So we can choose to not
         * cache them.
         *
         * This class is a weasel class so that I can externalise the 'putting' to the static cache
         * or not.  If we run with this as off for longer enough you can do away with this
         * and make Query not cache anything.
         */
        WebworkCacheControl.setCacheQueries(false);
        WebworkCacheControl.setCacheSimpleTests(false);

        pluginEventManager.register(configResetListener);
    }

    public static void setupConfiguration()
    {
        ClassLoader previousCL = Thread.currentThread().getContextClassLoader();

        // set the TCCL before calling into Webwork. this code ends up getting called in one of Felix's shutdown
        // threads when the TCCL is not set at all.
        Thread.currentThread().setContextClassLoader(WebworkConfigurator.class.getClassLoader());
        try
        {
            configureWebwork();
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(previousCL);
        }
    }

    private static void configureWebwork()
    {
        final ConfigurationInterface cfg = Configuration.getConfiguration();

        if (cfg instanceof DelegatingConfiguration)
        {
            // we've already set it before, system must be "resetting" so we need to reset the Configuration
            final DelegatingConfiguration delegatingConfiguration = (DelegatingConfiguration) cfg;
            //
            // wrap the configuration in a caching one
            delegatingConfiguration.setDelegateConfiguration(new DefaultConfiguration());
        }
        else
        {
            // BB I am unsure how we get into this part of the method.  It seems suss!  it doesnt come in here during startup
            // not during system reload.
            //
            // But leaving it it in for now but I strongly suspect its dead code
            //
            try
            {
                // only way to know if the real configuration has been set yet is to try and set it
                final ConfigurationInterface delegator = new DelegatingConfiguration(new DefaultConfiguration());
                //
                // wrap the configuration in a caching one
                Configuration.setConfiguration(delegator);
            }
            catch (final IllegalStateException e)
            {
                // thrown if the configuration has been set by someone else already and we can't set it to
                // something different, that's bad mojo
                throw new InfrastructureException("WebWork Configuration has already been set and can't be reset.");
            }
        }
    }

    private void setupActionFactory()
    {
        ActionFactory actionFactory = ActionFactory.getActionFactory();

        if (!(actionFactory instanceof JiraActionFactory))
        {
            try
            {
                // only way to know if the real configuration has been set yet is to try and set it
                actionFactory = new JiraActionFactory();
                ActionFactory.setActionFactory(actionFactory);
            }
            catch (final IllegalStateException e)
            {
                // thrown if the action factory has been set by someone else already and we can't set it to
                // something different, that's bad mojo
                throw new InfrastructureException(
                        "WebWork ActionFactory has already been set to something other than a JiraActionFactory and can't be reset.");
            }
        }

        ((JiraActionFactory) actionFactory).setPluginClassLoader(applicationClassLoader);
    }

    /**
     * This listener ensures that we don't leave any stale action configuration when webwork action plugin modules are
     * disabled or enabled.
     *
     */
    public final class ActionConfigResetListener
    {
        @EventListener
        public void onPluginModuleEnabled(final PluginModuleEnabledEvent event)
        {
            final ModuleDescriptor moduleDescriptor = event.getModule();
            if (moduleDescriptor instanceof WebworkModuleDescriptor)
            {
                clearAllConfigCaches();
            }
        }

        @EventListener
        public void onPluginModuleDisabled(final PluginModuleDisabledEvent event)
        {
            final ModuleDescriptor moduleDescriptor = event.getModule();
            if (moduleDescriptor instanceof WebworkModuleDescriptor)
            {
                clearAllConfigCaches();
            }
        }

        private void clearAllConfigCaches()
        {
            //reset the configuration to clear out any caches
            setupConfiguration();
            //and flush any caches in the action factories, to ensure they'll pick up any potential new action classes correctly!
            ActionFactory.getActionFactory().flushCaches();
        }
    }

}
