package com.atlassian.jira.plugin.webfragment.conditions;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.mock.servlet.MockHttpServletRequest;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.NoopI18nHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestCanAdministerProjectCondition
{
    @Mock
    private PermissionManager permissionManager;

    @Mock
    private ProjectManager projectManager;

    private JiraAuthenticationContext jac;
    private User user;
    private CanAdministerProjectCondition canAdministerProjectCondition;
    private HttpServletRequest request;

    @Before
    public void setup()
    {
        user = new MockUser("bbain");
        jac = new MockSimpleAuthenticationContext(user, Locale.ENGLISH, new NoopI18nHelper());
        request = new MockHttpServletRequest();
        canAdministerProjectCondition = new CanAdministerProjectCondition(permissionManager, jac, projectManager)
        {
            @Override
            HttpServletRequest getRequest()
            {
                return request;
            }
        };
    }

    @Test
    public void testShouldDisplayNoProject()
    {
        assertFalse(canAdministerProjectCondition.shouldDisplay(Collections.<String, Object>emptyMap()));
    }

    @Test
    public void testShouldDisplayUserNoPermission()
    {
        checkNoPermissionForUser(user);
    }

    @Test
    public void testShouldDisplayAnonymousNoPermission()
    {
        checkNoPermissionForUser(null);
    }

    @Test
    public void testShouldDisplayUserAdminPermission()
    {
        checkAdminPermissionForUser(user);
    }

    @Test
    public void testShouldDisplayAnonymousAdminPermission()
    {
        checkAdminPermissionForUser(null);
    }

    @Test
    public void testShouldDisplaylUserProjectAdminPermission()
    {
        checkProjectAdminPermissionForUser(user);
    }

    @Test
    public void testShouldDisplayAnonymousProjectAdminPermission()
    {
        checkProjectAdminPermissionForUser(null);
    }

    @Test
    public void testShouldDisplayUserNoRequest()
    {
        checkProjectAdminNoRequest(user);
    }

    @Test
    public void testShouldDisplayAnonymousNoRequest()
    {
        checkProjectAdminNoRequest(null);
    }

    private void checkProjectAdminNoRequest(User user)
    {
        request = null;
        jac.setLoggedInUser(user);
        Project project = new MockProject(2881L, "ONE");

        when(permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, user)).thenReturn(true);

        assertTrue(canAdministerProjectCondition.shouldDisplay(createProjectContext(project)));
        assertTrue(canAdministerProjectCondition.shouldDisplay(createProjectContext(project)));

        verify(permissionManager, times(2)).hasPermission(Permissions.ADMINISTER, user);
        verify(permissionManager, times(2)).hasPermission(Permissions.PROJECT_ADMIN, project, user);
    }

    private void checkProjectAdminPermissionForUser(User user)
    {
        jac.setLoggedInUser(user);

        Project project = new MockProject(2881L, "ONE");

        when(permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, user)).thenReturn(true);

        assertTrue(canAdministerProjectCondition.shouldDisplay(createProjectContext(project)));
        assertTrue(canAdministerProjectCondition.shouldDisplay(createProjectContext(project)));

        verify(permissionManager, times(1)).hasPermission(Permissions.ADMINISTER, user);
        verify(permissionManager, times(1)).hasPermission(Permissions.PROJECT_ADMIN, project, user);
    }

    private void checkAdminPermissionForUser(User user)
    {
        jac.setLoggedInUser(user);

        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);

        Project project = new MockProject(2881L, "ONE");
        assertTrue(canAdministerProjectCondition.shouldDisplay(createProjectContext(project)));
        assertTrue(canAdministerProjectCondition.shouldDisplay(createProjectContext(project)));

        verify(permissionManager, times(1)).hasPermission(Permissions.ADMINISTER, user);
    }

    private void checkNoPermissionForUser(User user)
    {
        jac.setLoggedInUser(user);

        Project project = new MockProject(2881L, "ONE");
        assertFalse(canAdministerProjectCondition.shouldDisplay(createProjectContext(project)));
        assertFalse(canAdministerProjectCondition.shouldDisplay(createProjectContext(project)));

        verify(permissionManager, times(1)).hasPermission(Permissions.ADMINISTER, user);
    }

    private static Map<String, Object> createProjectContext(Project project)
    {
        return Collections.<String, Object>singletonMap("project", project);
    }
}
