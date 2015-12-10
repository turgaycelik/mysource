package com.atlassian.jira.webtests.ztests.customfield;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.Permissions;

import static com.atlassian.jira.permission.ProjectPermissions.CREATE_ISSUES;
import static com.atlassian.jira.permission.ProjectPermissions.EDIT_ISSUES;
import static com.atlassian.jira.permission.ProjectPermissions.MOVE_ISSUES;

/**
 * Test for JRA-13808
 *
 */
@WebTest ({ Category.FUNC_TEST, Category.CUSTOM_FIELDS, Category.FIELDS, Category.PERMISSIONS })
public class TestCustomFieldsNoSearcherPermissions extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        administration.restoreData("TestCustomFieldsNoSearcherPermissions.xml");
    }

    /**
     * Tests to ensure that setting the searcher of a customfield that's used in a permission scheme or issue level
     * security scheme will throw an error.
     */
    public void testEditCustomFieldSetSearcherToNone()
    {
        navigation.gotoAdmin();
        tester.clickLink("view_custom_fields");
        tester.clickLink("edit_User picker");
        tester.setFormElement("name", "User picker");
        //set the searcher to None.
        tester.selectOption("searcher", "None");
        tester.submit("Update");

        //check that the update didn't succeed.
        tester.assertTextPresent("Search Template cannot be set to &#39;None&#39; because this custom field is used in the following Permission Scheme(s): Default Permission Scheme");
        tester.assertTextPresent("Search Template cannot be set to &#39;None&#39; because this custom field is used in the following Issue Level Security Scheme(s): TestScheme");

        navigation.gotoAdmin();
        tester.clickLink("view_custom_fields");
        tester.clickLink("edit_multigrouppicker");
        tester.setFormElement("name", "multigrouppicker");
        tester.selectOption("searcher", "None");
        tester.submit("Update");

        tester.assertTextPresent("Search Template cannot be set to &#39;None&#39; because this custom field is used in the following Permission Scheme(s): Default Permission Scheme");
        tester.assertTextPresent("Search Template cannot be set to &#39;None&#39; because this custom field is used in the following Issue Level Security Scheme(s): TestScheme");
    }

    /**
     * Tests to ensure that deleting a customfield that's used in a permission scheme or issue level
     * security scheme will throw an error.
     */
    public void testDeleteCustomField()
    {
        //try deleting the user picker field
        navigation.gotoAdmin();
        tester.clickLink("view_custom_fields");
        tester.clickLink("del_customfield_10000");
        tester.submit("Delete");
        tester.assertTextPresent("Custom field cannot be deleted because it is used in the following Permission Scheme(s): Default Permission Scheme");
        tester.assertTextPresent("Custom field cannot be deleted because it is used in the following Issue Level Security Scheme(s): TestScheme");

        //try deleting the group picker field.
        navigation.gotoAdmin();
        tester.clickLink("view_custom_fields");
        tester.clickLink("del_customfield_10001");
        tester.submit("Delete");
        tester.assertTextPresent("Custom field cannot be deleted because it is used in the following Permission Scheme(s): Default Permission Scheme");
        tester.assertTextPresent("Custom field cannot be deleted because it is used in the following Issue Level Security Scheme(s): TestScheme");
    }

    public void testAddCustomFieldWithoutSearcherToPermission()
    {
        // We shouldn't be able to use the "nosearchercf" Custom Field in a Permission scheme because it has no searcher.
        navigation.gotoAdmin();
        tester.clickLink("permission_schemes");
        tester.clickLinkWithText("Default Permission Scheme");
        tester.clickLink("add_perm_" + CREATE_ISSUES.permissionKey());
        tester.checkCheckbox("type", "userCF");
        tester.selectOption("userCF", "nosearchercf");
        tester.submit(" Add ");
        tester.assertTextPresent("Custom field &#39;nosearchercf&#39; is not indexed for searching - please add a Search Template to this Custom Field.");
    }

    public void testAddCustomFieldWithoutSearcherToIssueLevelPermission()
    {
        // We shouldn&#39;t be able to use the "nosearchercf" Custom Field in an Issue Level Permission because it has no searcher.
        navigation.gotoAdmin();
        tester.clickLink("security_schemes");
        tester.clickLinkWithText("Security Levels");
        tester.clickLink("add_TestLevel");
        tester.checkCheckbox("type", "userCF");
        tester.selectOption("userCF", "nosearchercf");
        tester.submit(" Add ");
        tester.assertTextPresent("Custom field &#39;nosearchercf&#39; is not indexed for searching - please add a Search Template to this Custom Field.");
    }

    /** Test that adding a searcher to the customfield, makes it possible for that customfield to be added to a permission. */
    public void testAddingSearcherToCustomField()
    {
        navigation.gotoAdmin();
        tester.clickLink("view_custom_fields");
        tester.clickLink("edit_nosearchercf");
        tester.selectOption("searcher", "User Picker & Group Searcher");
        tester.submit("Update");
        tester.clickLink("permission_schemes");
        tester.clickLink("0_edit");
        tester.clickLink("add_perm_" + MOVE_ISSUES.permissionKey());
        tester.checkCheckbox("type", "userCF");
        tester.selectOption("userCF", "nosearchercf");
        tester.submit(" Add ");
        tester.assertTextPresent("Default Permission Scheme");
        final String response = tester.getDialog().getResponseText();
        assertions.text().assertTextSequence(response, "Move Issues", "nosearchercf");
    }

    /** Test that we can remove a customfield, after we&#39;ve removed it from permission and issuelevelschemes */
    public void testRemovingCustomField()
    {
        //remove from permission scheme
        navigation.gotoAdmin();
        tester.clickLink("permission_schemes");
        tester.clickLink("0_edit");
        tester.clickLink("del_perm_" + EDIT_ISSUES.permissionKey() + "_customfield_10000");
        tester.submit("Delete");

        //remove from security scheme
        tester.clickLink("security_schemes");
        tester.clickLinkWithText("Security Levels");
        tester.clickLink("delGroup_customfield_10000_TestLevel");
        tester.submit("Delete");

        //delete the custom field
        tester.clickLink("view_custom_fields");
        tester.clickLink("del_customfield_10000");
        tester.submit("Delete");

        tester.assertTextPresent("Custom Fields");
        tester.assertTextNotPresent("User picker");
    }

    /**
     * Tests to ensure that deleting a customfield that&#39;s used in a permission scheme or issue level
     * security scheme will throw an error.
     */
    public void testDeleteIssueSecurityLevelFlushesCache()
    {
        //grant the admin user permission to set issue security and add the scheme for the HSP project
        administration.permissionSchemes().defaultScheme().grantPermissionToSingleUser(Permissions.SET_ISSUE_SECURITY, ADMIN_USERNAME);
        administration.project().associateIssueLevelSecurityScheme("homosapien", "TestScheme");

        //grant the admin user permission to the security level.
        navigation.gotoAdmin();
        tester.clickLink("security_schemes");
        tester.clickLinkWithText("Security Levels");
        tester.clickLink("add_TestLevel");
        tester.checkCheckbox("type", "user");
        tester.setFormElement("user", ADMIN_USERNAME);
        tester.submit(" Add ");

        //check that the level is present.
        navigation.issue().gotoIssue("HSP-1");
        tester.clickLink("edit-issue");
        tester.assertTextPresent("TestLevel");

        //add the admin to the user CF
        tester.setFormElement("customfield_10000", ADMIN_USERNAME);
        tester.submit("Update");


        //now lets remove the user -> admin security level.  The level should still be available due to the
        //user CF
        navigation.gotoAdmin();
        tester.clickLink("security_schemes");
        tester.clickLinkWithText("Security Levels");
        tester.clickLink("delGroup_admin_TestLevel");
        tester.submit("Delete");

        //check that the level is present.
        navigation.issue().gotoIssue("HSP-1");
        tester.clickLink("edit-issue");
        tester.assertTextPresent("TestLevel");

        //now lets delete the user CF.  Level should no longer be present in the issue afterwards. - except for a temporary fix to JRA-14323
        navigation.gotoAdmin();
        tester.clickLink("security_schemes");
        tester.clickLinkWithText("Security Levels");
        tester.clickLink("delGroup_customfield_10000_TestLevel");
        tester.submit("Delete");

        // TODO: Remove this with the proper fix to  JRA-14323.
        tester.clickLink("security_schemes");
        tester.clickLinkWithText("Security Levels");
        tester.clickLink("delGroup_customfield_10001_TestLevel");
        tester.submit("Delete");

        //check that the level is no longer present.
        navigation.issue().gotoIssue("HSP-1");
        tester.clickLink("edit-issue");
        tester.assertTextNotPresent("TestLevel");
    }

}
