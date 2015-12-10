package com.atlassian.jira.config.util;

import com.atlassian.jira.startup.ApplicationPropertiesJiraHomePathLocator;
import com.atlassian.jira.startup.CompositeJiraHomePathLocator;
import com.atlassian.jira.startup.SystemPropertyJiraHomePathLocator;
import com.atlassian.jira.startup.WebContextJiraHomePathLocator;

import java.io.File;
import javax.annotation.Nonnull;

/**
 * Simple implementation of {@link JiraHome}.
*
* @since v4.1
*/
public final class DefaultJiraHome extends AbstractJiraHome
{
    final CompositeJiraHomePathLocator locator = new CompositeJiraHomePathLocator(
            new SystemPropertyJiraHomePathLocator(),
            new WebContextJiraHomePathLocator(),
            new ApplicationPropertiesJiraHomePathLocator());

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
