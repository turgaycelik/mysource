package com.atlassian.jira.startup;

import com.google.common.collect.ImmutableList;

import java.util.concurrent.atomic.AtomicReference;

/**
 * JIRA is stopped or bootstrapping (i.e. starting, but the plugin system has not been started yet).
 */
public class BootstrappingStartupState extends StartupStateTemplate implements JiraStartupState
{
    /**
     * List of startup checks to run in the bootstrapping state.
     */
    private static final ImmutableList<StartupCheck> STARTUP_CHECKS = ImmutableList.<StartupCheck>of(
            JiraHomeStartupCheck.getInstance()
    );

    /**
     * A reference to the current jira startup state.
     */
    private final AtomicReference<JiraStartupState> currentState;

    public BootstrappingStartupState(AtomicReference<JiraStartupState> currentState)
    {
        this.currentState = currentState;
    }

    @Override
    public void onPluginSystemStarted() throws IllegalStateException
    {
        throw new IllegalStateException("onPluginSystemStarted() called before onPluginSystemDelayed()");
    }

    @Override
    public void onPluginSystemDelayed() throws IllegalStateException
    {
        currentState.set(new DelayedStartupState(this, currentState));
    }

    @Override
    public void onPluginSystemStopped()
    {
        throw new IllegalStateException("onPluginSystemStopped() called before onPluginSystemDelayed()");
    }

    @Override
    public void onPluginSystemRestarted()
    {
        throw new IllegalStateException("onPluginSystemRestarted() called before onPluginSystemDelayed()");
    }

    @Override
    public void onJiraStopping()
    {
        for (final StartupCheck startupCheck : STARTUP_CHECKS)
        {
            startupCheck.stop();
        }

        // reset the current state to BOOTSTRAPPING
        currentState.set(new BootstrappingStartupState(currentState));
    }

    @Override
    protected ImmutableList<StartupCheck> getStartupChecks()
    {
        return STARTUP_CHECKS;
    }
}
