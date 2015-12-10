package com.atlassian.jira.webtests.ztests.user;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.Groups;
import com.atlassian.jira.webtests.JIRAWebTest;
import org.xml.sax.SAXException;

/**
 * Tests user group add/remove.
 *
 * @since v3.12
 */
@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS })
public class TestEditUserGroups extends JIRAWebTest
{
    public static final String ERROR_LEAVING_ALL_ADMIN_GROUPS = "You are trying to leave all of the administration groups jira-administrators. You cannot delete your own administration permission";
    public static final String ERROR_LEAVING_ALL_SYS_ADMIN_GROUPS = "You are trying to leave all of the system administration groups jira-administrators. You cannot delete your own system administration permission";

    public TestEditUserGroups(String name)
    {
        super(name);
    }

    public void testEditUserGroupsJoinAndLeaveAtSameTime() throws SAXException
    {
        restoreBlankInstance();

        navigateToUser(ADMIN_USERNAME);
        clickLink("editgroups_link");
        selectOption("groupsToLeave", Groups.DEVELOPERS);
        submit("leave");
        assertOptionValuePresent("groupsToJoin", "jira-developers");
        assertOptionValuePresent("groupsToLeave", "jira-administrators");
        assertOptionValuePresent("groupsToLeave", "jira-users");

        // Now go back and select to join while some to leave are selected, we should only join
        selectOption("groupsToLeave", Groups.USERS);
        selectOption("groupsToJoin", Groups.DEVELOPERS);
        submit("join");
        assertOptionValuePresent("groupsToLeave", "jira-developers");
        assertOptionValuePresent("groupsToLeave", "jira-administrators");
        assertOptionValuePresent("groupsToLeave", "jira-users");
    }

    public void testEditUserGroupsRemoveLastSysAdminGroup()
    {
        restoreBlankInstance();

        navigateToUser(ADMIN_USERNAME);
        clickLink("editgroups_link");
        selectOption("groupsToLeave", Groups.ADMINISTRATORS);
        submit("leave");
        assertTextPresent(ERROR_LEAVING_ALL_SYS_ADMIN_GROUPS);
        assertOptionValuePresent("groupsToLeave", Groups.ADMINISTRATORS);
    }

    public void testEditUserGroupsRemoveSysAdminGroupWithAnotherPresent()
    {
        restoreBlankInstance();

        createGroup("sys-admin-group2");
        grantGlobalPermission(SYSTEM_ADMINISTER, "sys-admin-group2");
        addUserToGroup(ADMIN_USERNAME, "sys-admin-group2");

        removeUserFromGroup(ADMIN_USERNAME, Groups.ADMINISTRATORS);
    }

    public void testEditUserGroupsWithNoSysAdminPermRemoveLastAdmin()
    {
        try
        {
            restoreData("TestWithSystemAdmin.xml");

            navigateToUser(ADMIN_USERNAME);
            clickLink("editgroups_link");
            selectOption("groupsToLeave", Groups.ADMINISTRATORS);
            submit("leave");
            assertTextPresent(ERROR_LEAVING_ALL_ADMIN_GROUPS);
        }
        finally
        {
            logout();
            // go back to sysadmin user
            login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            restoreBlankInstance();
        }
    }

    public void testEditUserGroupsWithNoSysAdminPermRemoveAdmin()
    {
        try
        {
            restoreData("TestWithSystemAdmin.xml");

            createGroup("admin-group2");
            grantGlobalPermission(ADMINISTER, "admin-group2");
            addUserToGroup(ADMIN_USERNAME, "admin-group2");

            removeUserFromGroup(ADMIN_USERNAME, Groups.ADMINISTRATORS);
            assertOptionValueNotPresent("groupsToLeave", "jira-administrators");
        }
        finally
        {
            logout();
            // go back to sysadmin user
            login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            restoreBlankInstance();
        }
    }

    public void testSysAdminEditGroups()
    {
        try
        {
            restoreData("TestWithSystemAdmin.xml");
            logout();
            login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);

            // admin should not be in sysadmins group
            navigateToUser(ADMIN_USERNAME);
            clickLink("editgroups_link");
            assertOptionValuePresent("groupsToJoin", "jira-sys-admins");
            assertOptionValueNotPresent("groupsToLeave", "jira-sys-admins");

            navigateToUser(SYS_ADMIN_USERNAME);
            clickLink("editgroups_link");
            assertOptionValueNotPresent("groupsToJoin", "jira-sys-admins");
            assertOptionValuePresent("groupsToLeave", "jira-sys-admins");
        }
        finally
        {
            logout();
            // go back to sysadmin user
            login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            restoreBlankInstance();
        }
    }

    public void testAdminEditGroups()
    {
        try
        {
            restoreData("TestWithSystemAdmin.xml");

            // admin should not be in sysadmins group
            navigateToUser(ADMIN_USERNAME);
            clickLink("editgroups_link");
            assertOptionValueNotPresent("groupsToJoin", "jira-sys-admins");
            assertOptionValueNotPresent("groupsToLeave", "jira-sys-admins");

            // validate that I can't fudge the url to add myself to the sys admins
            gotoPage(page.addXsrfToken("/secure/admin/user/EditUserGroups.jspa?name=" + ADMIN_USERNAME + "&groupsToJoin=jira-sys-admins&join=true"));
            assertTextPresent("You cannot add users to groups which are not visible to you.");

            // validate that as admin I can't see the sys-admin groups
            navigateToUser(SYS_ADMIN_USERNAME);
            clickLink("editgroups_link");
            assertOptionValueNotPresent("groupsToJoin", "jira-sys-admins");
            assertOptionValueNotPresent("groupsToLeave", "jira-sys-admins");

            // Cheat and try to remove the sys-admin group by url
            gotoPage(page.addXsrfToken("/secure/admin/user/EditUserGroups.jspa?name=root&groupsToLeave=jira-sys-admins&leave=true"));
            assertTextPresent("You can not remove a group from this user as it is not visible to you.");
        }
        finally
        {
            logout();
            // go back to sysadmin user
            login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            restoreBlankInstance();
        }
    }

    public void testFredEditGroups()
    {
        try
        {
            restoreData("TestWithSystemAdmin.xml");

            // Be the user that has no perms
            login(FRED_USERNAME, FRED_PASSWORD);

            // validate that I can't fudge the url to add myself to the sys admins
            gotoPage(page.addXsrfToken("/secure/admin/user/EditUserGroups.jspa?name=" + ADMIN_USERNAME + "&groupsToJoin=jira-sys-admins&join=true"));
            assertTextPresent("my login on this computer");

            // Cheat and try to remove the sys-admin group by url
            gotoPage(page.addXsrfToken("/secure/admin/user/EditUserGroups.jspa?name=root&groupsToLeave=jira-sys-admins&leave=true"));
            assertTextPresent("my login on this computer");
        }
        finally
        {
            logout();
            // go back to sysadmin user
            login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            restoreBlankInstance();
        }
    }

    public void testEditGroupsUserDoesNotExist()
    {
        restoreBlankInstance();

        gotoPage(page.addXsrfToken("/secure/admin/user/EditUserGroups.jspa?name=foo&groupsToJoin=jira-developers&join=true"));
        assertions.getJiraFormAssertions().assertFormErrMsg("This user does not exist please select a user from the user browser.");

        gotoPage(page.addXsrfToken("/secure/admin/user/EditUserGroups.jspa?name=foo&groupsToLeave=jira-developers&leave=true"));
        assertions.getJiraFormAssertions().assertFormErrMsg("This user does not exist please select a user from the user browser.");
    }

    public void testEditGroupsGroupDoesNotExist()
    {
        restoreBlankInstance();

        gotoPage(page.addXsrfToken("/secure/admin/user/EditUserGroups.jspa?name=admin&groupsToJoin=invalid&join=true"));
        assertions.getJiraFormAssertions().assertFormErrMsg("The group 'invalid' is not a valid group.");

        gotoPage(page.addXsrfToken("/secure/admin/user/EditUserGroups.jspa?name=admin&groupsToLeave=invalid&leave=true"));
        assertions.getJiraFormAssertions().assertFormErrMsg("The group 'invalid' is not a valid group.");
    }

    public void testEditGroupsCanNotJoinAlreadyAMemeber()
    {
        restoreBlankInstance();
        gotoPage(page.addXsrfToken("/secure/admin/user/EditUserGroups.jspa?name=admin&groupsToJoin=jira-administrators&join=true"));
        assertions.getJiraFormAssertions().assertFormErrMsg("Cannot add user 'admin', user is already a member of 'jira-administrators'");
    }

    public void testEditGroupsCanNotLeaveNotAMemeber()
    {
        restoreBlankInstance();
        gotoPage(page.addXsrfToken("/secure/admin/user/EditUserGroups.jspa?name=fred&groupsToLeave=jira-administrators&leave=true"));
        assertions.getJiraFormAssertions().assertFormErrMsg("Cannot remove user 'fred' from group 'jira-administrators' since user is not a member of 'jira-administrators'");
    }

    public void testEditGroupsMustSelectAGroup()
    {
        restoreBlankInstance();
        gotoPage(page.addXsrfToken("/secure/admin/user/EditUserGroups.jspa?name=fred"));
        // note: nothing is selected
        submit("join");
        assertTextPresent("You must select at least one group to join.");
        submit("leave");
        assertTextPresent("You must select at least one group to leave.");
    }
}
