package com.atlassian.jira.my_home.web.action;

import com.atlassian.crowd.model.user.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.my_home.MyJiraHomeUpdateService;
import com.atlassian.jira.plugin.myjirahome.MyJiraHomeUpdateException;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.action.MockRedirectSanitiser;
import com.atlassian.jira.web.action.RedirectSanitiser;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import webwork.action.ServletActionContext;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class UpdateMyJiraHomeTest
{
    private static final String MY_JIRA_HOME_PROVIDER = "my-jira-home:provider";

    private JiraAuthenticationContext mockJiraAuthenticationContext = mock(JiraAuthenticationContext.class);
    private User mockUser = mock(User.class);
    private HttpServletResponse mockHttpServletResponse = mock(HttpServletResponse.class);

    private MyJiraHomeUpdateService mockMyJiraHomeUpdateService = mock(MyJiraHomeUpdateService.class);

    private UpdateMyJiraHome action = new UpdateMyJiraHome(mockMyJiraHomeUpdateService);

    @Before
    public void setUpMocks()
    {
        ComponentAccessor.initialiseWorker(new MockComponentWorker()
                .addMock(JiraAuthenticationContext.class, mockJiraAuthenticationContext)
                .addMock(RedirectSanitiser.class, new MockRedirectSanitiser())
        );

        ServletActionContext.setResponse(mockHttpServletResponse);
    }

    @Test
    public void testUpdateMyJiraHomeToNullForAnonymousUser() throws Exception
    {
        expectAnonymousUser();

        action.setTarget(null);
        action.doExecute();

        verifyRedirectToMyJiraHome();
        verifyNoUpdateHome();
    }

    @Test
    public void testUpdateMyJiraHomeToEmptyStringForAnonymousUser() throws Exception
    {
        expectAnonymousUser();

        action.setTarget("");
        action.doExecute();

        verifyRedirectToMyJiraHome();
        verifyNoUpdateHome();
    }

    @Test
    public void testUpdateMyJiraHomeForAnonymousUser() throws Exception
    {
        expectAnonymousUser();

        action.setTarget(MY_JIRA_HOME_PROVIDER);
        action.doExecute();

        verifyRedirectToMyJiraHome();
        verifyNoUpdateHome();
    }

    @Test
    public void testUpdateMyJiraHomeFailed() throws Exception
    {
        expectAuthenticatedUser();
        doThrow(new MyJiraHomeUpdateException()).when(mockMyJiraHomeUpdateService).updateHome(any(User.class), anyString());

        action.setTarget(MY_JIRA_HOME_PROVIDER);
        final String result = action.doExecute();
        assertThat(result, is("error"));

        verifyNoRedirect();
    }

    @Test
    public void testUpdateMyJiraHomeToNull() throws Exception
    {
        expectAuthenticatedUser();

        action.setTarget(null);
        action.doExecute();

        verifyUpdateHome("");
        verifyRedirectToMyJiraHome();
    }

    @Test
    public void testUpdateMyJiraHomeToEmptyString() throws Exception
    {
        expectAuthenticatedUser();

        action.setTarget("");
        action.doExecute();

        verifyUpdateHome("");
        verifyRedirectToMyJiraHome();
    }

    @Test
    public void testUpdateMyJiraHome() throws Exception
    {
        expectAuthenticatedUser();

        action.setTarget(MY_JIRA_HOME_PROVIDER);
        action.doExecute();

        verifyUpdateHome(MY_JIRA_HOME_PROVIDER);
        verifyRedirectToMyJiraHome();
    }

    private void expectAnonymousUser()
    {
        when(mockJiraAuthenticationContext.getLoggedInUser()).thenReturn(null);
    }

    private void expectAuthenticatedUser()
    {
        when(mockJiraAuthenticationContext.getLoggedInUser()).thenReturn(mockUser);
    }
    
    private void verifyUpdateHome(@Nonnull final String newHome)
    {
        verify(mockMyJiraHomeUpdateService).updateHome(mockUser, newHome);
    }

    private void verifyNoRedirect() throws IOException
    {
        verify(mockHttpServletResponse, never()).sendRedirect(anyString());
    }

    private void verifyRedirectToMyJiraHome() throws IOException
    {
        verify(mockHttpServletResponse).sendRedirect(eq(UpdateMyJiraHome.MY_JIRA_HOME));
    }

    private void verifyNoUpdateHome()
    {
        verify(mockMyJiraHomeUpdateService, never()).updateHome(any(User.class), anyString());
    }
}
