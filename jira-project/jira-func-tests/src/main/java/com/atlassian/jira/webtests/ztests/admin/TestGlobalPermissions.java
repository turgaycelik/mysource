package com.atlassian.jira.webtests.ztests.admin;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.BROWSING })
public class TestGlobalPermissions extends FuncTestCase
{
    public void testCanRemoveAnyoneFromJiraUsers() throws Exception
    {
        administration.restoreData("TestGlobalPermssionsJRA13577.xml");
        gotoGlobalPermissions();

        tester.clickLink("global_permissions");
        tester.assertTextPresent("Global Permissions");
        tester.assertTextPresent("JIRA Permissions");

        tester.clickLink("del_USE_");
        tester.assertTextPresent("Delete Global Permission");
        assertions.text().assertTextSequence(tester.getDialog().getResponseText(),
                "Are you sure you want to delete",
                "Anyone",
                "group from the",
                "JIRA Users",
                "permission?");

        tester.submit("Delete");
        tester.assertTextPresent("Global Permissions");
        tester.assertTextPresent("JIRA Permissions");
        tester.assertLinkNotPresent("del_USE_");
    }

    public void testErrorOnSysAdminDelete()
    {
        administration.restoreBlankInstance();
        //shouldn't be able to delte admin group as there is only one admin group
        gotoGlobalPermissions();
        tester.assertTextPresent("JIRA System Administrators");
        tester.assertTextPresent("jira-administrators");
        tester.clickLink("del_SYSTEM_ADMIN_jira-administrators");
        tester.assertTextPresent("You cannot delete this permission. You are not a member of any of the other system administration permissions.");
    }

    public void testAddThenDeletePermission()
    {
        administration.restoreBlankInstance();
        //Check that you can add a permission then delete it.
        gotoGlobalPermissions();
        tester.assertTextPresent("Browse Users");
        tester.assertTextPresent("jira-developers");

        //Check for the delete link
        tester.assertLinkPresent("del_USER_PICKER_jira-developers");
        tester.selectOption("globalPermType", "Browse Users");
        tester.selectOption("groupName", "Anyone");
        tester.submit("Add");                                       //add the group
        tester.assertLinkPresent("del_USER_PICKER_");

        //Delete the group
        tester.clickLink("del_USER_PICKER_");
        tester.assertTextPresent("Delete Global Permission");
        tester.assertTextPresent("Are you sure you want to delete the");   //check for confirmation
        tester.assertTextPresent("Anyone");
        tester.assertTextPresent("group from the");
        tester.assertTextPresent("Browse Users");
        tester.assertTextPresent("permission?");
        tester.submit("Delete");

        // Make sure the delete link for the Anyone permission does not exist
        tester.assertLinkNotPresent("del_USER_PICKER_");
    }

    public void testAddNoPermission()
    {
        administration.restoreBlankInstance();
        //should be prompted to select a permission from the dropdown
        gotoGlobalPermissions();
        tester.submit("Add");
        tester.assertTextPresent("You must select a permission");

        navigation.gotoAdminSection("global_permissions");
        tester.assertTextNotPresent("You must select a permission");
        tester.selectOption("globalPermType", "Please select a permission");
        tester.submit("Add");
        tester.assertTextPresent("You must select a permission");
    }

    public void testNotAllowedToAddAnyoneToJiraUsers()
    {
        administration.restoreBlankInstance();
        gotoGlobalPermissions();
        tester.assertTextPresent("JIRA Administrators");
        tester.assertTextPresent("jira-administrators");
        assertCannotAddAnyoneToJiraUsers();
        assertCannotAddAnyoneToJiraAdministrators();
        assertCannotAddAnyoneToSystemAdministrators();
    }


    private void assertCannotAddAnyoneToJiraUsers()
    {
        tester.selectOption("globalPermType", "JIRA Users");
        tester.selectOption("groupName", "Anyone");
        tester.submit("Add");
        assertions.getJiraFormAssertions().assertFieldErrMsg("The group 'Anyone' is not allowed to be added to the permission");
    }

    /**
     *  JRA-26627 - no longer allow anyone to be added to the Administrators group
     *
     */
    private void assertCannotAddAnyoneToJiraAdministrators()
    {
        tester.selectOption("globalPermType", "JIRA Administrators");
        tester.selectOption("groupName", "Anyone");
        tester.submit("Add"); //add the group
        assertions.getJiraFormAssertions().assertFieldErrMsg("The group 'Anyone' is not allowed to be added to the permission");
    }

    private void assertCannotAddAnyoneToSystemAdministrators()
    {
        tester.selectOption("globalPermType", "JIRA System Administrators");
        tester.selectOption("groupName", "Anyone");
        tester.submit("Add"); //add the group
        assertions.getJiraFormAssertions().assertFieldErrMsg("The group 'Anyone' is not allowed to be added to the permission");
    }


    public void testSystemAdminNotVisibleToNonAdmins()
    {
        try
        {
            // restore data that has the admin user not as a sys admin
            administration.restoreData("TestWithSystemAdmin.xml");

            // Confirm that we are not able to see the sys admin stuff
            gotoGlobalPermissions();
            tester.assertTextNotPresent("<b>JIRA System Administrators</b>");

            // Try to add something we are not allowed to add
            final String addUrl = page.addXsrfToken("/secure/admin/jira/GlobalPermissions.jspa?action=add&globalPermType=SYSTEM_ADMIN&groupName=jira-users");
            tester.gotoPage(addUrl);
            tester.assertTextPresent("You can not add a group to a global permission you do not have permission to see.");
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login("root", "root");
            administration.restoreBlankInstance();
        }
    }

    public void testAdminCannotDeleteSysAdminGroups()
    {
        try
        {
            // restore data that has the admin user not as a sys admin
            administration.restoreData("TestWithSystemAdmin.xml");

            final String removeUrl = page.addXsrfToken("/secure/admin/jira/GlobalPermissions.jspa?globalPermType=SYSTEM_ADMIN&action=del&groupName=jira-sys-admins");
            tester.gotoPage(removeUrl);
            tester.assertTextPresent("Only system administrators can delete groups from the system administrator permission.");
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login("root", "root");
            administration.restoreBlankInstance();
        }
    }

    public void testFilterPermsHaveCorrectVisibility()
    {
        administration.restoreBlankInstance();
        gotoGlobalPermissions();

        tester.assertTextPresent("Create Shared Objects");
        tester.assertTextPresent("Manage Group Filter Subscriptions");
    }

    public void testRemoveGroupDoesntExist()
    {
        administration.restoreData("TestRemoveGroupDoesntExist.xml");
        navigation.gotoAdmin();

        //first check the group doesn't exist.
        tester.clickLink("group_browser");
        tester.assertTextNotPresent("Stuff");

        //now check it's present in the global permissions.
        tester.clickLink("global_permissions");
        tester.assertTextPresent("Stuff");

        //try to remove the group stuff.  It doesn't exist any longer.
        tester.clickLink("del_USE_Stuff");
        tester.assertTextPresent("Delete Global Permission");
        assertions.text().assertTextSequence(tester.getDialog().getResponseText(),
                "Are you sure you want to delete the",
                "Stuff",
                "group from the",
                "JIRA Users",
                "permission");
        tester.submit("Delete");

        tester.assertTextPresent("Global Permissions");
        tester.assertTextNotPresent("Stuff");

        //also try removing a group that's not a member of a permission which should throw an error.
        final String removeUrl = page.addXsrfToken("secure/admin/jira/GlobalPermissions.jspa?groupName=bad&globalPermType=SYSTEM_ADMIN&action=confirm");
        tester.gotoPage(removeUrl);
        assertions.getJiraFormAssertions().assertFormErrMsg("Group 'bad' cannot be removed from permission "
                + "'JIRA System Administrators' since it is not a member of this permission.");
    }

    private void gotoGlobalPermissions()
    {
        navigation.gotoAdminSection("global_permissions");
        tester.assertTextPresent("Global Permissions");
    }

}
