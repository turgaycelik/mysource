package com.atlassian.jira.plugin.renderercomponent;

import com.atlassian.jira.util.JiraUtils;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.renderer.v2.V2SubRenderer;
import com.atlassian.renderer.v2.components.PluggableRendererComponentFactory;
import com.atlassian.renderer.v2.components.RendererComponent;
import com.atlassian.renderer.v2.components.block.BlockRenderer;
import com.atlassian.renderer.v2.components.block.BlockRendererComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Simple component factory that loads a renderer component specified as a parameter in the plugin descriptor.
 *
 * @since v3.12
 */
public class BlockRendererComponentFactory implements PluggableRendererComponentFactory
{
    private RendererComponent rendererComponent;
    private static final String BLOCKRENDERERS_PARAM = "blockrenderers";
    private final V2SubRenderer subRenderer;

    public BlockRendererComponentFactory(V2SubRenderer subRenderer)
    {
        this.subRenderer = subRenderer;
    }

    public void init(ModuleDescriptor moduleDescriptor) throws PluginParseException
    {
        final RendererComponentFactoryDescriptor descriptor = (RendererComponentFactoryDescriptor) moduleDescriptor;

        final Map listParams = descriptor.getListParams();
        if(!listParams.containsKey(BLOCKRENDERERS_PARAM))
        {
            throw new PluginParseException("Could not load block renderer component factory with key '" + descriptor.getCompleteKey() +
                                           "'. Missing required list-param '" + BLOCKRENDERERS_PARAM + "'");
        }

        final List blockRendererClasses = (List) listParams.get(BLOCKRENDERERS_PARAM);
        final List blockRendererObjects = new ArrayList();

        for (final Object blockRendererClass1 : blockRendererClasses)
        {
            String blockRendererClass = (String) blockRendererClass1;
            try
            {
                blockRendererObjects.add(loadBlockRenderer(blockRendererClass, moduleDescriptor.getPlugin()));
            }
            catch (ClassNotFoundException e)
            {
                //if a particular blockRendererClass can't be loaded the plugin initialisation should fail
                throw new PluginParseException("Could not load block renderer class '" + blockRendererClass +
                        "' for renderer component plugin with key '" + descriptor.getCompleteKey() + "'", e);
            }
        }
        rendererComponent = new BlockRendererComponent(subRenderer, blockRendererObjects);
    }

    BlockRenderer loadBlockRenderer(String blockRendererClassName, Plugin plugin)
            throws ClassNotFoundException
    {
        Class blockRendererClass = plugin.loadClass(blockRendererClassName, getClass());
        return (BlockRenderer) JiraUtils.loadComponent(blockRendererClass);
    }

    public RendererComponent getRendererComponent()
    {
        return rendererComponent;
    }
}
