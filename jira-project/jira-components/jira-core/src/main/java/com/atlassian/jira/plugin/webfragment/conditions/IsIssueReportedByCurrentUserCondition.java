package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Condition to check whether the current user reported the current issue
 * <p/>
 * An issue must be in the JiraHelper context params.
 *
 * @since v4.1
 */
public class IsIssueReportedByCurrentUserCondition extends AbstractIssueCondition
{
    private static final Logger log = Logger.getLogger(IsIssueReportedByCurrentUserCondition.class);

    public boolean shouldDisplay(User user, Issue issue, JiraHelper jiraHelper)
    {
        final String reportId = issue.getReporterId();
        return StringUtils.isNotBlank(reportId) && user != null && reportId.equals(user.getName());

    }

}