package com.atlassian.jira.web.action.setup;

import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.bc.user.UserService;
import com.atlassian.jira.bc.user.UserServiceResultHelper;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoContainer;
import com.atlassian.jira.license.LicenseDetails;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.mock.servlet.MockHttpServletRequest;
import com.atlassian.jira.mock.servlet.MockHttpSession;
import com.atlassian.jira.plugin.webresource.JiraWebResourceManager;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.MockGroup;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.HttpServletVariables;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import junit.framework.Assert;
import webwork.action.Action;
import webwork.action.ServletActionContext;

import static org.hamcrest.Matchers.isIn;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestSetupAdminAccount
{
    private SetupAdminAccount setupAdminAccountAction;

    @Rule
    public MockitoContainer mockitoContainer = new MockitoContainer(this);

    @Mock
    private UserService userService;

    @Mock
    private UserUtil userUtil;

    @Mock
    private GroupManager groupManager;

    @AvailableInContainer(instantiateMe = true)
    private MockApplicationProperties applicationProperties;

    @Mock
    @AvailableInContainer
    private JiraAuthenticationContext jiraAuthenticationContext;

    @AvailableInContainer(instantiateMe = true)
    private MockI18nHelper mockI18nHelper;

    @Mock
    @AvailableInContainer
    private GlobalPermissionManager globalPermissionManager;

    @Mock private HttpServletVariables servletVariables;
    @Mock private HttpSession httpSession;
    @Mock private JiraLicenseService jiraLicenseService;
    @Mock private LicenseDetails licenseDetails;
    @Mock private JiraWebResourceManager jiraWebResourceManager;

    @Before
    public void setUp() throws Exception
    {
        ServletActionContext.setRequest(new MockHttpServletRequest(new MockHttpSession()));
        when(jiraAuthenticationContext.getI18nHelper()).thenReturn(mockI18nHelper);
        when(servletVariables.getHttpSession()).thenReturn(httpSession);
        when(jiraLicenseService.getLicense()).thenReturn(licenseDetails);

        setupAdminAccountAction = new SetupAdminAccount(userService, groupManager, userUtil, null, servletVariables, null, jiraLicenseService, jiraWebResourceManager);
    }

    @After
    public void tearDown() throws Exception{
        ServletActionContext.setRequest(null);
    }

    @Test
    public void testWillVerifyMutators()
    {
        Assert.assertNull(setupAdminAccountAction.getUsername());
        Assert.assertNull(setupAdminAccountAction.getFullname());
        Assert.assertNull(setupAdminAccountAction.getEmail());
        Assert.assertNull(setupAdminAccountAction.getPassword());
        Assert.assertNull(setupAdminAccountAction.getConfirm());

        setupAdminAccountAction.setUsername("bob");
        setupAdminAccountAction.setFullname("bob smith");
        setupAdminAccountAction.setEmail("bob@bob.com");
        setupAdminAccountAction.setPassword("password");
        setupAdminAccountAction.setConfirm("password");

        Assert.assertEquals("bob", setupAdminAccountAction.getUsername());
        Assert.assertEquals("bob smith", setupAdminAccountAction.getFullname());
        Assert.assertEquals("bob@bob.com", setupAdminAccountAction.getEmail());
        Assert.assertEquals("password", setupAdminAccountAction.getPassword());
        Assert.assertEquals("password", setupAdminAccountAction.getConfirm());

    }

    @Test
    public void testWillVerifyForwardWhenJiraAlreadyConfigured() throws Exception
    {
        setupAdminAccountAction.getApplicationProperties().setString(APKeys.JIRA_SETUP, "true");
        Assert.assertEquals("setupalready", setupAdminAccountAction.doDefault());
    }


    @Test
    public void testWillVerifyForwardToExistingAdmins() throws Exception
    {
        when(userUtil.getJiraAdministrators()).thenReturn(ImmutableList.<User>of(new MockUser("fred"), new MockUser("george")));

        Assert.assertEquals("existingadmins", setupAdminAccountAction.doDefault());
    }

    @Test
    public void shouldDelegateUsersValidationToTheService() throws Exception{
        applicationProperties.setString(APKeys.JIRA_SETUP, "true");
        Assert.assertEquals("setupalready", setupAdminAccountAction.execute());
        applicationProperties.setString(APKeys.JIRA_SETUP, null);

        setupAdminAccountAction.setEmail("test@atlassian.com");
        setupAdminAccountAction.setFullname("test fullname");
        setupAdminAccountAction.setUsername("test username");
        setupAdminAccountAction.setPassword("password");
        setupAdminAccountAction.setConfirm("passwordconfirm");

        final MockUser fred = new MockUser("fred");
        when(jiraAuthenticationContext.getLoggedInUser()).thenReturn(fred);

        ErrorCollection ec = new SimpleErrorCollection();
        ec.addError("email", "This email address looks silly");
        UserService.CreateUserValidationResult result = UserServiceResultHelper.getCreateUserValidationResult(ec);
        when(userService.validateCreateUserForSetup(same(fred), eq("test username"), eq("password"), eq("passwordconfirm"), eq("test@atlassian.com"), eq("test fullname")))
                .thenReturn(result);


        Assert.assertEquals(Action.INPUT, setupAdminAccountAction.execute());
        Assert.assertEquals(ec.getErrors(), setupAdminAccountAction.getErrors());
    }


    @Test
    public void testWillForwardToExistingAdminsForExistingAdmin() throws Exception
    {
        setAllValidData();

        when(userUtil.getJiraAdministrators()).thenReturn(ImmutableList.<User>of(new MockUser("fred"), new MockUser("george")));

        Assert.assertEquals("existingadmins", setupAdminAccountAction.execute());
    }

    @Test
    public void testWillCreateUserSuccessfully() throws Exception
    {
        final UserService.CreateUserValidationResult result = setAllValidData();
        Assert.assertEquals(Action.SUCCESS, setupAdminAccountAction.execute());
        verify(userService).createUserNoNotification(result);
    }

    @Test
    public void shouldAddProperErrorOnPermissionException() throws Exception{
        final UserService.CreateUserValidationResult result = setAllValidData();
        when(userService.createUserNoNotification(result)).thenThrow(new PermissionException());
        Assert.assertEquals(Action.ERROR, setupAdminAccountAction.execute());

        assertThat(setupAdminAccountAction.getErrorMessages(), Matchers.containsInAnyOrder("signup.error.group.database.immutable [test username]"));
    }

    @Test
    public void shouldCreateUserGroupsWhichAreMissing() throws Exception{
        final UserService.CreateUserValidationResult result = setAllValidData();
        when(userService.createUserNoNotification(result)).thenReturn(new MockUser(result.getUsername()));
        when(groupManager.groupExists(anyString())).thenReturn(false);
        setupAdminAccountAction.execute();

        verify(groupManager).createGroup(AbstractSetupAction.DEFAULT_GROUP_ADMINS);
        verify(groupManager).createGroup(AbstractSetupAction.DEFAULT_GROUP_DEVELOPERS);
        verify(groupManager).createGroup(AbstractSetupAction.DEFAULT_GROUP_USERS);
    }

    @Test
    public void shouldAddUserToAllGroupsGroups() throws Exception{
        testAddUserToSpecifiedGroup(
                AbstractSetupAction.DEFAULT_GROUP_ADMINS,
                AbstractSetupAction.DEFAULT_GROUP_USERS,
                AbstractSetupAction.DEFAULT_GROUP_DEVELOPERS
        );
    }

    @Test
    public void shouldNotAddUserToAnyGroupWHenIsAlreadyThere() throws Exception{
        testAddUserToSpecifiedGroup(/*empty group list */);
    }

    @Test
    public void shouldAddOnlyToSpecifiedGroupsWhenUserIsAlreadyInSomeOfThem() throws Exception{
        testAddUserToSpecifiedGroup(
                AbstractSetupAction.DEFAULT_GROUP_ADMINS,
                AbstractSetupAction.DEFAULT_GROUP_USERS
        );
    }

    @Test
    public void shouldAddGlobalAdministrationPermissionForAdmins() throws Exception{
        final UserService.CreateUserValidationResult result = setAllValidData();
        final MockUser user = new MockUser(result.getUsername());
        when(userService.createUserNoNotification(result)).thenReturn(user);

        when(globalPermissionManager.getGroupNames(Permissions.ADMINISTER)).thenReturn(ImmutableList.<String>of());
        when(groupManager.getGroup(anyString())).thenReturn(new MockGroup("group"));

        setupAdminAccountAction.execute();

        verify(globalPermissionManager).addPermission(Permissions.ADMINISTER, AbstractSetupAction.DEFAULT_GROUP_ADMINS);
    }

    private void testAddUserToSpecifiedGroup(String... notInGroups) throws Exception{
        reset(userService, groupManager);
        Set<String> notAlreadyIn= ImmutableSet.copyOf(notInGroups);

        final UserService.CreateUserValidationResult result = setAllValidData();
        final MockUser user = new MockUser(result.getUsername());
        when(userService.createUserNoNotification(result)).thenReturn(user);

        Map<String, MockGroup> groups = ImmutableMap.of(
                AbstractSetupAction.DEFAULT_GROUP_ADMINS, new MockGroup(AbstractSetupAction.DEFAULT_GROUP_ADMINS),
                AbstractSetupAction.DEFAULT_GROUP_USERS, new MockGroup(AbstractSetupAction.DEFAULT_GROUP_USERS),
                AbstractSetupAction.DEFAULT_GROUP_DEVELOPERS, new MockGroup(AbstractSetupAction.DEFAULT_GROUP_DEVELOPERS)
        );
        when(groupManager.groupExists(anyString())).thenReturn(true);
        for (Map.Entry<String, MockGroup> groupEntry : groups.entrySet())
        {
            when(groupManager.getGroup(groupEntry.getKey())).thenReturn(groupEntry.getValue());
        }

        when(groupManager.isUserInGroup(anyString(), argThat(isIn(notAlreadyIn)))).thenReturn(false);
        when(groupManager.isUserInGroup(anyString(), argThat(Matchers.not(isIn(notAlreadyIn))))).thenReturn(true);

        setupAdminAccountAction.execute();

        for (String s : groups.keySet())
        {
            if(notAlreadyIn.contains(s)){
                verify(groupManager).addUserToGroup(user, groups.get(s));
            } else {
                verify(groupManager, never()).addUserToGroup(user, groups.get(s));
            }
        }
    }

    private UserService.CreateUserValidationResult setAllValidData()
    {
        setupAdminAccountAction.setEmail("test@atlassian.com");
        setupAdminAccountAction.setFullname("test fullname");
        setupAdminAccountAction.setUsername("test username");
        setupAdminAccountAction.setPassword("password");
        setupAdminAccountAction.setConfirm("passwordconfirm");
        UserService.CreateUserValidationResult result = UserServiceResultHelper.getCreateUserValidationResult("test username", "password", "test@atlassian.com", "test fullname");
        when(userService.validateCreateUserForSetup(any(User.class), eq("test username"), eq("password"), eq("passwordconfirm"), eq("test@atlassian.com"), eq("test fullname")))
                .thenReturn(result);
        return result;
    }
}
