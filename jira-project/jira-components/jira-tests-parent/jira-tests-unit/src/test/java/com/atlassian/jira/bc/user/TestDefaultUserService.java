package com.atlassian.jira.bc.user;

import java.util.Collections;
import java.util.List;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.ImmutableUser;
import com.atlassian.jira.bc.ServiceResultImpl;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.event.user.UserEventType;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.MockPermissionManager;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.plugin.user.PasswordPolicyManager;
import com.atlassian.jira.plugin.user.PreDeleteUserErrorsManager;
import com.atlassian.jira.plugin.user.WebErrorMessage;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.MockUserDeleteVeto;
import com.atlassian.jira.user.util.MockUserManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.ComponentLocator;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraContactHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventManager;

import com.google.common.collect.ImmutableList;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Clean tests for DefaultUserService.
 *
 * @since v5.0
 */
public class TestDefaultUserService
{
    @Rule
    public final RuleChain mockContainer = MockitoMocksInContainer.forTest(this);

    @Mock
    private GlobalPermissionManager globalPermissionManager;
    @Mock
    private PermissionManager permissionManager;
    @Mock
    private PreDeleteUserErrorsManager preDeleteUserErrorsManager;
    @Mock
    private PasswordPolicyManager passwordPolicyManager;
    @Mock
    private UserUtil userUtil;
    @Mock @AvailableInContainer
    private PluginAccessor pluginAccessor;
    @Mock @AvailableInContainer
    private PluginEventManager pluginEventManager;

    private I18nHelper.BeanFactory i18nFactory = new MockI18nBean.MockI18nBeanFactory();
    private MockUserManager userManager = new MockUserManager();
    private ApplicationUser admin = new MockApplicationUser("Admin");
    private JiraAuthenticationContext jiraAuthenticationContext = new MockSimpleAuthenticationContext(null);

    @Before
    public void setUp() throws Exception
    {
        userManager.addUser(admin);

        when(preDeleteUserErrorsManager.getWarnings(any(User.class))).thenReturn(ImmutableList.<WebErrorMessage>of());
        when(passwordPolicyManager.checkPolicy(any(ApplicationUser.class), anyString(), anyString())).thenReturn(ImmutableList.<WebErrorMessage>of());
        when(passwordPolicyManager.checkPolicy(anyString(), anyString(), anyString(), anyString())).thenReturn(ImmutableList.<WebErrorMessage>of());
        mockGlobalPermission(Permissions.ADMINISTER, admin);
    }

    @After
    public void tearDown() throws Exception
    {
        globalPermissionManager = null;
        permissionManager = null;
        preDeleteUserErrorsManager = null;
        userUtil = null;
        i18nFactory = null;
        userManager = null;
        admin = null;
        jiraAuthenticationContext = null;
    }



    private User createUser(final String username, final String password, final String emailAddress, final String displayName)
            throws CreateException, PermissionException
    {
        final long directoryId = 1L;
        final User expectedUser = new ImmutableUser(directoryId, username, displayName, emailAddress, true);

        final GlobalPermissionManager globalPermissionManager = mock(GlobalPermissionManager.class);
        final ComponentLocator componentLocator = mock(ComponentLocator.class);
        final JiraLicenseService jiraLicenseService = mock(JiraLicenseService.class);

        when(globalPermissionManager.getGroupsWithPermission(anyInt())).thenReturn(ImmutableList.<Group>of());
        when(componentLocator.getComponentInstanceOfType(JiraLicenseService.class)).thenReturn(jiraLicenseService);

        final UserService userService = new DefaultUserService(userUtil, null, permissionManager, null, null, null, null, null, null, passwordPolicyManager);
        UserService.CreateUserValidationResult validationResult = new UserService.CreateUserValidationResult(username, password, emailAddress, displayName);
        assertNoErrors(validationResult);

        when(userUtil.createUserWithNotification(username, password, emailAddress, displayName, null, UserEventType.USER_CREATED)).thenReturn(expectedUser);
        final User user = userService.createUserWithNotification(validationResult);

        verify(userUtil).createUserWithNotification(username, password, emailAddress, displayName, null, UserEventType.USER_CREATED);
        assertSame(expectedUser, user);

        userManager.addUser(expectedUser);
        return userManager.getUser(username);
    }

    @Test
    public void testValidateCreateJiraUserForAdminPasswordRequired()
    {
        final UserService userService = new DefaultUserService(userUtil, null, permissionManager, userManager, null, new MockI18nBean.MockI18nBeanFactory(), jiraAuthenticationContext, null, null, passwordPolicyManager);
        final UserService.CreateUserValidationResult result = userService.validateCreateUserForAdminPasswordRequired(
                admin.getDirectoryUser(), "fflintstone", "mypassword", "mypassword", "fred@flintstone.com", "Fred Flintstone");

        assertNoErrors(result);
        assertEquals("fflintstone", result.getUsername());
        assertEquals("mypassword", result.getPassword());
        assertEquals("Fred Flintstone", result.getFullname());
        assertEquals("fred@flintstone.com", result.getEmail());
    }

    @Test
    public void testValidateCreateJiraUserForAdminNoPermission()
    {
        final UserService userService = new DefaultUserService(userUtil, null, permissionManager, userManager, null, new MockI18nBean.MockI18nBeanFactory(), jiraAuthenticationContext, null, null, passwordPolicyManager);
        final ApplicationUser fred = new MockApplicationUser("Fred");
        userManager.addUser(fred);

        final UserService.CreateUserValidationResult result = userService.validateCreateUserForAdminPasswordRequired(
                fred.getDirectoryUser(), "fflintstone", "mypassword", "mypassword", "fred@flintstone.com", "Fred Flintstone");
        assertEquals(asList("You do not have permission to create a user."), result.getErrorCollection().getErrorMessages());
        assertFalse("Should not be a valid result", result.isValid());
    }


    @Test
    public void testValidateCreateJiraUserForSetupOk()
    {
        final UserService userService = new DefaultUserService(userUtil, null, permissionManager, userManager, null, new MockI18nBean.MockI18nBeanFactory(), jiraAuthenticationContext, null, null, passwordPolicyManager);
        final ApplicationUser fred = new MockApplicationUser("Fred");
        userManager.addUser(fred);

        final UserService.CreateUserValidationResult result = userService.validateCreateUserForSignupOrSetup(fred.getDirectoryUser(),
                "fflintstone", "mypassword", "mypassword", "fred@flintstone.com", "Fred Flintstone");
        assertNoErrors(result);
        assertEquals("fflintstone", result.getUsername());
        assertEquals("mypassword", result.getPassword());
        assertEquals("Fred Flintstone", result.getFullname());
        assertEquals("fred@flintstone.com", result.getEmail());
    }


    @Test
    public void testValidateCreateJiraUserForAdminExternalAdmin()
    {
        final UserService userService = new DefaultUserService(userUtil, null, permissionManager, userManager, null, new MockI18nBean.MockI18nBeanFactory(), jiraAuthenticationContext, null, null, passwordPolicyManager);
        userManager.setWritableDirectory(false);

        final UserService.CreateUserValidationResult result = userService.validateCreateUserForAdminPasswordRequired(
                admin.getDirectoryUser(), "fflintstone", "mypassword", "mypassword", "fred@flintstone.com", "Fred Flintstone");

        assertEquals(asList("Cannot add user, all the user directories are read-only."), result.getErrorCollection().getErrorMessages());
        assertTrue("Should have errors", result.getErrorCollection().hasAnyErrors());
        assertFalse("Should not be valid", result.isValid());
    }

    @Test
    public void testValidateCreateJiraUserForSignUpNoExtMgmnt()
    {
        final UserService userService = new DefaultUserService(userUtil, null, permissionManager, userManager, getJiraContactHelper(), new MockI18nBean.MockI18nBeanFactory(), jiraAuthenticationContext, null, null, passwordPolicyManager);
        userManager.setWritableDirectory(false);

        final UserService.CreateUserValidationResult result = userService.validateCreateUserForSignup(
                admin.getDirectoryUser(), "fflintstone", "mypassword", "mypassword", "fred@flintstone.com", "Fred Flintstone");

        assertEquals(asList("Cannot add user, all the user directories are read-only, please contact your JIRA administrators."), result.getErrorCollection().getErrorMessages());
        assertTrue("Should have errors", result.getErrorCollection().hasAnyErrors());
        assertFalse("Should not be valid", result.isValid());
    }

    @Test
    public void testValidateCreateJiraUserForSetupNoPassword()
    {
        final UserService userService = new DefaultUserService(userUtil, null, permissionManager, userManager, null, new MockI18nBean.MockI18nBeanFactory(), jiraAuthenticationContext, null, null, passwordPolicyManager);
        final UserService.CreateUserValidationResult result = userService.validateCreateUserForSignupOrSetup(
                admin.getDirectoryUser(), "fflintstone", "", "", "fred@flintstone.com", "Fred Flintstone");
        assertEquals("You must specify a password and a confirmation password.", result.getErrorCollection().getErrors().get("password"));
        assertTrue("Should have errors", result.getErrorCollection().hasAnyErrors());
        assertFalse("Should not be valid", result.isValid());
    }

    @Test
    public void testValidateCreateJiraUserForAdmin()
    {
        final UserService userService = new DefaultUserService(userUtil, null, permissionManager, userManager, null, new MockI18nBean.MockI18nBeanFactory(), jiraAuthenticationContext, null, null, passwordPolicyManager);
        final UserService.CreateUserValidationResult result = userService.validateCreateUserForAdmin(
                admin.getDirectoryUser(), "fflintstone", null, null, "fred@flintstone.com", "Fred Flintstone");
        assertTrue(result.isValid());
        assertFalse(result.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void testValidateCreateJiraUserForAdmin_nameDirectory()
    {
        final UserService userService = new DefaultUserService(userUtil, null, permissionManager, userManager, null, new MockI18nBean.MockI18nBeanFactory(), jiraAuthenticationContext, null, null, passwordPolicyManager);
        final UserService.CreateUserValidationResult result = userService.validateCreateUserForAdmin(
                admin.getDirectoryUser(), "fflintstone", null, null, "fred@flintstone.com", "Fred Flintstone", 1L);
        assertNoErrors(result);
    }

    @Test
    public void testValidateCreateJiraUserForAdminNoPermissionNoPassword()
    {
        final UserService userService = new DefaultUserService(userUtil, null, permissionManager, userManager, null, new MockI18nBean.MockI18nBeanFactory(), jiraAuthenticationContext, null, null, passwordPolicyManager);
        final ApplicationUser fred = new MockApplicationUser("Fred");
        userManager.addUser(fred);
        final UserService.CreateUserValidationResult result = userService.validateCreateUserForAdmin(
                fred.getDirectoryUser(), "fflintstone", null, null, "fred@flintstone.com", "Fred Flintstone");

        assertEquals(asList("You do not have permission to create a user."), result.getErrorCollection().getErrorMessages());
        assertTrue("Should have errors", result.getErrorCollection().hasAnyErrors());
        assertFalse("Should not be valid", result.isValid());
    }

    @Test
    public void testValidateCreateJiraUserForAdminNoPermissionNoExtMgmnt()
    {
        final UserService userService = new DefaultUserService(userUtil, null, permissionManager, userManager, null, new MockI18nBean.MockI18nBeanFactory(), jiraAuthenticationContext, null, null, passwordPolicyManager);
        userManager.setWritableDirectory(false);
        final UserService.CreateUserValidationResult result = userService.validateCreateUserForAdmin(
                admin.getDirectoryUser(), "fflintstone", null, null, "fred@flintstone.com", "Fred Flintstone");

        assertEquals(asList("Cannot add user, all the user directories are read-only."), result.getErrorCollection().getErrorMessages());
        assertFalse("Should not be valid", result.isValid());
        assertTrue("Should have errors", result.getErrorCollection().hasAnyErrors());
    }

    private void checkCreateUserValues(final String username, final String password, final String confirm, final String email, final String fullname, final String errorI18n, final String errorField)
    {
        final I18nHelper i18n = new MockI18nBean();
        final UserService userService = new DefaultUserService(userUtil, null, permissionManager, userManager, null, new MockI18nBean.MockI18nBeanFactory(), jiraAuthenticationContext, null, null, passwordPolicyManager);
        final String expectedMessage = i18n.getText(errorI18n);
        final UserService.CreateUserValidationResult result = userService.validateCreateUserForAdminPasswordRequired(
                admin.getDirectoryUser(), username, password, confirm, email, fullname);
        assertEquals(expectedMessage, result.getErrorCollection().getErrors().get(errorField));
        assertFalse(result.isValid());
        assertTrue(result.getErrorCollection().hasAnyErrors());
    }



    @Test
    public void testValidateCreateJiraUserPasswordNotMatch()
    {
        checkCreateUserValues("fflintstone", "mypassword", "mypassword2", "fred@flintstone.com", "Fred Flintstone",
            "signup.error.password.mustmatch", "confirm");
    }

    @Test
    public void testValidateCreateJiraUserNoEmail()
    {
        checkCreateUserValues("fflintstone", "mypassword", "mypassword", null, "Fred Flintstone", "signup.error.email.required", "email");
    }

    @Test
    public void testValidateCreateJiraUserEmailExceeds255()
    {
        checkCreateUserValues("testuser543", "mypassword", "mypassword", StringUtils.repeat("a", 256), "Fred Flintstone", "signup.error.email.greater.than.max.chars", "email");
    }

    @Test
    public void testValidateCreateJiraUserWrongEmail()
    {
        checkCreateUserValues("fflintstone", "mypassword", "mypassword", "fred", "Fred Flintstone", "signup.error.email.valid", "email");
    }

    @Test
    public void testValidateCreateJiraUserNoUsername()
    {
        checkCreateUserValues("", "mypassword", "mypassword", "fred@flintstone.com", "Fred Flintstone", "signup.error.username.required", "username");
    }

    @Test
    public void testValidateCreateJiraUserInvalidUsername1()
    {
        checkCreateUserValues("fred<bad", "mypassword", "mypassword", "fred@flintstone.com", "Fred Flintstone", "signup.error.username.invalid.chars", "username");
    }

    @Test
    public void testValidateCreateJiraUserInvalidUsername2()
    {
        checkCreateUserValues("fred>bad", "mypassword", "mypassword", "fred@flintstone.com", "Fred Flintstone", "signup.error.username.invalid.chars", "username");
    }

    @Test
    public void testValidateCreateJiraUserInvalidUsername3()
    {
        checkCreateUserValues("fred&bad", "mypassword", "mypassword", "fred@flintstone.com", "Fred Flintstone", "signup.error.username.invalid.chars", "username");
    }

    @Test
    public void testValidateCreateJiraUserUsernameExceeds255()
    {
        checkCreateUserValues(StringUtils.repeat("a", 256), "mypassword", "mypassword", "fred@flintstone.com", "Fred Flintstone", "signup.error.username.greater.than.max.chars", "username");
    }

    @Test
    public void testValidateCreateJiraUserExists()
    {
        when(userUtil.userExists("DupeMe")).thenReturn(true);
        checkCreateUserValues("DupeMe", "mypassword", "mypassword", "fred@flintstone.com", "Fred Flintstone", "signup.error.username.exists",
            "username");
    }

    @Test
    public void testValidateCreateJiraUserNoFullname()
    {
        checkCreateUserValues("fflintstone", "mypassword", "mypassword", "fred@flintstone.com", null, "signup.error.fullname.required", "fullname");
    }

    @Test
    public void testValidateCreateJiraUserFullnameExceeds255()
    {
        checkCreateUserValues("testuser654", "mypassword", "mypassword", "fred@flintstone.com", StringUtils.repeat("a", 256), "signup.error.full.name.greater.than.max.chars", "fullname");
    }

    @Test
    public void testValidateCreateJiraUserNullPassword()
    {
        checkCreateUserValues("fflintstone", null, null, "fred@flintstone.com", "Fred Flintstone", "signup.error.password.required", "password");
    }

    @Test
    public void testValidateDeleteUserNoPermission() throws Exception
    {
        final MockUserManager userManager = new MockUserManager();
        final ApplicationUser dude = new MockApplicationUser("DudeUser");
        final ApplicationUser fred = new MockApplicationUser("Fred");
        userManager.addUser(dude);
        userManager.addUser(fred);

        DefaultUserService userService = new DefaultUserService(null, null, new MockPermissionManager(), userManager, null, i18nFactory, null, null, preDeleteUserErrorsManager, passwordPolicyManager);
        UserService.DeleteUserValidationResult validationResult = userService.validateDeleteUser((ApplicationUser)null, "fred");

        final List<String> expectedErrors = asList("You do not have the permission to remove users.");
        assertEquals(expectedErrors, validationResult.getErrorCollection().getErrorMessages());
        assertTrue("Should have errors", validationResult.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void testValidateDeleteUserNull() throws Exception
    {
        DefaultUserService userService = new DefaultUserService(null, null, new MockPermissionManager(true), null, null, i18nFactory, null, null, preDeleteUserErrorsManager, passwordPolicyManager);
        UserService.DeleteUserValidationResult validationResult = userService.validateDeleteUser((ApplicationUser)null, (String)null);

        // This error gets returned on the username field, not the general error messages
        assertEquals("Username for delete can not be null or empty.", validationResult.getErrorCollection().getErrors().get("username"));
        assertEquals(Collections.<String>emptyList(), validationResult.getErrorCollection().getErrorMessages());
        assertTrue("Should have errors", validationResult.getErrorCollection().hasAnyErrors());
        assertFalse("Should not be valid", validationResult.isValid());
    }

    @Test
    public void testValidateDeleteUserEmpty() throws Exception
    {
        DefaultUserService userService = new DefaultUserService(null, null, new MockPermissionManager(true), null, null, i18nFactory, null, null, preDeleteUserErrorsManager, passwordPolicyManager);
        UserService.DeleteUserValidationResult validationResult = userService.validateDeleteUser((ApplicationUser)null, "");

        // This error gets returned on the username field, not the general error messages
        assertEquals("Username for delete can not be null or empty.", validationResult.getErrorCollection().getErrors().get("username"));
        assertEquals(Collections.<String>emptyList(), validationResult.getErrorCollection().getErrorMessages());
        assertTrue("Should have errors", validationResult.getErrorCollection().hasAnyErrors());
        assertFalse("Should not be valid", validationResult.isValid());
    }

    @Test
    public void testValidateDeleteUserDeleteSelf() throws Exception
    {
        final MockUserManager userManager = new MockUserManager();
        final MockApplicationUser fred1 = new MockApplicationUser("Fred");
        final MockApplicationUser fred2 = new MockApplicationUser("fReD");
        userManager.addUser(fred1);
        userManager.addUser(fred2);

        DefaultUserService userService = new DefaultUserService(null, null, new MockPermissionManager(true), userManager, null, i18nFactory, null, null, preDeleteUserErrorsManager, passwordPolicyManager);
        UserService.DeleteUserValidationResult validationResult = userService.validateDeleteUser(fred1, fred2);

        final List<String> expectedErrors = asList("You cannot delete the currently logged in user.");
        assertEquals(expectedErrors, validationResult.getErrorCollection().getErrorMessages());
        assertTrue("Should have errors", validationResult.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void testValidateDeleteUserNotExist() throws Exception
    {
        DefaultUserService userService = new DefaultUserService(null, null, new MockPermissionManager(true), new MockUserManager(), null, i18nFactory, null, null, preDeleteUserErrorsManager, passwordPolicyManager);
        UserService.DeleteUserValidationResult validationResult = userService.validateDeleteUser(new MockApplicationUser("dude"), "fred");

        final List<String> expectedErrors = asList("This user does not exist please select a user from the user browser.");
        assertEquals(expectedErrors, validationResult.getErrorCollection().getErrorMessages());
        assertTrue("Should have errors", validationResult.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void testValidateDeleteUserReadOnly() throws Exception
    {
        final MockUserManager userManager = new MockUserManager();
        userManager.addUser(new MockApplicationUser("fred"));
        userManager.setWritableDirectory(false);

        DefaultUserService userService = new DefaultUserService(null, null, new MockPermissionManager(true), userManager, null, i18nFactory, null, null, preDeleteUserErrorsManager, passwordPolicyManager);
        UserService.DeleteUserValidationResult validationResult = userService.validateDeleteUser(new MockApplicationUser("dude"), "fred");

        final List<String> expectedErrors = asList("Cannot delete user, the user directory is read-only.");
        assertEquals(expectedErrors, validationResult.getErrorCollection().getErrorMessages());
        assertTrue("Should have errors", validationResult.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void testValidateDeleteUserForeignKeysButInMultipleDirectories() throws Exception
    {
        final UserManager userManager = mock(UserManager.class);
        final ApplicationUser admin = new MockApplicationUser("Admin");
        final ApplicationUser fred = new MockApplicationUser("Fred");
        when(userManager.isUserExisting(fred)).thenReturn(true);
        when(userManager.canUpdateUser(fred)).thenReturn(true);
        when(userManager.getUserState(fred)).thenReturn(UserManager.UserState.NORMAL_USER_WITH_SHADOW);

        DefaultUserService userService = new DefaultUserService(userUtil, null, new MockPermissionManager(true), userManager, null, i18nFactory, null, null, preDeleteUserErrorsManager, passwordPolicyManager);
        UserService.DeleteUserValidationResult validationResult = userService.validateDeleteUser(admin, fred);

        // When the user is in multiple directories, these shouldn't get checked at all
        // (This also checks the old User.class forms, as Matchers.any doesn't do type checks)
        verify(userUtil, never()).getNumberOfReportedIssuesIgnoreSecurity(any(ApplicationUser.class), any(ApplicationUser.class));
        verify(userUtil, never()).getNumberOfAssignedIssuesIgnoreSecurity(any(ApplicationUser.class), any(ApplicationUser.class));
        verify(userUtil, never()).getProjectsLeadBy(any(ApplicationUser.class));

        assertNoErrors(validationResult);
    }

    @Test
    public void testValidateDeleteUserForeignKeys() throws Exception
    {
        final MockUserManager userManager = new MockUserManager();
        final ApplicationUser admin = new MockApplicationUser("Admin");
        final ApplicationUser fred = new MockApplicationUser("Fred");
        userManager.addUser(admin);
        userManager.addUser(fred);
        when(userUtil.getNumberOfReportedIssuesIgnoreSecurity(admin, fred)).thenReturn(387L);
        when(userUtil.getNumberOfAssignedIssuesIgnoreSecurity(admin, fred)).thenReturn(26L);
        when(userUtil.getProjectsLeadBy(fred)).thenReturn(Collections.<Project>singleton(new MockProject(10003L)));

        DefaultUserService userService = new DefaultUserService(userUtil, new MockUserDeleteVeto().setDefaultCommentCount(12), new MockPermissionManager(true), userManager, null, i18nFactory, null, null, preDeleteUserErrorsManager, passwordPolicyManager);
        UserService.DeleteUserValidationResult validationResult = userService.validateDeleteUser(admin, "fred");

        final List<String> expectedErrors = asList(
                "Cannot delete user 'Fred' because 387 issues were reported by this person.",
                "Cannot delete user 'Fred' because 26 issues are currently assigned to this person.",
                "Cannot delete user 'Fred' because they have made 12 comments.",
                "Cannot delete user 'Fred' because they are currently the project lead on 1 projects.");
        assertEquals(expectedErrors, validationResult.getErrorCollection().getErrorMessages());
        assertTrue("Should have errors", validationResult.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void testValidateDeleteUserNonSysAdminAttemptingToDeleteSysAdmin() throws Exception
    {
        final MockUserManager userManager = new MockUserManager();
        final MockApplicationUser fred = new MockApplicationUser("Fred");
        userManager.addUser(fred);
        mockGlobalPermission(Permissions.SYSTEM_ADMIN, fred);

        DefaultUserService userService = new DefaultUserService(null, null, permissionManager, userManager, null, i18nFactory, null, null, preDeleteUserErrorsManager, passwordPolicyManager);
        UserService.DeleteUserValidationResult validationResult = userService.validateDeleteUser(admin, "frEd");

        final List<String> expectedErrors = asList("As a user with JIRA Administrators permission, you cannot delete users with JIRA System Administrators permission.");
        assertEquals(expectedErrors, validationResult.getErrorCollection().getErrorMessages());
        assertTrue("Should have errors", validationResult.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void testValidateDeleteUserHappyHappyJoyJoy() throws Exception
    {
        final MockUserManager userManager = new MockUserManager();
        userManager.addUser(new MockApplicationUser("fred"));

        DefaultUserService userService = new DefaultUserService(userUtil, new MockUserDeleteVeto(), new MockPermissionManager(true), userManager, null, i18nFactory, null, null, preDeleteUserErrorsManager, passwordPolicyManager);
        UserService.DeleteUserValidationResult validationResult = userService.validateDeleteUser(new MockApplicationUser("admin"), "fred");

        assertNoErrors(validationResult);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveUserNullResult()
    {
        final UserService userService = new DefaultUserService(userUtil, null, new MockPermissionManager(true), null, null, null, null, null, preDeleteUserErrorsManager, passwordPolicyManager);
        userService.removeUser(new MockApplicationUser("admin"), null);
    }

    @Test(expected = IllegalStateException.class)
    public void testRemoveUserResultWithError()
    {
        final ErrorCollection errors = new SimpleErrorCollection();
        errors.addErrorMessage("error");

        final UserService userService = new DefaultUserService(userUtil, null, new MockPermissionManager(true), null, null, null, null, null, preDeleteUserErrorsManager, passwordPolicyManager);
        final UserService.DeleteUserValidationResult result = new UserService.DeleteUserValidationResult(errors);
        userService.removeUser(new MockApplicationUser("admin"), result);
    }

    @Test
    public void testRemoveUser() throws Exception
    {
        final MockUserManager userManager = new MockUserManager();
        final MockApplicationUser loggedInUser = new MockApplicationUser("admin");
        final MockApplicationUser fred = new MockApplicationUser("Fred");
        userManager.addUser(fred);

        DefaultUserService userService = new DefaultUserService(userUtil, new MockUserDeleteVeto(), new MockPermissionManager(true), userManager, null, i18nFactory, null, null, preDeleteUserErrorsManager, passwordPolicyManager);
        UserService.DeleteUserValidationResult validationResult = userService.validateDeleteUser(loggedInUser, "fred");
        assertNoErrors(validationResult);

        userService.removeUser(loggedInUser, validationResult);
        verify(userUtil).removeUser(loggedInUser, fred);
    }

    private void assertNoErrors(ServiceResultImpl validationResult)
    {
        assertEquals(Collections.<String>emptyList(), validationResult.getErrorCollection().getErrorMessages());
        assertFalse("Should have no errors", validationResult.getErrorCollection().hasAnyErrors());
        assertTrue("Should be valid", validationResult.isValid());
    }

    @Test
    public void testCreateUserNullResult() throws PermissionException, CreateException
    {
        final UserService userService = new DefaultUserService(null, null, null, null, null, null, null, null, null, passwordPolicyManager);
        try
        {
            userService.createUserWithNotification(null);
            fail("Should not be able to create a user with a null validation result.");
        }
        catch (final IllegalArgumentException e)
        {
            assertEquals("You can not create a user, validation result should not be null!", e.getMessage());
        }
    }

    @Test
    public void testCreateUserInvalidResult() throws PermissionException, CreateException
    {
        final ErrorCollection errors = new SimpleErrorCollection();
        errors.addErrorMessage("Something went wrong");
        final UserService.CreateUserValidationResult result = new UserService.CreateUserValidationResult(errors);
        final UserService userService = new DefaultUserService(null, null, null, null, null, null, null, null, null, passwordPolicyManager);

        try
        {
            userService.createUserWithNotification(result);
            fail("Should not be able to create a user with an invalid validation result.");
        }
        catch (final IllegalStateException e)
        {
            assertEquals("You can not create a user with an invalid validation result.", e.getMessage());
        }
    }

    @Test
    public void testCreateJiraUserWithPassword() throws PermissionException, CreateException
    {
        createUser("fflintstone", "mypassword", "fred@flintstone.com", "Fred Flintstone");
    }


    @Test
    public void testCreateJiraUserWithNullPassword() throws PermissionException, CreateException
    {
        // We don't use it anywhere in JIRA, but it should simply generate a random password for wilma.
        // Since that's done inside UserUtil, we don't need to verify that here.
        createUser("wflintstone", null, "wilma@flintstone.com", "Wilma Flintstone");
    }


    private void mockGlobalPermission(int permission, ApplicationUser user)
    {
        when(permissionManager.hasPermission(permission, user)).thenReturn(true);
        when(permissionManager.hasPermission(permission, user.getDirectoryUser())).thenReturn(true);
    }

    private JiraContactHelper getJiraContactHelper()
    {
        final JiraContactHelper jiraContactHelper = mock(JiraContactHelper.class);
        when(jiraContactHelper.getAdministratorContactMessage(any(I18nHelper.class))).thenReturn("please contact your JIRA administrators");
        return jiraContactHelper;
    }
}
