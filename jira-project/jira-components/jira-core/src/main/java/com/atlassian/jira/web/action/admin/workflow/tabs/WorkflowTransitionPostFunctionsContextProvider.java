package com.atlassian.jira.web.action.admin.workflow.tabs;

import java.util.Map;

import com.atlassian.fugue.Suppliers;

import com.google.common.base.Function;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.ResultDescriptor;

public class WorkflowTransitionPostFunctionsContextProvider extends WorkflowTransitionContextProvider
{
    @Override
    protected int getCount(final Map<String, Object> context)
    {
        return WorkflowTransitionContext.getTransition(context).fold(Suppliers.ofInstance(0), new Function<ActionDescriptor, Integer>()
        {
            @Override
            public Integer apply(final ActionDescriptor input)
            {
                final ResultDescriptor result = input.getUnconditionalResult();
                return result != null && result.getPostFunctions() != null ? result.getPostFunctions().size() : 0 ;
            }
        });
    }
}
