package com.atlassian.jira.plugin.decorator;

import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.plugin.PluginInjector;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.google.common.annotations.VisibleForTesting;
import com.opensymphony.module.sitemesh.Config;
import com.opensymphony.module.sitemesh.DecoratorMapper;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import java.util.Properties;

/**
 * Module descriptor for Sitemesh decorator mappers
 */
public class DecoratorMapperModuleDescriptor extends AbstractModuleDescriptor<DecoratorMapper>
{
    private DecoratorMapper decoratorMapper;
    private static final Logger log = Logger.getLogger(DecoratorModuleDescriptor.class);

    public DecoratorMapperModuleDescriptor(ModuleFactory moduleFactory)
    {
        super(moduleFactory);
    }

    public void init(Plugin plugin, Element element) throws PluginParseException
    {
        super.init(plugin, element);
        if (element.attributeValue("class") == null)
        {
            throw new PluginParseException("decorator-mapper modules must have a class specified");
        }
    }

    /**
     * See getDecoratorMapper() for why this returns null
     *
     * @return null
     */
    public DecoratorMapper getModule()
    {
        return null;
    }

    /**
     * Get the decorator mapper for this module.  This method will return an initialised decorator mapper.  In order to
     * initialise it though, it needs a parent decorator mapper and configuration, that's why getModule() isn't used.
     *
     * @param config The Sitemesh configuration
     * @param parent The parent decorator mapper
     * @return An initialised decorator mapper
     */
    @ClusterSafe("Local. We are just manipulating the sitemesh configuration in the local web app.")
    public synchronized DecoratorMapper getDecoratorMapper(Config config, DecoratorMapper parent)
    {
        if (decoratorMapper == null)
        {
            decoratorMapper = createDecoratorMapper();
            try
            {
                decoratorMapper.init(config, new Properties(), parent);
            }
            catch (InstantiationException ie)
            {
                log.error("Error initialising decorator mapper in plugin " + getKey(), ie);
                decoratorMapper = null;
            }
        }
        return decoratorMapper;
    }

    @VisibleForTesting
    DecoratorMapper createDecoratorMapper()
    {
        return PluginInjector.newInstance(getModuleClass(), plugin);
    }
}