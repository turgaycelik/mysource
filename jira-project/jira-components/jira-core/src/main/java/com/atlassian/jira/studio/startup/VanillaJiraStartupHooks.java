package com.atlassian.jira.studio.startup;

import java.util.Properties;

/**
 * Implementation of {@link com.atlassian.jira.studio.startup.StudioStartupHooks} used by a JIRA outside of studio.
 *
 * @since v4.4.1
 */
class VanillaJiraStartupHooks implements StudioStartupHooks
{
    @Override
    public Properties getLog4jConfiguration(Properties initialConfiguration)
    {
        return null;
    }

    @Override
    public void beforeJiraStart()
    {
    }

    @Override
    public void afterJiraStart()
    {
    }
}
