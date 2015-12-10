package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.util.UserManager;


public class CreatorStatisticsMapper extends UserStatisticsMapper
{
    public CreatorStatisticsMapper(UserManager userManager, JiraAuthenticationContext authenticationContext)
    {
        super(SystemSearchConstants.forCreator(), userManager, authenticationContext);
    }
}
