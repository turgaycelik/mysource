package com.atlassian.jira.webtests.ztests.dashboard;

import com.atlassian.jira.functest.framework.Dashboard;
import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.sharing.GlobalTestSharingPermission;
import com.atlassian.jira.functest.framework.sharing.GroupTestSharingPermission;
import com.atlassian.jira.functest.framework.sharing.ProjectTestSharingPermission;
import com.atlassian.jira.functest.framework.sharing.SharedEntityInfo;
import com.atlassian.jira.functest.framework.sharing.SimpleTestSharingPermission;
import com.atlassian.jira.functest.framework.sharing.TestSharingPermission;
import com.atlassian.jira.functest.framework.sharing.TestSharingPermissionUtils;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.framework.util.json.TestJSONException;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Test the sharing functionality of the AddPortalPage.
 *
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.DASHBOARDS })
public class TestAddPortalPageSharing extends FuncTestCase
{
    private static final Long CLONE_BLANK = null;
    private static final int PROJECT_HIDDENFROMFRED_ID = 10010;

    private static final int PROJECT_HOMOSAPIEN_ID = 10000;
    private static final String PROJECT_HOMOSAPIEN_NAME = "homosapien";
    private static final int PERMISSION_CREATE_SHARED = 22;

    private static final int USERS_ROLE_ID = 10000;

    private static final int ROLE_ADMIN_ID = 10002;
    private static final String USERS_ROLE_NAME = "Users";
    private static final String GROUP_JIRA_USERS = "jira-users";

    private static final String GROUP_JIRA_DEVELOPERS = "jira-developers";
    private static final String GROUP_SHARINGUSERS = "sharing_users";
    private static final String USER_FRED = FRED_USERNAME;

    private static final SharedEntityInfo PAGE_FRED_PRIVATE = new SharedEntityInfo(10010L, "PrivateFredDashboard", null, true, TestSharingPermissionUtils.createPrivatePermissions());
    private static final SharedEntityInfo PAGE_FRED_PUBLIC = new SharedEntityInfo(10011L, "PublicFredDashboard", null, true, TestSharingPermissionUtils.createPublicPermissions());

    private static final List <SharedEntityInfo> ORIGINAL_PAGES = Arrays.asList(PAGE_FRED_PRIVATE, PAGE_FRED_PUBLIC);

    protected void setUpTest()
    {
        administration.restoreData("BaseProfessionalPortalPage.xml");
    }

    /**
     * Make sure sees public sharing by default when configured that way.
     */
    public void testPublicDefaultState()
    {
        navigation.userProfile().changeUserSharingType(true);

        tester.gotoPage("secure/AddPortalPage!default.jspa");

        assertEquals(TestSharingPermissionUtils.createPublicPermissions(), parseSharingPermissions());
    }

    /**
     * Make sure sees public sharing by default when configured that way.
     */
    public void testPrivateDefaultState()
    {
        navigation.userProfile().changeUserSharingType(false);

        tester.gotoPage("secure/AddPortalPage!default.jspa");

        assertEquals(TestSharingPermissionUtils.createPrivatePermissions(), parseSharingPermissions());
    }

    /**
     * Make sure user is able to keep page private.
     */
    public void testCreateDashboardPagePrivate()
    {
        final SharedEntityInfo page = new SharedEntityInfo("testCreateSharePrivate", null, true, TestSharingPermissionUtils.createPrivatePermissions());
        _testPageCreatedCorrectly(page);
    }

    /**
     * Make sure user is able share page publicly.
     */
    public void testCreateDashboardPagePublic()
    {
        final SharedEntityInfo page = new SharedEntityInfo("testCreateDashboardPagePublic", "Description for testCreateDashboardPagePublic", true, TestSharingPermissionUtils.createPublicPermissions());
        _testPageCreatedCorrectly(page);
    }

    /**
     * Make sure the user is able to share a page with a group.
     */
    public void testCreateDashboardPageGroup()
    {
        final SharedEntityInfo page = new SharedEntityInfo("testCreateDashboardGroup", "Description for testCreateDashboardGroup", true, createGroupPermission(GROUP_JIRA_USERS));
        _testPageCreatedCorrectly(page);
    }

    /**
     * Make sure we can save a project.
     */
    public void testCreateDashboardPageProject()
    {
        final SharedEntityInfo page = new SharedEntityInfo("testCreateDashboardGroup", "Description for testCreateDashboardGroup", true, createProjectPermission(PROJECT_HOMOSAPIEN_ID, PROJECT_HOMOSAPIEN_NAME));
        _testPageCreatedCorrectly(page);
    }

    /**
     * Make sure that we can saved a project and a role.
     */
    public void testCreateDashboardPageProjectAndRole()
    {
        final SharedEntityInfo page = new SharedEntityInfo("testCreateDashboardPageProjectAndRole", "Description for testCreateDashboardPageProjectAndRole", true, createProjectPermission(PROJECT_HOMOSAPIEN_ID, PROJECT_HOMOSAPIEN_NAME, USERS_ROLE_ID, USERS_ROLE_NAME));
        _testPageCreatedCorrectly(page);
    }

    /**
     * Make sure that we can save with multiple shares.
     */
    public void testCreateDashboardWithMultipleShares()
    {
        final Set<TestSharingPermission> permissions = new HashSet<TestSharingPermission>();
        permissions.add(new ProjectTestSharingPermission(PROJECT_HOMOSAPIEN_ID, USERS_ROLE_ID, PROJECT_HOMOSAPIEN_NAME, USERS_ROLE_NAME));
        permissions.add(new ProjectTestSharingPermission(PROJECT_HOMOSAPIEN_ID, PROJECT_HOMOSAPIEN_NAME));
        permissions.add(new GroupTestSharingPermission(GROUP_JIRA_USERS));

        final SharedEntityInfo page = new SharedEntityInfo("testCreateDashboardWithMultipleShares", "Description for testCreateDashboardWithMultipleShares", true, permissions);
        _testPageCreatedCorrectly(page);
    }

    /**
     * Make sure you can't share with a group that does not exist.
     */
    public void testCantCreateDashboardGroupDoesNotExist()
    {
        final SharedEntityInfo page = new SharedEntityInfo("testCantCreateDashboardGroupDoesNotExist", "Description for testCantCreateDashboardGroupDoesNotExist", true, createGroupPermission("thisGroupDoesNotExistInTheTestDataOrElseItWasVeryLucky"));
        _testPageNotCreated(page, "Group: 'thisGroupDoesNotExistInTheTestDataOrElseItWasVeryLucky' does not exist.");
    }

    /**
     * Make sure you can't share with a group you are not a member of.
     */
    public void testCantCreateDashboardGroupNotMember()
    {
        final SharedEntityInfo page = new SharedEntityInfo("testCantCreateDashboardGroupNotMember", "Description for testCantCreateDashboardGroupNotMember", true, createGroupPermission(GROUP_JIRA_DEVELOPERS));
        _testPageNotCreated(page, "You do not have permission to share with Group: 'jira-developers'.");
    }

    /**
     * Make sure you can't share with an invalid group.
     */
    public void testCantCreateDashboardGroupNameEmpty()
    {
        final SharedEntityInfo page = new SharedEntityInfo("testCantCreateDashboardGroupNameEmpty", "Description for testCantCreateDashboardGroupNameEmpty", true, createGroupPermission(null));
        _testPageNotCreated(page, "Group permission is not valid: Invalid group name ''.");
    }

    /**
     * Make sure you can't share with non existent project.
     */
    public void testCanCreateDashboardProjectDoesNotExist()
    {
        final SharedEntityInfo page = new SharedEntityInfo("testCanCreateDashboardProjectDoesNotExist", null, true, createProjectPermission(100, null));
        _testPageNotCreated(page, "Selected project does not exist.");
    }

    /**
     * Make sure you can't share with project you can't see.
     */
    public void testCanCreateDashboardProjectCantSee()
    {
        final SharedEntityInfo page = new SharedEntityInfo("testCanCreateDashboardProjectCantSee", null, true, createProjectPermission(PROJECT_HIDDENFROMFRED_ID, null));
        _testPageNotCreated(page, "You do not have permission to share with Project: 'hidden_from_fred'.");
    }

    /**
     * Make sure you can't share with non existent role..
     */
    public void testCanCreateDashboardRoleDoesNotExist()
    {
        final SharedEntityInfo page = new SharedEntityInfo("testCanCreateDashboardRoleDoesNotExist", null, true, createProjectPermission(PROJECT_HOMOSAPIEN_ID, null, 123, null));
        _testPageNotCreated(page, "Selected role does not exist.");
    }

    /**
     * Make sure you can't share with role you can't see.
     */
    public void testCanCreateDashboardRoleCantSee()
    {
        final SharedEntityInfo page = new SharedEntityInfo("testCanCreateDashboardRoleCantSee", null, true, createProjectPermission(PROJECT_HOMOSAPIEN_ID, null, ROLE_ADMIN_ID, null));
        _testPageNotCreated(page, "You do not have permission to share with Project: 'homosapien' Role: 'Administrators'.");
    }

    /**
     * Test a really messed up share.
     */
    public void testReallyBadShare()
    {
        final SharedEntityInfo page = new SharedEntityInfo("testReallyBadShare", null, true, Collections.singleton(new SimpleTestSharingPermission("badType")));
        _testPageNotCreated(page, "Share permission of type 'badType' is unknown.");
    }

    /**
     * Make sure that invalid permissions combinations are not working.
     */
    public void testReallyBadShareCombination()
    {
        final Set<TestSharingPermission> permissions = new HashSet<TestSharingPermission>();
        permissions.add(new ProjectTestSharingPermission(PROJECT_HOMOSAPIEN_ID, PROJECT_HOMOSAPIEN_NAME));
        permissions.add(new GlobalTestSharingPermission());

        final SharedEntityInfo page = new SharedEntityInfo("testCreateDashboardWithMultipleShares", "Description for testCreateDashboardWithMultipleShares", true, permissions);
        _testPageNotCreated(page, "Permission type 'global' must not be included with other permissions.");
    }

    /**
     * Make sure that sharing is not available to people without permission. Also check they have permission to save
     * a page without sharing.
     */
    public void testUserCantSeeSharingOptionsWithoutPermission()
    {
        administration.removeGlobalPermission(PERMISSION_CREATE_SHARED, GROUP_SHARINGUSERS);
        tester.gotoPage("secure/AddPortalPage!default.jspa");


        tester.assertTextNotPresent("Shares:");
        XPathLocator locator = new XPathLocator(tester, "//div[@id = 'share_div']");
        assertNull(locator.getNode());

        final SharedEntityInfo page = new SharedEntityInfo("testUserCantSeeSharingOptionsWithoutPermission", null, true, null);
        _testPageCreatedCorrectly(page);
    }

    /**
     * Make sure a user cannot save shares without permission.
     */
    public void testUserCantSaveSharesWithoutPermission()
    {
        administration.removeGlobalPermission(PERMISSION_CREATE_SHARED, GROUP_SHARINGUSERS);

        final SharedEntityInfo page = new SharedEntityInfo("testUserCantSaveSharesWithoutPermission", null, true, TestSharingPermissionUtils.createPublicPermissions());
        _testPageNotCreated(page, "You do not have permission to share. All shares are invalid.");
    }

    private void _testPageNotCreated(final SharedEntityInfo page, final String expectedErrorMessage)
    {
        loginAs(USER_FRED);
        navigation.dashboard().addPage(page, CLONE_BLANK);

        assertions.getJiraFormAssertions().assertAuiFieldErrMsg(expectedErrorMessage);

        navigation.dashboard().navigateToFavourites();
        assertions.getDashboardAssertions().assertDashboardPages(ORIGINAL_PAGES, Dashboard.Table.FAVOURITE);
    }

    private void _testPageCreatedCorrectly(final SharedEntityInfo page)
    {
        loginAs(USER_FRED);
        navigation.dashboard().addPage(page, CLONE_BLANK);
        navigation.dashboard().navigateToFavourites();
        assertions.getDashboardAssertions().assertDashboardPages(createExpectedList(page), Dashboard.Table.FAVOURITE);
    }

    private static Set<TestSharingPermission> createGroupPermission(final String group)
    {
        return Collections.<TestSharingPermission>singleton(new GroupTestSharingPermission(group));
    }

    private static Set<TestSharingPermission> createProjectPermission(final long projectId, final String projectName)
    {
        return Collections.<TestSharingPermission>singleton(new ProjectTestSharingPermission(projectId, projectName));
    }

    private static Set<TestSharingPermission> createProjectPermission(final long projectId, final String projectName, final long roleId, final String roleName)
    {
        return Collections.<TestSharingPermission>singleton(new ProjectTestSharingPermission(projectId, roleId, projectName, roleName));
    }

    private static List<SharedEntityInfo> createExpectedList(final SharedEntityInfo page)
    {
        return Arrays.asList(PAGE_FRED_PRIVATE, PAGE_FRED_PUBLIC, page);
    }

    private void loginAs(final String userName)
    {
        navigation.login(userName, userName);
    }

    private Set<TestSharingPermission> parseSharingPermissions()
    {
        final XPathLocator locator = new XPathLocator(tester, "//span[@id='shares_data']");

        try
        {
            return TestSharingPermissionUtils.parsePermissions(locator.getText());
        }
        catch (TestJSONException e)
        {
            throw new RuntimeException("Unable to parse JSON shares.", e);
        }
    }
}
