package com.atlassian.jira.my_home;

import com.atlassian.core.AtlassianCoreException;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.myjirahome.MyJiraHomeUpdateException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class MyJiraHomeUpdateServiceImplTest
{
    private static final String MY_JIRA_HOME = "/my-location";

    private User mockUser = mock(User.class);

    private MyJiraHomeValidator mockValidator = mock(MyJiraHomeValidator.class);
    private MyJiraHomeStorage mockStorage = mock(MyJiraHomeStorage.class);

    private MyJiraHomeUpdateService service = new MyJiraHomeUpdateServiceImpl(mockValidator, mockStorage);

    @Test
    public void testUpdateHomeWithInvalidHome()
    {
        expectHomeIsNotValid();

        try
        {
            service.updateHome(mockUser, "invalid");
            fail("expected exception");
        }
        catch(MyJiraHomeUpdateException e)
        {
            assertThat(e.getMessage(), containsString("plugin module key is not usable"));
        }

        verifyHomeNotUpdated();
    }

    @Test(expected = MyJiraHomeUpdateException.class)
    public void testUpdateHomeExceptionWhileUpdate()
    {
        expectHomeIsValid();
        doThrow(new MyJiraHomeUpdateException()).when(mockStorage).store(eq(mockUser), eq(MY_JIRA_HOME));

        service.updateHome(mockUser, MY_JIRA_HOME);
    }

    @Test
    public void testUpdateHomeNoValidationOfEmptyStrings() throws AtlassianCoreException
    {
        service.updateHome(mockUser, "");

        verify(mockValidator, never()).isInvalid("");
        verifyHomeUpdated("");
    }

    @Test
    public void testUpdateHome() throws AtlassianCoreException
    {
        expectHomeIsValid();

        service.updateHome(mockUser, MY_JIRA_HOME);

        verifyHomeUpdated(MY_JIRA_HOME);
    }

    private void expectHomeIsValid()
    {
        when(mockValidator.isInvalid(anyString())).thenReturn(Boolean.FALSE);
    }

    private void expectHomeIsNotValid()
    {
        when(mockValidator.isInvalid(anyString())).thenReturn(Boolean.TRUE);
    }

    private void verifyHomeUpdated(final String newHome)
    {
        verify(mockStorage).store(eq(mockUser), eq(newHome));
    }

    private void verifyHomeNotUpdated()
    {
        verify(mockStorage, never()).store(any(User.class), anyString());
    }

}
