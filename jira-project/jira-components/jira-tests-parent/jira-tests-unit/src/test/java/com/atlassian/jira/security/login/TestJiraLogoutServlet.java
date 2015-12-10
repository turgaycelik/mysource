package com.atlassian.jira.security.login;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.xsrf.XsrfCheckResult;
import com.atlassian.jira.security.xsrf.XsrfInvocationChecker;
import com.atlassian.jira.user.MockUser;

import org.junit.Before;
import org.junit.Test;

/**
 * Responsible for testing {@link com.atlassian.jira.security.login.JiraLogoutServlet}
 *
 * @since v4.1.1
 */
public class TestJiraLogoutServlet extends MockControllerTestCase
{
    private HttpServletRequest mockHttpServletRequest;
    private HttpServletResponse mockHttpServletResponse;
    private XsrfInvocationChecker mockXsrfInvocationChecker;
    private HttpServlet mockSeraphLogoutServlet;
    private XsrfTokenAppendingResponse mockXsrfTokenAppendingResponse;

    private HttpServlet jiraLogoutServlet;
    private JiraAuthenticationContext mockAuthenticationContext;

    @Before
    public void setUp() throws Exception
    {

        mockHttpServletRequest = getMock(HttpServletRequest.class);
        mockHttpServletResponse = getMock(HttpServletResponse.class);
        mockSeraphLogoutServlet = getMock(HttpServlet.class);
        mockXsrfInvocationChecker = getMock(XsrfInvocationChecker.class);
        mockXsrfTokenAppendingResponse = getMock(XsrfTokenAppendingResponse.class);
        mockAuthenticationContext = getMock(JiraAuthenticationContext.class);

        jiraLogoutServlet = new JiraLogoutServlet()
        {
            @Override
            HttpServlet getSeraphLogoutServlet()
            {
                return mockSeraphLogoutServlet;
            }

            @Override
            XsrfInvocationChecker getXsrfInvocationChecker()
            {
                return mockXsrfInvocationChecker;
            }

            @Override
            XsrfTokenAppendingResponse createXsrfTokenAppendingResponse(final HttpServletRequest request,
                    final HttpServletResponse response)
            {
                return mockXsrfTokenAppendingResponse;
            }

            @Override
            JiraAuthenticationContext getAuthenticationContext()
            {
                return mockAuthenticationContext;
            }
        };

    }

    @Test
    public void testDelegatesToSeraphServletWhenTokenIsValid() throws Exception
    {
        expect(mockXsrfInvocationChecker.checkWebRequestInvocation(mockHttpServletRequest)).andReturn(result(true, true, true));

        mockSeraphLogoutServlet.service(mockHttpServletRequest, mockXsrfTokenAppendingResponse);
        expectLastCall().once();

        replay();

        jiraLogoutServlet.service(mockHttpServletRequest, mockHttpServletResponse);
    }

    private XsrfCheckResult result(final boolean required, final boolean valid, final boolean authed)
    {
        return new XsrfCheckResult()
        {
            @Override
            public boolean isRequired()
            {
                return required;
            }

            @Override
            public boolean isValid()
            {
                return valid;
            }

            @Override
            public boolean isGeneratedForAuthenticatedUser()
            {
                return authed;
            }
        };
    }
    
    @Test
    public void testRedirectsToAlreadyLoggedOutPageWhenTheXsrfTokenIsNotValidAndThereIsNoRememberMeCookie()
            throws Exception
    {
        String contextPath = "context";

        expect(mockXsrfInvocationChecker.checkWebRequestInvocation(mockHttpServletRequest)).andReturn(result(true, false, true));
        expect(mockHttpServletRequest.getContextPath()).andReturn(contextPath);

        expect(mockAuthenticationContext.getLoggedInUser()).andReturn(null);

        mockHttpServletResponse.sendRedirect(eq(contextPath + JiraLogoutServlet.ALREADY_LOGGED_OUT_PAGE));
        expectLastCall().once();

        replay();

        jiraLogoutServlet.service(mockHttpServletRequest, mockHttpServletResponse);
    }

    @Test
    public void testRedirectsToConfirmLogOutPageWhenTheXsrfTokenIsNotValidAndThereIsARememberMeCookie()
            throws Exception
    {
        String contextPath = "context";

        expect(mockXsrfInvocationChecker.checkWebRequestInvocation(mockHttpServletRequest)).andReturn(result(true, false, true));
        expect(mockHttpServletRequest.getContextPath()).andReturn(contextPath);

        MockUser mockUser = new MockUser("fred");
        expect(mockAuthenticationContext.getLoggedInUser()).andReturn(mockUser);

        expectLastCall().once();

        mockHttpServletResponse.sendRedirect(eq(contextPath + JiraLogoutServlet.LOG_OUT_CONFIRM_PAGE));
        expectLastCall().once();

        replay();

        jiraLogoutServlet.service(mockHttpServletRequest, mockHttpServletResponse);
    }
}
