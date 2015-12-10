package com.atlassian.jira.issue.fields.renderer.wiki;

import com.atlassian.event.api.EventListener;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.renderercomponent.RendererComponentFactoryDescriptor;
import com.atlassian.jira.plugin.util.ModuleDescriptorComparator;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.events.PluginModuleDisabledEvent;
import com.atlassian.plugin.event.events.PluginModuleEnabledEvent;
import com.atlassian.renderer.RendererConfiguration;
import com.atlassian.renderer.embedded.EmbeddedResourceRenderer;
import com.atlassian.renderer.links.LinkRenderer;
import com.atlassian.renderer.v2.MutableRenderer;
import com.atlassian.renderer.v2.Renderer;
import com.atlassian.renderer.v2.V2RendererFacade;
import com.atlassian.renderer.v2.components.PluggableRendererComponentFactory;
import com.atlassian.renderer.v2.components.RendererComponent;
import com.atlassian.util.concurrent.ResettableLazyReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Creates a wiki renderer.
 */
public class WikiRendererFactory
{
    private static final Logger logger = LoggerFactory.getLogger(WikiRendererFactory.class);

    private final ResettableLazyReference<V2RendererFacade> wikiRendererRef = new ResettableLazyReference<V2RendererFacade>()
    {
        @Override
        protected V2RendererFacade create() throws Exception
        {
            initializeComponents();
            return new V2RendererFacade(ComponentAccessor.getComponent(RendererConfiguration.class),
                    ComponentAccessor.getComponent(LinkRenderer.class),
                    ComponentAccessor.getComponent(EmbeddedResourceRenderer.class),
                    ComponentAccessor.getComponent(Renderer.class));
        }

        private void initializeComponents()
        {
            final ArrayList<RendererComponent> components = new ArrayList<RendererComponent>();
            final MutableRenderer renderer = (MutableRenderer) ComponentAccessor.getComponent(Renderer.class);

            final PluginAccessor pluginAccessor = ComponentAccessor.getComponentOfType(PluginAccessor.class);
            final List<RendererComponentFactoryDescriptor> descriptors = new ArrayList<RendererComponentFactoryDescriptor>(pluginAccessor.getEnabledModuleDescriptorsByClass(RendererComponentFactoryDescriptor.class));

            Collections.sort(descriptors, ModuleDescriptorComparator.COMPARATOR);

            for (final RendererComponentFactoryDescriptor descriptor : descriptors)
            {
                PluggableRendererComponentFactory rendererComponentFactory = descriptor.getModule();
                if (rendererComponentFactory != null)
                {
                    RendererComponent rendererComponent = rendererComponentFactory.getRendererComponent();
                    if (rendererComponent != null)
                    {
                        components.add(rendererComponent);
                    }
                    else
                    {
                        logger.warn("Renderer component factory " + rendererComponentFactory + " returned null renderer component");
                    }
                }
                else
                {
                    logger.warn("Got null renderer component factory module from descriptor " + descriptor);
                }
            }
            // TokenRendererComponent must always appear at the end of the list of renderers as it does substitutions
            // that other renderers can specify.
            Collections.sort(components, TokenRendererAwareRendererComparator.COMPARATOR);
            // now change the state of the renderer so that it is useful
            renderer.setComponents(components);
        }
    };

    public V2RendererFacade getWikiRenderer()
    {
        return wikiRendererRef.get();
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

    private void onPluginModuleEvent(ModuleDescriptor<?> descriptor)
    {
        if (descriptor instanceof RendererComponentFactoryDescriptor)
        {
            // refresh underlying renderer components
            wikiRendererRef.reset();
        }
    }

}
