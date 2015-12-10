package com.atlassian.jira.my_home;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.myjirahome.MyJiraHomePreference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.annotation.Nonnull;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class MyJiraHomePreferenceImplTest
{
    private static final String MY_JIRA_HOME = "/my-location";

    private User mockUser = mock(User.class);

    private MyJiraHomeValidator mockValidator = mock(MyJiraHomeValidator.class);
    private MyJiraHomeStorage mockStorage = mock(MyJiraHomeStorage.class);

    private MyJiraHomePreference preference = new MyJiraHomePreferenceImpl(mockValidator, mockStorage);

    @Test
    public void testFindMyHomeUserNotLoggedInExpectFallBack()
    {
        final String home = preference.findHome(null);
        assertThat(home, is(""));
    }

    @Test
    public void testFindMyHomeEmptyStringFallBackToDefault()
    {
        expectStorageLoadHome("");
        expectHomeIsValid();

        final String home = preference.findHome(mockUser);
        assertThat(home, is(MyJiraHomePreferenceImpl.DASHBOARD_PLUGIN_MODULE_KEY));
    }

    @Test
    public void testFindMyHomeReturnsInvalidStringFallBackToDefault()
    {
        expectStorageLoadHome("invalid");
        expectHomeIsNotValid();

        final String home = preference.findHome(mockUser);
        assertThat(home, is(MyJiraHomePreferenceImpl.DASHBOARD_PLUGIN_MODULE_KEY));
    }

    @Test
    public void testFindMyHome()
    {
        expectStorageLoadHome(MY_JIRA_HOME);
        expectHomeIsValid();

        final String home = preference.findHome(mockUser);
        assertThat(home, is(MY_JIRA_HOME));
    }

    private void expectStorageLoadHome(@Nonnull final String myHome)
    {
        when(mockStorage.load(mockUser)).thenReturn(myHome);
    }

    private void expectHomeIsValid()
    {
        when(mockValidator.isInvalid(anyString())).thenReturn(Boolean.FALSE);
    }

    private void expectHomeIsNotValid()
    {
        when(mockValidator.isInvalid(anyString())).thenReturn(Boolean.TRUE);
    }

}
