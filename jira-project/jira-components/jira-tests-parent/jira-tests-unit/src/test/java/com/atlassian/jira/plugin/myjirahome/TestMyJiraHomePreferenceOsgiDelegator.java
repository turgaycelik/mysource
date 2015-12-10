package com.atlassian.jira.plugin.myjirahome;

import javax.annotation.Nonnull;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class TestMyJiraHomePreferenceOsgiDelegator
{
    private static final String MY_HOME = "/my-jira-home";

    private final User mockUser = mock(User.class);
    private final MyJiraHomePreference mockDelegate = mock(MyJiraHomePreference.class);

    private final MyJiraHomePreferenceOsgiDelegator delegator = new MyJiraHomePreferenceOsgiDelegator();

    @Test
    public void testFindHomeDelegateNotAvailableFallBack() throws Exception
    {
        expectDelegateIsNotAvailable();

        final String result = delegator.findHome(mockUser);
        assertThat(result, is(""));
    }

    @Test
    public void testFindHomeDelegateAvailable() throws Exception
    {
        expectDelegateIsAvailable();
        when(mockDelegate.findHome(mockUser)).thenReturn(MY_HOME);

        final String result = delegator.findHome(mockUser);
        assertThat(result, is(MY_HOME));
    }

    private void expectDelegateIsNotAvailable()
    {
        initializeWorker();
    }

    private void expectDelegateIsAvailable()
    {
        final ComponentAccessor.Worker mockWorker = initializeWorker();

        when(mockWorker.getOSGiComponentInstanceOfType(MyJiraHomePreference.class)).thenReturn(mockDelegate);
    }

    @Nonnull
    private ComponentAccessor.Worker initializeWorker()
    {
        final ComponentAccessor.Worker mockWorker = mock(ComponentAccessor.Worker.class);
        ComponentAccessor.initialiseWorker(mockWorker);
        return mockWorker;
    }
}
