package com.atlassian.jira.config.util;

import java.io.File;

import javax.annotation.Nonnull;

import com.atlassian.jira.startup.JiraHomePathLocator;

/**
 * Simple implementation of {@link com.atlassian.jira.config.util.JiraHome}.
*
* @since v4.1
*/
public final class StartupJiraHome extends AbstractJiraHome
{
    JiraHomePathLocator locator;

    public StartupJiraHome(final JiraHomePathLocator locator)
    {
        this.locator = locator;
    }

    @Nonnull
    @Override
    public File getLocalHome()
    {
        final String jiraHome = locator.getJiraHome();

        if (jiraHome == null)
        {
            throw new IllegalStateException("No valid JIRA Home directory.");
        }
        return new File(jiraHome);
    }

}
