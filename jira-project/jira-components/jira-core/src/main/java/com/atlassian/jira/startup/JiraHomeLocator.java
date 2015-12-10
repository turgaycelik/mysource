package com.atlassian.jira.startup;

import java.io.File;

import javax.annotation.Nonnull;

import com.atlassian.jira.config.util.AbstractJiraHome;

/**
 * @since v4.3
 */
public class JiraHomeLocator implements JiraHomePathLocator
{
    final CompositeJiraHomePathLocator pathLocator = new CompositeJiraHomePathLocator(
            new SystemPropertyJiraHomePathLocator(),
            new WebContextJiraHomePathLocator(),
            new ApplicationPropertiesJiraHomePathLocator());

    @Override
    public String getJiraHome()
    {
        return pathLocator.getJiraHome();
    }

    @Override
    public String getDisplayName()
    {
        return "JIRA Home Path Locator";
    }

    public static class SystemJiraHome extends AbstractJiraHome
    {
        final JiraHomePathLocator locator = new CompositeJiraHomePathLocator(new JiraHomeLocator(), new SystemPropertyJiraHomePathLocator(), new ApplicationPropertiesJiraHomePathLocator());

        @Nonnull
        @Override
        public File getLocalHome()
        {
            String homePath = locator.getJiraHome();
            if (homePath == null)
            {
                // according to the contract we need to throw IllegalStateException here
                throw new IllegalStateException("No valid JIRA Home directory.");
            }
            return new File(homePath);
        }

    }
}
