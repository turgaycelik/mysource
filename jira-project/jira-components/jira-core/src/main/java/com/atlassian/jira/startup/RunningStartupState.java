package com.atlassian.jira.startup;

import com.google.common.collect.ImmutableList;

import java.util.concurrent.atomic.AtomicReference;

/**
 * JIRA has been bootstrapped and the plugin system has been started.
 */
public class RunningStartupState extends StartupStateTemplate implements JiraStartupState
{
    /**
     * Startup checks to use in the running state.
     */
    private static final ImmutableList<StartupCheck> STARTUP_CHECKS = ImmutableList.<StartupCheck>of(
            new SystemPluginsEnabledStartupCheck()
    );

    /**
     * The bootstrapping state to delegate to.
     */
    private final BootstrappingStartupState bootstrapping;

    /**
     * An atomic reference to the current running state.
     */
    private final AtomicReference<JiraStartupState> currentState;

    public RunningStartupState(BootstrappingStartupState bootstrapping, AtomicReference<JiraStartupState> currentState)
    {
        this.bootstrapping = bootstrapping;
        this.currentState = currentState;
    }

    @Override
    public boolean isStartupChecksPassed()
    {
        return bootstrapping.isStartupChecksPassed() && super.isStartupChecksPassed();
    }

    @Override
    public void onPluginSystemStarted() throws IllegalStateException
    {
        throw new IllegalStateException("onPluginSystemStarted called twice");
    }

    @Override
    public void onPluginSystemDelayed() throws IllegalStateException
    {
        throw new IllegalStateException("onPluginSystemDelayed() called after onPluginStarted()");
    }

    @Override
    public StartupCheck getFailedStartupCheck()
    {
        StartupCheck failedBootstrappingcheck = bootstrapping.getFailedStartupCheck();
        if (failedBootstrappingcheck != null)
        {
            return failedBootstrappingcheck;
        }

        return super.getFailedStartupCheck();
    }

    @Override
    public void setFailedStartupCheck(StartupCheck startupCheck)
    {
        bootstrapping.setFailedStartupCheck(startupCheck);
    }

    @Override
    public void onPluginSystemStopped()
    {
        currentState.set(new BootstrappingStartupState(currentState));
    }

    @Override
    public void onPluginSystemRestarted()
    {
        currentState.set(new RunningStartupState(bootstrapping, currentState));
    }

    @Override
    public void onJiraStopping()
    {
        bootstrapping.onJiraStopping();
    }

    @Override
    protected ImmutableList<StartupCheck> getStartupChecks()
    {
        return STARTUP_CHECKS;
    }
}
