package com.atlassian.jira.util.system.status;

/**
 * @since 6.3.4
 */
public enum ApplicationState
{

    /**
     * The application is starting up, but not yet available
     */
    STARTING,
    /**
     * The application is running for the first time and has not yet been configured. All requests to the web UI will be
     * redirected to the First Run Wizard.
     */
    FIRST_RUN,
    /**
     * The application has been setup and is running normally
     */
    RUNNING,
    /**
     * The application is currently not available because the application is under maintenance
     */
    MAINTENANCE,
    /**
     * The application is currently not available because of an error
     */
    ERROR,
    /**
     * The application is shutting down
     */
    STOPPING
}
