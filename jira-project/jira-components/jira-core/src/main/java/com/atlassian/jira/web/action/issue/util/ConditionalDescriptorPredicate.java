package com.atlassian.jira.web.action.issue.util;

import com.atlassian.plugin.web.descriptors.ConditionalDescriptor;
import com.google.common.base.Predicate;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Use it to filter out module descriptors implementing {@link ConditionalDescriptor}. It will select only those that
 * have {@link com.atlassian.plugin.web.descriptors.ConditionalDescriptor#getCondition()} returning true from
 * {@link com.atlassian.plugin.web.Condition#shouldDisplay(java.util.Map)}.
 *
 * @since v5.2
 */
public class ConditionalDescriptorPredicate implements Predicate<ConditionalDescriptor>
{
    private final Map<String, Object> context;

    public ConditionalDescriptorPredicate(Map<String, Object> context)
    {
        this.context = context;
    }

    @Override
    public boolean apply(@Nullable ConditionalDescriptor input)
    {
        return input != null && input.getCondition().shouldDisplay(context);
    }
}
