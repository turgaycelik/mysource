package com.atlassian.jira.bc.project.properties;

import com.atlassian.jira.bc.project.property.ProjectPropertyHelper;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.DefaultGlobalPermissionManager;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.MockGlobalPermissionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static com.atlassian.jira.util.ErrorCollection.Reason.FORBIDDEN;
import static com.atlassian.jira.util.ErrorCollection.Reason.NOT_LOGGED_IN;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v6.2
 */
public class TestProjectPropertyHelper
{
    @Rule
    public final RuleChain mockitoMocks = MockitoMocksInContainer.forTest(this);

    @Mock public PermissionManager permissionManager;
    @Mock public ProjectManager projectManager;
    @AvailableInContainer public GlobalPermissionManager globalPermissionManager = MockGlobalPermissionManager.withSystemGlobalPermissions();
    private final I18nHelper i18n = new MockI18nHelper();

    private ProjectPropertyHelper propertyHelper;

    @Before
    public void setUp()
    {
        this.propertyHelper = new ProjectPropertyHelper(i18n, projectManager, permissionManager);
    }

    @Test
    public void getProjectByKey()
    {
        Project project = mock(Project.class);
        when(projectManager.getProjectObjByKey("HSP")).thenReturn(project);

        assertThat(propertyHelper.getEntityByKeyFunction().apply("HSP").isDefined(), is(true));
        assertThat(propertyHelper.getEntityByKeyFunction().apply("MKY").isDefined(), is(false));
    }

    @Test
    public void getProjectById()
    {
        Project project = mock(Project.class);
        when(projectManager.getProjectObj(1l)).thenReturn(project);

        assertThat(propertyHelper.getEntityByIdFunction().apply(1l).isDefined(), is(true));
        assertThat(propertyHelper.getEntityByIdFunction().apply(2l).isDefined(), is(false));
    }

    @Test
    public void projectAdminHasEditPermission()
    {
        Project project = mock(Project.class);
        ApplicationUser user = mock(ApplicationUser.class);
        when(permissionManager.hasPermission(eq(Permissions.PROJECT_ADMIN), eq(project), eq(user))).thenReturn(true);

        assertThat(propertyHelper.hasEditPermissionFunction().apply(user, project).hasAnyErrors(), is(false));
    }

    @Test
    public void adminHasEditPermission()
    {
        Project project = mock(Project.class);
        ApplicationUser user = mock(ApplicationUser.class);
        when(permissionManager.hasPermission(eq(Permissions.ADMINISTER), eq(user))).thenReturn(true);

        assertThat(propertyHelper.hasEditPermissionFunction().apply(user, project).hasAnyErrors(), is(false));
    }

    @Test
    public void noEditPermission()
    {
        Project project = mock(Project.class);
        ApplicationUser user = mock(ApplicationUser.class);
        when(permissionManager.hasPermission(eq(Permissions.PROJECT_ADMIN), eq(project), eq(user))).thenReturn(false);
        when(permissionManager.hasPermission(eq(Permissions.ADMINISTER), eq(user))).thenReturn(false);

        ErrorCollection errorCollection = propertyHelper.hasEditPermissionFunction().apply(user, project);
        assertHasError(errorCollection, "admin.errors.project.no.config.permission", FORBIDDEN);
    }

    @Test
    public void noEditPermissionForNotLoggedInUser()
    {
        Project project = mock(Project.class);
        ApplicationUser user = mock(ApplicationUser.class);
        when(permissionManager.hasPermission(eq(Permissions.PROJECT_ADMIN), eq(project), any(ApplicationUser.class))).thenReturn(false);
        when(permissionManager.hasPermission(eq(Permissions.ADMINISTER), any(ApplicationUser.class))).thenReturn(false);

        ErrorCollection errorCollection = propertyHelper.hasEditPermissionFunction().apply(null, project);
        assertHasError(errorCollection, "admin.errors.project.no.config.permission", NOT_LOGGED_IN);
    }

    @Test
    public void adminHasReadPermission()
    {
        Project project = mock(Project.class);
        ApplicationUser user = mock(ApplicationUser.class);
        when(permissionManager.hasPermission(eq(Permissions.ADMINISTER), eq(user))).thenReturn(true);

        assertThat(propertyHelper.hasReadPermissionFunction().apply(user, project).hasAnyErrors(), is(false));
    }

    @Test
    public void projectAdminHasReadPermission()
    {
        Project project = mock(Project.class);
        ApplicationUser user = mock(ApplicationUser.class);
        when(permissionManager.hasPermission(eq(Permissions.PROJECT_ADMIN), eq(project), eq(user))).thenReturn(true);

        assertThat(propertyHelper.hasReadPermissionFunction().apply(user, project).hasAnyErrors(), is(false));
    }

    @Test
    public void projectUserHasReadPermission()
    {
        Project project = mock(Project.class);
        ApplicationUser user = mock(ApplicationUser.class);
        when(permissionManager.hasPermission(eq(Permissions.BROWSE), eq(project), eq(user))).thenReturn(true);

        assertThat(propertyHelper.hasReadPermissionFunction().apply(user, project).hasAnyErrors(), is(false));
    }

    @Test
    public void noReadPermission()
    {
        Project project = mock(Project.class);
        ApplicationUser user = mock(ApplicationUser.class);
        when(permissionManager.hasPermission(eq(Permissions.BROWSE), eq(project), eq(user))).thenReturn(false);
        when(permissionManager.hasPermission(eq(Permissions.PROJECT_ADMIN), eq(project), eq(user))).thenReturn(false);
        when(permissionManager.hasPermission(eq(Permissions.ADMINISTER), eq(user))).thenReturn(false);

        ErrorCollection errorCollection = propertyHelper.hasReadPermissionFunction().apply(user, project);
        assertHasError(errorCollection, "admin.errors.project.no.view.permission", FORBIDDEN);
    }

    @Test
    public void noReadPermissionForNotLoggedInUser()
    {
        Project project = mock(Project.class);
        when(permissionManager.hasPermission(eq(Permissions.BROWSE), eq(project), any(ApplicationUser.class))).thenReturn(false);
        when(permissionManager.hasPermission(eq(Permissions.PROJECT_ADMIN), eq(project), any(ApplicationUser.class))).thenReturn(false);
        when(permissionManager.hasPermission(eq(Permissions.ADMINISTER), any(ApplicationUser.class))).thenReturn(false);

        ErrorCollection errorCollection = propertyHelper.hasReadPermissionFunction().apply(null, project);
        assertHasError(errorCollection, "admin.errors.project.no.view.permission", NOT_LOGGED_IN);
    }

    private static void assertHasError(ErrorCollection errorCollection, String errorMsg, ErrorCollection.Reason reason)
    {
        assertThat(errorCollection.hasAnyErrors(), is(true));
        assertThat(errorCollection.getErrorMessages(), hasItem(errorMsg));
        assertThat(errorCollection.getReasons(), hasItem(reason));
    }
}
