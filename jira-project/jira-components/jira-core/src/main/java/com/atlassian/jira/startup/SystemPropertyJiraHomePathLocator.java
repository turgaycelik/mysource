package com.atlassian.jira.startup;

import com.atlassian.jira.config.properties.JiraSystemProperties;

import org.apache.log4j.Logger;

/**
 * Attempts to find a jira.home configured as a system property.
 *
 * @since v4.0
 */
public class SystemPropertyJiraHomePathLocator implements JiraHomePathLocator
{
    private static final Logger log = Logger.getLogger(SystemPropertyJiraHomePathLocator.class);

    public String getJiraHome()
    {
        try
        {
            return JiraSystemProperties.getInstance().getProperty(Property.JIRA_HOME);
        }
        catch (final SecurityException e)
        {
            // Some app servers may restrict access.
            final String message = String.format("Unable to obtain JIRA home from system property: %s.", e.getMessage());
            if (log.isDebugEnabled())
            {
                log.debug(message, e);
            }
            else
            {
                log.info(message);
            }
            return null;
        }
    }

    public String getDisplayName()
    {
        return "System Property";
    }
}
