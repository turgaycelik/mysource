package com.atlassian.jira.web.sitemesh;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.decorator.DecoratorMapperModuleDescriptor;
import com.atlassian.jira.plugin.decorator.DecoratorModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.opensymphony.module.sitemesh.Decorator;
import com.opensymphony.module.sitemesh.DecoratorMapper;
import com.opensymphony.module.sitemesh.Page;
import com.opensymphony.module.sitemesh.mapper.AbstractDecoratorMapper;
import com.opensymphony.module.sitemesh.mapper.DefaultDecorator;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Sitemesh decorator mapper that looks up decorator modules and decorator mapper modules to get decorators from.
 */
public class PluginDecoratorMapper extends AbstractDecoratorMapper
{
    private static final Logger log = Logger.getLogger(PluginDecoratorMapper.class);

    public Decorator getDecorator(HttpServletRequest httpServletRequest, Page page)
    {
        PluginAccessor pluginAccessor = getPluginAccessor();
        if (pluginAccessor != null)
        {
            // Decorator mappers
            Decorator decorator = getDecoratorFromPluginMappers(httpServletRequest, page, pluginAccessor);
            if (decorator != null)
            {
                return decorator;
            }
            // Regex decorator templates
            decorator = getDecoratorFromPluginDecorators(httpServletRequest, pluginAccessor);
            if (decorator != null)
            {
                return decorator;
            }
        }
        return super.getDecorator(httpServletRequest, page);
    }

    private Decorator getDecoratorFromPluginDecorators(HttpServletRequest httpServletRequest,
        PluginAccessor pluginAccessor)
    {
        List<DecoratorModuleDescriptor> decoratorModuleDescriptors = pluginAccessor.getEnabledModuleDescriptorsByClass(DecoratorModuleDescriptor.class);
        for (final DecoratorModuleDescriptor desc : decoratorModuleDescriptors)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Trying decorator " + desc.getPluginKey() + ":" + desc.getKey() + " on servlet path " +
                          httpServletRequest.getServletPath());
            }
            if (desc.getPattern() != null)
            {
                Matcher matcher = desc.getPattern().matcher(httpServletRequest.getRequestURI().substring(httpServletRequest.getContextPath().length()));
                if (matcher.matches())
                {
                    if (log.isDebugEnabled())
                    {
                        log.debug("Matched decorator plugin with key " + desc.getPluginKey() + ":" + desc.getKey() +
                                  " on path " + httpServletRequest.getServletPath() +
                                  " using expression " + desc.getPattern() + ".  Decorating with page " + desc.getPage());
                    }
                    return createDefaultDecorator(desc);
                }
            }
        }
        return null;
    }

    private Decorator getDecoratorFromPluginMappers(HttpServletRequest httpServletRequest, Page page,
        PluginAccessor pluginAccessor)
    {
        List<DecoratorMapperModuleDescriptor> decoratorMapperModuleDescriptors = pluginAccessor.getEnabledModuleDescriptorsByClass(DecoratorMapperModuleDescriptor.class);
        for (final DecoratorMapperModuleDescriptor desc : decoratorMapperModuleDescriptors)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Trying mapper " + desc.getPluginKey() + ":" + desc.getKey() + " on servlet path " +
                          httpServletRequest.getServletPath());
            }
            DecoratorMapper decoratorMapper = desc.getDecoratorMapper(config, parent);
            if (decoratorMapper != null)
            {
                Decorator decorator = decoratorMapper.getDecorator(httpServletRequest, page);
                if (decorator != null)
                {
                    if (log.isDebugEnabled())
                    {
                        log.debug(
                                "Matched decorator mapper plugin with key " + desc.getPluginKey() + ":" + desc.getKey() +
                                " on path " + httpServletRequest.getServletPath() + ". Returning decorator named " +
                                decorator.getName());
                    }
                    return decorator;
                }
            }
            else
            {
                log.warn("Decorator mapper null: " + desc.getPluginKey() + ":" + desc.getKey());
            }
        }
        return null;
    }

    public Decorator getNamedDecorator(HttpServletRequest httpServletRequest, String name)
    {
        PluginAccessor pluginAccessor = getPluginAccessor();
        if (pluginAccessor != null)
        {
            // Decorator mappers
            List<DecoratorMapperModuleDescriptor> decoratorMapperModuleDescriptors = pluginAccessor.getEnabledModuleDescriptorsByClass(
                DecoratorMapperModuleDescriptor.class);
            for (final DecoratorMapperModuleDescriptor desc : decoratorMapperModuleDescriptors)
            {
                DecoratorMapper decoratorMapper = desc.getDecoratorMapper(config, parent);
                if (decoratorMapper != null)
                {
                    Decorator decorator = decoratorMapper.getNamedDecorator(httpServletRequest, name);
                    if (decorator != null)
                    {
                        if (log.isDebugEnabled())
                        {
                            log.debug("Decorator mapper " + desc.getPluginKey() + ":" + desc.getKey() +
                                      " returned decorator for name " + name);
                        }
                        return decorator;
                    }
                }
                else
                {
                    log.warn("Decorator mapper null: " + desc.getPluginKey() + ":" + desc.getKey());
                }
            }
            // Regex decorator templates
            List<DecoratorModuleDescriptor> decoratorModuleDescriptors = pluginAccessor.getEnabledModuleDescriptorsByClass(
                DecoratorModuleDescriptor.class);
            for (final DecoratorModuleDescriptor desc : decoratorModuleDescriptors)
            {
                if (name.equals(desc.getName()))
                {
                    if (log.isDebugEnabled())
                    {
                        log.debug("Decorator " + desc.getPluginKey() + ":" + desc.getKey() +
                                  " matched name " + name);
                    }
                    return createDefaultDecorator(desc);
                }

            }
        }
        return super.getNamedDecorator(httpServletRequest, name);
    }

    private Decorator createDefaultDecorator(DecoratorModuleDescriptor desc)
    {
        return new DefaultDecorator(desc.getName(), desc.getPage(), null);
    }

    PluginAccessor getPluginAccessor()
    {
        if (ComponentManager.getInstance() != null)
        {
            return ComponentAccessor.getPluginAccessor();
        }
        else
        {
            return null;
        }
    }
}
