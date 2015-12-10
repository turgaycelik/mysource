package com.atlassian.jira.user;

import java.util.Collections;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
public class TestDefaultUserProjectHistoryManager
{
    @Rule
    public final RuleChain mockitoMocks = MockitoMocksInContainer.forTest(this);

    @Mock
    private UserHistoryManager historyManager;
    @Mock
    private PermissionManager permissionManager;
    @Mock
    private ProjectManager projectManager;
    @Mock
    private UserProjectHistoryManager projectHistoryManager;
    @Mock
    private User user;
    @Mock @AvailableInContainer
    private GlobalPermissionManager globalPermissionManager;

    @Before
    public void setUp() throws Exception
    {
        user = new MockUser("admin");
        projectHistoryManager = new DefaultUserProjectHistoryManager(historyManager, projectManager, permissionManager);
    }

    @After
    public void tearDown() throws Exception
    {
        user = null;
        historyManager = null;
        projectManager = null;
        permissionManager = null;
        projectHistoryManager = null;
    }

    @Test
    public void testAddProjectNullProject()
    {
        try
        {
            projectHistoryManager.addProjectToHistory(user, null);
            fail("project can not be bull");
        }
        catch (IllegalArgumentException e)
        {
            // pass
        }
    }

    @Test
    public void testAddProjectNullUser()
    {
        final Project project = mock(Project.class);
        when(project.getId()).thenReturn(123l);

        historyManager.addItemToHistory(UserHistoryItem.PROJECT, (com.atlassian.crowd.embedded.api.User) null, "123");
        projectHistoryManager.addProjectToHistory(null, project);
    }

    @Test
    public void testAddProject()
    {
        final Project project = mock(Project.class);
        when(project.getId()).thenReturn(123l);

        historyManager.addItemToHistory(UserHistoryItem.PROJECT, user, "123");
        projectHistoryManager.addProjectToHistory(user, project);
    }

    @Test
    public void testHasProjectHistoryNullUserNullHistory()
    {
        when(historyManager.getHistory(eq(UserHistoryItem.PROJECT), Mockito.any(User.class))).thenReturn(null);
        assertFalse(projectHistoryManager.hasProjectHistory(Permissions.BROWSE, null));
    }


    @Test
    public void testHasProjectHistoryNullHistory()
    {
        when(historyManager.getHistory(UserHistoryItem.PROJECT, user)).thenReturn(null);
        assertFalse(projectHistoryManager.hasProjectHistory(Permissions.BROWSE, user));
    }

    @Test
    public void testHasProjectHistoryEmptyHistory()
    {
        when(historyManager.getHistory(UserHistoryItem.PROJECT, user)).thenReturn(Collections.<UserHistoryItem>emptyList());
        assertFalse(projectHistoryManager.hasProjectHistory(Permissions.BROWSE, user));
    }

    @Test
    public void testHasProjectHistoryNullProject()
    {
        UserHistoryItem item = new UserHistoryItem(UserHistoryItem.PROJECT, "123");

        List<UserHistoryItem> list = CollectionBuilder.newBuilder(item).asList();
        when(historyManager.getHistory(UserHistoryItem.PROJECT, user)).thenReturn(list);
        when(projectManager.getProjectObj(123L)).thenReturn(null);

        assertFalse(projectHistoryManager.hasProjectHistory(Permissions.BROWSE, user));
    }

    @Test
    public void testHasProjectHistoryNoPermission()
    {
        UserHistoryItem item = new UserHistoryItem(UserHistoryItem.PROJECT, "123");

        List<UserHistoryItem> list = CollectionBuilder.newBuilder(item).asList();
        when(historyManager.getHistory(UserHistoryItem.PROJECT, user)).thenReturn(list);

        final Project project = mock(Project.class);

        when(projectManager.getProjectObj(123L)).thenReturn(project);
        when(permissionManager.hasPermission(Permissions.BROWSE, project, user)).thenReturn(false);

        assertFalse(projectHistoryManager.hasProjectHistory(Permissions.BROWSE, user));
    }

    @Test
    public void testHasProjectHistory()
    {
        UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.PROJECT, "123");
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.PROJECT, "1234");
        UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.PROJECT, "1235");
        UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.PROJECT, "1236");

        List<UserHistoryItem> list = CollectionBuilder.newBuilder(item1, item2, item3, item4).asList();
        when(historyManager.getHistory(UserHistoryItem.PROJECT, user)).thenReturn(list);
        when(projectManager.getProjectObj(123L)).thenReturn(null);

        final Project project = mock(Project.class);
        when(projectManager.getProjectObj(1234L)).thenReturn(project);
        when(permissionManager.hasPermission(Permissions.BROWSE, project, user)).thenReturn(false);

        final Project project2 = mock(Project.class);
        when(projectManager.getProjectObj(1235L)).thenReturn(project2);
        when(permissionManager.hasPermission(Permissions.BROWSE, project2, user)).thenReturn(true);

        assertTrue(projectHistoryManager.hasProjectHistory(Permissions.BROWSE, user));
    }

    @Test
    public void testHasProjectHistoryNullUserWithPermissions()
    {
        UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.PROJECT, "123");
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.PROJECT, "1234");
        UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.PROJECT, "1235");
        UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.PROJECT, "1236");

        List<UserHistoryItem> list = CollectionBuilder.newBuilder(item1, item2, item3, item4).asList();
        when(historyManager.getHistory(eq(UserHistoryItem.PROJECT), any(User.class))).thenReturn(list);
        when(projectManager.getProjectObj(123L)).thenReturn(null);

        final Project project = mock(Project.class);
        when(projectManager.getProjectObj(1234L)).thenReturn(project);

        when(permissionManager.hasPermission(eq(Permissions.BROWSE), eq(project), any(User.class))).thenReturn(false);

        final Project project2 = mock(Project.class);
        when(projectManager.getProjectObj(1235L)).thenReturn(project2);
        when(permissionManager.hasPermission(eq(Permissions.BROWSE), eq(project2), any(User.class))).thenReturn(true);

        assertTrue(projectHistoryManager.hasProjectHistory(Permissions.BROWSE, (com.atlassian.crowd.embedded.api.User) null));
    }

    @Test
    public void testGetCurrentProjectNullUserNullHistory()
    {
        when(historyManager.getHistory(eq(UserHistoryItem.PROJECT), any(User.class))).thenReturn(null);

        assertNull(projectHistoryManager.getCurrentProject(Permissions.CLOSE_ISSUE, null));
    }

    @Test
    public void testGetCurrentProjectNullHistory()
    {
        when(historyManager.getHistory(UserHistoryItem.PROJECT, user)).thenReturn(null);

        assertNull(projectHistoryManager.getCurrentProject(Permissions.CLOSE_ISSUE, user));
    }

    @Test
    public void testGetCurrentProjectEmptyHistory()
    {
        when(historyManager.getHistory(UserHistoryItem.PROJECT, user)).thenReturn(Collections.<UserHistoryItem>emptyList());

        assertNull(projectHistoryManager.getCurrentProject(Permissions.CLOSE_ISSUE, user));
    }

    @Test
    public void testGetCurrentProjectNullProject()
    {
        UserHistoryItem item = new UserHistoryItem(UserHistoryItem.PROJECT, "123");

        List<UserHistoryItem> list = CollectionBuilder.newBuilder(item).asList();
        when(historyManager.getHistory(UserHistoryItem.PROJECT, user)).thenReturn(list);

        when(projectManager.getProjectObj(123L)).thenReturn(null);

        assertNull(projectHistoryManager.getCurrentProject(Permissions.CLOSE_ISSUE, user));
    }

    @Test
    public void testGetCurrentProjectNoPermission()
    {
        UserHistoryItem item = new UserHistoryItem(UserHistoryItem.PROJECT, "123");

        List<UserHistoryItem> list = CollectionBuilder.newBuilder(item).asList();
        when(historyManager.getHistory(UserHistoryItem.PROJECT, user)).thenReturn(list);

        final Project project = mock(Project.class);

        when(projectManager.getProjectObj(123L)).thenReturn(project);
        when(permissionManager.hasPermission(Permissions.CLOSE_ISSUE, project, user)).thenReturn(false);

        assertNull(projectHistoryManager.getCurrentProject(Permissions.CLOSE_ISSUE, user));
    }

    @Test
    public void testGetCurrentProject()
    {
        UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.PROJECT, "123");
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.PROJECT, "1234");
        UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.PROJECT, "1235");
        UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.PROJECT, "1236");

        List<UserHistoryItem> list = CollectionBuilder.newBuilder(item1, item2, item3, item4).asList();
        when(historyManager.getHistory(UserHistoryItem.PROJECT, user)).thenReturn(list);
        when(projectManager.getProjectObj(123L)).thenReturn(null);

        final Project project = mock(Project.class);
        when(projectManager.getProjectObj(1234L)).thenReturn(project);
        when(permissionManager.hasPermission(Permissions.CLOSE_ISSUE, project, user)).thenReturn(false);

        final Project project2 = mock(Project.class);
        when(projectManager.getProjectObj(1235L)).thenReturn(project2);
        when(permissionManager.hasPermission(Permissions.CLOSE_ISSUE, project2, user)).thenReturn(true);

        assertEquals(project2, projectHistoryManager.getCurrentProject(Permissions.CLOSE_ISSUE, user));
    }

    @Test
    public void testGetCurrentProjectNullUserWithPermissions()
    {
        UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.PROJECT, "123");
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.PROJECT, "1234");
        UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.PROJECT, "1235");
        UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.PROJECT, "1236");

        List<UserHistoryItem> list = CollectionBuilder.newBuilder(item1, item2, item3, item4).asList();
        when(historyManager.getHistory(eq(UserHistoryItem.PROJECT), any(User.class))).thenReturn(list);
        when(projectManager.getProjectObj(123L)).thenReturn(null);

        final Project project = mock(Project.class);
        when(projectManager.getProjectObj(1234L)).thenReturn(project);
        when(permissionManager.hasPermission(eq(Permissions.CLOSE_ISSUE), eq(project), any(User.class))).thenReturn(false);

        final Project project2 = mock(Project.class);
        when(projectManager.getProjectObj(1235L)).thenReturn(project2);

        when(permissionManager.hasPermission(eq(Permissions.CLOSE_ISSUE), eq(project2), any(User.class))).thenReturn(true);

        assertEquals(project2, projectHistoryManager.getCurrentProject(Permissions.CLOSE_ISSUE, (User) null));
    }

    @Test
    public void testGetProjectHistoryWithChecksNullUserNullHistory()
    {
        when(historyManager.getHistory(eq(UserHistoryItem.PROJECT), any(User.class))).thenReturn(null);

        assertTrue(projectHistoryManager.getProjectHistoryWithPermissionChecks(Permissions.CREATE_ISSUE, (User) null).isEmpty());
    }

    @Test
    public void testGetProjectHistoryWithChecksNullHistory()
    {
        when(historyManager.getHistory(UserHistoryItem.PROJECT, user)).thenReturn(null);

        assertTrue(projectHistoryManager.getProjectHistoryWithPermissionChecks(Permissions.CREATE_ISSUE, user).isEmpty());
    }

    @Test
    public void testGetProjectHistoryWithChecksEmptyHistory()
    {
        when(historyManager.getHistory(UserHistoryItem.PROJECT, user)).thenReturn(Collections.<UserHistoryItem>emptyList());

        assertTrue(projectHistoryManager.getProjectHistoryWithPermissionChecks(Permissions.CREATE_ISSUE, user).isEmpty());
    }

    @Test
    public void testGetProjectHistoryWithChecksNullProject()
    {
        UserHistoryItem item = new UserHistoryItem(UserHistoryItem.PROJECT, "123");

        List<UserHistoryItem> list = CollectionBuilder.newBuilder(item).asList();
        when(historyManager.getHistory(UserHistoryItem.PROJECT, user)).thenReturn(list);
        when(projectManager.getProjectObj(123L)).thenReturn(null);

        assertTrue(projectHistoryManager.getProjectHistoryWithPermissionChecks(Permissions.CREATE_ISSUE, user).isEmpty());
    }

    @Test
    public void testGetProjectHistoryWithChecksNoPermission()
    {
        UserHistoryItem item = new UserHistoryItem(UserHistoryItem.PROJECT, "123");

        List<UserHistoryItem> list = CollectionBuilder.newBuilder(item).asList();
        when(historyManager.getHistory(UserHistoryItem.PROJECT, user)).thenReturn(list);

        final Project project = mock(Project.class);

        when(projectManager.getProjectObj(123L)).thenReturn(project);

        when(permissionManager.hasPermission(Permissions.CREATE_ISSUE, project, user)).thenReturn(false);

        assertTrue(projectHistoryManager.getProjectHistoryWithPermissionChecks(Permissions.CREATE_ISSUE, user).isEmpty());
    }

    @Test
    public void testGetProjectHistoryWithChecks()
    {
        UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.PROJECT, "123");
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.PROJECT, "1234");
        UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.PROJECT, "1235");
        UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.PROJECT, "1236");

        List<UserHistoryItem> list = CollectionBuilder.newBuilder(item1, item2, item3, item4).asList();
        when(historyManager.getHistory(UserHistoryItem.PROJECT, user)).thenReturn(list);

        when(projectManager.getProjectObj(123L)).thenReturn(null);

        final Project project = mock(Project.class);
        when(projectManager.getProjectObj(1234L)).thenReturn(project);
        when(permissionManager.hasPermission(Permissions.CREATE_ISSUE, project, user)).thenReturn(false);

        final Project project2 = mock(Project.class);
        when(projectManager.getProjectObj(1235L)).thenReturn(project2);

        when(permissionManager.hasPermission(Permissions.CREATE_ISSUE, project2, user)).thenReturn(true);

        final Project project3 = mock(Project.class);
        when(projectManager.getProjectObj(1236L)).thenReturn(project3);
        when(permissionManager.hasPermission(Permissions.CREATE_ISSUE, project3, user)).thenReturn(true);

        List<Project> expectedList = CollectionBuilder.newBuilder(project2, project3).asList();
        List<Project> returnedList = projectHistoryManager.getProjectHistoryWithPermissionChecks(Permissions.CREATE_ISSUE, user);

        assertEquals(expectedList, returnedList);
    }

    @Test
    public void testGetProjectHistoryWithOutChecks()
    {
        UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.PROJECT, "123");
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.PROJECT, "1234");
        UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.PROJECT, "1235");
        UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.PROJECT, "1236");

        List<UserHistoryItem> list = CollectionBuilder.newBuilder(item1, item2, item3, item4).asList();
        when(historyManager.getHistory(UserHistoryItem.PROJECT, user)).thenReturn(list);

        List<UserHistoryItem> returnedList = projectHistoryManager.getProjectHistoryWithoutPermissionChecks(user);

        assertEquals(list, returnedList);
    }

    @Test
    public void testGetProjectHistoryWithOutChecksNullUser()
    {
        UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.PROJECT, "123");
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.PROJECT, "1234");
        UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.PROJECT, "1235");
        UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.PROJECT, "1236");

        List<UserHistoryItem> list = CollectionBuilder.newBuilder(item1, item2, item3, item4).asList();
        when(historyManager.getHistory(eq(UserHistoryItem.PROJECT), any(User.class))).thenReturn(list);

        List<UserHistoryItem> returnedList = projectHistoryManager.getProjectHistoryWithoutPermissionChecks(null);

        assertEquals(list, returnedList);
    }


    @Test
    public void testGetProjectHistoryWithOutChecksNullHistory()
    {
        when(historyManager.getHistory(UserHistoryItem.PROJECT, user)).thenReturn(null);

        assertNull(projectHistoryManager.getProjectHistoryWithoutPermissionChecks(user));
    }

    @Test
    public void testGetProjectHistoryWithPermissionChecksUsingProjectAction()
    {
        UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.PROJECT, "123");
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.PROJECT, "1234");
        UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.PROJECT, "1235");
        UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.PROJECT, "1236");

        List<UserHistoryItem> list = CollectionBuilder.newBuilder(item1, item2, item3, item4).asList();
        when(historyManager.getHistory(UserHistoryItem.PROJECT, user)).thenReturn(list);

        when(projectManager.getProjectObj(123L)).thenReturn(null);

        final Project project1 = mock(Project.class);
        when(projectManager.getProjectObj(1234L)).thenReturn(project1);
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(false);
        when(permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project1, user)).thenReturn(true);

        final Project project2 = mock(Project.class);
        when(projectManager.getProjectObj(1235L)).thenReturn(project2);
        when(permissionManager.hasPermission(Permissions.ADMINISTER, project2, user)).thenReturn(true);

        final Project project3 = mock(Project.class);
        when(projectManager.getProjectObj(1236L)).thenReturn(project3);
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(false);
        when(permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project3, user)).thenReturn(false);

        List<Project> expectedList = CollectionBuilder.newBuilder(project1, project2).asList();
        List<Project> returnedList = projectHistoryManager.getProjectHistoryWithPermissionChecks(ProjectAction.EDIT_PROJECT_CONFIG, user);

        assertEquals(expectedList, returnedList);
    }
}
