package com.atlassian.jira.web.dispatcher;

import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.webwork.WebworkModuleDescriptor;
import com.atlassian.plugin.event.events.PluginModuleDisabledEvent;
import com.atlassian.util.concurrent.LazyReference;
import webwork.dispatcher.ConfigurationViewMapping;
import webwork.dispatcher.DynamicViewMapping;
import webwork.dispatcher.ViewMapping;

import java.util.Map;

import static com.atlassian.jira.component.ComponentAccessor.getComponentOfType;


/**
 * <p>Resolves a mapping from action result code to a particular view that should be used to present the results to the
 * end user.</p>
 *
 * <p>It delegates the actual handling of webwork plugin module events to
 * {@link PluginsAwareViewMapping.Component}</p>
 *
 * <p>This separate component is <strong>necessary</strong> because the mapping is instantiated and stored statically by
 * webwork's {@link webwork.dispatcher.GenericDispatcher}</p>
 *
 * @since v4.4
 */
public class PluginsAwareViewMapping implements ViewMapping
{
    /**
     * <p>Retrieves a delegate component that lives in the container and is able to initialise itself when the container
     * is restarted.</p>
     *
     * <p>This <strong>needs to be looked up every time </strong> because the mapping object survives pico restarts
     * so we need to  make sure we always retrieve the current component from the container.</p>
     *
     * @return A delegate which is aware of the lifecycle of the pico container.
     */
    ViewMapping getDelegateComponent()
    {
        return getComponentOfType(PluginsAwareViewMapping.Component.class);
    }

    @Override
    public Object getView(final String anActionName, final String aViewName)
    {
        return getDelegateComponent().getView(anActionName, aViewName);
    }

    /**
     * A component that reacts to plugin events and clears the current cache of view mappings when webwork plugin module
     * descriptors are enabled / disabled.
     */
    @EventComponent
    public static class Component implements ViewMapping
    {
        // This needs to be lazy because we can not create the object when pico is starting up, this is because the view
        // mapping looks up values in webwork's configuration which is not setup until pico has finished starting :-(
        @ClusterSafe("Driven by plugin state, which is kept in synch across the cluster")
        private final LazyReference<ResettableDynamicViewMapping> resettableDynamicViewMapping =
                new LazyReference<ResettableDynamicViewMapping>()
        {
            @Override
            protected ResettableDynamicViewMapping create() throws Exception
            {
                return new ResettableDynamicViewMapping(new ConfigurationViewMapping());
            }
        };

        /**
         * A dynamic view mapping that exposes the ability to retrieve its internal cache of view mappings. We expose
         * this cache so that we can clear it on plugin events.
         */
        private static class ResettableDynamicViewMapping extends DynamicViewMapping
        {
            public ResettableDynamicViewMapping(final ViewMapping aDelegate)
            {
                super(aDelegate);
            }

            Map getCache()
            {
                return cache;
            }
        }

        @Override
        public Object getView(final String anActionName, final String aViewName)
        {
            return resettableDynamicViewMapping.get().getView(anActionName, aViewName);
        }

        @EventListener
        public void onPluginModuleDisabled(final PluginModuleDisabledEvent event)
        {
            if (event.getModule() instanceof WebworkModuleDescriptor)
            {
                resettableDynamicViewMapping.get().getCache().clear();
            }
        }
    }
}
