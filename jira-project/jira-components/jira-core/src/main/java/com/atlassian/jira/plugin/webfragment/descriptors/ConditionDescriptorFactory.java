package com.atlassian.jira.plugin.webfragment.descriptors;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.conditions.AlwaysDisplayCondition;
import org.dom4j.Element;

import javax.annotation.Nonnull;

/**
 * @since 5.1
 */
public interface ConditionDescriptorFactory
{
    final static Condition DEFAULT_CONDITION = new AlwaysDisplayCondition();

    @Nonnull
    Condition retrieveCondition(@Nonnull Plugin plugin, @Nonnull Element element);
}
