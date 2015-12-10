package com.atlassian.jira.issue.search.quicksearch;

import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;

public class FixForQuickSearchHandler extends VersionQuickSearchHandler
{
    public FixForQuickSearchHandler(VersionManager versionManager, ProjectManager projectManager, PermissionManager permissionManager, JiraAuthenticationContext authenticationContext)
    {
        super(versionManager, projectManager, permissionManager, authenticationContext);
    }

    protected String getPrefix()
    {
        return "ff:";
    }

    protected String getSearchParamName()
    {
        return "fixfor";
    }
}
