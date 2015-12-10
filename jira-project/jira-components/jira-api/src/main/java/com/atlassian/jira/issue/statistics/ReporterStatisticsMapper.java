package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.util.UserManager;

public class ReporterStatisticsMapper extends UserStatisticsMapper
{
    public ReporterStatisticsMapper(UserManager userManager, JiraAuthenticationContext authenticationContext)
    {
        super(SystemSearchConstants.forReporter(), userManager, authenticationContext);
    }
}
