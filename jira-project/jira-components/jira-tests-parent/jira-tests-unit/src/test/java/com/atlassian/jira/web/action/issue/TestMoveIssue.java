package com.atlassian.jira.web.action.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.IssueTypeField;
import com.atlassian.jira.issue.fields.ProjectSystemField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockHttp;
import com.atlassian.jira.junit.rules.MockitoContainer;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.UserIssueHistoryManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.bean.MoveIssueBean;
import com.atlassian.jira.web.util.AuthorizationSupport;
import com.atlassian.jira.web.util.DefaultAuthorizationSupport;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.plugin.PluginAccessor;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;
import webwork.action.ActionContext;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestMoveIssue
{
    public static final long ISSUE_ID = 10l;
    public static final long PROJECT_ID = 1l;

    @Rule
    public MockitoContainer mockitoContainer = new MockitoContainer(this);

    @Rule
    public final MockHttp.DefaultMocks mockHttp = MockHttp.withDefaultMocks();

    @Mock
    @AvailableInContainer
    private SubTaskManager subTaskManager;
    @Mock
    @AvailableInContainer
    private ConstantsManager constantsManager;
    @Mock
    @AvailableInContainer
    private WorkflowManager workflowManager;
    @Mock
    @AvailableInContainer
    private FieldManager fieldManager;
    @Mock
    @AvailableInContainer
    private FieldLayoutManager fieldLayoutManager;
    @Mock
    @AvailableInContainer
    private IssueFactory issueFactory;
    @Mock
    @AvailableInContainer
    private FieldScreenRendererFactory fieldScreenRendererFactory;
    @Mock
    @AvailableInContainer
    private CommentService commentService;
    @Mock
    @AvailableInContainer
    private UserUtil userUtil;
    @Mock
    @AvailableInContainer
    private PermissionManager permissionManager;
    @Mock
    @AvailableInContainer
    private GlobalPermissionManager globalPermissionManager;
    @Mock
    @AvailableInContainer
    private UserIssueHistoryManager userHistoryManager;
    @Mock
    @AvailableInContainer
    private IssueManager issueManager;
    @Mock
    @AvailableInContainer
    private ProjectManager projectManager;

    private AuthorizationSupport authorizationSupport;

    @Mock
    @AvailableInContainer
    private JiraAuthenticationContext jiraAuthenticationContext;

    @Mock
    @AvailableInContainer
    private PermissionSchemeManager permissionSchemeManager;
    @Mock
    @AvailableInContainer
    private PluginAccessor pluginAccessor;

    @AvailableInContainer (instantiateMe = true)
    private MockI18nHelper i18nHelper;

    private ApplicationUser currentUser = new MockApplicationUser("user");
    private User mockUser = currentUser.getDirectoryUser();

    private final Issue issue = new MockIssue(ISSUE_ID);

    @Before
    public void setUp()
    {
        when(jiraAuthenticationContext.getI18nHelper()).thenReturn(i18nHelper);
        when(jiraAuthenticationContext.getUser()).thenReturn(currentUser);

        when(jiraAuthenticationContext.getLoggedInUser()).thenReturn(mockUser);

        authorizationSupport = new DefaultAuthorizationSupport(permissionManager, jiraAuthenticationContext);
        mockitoContainer.getMockWorker().addMock(AuthorizationSupport.class, authorizationSupport);

        mockHttp.mockRequest().setAttribute(AbstractIssueSelectAction.PREPOPULATED_ISSUE_OBJECT, issue);
    }

    @After
    public void tearDown()
    {
        ActionContext.setSingleValueParameters(ImmutableMap.of());
        ActionContext.getSession().remove(SessionKeys.MOVEISSUEBEAN);
    }

    @Test
    public void doDefaultError()
            throws Exception
    {
        assertEquals("Result action is error", Action.ERROR, createMoveIssue().doDefault());
    }

    @Test
    public void doDefaultSecurityBreach()
            throws Exception
    {
        mockPermissions(issue, Permissions.BROWSE);

        assertEquals("Result action is security breach", "securitybreach", createMoveIssue().doDefault());
    }

    @Test
    public void doDefaultIssueNotFound()
            throws Exception
    {
        mockPermissions(issue, Permissions.BROWSE, Permissions.MOVE_ISSUE);

        mockHttp.mockRequest().setAttribute(AbstractIssueSelectAction.PREPOPULATED_ISSUE_OBJECT, null);

        assertEquals("Result action is ERROR", Action.ERROR, createMoveIssue().doDefault());
    }

    @Test
    public void doDefaultPermissionDenied()
            throws Exception
    {
        assertEquals("Result action is ERROR", Action.ERROR, createMoveIssue().doDefault());
    }

    @Test
    public void doDefaultNewMoveIssueBean()
            throws Exception
    {
        mockPermissions(issue, Permissions.BROWSE, Permissions.MOVE_ISSUE);

        when(fieldManager.getProjectField()).thenReturn(mock(ProjectSystemField.class));
        when(fieldManager.getIssueTypeField()).thenReturn(mock(IssueTypeField.class));

        final MoveIssue moveIssue = createMoveIssue();
        assertEquals("Result action is INPUT", Action.INPUT, moveIssue.doDefault());

        final MoveIssueBean moveIssueBean = (MoveIssueBean) ActionContext.getSession().get(SessionKeys.MOVEISSUEBEAN);
        assertNotNull("Move issue bean is not null", moveIssueBean);

        assertEquals("Current step is equal 1", 1, moveIssueBean.getCurrentStep());
    }

    @Test
    public void doDefaultResetBean() throws Exception
    {
        final MoveIssueBean mockMoveIssueBean = mock(MoveIssueBean.class);
        ActionContext.getSession().put(SessionKeys.MOVEISSUEBEAN, mockMoveIssueBean);

        mockPermissions(issue, Permissions.BROWSE, Permissions.MOVE_ISSUE);

        when(fieldManager.getProjectField()).thenReturn(mock(ProjectSystemField.class));
        when(fieldManager.getIssueTypeField()).thenReturn(mock(IssueTypeField.class));

        ActionContext.setSingleValueParameters(ImmutableMap.of("reset", "true"));

        final MoveIssue moveIssue = createMoveIssue();
        assertEquals("Result action is INPUT", Action.INPUT, moveIssue.doDefault());

        verify(mockMoveIssueBean).reset();
    }

    @Test
    public void doDefaultResetBeanWhenNoBeanInSession() throws Exception
    {
        final MoveIssueBean mockMoveIssueBean = mock(MoveIssueBean.class);

        mockPermissions(issue, Permissions.BROWSE, Permissions.MOVE_ISSUE);

        when(fieldManager.getProjectField()).thenReturn(mock(ProjectSystemField.class));
        when(fieldManager.getIssueTypeField()).thenReturn(mock(IssueTypeField.class));

        ActionContext.setSingleValueParameters(ImmutableMap.of("reset", "true"));

        final MoveIssue moveIssue = createMoveIssue();
        assertEquals("Result action is INPUT", Action.INPUT, moveIssue.doDefault());

        verify(mockMoveIssueBean, never()).reset();
    }

    @Test
    public void doValidationMoveIssueBeanIsNull()
    {
        // Explicitly null out the session value, else this test is flakey due to execution order
        ActionContext.getSession().put(SessionKeys.MOVEISSUEBEAN, null);
        
        final MoveIssue moveIssue = createMoveIssue();
        moveIssue.doValidation();
        assertEquals("No error messages", moveIssue.getErrorMessages().size(), 0);
    }

    @Test
    public void doValidationMoveIssueBeanIsNotNullWithoutPermission()
    {
        final MoveIssueBean mockMoveIssueBean = mock(MoveIssueBean.class);
        ActionContext.getSession().put(SessionKeys.MOVEISSUEBEAN, mockMoveIssueBean);

        mockPermissions(issue, Permissions.BROWSE);

        final MoveIssue moveIssue = createMoveIssue();
        moveIssue.doValidation();
        assertEquals(moveIssue.getErrorMessages(), ImmutableList.of("move.issue.nopermissions"));
    }

    @Test
    public void doValidationMoveIssueBeanIsNotNullNoChange()
    {
        final MoveIssueBean mockMoveIssueBean = mock(MoveIssueBean.class);
        when(mockMoveIssueBean.getTargetPid()).thenReturn(PROJECT_ID);
        ActionContext.getSession().put(SessionKeys.MOVEISSUEBEAN, mockMoveIssueBean);

        when(fieldManager.getProjectField()).thenReturn(mock(ProjectSystemField.class));
        when(fieldManager.getIssueTypeField()).thenReturn(mock(IssueTypeField.class));

        final MockIssue mockIssue = new MockIssue(ISSUE_ID)
        {
            @Override
            public Collection getAttachments()
            {
                return Collections.emptyList();
            }
        };

        mockIssue.setGenericValue(mockIssue.getGenericValue());
        mockIssue.getGenericValue().set("project", PROJECT_ID);
        mockIssue.getGenericValue().set("type", "story");
        when(mockMoveIssueBean.getTargetIssueType()).thenReturn("story");

        final Project mockProject = mock(Project.class);
        when(projectManager.getProjectObj(PROJECT_ID)).thenReturn(mockProject);

        mockPermissions(mockIssue, Permissions.BROWSE, Permissions.MOVE_ISSUE);
        mockPermissions(mockProject, Permissions.CREATE_ISSUE);

        mockHttp.mockRequest().setAttribute(AbstractIssueSelectAction.PREPOPULATED_ISSUE_OBJECT, mockIssue);

        final MoveIssue moveIssue = createMoveIssue();
        moveIssue.doValidation();

        assertEquals(ImmutableList.of("move.issue.nochange"), moveIssue.getErrorMessages());
    }

    @Test
    public void getAllowedProjectsWorkflowRestrictions()
    {
        final GenericValue mockProject = mock(GenericValue.class);
        when(mockProject.getEntityName()).thenReturn("Project");
        final List<GenericValue> projects = Collections.singletonList(mockProject);

        when(projectManager.getProjects()).thenReturn(projects);

        MoveIssue moveIssue = createMoveIssue();

        mockitoContainer.getMockWorker().addMock(PermissionManager.class, permissionManager);

        when(permissionManager.getProjects(Permissions.CREATE_ISSUE, mockUser)).thenReturn(projects);
        assertEquals(projects, moveIssue.getAllowedProjects());

        when(permissionManager.getProjects(Permissions.CREATE_ISSUE, mockUser)).thenReturn(Collections.<GenericValue>emptyList());
        assertEquals(Collections.emptyList(), moveIssue.getAllowedProjects());
    }

    private void mockPermissions(Issue issue, Integer... permissions)
    {
        for (Integer permission : permissions)
        {
            when(permissionManager.hasPermission(permission, issue, currentUser)).thenReturn(Boolean.TRUE);
            when(permissionManager.hasPermission(permission, issue, mockUser)).thenReturn(Boolean.TRUE);
        }
    }

    private void mockPermissions(Project project, Integer... permissions)
    {
        for (Integer permission : permissions)
        {
            when(permissionManager.hasPermission(permission, project, currentUser)).thenReturn(Boolean.TRUE);
            when(permissionManager.hasPermission(permission, project, mockUser)).thenReturn(Boolean.TRUE);
        }
    }

    private MoveIssue createMoveIssue()
    {
        final MoveIssue moveIssue = new MoveIssue(subTaskManager, constantsManager, workflowManager, fieldManager,
                fieldLayoutManager, issueFactory, fieldScreenRendererFactory, commentService, userUtil);
        moveIssue.setId(ISSUE_ID);

        return moveIssue;
    }

}
