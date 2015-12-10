package com.atlassian.jira.webtests.ztests.user.rename;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.restclient.ProjectRole;

import java.util.List;

/**
 * @since v6.0
 */
@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS, Category.RENAME_USER, Category.PROJECTS})
public class TestUserRenameOnProjectRoles extends FuncTestCase
{

    private static final String USERS_ROLE_ID = "10000";
    private static final String DEVELOPERS_ROLE_ID = "10001";
    private static final String ADMINISTRATORS_ROLE_ID = "10002";
    private static final String BATMAN_ROLE_ID = "10100";
    private static final String EDIT_ROLES_FOR_USER_URL = "secure/admin/user/EditUserProjectRoles!refresh.jspa?projects_to_add=10000&name=%s";
    private static final String VIEW_ROLES_FOR_USER_URL = "secure/admin/user/ViewUserProjectRoles!default.jspa?name=%s";
    private static final String ROLE_CHECKBOX_ID = "10000_%s";

    @Override
    protected void setUpTest()
    {
        administration.restoreData("user_rename_project_roles.xml");

        //    USER_KEY  LOWER_USER_NAME
        //    ID10101"	"cc"
        //    cc"		"cat"
        //    bb"		"betty"
        //    ID10001"	"bb"
        //    bw"		"bruce"
        //    ID10201"	"bw"
    }

    public void testJqlFunctionsFindRenamedProjectRoleMembers()
    {
        makeUserBatman("bw");
        renameUser("bw", "thedarkknight");
        renameUser("bruce", "bw");

        // Check recycled user does not inherit their forebear's roles
        navigation.login("bw", "bruce");
        navigation.gotoResource("../rest/api/2/search?jql=project%20in%20projectsWhereUserHasRole(\"Batman\")");
        tester.assertTextPresent("\"total\":0");

        // Check renamed user still keeps their roles
        navigation.login("thedarkknight", "bw");
        navigation.gotoResource("../rest/api/2/search?jql=project%20in%20projectsWhereUserHasRole(\"Batman\")");
        tester.assertTextPresent("\"total\":1");
    }

    public void testRenamedUserRetainsProjectRoles()
    {
        // Add bw to the batman role
        makeUserBatman("bw");
        assertUserIsBatmanEverywhere("bw", "Batman Wayne", true);
        renameUser("bw", "thedarkknight");
        assertUserIsBatmanEverywhere("thedarkknight", "Batman Wayne", true);
        retireUserFromBatmanRole("thedarkknight");
        assertUserIsBatmanEverywhere("thedarkknight", "Batman Wayne", false);

        // Re-rename renamed user and check their role is retained
        renameUser("bruce", "bw");
        makeUserBatman("bw");
        assertUserIsBatmanEverywhere("bw", "Bruce Wayne", true);
    }

    public void testRenamedUserRetainsGroupMembership()
    {
        // Set up a group for Batman
        administration.usersAndGroups().addGroup("super-heroes");
        administration.usersAndGroups().addUserToGroup("bw", "super-heroes");
        backdoor.projectRole().addActors("COW", "Batman", new String[]{"super-heroes"}, null);

        // Check Batman's group shows up on his roles page and the corresponding REST resource
        navigation.gotoPage(String.format(VIEW_ROLES_FOR_USER_URL, "bw"));
        tester.assertElementPresent("10000_10100_group");
        assertTrue(backdoor.usersAndGroups().isUserInGroup("bw", "super-heroes"));

        // Rename Batman and re-check
        renameUser("bw", "thedarkknight");
        navigation.gotoPage(String.format(VIEW_ROLES_FOR_USER_URL, "thedarkknight"));
        tester.assertElementPresent("10000_10100_group");
        assertTrue(backdoor.usersAndGroups().isUserInGroup("thedarkknight", "super-heroes"));

        // Check that a recycled user isn't part of the group
        renameUser("bruce", "bw");
        navigation.gotoPage(String.format(VIEW_ROLES_FOR_USER_URL, "bw"));
        tester.assertElementNotPresent("10000_10100_group");
        assertFalse(backdoor.usersAndGroups().isUserInGroup("bw", "super-heroes"));
    }

    public void testRenamedUserRemainsDefaultRoleUser()
    {
        navigation.gotoPage(String.format("secure/project/UserRoleActorAction.jspa?projectRoleId=%s", BATMAN_ROLE_ID));
        tester.setFormElement("userNames","bw");
        tester.submit("add");
        text.assertTextPresent(locator.id("watcher_link_bw"),"Batman Wayne");

        renameUser("bw", "thedarkknight");

        navigation.gotoPage(String.format("secure/project/UserRoleActorAction.jspa?projectRoleId=%s", BATMAN_ROLE_ID));
        text.assertTextPresent(locator.id("watcher_link_thedarkknight"), "Batman Wayne");
        tester.assertElementNotPresent("watcher_linke_bw");
    }

    private void makeUserBatman(String userName)
    {
        navigation.gotoPage(String.format(EDIT_ROLES_FOR_USER_URL, userName));
        tester.checkCheckbox(String.format(ROLE_CHECKBOX_ID, BATMAN_ROLE_ID));
        tester.submit("Save");
    }

    private void retireUserFromBatmanRole(String userName)
    {
        navigation.gotoPage(String.format(EDIT_ROLES_FOR_USER_URL, userName));
        tester.uncheckCheckbox(String.format(ROLE_CHECKBOX_ID, BATMAN_ROLE_ID));
        tester.submit("Save");
    }

    private void assertUserIsBatmanEverywhere(String userName, String expectedDisplayName, boolean shouldBeBatman)
    {
        // Edit roles for user page
        navigation.gotoPage(String.format(EDIT_ROLES_FOR_USER_URL, userName));
        if (shouldBeBatman)
        {
            tester.assertCheckboxSelected(String.format(ROLE_CHECKBOX_ID, BATMAN_ROLE_ID));
        }
        else
        {
            tester.assertCheckboxNotSelected(String.format(ROLE_CHECKBOX_ID, BATMAN_ROLE_ID));
        }

        // View roles for user page
        navigation.gotoPage(String.format(VIEW_ROLES_FOR_USER_URL, userName));
        if (shouldBeBatman)
        {
            assertTrue("Role not shown on view roles page", locator.id("10000_10100_direct").exists());
        }
        else
        {
            assertTrue("Role wrongly shown on view roles page", locator.id("10000_10100_none").exists());
        }

        // Check roles as reported by REST
        List<ProjectRole.Actor> restBatmen = getBatmenViaREST();
        if (shouldBeBatman)
        {
            assertEquals(expectedDisplayName, restBatmen.get(0).displayName);
        }
        else
        {
            assertEquals(restBatmen.size(),0);
        }
    }

    private List<ProjectRole.Actor> getBatmenViaREST()
    {
        return backdoor.projectRole().get("COW","Batman").actors;
    }

    private void renameUser(String from, String to)
    {
        navigation.gotoPage(String.format("secure/admin/user/EditUser!default.jspa?editName=%s", from));
        tester.setFormElement("username", to);
        tester.submit("Update");
    }
}
