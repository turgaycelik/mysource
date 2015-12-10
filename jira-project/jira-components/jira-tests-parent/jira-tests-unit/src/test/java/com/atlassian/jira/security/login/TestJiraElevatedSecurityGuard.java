package com.atlassian.jira.security.login;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.jira.local.MockControllerTestCase;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 */
public class TestJiraElevatedSecurityGuard extends MockControllerTestCase
{
    private LoginManager loginManager;
    private JiraElevatedSecurityGuard securityGuard;
    private HttpServletRequest httpServletRequest;

    @Before
    public void setUp() throws Exception
    {
        loginManager = getMock(LoginManager.class);
        httpServletRequest = getMock(HttpServletRequest.class);
        securityGuard = new JiraElevatedSecurityGuard()
        {
            @Override
            LoginManager getLoginManager()
            {
                return loginManager;
            }
        };
    }

    @Test
    public void testPerformElevatedSecurityCheck()
    {
        expect(loginManager.performElevatedSecurityCheck(httpServletRequest, "userName")).andReturn(true);
        replay();

        assertTrue(securityGuard.performElevatedSecurityCheck(httpServletRequest, "userName"));
    }

    @Test
    public void testOnFailedLoginAttempt()
    {
        expect(loginManager.onLoginAttempt(httpServletRequest, "userName", false)).andReturn(null);
        replay();

        securityGuard.onFailedLoginAttempt(httpServletRequest, "userName");
    }

    @Test
    public void testOnSuccessfulLoginAttempt()
    {
        expect(loginManager.onLoginAttempt(httpServletRequest, "userName", true)).andReturn(null);
        replay();

        securityGuard.onSuccessfulLoginAttempt(httpServletRequest, "userName");
    }
}
