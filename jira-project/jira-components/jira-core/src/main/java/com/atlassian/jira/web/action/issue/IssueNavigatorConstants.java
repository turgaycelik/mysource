package com.atlassian.jira.web.action.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;

import java.util.Map;

/**
 * Constants extracted out from the old IssueNavigator.java
 *
 * @since v6.1
 */
public class IssueNavigatorConstants
{
    public static final String JQL_QUERY_PARAMETER = "jqlQuery";
    public static final String MODE_SHOW = "show";
    public static final String MODE_HIDE = "hide";

    public static Map<String, Object> makeContext(User remoteUser, JiraHelper jiraHelper)
    {
        return IssueNavigatorViewsHelper.makeContext(remoteUser, jiraHelper);
    }
}
