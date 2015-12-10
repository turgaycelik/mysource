package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import org.apache.log4j.Logger;

/**
 * Condition to determine whether Subtasks are turned on or not
 *
 * @since v4.1
 */
public class SubTasksEnabledCondition extends AbstractJiraCondition
{
    private static final Logger log = Logger.getLogger(SubTasksEnabledCondition.class);
    private final SubTaskManager subTaskManager;

    public SubTasksEnabledCondition(SubTaskManager subTaskManager)
    {
        this.subTaskManager = subTaskManager;
    }

    public boolean shouldDisplay(User user, JiraHelper jiraHelper)
    {

        return subTaskManager.isSubTasksEnabled();
    }

}