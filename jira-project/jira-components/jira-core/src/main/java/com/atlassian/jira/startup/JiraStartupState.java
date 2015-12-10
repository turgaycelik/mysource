package com.atlassian.jira.startup;

/**
 * Interface for JIRA startup states. The startup state contains any startup checks that need to be performed when JIRA
 * is in a given state.
 *
 * @see BootstrappingStartupState
 * @see RunningStartupState
 * @see DelayedStartupState
 * @since 4.3.1
 */
public interface JiraStartupState
{
    /**
     * Returns true if this JiraStartupState's prerequisites have been satisfied (i.e. JIRA is "good to go" in this
     * state). In practice this means that JIRA is able to run in this startup state.
     * <p/>
     * Implementations of this method should run any necessary validity/consistency checks the first time it is called,
     * the result of which should be cached.
     *
     * @return true if JIRA started correctly
     */
    boolean isStartupChecksPassed();

    /**
     * Returns the first StartupCheck that failed, if any.
     *
     * @return the first StartupCheck that failed, if any.
     */
    StartupCheck getFailedStartupCheck();

    /**
     * Sets the first StartupCheck that failed.
     *
     * @param startupCheck a StartupCheck
     */
    void setFailedStartupCheck(StartupCheck startupCheck);

    /**
     * This method is called when the Atlassian Plugins system has been started within JIRA.
     *
     * @throws IllegalStateException if this is not a valid event for this state
     */
    void onPluginSystemStarted() throws IllegalStateException;

    /**
     * This method is called when the Atlassian Plugins system has completed earlyStartup within JIRA.
     *
     * @throws IllegalStateException if this is not a valid event for this state
     */
    void onPluginSystemDelayed() throws IllegalStateException;

    /**
     * This method is called when the Atlassian Plugins system has been stopped within JIRA.
     *
     * @throws IllegalStateException if this is not a valid event for this state
     */
    void onPluginSystemStopped() throws IllegalStateException;

    /**
     * This method is called when the Atlassian Plugins system has been restarted within JIRA.
     *
     * @throws IllegalStateException if this is not a valid event for this state
     */
    void onPluginSystemRestarted() throws IllegalStateException;

    /**
     * This method is called when JIRA is being stopped.
     *
     * @throws IllegalStateException if this is not a valid event for this state
     */
    void onJiraStopping();
}
