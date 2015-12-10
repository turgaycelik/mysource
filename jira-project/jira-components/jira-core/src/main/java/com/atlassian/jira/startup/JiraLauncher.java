package com.atlassian.jira.startup;

/**
 * JIRA used to have context-listeners that did things on startup. These have been converted from ServletContextListener
 * to JiraLauncher. Instead of contextInitialized and contextDestroyed they now implement start() and stop(). This allows
 * us to decouple JIRA creation from context creation and gives us further flexibility about when exactly bits of the
 * start up occur.
 * @since v4.3
 */
public interface JiraLauncher
{
    /**
     * Called during JIRA "startup".
     * Despite no longer being multitenant, it is still a good idea to chunk this.  The order is defined
     * in DefaultJiraLauncher
     *
     * The logic for ordering all of this will be handled by the DefaultJiraLauncher.
     * @see DefaultJiraLauncher
     */
    void start();

    /**
     * Called when JIRA is shutting down. Just like startup this can .
     * The logic of what exactly happens when is encapsulated in the DefaultJiraLauncher.
     *
     * @see DefaultJiraLauncher
     */
    void stop();
}
