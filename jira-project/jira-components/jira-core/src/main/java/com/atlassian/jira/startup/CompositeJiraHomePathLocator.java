package com.atlassian.jira.startup;

import com.atlassian.jira.util.collect.CollectionBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.List;

public class CompositeJiraHomePathLocator implements JiraHomePathLocator
{
    private static final Logger log = Logger.getLogger(CompositeJiraHomePathLocator.class);
    private final List<JiraHomePathLocator> locators;

    public CompositeJiraHomePathLocator(final JiraHomePathLocator... locators)
    {
        this.locators = CollectionBuilder.newBuilder(locators).asList();
    }

    public String getJiraHome()
    {
        for (final JiraHomePathLocator homePathLocator : locators)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Looking for jira.home using " + homePathLocator);
            }
            final String jiraHome = homePathLocator.getJiraHome();
            if (StringUtils.isNotBlank(jiraHome))
            {
                log.debug("jira.home '" + jiraHome + "' found using " + homePathLocator.getDisplayName() + '.');
                return jiraHome.trim();
            }
        }
        return null;
    }

    public String getDisplayName()
    {
        return "Composite";
    }
}
