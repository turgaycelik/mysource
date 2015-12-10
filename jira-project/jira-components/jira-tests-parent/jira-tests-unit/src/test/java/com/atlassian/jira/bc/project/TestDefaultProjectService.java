package com.atlassian.jira.bc.project;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.ImmutableUser;
import com.atlassian.jira.association.NodeAssociationStore;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.bc.EntityNotFoundException;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.MockJiraServiceContext;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.bc.project.component.MockProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.project.ProjectEventManager;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.issue.security.IssueSecurityLevelScheme;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.MockProjectManager;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.project.MockVersion;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.project.AssigneeTypes;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectImpl;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.util.ProjectKeyStore;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeFactory;
import com.atlassian.jira.security.DefaultGlobalPermissionManager;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.MockGlobalPermissionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.sharing.SharePermissionDeleteUtils;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.MockUserManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.NoopI18nFactory;
import com.atlassian.jira.util.NoopI18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
public class TestDefaultProjectService
{
    private I18nHelper.BeanFactory i18nFactory = new NoopI18nFactory();

    @Rule
    public final RuleChain mockitoMocks = MockitoMocksInContainer.forTest(this);

    @Mock JiraAuthenticationContext jiraAuthenticationContext;
    @Mock AvatarManager avatarManager;
    @Mock PermissionManager permissionManager;
    @Mock ApplicationProperties applicationProperties;
    @Mock ProjectManager projectManager;
    @Mock PermissionSchemeManager permissionSchemeManager;
    @Mock NotificationSchemeManager notificationSchemeManager;
    @Mock IssueSecuritySchemeManager issueSecuritySchemeManager;
    @Mock IssueTypeScreenSchemeManager issueTypeScreenSchemeManager;
    @Mock WorkflowSchemeManager workflowSchemeManager;
    @Mock ProjectEventManager projectEventManager;
    @Mock SchemeFactory schemeFactory;
    @Mock CustomFieldManager customFieldManager;
    @Mock WorkflowManager workflowManager;
    @Mock NodeAssociationStore nodeAssociationStore;
    @Mock VersionManager versionManager;
    @Mock ProjectComponentManager projectComponentManager;
    @Mock SharePermissionDeleteUtils sharePermissionDeleteUtils;
    @Mock ProjectKeyStore projectKeyStore;
    @AvailableInContainer GlobalPermissionManager globalPermissionManager = MockGlobalPermissionManager.withSystemGlobalPermissions();

    MockUserManager userManager;
    MockApplicationUser adminAppUser;
    User admin;

    @Before
    public void setup()
    {
        // prepare the admin user
        userManager = new MockUserManager();
        adminAppUser = new MockApplicationUser("adminKey", "AdMiN");
        admin = adminAppUser.getDirectoryUser();
        userManager.addUser(adminAppUser);
        when(permissionManager.hasPermission(Permissions.ADMINISTER, admin)).thenReturn(true);

        // prepare mocks for the project name/key that we'll try to create
        when(projectManager.getProjectObjByName("projectName")).thenReturn(null);
        when(projectManager.getProjectObjByKey("KEY")).thenReturn(null);
        when(applicationProperties.getDefaultBackedString(APKeys.JIRA_PROJECTKEY_MAX_LENGTH)).thenReturn("10");

        // default max length
        when(applicationProperties.getDefaultBackedString(APKeys.JIRA_PROJECTNAME_MAX_LENGTH)).thenReturn("80");

        when(projectKeyStore.getProjectId(anyString())).thenReturn(null);
    }

    @Test
    public void testRetrieveProjectByIdProjectDoesntExist()
    {
        when(projectManager.getProjectObj(1L)).thenReturn(null);

        DefaultProjectService projectService = new MyProjectService();

        final ProjectService.GetProjectResult projectResult = projectService.getProjectById((User) null, 1L);
        assertNotNull(projectResult);
        assertNull(projectResult.getProject());
        assertThat(projectResult.getErrorCollection().getErrorMessages(), hasItem(NoopI18nHelper.makeTranslation("admin.errors.project.not.found.for.id", 1L)));
    }

    @Test
    public void testRetrieveProjectByIdNoBrowsePermission()
    {
        final Project mockProject = new ProjectImpl(null);
        MockUser user = new MockUser("luser");

        when(permissionManager.hasPermission(Permissions.BROWSE, mockProject, user)).thenReturn(false);
        when(projectManager.getProjectObj(1L)).thenReturn(mockProject);

        DefaultProjectService projectService = new MyProjectService();

        final ProjectService.GetProjectResult projectResult = projectService.getProjectById(user, 1L);
        assertNotNull(projectResult);
        assertNull(projectResult.getProject());
        assertThat(projectResult.getErrorCollection().getErrorMessages(), hasItem(NoopI18nHelper.makeTranslation("admin.errors.project.not.found.for.id", 1L)));
    }

    @Test
    public void testRetrieveProjectById()
    {
        final Project mockProject = new ProjectImpl(null);

        when(permissionManager.hasPermission(Permissions.BROWSE, mockProject, admin)).thenReturn(true);
        when(projectManager.getProjectObj(1L)).thenReturn(mockProject);

        DefaultProjectService projectService = new MyProjectService();

        final ProjectService.GetProjectResult projectResult = projectService.getProjectById(admin, 1L);
        assertNotNull(projectResult);
        assertEquals(mockProject, projectResult.getProject());
        assertFalse(projectResult.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void testRetrieveProjectByKeyAndActionProjectDoesntExist()
    {
        String projectKey = "projectKey";

        when(projectManager.getProjectObjByKey(projectKey)).thenReturn(null);

        DefaultProjectService projectService = new MyProjectService();


        final ProjectService.GetProjectResult projectResult = projectService.getProjectByKeyForAction((User) null, projectKey, ProjectAction.VIEW_ISSUES);
        assertNotNull(projectResult);
        assertNull(projectResult.getProject());
        assertEquals(NoopI18nHelper.makeTranslation("admin.errors.project.not.found.for.key", projectKey),
                projectResult.getErrorCollection().getErrorMessages().iterator().next());
    }

    @Test
    public void testRetrieveProjectByKeyAndActionPermissions()
    {
        final Project project1 = new MockProject(1818L, "ONE");
        final Project project2 = new MockProject(1819L, "TW0");
        final MockUser expectedUser = new MockUser("mockUser");

        when(projectManager.getProjectObjByKey(project1.getKey())).thenReturn(project1);
        when(projectManager.getProjectObjByKey(project2.getKey())).thenReturn(project2);

        // user can BROWSE but not PROJECT admin for project1
        when(permissionManager.hasPermission(Permissions.BROWSE, project1, expectedUser)).thenReturn(true);
        when(permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project1, expectedUser)).thenReturn(false);

        // anon can BROWSE project2
        when(permissionManager.hasPermission(Permissions.BROWSE, project2, (User) null)).thenReturn(true);

        DefaultProjectService projectService = new MyProjectService();


        ProjectService.GetProjectResult projectResult = projectService.getProjectByKeyForAction(expectedUser, project1.getKey(), ProjectAction.EDIT_PROJECT_CONFIG);
        assertNotNull(projectResult);
        assertNull(projectResult.getProject());
        assertEquals(NoopI18nHelper.makeTranslation("admin.errors.project.no.config.permission"),
                projectResult.getErrorCollection().getErrorMessages().iterator().next());

        projectResult = projectService.getProjectByKeyForAction((User) null, project2.getKey(), ProjectAction.VIEW_PROJECT);
        assertNotNull(projectResult);
        assertEquals(project2, projectResult.getProject());
        assertTrue(projectResult.isValid());
    }

    @Test
    public void testRetrieveProjectKeyId()
    {
        final String expectedKey = "KEY";
        final ProjectService.GetProjectResult expectedResult =
                new ProjectService.GetProjectResult(new SimpleErrorCollection());

        final DefaultProjectService projectService = new MyProjectService()
        {
            @Override
            public GetProjectResult getProjectByKeyForAction(User user, String key, ProjectAction action)
            {
                assertEquals(admin, user);
                assertEquals(expectedKey, key);
                assertSame(ProjectAction.VIEW_ISSUES, action);

                return expectedResult;
            }
        };

        ProjectService.GetProjectResult actualResult = projectService.getProjectByKey(admin, expectedKey);
        assertSame(expectedResult, actualResult);
    }

    @Test
    public void testRetrieveProjectsForUserWithAction() throws Exception
    {
        final Project mockProject1 = new MockProject(11781L, "ABC");
        final Project mockProject2 = new MockProject(171718L, "EX");
        final List<Project> projects = Arrays.asList(mockProject1, mockProject2);
        final List<Project> checkedProjects = Lists.newArrayList();

        when(projectManager.getProjectObjects()).thenReturn(projects);

        DefaultProjectService projectService = new MyProjectService()
        {
            @Override
            boolean checkActionPermission(User user, Project project, ProjectAction action)
            {
                checkedProjects.add(project);
                assertEquals(admin, user);
                assertEquals(ProjectAction.EDIT_PROJECT_CONFIG, action);
                return project.equals(mockProject1);
            }
        };

        final List<Project> expectedList = Lists.newArrayList(mockProject1);
        final ServiceOutcome<List<Project>> outcome = projectService.getAllProjectsForAction(admin, ProjectAction.EDIT_PROJECT_CONFIG);
        assertTrue(outcome.isValid());
        assertEquals(expectedList, outcome.getReturnedValue());
        assertEquals(projects, checkedProjects);
    }

    @Test
    public void testRetrieveProjectsForUser() throws Exception
    {
        final Project mockProject1 = createProjectObj("ABC");
        final Project mockProject2 = createProjectObj("EX");
        final List<Project> projectArrayList = Lists.newArrayList(mockProject1, mockProject2);

        DefaultProjectService projectService = new MyProjectService()
        {
            @Override
            public ServiceOutcome<List<Project>> getAllProjectsForAction(User user, ProjectAction action)
            {
                assertEquals(admin, user);
                assertEquals(ProjectAction.VIEW_ISSUES, action);
                return ServiceOutcomeImpl.ok(projectArrayList);
            }
        };

        final ServiceOutcome<List<Project>> outcome = projectService.getAllProjects(admin);
        assertEquals(projectArrayList, outcome.getReturnedValue());
    }

    public static Project createProjectObj(String projectKey)
    {
        return new MockProject(18181L, projectKey);
    }

    @Test
    public void testValidateCreateProjectNoPermission()
    {
        when(permissionManager.hasPermission(Permissions.ADMINISTER, admin)).thenReturn(false);

        DefaultProjectService projectService = new MyProjectService();

        final ProjectService.CreateProjectValidationResult projectResult = projectService.validateCreateProject(admin,
                "projectName", "invalidKey", "description", "admin", null, null);

        assertNotNull(projectResult);
        assertFalse(projectResult.isValid());
        assertThat(projectResult.getErrorCollection().getErrorMessages(), hasItem(NoopI18nHelper.makeTranslation("admin.projects.service.error.no.admin.permission")));
    }

    @Test
    public void testValidateUpdateProjectNoPermissionToView()
    {
        final GenericValue mockProjectGV = new MockGenericValue("Project",
                EasyMap.build("id", 1000L, "key", "HSP", "name", "homosapien"));
        final Project mockProject = new ProjectImpl(mockProjectGV);

        when(projectManager.getProjectObjByKey("HSP")).thenReturn(mockProject);
        when(permissionManager.hasPermission(Permissions.BROWSE, admin)).thenReturn(false);
        when(permissionManager.hasPermission(Permissions.ADMINISTER, admin)).thenReturn(false);
        when(permissionManager.hasPermission(Permissions.PROJECT_ADMIN, mockProject, admin)).thenReturn(false);

        DefaultProjectService projectService = new MyProjectService();

        final ProjectService.UpdateProjectValidationResult projectResult = projectService.validateUpdateProject(admin,
                "projectName", "HSP", "description", "admin", null, null);

        assertNotNull(projectResult);
        assertFalse(projectResult.isValid());
        assertThat(projectResult.getErrorCollection().getErrorMessages(), hasItem(NoopI18nHelper.makeTranslation("admin.errors.project.not.found.for.key", "HSP")));
    }

    @Test
    public void testValidateUpdateProjectNoPermissionToEdit()
    {
        final GenericValue mockProjectGV = new MockGenericValue("Project",
                EasyMap.build("id", 1000L, "key", "HSP", "name", "homosapien"));
        final Project mockProject = new ProjectImpl(mockProjectGV);


        when(permissionManager.hasPermission(Permissions.ADMINISTER, admin)).thenReturn(false);
        when(permissionManager.hasPermission(Permissions.BROWSE, mockProject, admin)).thenReturn(true);
        when(permissionManager.hasPermission(Permissions.PROJECT_ADMIN, mockProject, admin)).thenReturn(false);
        when(projectManager.getProjectObjByKey("HSP")).thenReturn(mockProject);

        DefaultProjectService projectService = new MyProjectService();

        final ProjectService.UpdateProjectValidationResult projectResult = projectService.validateUpdateProject(admin,
                "projectName", "HSP", "description", "admin", null, null);

        assertNotNull(projectResult);
        assertFalse(projectResult.isValid());
        assertThat(projectResult.getErrorCollection().getErrorMessages(), hasItem(NoopI18nHelper.makeTranslation("admin.errors.project.no.config.permission")));
    }

    @Test
    public void testValidateCreateProjectNullValues()
    {
        when(projectManager.getProjectObjByKey(null)).thenReturn(null);
        when(applicationProperties.getDefaultBackedString(APKeys.JIRA_PROJECTKEY_WARNING)).thenReturn("You must specify a valid project key.");
        DefaultProjectService projectService = new MyProjectService()
                .setProjectKeyValid(false)
                .setReserveKeyword(false)
                .setUserExists(false);

        final ProjectService.CreateProjectValidationResult projectResult = projectService.validateCreateProject(admin,
                null, null, null, null, null, null);

        assertNotNull(projectResult);
        assertFalse(projectResult.isValid());
        assertTrue(projectResult.getErrorCollection().hasAnyErrors());
        assertEquals("You must specify a valid project name.",
                projectResult.getErrorCollection().getErrors().get("projectName"));
        assertEquals("You must specify a valid project key.", projectResult.getErrorCollection().getErrors().get("projectKey"));
    }

    @Test
    public void testValidateUpdateProjectNullValues()
    {
        when(projectManager.getProjectObjByKey(null)).thenReturn(null);

        DefaultProjectService projectService = new MyProjectService()
                .setProjectKeyValid(false)
                .setReserveKeyword(false)
                .setUserExists(false);

        final ProjectService.UpdateProjectValidationResult projectResult = projectService.validateUpdateProject(admin,
                null, null, null, null, null, null);

        assertNotNull(projectResult);
        assertFalse(projectResult.isValid());
        assertTrue(projectResult.getErrorCollection().hasAnyErrors());
        assertThat(projectResult.getErrorCollection().getErrorMessages(), hasItem(NoopI18nHelper.makeTranslation("admin.errors.project.not.found.for.key", (Object) null)));
    }

    @Test
    public void testValidateCreateProjectAlreadyExists()
    {
        final Project mockProject = new MockProject(12L, "KEY", "Already here");

        when(projectManager.getProjectObjByName("projectName")).thenReturn(mockProject);
        when(projectManager.getProjectObjByKey("KEY")).thenReturn(mockProject);
        when(applicationProperties.getDefaultBackedString(APKeys.JIRA_PROJECTKEY_MAX_LENGTH)).thenReturn("10");

        DefaultProjectService projectService = new MyProjectService();

        final ProjectService.CreateProjectValidationResult projectResult = projectService.validateCreateProject(admin,
                "projectName", "KEY", "description", "admin", null, null);

        assertNotNull(projectResult);
        assertFalse(projectResult.isValid());
        assertTrue(projectResult.getErrorCollection().hasAnyErrors());
        assertEquals("A project with that name already exists.",
                projectResult.getErrorCollection().getErrors().get("projectName"));
        assertEquals("Project 'Already here' uses this project key.",
                projectResult.getErrorCollection().getErrors().get("projectKey"));
    }

    @Test
    public void testValidateUpdateProjectNotExists()
    {
        when(projectManager.getProjectObjByKey("KEY")).thenReturn(null);

        DefaultProjectService projectService = new MyProjectService()
                .setProjectKeyValid(true)
                .setReserveKeyword(false)
                .setUserExists(true);

        final ProjectService.UpdateProjectValidationResult projectResult = projectService.validateUpdateProject(admin,
                "projectName", "KEY", "description", "admin", null, null);

        assertNotNull(projectResult);
        assertFalse(projectResult.isValid());
        assertTrue(projectResult.getErrorCollection().hasAnyErrors());
        assertThat(projectResult.getErrorCollection().getErrorMessages(), hasItem(NoopI18nHelper.makeTranslation("admin.errors.project.not.found.for.key", "KEY")));
    }

    @Test
    public void testValidateProjectInvalidKey()
    {
        when(projectManager.getProjectObjByName("projectName")).thenReturn(null);
        when(applicationProperties.getDefaultBackedString(APKeys.JIRA_PROJECTKEY_WARNING)).thenReturn("projectKeyWarning");
        when(projectManager.getProjectObjByKey("invalidKey")).thenReturn(null);

        DefaultProjectService projectService = new MyProjectService()
                .setProjectKeyValid(false)
                .setUserExists(true);

        final ProjectService.CreateProjectValidationResult projectResult = projectService.validateCreateProject(admin,
                "projectName", "invalidKey", "description", "admin", null, null);

        assertNotNull(projectResult);
        assertFalse(projectResult.isValid());
        assertTrue(projectResult.getErrorCollection().hasAnyErrors());
        assertEquals("projectKeyWarning", projectResult.getErrorCollection().getErrors().get("projectKey"));
    }

    @Test
    public void testValidateProjectReservedKeyword()
    {
        when(projectManager.getProjectObjByName("projectName")).thenReturn(null);
        when(projectManager.getProjectObjByKey("invalidKey")).thenReturn(null);
        when(applicationProperties.getDefaultBackedString(APKeys.JIRA_PROJECTNAME_MAX_LENGTH)).thenReturn("80");

        DefaultProjectService projectService = new MyProjectService()
                .setProjectKeyValid(true)
                .setReserveKeyword(true)
                .setUserExists(true);

        final ProjectService.CreateProjectValidationResult projectResult = projectService.validateCreateProject(admin,
                "projectName", "invalidKey", "description", "admin", null, null);

        assertNotNull(projectResult);
        assertFalse(projectResult.isValid());
        assertTrue(projectResult.getErrorCollection().hasAnyErrors());
        assertEquals("This keyword is invalid as it is a reserved word on this operating system.",
                projectResult.getErrorCollection().getErrors().get("projectKey"));
    }

    @Test
    public void testValidateCreateProjectLeadNotexists()
    {
        DefaultProjectService projectService = new MyProjectService()
                .setProjectKeyValid(true)
                .setReserveKeyword(false)
                .setUserExists(false);

        final ProjectService.CreateProjectValidationResult projectResult = projectService.validateCreateProject(admin,
                "projectName", "KEY", "description", "admin", null, null);

        assertNotNull(projectResult);
        assertFalse(projectResult.isValid());
        assertTrue(projectResult.getErrorCollection().hasAnyErrors());
        assertEquals("The user you have specified as project lead does not exist.",
                projectResult.getErrorCollection().getErrors().get("projectLead"));
    }

    @Test
    public void testValidateUpdateProjectLeadNotExist()
    {
        final Project mockProject = new ProjectImpl(null)
        {
            public String getKey()
            {
                return "KEY";
            }
        };

        when(projectManager.getProjectObjByKey("KEY")).thenReturn(mockProject);
        when(projectManager.getProjectObjByName("KEY")).thenReturn(mockProject);
        when(applicationProperties.getDefaultBackedString(APKeys.JIRA_PROJECTNAME_MAX_LENGTH)).thenReturn("80");

        DefaultProjectService projectService = new MyProjectService()
                .setProjectKeyValid(true)
                .setReserveKeyword(false)
                .setUserExists(false);

        final ProjectService.UpdateProjectValidationResult projectResult = projectService.validateUpdateProject(admin,
                "projectName", "KEY", "description", "admin", null, null);

        assertNotNull(projectResult);
        assertFalse(projectResult.isValid());
        assertTrue(projectResult.getErrorCollection().hasAnyErrors());
        assertThat(projectResult.getErrorCollection().getErrors().get("projectLead"), equalTo(NoopI18nHelper.makeTranslation("admin.errors.not.a.valid.user")));
    }

    @Test
    public void testValidateCreateProjectLongNameUrlAndKey()
    {
        String longProjectKey = StringUtils.repeat("B", 11);

        when(projectManager.getProjectObjByKey(longProjectKey)).thenReturn(null);
        when(applicationProperties.getDefaultBackedString(APKeys.JIRA_PROJECTKEY_MAX_LENGTH)).thenReturn("10");

        DefaultProjectService projectService = new MyProjectService()
                .setProjectKeyValid(true)
                .setReserveKeyword(false)
                .setUserExists(true);

        final ProjectService.CreateProjectValidationResult projectResult = projectService.validateCreateProject(admin,
                StringUtils.repeat("A", 81), longProjectKey, "description", "admin", StringUtils.repeat("C", 256), null);

        assertNotNull(projectResult);
        assertFalse(projectResult.isValid());
        assertTrue(projectResult.getErrorCollection().hasAnyErrors());
        assertEquals(3, projectResult.getErrorCollection().getErrors().size());
        assertEquals("The URL must not exceed 255 characters in length.",
                projectResult.getErrorCollection().getErrors().get("projectUrl"));
        assertEquals("The project name must not exceed 80 characters in length.",
                projectResult.getErrorCollection().getErrors().get("projectName"));
        assertEquals("The project key must not exceed 10 characters in length.",
                projectResult.getErrorCollection().getErrors().get("projectKey"));
    }

    @Test
    public void testValidateCreateProjectWithNonDefaultNameLength()
    {
        String longProjectName = StringUtils.repeat("A", 11);

        when(applicationProperties.getDefaultBackedString(APKeys.JIRA_PROJECTKEY_MAX_LENGTH)).thenReturn("10");
        when(applicationProperties.getDefaultBackedString(APKeys.JIRA_PROJECTNAME_MAX_LENGTH)).thenReturn("10");

        DefaultProjectService projectService = new MyProjectService();

        final ProjectService.CreateProjectValidationResult projectResult = projectService.validateCreateProject(admin,
                longProjectName, "KEY", "description", "admin", StringUtils.repeat("C", 200), null);

        assertNotNull(projectResult);
        assertFalse(projectResult.isValid());
        assertTrue(projectResult.getErrorCollection().hasAnyErrors());
        assertEquals(2, projectResult.getErrorCollection().getErrors().size());
        assertEquals("The URL specified is not valid - it must start with http://",
                projectResult.getErrorCollection().getErrors().get("projectUrl"));
        assertEquals("The project name must not exceed 10 characters in length.",
                projectResult.getErrorCollection().getErrors().get("projectName"));
    }

    @Test
    public void testValidateCreateProjectWithTooShortName()
    {
        String longProjectName = StringUtils.repeat("A", 1);

        when(applicationProperties.getDefaultBackedString(APKeys.JIRA_PROJECTKEY_MAX_LENGTH)).thenReturn("10");
        when(applicationProperties.getDefaultBackedString(APKeys.JIRA_PROJECTNAME_MAX_LENGTH)).thenReturn("10");

        DefaultProjectService projectService = new MyProjectService();

        final ProjectService.CreateProjectValidationResult projectResult = projectService.validateCreateProject(admin,
                longProjectName, "KEY", "description", "admin", StringUtils.repeat("C", 200), null);

        assertNotNull(projectResult);
        assertFalse(projectResult.isValid());
        assertTrue(projectResult.getErrorCollection().hasAnyErrors());
        assertEquals(2, projectResult.getErrorCollection().getErrors().size());
        assertEquals("The URL specified is not valid - it must start with http://",
                projectResult.getErrorCollection().getErrors().get("projectUrl"));
        assertEquals("The project name should be at least 2 characters in length.",
                projectResult.getErrorCollection().getErrors().get("projectName"));
    }

    @Test
    public void testValidateCreateProjectWithNonDefaultKeyLength()
    {
        String longProjectKey = StringUtils.repeat("B", 6);
        String validProjectName = StringUtils.repeat("A", 10);

        when(projectManager.getProjectObjByName(validProjectName)).thenReturn(null);
        when(projectManager.getProjectObjByKey(longProjectKey)).thenReturn(null);
        when(applicationProperties.getDefaultBackedString(APKeys.JIRA_PROJECTKEY_MAX_LENGTH)).thenReturn("5");

        DefaultProjectService projectService = new MyProjectService();

        final ProjectService.CreateProjectValidationResult projectResult = projectService.validateCreateProject(admin,
                validProjectName, longProjectKey, "description", "admin", StringUtils.repeat("C", 200), null);

        assertNotNull(projectResult);
        assertFalse(projectResult.isValid());
        assertTrue(projectResult.getErrorCollection().hasAnyErrors());
        assertEquals(2, projectResult.getErrorCollection().getErrors().size());
        assertEquals("The URL specified is not valid - it must start with http://",
                projectResult.getErrorCollection().getErrors().get("projectUrl"));
        assertEquals("The project key must not exceed 5 characters in length.",
                projectResult.getErrorCollection().getErrors().get("projectKey"));
    }

    @Test
    public void testValidateCreateProjectInvalidUrl()
    {
        DefaultProjectService projectService = new MyProjectService();

        final ProjectService.CreateProjectValidationResult projectResult = projectService.validateCreateProject(admin,
                "projectName", "KEY", "description", "admin", "invalidUrl", null);

        assertNotNull(projectResult);
        assertFalse(projectResult.isValid());
        assertTrue(projectResult.getErrorCollection().hasAnyErrors());
        assertEquals("The URL specified is not valid - it must start with http://",
                projectResult.getErrorCollection().getErrors().get("projectUrl"));
    }

    @Test
    public void testValidateUpdateProjectInvalidUrl()
    {
        final Project mockProject = new ProjectImpl(null)
        {
            public String getKey()
            {
                return "KEY";
            }
        };

        when(projectManager.getProjectObjByKey("KEY")).thenReturn(mockProject);
        when(projectManager.getProjectObjByName("projectName")).thenReturn(mockProject);
        when(applicationProperties.getDefaultBackedString(APKeys.JIRA_PROJECTNAME_MAX_LENGTH)).thenReturn("80");

        DefaultProjectService projectService = new MyProjectService()
                .setProjectKeyValid(true)
                .setReserveKeyword(false)
                .setUserExists(true);

        final ProjectService.UpdateProjectValidationResult projectResult = projectService.validateUpdateProject(admin,
                "projectName", "KEY", "description", "admin", "invalidUrl", null);

        assertNotNull(projectResult);
        assertFalse(projectResult.isValid());
        assertTrue(projectResult.getErrorCollection().hasAnyErrors());
        assertThat(projectResult.getErrorCollection().getErrors().get("projectUrl"), equalTo(NoopI18nHelper.makeTranslation("admin.errors.url.specified.is.not.valid")));
    }

    @Test
    public void testValidateUpdateProjectKeyUsedByDifferentProject()
    {
        ProjectManager projectManager = mock(ProjectManager.class);
        final ApplicationUser user = new MockApplicationUser("admin");
        final ApplicationProperties applicationProperties = mock(ApplicationProperties.class);

        final Project mockProject = new MockProject(11L, "KEY");
        final Project abcProject = new MockProject(12L, "ABC");

        when(permissionManager.hasPermission(eq(Permissions.ADMINISTER), any(User.class))).thenReturn(true);
        when(projectManager.getProjectObjByKey("KEY")).thenReturn(mockProject);
        when(projectManager.getProjectObjByKey("ABC")).thenReturn(abcProject);
        when(projectManager.getProjectObjByName("projectName")).thenReturn(mockProject);
        when(applicationProperties.getDefaultBackedString(APKeys.JIRA_PROJECTNAME_MAX_LENGTH)).thenReturn("80");

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, applicationProperties, permissionManager, null,
                null, null, null, null, null, null, null, null, null, null, null, i18nFactory, null, null, null, null)
        {
            boolean isProjectKeyValid(final String key)
            {
                return true;
            }

            boolean isReservedKeyword(final String key)
            {
                return false;
            }

            boolean checkUserExists(final String user)
            {
                return true;
            }
        };

        final ProjectService.UpdateProjectValidationResult projectResult = projectService.validateUpdateProject(user, mockProject,
                "projectName", "ABC", "description", new MockApplicationUser("admin"), null, null, null);

        assertNotNull(projectResult);
        assertThat(projectResult.isValid(), equalTo(false));
        assertTrue(projectResult.getErrorCollection().hasAnyErrors());
        assertThat(projectResult.getErrorCollection().getErrors().get("projectKey"), equalTo(NoopI18nHelper.makeTranslation("admin.errors.project.with.that.key.already.exists", "ABC")));
    }

    @Test
    public void testValidateUpdateProjectKeyByProjectAdmin()
    {
        ProjectManager projectManager = mock(ProjectManager.class);
        final ApplicationUser user = new MockApplicationUser("projectadmin");
        final ApplicationProperties applicationProperties = mock(ApplicationProperties.class);

        final Project abcProject = new MockProject(12L, "ABC");

        when(permissionManager.hasPermission(eq(Permissions.PROJECT_ADMIN), any(Project.class), any(User.class))).thenReturn(true);
        when(projectManager.getProjectObjByKey("ABC")).thenReturn(abcProject);
        when(projectManager.getProjectObjByName("projectName")).thenReturn(abcProject);
        when(applicationProperties.getDefaultBackedString(APKeys.JIRA_PROJECTNAME_MAX_LENGTH)).thenReturn("80");

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, applicationProperties, permissionManager, null,
                null, null, null, null, null, null, null, null, null, null, null, i18nFactory, null, null, null, null)
        {
            boolean isProjectKeyValid(final String key)
            {
                return true;
            }

            boolean isReservedKeyword(final String key)
            {
                return false;
            }

            boolean checkUserExists(final String user)
            {
                return true;
            }
        };

        final ProjectService.UpdateProjectValidationResult projectResult = projectService.validateUpdateProject(user, abcProject,
                "projectName", "NEWABC", "description", new MockApplicationUser("projectadmin"), null, null, null);

        assertNotNull(projectResult);
        assertThat(projectResult.isValid(), equalTo(false));
        assertTrue("there should be errors", projectResult.getErrorCollection().hasAnyErrors());
        assertThat(projectResult.getErrorCollection().getErrors().get("projectKey"),
                equalTo(NoopI18nHelper.makeTranslation("admin.errors.project.no.edit.key.permission")));
    }

    @Test
    public void testValidateUpdateProjectKeyUsedByTheSameProject()
    {
        ProjectManager projectManager = mock(ProjectManager.class);
        final ApplicationUser user = new MockApplicationUser("admin");
        final ApplicationProperties applicationProperties = mock(ApplicationProperties.class);

        final Project mockProject = new MockProject(11L, "KEY");

        when(permissionManager.hasPermission(eq(Permissions.ADMINISTER), any(User.class))).thenReturn(true);
        when(projectManager.getProjectObjByKey("KEY")).thenReturn(mockProject);
        when(projectManager.getProjectObjByKey("ABC")).thenReturn(mockProject);
        when(projectManager.getProjectObjByName("projectName")).thenReturn(mockProject);
        when(applicationProperties.getDefaultBackedString(APKeys.JIRA_PROJECTNAME_MAX_LENGTH)).thenReturn("80");

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, applicationProperties, permissionManager, null,
                null, null, null, null, null, null, null, null, null, null, null, i18nFactory, null, null, null, null)
        {
            boolean isProjectKeyValid(final String key)
            {
                return true;
            }

            boolean isReservedKeyword(final String key)
            {
                return false;
            }

            boolean checkUserExists(final String user)
            {
                return true;
            }
        };

        final ProjectService.UpdateProjectValidationResult projectResult = projectService.validateUpdateProject(user, mockProject,
                "projectName", "ABC", "description", new MockApplicationUser("admin"), null, null, null);

        assertNotNull(projectResult);
        assertThat(projectResult.isValid(), equalTo(true));
    }

    @Test
    public void testValidateUpdateProjectLongNameAndUrl()
    {
        final String projectKey = "KEY";
        final Project mockProject = new ProjectImpl(null)
        {
            @Override
            public String getKey()
            {
                return projectKey;
            }
        };

        when(projectManager.getProjectObjByKey(projectKey)).thenReturn(mockProject);

        DefaultProjectService projectService = new MyProjectService()
        {
            @Override
            public int getMaximumNameLength()
            {
                return 80;
            }
        }.setUserExists(true);

        final ProjectService.UpdateProjectValidationResult projectResult = projectService.validateUpdateProject(admin,
                StringUtils.repeat("N", 151), projectKey, "description", "admin", StringUtils.repeat("U", 256), null);

        assertNotNull(projectResult);
        assertFalse(projectResult.isValid());
        assertTrue(projectResult.getErrorCollection().hasAnyErrors());
        assertEquals(2, projectResult.getErrorCollection().getErrors().size());
        assertThat(projectResult.getErrorCollection().getErrors().get("projectUrl"), equalTo(NoopI18nHelper.makeTranslation("admin.errors.project.url.too.long")));
        assertThat(projectResult.getErrorCollection().getErrors().get("projectName"), equalTo(NoopI18nHelper.makeTranslation("admin.errors.project.name.too.long", projectService.getMaximumNameLength())));
    }

    @Test
    public void testValidateCreateProjectAssigneeType()
    {
        DefaultProjectService projectService = new MyProjectService();

        final ProjectService.CreateProjectValidationResult projectResult = projectService.validateCreateProject(admin,
                "projectName", "KEY", "description", "admin", null, new Long(-1));

        assertNotNull(projectResult);
        assertFalse(projectResult.isValid());
        assertTrue(projectResult.getErrorCollection().hasAnyErrors());
        assertEquals("Invalid default Assignee.", projectResult.getErrorCollection().getErrorMessages().iterator().next());
    }

    @Test
    public void defaultAssigneeShouldBeUnassignedIfGlobalAllowUnassignedSettingIsOn()
    {
        projectManager = new MockProjectManager();
        when(applicationProperties.getOption(APKeys.JIRA_OPTION_ALLOWUNASSIGNED)).thenReturn(true);

        DefaultProjectService projectService = new MyProjectService();

        final ProjectService.CreateProjectValidationResult projectResult = projectService.validateCreateProject(admin,
                "projectName", "KEY", "description", "admin", null, null);

        assertTrue(projectResult.isValid());
        assertThat("assignee type should be null in validation result",projectResult.getAssigneeType(), equalTo(null));

        Project createdProject = projectService.createProject(projectResult);
        assertThat(createdProject.getAssigneeType(), equalTo(AssigneeTypes.UNASSIGNED));
    }

    @Test
    public void defaultAssigneeShouldBeProjectLeadIfGlobalAllowUnassignedSettingIsOff()
    {
        projectManager = new MockProjectManager();
        when(applicationProperties.getOption(APKeys.JIRA_OPTION_ALLOWUNASSIGNED)).thenReturn(false);

        DefaultProjectService projectService = new MyProjectService();

        final ProjectService.CreateProjectValidationResult projectResult = projectService.validateCreateProject(admin,
                "projectName", "KEY", "description", "admin", null, null);

        assertTrue(projectResult.isValid());
        assertThat("assignee type should be null in validation result",projectResult.getAssigneeType(), equalTo(null));

        Project createdProject = projectService.createProject(projectResult);
        assertThat(createdProject.getAssigneeType(), equalTo(AssigneeTypes.PROJECT_LEAD));
    }

    @Test
    public void testValidateUpdateProjectAssigneeType()
    {
        final Project mockProject = new ProjectImpl(null)
        {
            public String getKey()
            {
                return "KEY";
            }
        };
        when(projectManager.getProjectObjByKey("KEY")).thenReturn(mockProject);
        when(projectManager.getProjectObjByName("projectName")).thenReturn(mockProject);
        when(applicationProperties.getDefaultBackedString(APKeys.JIRA_PROJECTNAME_MAX_LENGTH)).thenReturn("80");

        DefaultProjectService projectService = new MyProjectService()
                .setProjectKeyValid(true)
                .setReserveKeyword(false)
                .setUserExists(true);

        final ProjectService.UpdateProjectValidationResult projectResult = projectService.validateUpdateProject(admin,
                "projectName", "KEY", "description", "admin", null, new Long(-1));

        assertNotNull(projectResult);
        assertFalse(projectResult.isValid());
        assertTrue(projectResult.getErrorCollection().hasAnyErrors());
        assertThat(projectResult.getErrorCollection().getErrorMessages(), hasItem(NoopI18nHelper.makeTranslation("admin.errors.invalid.default.assignee")));
    }

    @Test
    public void testValidateSchemesNoPermission()
    {
        when(permissionManager.hasPermission(Permissions.ADMINISTER, admin)).thenReturn(false);

        DefaultProjectService projectService = new MyProjectService();

        final ProjectService.UpdateProjectSchemesValidationResult result = projectService
                .validateUpdateProjectSchemes(admin, 1L, 1L, 1L);
        assertNotNull(result);
        assertFalse(result.isValid());
        assertTrue(result.getErrorCollection().hasAnyErrors());
        assertThat(result.getErrorCollection().getErrorMessages(), hasItem(NoopI18nHelper.makeTranslation("admin.projects.service.error.no.admin.permission")));
    }

    @Test
    public void testValidateSchemesNullSchemes() throws Exception
    {

        DefaultProjectService projectService = new MyProjectService();

        final ProjectService.UpdateProjectSchemesValidationResult projectResult = projectService
                .validateUpdateProjectSchemes(admin,
                        null, null, null);

        assertNotNull(projectResult);
        assertTrue(projectResult.isValid());
    }

    @Test
    public void testValidateSchemesMinusOne() throws Exception
    {
        DefaultProjectService projectService = new MyProjectService();

        final Long schemeId = -1L;
        final ProjectService.UpdateProjectSchemesValidationResult projectResult = projectService
                .validateUpdateProjectSchemes(admin,
                        schemeId, schemeId, schemeId);

        assertNotNull(projectResult);
        assertTrue(projectResult.isValid());
    }

    @Test
    public void testValidateSchemesNotExistEnterprise() throws Exception
    {
        final Long schemeId = 1L;
        when(permissionSchemeManager.getScheme(schemeId)).thenReturn(null);
        when(notificationSchemeManager.getScheme(schemeId)).thenReturn(null);
        when(issueSecuritySchemeManager.getIssueSecurityLevelScheme(schemeId)).thenReturn(null);

        DefaultProjectService projectService = new MyProjectService();

        final ProjectService.UpdateProjectSchemesValidationResult projectResult = projectService
                .validateUpdateProjectSchemes(admin,
                        schemeId, schemeId, schemeId);

        assertNotNull(projectResult);
        assertFalse(projectResult.isValid());
        assertTrue(projectResult.getErrorCollection().hasAnyErrors());

        ErrorCollection errors = projectResult.getErrorCollection();
        assertTrue(errors.hasAnyErrors());
        assertThat(errors.getErrorMessages(), hasItem(NoopI18nHelper.makeTranslation("admin.errors.project.validation.permission.scheme.not.retrieved")));
        assertThat(errors.getErrorMessages(), hasItem(NoopI18nHelper.makeTranslation("admin.errors.project.validation.notification.scheme.not.retrieved")));
        assertThat(errors.getErrorMessages(), hasItem(NoopI18nHelper.makeTranslation("admin.errors.project.validation.issuesecurity.scheme.not.retrieved")));
    }

    @Test
    public void testValidateSchemesExistEnterprise() throws Exception
    {
        final Long schemeId = 1L;
        MockGenericValue permissionScheme = new MockGenericValue("permissionScheme", new HashMap());
        MockGenericValue notificationScheme = new MockGenericValue("notificationScheme", new HashMap());

        when(permissionSchemeManager.getScheme(schemeId)).thenReturn(permissionScheme);
        when(notificationSchemeManager.getScheme(schemeId)).thenReturn(notificationScheme);
        when(issueSecuritySchemeManager.getIssueSecurityLevelScheme(schemeId)).thenReturn(new IssueSecurityLevelScheme(12L, "blah", "", null));

        DefaultProjectService projectService = new MyProjectService();

        final ProjectService.UpdateProjectSchemesValidationResult projectResult = projectService.validateUpdateProjectSchemes(admin, schemeId, schemeId, schemeId);

        assertNotNull(projectResult);
        assertTrue(projectResult.isValid());
    }

    @Test
    public void testCreateProjectNullResult() throws Exception
    {
        DefaultProjectService projectService = new MyProjectService();

        try
        {
            projectService.createProject(null);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            //
        }
    }

    @Test
    public void testUpdateProjectNullResult() throws Exception
    {
        DefaultProjectService projectService = new MyProjectService();

        try
        {
            projectService.updateProject(null);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            //
        }
    }

    @Test
    public void testCreateProjectErrorResult() throws Exception
    {
        DefaultProjectService projectService = new MyProjectService();

        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addError("field", "error");
        ProjectService.CreateProjectValidationResult result = new ProjectService.CreateProjectValidationResult(errorCollection);

        try
        {
            projectService.createProject(result);
            fail();
        }
        catch (IllegalStateException e)
        {
            //
        }
    }

    @Test
    public void testUpdateProjectErrorResult() throws Exception
    {
        DefaultProjectService projectService = new MyProjectService();

        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addError("field", "error");
        ProjectService.UpdateProjectValidationResult result = new ProjectService.UpdateProjectValidationResult(errorCollection);

        try
        {
            projectService.updateProject(result);
            fail();
        }
        catch (IllegalStateException e)
        {
            //
        }
    }

    @Test
    public void testCreateProjectSuccess() throws Exception
    {
        final Project mockProject = new ProjectImpl(null);

        when(projectManager.createProject("projectName", "KEY", null, "adminKey", null, AssigneeTypes.PROJECT_LEAD, null)).thenReturn(mockProject);
        issueTypeScreenSchemeManager.associateWithDefaultScheme(mockProject);
        notificationSchemeManager.addDefaultSchemeToProject(mockProject);
        permissionSchemeManager.addDefaultSchemeToProject(mockProject);
        workflowSchemeManager.clearWorkflowCache();

        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        ProjectService.CreateProjectValidationResult result = new ProjectService.CreateProjectValidationResult(errorCollection,
                "projectName", "KEY", null, "admin", null, null, null, adminAppUser);

        DefaultProjectService projectService = new MyProjectService();
        Project project = projectService.createProject(result);
        assertNotNull(project);
        verify(projectEventManager, times(1)).dispatchProjectCreated(isA(ApplicationUser.class), isA(Project.class));
    }

    @Test
    public void testUpdateProjectSuccess() throws Exception
    {
        final Project mockProject = new ProjectImpl(null)
        {
            public String getKey()
            {
                return "KEY";
            }
        };

        when(projectManager.updateProject(mockProject, "projectName", null, "adminKey", null, null, null, "KEY")).thenReturn(mockProject);

        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        ProjectService.UpdateProjectValidationResult result = new ProjectService.UpdateProjectValidationResult(errorCollection,
                "projectName", "KEY", null, "admin", null, null, null, mockProject, false, adminAppUser);

        DefaultProjectService projectService = new MyProjectService();
        Project project = projectService.updateProject(result);
        assertNotNull(project);
        verify(projectEventManager, times(1)).dispatchProjectUpdated(isA(ApplicationUser.class), isA(Project.class), isA(Project.class));
    }

    @Test
    public void testUpdateSchemesNullResult()
    {
        DefaultProjectService projectService = new MyProjectService();

        final Project mockProject = new ProjectImpl(null);
        try
        {
            projectService.updateProjectSchemes(null, mockProject);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            //
        }
        verify(projectEventManager, times(0)).dispatchProjectUpdated(isA(ApplicationUser.class), isA(Project.class), isA(Project.class));
    }

    @Test
    public void testUpdateSchemesErrorResult()
    {
        DefaultProjectService projectService = new MyProjectService();

        final Project mockProject = new ProjectImpl(null);
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addError("field", "error");
        ProjectService.UpdateProjectSchemesValidationResult result = new ProjectService.UpdateProjectSchemesValidationResult(
                errorCollection);

        try
        {
            projectService.updateProjectSchemes(result, mockProject);
            fail();
        }
        catch (IllegalStateException e)
        {
            //
        }
        verify(projectEventManager, times(0)).dispatchProjectUpdated(isA(ApplicationUser.class), isA(Project.class), isA(Project.class));
    }

    @Test
    public void testUpdateSchemesNullProject()
    {
        DefaultProjectService projectService = new MyProjectService();
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        ProjectService.UpdateProjectSchemesValidationResult result = new ProjectService.UpdateProjectSchemesValidationResult(
                errorCollection);

        try
        {
            projectService.updateProjectSchemes(result, null);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            //
        }
    }

    @Test
    public void testUpdateSchemesSuccessEnterprise() throws Exception
    {
        final Project mockProject = new ProjectImpl(null);
        final Long schemeId = 1L;
        MockGenericValue permissionScheme = new MockGenericValue("permissionScheme", new HashMap());
        MockGenericValue notificationScheme = new MockGenericValue("notificationScheme", new HashMap());

        notificationSchemeManager.removeSchemesFromProject(mockProject);
        when(notificationSchemeManager.getScheme(schemeId)).thenReturn(notificationScheme);
        schemeFactory.getScheme(notificationScheme);
        final Scheme nScheme = new Scheme();
        notificationSchemeManager.addSchemeToProject(mockProject, nScheme);

        permissionSchemeManager.removeSchemesFromProject(mockProject);
        when(permissionSchemeManager.getScheme(schemeId)).thenReturn(permissionScheme);
        schemeFactory.getScheme(permissionScheme);
        final Scheme pScheme = new Scheme();
        permissionSchemeManager.addSchemeToProject(mockProject, pScheme);

        issueSecuritySchemeManager.setSchemeForProject(mockProject, schemeId);

        DefaultProjectService projectService = new MyProjectService();

        ErrorCollection errorCollection = new SimpleErrorCollection();
        ProjectService.UpdateProjectSchemesValidationResult schemesResult
                = new ProjectService.UpdateProjectSchemesValidationResult(errorCollection, schemeId, schemeId, schemeId);
        projectService.updateProjectSchemes(schemesResult, mockProject);
    }

    @Test
    public void testValidateDeleteProject()
    {
        final Project mockProject = new ProjectImpl(null);

        when(projectManager.getProjectObjByKey("HSP")).thenReturn(mockProject);

        DefaultProjectService projectService = new MyProjectService();

        final ProjectService.DeleteProjectValidationResult result = projectService.validateDeleteProject(admin, "HSP");

        assertTrue(result.isValid());
        assertFalse(result.getErrorCollection().hasAnyErrors());
        assertEquals(mockProject, result.getProject());
    }

    @Test
    public void testValidateDeleteProjectNoPermission()
    {
        when(permissionManager.hasPermission(Permissions.ADMINISTER, admin)).thenReturn(false);

        DefaultProjectService projectService = new MyProjectService();

        final ProjectService.DeleteProjectValidationResult result = projectService.validateDeleteProject(admin, "HSP");

        assertFalse(result.isValid());

        assertThat(result.getErrorCollection().getErrorMessages(), hasItem(NoopI18nHelper.makeTranslation("admin.projects.service.error.no.admin.permission")));
    }

    @Test
    public void testValidateDeleteProjectNoProject()
    {
        DefaultProjectService projectService = new MyProjectService()
        {
            public GetProjectResult getProjectByKeyForAction(final User user, final String key, ProjectAction projectAction)
            {
                ErrorCollection errors = new SimpleErrorCollection();
                errors.addErrorMessage("Error retrieving project.");
                return new GetProjectResult(errors);
            }
        };

        final ProjectService.DeleteProjectValidationResult result = projectService.validateDeleteProject(admin, "HSP");

        assertFalse(result.isValid());
        assertEquals("Error retrieving project.", result.getErrorCollection().getErrorMessages().iterator().next());
    }

    @Test
    public void testDeleteProjectNullResult()
    {
        DefaultProjectService projectService = new MyProjectService();

        try
        {
            projectService.deleteProject(admin, null);
            fail("Should have thrown exception");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("You can not delete a project with a null validation result.", e.getMessage());
        }
    }

    @Test
    public void testDeleteProjectInvalidResult()
    {
        ErrorCollection errors = new SimpleErrorCollection();
        errors.addErrorMessage("Something bad happend");
        ProjectService.DeleteProjectValidationResult result = new ProjectService.DeleteProjectValidationResult(errors, null);

        DefaultProjectService projectService = new MyProjectService();

        try
        {
            projectService.deleteProject(admin, result);
            fail("Should have thrown exception");
        }
        catch (IllegalStateException e)
        {
            assertEquals("You can not delete a project with an invalid validation result.", e.getMessage());
        }
    }

    @Test
    public void testDeleteProjectRemoveIssuesException() throws RemoveException
    {
        Project mockProject = new ProjectImpl(null);
        doThrow(new RemoveException("Error deleting issues")).when(projectManager).removeProjectIssues(mockProject);

        DefaultProjectService projectService = new MyProjectService();

        ProjectService.DeleteProjectValidationResult result =
                new ProjectService.DeleteProjectValidationResult(new SimpleErrorCollection(), mockProject);
        final ProjectService.DeleteProjectResult projectResult = projectService.deleteProject(admin, result);

        assertFalse(projectResult.isValid());
        assertThat(projectResult.getErrorCollection().getErrorMessages(), hasItem(NoopI18nHelper.makeTranslation("admin.errors.project.exception.removing", "Error deleting issues")));
        verify(projectEventManager, times(0)).dispatchProjectDeleted(isA(ApplicationUser.class), isA(Project.class));
    }

    @Test
    public void testDeleteProjectRemoveAssociationsThrowsException() throws RemoveException, GenericEntityException
    {
        final GenericValue mockProjectGV = new MockGenericValue("Project", EasyMap.build("key", "HSP", "name", "homosapien", "id", 10000L));
        Project mockProject = new ProjectImpl(mockProjectGV);

        projectManager.removeProjectIssues(mockProject);

        customFieldManager.removeProjectAssociations(mockProject);

        final IssueTypeScreenScheme issueTypeScreenScheme = Mockito.mock(IssueTypeScreenScheme.class);

        when(issueTypeScreenSchemeManager.getIssueTypeScreenScheme(mockProjectGV)).thenReturn(issueTypeScreenScheme);
        issueTypeScreenSchemeManager.removeSchemeAssociation(mockProjectGV, issueTypeScreenScheme);

        Mockito.doThrow(new DataAccessException("Error removing associations")).when(nodeAssociationStore).removeAssociationsFromSource(mockProjectGV);


        DefaultProjectService projectService = new MyProjectService();

        final ProjectService.DeleteProjectValidationResult result =
                new ProjectService.DeleteProjectValidationResult(new SimpleErrorCollection(), mockProject);
        final ProjectService.DeleteProjectResult projectResult = projectService.deleteProject(admin, result);

        assertFalse(projectResult.isValid());
        assertThat
                (
                        projectResult.getErrorCollection().getErrorMessages(),
                        hasItem
                                (
                                        NoopI18nHelper.makeTranslation
                                                (
                                                        "admin.errors.project.exception.removing",
                                                        "Error removing associations")
                                )
                );

        verify(projectEventManager, times(0)).dispatchProjectDeleted(isA(ApplicationUser.class), isA(Project.class));
    }

    @Test
    public void testDeleteProjectProjectComponentThrowsException()
            throws RemoveException, GenericEntityException, EntityNotFoundException
    {
        GenericValue mockProjectGV = new MockGenericValue("Project", EasyMap.build("key", "HSP", "name", "homosapien", "id", new Long(10000)));
        Project mockProject = new ProjectImpl(mockProjectGV);

        projectManager.removeProjectIssues(mockProject);

        customFieldManager.removeProjectAssociations(mockProject);

        IssueTypeScreenScheme mockIssueTypeScreenScheme = mock(IssueTypeScreenScheme.class);

        when(issueTypeScreenSchemeManager.getIssueTypeScreenScheme(mockProjectGV)).thenReturn(mockIssueTypeScreenScheme);
        issueTypeScreenSchemeManager.removeSchemeAssociation(mockProjectGV, mockIssueTypeScreenScheme);

        nodeAssociationStore.removeAssociationsFromSource(mockProjectGV);

        when(versionManager.getVersions(10000L)).thenReturn(Collections.EMPTY_LIST);

        ProjectComponent mockProjectComponent = mock(ProjectComponent.class);
        when(mockProjectComponent.getId()).thenReturn(-99L);

        when(projectComponentManager.findAllForProject(10000L)).thenReturn(EasyList.build(mockProjectComponent));
        doThrow(new EntityNotFoundException("Component could not be found!")).when(projectComponentManager).delete(-99L);

        DefaultProjectService projectService = new MyProjectService();

        ProjectService.DeleteProjectValidationResult result =
                new ProjectService.DeleteProjectValidationResult(new SimpleErrorCollection(), mockProject);
        final ProjectService.DeleteProjectResult projectResult = projectService.deleteProject(admin, result);

        assertFalse(projectResult.isValid());
        assertThat
                (
                        projectResult.getErrorCollection().getErrorMessages(),
                        hasItem
                                (
                                        NoopI18nHelper.makeTranslation
                                                (
                                                        "admin.errors.project.exception.removing",
                                                        "Component could not be found!")
                                )
                );
        verify(projectEventManager, times(0)).dispatchProjectDeleted(isA(ApplicationUser.class), isA(Project.class));
    }

    @Test
    public void testDeleteProjectNoVersionsNorComponents()
            throws RemoveException, GenericEntityException, EntityNotFoundException
    {
        GenericValue mockProjectGV = new MockGenericValue("Project", ImmutableMap.of("key", "HSP", "name", "homosapien", "id", new Long(10000)));
        Project mockProject = new ProjectImpl(mockProjectGV);

        projectManager.removeProjectIssues(mockProject);

        customFieldManager.removeProjectAssociations(mockProject);

        IssueTypeScreenScheme mockIssueTypeScreenScheme = mock(IssueTypeScreenScheme.class);

        when(issueTypeScreenSchemeManager.getIssueTypeScreenScheme(mockProjectGV)).thenReturn(mockIssueTypeScreenScheme);
        issueTypeScreenSchemeManager.removeSchemeAssociation(mockProjectGV, mockIssueTypeScreenScheme);

        nodeAssociationStore.removeAssociationsFromSource(mockProjectGV);

        when(versionManager.getVersions(10000L)).thenReturn(Collections.EMPTY_LIST);

        when(projectComponentManager.findAllForProject(10000L)).thenReturn(Collections.EMPTY_LIST);


        projectManager.removeProject(mockProject);

        projectManager.refresh();

        when(workflowSchemeManager.getSchemeFor(mockProject)).thenReturn(null);
        when(workflowSchemeManager.cleanUpSchemeDraft(mockProject, admin)).thenReturn(null);
        workflowSchemeManager.clearWorkflowCache();



        DefaultProjectService projectService = new MyProjectService();

        ProjectService.DeleteProjectValidationResult result =
                new ProjectService.DeleteProjectValidationResult(new SimpleErrorCollection(), mockProject);
        final ProjectService.DeleteProjectResult projectResult = projectService.deleteProject(admin, result);

        assertTrue(projectResult.isValid());

        verify(sharePermissionDeleteUtils).deleteProjectSharePermissions(10000L);
        verify(projectEventManager, times(1)).dispatchProjectDeleted(isA(ApplicationUser.class), isA(Project.class));
    }

    @Test
    public void testDeleteProject() throws RemoveException, GenericEntityException, EntityNotFoundException
    {
        final User user = ImmutableUser.newUser().name("admin").toUser();

        final GenericValue mockProjectGV = new MockGenericValue("Project", ImmutableMap.of("key", "HSP", "name", "homosapien", "id", 10000L));
        Project mockProject = new ProjectImpl(mockProjectGV);

        final IssueTypeScreenScheme mockIssueTypeScreenScheme = Mockito.mock(IssueTypeScreenScheme.class);

        when(issueTypeScreenSchemeManager.getIssueTypeScreenScheme(mockProjectGV)).thenReturn(mockIssueTypeScreenScheme);

        final Version mockVersion = new MockVersion(1000L, "the-version-to-delete");

        when(versionManager.getVersions(10000L)).thenReturn(ImmutableList.of(mockVersion));

        final ProjectComponent mockProjectComponent = new MockProjectComponent(12L, "the-component-to-delete");

        when(projectComponentManager.findAllForProject(10000L)).thenReturn(ImmutableList.of(mockProjectComponent));

        when(workflowSchemeManager.getSchemeFor(mockProject)).thenReturn(null);

        final List<JiraWorkflow> workflowsUsedByProject = Collections.emptyList();
        when(workflowManager.getWorkflowsFromScheme((Scheme) null)).thenReturn(workflowsUsedByProject);

        final DefaultProjectService projectService = new MyProjectService();

        ProjectService.DeleteProjectValidationResult result =
                new ProjectService.DeleteProjectValidationResult(new SimpleErrorCollection(), mockProject);
        final ProjectService.DeleteProjectResult projectResult = projectService.deleteProject(user, result);

        assertTrue(projectResult.isValid());

        Mockito.verify(projectManager).removeProjectIssues(mockProject);
        Mockito.verify(projectManager).removeProject(mockProject);
        Mockito.verify(projectManager).refresh();

        Mockito.verify(customFieldManager).removeProjectAssociations(mockProject);

        Mockito.verify(issueTypeScreenSchemeManager).removeSchemeAssociation(mockProjectGV, mockIssueTypeScreenScheme);

        Mockito.verify(nodeAssociationStore).removeAssociationsFromSource(mockProjectGV);

        Mockito.verify(versionManager).deleteVersion(mockVersion);
        Mockito.verify(projectComponentManager).delete(12L);

        Mockito.verify(sharePermissionDeleteUtils).deleteProjectSharePermissions(10000L);
        Mockito.verify(workflowSchemeManager).clearWorkflowCache();
        Mockito.verify(workflowManager).copyAndDeleteDraftsForInactiveWorkflowsIn(user, workflowsUsedByProject);
        verify(projectEventManager, times(1)).dispatchProjectDeleted(isA(ApplicationUser.class), isA(Project.class));
    }

    /**
     * Inner project service class to get around some nasty calls to static methods in DefaultProjectService.
     */
    class MyProjectService extends DefaultProjectService
    {
        private boolean projectKeyValid = true;
        private boolean reserveKeyword = false;
        private boolean userExists = true;

        public MyProjectService()
        {
            super(TestDefaultProjectService.this.jiraAuthenticationContext,
                    TestDefaultProjectService.this.projectManager,
                    TestDefaultProjectService.this.applicationProperties,
                    TestDefaultProjectService.this.permissionManager,
                    permissionSchemeManager,
                    notificationSchemeManager,
                    issueSecuritySchemeManager,
                    schemeFactory,
                    workflowSchemeManager,
                    issueTypeScreenSchemeManager,
                    customFieldManager,
                    nodeAssociationStore,
                    versionManager,
                    projectComponentManager,
                    sharePermissionDeleteUtils,
                    avatarManager,
                    TestDefaultProjectService.this.i18nFactory,
                    workflowManager,
                    TestDefaultProjectService.this.userManager,
                    TestDefaultProjectService.this.projectEventManager,
                    projectKeyStore);
        }

        public boolean isProjectKeyValid(final String key)
        {
            return projectKeyValid;
        }

        public MyProjectService setProjectKeyValid(final boolean projectKeyValid)
        {
            this.projectKeyValid = projectKeyValid;
            return this;
        }

        public boolean isReservedKeyword(final String key)
        {
            return reserveKeyword;
        }

        public MyProjectService setReserveKeyword(final boolean reserveKeyword)
        {
            this.reserveKeyword = reserveKeyword;
            return this;
        }

        public boolean checkUserExists(final String user)
        {
            return userExists;
        }

        public MyProjectService setUserExists(final boolean userExists)
        {
            this.userExists = userExists;
            return this;
        }

        @Override
        protected JiraServiceContext getServiceContext(final User user, final ErrorCollection errorCollection)
        {
            return new MockJiraServiceContext(user, errorCollection);
        }
    }
}
