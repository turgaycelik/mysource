package com.atlassian.jira.bc.license;

public class DefaultJiraServerIdProvider implements JiraServerIdProvider
{
    private final JiraLicenseService jiraLicenseService;

    public DefaultJiraServerIdProvider(JiraLicenseService jiraLicenseService)
    {
        this.jiraLicenseService = jiraLicenseService;
    }

    @Override
    public String getServerId()
    {
        return jiraLicenseService.getServerId();
    }
}
