package com.atlassian.jira.rest.v2.issue;

import java.util.Collection;
import java.util.TimeZone;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.avatar.AvatarPickerHelper;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.fields.ColumnService;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.bc.user.UserService;
import com.atlassian.jira.bc.user.UserValidationResultBuilder;
import com.atlassian.jira.bc.user.search.AssigneeService;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.plugin.user.PasswordPolicyManager;
import com.atlassian.jira.plugin.user.WebErrorMessageImpl;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.rest.exception.BadRequestWebException;
import com.atlassian.jira.rest.exception.ForbiddenWebException;
import com.atlassian.jira.rest.exception.NotFoundWebException;
import com.atlassian.jira.rest.exception.ServerErrorWebException;
import com.atlassian.jira.rest.internal.PermissionHelper;
import com.atlassian.jira.rest.internal.ResponseValidationHelper;
import com.atlassian.jira.rest.testutils.UserMatchers;
import com.atlassian.jira.rest.util.AttachmentHelper;
import com.atlassian.jira.rest.v2.issue.users.UserPickerResourceHelper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.xsrf.XsrfInvocationChecker;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.EmailFormatter;
import com.atlassian.jira.util.I18nHelper;

import com.google.common.collect.ImmutableList;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @since v6.1
 */
public class UserResourceTest
{
    private UserResource usersResource;

    private ResponseValidationHelper validationHelper;

    @Mock
    private UserService userServiceMock;

    @Mock
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

    @Rule
    public RuleChain chain = MockitoMocksInContainer.forTest(this);
    @Mock
    @AvailableInContainer
    private AvatarService avatarService;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private PermissionHelper permissionHelper;

    @Mock
    private UserManager userManager;


    @Before
    public void setUp() throws Exception
    {
        when(jiraBaseUrls.restApi2BaseUrl()).thenReturn(UriBuilder.fromUri("http://localhost").toString());

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
                mock(IssueService.class),
                mock(ProjectManager.class),
                mock(EventPublisher.class),
                mock(AssigneeService.class),
                mock(IssueManager.class),
                mock(UserPickerResourceHelper.class),
                jiraBaseUrls,
                mock(ColumnService.class),
                mock(XsrfInvocationChecker.class),
                userManager);

        validationResultBuilder = new UserValidationResultBuilder();
        userBean = createUserBean(null);

        permissionHelper = new PermissionHelper(permissionManagerMock, userUtilMock, authContext, timeZoneManager);
        permissionHelper.configureJiraUser("jira-user", PermissionHelper.Permission.NOT_ADMIN);

        validationHelper = new ResponseValidationHelper();
    }

    @Test
    public void testCreateUserNoNotificationForAuthorizedAdminUser() throws Exception
    {
        final MockApplicationUser adminUser = permissionHelper.configureCurrentLoggedJiraUser("charlie", PermissionHelper.Permission.ADMIN);

        final UserService.CreateUserValidationResult validationResult = prepareMocksForUserCreate(adminUser, userBean);
        when(timeZoneManager.getTimeZoneforUser(Matchers.<User>any())).thenReturn(TimeZone.getTimeZone("Europe/Warsaw"));

        final Response response = usersResource.createUser(userBean);
        validationHelper.assertCreated(response);

        verify(userServiceMock, times(1)).createUserNoNotification(validationResult);
        verify(userServiceMock, times(0)).createUserWithNotification(validationResult);
    }

    @Test
    public void testCreateUserWithNotificationForAuthorizedAdminUser() throws Exception
    {
        userBean = createUserBean(true);
        final MockApplicationUser adminUser = permissionHelper.configureCurrentLoggedJiraUser("charlie", PermissionHelper.Permission.ADMIN);

        final UserService.CreateUserValidationResult validationResult = prepareMocksForUserCreate(adminUser, userBean);
        when(timeZoneManager.getTimeZoneforUser(Matchers.<User>any())).thenReturn(TimeZone.getTimeZone("Europe/Warsaw"));

        final Response response = usersResource.createUser(userBean);
        validationHelper.assertCreated(response);

        verify(userServiceMock, times(0)).createUserNoNotification(validationResult);
        verify(userServiceMock, times(1)).createUserWithNotification(validationResult);
    }

    @Test
    public void testCreateUserNoNotificationForAuthorizedAdminUserRequestHasValidationErrors() throws Exception
    {
        final MockApplicationUser adminUser = permissionHelper.configureCurrentLoggedJiraUser("charlie", PermissionHelper.Permission.ADMIN);

        prepareMockForUserCreateError(adminUser, userBean);

        expectedException.expect(BadRequestWebException.class);

        usersResource.createUser(userBean);
    }

    @Test
    public void testCreateUserForAuthorizedNotAdminUser() throws Exception
    {
        permissionHelper.configureCurrentLoggedJiraUser("charlie", PermissionHelper.Permission.NOT_ADMIN);

        expectedException.expect(ForbiddenWebException.class);

        usersResource.createUser(userBean);
    }

    @Test
    public void testCreateUserForNullUser() throws Exception
    {
        expectedException.expect(ForbiddenWebException.class);

        usersResource.createUser(userBean);
    }

    @Test
    public void testCreateUserForExceptionRaised() throws Exception
    {
        userBean = createUserBean(true);
        final MockApplicationUser adminUser = permissionHelper.configureCurrentLoggedJiraUser("charlie", PermissionHelper.Permission.ADMIN);

        throwOnUserCreate(adminUser, userBean);

        expectedException.expect(ServerErrorWebException.class);

        usersResource.createUser(userBean);
    }

    @Test
    public void testUpdateUser() throws Exception
    {
        permissionHelper.configureCurrentLoggedJiraUser("admin-user", PermissionHelper.Permission.ADMIN);
        final ApplicationUser updateUser = configureJiraUser("charlie", PermissionHelper.Permission.NOT_ADMIN);
        final UserService.UpdateUserValidationResult validationResult = prepareMocksForUserUpdate(updateUser);

        final Response response = usersResource.updateUser(null, updateUser.getKey(), userBean);
        validationHelper.assertUpdated(response);

        verify(userServiceMock, times(1)).updateUser(validationResult);
    }

    @Test
    public void testUpdateUserWithAllFieldsBlank() throws Exception
    {
        //blank property means this property will not be changed
        permissionHelper.configureCurrentLoggedJiraUser("charlie", PermissionHelper.Permission.ADMIN);

        final UserWriteBean emptyUser = new UserWriteBean.Builder().toUserBean();

        expectedException.expect(BadRequestWebException.class);

        usersResource.updateUser(null, "key-charlie", emptyUser);
    }

    @Test
    public void testUpdateUserWithTooLongEmail() throws Exception
    {
        permissionHelper.configureCurrentLoggedJiraUser("admin-user", PermissionHelper.Permission.ADMIN);
        prepareMocksForUserUpdate(configureJiraUser("charlie", PermissionHelper.Permission.NOT_ADMIN));

        final UserWriteBean updateUser = new UserWriteBean.Builder()
                .emailAddress(StringUtils.repeat('X', 256) + "@localhost")
                .toUserBean();

        expectedException.expect(BadRequestWebException.class);

        usersResource.updateUser("charlie", null, updateUser);
    }

    @Test
    public void testUpdateUserWithTooLongDisplayName() throws Exception
    {
        permissionHelper.configureCurrentLoggedJiraUser("admin-user", PermissionHelper.Permission.ADMIN);
        prepareMocksForUserUpdate(configureJiraUser("charlie", PermissionHelper.Permission.NOT_ADMIN));

        final UserWriteBean updateUser = new UserWriteBean.Builder()
                .displayName(StringUtils.repeat('X', 256))
                .toUserBean();

        expectedException.expect(BadRequestWebException.class);

        usersResource.updateUser("charlie", null, updateUser);
    }

    @Test
    public void testUpdateUserWithInvalidEmail() throws Exception
    {
        permissionHelper.configureCurrentLoggedJiraUser("charlie", PermissionHelper.Permission.ADMIN);

        final UserWriteBean updateUser = new UserWriteBean.Builder()
                .emailAddress("wrongemailaddress")
                .toUserBean();

        expectedException.expect(BadRequestWebException.class);

        usersResource.updateUser(null, "key-charlie", updateUser);
    }


    @Test
    public void testUpdateUserAuthorizedNotAdminUser() throws Exception
    {
        permissionHelper.configureCurrentLoggedJiraUser("charlie", PermissionHelper.Permission.NOT_ADMIN);

        expectedException.expect(ForbiddenWebException.class);

        usersResource.updateUser(null, "key-charlie", userBean);
    }

    @Test
    public void testUpdateUserRequestHasValidationErrors() throws Exception
    {
        permissionHelper.configureCurrentLoggedJiraUser("charlie", PermissionHelper.Permission.ADMIN);
        prepareMockForUserUpdateError();

        expectedException.expect(BadRequestWebException.class);

        usersResource.updateUser(null, "key-charlie", userBean);
    }

    @Test
    public void testChangeUserPassword() throws Exception
    {
        final PasswordBean newPass = new PasswordBean("12443");

        permissionHelper.configureCurrentLoggedJiraUser("charlie", PermissionHelper.Permission.ADMIN);
        final Response response = usersResource.changeUserPassword(null, "key-charlie", newPass);
        validationHelper.assertNoContent(response);

        verify(userUtilMock, times(1)).changePassword(Matchers.<User>any(), eq(newPass.getPassword()));
    }

    @Test
    public void testChangeUserPasswordWithBlankPassword() throws Exception
    {
        final PasswordBean newPass = new PasswordBean(" ");

        permissionHelper.configureCurrentLoggedJiraUser("charlie", PermissionHelper.Permission.ADMIN);

        expectedException.expect(BadRequestWebException.class);

        usersResource.changeUserPassword(null, "key-charlie", newPass);
    }

    @Test
    public void testChangeUserPasswordWithInvalidPassword() throws Exception
    {
        final PasswordBean newPass = new PasswordBean("12443");

        final Collection errorMessages = ImmutableList.of(new WebErrorMessageImpl("some error message", null, null));
        when(passwordPolicyManagerMock.checkPolicy(any(ApplicationUser.class), anyString() , anyString())).thenReturn(errorMessages);

        permissionHelper.configureCurrentLoggedJiraUser("charlie", PermissionHelper.Permission.ADMIN);

        expectedException.expect(BadRequestWebException.class);

        usersResource.changeUserPassword(null, "key-charlie", newPass);
    }

    @Test
    public void testChangeUserPasswordAuthorizedNotAdminUser() throws Exception
    {
        final PasswordBean newPass = new PasswordBean("12443");

        permissionHelper.configureCurrentLoggedJiraUser("charlie", PermissionHelper.Permission.NOT_ADMIN);

        expectedException.expect(ForbiddenWebException.class);

        usersResource.changeUserPassword(null, "key-charlie", newPass);
    }

    @Test
    public void testChangeSysadminPasswordByAdmin() throws Exception
    {
        final PasswordBean newPass = new PasswordBean("12443");

        permissionHelper.configureCurrentLoggedJiraUser("charlie", PermissionHelper.Permission.ADMIN);

        final String sysadminName = "sysadmin";
        configureJiraUser(sysadminName, PermissionHelper.Permission.SYSADMIN);

        expectedException.expect(ForbiddenWebException.class);

        usersResource.changeUserPassword(sysadminName, null, newPass);
    }

    @Test
    public void testChangeUserPasswordThrowsPermissionException() throws Exception
    {
        final PasswordBean newPass = new PasswordBean("12443");

        throwOnChangePassword(newPass);

        permissionHelper.configureCurrentLoggedJiraUser("charlie", PermissionHelper.Permission.ADMIN);

        expectedException.expect(ForbiddenWebException.class);

        usersResource.changeUserPassword(null, "key-charlie", newPass);
    }

    @Test
    public void testRemoveUser() throws Exception
    {
        final String userKey = "key-charlie";

        MockApplicationUser adminUser = permissionHelper.configureCurrentLoggedJiraUser("charlie", PermissionHelper.Permission.ADMIN);


        final UserService.DeleteUserValidationResult validationResult = validationResultBuilder.buildUserDelete();
        when(userServiceMock.validateDeleteUser(Matchers.eq(adminUser), Matchers.<ApplicationUser>anyObject())).thenReturn(validationResult);

        Response response = usersResource.removeUser(null, userKey);
        validationHelper.assertNoContent(response);
    }

    @Test
    public void testRemoveUserAuthorizedNotAdminUser() throws Exception
    {
        final String userKey = "key-charlie";

        permissionHelper.configureCurrentLoggedJiraUser("charlie", PermissionHelper.Permission.NOT_ADMIN);

        expectedException.expect(ForbiddenWebException.class);

        usersResource.removeUser(null, userKey);
    }

    @Test
    public void testRemoveUserNoSuchUser() throws Exception
    {
        final String userKey = "key-charlie";

        MockApplicationUser adminUser = permissionHelper.configureCurrentLoggedJiraUser("charlie", PermissionHelper.Permission.ADMIN);

        validationResultBuilder.addError("key", "User does not exist");
        final UserService.DeleteUserValidationResult validationResult = validationResultBuilder.buildUserDeleteErr();
        when(userServiceMock.validateDeleteUser(Matchers.eq(adminUser), Matchers.<ApplicationUser>anyObject())).thenReturn(validationResult);

        expectedException.expect(BadRequestWebException.class);

        usersResource.removeUser(null, userKey);
    }

    @Test
    public void testRemoveUserThrowsException() throws Exception
    {
        final String userKey = "key-charlie";

        MockApplicationUser adminUser = permissionHelper.configureCurrentLoggedJiraUser("charlie", PermissionHelper.Permission.ADMIN);


        final UserService.DeleteUserValidationResult validationResult = validationResultBuilder.buildUserDelete();
        when(userServiceMock.validateDeleteUser(Matchers.eq(adminUser), Matchers.<ApplicationUser>anyObject())).thenReturn(validationResult);

        usersResource.removeUser(null, userKey);

        throwOnRemoveUser(adminUser);

        expectedException.expect(BadRequestWebException.class);

        usersResource.removeUser(null, userKey);
    }

    @Test
    public void testGetUser() throws Exception
    {
        final String userKey = "key-charlie";

        permissionHelper.configureCurrentLoggedJiraUser("charlie", PermissionHelper.Permission.ADMIN);
        final Response response = usersResource.getUser(null, userKey);
        validationHelper.assertOk(response);

        verify(userUtilMock, times(1)).getUserByKey(eq(userKey));
    }

    @Test
    public void testGetUserWhenNotFound() throws Exception
    {
        final String userKey = "key-user-that-dont-exists";

        permissionHelper.configureCurrentLoggedJiraUser("charlie", PermissionHelper.Permission.ADMIN);

        expectedException.expect(NotFoundWebException.class);

        usersResource.getUser(null, userKey);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private UserService.UpdateUserValidationResult prepareMocksForUserUpdate(final ApplicationUser user)
    {
        final UserService.UpdateUserValidationResult validationResult = validationResultBuilder.buildUserUpdate(user);
        when(userServiceMock.validateUpdateUser(argThat(new UserMatchers.IsApplicationUserWithKey(user.getKey())))).thenReturn(validationResult);
        return validationResult;
    }

    private UserService.UpdateUserValidationResult prepareMockForUserUpdateError()
    {
        validationResultBuilder.addError("email", "Email is not valid");
        final UserService.UpdateUserValidationResult validationResult = validationResultBuilder.buildUserUpdateErr();
        when(userServiceMock.validateUpdateUser(Matchers.<ApplicationUser>anyObject())).thenReturn(validationResult);
        return validationResult;
    }

    private UserService.CreateUserValidationResult prepareMocksForUserCreate(final MockApplicationUser adminUser, final UserWriteBean userBean)
            throws PermissionException, CreateException
    {
        final UserService.CreateUserValidationResult validationResult = prepareMocksForUserCreateValidation(adminUser, userBean);
        Mockito.when(userServiceMock.createUserNoNotification(validationResult)).thenReturn(Mockito.mock(User.class));
        Mockito.when(userServiceMock.createUserWithNotification(validationResult)).thenReturn(Mockito.mock(User.class));


        final MockApplicationUser createdUser = new MockApplicationUser("charlie");
        when(userUtilMock.getUserByName(userBean.getName())).thenReturn(createdUser);
        return validationResult;
    }

    private void prepareMockForUserCreateError(final MockApplicationUser adminUser, final UserWriteBean userBean)
    {
        validationResultBuilder.addError("name", "Username is already taken");
        final UserService.CreateUserValidationResult validationResult = validationResultBuilder.buildUserCreateErr();
        when(userServiceMock.validateCreateUserForAdmin(adminUser.getDirectoryUser(),
                userBean.getName(),
                userBean.getPassword(),
                userBean.getPassword(),
                userBean.getEmailAddress(),
                userBean.getDisplayName()
        )).thenReturn(validationResult);
    }

    private void throwOnUserCreate(final MockApplicationUser adminUser, final UserWriteBean userBean) throws CreateException, PermissionException
    {
        final UserService.CreateUserValidationResult validationResult = prepareMocksForUserCreateValidation(adminUser, userBean);
        Mockito.when(userServiceMock.createUserNoNotification(validationResult)).thenThrow(new CreateException());
        Mockito.when(userServiceMock.createUserWithNotification(validationResult)).thenThrow(new CreateException());
    }

    private UserService.CreateUserValidationResult prepareMocksForUserCreateValidation(final MockApplicationUser adminUser, final UserWriteBean userBean)
    {
        final UserService.CreateUserValidationResult validationResult = validationResultBuilder.buildUserCreate();
        when(userServiceMock.validateCreateUserForAdmin(adminUser.getDirectoryUser(),
                userBean.getName(),
                userBean.getPassword(),
                userBean.getPassword(),
                userBean.getEmailAddress(),
                userBean.getDisplayName()
        )).thenReturn(validationResult);
        return validationResult;
    }

    private MockApplicationUser configureJiraUser(final String username, final PermissionHelper.Permission permission)
    {
        final String key = "key-" + username;
        final MockApplicationUser user = new MockApplicationUser(key, username);

        final boolean isAdmin = PermissionHelper.Permission.ADMIN.equals(permission) || PermissionHelper.Permission.SYSADMIN.equals(permission);
        when(permissionManagerMock.hasPermission(Permissions.ADMINISTER, user)).thenReturn(isAdmin);

        final boolean isSysadmin = PermissionHelper.Permission.SYSADMIN.equals(permission);
        when(permissionManagerMock.hasPermission(Permissions.SYSTEM_ADMIN, user)).thenReturn(isSysadmin);

        when(timeZoneManager.getTimeZoneforUser(user.getDirectoryUser())).thenReturn(TimeZone.getTimeZone("Europe/Warsaw"));

        when(userUtilMock.getUserByKey(key)).thenReturn(user);
        when(userUtilMock.getUserByName(username)).thenReturn(user);

        return user;
    }

    private UserWriteBean createUserBean(final Boolean notification)
    {
        return new UserWriteBean("http://aaa", "key-charlie", "charlie", "pass", "charlie@atlassian.com", "Charlie", notification == null ? null : notification.toString());
    }

    private void throwOnChangePassword(final PasswordBean newPass)
            throws UserNotFoundException, InvalidCredentialException, OperationNotPermittedException, PermissionException
    {
        doThrow(new PermissionException()).when(userUtilMock).changePassword(Matchers.<User>any(), Matchers.eq(newPass.getPassword()));
    }

    private void throwOnRemoveUser(final MockApplicationUser adminUser)
    {
        doThrow(new RuntimeException("User does not exist")).when(userServiceMock).removeUser(Matchers.<ApplicationUser>eq(adminUser), Matchers.<UserService.DeleteUserValidationResult>anyObject());
    }
}
