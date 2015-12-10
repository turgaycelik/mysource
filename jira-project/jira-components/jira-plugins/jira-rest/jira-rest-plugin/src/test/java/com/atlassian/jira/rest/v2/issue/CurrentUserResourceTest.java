package com.atlassian.jira.rest.v2.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.bc.user.UserService;
import com.atlassian.jira.bc.user.UserValidationResultBuilder;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.plugin.user.PasswordPolicyManager;
import com.atlassian.jira.plugin.user.WebErrorMessageImpl;
import com.atlassian.jira.rest.exception.BadRequestWebException;
import com.atlassian.jira.rest.exception.ForbiddenWebException;
import com.atlassian.jira.rest.testutils.UserMatchers;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.EmailFormatter;
import com.atlassian.jira.util.I18nHelper;
import com.google.common.collect.ImmutableList;
import junit.framework.Assert;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.mockito.Matchers;
import org.mockito.Mock;

import java.util.Collection;
import java.util.TimeZone;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

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
public class CurrentUserResourceTest
{

    private CurrentUserResource currentUserResource;

    @Mock
    private UserService userServiceMock;

    @Mock
    private JiraAuthenticationContext jiraAuthenticationContextMock;

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
    private UserManager userManager;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private PasswordPolicyManager passwordPolicyManagerMock;

    @Rule
    public RuleChain chain = MockitoMocksInContainer.forTest(this);
    @Mock
    @AvailableInContainer
    private AvatarService avatarService;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();


    @Before
    public void setUp() throws Exception
    {
        when(jiraBaseUrls.restApi2BaseUrl()).thenReturn(UriBuilder.fromUri("http://localhost").toString());


        currentUserResource = new CurrentUserResource(
                userServiceMock,
                userUtilMock,
                userManager,
                passwordPolicyManagerMock,
                eventPublisher,
                mock(I18nHelper.class),
                mock(EmailFormatter.class),
                jiraAuthenticationContextMock,
                timeZoneManager,
                mock(AvatarService.class),
                jiraBaseUrls);

        configureCurrentLoggedJiraUser("charlie");

        validationResultBuilder = new UserValidationResultBuilder();
        userBean = createUserBean();
    }

    @Test
    public void testUpdateUser() throws Exception
    {
        prepareMocksForUserUpdate();

        final Response response = currentUserResource.updateUser(userBean);
        validateUpdateResponse(response);

        verify(userManager, times(1)).updateUser(argThat(new UserMatchers.IsUserWithName(jiraAuthenticationContextMock.getUser().getName())));
    }

    @Test
    public void testUpdateUserWhenNotLoggedIn() throws Exception
    {
        when(jiraAuthenticationContextMock.getUser()).thenReturn(null);
        expectedException.expect(ForbiddenWebException.class);

        prepareMocksForUserUpdate();
        currentUserResource.updateUser(userBean);
    }


    @Test
    public void testUpdateUserWithAllFieldsBlank() throws Exception
    {
        //blank property means this property will not be changed
        final UserWriteBean emptyUser = new UserWriteBean.Builder().toUserBean();
        expectedException.expect(BadRequestWebException.class);

        currentUserResource.updateUser(emptyUser);
    }

    @Test
    public void testUpdateUserWithTooLongEmail() throws Exception
    {
        expectedException.expect(BadRequestWebException.class);

        final UserWriteBean updateUser = new UserWriteBean.Builder()
                .emailAddress(StringUtils.repeat('X', 256) + "@localhost")
                .toUserBean();

        currentUserResource.updateUser(updateUser);
    }

    @Test
    public void testUpdateUserWithTooLongDisplayName() throws Exception
    {
        expectedException.expect(BadRequestWebException.class);

        final UserWriteBean updateUser = new UserWriteBean.Builder()
                .displayName(StringUtils.repeat('X', 256))
                .toUserBean();

        currentUserResource.updateUser(updateUser);
    }

    @Test
    public void testUpdateUserWithInvalidEmail() throws Exception
    {
        expectedException.expect(BadRequestWebException.class);

        final UserWriteBean updateUser = new UserWriteBean.Builder()
                .emailAddress("wrongemailaddress")
                .toUserBean();

        currentUserResource.updateUser(updateUser);
    }

    @Test
    public void testUpdateUserInReadOnlyDirectory() throws Exception
    {
        prepareMocksForUserUpdate();
        when(userManager.canUpdateUser(any(ApplicationUser.class))).thenReturn(false);
        expectedException.expect(BadRequestWebException.class);

        currentUserResource.updateUser(userBean);
    }


    @Test
    public void testChangeMyPasswordWhenNotLoggedIn() throws Exception
    {
        when(jiraAuthenticationContextMock.getUser()).thenReturn(null);
        expectedException.expect(ForbiddenWebException.class);

        final PasswordBean newPass = new PasswordBean("12443");
        currentUserResource.changeMyPassword(newPass);
    }

    @Test
    public void testChangeMyPassword() throws Exception
    {
        final PasswordBean newPass = new PasswordBean("12443");

        final Response response = currentUserResource.changeMyPassword(newPass);
        validateNoContentResponse(response);

        verify(userUtilMock, times(1)).changePassword(Matchers.<User>any(), eq(newPass.getPassword()));
    }

    @Test
    public void testChangeMyPasswordWithBlankPassword() throws Exception
    {
        final PasswordBean newPass = new PasswordBean(" ");

        expectedException.expect(BadRequestWebException.class);

        currentUserResource.changeMyPassword(newPass);
    }

    @Test
    public void testChangeMyPasswordWithInvalidPassword() throws Exception
    {
        final PasswordBean newPass = new PasswordBean("12443");

        final Collection errorMessages = ImmutableList.of(new WebErrorMessageImpl("some error message", null, null));
        when(passwordPolicyManagerMock.checkPolicy(any(ApplicationUser.class), anyString() , anyString())).thenReturn(errorMessages);

        expectedException.expect(BadRequestWebException.class);

        currentUserResource.changeMyPassword(newPass);
    }

    @Test
    public void testChangeMyPasswordThrowsPermissionException() throws Exception
    {
        final PasswordBean newPass = new PasswordBean("12443");

        throwOnChangePassword(newPass);

        expectedException.expect(ForbiddenWebException.class);

        currentUserResource.changeMyPassword(newPass);
    }


    @Test
    public void testGetUser() throws Exception
    {
        final Response response = currentUserResource.getUser();
        validateGetResponse(response);
    }

    @Test
    public void testGetUserWhenNotLoggedIn() throws Exception
    {
        when(jiraAuthenticationContextMock.getUser()).thenReturn(null);
        expectedException.expect(ForbiddenWebException.class);

        currentUserResource.getUser();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private UserService.UpdateUserValidationResult prepareMocksForUserUpdate()
    {
        final UserService.UpdateUserValidationResult validationResult = validationResultBuilder.buildUserUpdate(jiraAuthenticationContextMock.getUser());
        when(userServiceMock.validateUpdateUser(any(ApplicationUser.class))).thenReturn(validationResult);
        return validationResult;
    }

    private MockApplicationUser configureCurrentLoggedJiraUser(final String username)
    {
        final MockApplicationUser loggedUser = configureJiraUser(username);
        when(jiraAuthenticationContextMock.getUser()).thenReturn(loggedUser);

        return loggedUser;
    }

    private MockApplicationUser configureJiraUser(final String username)
    {
        final String key = "key-" + username;
        final MockApplicationUser user = new MockApplicationUser(key, username);

        when(userManager.canUpdateUser(any(ApplicationUser.class))).thenReturn(true);

        when(timeZoneManager.getTimeZoneforUser(user.getDirectoryUser())).thenReturn(TimeZone.getTimeZone("Europe/Warsaw"));

        when(userUtilMock.getUserByKey(key)).thenReturn(user);
        when(userUtilMock.getUserByName(username)).thenReturn(user);

        return user;
    }

    private void validateUpdateResponse(final Response response)
    {
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    private void validateNoContentResponse(final Response response)
    {
        Assert.assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
    }

    private void validateGetResponse(final Response response)
    {
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    private UserWriteBean createUserBean()
    {
        return new UserWriteBean.Builder().displayName("Charlie").emailAddress("charlie@atlassian.com").toUserBean();
    }

    private void throwOnChangePassword(final PasswordBean newPass)
            throws UserNotFoundException, InvalidCredentialException, OperationNotPermittedException, PermissionException
    {
        doThrow(new PermissionException()).when(userUtilMock).changePassword(Matchers.<User>any(), Matchers.eq(newPass.getPassword()));
    }
}
