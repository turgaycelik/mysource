package com.atlassian.jira.startup;


import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;


public class TestBootstrappingStartupState
{
    private AtomicReference<JiraStartupState> stateTracker = new AtomicReference<JiraStartupState>();
    private BootstrappingStartupState bootstrappingState;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void createBootStrappingState()
    {
        bootstrappingState = new BootstrappingStartupState(stateTracker);
        stateTracker.set(bootstrappingState);
    }

    @Test
    public void testJiraStopping()
    {
        stateTracker.get().onJiraStopping();
        assertThat(stateTracker.get(), instanceOf(BootstrappingStartupState.class));
        assertThat((BootstrappingStartupState)stateTracker.get(), not(bootstrappingState));
    }

    @Test()
    public void testPluginSystemStarted()
    {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("onPluginSystemStarted() called before onPluginSystemDelayed()");
        stateTracker.get().onPluginSystemStarted();
    }

    @Test
    public void testPluginSystemStopped()
    {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("onPluginSystemStopped() called before onPluginSystemDelayed()");
        stateTracker.get().onPluginSystemStopped();
    }

    @Test
    public void testPluginSystemDelayed()
    {
        stateTracker.get().onPluginSystemDelayed();
        assertThat(stateTracker.get(), instanceOf(DelayedStartupState.class));
    }

    @Test
    public void testPluginSystemRestarted()
    {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("onPluginSystemRestarted() called before onPluginSystemDelayed()");
        stateTracker.get().onPluginSystemRestarted();
    }
}
