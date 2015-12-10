package com.atlassian.jira.web.action.admin.workflow.tabs;

import java.util.Map;

import com.atlassian.fugue.Suppliers;

import com.google.common.base.Function;
import com.opensymphony.workflow.loader.ActionDescriptor;

public class WorkflowTransitionValidatorsContextProvider extends WorkflowTransitionContextProvider
{
    @Override
    protected int getCount(final Map<String, Object> context)
    {
        return WorkflowTransitionContext.getTransition(context).fold(Suppliers.ofInstance(0), new Function<ActionDescriptor, Integer>()
        {
            @Override
            public Integer apply(final ActionDescriptor input)
            {
                return input.getValidators() == null ? 0 : input.getValidators().size();
            }
        });
    }
}
