package com.atlassian.jira.web.action.admin.workflow.tabs;

import java.util.Map;

import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

import com.google.common.base.Predicate;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.ResultDescriptor;

public class WorkflowTransitionOtherCondition implements Condition
{
    @Override
    public void init(final Map<String, String> params) throws PluginParseException
    {
    }

    @Override
    public boolean shouldDisplay(final Map<String, Object> context)
    {
        return WorkflowTransitionContext.getTransition(context).exists(new Predicate<ActionDescriptor>()
        {
            @Override
            public boolean apply(final ActionDescriptor actionDescriptor)
            {
                if (actionDescriptor.getConditionalResults() != null && !actionDescriptor.getConditionalResults().isEmpty())
                {
                    return true;
                }

                ResultDescriptor unconditionalResult = actionDescriptor.getUnconditionalResult();
                if (unconditionalResult != null)
                {
                    if (unconditionalResult.getValidators() != null && !unconditionalResult.getValidators().isEmpty())
                    {
                        return true;
                    }

                    if (unconditionalResult.getPreFunctions() != null && !unconditionalResult.getPreFunctions().isEmpty())
                    {
                        return true;
                    }
                }

                if (actionDescriptor.getPreFunctions() != null && !actionDescriptor.getPreFunctions().isEmpty())
                {
                    return true;
                }

                if (actionDescriptor.getPostFunctions() != null && !actionDescriptor.getPostFunctions().isEmpty())
                {
                    return true;
                }

                return false;
            }
        });
    }
}
