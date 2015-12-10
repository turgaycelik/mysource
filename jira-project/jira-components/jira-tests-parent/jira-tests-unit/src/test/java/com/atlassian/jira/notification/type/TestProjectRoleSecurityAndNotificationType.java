package com.atlassian.jira.notification.type;

import java.util.Map;
import java.util.Set;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;

import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestProjectRoleSecurityAndNotificationType
{
    private static final long ROLE_ID = 1;

    private ProjectRoleSecurityAndNotificationType projectRoleSecurityAndNotificationType;
    @Mock private JiraAuthenticationContext mockJiraAuthenticationContext;
    @Mock private ProjectFactory mockProjectFactory;
    @Mock private ProjectRole mockProjectRole;
    @Mock private ProjectRoleManager mockProjectRoleManager;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        projectRoleSecurityAndNotificationType = new ProjectRoleSecurityAndNotificationType(
                mockJiraAuthenticationContext, mockProjectRoleManager, mockProjectFactory);
        when(mockProjectRole.getId()).thenReturn(ROLE_ID);
        when(mockProjectRoleManager.getProjectRole(ROLE_ID)).thenReturn(mockProjectRole);
        when(mockProjectRoleManager.getProjectRoles()).thenReturn(singleton(mockProjectRole));
    }

    @Test
    public void shouldReturnEmptySetWhenProjectRoleActorsCannotBeResolved()
    {
        // Set up
        final Project mockProject = mock(Project.class);
        when(mockProjectRoleManager.getProjectRoleActors(mockProjectRole, mockProject))
                .thenThrow(new IllegalArgumentException());

        // Invoke
        final Set<User> users =
                projectRoleSecurityAndNotificationType.getUsersFromRole(mockProject, String.valueOf(ROLE_ID));

        // Check
        assertTrue(users.isEmpty());
    }

    @Test
    public void validationShouldPassWhenRoleIdIsKnown()
    {
        // Set up
        final String key = "test";
        final Map<String, String> paramMap = ImmutableMap.of(key, String.valueOf(ROLE_ID));

        // Invoke and check
        assertTrue(projectRoleSecurityAndNotificationType.doValidation(key, paramMap));
    }

    @Test
    public void validationShouldFailWhenRoleIdIsUnknown()
    {
        // Set up
        final String key = "test";
        final Map<String, String> paramMap = ImmutableMap.of(key, String.valueOf(ROLE_ID + 1));

        // Invoke and check
        assertFalse(projectRoleSecurityAndNotificationType.doValidation(key, paramMap));
    }

    @Test
    public void testGetArgumentDisplayName()
    {
        // Set up
        final String roleName = "Saboteur";
        when(mockProjectRole.getName()).thenReturn(roleName);

        assertEquals(roleName, projectRoleSecurityAndNotificationType.getArgumentDisplay(String.valueOf(ROLE_ID)));
    }
}
