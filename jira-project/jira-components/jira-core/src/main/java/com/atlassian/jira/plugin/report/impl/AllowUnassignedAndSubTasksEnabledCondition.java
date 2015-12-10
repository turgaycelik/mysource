package com.atlassian.jira.plugin.report.impl;

import com.atlassian.jira.config.SubTaskManager;

/**
 * Composite of the AllowUnassigned and SubTasks EnabledConditions
 *
 * @since v3.11
 */
public class AllowUnassignedAndSubTasksEnabledCondition extends AndEnabledCondition
{
    public AllowUnassignedAndSubTasksEnabledCondition(SubTaskManager subTaskManager)
    {
        super(new AllowUnassignedIssuesEnabledCondition(), new SubTasksEnabledCondition(subTaskManager));
    }
}
