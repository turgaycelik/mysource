/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.workflow.condition;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.Issue;
import com.opensymphony.module.propertyset.PropertySet;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Map;
import java.util.StringTokenizer;

public class SubTaskBlockingCondition extends AbstractJiraCondition
{
    // Ensure that each subtask has one of the selected statuses before allowing parent issue transitions
    public boolean passesCondition(Map transientVars, Map args, PropertySet ps)
    {
        Issue issue = getIssue(transientVars);
        SubTaskManager subTaskManager = ComponentAccessor.getSubTaskManager();

        Collection<GenericValue> subTasks = issue.getSubTasks();
        if (subTaskManager.isSubTasksEnabled() && !subTasks.isEmpty())
        {
            boolean passCondition = false;

            // Check if each subtask has one of the selected statuses
            for (final GenericValue subTask : subTasks)
            {
                // Comma separated list of status ids
                String statuses = (String) args.get("statuses");

                StringTokenizer st = new StringTokenizer(statuses, ",");

                while (st.hasMoreTokens())
                {
                    String statusId = st.nextToken();

                    if (subTask.getString("status").equals(statusId))
                    {
                        passCondition = true;
                        break;
                    }
                    else
                    {
                        passCondition = false;
                    }
                }
                // If any subtask does not have one of the selected statuses - the condition is not satisified.
                if (!passCondition)
                {
                    return false;
                }
            }
        }
        return true;
    }
}
