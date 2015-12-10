package com.atlassian.jira.avatar.types;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.types.project.ProjectAvatarAccessPolicy;
import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.MockAvatar;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.Mockito;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProjectAvatarAccessPolicyTest
{
    public static final long AVATAR_ID = 34555;
    public static final long PROJECT_ID = 33;
    private final MockProject mockProject = new MockProject(PROJECT_ID);
    @Rule
    public final TestRule mockInContainer = MockitoMocksInContainer.forTest(this);

    @Mock
    private ProjectManager projectManager;
    @Mock
    private PermissionManager permissionManager;
    @Mock
    private ProjectService projectService;

    @InjectMocks
    private ProjectAvatarAccessPolicy testObj;

    final Avatar systemIssueAvatar = new MockAvatar(AVATAR_ID, "file.jpg", "nonsense", Avatar.Type.ISSUETYPE, null, true);
    final Avatar systemAvatar = new MockAvatar(AVATAR_ID, "file.jpg", "nonsense", Avatar.Type.PROJECT, null, true);
    final Avatar projectAvatar = new MockAvatar(AVATAR_ID, "file.jpg", "nonsense", Avatar.Type.PROJECT, String.valueOf(PROJECT_ID), false);

    private ApplicationUser fredUser = new MockApplicationUser("fred");

    @Before
    public void setUp() throws Exception
    {
    }

    @Test
    public void shouldInvalidTypeAvatarBeInaccesible() throws Exception
    {
        final boolean hasAccess = testObj.userCanViewAvatar(fredUser, systemIssueAvatar);

        assertThat(hasAccess, is(false));
    }

    @Test
    public void shouldSystemAvatarBeAlwaysAccessible() throws Exception
    {
        final boolean hasAccess = testObj.userCanViewAvatar(fredUser, systemAvatar);

        assertThat(hasAccess, is(true));
    }

    @Test
    public void shoudldAvatarForNonExistingProjectBeInaccesible() throws Exception
    {
        final boolean hasAccess = testObj.userCanViewAvatar(fredUser, projectAvatar);

        assertThat(hasAccess, is(false));
    }

    @Test
    public void shoudldAvatarForExistingProjectWithoutPermissionsBeInaccesible() throws Exception
    {
        when(projectManager.getProjectObj(PROJECT_ID)).thenReturn(mockProject);

        final boolean hasAccess = testObj.userCanViewAvatar(fredUser, projectAvatar);

        assertThat(hasAccess, is(false));
    }

    @Test
    public void shoudldAdminHaveAccessToAvatar() throws Exception
    {
        when(projectManager.getProjectObj(PROJECT_ID)).thenReturn(new MockProject(PROJECT_ID));
        when(permissionManager.hasPermission(Permissions.ADMINISTER, fredUser)).thenReturn(true);

        final boolean hasAccess = testObj.userCanViewAvatar(fredUser, projectAvatar);

        assertThat(hasAccess, is(true));
    }

    @Test
    public void shoudldProjectAdminHaveAccessToAvatar() throws Exception
    {
        when(projectManager.getProjectObj(PROJECT_ID)).thenReturn(mockProject);
        when(permissionManager.hasPermission(Permissions.PROJECT_ADMIN, mockProject, fredUser)).thenReturn(true);

        final boolean hasAccess = testObj.userCanViewAvatar(fredUser, projectAvatar);

        assertThat(hasAccess, is(true));
    }

    @Test
    public void shoudldProjectUserHaveAccessToAvatar() throws Exception
    {
        when(projectManager.getProjectObj(PROJECT_ID)).thenReturn(mockProject);
        when(permissionManager.hasPermission(Permissions.BROWSE, mockProject, fredUser)).thenReturn(true);

        final boolean hasAccess = testObj.userCanViewAvatar(fredUser, projectAvatar);

        assertThat(hasAccess, is(true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenInvalidProjectIdIsPassed()
    {
        testObj.userCanCreateAvatarFor(fredUser, "a");
    }

    @Test
    public void shouldAllowAccesWhenProjectServiceResponseIsValid()
    {
        // given
        ProjectService.GetProjectResult mockResult = mock(ProjectService.GetProjectResult.class);
        when(mockResult.isValid()).thenReturn(true);
        when(projectService.
                getProjectByIdForAction(fredUser, PROJECT_ID, ProjectAction.EDIT_PROJECT_CONFIG)).
                thenReturn(mockResult);

        // when
        final boolean userCanCreateAvatar = testObj.userCanCreateAvatarFor(fredUser, String.valueOf(PROJECT_ID));

        // theh
        assertThat(userCanCreateAvatar, is(true));
    }

    @Test
    public void shouldDenyAccesWhenProjectServiceResponseIsNotValid()
    {
        // given
        ProjectService.GetProjectResult mockResult = mock(ProjectService.GetProjectResult.class);
        when(mockResult.isValid()).thenReturn(false);
        when(projectService.
                getProjectByIdForAction(fredUser, PROJECT_ID, ProjectAction.EDIT_PROJECT_CONFIG)).
                thenReturn(mockResult);

        // when
        final boolean userCanCreateAvatar = testObj.userCanCreateAvatarFor(fredUser, String.valueOf(PROJECT_ID));

        // theh
        assertThat(userCanCreateAvatar, is(false));
    }
}
