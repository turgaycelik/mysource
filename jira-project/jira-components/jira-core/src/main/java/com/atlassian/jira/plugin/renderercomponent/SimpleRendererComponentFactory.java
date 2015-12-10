package com.atlassian.jira.plugin.renderercomponent;

import com.atlassian.jira.plugin.PluginInjector;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.renderer.v2.components.PluggableRendererComponentFactory;
import com.atlassian.renderer.v2.components.RendererComponent;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * Simple component factory that loads a renderer component specified as a parameter in the plugin descriptor.
 *
 * @since v3.12
 */
public class SimpleRendererComponentFactory implements PluggableRendererComponentFactory
{
    private static final String RENDERER_COMPONENT_CLASS_PARAM = "rendererComponentClass";
    private RendererComponent rendererComponent;
    private Class<RendererComponent> rendererComponentClass;
    private Plugin plugin;

    public void init(ModuleDescriptor moduleDescriptor) throws PluginParseException
    {
        this.plugin = moduleDescriptor.getPlugin();

        final RendererComponentFactoryDescriptor descriptor = (RendererComponentFactoryDescriptor) moduleDescriptor;
        final Map params = descriptor.getParams();
        if (params == null || StringUtils.isEmpty((String) params.get(RENDERER_COMPONENT_CLASS_PARAM)))
        {
            throw new PluginParseException("The plugin with key '" + descriptor.getCompleteKey() + "' is missing the required '"
                      + RENDERER_COMPONENT_CLASS_PARAM + "' parameter.");
        }
        else
        {
            final String rendererComponentClassName = (String) params.get(RENDERER_COMPONENT_CLASS_PARAM);
            try
            {
                rendererComponentClass = moduleDescriptor.getPlugin().loadClass(rendererComponentClassName, getClass());
            }
            catch (ClassNotFoundException e)
            {
                throw new PluginParseException("Could not load renderer component with class '" + rendererComponentClassName
                                           + "' for plugin with key '" + descriptor.getCompleteKey() + "'", e);
            }
        }
    }

    public RendererComponent getRendererComponent()
    {
        // NOTE: We need to load the renderer component after the init so that any renderer components that depend
        // on other managers can be injected with them. If it is instantiated in the init method the ComponentManager
        // has not yet had a chance to be created.
        if (rendererComponent == null)
        {
            try
            {
                rendererComponent = loadRendererComponent(rendererComponentClass);
            }
            // This should never happen since we validated in in the init
            catch (ClassNotFoundException e)
            {
                throw new RuntimeException(e);
            }
        }
        return rendererComponent;
    }

    RendererComponent loadRendererComponent(Class<RendererComponent> rendererComponentClass)
            throws ClassNotFoundException
    {
        if (plugin == null)
        {
            throw new IllegalStateException("SimpleRendererComponentFactory has not been initialised.");
        }
        // Construct the object by injecting with Spring or Pico as appropriate.
        return PluginInjector.newInstance(rendererComponentClass, plugin);
    }
}
