package com.atlassian.jira.startup;

import org.apache.log4j.Logger;

/**
 * This is the very FIRST bit of log output that JIRA does. If this is not the case then make sure this is moved
 * earlier. The intent of this is when the shite hits the fan during startup, we at least know when JIRA started, what
 * the state of it was. This is aimed squarely at support.
 *
 * @since v4.4
 */
public class SystemInfoLauncher implements JiraLauncher
{
    private static final Logger log = Logger.getLogger(SystemInfoLauncher.class);

    @Override
    public void start()
    {
        try
        {
            //
            // This is very careful not to touch the PICO world in any way!
            //
            final JiraStartupLogger jiraStartupLog = new JiraStartupLogger();
            jiraStartupLog.printStartingMessage();
        }
        catch (Exception e)
        {
            log.error("Couldn't log startup messages", e);
        }
    }

    @Override
    public void stop()
    {
    }
}
