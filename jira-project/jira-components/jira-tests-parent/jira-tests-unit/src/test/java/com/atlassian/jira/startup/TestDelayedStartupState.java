package com.atlassian.jira.startup;


import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;


public class TestDelayedStartupState
{
    private AtomicReference<JiraStartupState> stateTracker = new AtomicReference<JiraStartupState>();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void createRunningState()
    {
        BootstrappingStartupState bootstrappingState = new BootstrappingStartupState(stateTracker);
        stateTracker.set(new DelayedStartupState(bootstrappingState, stateTracker));
    }

    @Test
    public void testJiraStopping()
    {
        JiraStartupState startingState =  stateTracker.get();
        stateTracker.get().onJiraStopping();
        assertCorrectState(startingState, BootstrappingStartupState.class);
    }

    @Test()
    public void testPluginSystemStarted()
    {
        JiraStartupState runningState = stateTracker.get();
        stateTracker.get().onPluginSystemStarted();
        assertCorrectState(runningState, RunningStartupState.class);
    }

    @Test
    public void testPluginSystemStopped()
    {
        JiraStartupState startingState =  stateTracker.get();
        stateTracker.get().onPluginSystemStopped();
        assertCorrectState(startingState, BootstrappingStartupState.class);
    }

    @Test
    public void testPluginSystemDelayed()
    {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("onPluginSystemDelayed() called twice");
        stateTracker.get().onPluginSystemDelayed();
    }

    @Test
    public void testPluginSystemRestarted()
    {
        JiraStartupState runningState = stateTracker.get();
        stateTracker.get().onPluginSystemRestarted();
        assertCorrectState(runningState, RunningStartupState.class);

    }

    private void assertCorrectState(final JiraStartupState startingState, final Class<? extends JiraStartupState> expectedNewStateClass)
    {
        JiraStartupState newState =  stateTracker.get();
        assertThat(newState, instanceOf(expectedNewStateClass));
        assertThat(newState, not(startingState));
    }
}
