package com.atlassian.jira.webtests.ztests.user;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.restclient.ProjectRole;
import com.atlassian.jira.testkit.client.restclient.ProjectRoleClient;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.meterware.httpunit.WebTable;
import org.junit.Ignore;
import org.xml.sax.SAXException;

import java.util.Map;

import static com.atlassian.jira.permission.ProjectPermissions.ADMINISTER_PROJECTS;

@WebTest ({ Category.FUNC_TEST, Category.PROJECTS, Category.ROLES, Category.USERS_AND_GROUPS })
public class TestEditUserProjectRoles extends JIRAWebTest
{
    private static final String ICON_DIRECT_MEMBER = "/images/icons/emoticons/user_16.gif";
    private static final String ICON_NOT_MEMBER = "/images/icons/emoticons/user_bw_16.gif";
    private static final String ICON_GROUP_MEMBER = "/images/icons/emoticons/group_16.gif";

    public TestEditUserProjectRoles(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestEditUserProjectRoles.xml");
    }

    public void testNoProjectAssociationsForViewPage()
    {
        gotoAdmin();
        clickLink("user_browser");
        clickLink("tester");
        clickLink("viewprojectroles_link");
        assertTextPresent("There are currently no project role associations for this user.");
    }

    @Ignore ("JRADEV-15506 fix this test - there is no longer one big table - there are multiple and they have the same class"
            + " will need to add some unique identifier to the tables on viewuserprojectroles.jsp")
    public void testAddAssociationToProjects() throws SAXException
    {
        gotoAdmin();
        // Assert no associations and then add some
        clickLink("user_browser");
        clickLink("john");
        clickLink("viewprojectroles_link");
        assertTextNotPresent("There are currently no project role associations for this user.");
        clickLinkWithText("Edit Project Roles");
        assertTextNotPresent("There are currently no project role associations for this user.");

        // Add an association to Chimps
        gotoPage("secure/admin/user/EditUserProjectRoles!refresh.jspa?projects_to_add=10010&name=john");
        assertCheckboxNotSelected("10010_10011");
        assertCheckboxNotSelected("10010_10010");
        assertCheckboxNotSelected("10010_10012");
        checkCheckbox("10010_10011", "on");
        checkCheckbox("10010_10010", "on");
        assertCheckboxSelected("10010_10011");
        assertCheckboxSelected("10010_10010");

        submit("Save");

        WebTable userProjectRolesTable = getDialog().getResponse().getTableWithID("projecttable");
        assertTrue(tableCellHasText(userProjectRolesTable, 2, 0, "Chimps"));
        assertTableCellHasImage(userProjectRolesTable, 2, 1, ICON_DIRECT_MEMBER);
        assertTableCellHasImage(userProjectRolesTable, 2, 2, ICON_DIRECT_MEMBER);
        assertTableCellHasImage(userProjectRolesTable, 2, 3, ICON_NOT_MEMBER);
        assertTrue(tableCellHasText(userProjectRolesTable, 3, 0, "Donkeys"));
        assertTableCellHasImage(userProjectRolesTable, 3, 1, ICON_DIRECT_MEMBER);
        assertTableCellHasImage(userProjectRolesTable, 3, 1, ICON_GROUP_MEMBER);
        assertTableCellHasImage(userProjectRolesTable, 3, 2, ICON_DIRECT_MEMBER);
        assertTableCellHasImage(userProjectRolesTable, 3, 3, ICON_DIRECT_MEMBER);
        assertTrue(tableCellHasText(userProjectRolesTable, 4, 0, PROJECT_HOMOSAP));
        assertTableCellHasImage(userProjectRolesTable, 4, 1, ICON_GROUP_MEMBER);
        assertTableCellHasImage(userProjectRolesTable, 4, 2, ICON_NOT_MEMBER);
        assertTableCellHasImage(userProjectRolesTable, 4, 3, ICON_NOT_MEMBER);

        // Now go to the project and see that the association is present in the Project Role Browser
        ProjectRoleClient prc = new ProjectRoleClient(environmentData);
        final ProjectRole projectRole = prc.get("HSP", "Administrators");
        assertEquals("admin", projectRole.actors.get(0).name);
    }

    /**
     * The import xml used in the setup has the following:
     * <ul>
     *  <li> Has a user with username 'user1' and fullname 'SameName'
     *  <li> Another user with username 'user2' and fullname 'SameName'
     *  <li> user 'user1' is already a member of Role 'Developers'
     * </ul>
     * This test will try to add user 'user2' to the 'Developers' Role.
     * <p>
     * Before release of 3.7, 'user2' could not be added to the 'Developers' role
     * because a user with the same fullname was already added.
     * @throws org.xml.sax.SAXException caused from table cell checks
     */
    @Ignore ("JRADEV-15506 fix this test - there is no longer one big table - there are multiple and they have the same class"
            + " will need to add some unique identifier to the tables on viewuserprojectroles.jsp")
    public void testAddAssociationToProjectThatHasAMemberWithTheSameFullname() throws SAXException
    {
        gotoAdmin();
        // Assert no associations and then add some
        clickLink("user_browser");
        clickLink("user2");
        clickLink("viewprojectroles_link");
        clickLinkWithText("Edit Project Roles");
        assertTextPresent("There are currently no project role associations for this user.");

        // Add an association to project Donkeys
        gotoPage("secure/admin/user/EditUserProjectRoles!refresh.jspa?projects_to_add=10020&name=user2");
        assertCheckboxNotSelected("10020_10011");
        assertCheckboxNotSelected("10020_10010");
        assertCheckboxNotSelected("10020_10012");
        checkCheckbox("10020_10011", "on");
        checkCheckbox("10020_10010", "on");
        assertCheckboxNotSelected("10020_10012");//dont select users still

        submit("Save");
        assertTextPresent("Donkeys");
        WebTable userProjectRolesTable = getDialog().getResponse().getTableWithID("projecttable");
        assertTrue(tableCellHasText(userProjectRolesTable, 2, 0, "Donkeys"));
        assertTableCellHasImage(userProjectRolesTable, 2, 1, ICON_DIRECT_MEMBER);
        assertTableCellHasImage(userProjectRolesTable, 2, 2, ICON_DIRECT_MEMBER);
        assertTableCellHasImage(userProjectRolesTable, 2, 3, ICON_NOT_MEMBER);
        assertTextNotPresent("There are currently no project role associations for this user.");

        // Now go to the project and see that the association is present in the Project Role Browser
        ProjectRoleClient prc = new ProjectRoleClient(environmentData);
        final Map dky = prc.get("DKY");
        assertEquals(3, dky.size());

        ProjectRole projectRole = prc.get("DKY", "Administrators");

        assertEquals(4, projectRole.actors.size());
        assertEquals("user1", projectRole.actors.get(3).name);
        assertEquals("user2", projectRole.actors.get(2).name);

        projectRole = prc.get("DKY", "Developers");

        assertEquals(3, projectRole.actors.size());
        assertEquals("user1", projectRole.actors.get(2).name);
        assertEquals("user2", projectRole.actors.get(1).name);

        projectRole = prc.get("DKY", "Users");

        assertEquals(2, projectRole.actors.size());
        assertEquals("user1", projectRole.actors.get(1).name);
        assertFalse("user2".equals(projectRole.actors.get(0).name));
    }

    @Ignore ("JRADEV-15506 fix this test - there is no longer one big table - there are multiple and they have the same class"
            + " will need to add some unique identifier to the tables on viewuserprojectroles.jsp")
    public void testRemoveAssociationFromProject()
    {
        gotoAdmin();
        clickLink("user_browser");
        clickLink(ADMIN_USERNAME);
        clickLink("viewprojectroles_link");
        clickLinkWithText("Edit Project Roles");

        // Assert that the two projects we expect to be there are present
        assertTextPresent(PROJECT_MONKEY);
        assertTextPresent(PROJECT_HOMOSAP);

        // remove the roles for monkey
        uncheckCheckbox("10001_10011");
        submit("Save");

        // Verify that only the homosap project is present
        assertTextPresent(PROJECT_HOMOSAP);
        assertTextNotPresent(PROJECT_MONKEY);

        // Make sure that only homosap shows on the edit page as well
        clickLinkWithText("Edit Project Roles");

        assertTextInTable("projecttable", PROJECT_HOMOSAP);
        assertTextNotInTable("projecttable", PROJECT_MONKEY);
    }

    public void testGlobalAdminCanRemoveAnyoneFromRole()
    {
        gotoAdmin();

        //First lets remove the various groups from the Admin role.
        removeGroupFromProjectRole("jira-administrators", "Donkeys", "Administrators");
        removeGroupFromProjectRole("jira-developers", "homosapien", "Administrators");

        //add the administer projects permission for the admin role.
        gotoPermissionSchemes();
        clickLinkWithText("Default Permission Scheme");
        clickLink("add_perm_" + ADMINISTER_PROJECTS.permissionKey());
        checkCheckbox("type", "projectrole");
        selectOption("projectrole", "Administrators");
        submit();


        //now lets try to remove the admin user from the admin role.  This should not produce an error
        //(namely You can not remove a user/group that will result in completely removing yourself from this role.)
        //See JRA-12528.
        clickLink("user_browser");
        gotoPage("/secure/admin/user/ViewUserProjectRoles!default.jspa?returnUrl=UserBrowser.jspa&name=admin");
        clickLinkWithText("Edit Project Roles");
        uncheckCheckbox("10000_10011");
        uncheckCheckbox("10001_10011");
        submit("Save");
        assertTextNotPresent("You can not remove a user/group that will result in completely removing yourself from this role.");
        assertTextPresent("View Project Roles for User");
    }

    public void testGroupAssociationPresentInEditProjectRolesForUser()
    {
        gotoAdmin();
        clickLink("user_browser");
        clickLink(ADMIN_USERNAME);
        clickLink("viewprojectroles_link");
        assertTextPresent("jira-developers");
    }

    public void testGroupAssociationPresentInViewProjectRolesForUser()
    {
        gotoAdmin();
        clickLink("user_browser");
        clickLink("barney");
        clickLink("viewprojectroles_link");
        assertTextPresent("jira-developers");
    }
}
