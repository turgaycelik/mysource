package com.atlassian.jira.bc.security.login;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.login.LoginManager;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.MockUserManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.JiraContactHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(ListeningMockitoRunner.class)
public class TestLoginServiceImpl
{
    @Mock private LoginManager loginManager;
    @Mock private ApplicationProperties applicationProperties;
    @Mock private UserManager userManager;
    @Mock private JiraContactHelper mockJiraContactHelper;
    @Mock private JiraAuthenticationContext mockJiraAuthenticationContext;

    private User user;

    @Before
    public void setUp() throws Exception
    {
        user = new MockUser("username");
    }

    @Test
    public void testGetLoginInfo()
    {
        LoginInfo loginInfo = new LoginInfoImpl(1L, 2L, 3L, 4L, 5L, 6L, 7L, true);

        when(loginManager.getLoginInfo("userName")).thenReturn(loginInfo);

        final LoginService loginService = createLoginService();
        final LoginInfo actualLoginInfo = loginService.getLoginInfo("userName");
        assertSame(loginInfo, actualLoginInfo);
    }

    private LoginService createLoginService()
    {
        return new LoginServiceImpl(loginManager, applicationProperties, userManager, mockJiraContactHelper, mockJiraAuthenticationContext);
    }

    @Test
    public void testIsElevatedSecurityCheckAlwaysShown()
    {
        when(loginManager.isElevatedSecurityCheckAlwaysShown()).thenReturn(true);

        final LoginService loginService = createLoginService();
        assertTrue(loginService.isElevatedSecurityCheckAlwaysShown());
    }


    @Test
    public void testResetFailedLoginCount()
    {
        final LoginService loginService = createLoginService();
        loginService.resetFailedLoginCount(user);
        verify(loginManager).resetFailedLoginCount(user);
    }

    @Test
    public void testAuthenticate()
    {
        LoginResult loginResult = new LoginResultImpl(LoginReason.OK, null, "username");
        when(loginManager.authenticate(user, "password")).thenReturn(loginResult);

        final LoginService loginService = createLoginService();
        assertSame(loginResult, loginService.authenticate(user, "password"));
    }

    @Test(expected = Exception.class)
    public void testGetLoginPropertiesFail()
    {
        createLoginService().getLoginProperties(null, null);
    }
    
    @Test
    public void testGetLoginProperties()
    {
        _testLoginProperties(false, false, false, false);
    }

    @Test
    public void testGetLoginPropertiesCookies()
    {
        _testLoginProperties(true, false, false, false);
    }

    @Test
    public void testGetLoginPropertiesExternalUser()
    {
        _testLoginProperties(false, true, false, false);
    }

    @Test
    public void testGetLoginPropertiesElevatedSecurityCheck()
    {
        _testLoginProperties(false, false, true, false);
    }

    @Test
    public void testGetLoginPropertiesPublic()
    {
        _testLoginProperties(false, false, false, true);
    }

    private void _testLoginProperties(boolean allowCookies, boolean externalUser, boolean elevatedSecurityCheck, final boolean isPublic)
    {
        final HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        final UserManager userManager = new MockUserManager();
        when(applicationProperties.getOption("jira.option.allowcookies")).thenReturn(allowCookies);
        when(applicationProperties.getOption("jira.option.user.externalmanagement")).thenReturn(externalUser);
        when(mockRequest.getScheme()).thenReturn("http");
        when(mockRequest.getAttribute("com.atlassian.jira.security.login.LoginManager.LoginResult")).thenReturn(new LoginResultImpl(null, null, null));
        when(mockRequest.getAttribute("os_authstatus")).thenReturn(null);
        when(mockRequest.getAttribute("auth_error_type")).thenReturn(null);
        when(loginManager.isElevatedSecurityCheckAlwaysShown()).thenReturn(elevatedSecurityCheck);

        final LoginService loginService = new LoginServiceImpl(loginManager, applicationProperties, userManager,
                mockJiraContactHelper, mockJiraAuthenticationContext)
        {
            @Override
            boolean isPublicMode()
            {
                return isPublic;
            }
        };
        final LoginProperties loginProperties = loginService.getLoginProperties(null, mockRequest);
        assertFalse(loginProperties.getLoginFailedByPermissions());
        assertFalse(loginProperties.isCaptchaFailure());
        assertFalse(loginProperties.isLoginSucceeded());

        if(allowCookies)
        {
            assertTrue(loginProperties.isAllowCookies());
        }
        else
        {
            assertFalse(loginProperties.isAllowCookies());
        }
        if(externalUser)
        {
            assertTrue(loginProperties.isExternalUserManagement());
        }
        else
        {
            assertFalse(loginProperties.isExternalUserManagement());
        }
        if(elevatedSecurityCheck)
        {
            assertTrue(loginProperties.isElevatedSecurityCheckShown());
        }
        else
        {
            assertFalse(loginProperties.isElevatedSecurityCheckShown());
        }
        if(isPublic)
        {
            assertTrue(loginProperties.isPublicMode());
        }
        else
        {
            assertFalse(loginProperties.isPublicMode());
        }
    }
}
