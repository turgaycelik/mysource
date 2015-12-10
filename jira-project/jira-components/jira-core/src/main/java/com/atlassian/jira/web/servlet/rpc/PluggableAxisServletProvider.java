package com.atlassian.jira.web.servlet.rpc;

import javax.servlet.ServletConfig;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.plugin.rpc.SoapModuleDescriptor;
import com.atlassian.jira.soap.axis.JiraAxisServerFactory;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.event.events.PluginModuleDisabledEvent;
import com.atlassian.plugin.event.events.PluginModuleEnabledEvent;
import com.atlassian.util.concurrent.ResettableLazyReference;

import org.apache.axis.AxisProperties;
import org.apache.axis.encoding.TypeMappingImpl;
import org.apache.axis.transport.http.AxisServlet;

import static com.atlassian.jira.util.dbc.Assertions.stateTrue;

/**
 * Plugin system based implementation of {@link com.atlassian.jira.web.servlet.rpc.AxisServletProvider}.
 *
 * @since v4.4
 */
public class PluggableAxisServletProvider implements AxisServletProvider
{

    private final EventPublisher eventPublisher;
    @ClusterSafe("Driven by plugin state, which is kept in synch across the cluster")
    private volatile Holder holder;

    public PluggableAxisServletProvider(EventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
    }

    public void init(ServletConfig config)
    {
        if (!isInitialized())
        {
            holder = new Holder(config);
            eventPublisher.register(holder);
        }
    }

    @Override
    public void reset()
    {
        if (isInitialized())
        {
            holder.reset();
        }
    }

    @Override
    public AxisServlet getAxisServlet()
    {
        stateTrue("Not initialized", isInitialized());
        return holder.get();
    }

    /**
     * This only checks if the holder is initialized (i.e. init() has been called), and not the lazy loaded servlet instance!
     *
     * @return <code>true</code> if init() has been called, <code>false</code> otherwise
     */
    private boolean isInitialized()
    {
        return holder != null;
    }

    public static final class Holder extends ResettableLazyReference<AxisServlet>
    {
        private final ServletConfig config;

        private Holder(ServletConfig config)
        {
            this.config = config;
        }

        @Override
        protected AxisServlet create() throws Exception {
            AxisProperties.setProperty("axis.ServerFactory", JiraAxisServerFactory.class.getName());
            AxisProperties.setProperty("axis.doAutoTypes", Boolean.TRUE.toString());
            TypeMappingImpl.dotnet_soapenc_bugfix = true;
            AxisServlet axisServlet = new AxisServlet();
            axisServlet.init(config);
            return axisServlet;
        }

        @Override
        public void reset() {
            AxisServlet current = null;
            if (isInitialized())
            {
                current = get();
            }
            super.reset();
            if (current != null)
            {
                current.destroy();
            }
        }

        @EventListener
        public void onPluginModuleEnabled(PluginModuleEnabledEvent event)
        {
            onPluginModuleEvent(event.getModule());
        }

        @EventListener
        public void onPluginModuleDisabled(PluginModuleDisabledEvent event)
        {
            onPluginModuleEvent(event.getModule());
        }

        private void onPluginModuleEvent(ModuleDescriptor<?> moduleDescriptor)
        {
            if (moduleDescriptor instanceof SoapModuleDescriptor)
            {
                reset();
            }
        }

    }
}
