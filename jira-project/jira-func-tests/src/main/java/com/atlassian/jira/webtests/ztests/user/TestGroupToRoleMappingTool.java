package com.atlassian.jira.webtests.ztests.user;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.atlassian.jira.testkit.client.restclient.ProjectRole;
import com.atlassian.jira.testkit.client.restclient.ProjectRoleClient;
import com.meterware.httpunit.WebTable;
import org.xml.sax.SAXException;

import java.util.Map;

/**
 * 
 */
@WebTest ({ Category.FUNC_TEST, Category.ROLES, Category.USERS_AND_GROUPS })
public class TestGroupToRoleMappingTool extends JIRAWebTest
{
    private static final String DEFAULT_PERMISSION_SCHEME_DESC = "This is the default Permission Scheme. Any new projects that are created will be assigned this scheme";
    private static final String HOMOSAPIEN_PERMISSION_SCHEME_DESC = "This is the homo Permission Scheme. Any new projects that are created will be assigned this scheme";
    private static final String WARNING_USE_PERMISSION_DETAILS = "Note: The following group(s) are groups to which new JIRA users are automatically added. For ease of maintenance, it is recommended that you do not map these groups to project roles.";
    private static final String WARNING_USE_PERMISSION_PREVIEW = "Warning: The following group(s) are groups to which new JIRA users are automatically added. For ease of maintenance, it is recommended that you do not include these groups in the transformation.";
    private static final int USE_PERMISSION_CODE = 1;

    public TestGroupToRoleMappingTool(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestGroupToRoleMappingTool.xml");
    }

    public void testAssociatedUnassociatedSchemeSelect()
    {
        gotoPage("secure/admin/SchemePicker!default.jspa");

        // Check that all the schemes that should be there are and the ones that should not, are not
        assertTextPresent("Default Permission Scheme");
        assertTextPresent("Homosapien Permission Scheme");
        assertTextNotPresent("Unassociated Permission Scheme");

        gotoPage("secure/admin/SchemePicker!switch.jspa?typeOfSchemesToDisplay=associated&selectedSchemeType=NotificationScheme");

        // Check that all the schemes that should be there are and the ones that should not, are not for Notifications
        assertTextPresent("Default Notification Scheme");
        assertTextPresent("Homosapien Notification Scheme");
        assertTextNotPresent("Unassociated Notification Scheme");

        gotoPage("secure/admin/SchemePicker!switch.jspa?typeOfSchemesToDisplay=all&selectedSchemeType=PermissionScheme");

        // Check that all the Permission schemes are shown
        assertTextPresent("Default Permission Scheme");
        assertTextPresent("Homosapien Permission Scheme");
        assertTextPresent("Unassociated Permission Scheme");

        gotoPage("secure/admin/SchemePicker!switch.jspa?typeOfSchemesToDisplay=all&selectedSchemeType=NotificationScheme");

        // Check that all the Permission schemes are shown
        assertTextPresent("Default Notification Scheme");
        assertTextPresent("Homosapien Notification Scheme");
        assertTextPresent("Unassociated Notification Scheme");
    }

    public void testGroupToRoleMappingScreen()
    {
        gotoPage("secure/admin/SchemePicker!switch.jspa?typeOfSchemesToDisplay=all&selectedSchemeType=PermissionScheme");

        selectMultiOptionByValue("selectedSchemeIds", "0");
        selectMultiOptionByValue("selectedSchemeIds", "10000");
        selectMultiOptionByValue("selectedSchemeIds", "10010");

        submit("Map Groups to Roles");

        //Check that all 4 groups that are in the selected schemes above are present
        assertTextPresent("jira-administrators");
        assertTextPresent("jira-developers");
        assertTextPresent("jira-users");
        assertTextPresent("homosapien-users");

        //Check that the correct roles are selectable
        assertTextPresent("Administrators");
        assertTextPresent("Developers");
        assertTextPresent("Users");
        assertTextPresent("Do not map group");
    }

    public void testGroupToRoleMappingPreview() throws SAXException
    {
        gotoPage("secure/admin/SchemePicker!switch.jspa?typeOfSchemesToDisplay=all&selectedSchemeType=PermissionScheme");

        selectMultiOptionByValue("selectedSchemeIds", "0");
        selectMultiOptionByValue("selectedSchemeIds", "10000");
        selectMultiOptionByValue("selectedSchemeIds", "10010");

        submit("Map Groups to Roles");

        selectOption("homosapien-users_project_role", "Users");
        selectOption("jira-administrators_project_role", "Administrators");
        selectOption("jira-developers_project_role", "Developers");
        selectOption("jira-users_project_role", "Users");

        submit("Preview Mappings");

        //Check the correct number of schemes is shown
        assertTextPresent("3 scheme(s)");

        WebTable tableWithID = getDialog().getResponse().getTableWithID("group_to_role_mappings");
        String groupName = tableWithID.getCellAsText(0, 0).trim();
        String roleName = tableWithID.getCellAsText(0, 2).trim();
        assertEquals(groupName,"jira-administrators");
        assertEquals(roleName,"Administrators");

        groupName = tableWithID.getCellAsText(1, 0).trim();
        roleName = tableWithID.getCellAsText(1, 2).trim();
        assertEquals(groupName,"jira-developers");
        assertEquals(roleName,"Developers");

        groupName = tableWithID.getCellAsText(2, 0).trim();
        roleName = tableWithID.getCellAsText(2, 2).trim();
        assertTrue("homosapien-users".equals(groupName) || "jira-users".equals(groupName));
        assertEquals(roleName,"Users");

        groupName = tableWithID.getCellAsText(4, 0).trim();
        roleName = tableWithID.getCellAsText(4, 2).trim();
        assertTrue("homosapien-users".equals(groupName) || "jira-users".equals(groupName));
        assertEquals(roleName,"Users");

        //Check the correct number of projects are being persisted to
        assertTextPresent("3 project(s)");

        // Check all the correct users are being added for the 'Chimps' project
        WebTable projectTableWithID = getDialog().getResponse().getTableWithID("Chimps_summary");
        String adminUsers = projectTableWithID.getCellAsText(1, 1);
        String devUsers = projectTableWithID.getCellAsText(2, 1);
        String userUsers = projectTableWithID.getCellAsText(3, 1);

        assertTrue(adminUsers.indexOf(ADMIN_FULLNAME)!=-1);
        assertTrue(devUsers.indexOf(ADMIN_FULLNAME)!=-1);
        assertTrue(devUsers.indexOf("Barney Rubble")!=-1);
        assertTrue(userUsers.indexOf(ADMIN_FULLNAME)!=-1);
        assertTrue(userUsers.indexOf(FRED_FULLNAME)!=-1);
        assertTrue(userUsers.indexOf("Dino Flintstone")!=-1);

        // Check all the correct users are being added for the 'Monkey' project
        projectTableWithID = getDialog().getResponse().getTableWithID("monkey_summary");
        adminUsers = projectTableWithID.getCellAsText(1, 1);
        devUsers = projectTableWithID.getCellAsText(2, 1);
        userUsers = projectTableWithID.getCellAsText(3, 1);

        assertTrue(adminUsers.indexOf(ADMIN_FULLNAME)!=-1);
        assertTrue(devUsers.indexOf(ADMIN_FULLNAME)!=-1);
        assertTrue(devUsers.indexOf("Barney Rubble")!=-1);
        assertTrue(userUsers.indexOf(ADMIN_FULLNAME)!=-1);
        assertTrue(userUsers.indexOf(FRED_FULLNAME)!=-1);
        assertTrue(userUsers.indexOf("Dino Flintstone")!=-1);
        //make sure that Wilma isn't part of this (as she's only in the homosapien-users group which is not
        //part of the default permission scheme)
        assertTrue(userUsers.indexOf("Wilma Flintstone")==-1);

         // Check all the correct users are being added for the 'homosapien' project
        projectTableWithID = getDialog().getResponse().getTableWithID("homosapien_summary");
        adminUsers = projectTableWithID.getCellAsText(1, 1);
        devUsers = projectTableWithID.getCellAsText(2, 1);
        userUsers = projectTableWithID.getCellAsText(3, 1);

        assertTrue(adminUsers.indexOf(ADMIN_FULLNAME)!=-1);
        assertTrue(devUsers.indexOf(ADMIN_FULLNAME)!=-1);
        assertTrue(devUsers.indexOf("Barney Rubble")!=-1);
        //in this project we need to make sure that Fred Normal isn't part of the users, as he's in jira-users and
        //not in homosapien-users
        assertTrue(userUsers.indexOf(FRED_FULLNAME)==-1);
        assertTrue(userUsers.indexOf("Dino Flintstone")!=-1);
        assertTrue(userUsers.indexOf("Wilma Flintstone")!=-1);
        //we also don't want the administrator here, because he's also not part of homosapien-users
        assertTrue(userUsers.indexOf(ADMIN_FULLNAME)==-1);
    }

    public void testNoSelectedSchemes()
    {
        gotoPage("secure/admin/SchemePicker!default.jspa");
        submit("Map Groups to Roles");

        assertTextPresent("You need to select schemes to be able to perform a mapping.");
    }

    public void testNoGroupToRoleMappings()
    {
        gotoPage("secure/admin/SchemePicker!switch.jspa?typeOfSchemesToDisplay=all&selectedSchemeType=PermissionScheme");

        selectMultiOptionByValue("selectedSchemeIds", "0");

        submit("Map Groups to Roles");

        submit("Preview Mappings");

        assertTextPresent("You must select at least one group to role mapping to proceed with this wizard.");
        //check that the button is still there
        assertSubmitButtonPresent("Preview Mappings");
    }

    public void testSelectedNotificationSchemes()
    {
        gotoPage("secure/admin/SchemePicker!switch.jspa?typeOfSchemesToDisplay=all&selectedSchemeType=NotificationScheme");

        selectMultiOptionByValue("selectedSchemeIds", "10010");
        selectMultiOptionByValue("selectedSchemeIds", "10011");

        submit("Map Groups to Roles");

        assertTextPresent("There are no groups in the selected schemes.");
        assertSubmitButtonNotPresent("Preview Mappings");
    }

    public void testPersistMappingsPermissionSchemes() throws SAXException
    {
        gotoPage("secure/admin/SchemePicker!switch.jspa?typeOfSchemesToDisplay=all&selectedSchemeType=PermissionScheme");

        selectMultiOptionByValue("selectedSchemeIds", "0");
        selectMultiOptionByValue("selectedSchemeIds", "10000");
        selectMultiOptionByValue("selectedSchemeIds", "10010");

        submit("Map Groups to Roles");

        selectOption("homosapien-users_project_role", "Users");
        selectOption("jira-administrators_project_role", "Administrators");
        selectOption("jira-developers_project_role", "Developers");
        selectOption("jira-users_project_role", "Users");

        submit("Preview Mappings");

        submit("Save");

        assertTextPresent("3 scheme(s)");

        assertTextPresentBeforeText("Default Permission Scheme", "Backup of Default Permission Scheme");
        assertTextPresentBeforeText("Homosapien Permission Scheme", "Backup of Homosapien Permission Scheme");
        assertTextPresentBeforeText("Unassociated Permission Scheme", "Backup of Unassociated Permission Scheme");

        // Test that the link to the PurgeSchemeTool is present and works
        clickLink("delete_tool");
        assertTextPresent("Bulk Delete Schemes: Select Schemes");
        assertTextPresent("Backup of Unassociated Permission Scheme");

        //Need to check that the Permissions screen contains all the backed up schemes, and that the project
        //associations were changed.
        clickLink("permission_schemes");
        WebTable schemesTableWithID = getDialog().getResponse().getTableWithID("permission_schemes_table");
        assertTrue(tableCellHasText(schemesTableWithID, 1,0,"Backup of Default Permission Scheme"));
        assertTrue(tableCellHasText(schemesTableWithID, 1,0,DEFAULT_PERMISSION_SCHEME_DESC));
        assertFalse(tableCellHasText(schemesTableWithID, 1,1,"Chimp"));
        assertFalse(tableCellHasText(schemesTableWithID, 1,1,"monkey"));
        assertFalse(tableCellHasText(schemesTableWithID, 1,1,"homosapien"));

        assertTrue(tableCellHasText(schemesTableWithID, 2,0,"Backup of Homosapien Permission Scheme"));
        assertTrue(tableCellHasText(schemesTableWithID, 2,0, HOMOSAPIEN_PERMISSION_SCHEME_DESC));
        assertFalse(tableCellHasText(schemesTableWithID, 2,1,"Chimp"));
        assertFalse(tableCellHasText(schemesTableWithID, 2,1,"monkey"));
        assertFalse(tableCellHasText(schemesTableWithID, 2,1,"homosapien"));

        assertTrue(tableCellHasText(schemesTableWithID, 3,0,"Backup of Unassociated Permission Scheme"));
        assertFalse(tableCellHasText(schemesTableWithID, 3,1,"Chimp"));
        assertFalse(tableCellHasText(schemesTableWithID, 3,1,"monkey"));
        assertFalse(tableCellHasText(schemesTableWithID, 3,1,"homosapien"));

        assertTrue(tableCellHasText(schemesTableWithID, 4,0,"Default Permission Scheme"));
        assertTrue(tableCellHasText(schemesTableWithID, 4,0,DEFAULT_PERMISSION_SCHEME_DESC));
        assertTrue(tableCellHasText(schemesTableWithID, 4,1,"Chimp"));
        assertTrue(tableCellHasText(schemesTableWithID, 4,1,"monkey"));
        assertFalse(tableCellHasText(schemesTableWithID, 4,1,"homosapien"));

        assertTrue(tableCellHasText(schemesTableWithID, 5,0,"Homosapien Permission Scheme"));
        assertTrue(tableCellHasText(schemesTableWithID, 5,0,HOMOSAPIEN_PERMISSION_SCHEME_DESC));
        assertFalse(tableCellHasText(schemesTableWithID, 5,1,"Chimp"));
        assertFalse(tableCellHasText(schemesTableWithID, 5,1,"monkey"));
        assertTrue(tableCellHasText(schemesTableWithID, 5,1,"homosapien"));

        assertTrue(tableCellHasText(schemesTableWithID, 6,0,"Unassociated Permission Scheme"));
        assertFalse(tableCellHasText(schemesTableWithID, 6,1,"Chimp"));
        assertFalse(tableCellHasText(schemesTableWithID, 6,1,"monkey"));
        assertFalse(tableCellHasText(schemesTableWithID, 6,1,"homosapien"));

        // Check that the permission schemes weren't altered in the backup
        clickLink("0_edit");
        assertTextNotPresentInPermissionsTables("Administrators");
        assertTextNotPresentInPermissionsTables("Developers");

        clickLinkWithText("permission schemes");

        clickLinkWithText("Default Permission Scheme", 1);
        assertTextInTable("edit_project_permissions", "Project Role");
        assertTextInTable("edit_votersandwatchers_permissions", "unmapped-group");
        assertTextNotPresentInPermissionsTables("jira-administrators");
        assertTextNotPresentInPermissionsTables("jira-developers");
        assertTextNotPresentInPermissionsTables("jira-users");

        //Check that the correct users have been added to the individual projects
        ProjectRoleClient prc = new ProjectRoleClient(environmentData);
        Map roles = prc.get("CHM");
        assertEquals(3, roles.size());

        ProjectRole projectRole = prc.get("CHM", "Administrators");
        assertEquals(1, projectRole.actors.size());
        assertEquals("admin", projectRole.actors.get(0).name);

        projectRole = prc.get("CHM", "Developers");
        assertEquals(2, projectRole.actors.size());
        assertEquals("admin", projectRole.actors.get(0).name);
        assertEquals("barney", projectRole.actors.get(1).name);

        projectRole = prc.get("CHM", "Users");
        assertEquals(3, projectRole.actors.size());
        assertEquals("admin", projectRole.actors.get(0).name);
        assertEquals("dino", projectRole.actors.get(1).name);
        assertEquals("fred", projectRole.actors.get(2).name);

        roles = prc.get("MKY");
        assertEquals(3, roles.size());

        projectRole = prc.get("MKY", "Administrators");
        assertEquals(1, projectRole.actors.size());
        assertEquals("admin", projectRole.actors.get(0).name);

        projectRole = prc.get("MKY", "Developers");
        assertEquals(2, projectRole.actors.size());
        assertEquals("admin", projectRole.actors.get(0).name);
        assertEquals("barney", projectRole.actors.get(1).name);

        projectRole = prc.get("MKY", "Users");
        assertEquals(3, projectRole.actors.size());
        assertEquals("admin", projectRole.actors.get(0).name);
        assertEquals("dino", projectRole.actors.get(1).name);
        assertEquals("fred", projectRole.actors.get(2).name);

        roles = prc.get("HSP");
        assertEquals(3, roles.size());

        projectRole = prc.get("HSP", "Administrators");
        assertEquals(1, projectRole.actors.size());
        assertEquals("admin", projectRole.actors.get(0).name);

        projectRole = prc.get("HSP", "Developers");
        assertEquals(2, projectRole.actors.size());
        assertEquals("admin", projectRole.actors.get(0).name);
        assertEquals("barney", projectRole.actors.get(1).name);

        projectRole = prc.get("HSP", "Users");
        assertEquals(2, projectRole.actors.size());
        assertEquals("dino", projectRole.actors.get(0).name);
        assertEquals("wilma", projectRole.actors.get(1).name);

//
//        goToProject(PROJECT_MONKEY);
//        clickLinkWithText(TestProjectRoles.VIEW_PROJECT_ROLES);
//
//        projectRolesTable = getDialog().getResponse().getTableWithID("project_role_actors");
//        assertTrue(tableCellHasText(projectRolesTable, 1, 1, ADMIN_FULLNAME));
//        assertTrue(tableCellHasText(projectRolesTable, 2, 1, ADMIN_FULLNAME));
//        assertTrue(tableCellHasText(projectRolesTable, 2, 1, "Barney Rubble"));
//        assertTrue(tableCellHasText(projectRolesTable, 3, 1, ADMIN_FULLNAME));
//        assertTrue(tableCellHasText(projectRolesTable, 3, 1, FRED_FULLNAME));
//        assertTrue(tableCellHasText(projectRolesTable, 3, 1, "Dino Flintstone"));
//
//        goToProject(PROJECT_HOMOSAP);
//        clickLinkWithText(TestProjectRoles.VIEW_PROJECT_ROLES);
//
//        projectRolesTable = getDialog().getResponse().getTableWithID("project_role_actors");
//        assertTrue(tableCellHasText(projectRolesTable, 1, 1, ADMIN_FULLNAME));
//        assertTrue(tableCellHasText(projectRolesTable, 2, 1, ADMIN_FULLNAME));
//        assertTrue(tableCellHasText(projectRolesTable, 2, 1, "Barney Rubble"));
//        assertTrue(tableCellHasText(projectRolesTable, 3, 1, "Wilma Flintstone"));
//        assertTrue(tableCellHasText(projectRolesTable, 3, 1, "Dino Flintstone"));

    }

    private void assertTextNotPresentInPermissionsTables(String text)
    {
        assertTextNotInTable("edit_project_permissions", text);
        assertTextNotInTable("edit_issue_permissions", text);
        assertTextNotInTable("edit_attachments_permissions", text);
        assertTextNotInTable("edit_comments_permissions", text);
        assertTextNotInTable("edit_votersandwatchers_permissions", text);
        assertTextNotInTable("edit_timetracking_permissions", text);        
    }

    public void testPersistMappingsNotificationSchemes() throws SAXException
    {
        gotoPage("secure/admin/SchemePicker!switch.jspa?typeOfSchemesToDisplay=all&selectedSchemeType=NotificationScheme");

        selectMultiOptionByValue("selectedSchemeIds", "10000");

        submit("Map Groups to Roles");

        selectOption("jira-administrators_project_role", "Administrators");

        submit("Preview Mappings");

        submit("Save");

        assertTextPresent("1 scheme(s)");

        assertTextPresentBeforeText("Default Notification Scheme", "Backup of Default Notification Scheme");
        //Check that the untransformed schemes are not shown on the results page.
        assertTextNotPresent("Homosapien Notification Scheme");
        assertTextNotPresent("Unassociated Notification Scheme");

        // Test that the link to the scheme merge tool is present and works
        clickLink("merge_tool");
        assertTextPresent("Merge Schemes: Select Schemes");

        //Need to check that the Notification screen contains the backed up scheme, and that the project
        //associations were changed.
        clickLink("notification_schemes");
        WebTable schemesTableWithID = getDialog().getResponse().getTableWithID("notification_schemes");
        assertTrue(tableCellHasText(schemesTableWithID, 1,0,"Backup of Default Notification Scheme"));
        assertFalse(tableCellHasText(schemesTableWithID, 1,1,"Chimp"));
        assertFalse(tableCellHasText(schemesTableWithID, 1,1,"monkey"));

        assertTrue(tableCellHasText(schemesTableWithID, 2,0,"Default Notification Scheme"));
        assertTrue(tableCellHasText(schemesTableWithID, 2,1,"Chimp"));
        assertTrue(tableCellHasText(schemesTableWithID, 2,1,"monkey"));

        // Check that the notifcation scheme was created correctly
        clickLink("10020_edit");
        assertTextNotPresent("(jira-administrators)");
        assertTextPresent("(Administrators)");
    }

    public void testBackupOfBackupOfNamedCorrectly()
    {
        // We have some logic that determines if the name for the backup schemes the tools creates are valid
        // and this test checks it works

        // Map the default notification scheme so that there is a scheme called Backup of Default Notification Scheme
        gotoPage("secure/admin/SchemePicker!switch.jspa?typeOfSchemesToDisplay=all&selectedSchemeType=NotificationScheme");

        selectMultiOptionByValue("selectedSchemeIds", "10000");

        submit("Map Groups to Roles");

        selectOption("jira-administrators_project_role", "Administrators");

        submit("Preview Mappings");

        submit("Save");

        assertTextPresent("1 scheme(s)");

        clickLink("notification_schemes");
        assertTextPresent("Backup of Default Notification Scheme");

        // Now run the tool again
        gotoPage("secure/admin/SchemePicker!switch.jspa?typeOfSchemesToDisplay=all&selectedSchemeType=NotificationScheme");

        selectMultiOptionByValue("selectedSchemeIds", "10020");

        submit("Map Groups to Roles");

        selectOption("jira-developers_project_role", "Developers");

        submit("Preview Mappings");

        submit("Save");

        assertTextPresent("1 scheme(s)");

        clickLink("notification_schemes");
        assertTextPresent("Backup of Default Notification Scheme (1)");
    }

    /**
     * This is because the SchemeEntity of a Group of type Anyone is represented
     * by a null parameter on the SchemeEntity. Our code did not originally
     * handle this well. The UI should just NOT present the user with the
     * 'Anyone' group.
     */
    public void testMappingSchemeWithEntityGroupAnyone()
    {
        gotoPage("secure/admin/SchemePicker!switch.jspa?typeOfSchemesToDisplay=all&selectedSchemeType=PermissionScheme");

        selectMultiOptionByValue("selectedSchemeIds", "0");

        submit("Map Groups to Roles");

        // Make sure the correct groups to map are present
        selectOption("unmapped-group_project_role", "Users");
        selectOption("jira-administrators_project_role", "Administrators");
        selectOption("jira-developers_project_role", "Developers");
        selectOption("jira-users_project_role", "Users");

        // Make sure that the Anyone group is not shown on the mapping page
        assertTextNotPresent("Anyone");
    }

    /**
     * Check the Group to Role mapper UI when there is no selected schemes in session by going to the page directly
     */
    public void testGroupToRoleMapperWithNoSession()
    {
        //go directly to the group to role mapper page. there should be no selected schemes in the sesion
        gotoPage("secure/admin/SchemeGroupToRoleMapper!default.jspa?selectedSchemeType=PermissionScheme");
        //assert correct message appears
        assertTextPresentOnlyOnce("You do not have any selected schemes to map groups to roles. Please start the wizard again.");
        //and assert that the 'Preview Mappings' submit button is hidden
        assertSubmitButtonNotPresent("Preview Mappings");

        //repeat for the Notification scheme
        gotoPage("secure/admin/SchemeGroupToRoleMapper!default.jspa?selectedSchemeType=NotificationScheme");
        assertTextPresentOnlyOnce("You do not have any selected schemes to map groups to roles. Please start the wizard again.");
        assertSubmitButtonNotPresent("Preview Mappings");

        //check the doExecute() with no session
        gotoPage("secure/admin/SchemeGroupToRoleMapper.jspa");
        assertTextPresentOnlyOnce("You do not have any selected schemes to map groups to roles. Please start the wizard again.");
        assertSubmitButtonNotPresent("Preview Mappings");
    }

    /**
     * Check the Group to Role transformer UI when there is no 'selected schemes' or 'group to role mapping' in session
     * by going to the page directly
     */
    public void testGroupToRoleTransformerWithNoSession()
    {
        //go directly to the group to role mapper page. there should be no selected schemes in the sesion
        gotoPage("secure/admin/SchemeGroupToRoleTransformer!default.jspa?selectedSchemeType=PermissionScheme");
        //assert correct message appears
        assertTextPresentOnlyOnce("No schemes were selected to map groups to roles. Please start the wizard again.");
        //and assert that the 'Save' submit button is hidden
        assertSubmitButtonNotPresent("Save");

        //repeat for the Notification Scheme
        gotoPage("secure/admin/SchemeGroupToRoleTransformer!default.jspa?selectedSchemeType=NotificationScheme");
        assertTextPresentOnlyOnce("No schemes were selected to map groups to roles. Please start the wizard again.");
        assertSubmitButtonNotPresent("Save");

        //check the doExecute() with no session
        gotoPage("secure/admin/SchemeGroupToRoleTransformer.jspa");
        assertTextPresentOnlyOnce("No schemes were selected to map groups to roles. Please start the wizard again.");
        assertSubmitButtonNotPresent("Save");
    }

    public void testGroupToRoleTransformerSomeWithGlobalUsePermission() throws SAXException
    {
        gotoPage("secure/admin/SchemePicker!switch.jspa?typeOfSchemesToDisplay=all&selectedSchemeType=PermissionScheme");

        selectMultiOptionByValue("selectedSchemeIds", "0");

        submit("Map Groups to Roles");

        assertTextPresent(WARNING_USE_PERMISSION_DETAILS);

        WebTable noPermTable = getDialog().getResponse().getTableWithID("group_to_role_mappings_no_use_permission");
        
        assertTrue(tableCellHasText(noPermTable, 1, 0, "jira-administrators"));
        assertTrue(tableCellHasText(noPermTable, 2, 0, "jira-developers"));
        assertTrue(tableCellHasText(noPermTable, 3, 0, "unmapped-group"));

        WebTable hasPermTable = getDialog().getResponse().getTableWithID("group_to_role_mappings_use_permission");

        assertTrue(tableCellHasText(hasPermTable, 1, 0, "jira-users"));

        selectOption("jira-administrators_project_role", "Developers");
        selectOption("jira-developers_project_role", "Developers");
        selectOption("unmapped-group_project_role", "Developers");
        selectOption("jira-users_project_role", "Developers");

        submit("Preview Mappings");

        assertTextPresent("Map Groups to Project Roles: Preview Transformation for Schemes");

        assertTextSequence(new String[] {
                "jira-administrators",
                "jira-developers",
                "unmapped-group",
                WARNING_USE_PERMISSION_PREVIEW,
                "jira-users"
        } );

        submit("Save");
    }

    public void testGroupToRoleTransformerAllWithGlobalUsePermission() throws SAXException
    {
        grantGlobalPermission(USE_PERMISSION_CODE, "jira-developers");
        grantGlobalPermission(USE_PERMISSION_CODE, "unmapped-group");

        gotoPage("secure/admin/SchemePicker!switch.jspa?typeOfSchemesToDisplay=all&selectedSchemeType=PermissionScheme");

        selectMultiOptionByValue("selectedSchemeIds", "0");

        submit("Map Groups to Roles");

        assertTextPresent("Map Groups to Project Roles: Select Mappings");

        WebTable noPermTable = getDialog().getResponse().getTableWithID("group_to_role_mappings_no_use_permission");
        assertNotNull(noPermTable);

        WebTable hasPermTable = getDialog().getResponse().getTableWithID("group_to_role_mappings_use_permission");
        assertNotNull(hasPermTable);

        //make sure displayed table is below the warning note
        assertTextSequence(new String[] {
                WARNING_USE_PERMISSION_DETAILS,
                "jira-developers",
                "jira-users",
                "unmapped-group"
        } );

        selectOption("jira-administrators_project_role", "Developers");
        selectOption("jira-developers_project_role", "Developers");
        selectOption("jira-users_project_role", "Developers");
        selectOption("unmapped-group_project_role", "Developers");

        submit("Preview Mappings");

        assertTextPresent("Map Groups to Project Roles: Preview Transformation for Schemes");

        assertTextSequence(new String[] {
                WARNING_USE_PERMISSION_PREVIEW,
                "jira-developers",
                "jira-users",
                "unmapped-group"
        } );
    }

    public void testGroupToRoleTransformerNoneWithGlobalUsePermission() throws SAXException
    {
        gotoPage("secure/admin/SchemePicker!switch.jspa?typeOfSchemesToDisplay=all&selectedSchemeType=PermissionScheme");

        selectMultiOptionByValue("selectedSchemeIds", "10000");

        submit("Map Groups to Roles");

        assertTextPresent("Map Groups to Project Roles: Select Mappings");

        assertTextNotPresent(WARNING_USE_PERMISSION_DETAILS);

        WebTable noPermTable = getDialog().getResponse().getTableWithID("group_to_role_mappings_no_use_permission");

        assertTrue(tableCellHasText(noPermTable, 1, 0, "homosapien-users"));
        assertTrue(tableCellHasText(noPermTable, 2, 0, "jira-administrators"));
        assertTrue(tableCellHasText(noPermTable, 3, 0, "jira-developers"));

        WebTable hasPermTable = getDialog().getResponse().getTableWithID("group_to_role_mappings_use_permission");
        assertNull(hasPermTable);

        selectOption("homosapien-users_project_role", "Developers");
        selectOption("jira-administrators_project_role","Developers");
        selectOption("jira-developers_project_role", "Developers");

        submit("Preview Mappings");

        assertTextPresent("Map Groups to Project Roles: Preview Transformation for Schemes");

        assertTextSequence(new String[] {
                "homosapien-users",
                "jira-administrators",
                "jira-developers"
        } );

        assertTextNotPresent(WARNING_USE_PERMISSION_PREVIEW);

        submit("Save");
    }
}
