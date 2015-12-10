package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.worklog.WorklogService;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import org.apache.log4j.Logger;

/**
 * Condition to determine whether time tracking is turned on or not
 *
 * @since v4.1
 */
public class TimeTrackingEnabledCondition extends AbstractJiraCondition
{
    private static final Logger log = Logger.getLogger(TimeTrackingEnabledCondition.class);
    private final WorklogService worklogService;

    public TimeTrackingEnabledCondition(WorklogService worklogService)
    {
        this.worklogService = worklogService;
    }

    public boolean shouldDisplay(User user, JiraHelper jiraHelper)
    {
        return worklogService.isTimeTrackingEnabled();
    }

}