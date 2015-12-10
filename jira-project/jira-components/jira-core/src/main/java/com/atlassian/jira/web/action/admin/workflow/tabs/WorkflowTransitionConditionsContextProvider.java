package com.atlassian.jira.web.action.admin.workflow.tabs;

import java.util.Collection;
import java.util.Map;

import com.google.common.base.Function;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.ConditionDescriptor;
import com.opensymphony.workflow.loader.ConditionsDescriptor;
import com.opensymphony.workflow.loader.RestrictionDescriptor;

public class WorkflowTransitionConditionsContextProvider extends WorkflowTransitionContextProvider
{
    public int getCount(final Map<String, Object> context)
    {
        RestrictionDescriptor restriction = WorkflowTransitionContext.getTransition(context).map(new Function<ActionDescriptor, RestrictionDescriptor>()
        {
            @Override
            public RestrictionDescriptor apply(final ActionDescriptor input)
            {
                return input.getRestriction();
            }
        }).getOrNull();
        if (restriction != null)
        {
            return getNumberConditions(restriction.getConditionsDescriptor());
        }
        else
        {
            return 0;
        }
    }

    private int getNumberConditions(ConditionsDescriptor conditionsDescriptor)
    {
        int number = 0;
        if (conditionsDescriptor != null)
        {
            Collection conditions = conditionsDescriptor.getConditions();
            if (conditions != null)
            {
                for (Object o : conditions)
                {
                    if (o instanceof ConditionDescriptor)
                    {
                        number++;
                    }
                    else if (o instanceof ConditionsDescriptor)
                    {
                        number += getNumberConditions((ConditionsDescriptor) o);
                    }
                    else
                    {
                        throw new IllegalArgumentException("Invalid object " + o + " found in condition collection.");
                    }
                }
            }
        }

        return number;
    }
}
