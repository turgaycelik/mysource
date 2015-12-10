package com.atlassian.jira.plugin.webfragment.descriptors;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.conditions.ConditionLoadingException;
import com.atlassian.plugin.web.descriptors.ConditionElementParser;
import org.dom4j.Element;

import javax.annotation.Nonnull;

public class ConditionDescriptorFactoryImpl implements ConditionDescriptorFactory, ConditionElementParser.ConditionFactory
{
    private ConditionElementParser conditionElementParser;
    private HostContainer hostContainer;

    public ConditionDescriptorFactoryImpl(HostContainer hostContainer)
    {
        this.conditionElementParser = new ConditionElementParser(this);
        this.hostContainer = hostContainer;
    }

    @Override
    public Condition create(final String className, final Plugin plugin) throws ConditionLoadingException
    {
        try
        {
            final Class<Condition> aCondition = plugin.loadClass(className, getClass());
            return hostContainer.create(aCondition);
        }
        catch (ClassNotFoundException e)
        {
            throw new ConditionLoadingException(e);
        }
        catch (IllegalArgumentException e)
        {
            throw new ConditionLoadingException(e);
        }
    }

    @Nonnull
    @Override
    public Condition retrieveCondition(@Nonnull final Plugin plugin, @Nonnull final Element element)
    {
        final Element conditionEl = element.element("condition");
        final Element conditionsEl = element.element("conditions");
        if (conditionEl != null || conditionsEl != null)
        {
            if (conditionEl != null && conditionEl.attribute("class") == null)
            {
                throw new PluginParseException("class is a required attribute of the condition tag; plugin module: " + plugin.getKey());
            }
            if (conditionsEl != null && !conditionsEl.selectNodes("./condition[not(@class)]").isEmpty())
            {
                throw new PluginParseException("class is a required attribute of the conditions tag; plugin module: " + plugin.getKey());
            }
            return conditionElementParser.makeConditions(plugin, element, ConditionElementParser.CompositeType.AND);
        }
        else{
            return DEFAULT_CONDITION;
        }
    }
}
