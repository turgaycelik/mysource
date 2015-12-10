/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.condition;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.Condition;
import com.opensymphony.workflow.StoreException;
import com.opensymphony.workflow.spi.Step;
import com.opensymphony.workflow.spi.WorkflowEntry;
import com.opensymphony.workflow.spi.WorkflowStore;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Condition that can be placed on a 'common' action (one shared in multiple steps) which prevents the action
 * being executed for some of those steps.  In effect, allows a "common for everything except.." action.
 */
public class DisallowIfInStepCondition implements Condition
{
    private static final Logger log = Logger.getLogger(DisallowIfInStepCondition.class);

    public boolean passesCondition(Map transientVars, Map args, PropertySet ps)
    {
        WorkflowEntry entry = (WorkflowEntry) transientVars.get("entry");
        WorkflowStore store = (WorkflowStore) transientVars.get("store");

        String stepIdsString = (String) args.get("stepIds");
        StringTokenizer st = new StringTokenizer(stepIdsString, ", ");
        HashSet stepIds = new HashSet();
        while (st.hasMoreTokens())
        {
            stepIds.add(new Integer(st.nextToken()));
        }

        List currentSteps = null;
        try
        {
            currentSteps = store.findCurrentSteps(entry.getId());
        }
        catch (StoreException e)
        {
            log.error("Error occurred while retrieving current steps.", e);
            return false;
        }
        for (final Object currentStep : currentSteps)
        {
            Step step = (Step) currentStep;
            Integer stepId = new Integer(step.getStepId());
            if (stepIds.contains(stepId))
            {
                return false;
            }
        }

        return true;
    }
}
