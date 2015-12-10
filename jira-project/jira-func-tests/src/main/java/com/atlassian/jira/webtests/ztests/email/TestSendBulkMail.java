package com.atlassian.jira.webtests.ztests.email;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

@WebTest ({ Category.FUNC_TEST, Category.BULK_OPERATIONS, Category.EMAIL })
// Currently passing
public class TestSendBulkMail extends FuncTestCase
{
    private static final String LINK_TEXT_CONFIG_NEW_SMTP = "Configure new SMTP mail server";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_FROM = "from";
    private static final String FIELD_PREFIX = "prefix";
    private static final String FIELD_SERVER_NAME = "serverName";
    private static final String FIELD_USERNAME = "username";
    private static final String FIELD_PASSWORD = "password";
    private static final String BUTTON_ADD = "Add";

    private static final class DummyMailServer
    {
        private static final String NAME = "name SMTP";
        private static final String FROM_ADDRESS = "from@atlassian.com";
        private static final String EMAIL_PREFIX = "prefix";
        private static final String SERVER_NAME = "server name";
        private static final String USER_NAME = "username";
        private static final String PASSWORD = "password";
    }

    private static final Map<String, String> ROLE_TO_ID =
            ImmutableMap.of
                    (
                            JIRA_ADMIN_ROLE, "10002",
                            JIRA_DEV_ROLE, "10001",
                            JIRA_USERS_ROLE, "10000"
                    );

    private static final String TEST_ROLES_PROJECT = "Test Roles Project";
    private static final String TEST_ROLES_PROJECT_KEY = "TRP";
    private static final String TEST_GROUP = "testGroup";

    public void setUpTest()
    {
        super.setUpTest();
        administration.restoreBlankInstance();

        //add dummy mail server (otherwise you cannot access the SendBulkMail page)
        setUpDummyMailServer();
    }

    public void tearDownTest()
    {
        administration.restoreBlankInstance();
        super.tearDownTest();
    }

    public void testRolesSelectedAsDefault()
    {
        administration.sendBulkMail().goTo();
        tester.assertRadioOptionSelected("sendToRoles", "true");
    }

    public void testErrors()
    {
        ////
        //Test errors related to sending bulk e-mails to Project Roles
        ////

        //add users with allocated Project Roles
        final long projectId = administration.project().addProject(TEST_ROLES_PROJECT, TEST_ROLES_PROJECT_KEY, ADMIN_USERNAME);
        createUserAndAddToRole(BOB_USERNAME, TEST_ROLES_PROJECT, projectId, JIRA_DEV_ROLE);


        administration.sendBulkMail().goTo();
        tester.setFormElement("replyTo", "TEST@TEST.NET");
        tester.setFormElement("subject", "TEST");
        tester.setFormElement("message", "TEST");
        tester.submit("Send");

        text.assertTextPresent(locator.page(), "Please select at least one project and one role.");

        tester.selectOption("projects", "homosapien");
        tester.submit("Send");

        text.assertTextPresent(locator.page(), "Please select at least one role.");

        administration.sendBulkMail().goTo();
        tester.selectOption("roles", "Administrators");
        tester.submit("Send");

        text.assertTextPresent(locator.page(), "Please select at least one project.");

        tester.selectOption("projects", "homosapien");
        tester.submit("Send");

        text.assertTextPresent(locator.page(), "The chosen project/role combination(s) have no members.");

        ////
        //Test errors related to sending bulk e-mails to Groups
        ////

        //add a group with no users
        administration.usersAndGroups().addGroup(TEST_GROUP);

        administration.sendBulkMail().goTo();
        tester.checkRadioOption("sendToRoles", "false");

        tester.setFormElement("replyTo", "TEST@TEST.NET");
        tester.setFormElement("subject", "TEST");
        tester.setFormElement("message", "TEST");
        tester.submit("Send");

        text.assertTextPresent(locator.page(), "Please select at least one group.");

        tester.selectOption("groups", TEST_GROUP);
        tester.submit("Send");

        text.assertTextPresent(locator.page(), "The chosen group(s) have no members.");

        ////
        //Test errors related to the mail contents
        ////

        administration.sendBulkMail().goTo();

        tester.selectOption("projects", "homosapien");
        tester.selectOption("roles", "Administrators");
        tester.setFormElement("replyTo", "not a valid e-mail address");
        tester.submit("Send");

        text.assertTextPresent(locator.page(), "Invalid email address format.");
        text.assertTextPresent(locator.page(), "Please specify a subject.");
        text.assertTextPresent(locator.page(), "Please provide a message body.");
    }

    private void createUserAndAddToRole(final String userName, final String projectName, final long projectId,
            final String role)
    {
        administration.usersAndGroups().addUser(userName, userName, userName, userName + "@atlassian.com");
        administration.usersAndGroups().gotoViewUser(userName);
        tester.clickLink("viewprojectroles_link");
        tester.clickLinkWithText("Edit Project Roles");
        // check for a link to make the project visible, if it's there, click it so we can choose the project
        if (tester.getDialog().isLinkPresent(projectName))
        {
            tester.clickLinkWithText(projectName);
        }
        String targetCheckboxId = projectId + "_" + ROLE_TO_ID.get(role); //checkbox id format: 'projId_roleId'
        tester.checkCheckbox(targetCheckboxId, "on");
        tester.submit("Save");
    }

    private void setUpDummyMailServer()
    {
        administration.mailServers().Smtp().goTo();
        tester.clickLinkWithText(LINK_TEXT_CONFIG_NEW_SMTP);
        tester.setFormElement(FIELD_NAME, DummyMailServer.NAME);
        tester.setFormElement(FIELD_FROM, DummyMailServer.FROM_ADDRESS);
        tester.setFormElement(FIELD_PREFIX, DummyMailServer.EMAIL_PREFIX);
        tester.setFormElement(FIELD_SERVER_NAME, DummyMailServer.SERVER_NAME);
        tester.setFormElement(FIELD_USERNAME, DummyMailServer.USER_NAME);
        tester.setFormElement(FIELD_PASSWORD, DummyMailServer.PASSWORD);
        tester.submit(BUTTON_ADD);
    }
}
