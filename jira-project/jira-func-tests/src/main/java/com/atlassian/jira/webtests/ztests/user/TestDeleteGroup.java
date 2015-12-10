package com.atlassian.jira.webtests.ztests.user;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.Groups;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.meterware.httpunit.WebTable;
import org.xml.sax.SAXException;

/**
 * These tests cover the deletion of groups through the Group Browser
 */
@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS })
public class TestDeleteGroup extends JIRAWebTest
{
    private static final String ADMIN_GROUP_2 = "jira-admin-2";
    private static final String ADMIN_USER = ADMIN_USERNAME;
    private static final String OTHER_GROUP = "jira-developers";
    private static final String ADMIN_GROUP = "jira-administrators";

    public TestDeleteGroup(String name)
    {
        super(name);
    }

    public void testOneAdminGroupOneNonAdminGroup()
    {
        //Setup test data for TC1&2 (One admin group, user is a member)
        restoreBlankInstance();

        //Test Case 1: User attempts to delete the admin group (they are a member).
        attemptToDeleteGroup(ADMIN_GROUP);
        assertTextPresent("You cannot delete a group that grants you system administration privileges if no other group exists that also grants you system administration privileges.");

        //Test Case 2: User then attempts to delete a different (non-admin) group they aren't a part of.
        attemptToDeleteGroup(OTHER_GROUP);
        submit("Delete");
        assertTextNotPresent(OTHER_GROUP);
    }

    public void testTwoAdminGroupsUserIsMemberOfOne()
    {
        //Setup test data for TC3&4 (Two admin groups, user is a member of one)
        restoreBlankInstance();
        createGroup(ADMIN_GROUP_2);
        grantGlobalPermission(SYSTEM_ADMINISTER, ADMIN_GROUP_2);

        //Test Case 3: User attempts to delete the admin group they are a part of.
        attemptToDeleteGroup(ADMIN_GROUP);
        assertTextPresent("You cannot delete a group that grants you system administration privileges if no other group exists that also grants you system administration privileges.");

        //Test Case 4: User then attempts to delete the admin group they aren't a part of.
        attemptToDeleteGroup(ADMIN_GROUP_2);
        submit("Delete");
        assertTextNotPresent(ADMIN_GROUP_2);
    }

    public void testTwoSysAdminGroupsUserIsMemberOfBoth()
    {
        //Setup test data for TC5&6 (Two admin groups, user is a member of both)
        restoreBlankInstance();
        createGroup(ADMIN_GROUP_2);
        grantGlobalPermission(SYSTEM_ADMINISTER, ADMIN_GROUP_2);
        addUserToGroup(ADMIN_USER, ADMIN_GROUP_2);

        //Test Case 5: User attempts to delete one of their admin groups.
        attemptToDeleteGroup(ADMIN_GROUP);
        submit("Delete");
        assertTextNotPresent(ADMIN_GROUP);

        //Test Case 6: User attempts to delete their other admin group.
        attemptToDeleteGroup(ADMIN_GROUP_2);
        assertTextPresent("You cannot delete a group that grants you system administration privileges if no other group exists that also grants you system administration privileges.");
    }

    public void testNoSysAdminGroupOneAdminGroupOneOther()
    {
        try
        {
            //Setup test data for TC1&2 (One admin group, user is a member)
            restoreData("TestWithSystemAdmin.xml");

            //Test Case 1: User attempts to delete the admin group (they are a member).
            attemptToDeleteGroup(ADMIN_GROUP);
            assertTextPresent("You cannot delete a group that grants you administration privileges if no other group exists that also grants you administration privileges.");

            //Test Case 2: User then attempts to delete a different (non-admin) group they aren't a part of.
            attemptToDeleteGroup(OTHER_GROUP);
            submit("Delete");
            assertTextNotPresent(OTHER_GROUP);
        }
        finally
        {
            logout();
            // go back to sysadmin user
            login("root", "root");
            restoreBlankInstance();

        }
    }

    public void testNoSysAdminGroupTwoAdminGroupUserIsMemberOfBoth()
    {
        try
        {
            //Setup test data for TC1&2 (One admin group, user is a member)
            restoreData("TestWithSystemAdmin.xml");

            createGroup(ADMIN_GROUP_2);
            giveAdminPermission(ADMIN_GROUP_2);
            addUserToGroup(ADMIN_USER, ADMIN_GROUP_2);

            //Test Case 5: User attempts to delete one of their admin groups.
            attemptToDeleteGroup(ADMIN_GROUP);
            submit("Delete");
            assertTextNotPresent(ADMIN_GROUP);

            //Test Case 6: User attempts to delete their other admin group.
            attemptToDeleteGroup(ADMIN_GROUP_2);
            assertTextPresent("You cannot delete a group that grants you administration privileges if no other group exists that also grants you administration privileges.");
        }
        finally
        {
            logout();
            // go back to sysadmin user
            login("root", "root");
            restoreBlankInstance();
        }
    }

    public void testDeleteSysAdminGroupAsAdmin()
    {
        try
        {
            //Setup test data for TC1&2 (One admin group, user is a member)
            restoreData("TestWithSystemAdmin.xml");

            // Try to delete a group we should not have permission to delete
            gotoPage("/secure/admin/user/DeleteGroup!default.jspa?name=jira-sys-admins");

            assertTextPresent("Cannot delete group, only System Administrators can delete groups associated with the System Administrators global permission.");
        }
        finally
        {
            logout();
            // go back to sysadmin user
            login("root", "root");
            restoreBlankInstance();
        }
    }

    public void testSysAdminGroupsOperationsDoNotShowToAdmins() throws SAXException
    {
        try
        {
            //Setup test data for TC1&2 (One admin group, user is a member)
            restoreData("TestWithSystemAdmin.xml");

            gotoAdmin();
            clickLink("group_browser");

            WebTable groupTable = getDialog().getResponse().getTableWithID("group_browser_table");
            assertTableCellHasText("group_browser_table", 1, 0, "jira-administrators");
            assertTableCellHasText("group_browser_table", 1, 3, "Delete");
            assertTableCellHasText("group_browser_table", 1, 3, "Edit Members");
            assertLinkPresent("del_jira-administrators");
            assertLinkPresent("edit_members_of_jira-administrators");

            assertTableCellHasText("group_browser_table", 2, 0, "jira-developers");
            assertTableCellHasText("group_browser_table", 2, 3, "Delete");
            assertTableCellHasText("group_browser_table", 2, 3, "Edit Members");
            assertLinkPresent("del_jira-developers");
            assertLinkPresent("edit_members_of_jira-developers");

            assertTableCellHasText("group_browser_table", 3, 0, "jira-sys-admins");
            assertTrue("shouldn't have delete operation", groupTable.getCellAsText(3, 3).indexOf("Delete") == -1);
            assertTrue("shouldn't have edit operation", groupTable.getCellAsText(3, 3).indexOf("Edit Members") == -1);
            assertLinkNotPresent("del_jira-sys-admins");
            assertLinkNotPresent("edit_members_of_jira-sys-admins");

            assertTableCellHasText("group_browser_table", 4, 0, "jira-users");
            assertTableCellHasText("group_browser_table", 4, 3, "Delete");
            assertTableCellHasText("group_browser_table", 4, 3, "Edit Members");
            assertLinkPresent("del_jira-users");
            assertLinkPresent("edit_members_of_jira-users");
        }
        finally
        {
            logout();
            // go back to sysadmin user
            login("root", "root");
            restoreBlankInstance();
        }
    }

    public void testDeleteGroupSwapGroup()
    {
        restoreData("TestDeleteGroupSwapGroup.xml");

        gotoIssue("HSP-1");
        assertTextPresent("Test Comment Visibility");

        attemptToDeleteGroup(Groups.ADMINISTRATORS);
        selectOption("swapGroup", "other-admins");
        submit("Delete");

        gotoIssue("HSP-1");
        assertTextPresent("Test Comment Visibility");
    }

    public void testDeleteGroupSwapGroupSameGroup()
    {
        restoreData("TestDeleteGroupSwapGroup.xml");

        gotoIssue("HSP-1");
        assertTextPresent("Test Comment Visibility");

        gotoPage(page.addXsrfToken("/secure/admin/user/DeleteGroup.jspa?name=jira-administrators&swapGroup=jira-administrators"));
        assertTextPresent("You cannot swap comments/worklogs to the group you are deleting.");
    }

    public void testDeleteGroupAsFred()
    {
        restoreBlankInstance();

        try
        {
            getNavigation().loginUsingForm(FRED_USERNAME, FRED_PASSWORD);
            gotoPage(page.addXsrfToken("/secure/admin/user/DeleteGroup!default.jspa?name=jira-administrators"));
            assertTextPresent("my login on this computer");
        }
        finally
        {
            login(ADMIN_USERNAME, ADMIN_PASSWORD);
        }
    }

    public void testDeleteInvalidGroup()
    {
        restoreBlankInstance();

        gotoPage(page.addXsrfToken("/secure/admin/user/DeleteGroup.jspa?name=invalid"));
        assertions.getJiraFormAssertions().assertFormErrMsg("The group 'invalid' is not a valid group.");
    }

    private void attemptToDeleteGroup(String groupName)
    {
        gotoAdmin();
        clickLink("group_browser");
        String linkId = "del_" + groupName;
        clickLink(linkId);
    }

    private void giveAdminPermission(String groupName)
    {
        gotoAdmin();
        clickLink("global_permissions");
        selectOption("globalPermType", "JIRA Administrators");
        selectOption("groupName", groupName);
        submit("Add");
    }
}
