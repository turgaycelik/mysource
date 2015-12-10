package com.atlassian.jira.scheduler;

import org.apache.log4j.Logger;

/**
 * Will Launch the scheduler but leave it in standby mode.
 *
 * @since v6.1
 */
public class JiraPassivatedSchedulerLauncher extends JiraSchedulerLauncher
{
    private static final Logger log = Logger.getLogger(JiraPassivatedSchedulerLauncher.class);

    @Override
    protected void proceedIfAllClear()
    {
        log.info("JIRA Scheduler not started: This node is in the passivated state.");
    }
}
