package com.atlassian.jira.util;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;

public class JiraKeyUtilsAccesor
{
    /**
     * This is a public static method to be called by test classes outside of this package.
     * <p/>
     * It makes JiraKeyUtils recreate it's cached DefaultKeyMatcher class in order to pick up any changes to the
     * application properties (for example, the project key regex)
     */
    public static void refreshKeyMatcherOnUtilClass()
    {
        JiraKeyUtils.setKeyMatcher(new JiraKeyUtils.ProductionKeyMatcher());
    }

    public static JiraKeyUtils.KeyMatcher getCurrentKeyMatcher()
    {
        return JiraKeyUtils.getCurrentKeyMatcher();
    }

    public static void setKeyMatcher(final JiraKeyUtils.KeyMatcher keyMatcher)
    {
        JiraKeyUtils.setKeyMatcher(keyMatcher);
    }

    public static void resetKeyMatcher(final String regex)
    {
        setKeyMatcher(new JiraKeyUtils.ProductionKeyMatcher(regex));
    }

    public static class MockKeyMatcher extends JiraKeyUtils.DefaultKeyMatcher
    {
        public MockKeyMatcher(final String projectRegex)
        {
            super(projectRegex);
        }
    }

    public static class MockProductionKeyMatcher extends JiraKeyUtils.ProductionKeyMatcher
    {
        ApplicationProperties applicationProperties;
        IssueManager issueManager;

        public MockProductionKeyMatcher(final String projectRegexp, final ApplicationProperties applicationProperties, final IssueManager issueManager)
        {
            super(projectRegexp);
            this.applicationProperties = applicationProperties;
            this.issueManager = issueManager;
        }

        @Override
        ApplicationProperties getApplicationProperties()
        {
            return applicationProperties;
        }

        @Override
        IssueManager getIssueManager()
        {
            return issueManager;
        }

        @Override
        boolean canCurrentUserSeeIssue(final Issue issue)
        {
            return true;
        }
    }
}