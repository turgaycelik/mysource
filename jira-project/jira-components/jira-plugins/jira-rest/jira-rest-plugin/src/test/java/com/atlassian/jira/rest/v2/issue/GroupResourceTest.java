package com.atlassian.jira.rest.v2.issue;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.runtime.OperationFailedException;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.group.GroupRemoveChildMapper;
import com.atlassian.jira.bc.group.GroupService;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.rest.exception.BadRequestWebException;
import com.atlassian.jira.rest.exception.ForbiddenWebException;
import com.atlassian.jira.rest.exception.NotFoundWebException;
import com.atlassian.jira.rest.exception.ServerErrorWebException;
import com.atlassian.jira.rest.internal.PermissionHelper;
import com.atlassian.jira.rest.internal.ResponseValidationHelper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.MockGroup;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.EmailFormatter;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.google.common.collect.ImmutableList;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GroupResourceTest
{
    private static final String GROUP_NAME = "jedi";
    private static final String SWAP_GROUP = "sith";
    private static final String USER_NAME = "luke";

    private GroupResource groupResource;

    private PermissionHelper permissionHelper;

    private ResponseValidationHelper validationHelper;

    @Mock
    private PermissionManager permissionManager;

    @Mock
    private GroupManager groupManager;

    @Mock
    private JiraBaseUrls jiraBaseUrls;

    @Mock
    private CrowdService crowdService;

    @Rule
    public RuleChain chain = MockitoMocksInContainer.forTest(this);

    @Mock
    @AvailableInContainer
    private JiraAuthenticationContext authContext;

    @Mock
    private UserUtil userUtilMock;

    @Mock
    private GroupService groupService;

    @Mock
    @AvailableInContainer
    private ApplicationProperties applicationProperties;

    @Mock
    @AvailableInContainer
    private EmailFormatter emailFormatter;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception
    {
        when(jiraBaseUrls.restApi2BaseUrl()).thenReturn(UriBuilder.fromUri("http://localhost").toString());
        when(applicationProperties.getEncoding()).thenReturn("UTF-8");

        groupResource = new GroupResource(permissionManager,
                authContext,
                mock(I18nHelper.class),
                groupManager,
                groupService, jiraBaseUrls, crowdService, emailFormatter);

        permissionHelper = new PermissionHelper(permissionManager, userUtilMock, authContext);
        validationHelper = new ResponseValidationHelper();
    }

    @Test
    public void testAddGroupAdminHappyPath() throws Exception
    {
        final MockApplicationUser adminUser = permissionHelper.configureCurrentLoggedJiraUser("charlie", PermissionHelper.Permission.ADMIN);

        when(crowdService.addGroup(Matchers.<Group>any())).thenReturn(new MockGroup(GROUP_NAME));

        final Response response = groupResource.createGroup(createAddGroupRequest(GROUP_NAME));
        validationHelper.assertCreated(response);
    }

    @Test
    public void testAddGroupSysAdminHappyPath() throws Exception
    {
        final MockApplicationUser adminUser = permissionHelper.configureCurrentLoggedJiraUser("charlie", PermissionHelper.Permission.SYSADMIN);

        when(crowdService.addGroup(Matchers.<Group>any())).thenReturn(new MockGroup(GROUP_NAME));

        final Response response = groupResource.createGroup(createAddGroupRequest(GROUP_NAME));
        validationHelper.assertCreated(response);
    }

    @Test
    public void testAddGroupAdminEmptyGroupName() throws Exception
    {
        final MockApplicationUser adminUser = permissionHelper.configureCurrentLoggedJiraUser("charlie", PermissionHelper.Permission.ADMIN);

        expectedException.expect(BadRequestWebException.class);

        groupResource.createGroup(createAddGroupRequest(StringUtils.EMPTY));
    }

    @Test
    public void testAddGroupWithUserAdminHappyPath() throws Exception
    {
        final MockApplicationUser adminUser = permissionHelper.configureCurrentLoggedJiraUser("charlie", PermissionHelper.Permission.SYSADMIN);

        when(crowdService.getUser(Matchers.eq(USER_NAME))).thenReturn(new MockUser(USER_NAME));
        when(crowdService.addGroup(Matchers.<Group>any())).thenReturn(new MockGroup(GROUP_NAME));

        final Response response = groupResource.createGroup(createAddGroupRequest(GROUP_NAME));
        validationHelper.assertCreated(response);
    }

    @Test
    public void testAddGroupNotAdmin() throws Exception
    {
        final MockApplicationUser adminUser = permissionHelper.configureCurrentLoggedJiraUser("tod", PermissionHelper.Permission.NOT_ADMIN);

        when(crowdService.addGroup(Matchers.<Group>any())).thenReturn(new MockGroup(GROUP_NAME));
        expectedException.expect(ForbiddenWebException.class);

        groupResource.createGroup(createAddGroupRequest(GROUP_NAME));
    }

    @Test
    public void testAddGroupWithUserAdminGroupExists() throws Exception
    {
        final MockApplicationUser adminUser = permissionHelper.configureCurrentLoggedJiraUser("charlie", PermissionHelper.Permission.SYSADMIN);

        when(crowdService.getGroup(Matchers.eq(GROUP_NAME))).thenReturn(new MockGroup(GROUP_NAME));
        expectedException.expect(BadRequestWebException.class);

        groupResource.createGroup(createAddGroupRequest(GROUP_NAME));
    }

    @Test
    public void testAddGroupAdminThrowsOperationNotPermittedException() throws Exception
    {
        final MockApplicationUser adminUser = permissionHelper.configureCurrentLoggedJiraUser("charlie", PermissionHelper.Permission.ADMIN);

        when(crowdService.addGroup(Matchers.<Group>any())).thenThrow(new OperationNotPermittedException());
        expectedException.expect(ForbiddenWebException.class);

        groupResource.createGroup(createAddGroupRequest(GROUP_NAME));
    }

    @Test
    public void testAddUserToGroupAdminHappyPath() throws Exception
    {
        final MockApplicationUser adminUser = permissionHelper.configureCurrentLoggedJiraUser("charlie", PermissionHelper.Permission.ADMIN);
        final MockUser user = new MockUser(USER_NAME);
        final MockGroup group = new MockGroup(GROUP_NAME);
        when(crowdService.getUser(Matchers.eq(USER_NAME))).thenReturn(user);
        when(crowdService.getGroup(Matchers.eq(GROUP_NAME))).thenReturn(group);
        when(crowdService.addUserToGroup(Matchers.any(User.class), Matchers.any(Group.class))).thenReturn(true);
        when(groupService.validateAddUserToGroup(Matchers.<JiraServiceContext>any(), Matchers.anyList(), Matchers.eq(USER_NAME))).thenReturn(true);

        final Response response = groupResource.addUserToGroup(GROUP_NAME, createAddUserToGroupRequest(USER_NAME));
        validationHelper.assertCreated(response);
    }

    @Test
    public void testAddUserToGroupAdminTriesToBecomeSysadmin() throws Exception
    {
        final MockApplicationUser adminUser = permissionHelper.configureCurrentLoggedJiraUser("charlie", PermissionHelper.Permission.ADMIN);

        when(crowdService.getUser(Matchers.eq(USER_NAME))).thenReturn(new MockUser(USER_NAME));
        when(crowdService.getGroup(Matchers.eq(GROUP_NAME))).thenReturn(new MockGroup(GROUP_NAME));

        when(permissionManager.hasPermission(Matchers.eq(Permissions.SYSTEM_ADMIN), Matchers.<User>any())).thenReturn(false);
        setAddUserValidationError(ErrorCollection.Reason.FORBIDDEN);

        expectedException.expect(ForbiddenWebException.class);
        groupResource.addUserToGroup(GROUP_NAME, createAddUserToGroupRequest(USER_NAME));
    }

    @Test
    public void testAddUserToGroupAdminUserNotExists() throws Exception
    {
        final MockApplicationUser adminUser = permissionHelper.configureCurrentLoggedJiraUser("charlie", PermissionHelper.Permission.ADMIN);

        when(crowdService.getUser(Matchers.eq(USER_NAME))).thenReturn(null);
        when(crowdService.getGroup(Matchers.eq(GROUP_NAME))).thenReturn(new MockGroup(GROUP_NAME));
        setAddUserValidationError(ErrorCollection.Reason.NOT_FOUND);

        expectedException.expect(NotFoundWebException.class);
        groupResource.addUserToGroup(GROUP_NAME, createAddUserToGroupRequest(USER_NAME));
    }

    @Test
    public void testAddUserToGroupAdminGroupNotExists() throws Exception
    {
        final MockApplicationUser adminUser = permissionHelper.configureCurrentLoggedJiraUser("charlie", PermissionHelper.Permission.ADMIN);

        when(crowdService.getUser(Matchers.eq(USER_NAME))).thenReturn(new MockUser(USER_NAME));
        when(crowdService.getGroup(Matchers.eq(GROUP_NAME))).thenReturn(null);

        expectedException.expect(NotFoundWebException.class);
        groupResource.addUserToGroup(GROUP_NAME, createAddUserToGroupRequest(USER_NAME));
    }

    @Test
    public void testAddUserToGroupAdminThrowsOperationFailedEx() throws Exception
    {
        final MockApplicationUser adminUser = permissionHelper.configureCurrentLoggedJiraUser("charlie", PermissionHelper.Permission.ADMIN);

        when(crowdService.getUser(Matchers.eq(USER_NAME))).thenReturn(new MockUser(USER_NAME));
        when(crowdService.getGroup(Matchers.eq(GROUP_NAME))).thenReturn(new MockGroup(GROUP_NAME));
        setAddUserValidationError(ErrorCollection.Reason.SERVER_ERROR);

        doThrow(new OperationFailedException()).when(crowdService).addUserToGroup(Mockito.<User>anyObject(), Mockito.<Group>anyObject());
        expectedException.expect(ServerErrorWebException.class);

        groupResource.addUserToGroup(GROUP_NAME, createAddUserToGroupRequest(USER_NAME));
    }

    @Test
    public void testRemoveGroupWithUserAdminHappyPath() throws Exception
    {
        final MockApplicationUser adminUser = permissionHelper.configureCurrentLoggedJiraUser("charlie", PermissionHelper.Permission.SYSADMIN);

        when(crowdService.getGroup(Matchers.eq(GROUP_NAME))).thenReturn(new MockGroup(GROUP_NAME));
        when(groupService.validateDelete(Matchers.<JiraServiceContext>anyObject(), anyString(), anyString())).thenReturn(true);
        when(groupService.delete(Matchers.<JiraServiceContext>anyObject(), anyString(), anyString())).thenReturn(true);

        final Response response = groupResource.removeGroup(GROUP_NAME, SWAP_GROUP);
        validationHelper.assertOk(response);
    }

    @Test
    public void testRemoveGroupWithUserAdminGroupDoesNotExist() throws Exception
    {
        final MockApplicationUser adminUser = permissionHelper.configureCurrentLoggedJiraUser("charlie", PermissionHelper.Permission.SYSADMIN);

        when(crowdService.getGroup(Matchers.eq(GROUP_NAME))).thenReturn(null);

        expectedException.expect(NotFoundWebException.class);
        groupResource.removeGroup(GROUP_NAME, SWAP_GROUP);
    }

    @Test
    public void testRemoveGroupWithUserAdminValidationError() throws Exception
    {
        final MockApplicationUser adminUser = permissionHelper.configureCurrentLoggedJiraUser("charlie", PermissionHelper.Permission.SYSADMIN);

        when(crowdService.getGroup(Matchers.eq(GROUP_NAME))).thenReturn(new MockGroup(GROUP_NAME));
        setDeleteGroupValidationError(ErrorCollection.Reason.SERVER_ERROR);
        when(groupService.delete(Matchers.<JiraServiceContext>anyObject(), anyString(), anyString())).thenReturn(true);

        expectedException.expect(ServerErrorWebException.class);
        groupResource.removeGroup(GROUP_NAME, SWAP_GROUP);
    }

    @Test
    public void testRemoveGroupWithUserNotAdmin() throws Exception
    {
        final MockApplicationUser adminUser = permissionHelper.configureCurrentLoggedJiraUser("charlie", PermissionHelper.Permission.NOT_ADMIN);

        when(crowdService.getGroup(Matchers.eq(GROUP_NAME))).thenReturn(new MockGroup(GROUP_NAME));
        when(groupService.validateDelete(Matchers.<JiraServiceContext>anyObject(), anyString(), anyString())).thenReturn(true);
        when(groupService.delete(Matchers.<JiraServiceContext>anyObject(), anyString(), anyString())).thenReturn(true);

        expectedException.expect(ForbiddenWebException.class);
        groupResource.removeGroup(GROUP_NAME, SWAP_GROUP);
    }

    @Test
    public void testRemoveUserFromGroupAdminHappyPath() throws Exception
    {
        final MockApplicationUser adminUser = permissionHelper.configureCurrentLoggedJiraUser("charlie", PermissionHelper.Permission.ADMIN);

        when(crowdService.getUser(Matchers.eq(USER_NAME))).thenReturn(new MockUser(USER_NAME));
        when(crowdService.getGroup(Matchers.eq(GROUP_NAME))).thenReturn(new MockGroup(GROUP_NAME));
        when(groupService.validateRemoveUserFromGroups(Matchers.<JiraServiceContext>any(), Mockito.eq(ImmutableList.of(GROUP_NAME)), Mockito.eq(USER_NAME))).thenReturn(true);
        when(groupService.removeUsersFromGroups(Matchers.<JiraServiceContext>any(), Mockito.<GroupRemoveChildMapper>any())).thenReturn(true);

        final Response response = groupResource.removeUserFromGroup(GROUP_NAME, USER_NAME);
        validationHelper.assertOk(response);
    }

    @Test
    public void testRemoveUserFromGroupAdminGroupDoesntExist() throws Exception
    {
        final MockApplicationUser adminUser = permissionHelper.configureCurrentLoggedJiraUser("charlie", PermissionHelper.Permission.ADMIN);

        when(crowdService.getUser(Matchers.eq(USER_NAME))).thenReturn(new MockUser(USER_NAME));
        when(crowdService.getGroup(Matchers.eq(GROUP_NAME))).thenReturn(null);

        expectedException.expect(NotFoundWebException.class);
        groupResource.removeUserFromGroup(GROUP_NAME, USER_NAME);
    }

    @Test
    public void testRemoveUserFromGroupAdminUserDoesntExist() throws Exception
    {
        final MockApplicationUser adminUser = permissionHelper.configureCurrentLoggedJiraUser("charlie", PermissionHelper.Permission.ADMIN);

        when(crowdService.getUser(Matchers.eq(USER_NAME))).thenReturn(null);
        when(crowdService.getGroup(Matchers.eq(GROUP_NAME))).thenReturn(new MockGroup(GROUP_NAME));

        expectedException.expect(NotFoundWebException.class);
        groupResource.removeUserFromGroup(GROUP_NAME, USER_NAME);
    }

    @Test
    public void testRemoveUserFromGroupAdminValidationError() throws Exception
    {
        final MockApplicationUser adminUser = permissionHelper.configureCurrentLoggedJiraUser("charlie", PermissionHelper.Permission.ADMIN);

        when(crowdService.getUser(Matchers.eq(USER_NAME))).thenReturn(new MockUser(USER_NAME));
        when(crowdService.getGroup(Matchers.eq(GROUP_NAME))).thenReturn(new MockGroup(GROUP_NAME));
        setRemoveUserValidationError(ErrorCollection.Reason.SERVER_ERROR);
        when(groupService.removeUsersFromGroups(Matchers.<JiraServiceContext>any(), Mockito.<GroupRemoveChildMapper>any())).thenReturn(true);

        expectedException.expect(ServerErrorWebException.class);
        groupResource.removeUserFromGroup(GROUP_NAME, USER_NAME);
    }

    @Test
    public void testRemoveUserFromGroupNotAdmin() throws Exception
    {
        final MockApplicationUser adminUser = permissionHelper.configureCurrentLoggedJiraUser("tod", PermissionHelper.Permission.NOT_ADMIN);

        when(crowdService.getUser(Matchers.eq(USER_NAME))).thenReturn(new MockUser(USER_NAME));
        when(crowdService.getGroup(Matchers.eq(GROUP_NAME))).thenReturn(new MockGroup(GROUP_NAME));
        when(groupService.validateRemoveUserFromGroups(Matchers.<JiraServiceContext>any(), Mockito.eq(ImmutableList.of(GROUP_NAME)), Mockito.eq(USER_NAME))).thenReturn(true);
        when(groupService.removeUsersFromGroups(Matchers.<JiraServiceContext>any(), Mockito.<GroupRemoveChildMapper>any())).thenReturn(true);

        expectedException.expect(ForbiddenWebException.class);
        groupResource.removeUserFromGroup(GROUP_NAME, USER_NAME);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private AddGroupBean createAddGroupRequest(final String groupName)
    {
        final AddGroupBean addGroupBean = new AddGroupBean();
        addGroupBean.setName(groupName);
        return addGroupBean;
    }

    private UpdateUserToGroupBean createAddUserToGroupRequest(final String userName)
    {
        final UpdateUserToGroupBean updateUserToGroupBean = new UpdateUserToGroupBean();
        updateUserToGroupBean.setName(userName);
        return updateUserToGroupBean;
    }

    private void setAddUserValidationError(final ErrorCollection.Reason reason)
    {
        when(groupService.validateAddUserToGroup(Matchers.<JiraServiceContext>any(), Matchers.anyList(), Matchers.eq(USER_NAME)))
                .thenAnswer(new SingleErrorAnswer(reason));
    }

    private void setRemoveUserValidationError(final ErrorCollection.Reason reason)
    {
        when(groupService.validateRemoveUserFromGroups(Matchers.<JiraServiceContext>any(), Mockito.eq(ImmutableList.of(GROUP_NAME)), Mockito.eq(USER_NAME)))
                .thenAnswer(new SingleErrorAnswer(reason));
    }

    private void setDeleteGroupValidationError(final ErrorCollection.Reason reason)
    {
        when(groupService.validateDelete(Matchers.<JiraServiceContext>anyObject(), anyString(), anyString()))
                .thenAnswer(new SingleErrorAnswer(reason));
    }

    private static class SingleErrorAnswer implements Answer<Object>
    {
        private final ErrorCollection.Reason reason;

        public SingleErrorAnswer(final ErrorCollection.Reason reason) {this.reason = reason;}

        @Override
        public Object answer(final InvocationOnMock invocation) throws Throwable
        {
            ((JiraServiceContext) invocation.getArguments()[0]).getErrorCollection().addError("error", "error", reason);
            return false;
        }
    }
}
