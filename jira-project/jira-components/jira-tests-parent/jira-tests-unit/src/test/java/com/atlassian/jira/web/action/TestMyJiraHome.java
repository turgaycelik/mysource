package com.atlassian.jira.web.action;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import com.atlassian.crowd.model.user.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.plugin.myjirahome.MyJiraHomeLinker;
import com.atlassian.jira.security.JiraAuthenticationContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import webwork.action.ServletActionContext;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class TestMyJiraHome
{
    private JiraAuthenticationContext mockJiraAuthenticationContext = mock(JiraAuthenticationContext.class);
    private User mockUser = mock(User.class);
    private HttpServletResponse mockHttpServletRespone = mock(HttpServletResponse.class);

    private MyJiraHomeLinker myJiraHomeLinker = mock(MyJiraHomeLinker.class);

    private MyJiraHome action = new MyJiraHome(myJiraHomeLinker);

    @Before
    public void setUpMocks()
    {
        ComponentAccessor.initialiseWorker(new MockComponentWorker()
                .addMock(JiraAuthenticationContext.class, mockJiraAuthenticationContext)
                .addMock(RedirectSanitiser.class, new MockRedirectSanitiser())
        );

        ServletActionContext.setResponse(mockHttpServletRespone);
    }

    @Test
    public void testExecuteAsAnonymousUser() throws Exception
    {
        expectAnonymousUser();

        action.doExecute();

        verifyRedirect(MyJiraHomeLinker.DEFAULT_HOME_OD_ANON);
    }


    @Test
    public void testExecuteAsAuthenticatedUser() throws Exception
    {
        expectAuthenticatedUser();

        action.doExecute();

        verifyRedirect(MyJiraHomeLinker.DEFAULT_HOME_NOT_ANON);
    }

    private void expectAnonymousUser()
    {
        when(mockJiraAuthenticationContext.getLoggedInUser()).thenReturn(null);
        when(myJiraHomeLinker.getHomeLink(null)).thenReturn(MyJiraHomeLinker.DEFAULT_HOME_OD_ANON);
    }

    private void expectAuthenticatedUser()
    {
        when(mockJiraAuthenticationContext.getLoggedInUser()).thenReturn(mockUser);
        when(myJiraHomeLinker.getHomeLink(mockUser)).thenReturn(MyJiraHomeLinker.DEFAULT_HOME_NOT_ANON);
    }

    private void verifyRedirect(final String expectedUrl) throws IOException
    {
        verify(mockHttpServletRespone).sendRedirect(eq(expectedUrl));
    }

}
