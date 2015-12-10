package com.atlassian.jira.bc.license;

/**
 * Used only during the boostrap run level.
 *
 * @since v4.3
 */
public class BootstrapJiraServerIdProvider implements JiraServerIdProvider
{
    @Override
    public String getServerId()
    {
        return "bootstrapping";
    }
}
