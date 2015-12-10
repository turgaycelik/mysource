package com.atlassian.jira.workflow;

import java.util.Map;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.Condition;
import com.opensymphony.workflow.WorkflowException;

/**
 * A Condition that is Skippable
 */
public class SkippableCondition implements Condition
{
    private final Condition condition;

    private SkippableCondition(Condition condition)
    {
        this.condition = condition;
    }

    /**
     * Generate a new SkippableCondition for a given condition.
     *
     * This is done to preserve existing behaviour around returning nulls.
     *
     * @param condition
     * @return condition == null ? null : new SkippableCondition(condition);
     */
    public static SkippableCondition of(Condition condition)
    {
        if (condition == null)
        {
            return null;
        }
        else
        {
            return new SkippableCondition(condition);
        }
    }

    @Override
    public boolean passesCondition(Map transientVars, Map args, PropertySet ps) throws WorkflowException
    {
        TransitionOptions transitionOptions= TransitionOptions.toTransitionOptions(transientVars);
        if (transitionOptions.skipConditions())
        {
            return true;
        }

        return condition.passesCondition(transientVars, args, ps);
    }


}
