package com.atlassian.jira.webtests.ztests.user;

import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.EmailFuncTestCase;
import com.atlassian.jira.webtests.ztests.tpm.ldap.UserDirectoryTable;
import com.meterware.httpunit.WebLink;
import org.junit.Ignore;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS })
public class TestAddUser extends EmailFuncTestCase
{
    private static final String CREATE_USER_SUBMIT = "Create";
    
    private static final String TEST_USERNAME = "user";
    private static final String TEST_FULL_NAME = "User Tested";
    private static final String TEST_EMAIL = "username@email.com";
    private static final String TEST_PASSWORD = "evilwoman";

    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreBlankInstance();
    }

    public void testEmptyData()
    {
        buildUser("", "", "");
        tester.submit(CREATE_USER_SUBMIT);

        tester.assertTextPresent("Create New User");
        tester.assertTextPresent("You must specify a username.");
        tester.assertTextPresent("You must specify a full name.");
        tester.assertTextPresent("You must specify an email address.");
    }

    public void testCreateDuplicateUser()
    {
        checkSuccessUserCreate(false);

        buildUser(TEST_USERNAME, TEST_FULL_NAME, TEST_EMAIL);
        tester.submit(CREATE_USER_SUBMIT);
        tester.assertTextPresent("A user with that username already exists.");
    }

    public void testCreateUserSuccess()
    {
        checkSuccessUserCreate(false);
    }

    public void testCreateUserInvalidEmail()
    {
        buildUser(TEST_USERNAME, TEST_FULL_NAME, "username.email.com");
        tester.submit(CREATE_USER_SUBMIT);
        tester.assertTextPresent("You must specify a valid email address.");
    }

    public void testCreateUserPassword()
    {
        buildUser(TEST_USERNAME, TEST_FULL_NAME, TEST_EMAIL);
        tester.setFormElement("password", "password");
        tester.submit(CREATE_USER_SUBMIT);
        tester.assertTextPresent("Your password and confirmation password do not match.");

        tester.setFormElement("confirm", "confirm");
        tester.submit(CREATE_USER_SUBMIT);
        tester.assertTextPresent("Your password and confirmation password do not match.");

        tester.setFormElement("password", "abc");
        tester.setFormElement("confirm", "def");
        tester.submit(CREATE_USER_SUBMIT);
        tester.assertTextPresent("Your password and confirmation password do not match.");

        tester.setFormElement("password", "password");
        tester.setFormElement("confirm", "password");
        tester.submit(CREATE_USER_SUBMIT);

        tester.assertTextPresent("User: " + TEST_FULL_NAME);
        final String[] userDetails = { "Username:", TEST_USERNAME,
                "Full Name:", TEST_FULL_NAME,
                "Email:", TEST_EMAIL
        };
        text.assertTextSequence(new WebPageLocator(tester), userDetails);
    }

    public void testNoPermission()
    {
        navigation.logout();
        navigation.login(FRED_USERNAME, FRED_PASSWORD);

        tester.gotoPage("http://localhost:8090/jira/secure/admin/user/AddUser!default.jspa");

        tester.assertTextPresent("Welcome to jWebTest JIRA installation");
        tester.assertTextNotPresent("Project: newproject");
        tester.assertTextNotPresent("Add a new project");
    }

    @Ignore ("JRADEV-8029 Can no longer do this in a simple func test. Need to make User Directories read-only")
    public void testCreateUserExternalUserConfiguration()
    {
        administration.generalConfiguration().setExternalUserManagement(true);

        navigation.gotoAdminSection("user_browser");
        tester.assertLinkNotPresent("create_user");
        tester.assertTextNotPresent("Create User");

        administration.generalConfiguration().setExternalUserManagement(false);

        checkSuccessUserCreate(false);
    }

    public void testCreateUserEmailSent() throws InterruptedException, IOException, MessagingException
    {
        configureAndStartSmtpServer();

        checkSuccessUserCreate(true);

        // now see if we got an email with the right details
        flushMailQueueAndWait(1);

        MimeMessage[] mimeMessages = mailService.getReceivedMessages();
        assertEquals(1, mimeMessages.length);

        final MimeMessage msg = mimeMessages[0];
        String body = msg.getContent().toString();
        assertEmailBodyContainsLine(msg, TEST_USERNAME);
        assertEmailBodyContainsLine(msg, TEST_FULL_NAME);
        assertEmailBodyContains(msg, TEST_EMAIL);

        // does it have the reset password part
        assertEmailBodyContains(msg, "Get started by setting your own password and logging in");
        assertEmailBodyContains(msg, "secure/ResetPassword!default.jspa?os_username="+TEST_USERNAME+"&amp;token=");

        // and make sure it has NO password in there
        assertEmailBodyDoesntContain(msg, TEST_PASSWORD);

    }

    /**
     * Goes to the create user form and fills in values specified, but does not submit the form.
     * @param username the username to give the user to be created.
     * @param fullName the full name to give the user to be created.
     * @param email the email address to give the user to be created.
     */
    private void buildUser(final String username, final String fullName, final String email)
    {
        navigation.gotoAdminSection("user_browser");
        tester.clickLink("create_user");
        tester.assertTextPresent("Create New User");

        tester.setFormElement("username", username);
        tester.setFormElement("fullname", fullName);
        tester.setFormElement("email", email);
    }

    private void checkSuccessUserCreate(final boolean sendEmail)
    {
        buildUser(TEST_USERNAME, TEST_FULL_NAME, TEST_EMAIL);
        tester.setFormElement("password", TEST_PASSWORD);
        tester.setFormElement("confirm", TEST_PASSWORD);
        if (sendEmail)
        {
            tester.checkCheckbox("sendEmail", "true");
        }
        tester.submit(CREATE_USER_SUBMIT);
        tester.assertTextPresent("User: " + TEST_FULL_NAME);

        final String[] userDetails = { "Username:", TEST_USERNAME,
                "Full Name:", TEST_FULL_NAME,
                "Email:", TEST_EMAIL
        };
        text.assertTextSequence(new WebPageLocator(tester), userDetails);
    }

    //JRA-22984
    public void testNewUsersAreNotGivenAdminRights()
    {
        navigation.gotoAdminSection("global_permissions");
        _addPermissionToGroup("USE", "jira-administrators");
        tester.assertElementNotPresent("del_USE_jira-administrators");
        _addPermissionToGroup("CREATE_SHARED_OBJECTS", "jira-users");
        tester.assertElementPresent("del_CREATE_SHARED_OBJECTS_jira-users");
        checkSuccessUserCreate(false);
        navigation.logout();
        navigation.login(TEST_USERNAME, TEST_PASSWORD);
        tester.assertElementNotPresent("admin_link");

        tester.gotoPage("/secure/ViewProfile.jspa");
        tester.assertTextPresent("jira-users");
        tester.assertTextNotPresent("jira-administrators");
        tester.assertTextNotPresent("jira-developers");
    }

    //JRA-25554
    public void testNewUsersNotAddedToNestedGroups()
    {
        try{

            addEditNestedGroups();

            checkSuccessUserCreate(false);

            // check the members groups
            navigation.gotoAdminSection("user_browser");
            tester.clickLink(TEST_USERNAME);
            tester.assertTextPresent("jira-users");
            tester.assertTextNotPresent("accounts");
            tester.assertTextNotPresent("sales");
            tester.assertTextNotPresent("customer-service");
        }
        finally
        {
            // Reset the admin password otherwise the next test to run will be fail because some dick hacked the authenticator in most tests.
            resetAdminPassword();
        }
    }

    private void _addPermissionToGroup(final String PermissionType, final String group) {

        tester.setFormElement("globalPermType", PermissionType);
        tester.setFormElement("groupName", group);
        tester.submit("Add");
    }

    private void addEditNestedGroups()
    {
        toggleNestedGroups(true);

        addGroup("accounts");
        addGroup("sales");
        addGroup("customer-service");
        navigation.gotoAdminSection("group_browser");
        tester.clickLink("edit_nested_groups");
        tester.assertTextPresent("This page allows you to edit nested group memberships.");

        tester.setWorkingForm("jiraform");
        selectMultiOption("selectedGroupsStr", "jira-users");
        selectMultiOption("childrenToAssignStr", "accounts");
        selectMultiOption("childrenToAssignStr", "sales");
        selectMultiOption("childrenToAssignStr", "customer-service");

        tester.submit("assign");
    }

    public void selectMultiOption(String selectName, String option)
    {
        // A bit of a hack. The only way to really select multiple options at the moment is to treat it like a checkbox
        String value = tester.getDialog().getValueForOption(selectName, option);
        tester.checkCheckbox(selectName, value);
    }

    private void addGroup(String groupName)
    {
        navigation.gotoAdmin();
        tester.clickLink("group_browser");
        tester.setFormElement("addName",groupName);
        tester.submit("add_group");
    }

    public void toggleNestedGroups(boolean enable)
    {
        navigation.gotoAdmin();
        tester.gotoPage("/plugins/servlet/embedded-crowd/directories/list");

        UserDirectoryTable userDirectoryTable = new UserDirectoryTable(this);
        WebLink link = userDirectoryTable.getTableCell(1, 4).getLinkWith("edit");
        navigation.clickLink(link);
        if (enable)
        {
            tester.checkCheckbox("nestedGroupsEnabled", "true");
        }
        else
        {
            tester.checkCheckbox("nestedGroupsEnabled", "false");
        }
        tester.submit("save");
    }

    public void resetAdminPassword()
    {
        navigation.gotoAdminSection("user_browser");
        tester.clickLink("admin");
        tester.clickLinkWithText("Set Password");
        tester.setFormElement("password", "admin");
        tester.setFormElement("confirm", "admin");
        tester.submit("Update");
    }

    private boolean find(final String s, final Pattern pattern)
    {
        return pattern.matcher(s).find();
    }

}