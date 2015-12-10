package com.atlassian.jira.security;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.security.login.LoginManager;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.util.UserManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith (ListeningMockitoRunner.class)
public class TestJiraRoleMapper
{
    @Mock
    HttpServletRequest httpServletRequest;

    @Mock
    PermissionManager permissionManager;

    @Mock
    LoginManager loginManager;

    @Mock
    UserManager userManager;

    MockApplicationUser fred;

    JiraRoleMapper jiraRoleMapper;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        fred = new MockApplicationUser("fred");
        jiraRoleMapper = new JiraRoleMapper()
        {
            @Override
            LoginManager getLoginManager()
            {
                return loginManager;
            }

            @Override
            UserManager getUserManager()
            {
                return userManager;
            }
        };

        when(userManager.getUserByName(anyString())).thenReturn(fred);
    }

    @Test
    public void testCanLogin_FAIL()
    {
        when(loginManager.authoriseForLogin(fred, httpServletRequest)).thenReturn(false);
        assertFalse(jiraRoleMapper.canLogin(fred, httpServletRequest));
    }

    @Test
    public void testCanLogin_OK()
    {
        when(loginManager.authoriseForLogin(fred, httpServletRequest)).thenReturn(true);
        assertTrue(jiraRoleMapper.canLogin(fred, httpServletRequest));
    }

    @Test
    public void canLoginShouldReturnFalseIfPrincipalIsNull() throws Exception
    {
        assertThat(jiraRoleMapper.canLogin(null, httpServletRequest), equalTo(false));
        verifyNoMoreInteractions(loginManager);
    }
}
