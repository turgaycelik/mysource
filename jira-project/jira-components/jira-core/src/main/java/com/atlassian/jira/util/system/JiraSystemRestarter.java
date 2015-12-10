package com.atlassian.jira.util.system;

import javax.servlet.ServletContext;

/**
 * This manager can restart the JIRA world.  This is used when JIRA needs
 * to end the PICO world and then restart it.
 *
 * @since v4.0
 */
public interface JiraSystemRestarter
{
    /**
     * This will stop essential services like the scheduler, restart PICO and then restart the essential services.
     *
     * No upgrades will be run when this method is invoked. 
     */
    void ariseSirJIRA();

    /**
     * This will stop essential services like the scheduler, restart PICO and will then run the upgrades if they are needed
     * and then finally restart the essential services.
     *
     * @param servletContext this is needed to raise Johnson events as the upgrade happens.
     */
    void ariseSirJIRAandUpgradeThySelf(ServletContext servletContext);
}
