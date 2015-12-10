package com.atlassian.jira.plugin.report.impl;

import com.atlassian.configurable.EnabledCondition;
import com.atlassian.jira.config.SubTaskManager;

/**
 * EnabledCondition that checks whether SubTasks are enabled or not.
 *
 * @since v6.3
 */
public class SubTasksEnabledCondition implements EnabledCondition
{
    private final SubTaskManager subTaskManager;

    public SubTasksEnabledCondition(SubTaskManager subTaskManager)
    {
        this.subTaskManager = subTaskManager;
    }

    public boolean isEnabled()
    {
        return subTaskManager.isSubTasksEnabled();
    }
}
