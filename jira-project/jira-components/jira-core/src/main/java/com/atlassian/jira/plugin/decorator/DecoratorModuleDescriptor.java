package com.atlassian.jira.plugin.decorator;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import org.dom4j.Element;

import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Module descriptor for Sitemesh decorators.  This module descriptor has a page, which is the name of a velocity
 * template resource, and a pattern, which is a regular expression matched against the request to see if this decorator
 * should be applied.  If more than regular expression matching is required, use a decorator mapper module instead.
 */
public class DecoratorModuleDescriptor extends AbstractModuleDescriptor
{
    private Pattern pattern;
    private String page; // name of the resource for the decorator

    public DecoratorModuleDescriptor(ModuleFactory moduleFactory)
    {
        super(moduleFactory);
    }

    public void init(Plugin plugin, Element element) throws PluginParseException
    {
        super.init(plugin, element);
        if (element.attribute("page") != null)
        {
            page = element.attributeValue("page");
        }
        else
        {
            throw new PluginParseException("No 'page' attribute specified for decorator module " + getName());
        }
        for (Iterator it = element.elementIterator(); it.hasNext();)
        {
            Element child = (Element) it.next();
            if (child.getName().equals("pattern"))
            {
                if (pattern != null)
                {
                    throw new PluginParseException("Multiple patterns specified for decorator module " + getName());
                }

                String patternText = child.node(0).getText();
                try
                {
                    pattern = Pattern.compile(patternText.replaceAll("\\*", ".*"));
                }
                catch (PatternSyntaxException pse)
                {
                    throw new PluginParseException("Invalid pattern: " + patternText, pse);
                }
            }
        }
    }

    public Object getModule()
    {
        return null;
    }

    public Pattern getPattern()
    {
        return pattern;
    }

    public String getPage()
    {
        return page;
    }

}
