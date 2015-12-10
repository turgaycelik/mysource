package com.atlassian.jira.web.action.admin.workflow.tabs;

import java.util.Map;

import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

import com.google.common.base.Predicate;
import com.opensymphony.workflow.loader.ActionDescriptor;

public class WorkflowTransitionConditionsCondtion implements Condition
{
    @Override
    public void init(final Map<String, String> params) throws PluginParseException
    {
    }

    @Override
    public boolean shouldDisplay(final Map<String, Object> context)
    {
        return WorkflowTransitionContext.getWorkflow(context).exists(new Predicate<JiraWorkflow>()
        {
            @Override
            public boolean apply(final JiraWorkflow workflow)
            {
                return WorkflowTransitionContext.getTransition(context).exists(new Predicate<ActionDescriptor>()
                {
                    @Override
                    public boolean apply(final ActionDescriptor actionDescriptor)
                    {
                        // "initial" is a special case - it's the transition that happens as you create an issue.
                        // You can't add conditions but you can add validators and post functions (eg assign to reporter).
                        return !workflow.isInitialAction(actionDescriptor);
                    }
                });
            }
        });
    }
}
