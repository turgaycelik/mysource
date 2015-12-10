package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;

/**
 * Used to determine if the logged in user is the same as the profile user in the jira helper.
 *
 * @since v3.12
 */
public class UserIsTheLoggedInUserCondition extends AbstractJiraCondition
{
    public static final String PROFILE_USER = "profileUser";

    public boolean shouldDisplay(User user, JiraHelper jiraHelper)
    {
        User profileUser = (jiraHelper == null) ? null : (User) jiraHelper.getRequest().getAttribute(PROFILE_USER);
        return jiraHelper != null && jiraHelper.getRequest() != null && user != null && user.equals(profileUser);
    }

}
