package com.atlassian.jira.rest.v2.issue;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.fugue.Either;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.avatar.AvatarPickerHelper;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.fields.ColumnService;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.bc.user.UserService;
import com.atlassian.jira.bc.user.UserValidationResultBuilder;
import com.atlassian.jira.bc.user.search.AssigneeService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.MockProjectManager;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.plugin.user.PasswordPolicyManager;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.rest.internal.PermissionHelper;
import com.atlassian.jira.rest.internal.ResponseValidationHelper;
import com.atlassian.jira.rest.testutils.WebExceptionMatcher;
import com.atlassian.jira.rest.util.AttachmentHelper;
import com.atlassian.jira.rest.v2.issue.users.UserPickerResourceHelper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.xsrf.XsrfInvocationChecker;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.EmailFormatter;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserResourceFindUsersWithPermissionsTest
{
    private UserResource usersResource;

    private ResponseValidationHelper validationHelper;

    @Mock
    private UserService userServiceMock;

    private User currentUser;
    private JiraAuthenticationContext authContext;

    @Mock
    private PermissionManager permissionManagerMock;

    @Mock
    private UserUtil userUtilMock;

    @Mock
    private UserValidationResultBuilder validationResultBuilder;

    @Mock
    private UserWriteBean userBean;

    @Mock
    private JiraBaseUrls jiraBaseUrls;

    @Mock
    private TimeZoneManager timeZoneManager;

    @Mock
    private PasswordPolicyManager passwordPolicyManagerMock;

    @Mock
    private UserPickerResourceHelper userPickerHelper;

    @Rule
    public RuleChain chain = MockitoMocksInContainer.forTest(this);

    @Mock
    @AvailableInContainer
    private AvatarService avatarService;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private PermissionHelper permissionHelper;
    private MockProjectManager projectManager;
    private IssueService issueService;

    @Mock
    private UserManager userManager;


    @Before
    public void setUp() throws Exception
    {
        when(jiraBaseUrls.restApi2BaseUrl()).thenReturn(UriBuilder.fromUri("http://localhost").toString());
        projectManager = new MockProjectManager();
        issueService = mock(IssueService.class);

        currentUser = new MockUser("current_user");
        authContext = new MockSimpleAuthenticationContext(currentUser);

        usersResource = new UserResource(
                userServiceMock,
                userUtilMock,
                passwordPolicyManagerMock,
                mock(I18nHelper.class),
                mock(EmailFormatter.class),
                authContext,
                timeZoneManager,
                mock(AvatarPickerHelper.class),
                mock(AvatarManager.class),
                mock(AvatarService.class),
                mock(AttachmentHelper.class),
                mock(UserPropertyManager.class),
                permissionManagerMock,
                mock(ProjectService.class),
                issueService,
                projectManager,
                mock(EventPublisher.class),
                mock(AssigneeService.class),
                mock(IssueManager.class),
                userPickerHelper,
                jiraBaseUrls,
                mock(ColumnService.class),
                mock(XsrfInvocationChecker.class),
                userManager);

    }

    @Test
    public void testParseSinglePermission() throws Exception
    {
        Assert.assertEquals(ImmutableList.of(Permissions.BROWSE), usersResource.parsePermissions("BROWSE"));
    }

    @Test
    public void testParseInvalidPermission() throws Exception
    {
        expectedException.expect(new WebExceptionMatcher(Response.Status.BAD_REQUEST));
        Assert.assertEquals(ImmutableList.of(Permissions.BROWSE), usersResource.parsePermissions("wrong"));
    }

    @Test
    public void testSeveralPermissions() throws Exception
    {
        Assert.assertEquals(ImmutableList.of(Permissions.BROWSE, Permissions.BULK_CHANGE, Permissions.COMMENT_ISSUE),
                usersResource.parsePermissions("BROWSE,BULK_CHANGE,COMMENT_ISSUE"));

    }

    @Test
    public void testSeveralPermissionsWithInvalid() throws Exception
    {
        expectedException.expect(new WebExceptionMatcher(Response.Status.BAD_REQUEST));
        usersResource.parsePermissions("BROWSE,BULK_CHANGE,invalid");
    }

    @Test
    public void testNoPermissionsSpecified() throws Exception
    {
        expectedException.expect(new WebExceptionMatcher(Response.Status.BAD_REQUEST));
        usersResource.findUsersWithAllPermissions(null, null, null, "PRJ", null, null);
    }

    @Test
    public void testEmptyPermissionsSpecified() throws Exception
    {
        expectedException.expect(new WebExceptionMatcher(Response.Status.BAD_REQUEST));
        usersResource.findUsersWithAllPermissions(null, " ", null, "PRJ", null, null);
    }

    @Test
    public void testNeitherIssueNorProject() throws Exception
    {
        expectedException.expect(new WebExceptionMatcher(Response.Status.BAD_REQUEST));
        usersResource.getIssueOrProject(null, null);
    }

    @Test
    public void testFindingValidIssue() throws Exception
    {
        final MockIssue issue = new MockIssue(10000, "PRJ-1");
        when(issueService.getIssue(currentUser, "PRJ-1")).thenReturn(new IssueService.IssueResult(issue));

        Assert.assertEquals(Either.<Project, Issue>right(issue), usersResource.getIssueOrProject("PRJ-1", null));
    }


    @Test
    public void testFindingInvalidIssue() throws Exception
    {
        final SimpleErrorCollection errors = new SimpleErrorCollection();
        errors.addErrorMessage("not found");
        when(issueService.getIssue(Mockito.<User>any(), Mockito.<String>any())).thenReturn(new IssueService.IssueResult(null, errors));

        expectedException.expect(new WebExceptionMatcher(Response.Status.NOT_FOUND));
        usersResource.getIssueOrProject("INVALID-123", null);
    }

    @Test
    public void testFindingInvalidProject() throws Exception
    {
        expectedException.expect(new WebExceptionMatcher(Response.Status.NOT_FOUND));
        usersResource.getIssueOrProject(null, "INVALID");
    }


    @Test
    public void testFindingValidProject() throws Exception
    {
        final Project project = new MockProject(10000, "PRJ");
        projectManager.addProject(project);

        Assert.assertEquals(Either.<Project, Issue>left(project), usersResource.getIssueOrProject(null, "PRJ"));
    }


    @Test
    public void testIssueMoreImportantThanProject() throws Exception
    {
        final Project project = new MockProject(10000, "PRJ");
        projectManager.addProject(project);

        final SimpleErrorCollection errors = new SimpleErrorCollection();
        errors.addErrorMessage("not found");
        when(issueService.getIssue(Mockito.<User>any(), Mockito.<String>any())).thenReturn(new IssueService.IssueResult(null, errors));

        Assert.assertEquals(Either.<Project, Issue>left(project), usersResource.getIssueOrProject(null, "PRJ")); // precondition

        expectedException.expect(new WebExceptionMatcher(Response.Status.NOT_FOUND));
        usersResource.getIssueOrProject("INVALID-123", "PRJ");
    }

    @Test
    public void testIssuePredicate() throws Exception
    {
        final MockIssue issue = new MockIssue(10000, "PRJ-1");

        final User user0 = new MockUser("user0");
        final User user1 = new MockUser("user1");
        final User user2 = new MockUser("user2");

        when(permissionManagerMock.hasPermission(1, issue, user1)).thenReturn(true);
        when(permissionManagerMock.hasPermission(1, issue, user2)).thenReturn(true);
        when(permissionManagerMock.hasPermission(2, issue, user2)).thenReturn(true);

        final ImmutableList<Integer> single = ImmutableList.<Integer>of(1);
        final Predicate<User> predicate1 = usersResource.createIssuePredicate(single, issue);

        Assert.assertFalse(predicate1.apply(user0));
        Assert.assertTrue(predicate1.apply(user1));
        Assert.assertTrue(predicate1.apply(user2));

        final ImmutableList<Integer> multiple = ImmutableList.<Integer>of(1, 2);
        final Predicate<User> predicate2 = usersResource.createIssuePredicate(multiple, issue);

        Assert.assertFalse(predicate2.apply(user0));
        Assert.assertFalse(predicate2.apply(user1));
        Assert.assertTrue(predicate2.apply(user2));

    }

    @Test
    public void testProjectPredicate() throws Exception
    {
        final Project project = new MockProject(10000, "PRJ");

        final User user0 = new MockUser("user0");
        final User user1 = new MockUser("user1");
        final User user2 = new MockUser("user2");

        when(permissionManagerMock.hasPermission(1, project, user1, true)).thenReturn(true);
        when(permissionManagerMock.hasPermission(1, project, user2, true)).thenReturn(true);
        when(permissionManagerMock.hasPermission(2, project, user2, true)).thenReturn(true);

        final ImmutableList<Integer> single = ImmutableList.<Integer>of(1);
        final Predicate<User> predicate1 = usersResource.createProjectPredicate(single, project);

        Assert.assertFalse(predicate1.apply(user0));
        Assert.assertTrue(predicate1.apply(user1));
        Assert.assertTrue(predicate1.apply(user2));

        final ImmutableList<Integer> multiple = ImmutableList.<Integer>of(1, 2);
        final Predicate<User> predicate2 = usersResource.createProjectPredicate(multiple, project);

        Assert.assertFalse(predicate2.apply(user0));
        Assert.assertFalse(predicate2.apply(user1));
        Assert.assertTrue(predicate2.apply(user2));
    }

    @Test
    public void testPermissions() throws Exception
    {
        final Project project = new MockProject(10000, "PRJ");
        projectManager.addProject(project);
        final MockIssue issue = new MockIssue(10000, "PRJ-1");
        issue.setProjectObject(project);
        when(issueService.getIssue(currentUser, "PRJ-1")).thenReturn(new IssueService.IssueResult(issue));

        when(userPickerHelper.findActiveUsers(anyString())).thenReturn(ImmutableList.<User>of());

        when(permissionManagerMock.hasPermission(Permissions.SYSTEM_ADMIN, authContext.getUser())).thenReturn(true);
        usersResource.findUsersWithAllPermissions("", "BROWSE", null, "PRJ", null, null);

        Mockito.reset(permissionManagerMock);
        when(permissionManagerMock.hasPermission(Permissions.ADMINISTER, authContext.getUser())).thenReturn(true);
        usersResource.findUsersWithAllPermissions("", "BROWSE", null, "PRJ", null, null);

        Mockito.reset(permissionManagerMock);
        when(permissionManagerMock.hasPermission(Permissions.PROJECT_ADMIN, project, authContext.getUser())).thenReturn(true);
        usersResource.findUsersWithAllPermissions("", "BROWSE", null, "PRJ", null, null);
        usersResource.findUsersWithAllPermissions("", "BROWSE", "PRJ-1", null, null, null);


        Mockito.reset(permissionManagerMock);
        expectedException.expect(new WebExceptionMatcher(Response.Status.FORBIDDEN));
        usersResource.findUsersWithAllPermissions("", "BROWSE", null, "PRJ", null, null);
    }
}
