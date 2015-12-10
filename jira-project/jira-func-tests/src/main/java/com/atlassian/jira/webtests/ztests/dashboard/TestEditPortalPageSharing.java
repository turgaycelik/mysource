package com.atlassian.jira.webtests.ztests.dashboard;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.functest.framework.Dashboard;
import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.sharing.GroupTestSharingPermission;
import com.atlassian.jira.functest.framework.sharing.ProjectTestSharingPermission;
import com.atlassian.jira.functest.framework.sharing.SharedEntityInfo;
import com.atlassian.jira.functest.framework.sharing.SimpleTestSharingPermission;
import com.atlassian.jira.functest.framework.sharing.TestSharingPermission;
import com.atlassian.jira.functest.framework.sharing.TestSharingPermissionUtils;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.framework.util.json.TestJSONException;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Test the EditPortalPage action on professional and enterprise for sharing
 *
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.DASHBOARDS })
public class TestEditPortalPageSharing extends FuncTestCase
{
    private static final SharedEntityInfo PAGE_FRED_PRIVATE = new SharedEntityInfo(10010L, "PrivateFredDashboard", "This is private to fred and should not be visible to anyone else.", true, TestSharingPermissionUtils.createPrivatePermissions());
    private static final SharedEntityInfo PAGE_FRED_PUBLIC = new SharedEntityInfo(10011L, "PublicFredDashboard", "This is a dashboard page that can be seen by everyone.", true, TestSharingPermissionUtils.createPublicPermissions());

    private static final SharedEntityInfo PAGE_EXISTS = new SharedEntityInfo(10012L, "Exists", null, true, TestSharingPermissionUtils.createPrivatePermissions());
    private static final SharedEntityInfo PAGE_ADMINNOTFAVOURITE = new SharedEntityInfo(10013L, "AdminNotFavourite", null, false, TestSharingPermissionUtils.createPublicPermissions());
    private static final SharedEntityInfo PAGE_ADMINFAVOURITE = new SharedEntityInfo(10014L, "AdminFavourite", null, true, TestSharingPermissionUtils.createPublicPermissions());

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
    private static final String GROUP_JIRA_ADMINISTRATORS = "jira-administrators";

    protected void setUpTest()
    {
        administration.restoreData("BaseProfessionalPortalPage.xml");
    }

    /**
     * Make sure initial sharing state is correct. It should reflect the page being edited.
     */
    public void testSharingStateCorrect()
    {
        validatePermissions(PAGE_EXISTS);
        validatePermissions(PAGE_ADMINFAVOURITE);
        validatePermissions(PAGE_ADMINNOTFAVOURITE);

        navigation.login(USER_FRED);

        validatePermissions(PAGE_FRED_PUBLIC);
        validatePermissions(PAGE_FRED_PRIVATE);
    }

    /**
     * Make sure initial sharing state is correct. This case if for a complex share.
     */
    public void testSharingStateCorrectComplex()
    {
        navigation.login(USER_FRED);

        final Set<TestSharingPermission> permissions = new HashSet<TestSharingPermission>();
        permissions.add(new ProjectTestSharingPermission(PROJECT_HOMOSAPIEN_ID, USERS_ROLE_ID, PROJECT_HOMOSAPIEN_NAME, USERS_ROLE_NAME));
        permissions.add(new ProjectTestSharingPermission(PROJECT_HOMOSAPIEN_ID, PROJECT_HOMOSAPIEN_NAME));
        permissions.add(new GroupTestSharingPermission(GROUP_JIRA_USERS));

        SharedEntityInfo page = new SharedEntityInfo("testSharingStateCorrectComplex", null, true, permissions);
        final Dashboard dashboard = navigation.dashboard();

        dashboard.addPage(page, null);
        dashboard.navigateToFavourites();
        final Long id = dashboard.getDashboardPageId(page.getName(), new XPathLocator(tester, Dashboard.Table.FAVOURITE.toXPath()));
        page.setId(id);

        validatePermissions(page);
    }

    /**
     * Make sure we can edit with a global share in play
     */
    public void testEditWithGlobalShare()
    {
        SharedEntityInfo page = new SharedEntityInfo(PAGE_EXISTS);
        page.setSharingPermissions(TestSharingPermissionUtils.createPublicPermissions());
        _testPageEditedCorrectly(page, EasyList.build(page, PAGE_ADMINFAVOURITE));
    }

    /**
     * Make sure we can edit with a private share in play
     */
    public void testEditWithPrivateShare()
    {
        SharedEntityInfo page = new SharedEntityInfo(PAGE_ADMINFAVOURITE);
        page.setSharingPermissions(TestSharingPermissionUtils.createPrivatePermissions());
        _testPageEditedCorrectly(page, EasyList.build(PAGE_EXISTS, page));
    }

    /**
     * Make sure we can edit with a group share in play
     */
    public void testEditWithGroupShare()
    {
        SharedEntityInfo page = new SharedEntityInfo(PAGE_EXISTS);
        page.setSharingPermissions(Collections.singleton(new GroupTestSharingPermission(GROUP_JIRA_ADMINISTRATORS)));
        _testPageEditedCorrectly(page, EasyList.build(page, PAGE_ADMINFAVOURITE));
    }

    /**
     * Make sure we can edit with a single project share in play
     */
    public void testEditWithSingleProjectShare()
    {
        SharedEntityInfo page = new SharedEntityInfo(PAGE_EXISTS);
        page.setSharingPermissions(Collections.singleton(new ProjectTestSharingPermission(PROJECT_HOMOSAPIEN_ID, PROJECT_HOMOSAPIEN_NAME)));
        _testPageEditedCorrectly(page, EasyList.build(page, PAGE_ADMINFAVOURITE));
    }

    /**
     * Make sure we can edit with a project and role share in play
     */
    public void testEditWithProjectAndRoleShare()
    {
        navigation.login(USER_FRED);
        SharedEntityInfo page = new SharedEntityInfo(PAGE_FRED_PRIVATE);
        page.setSharingPermissions(Collections.singleton(new ProjectTestSharingPermission(PROJECT_HOMOSAPIEN_ID, USERS_ROLE_ID, PROJECT_HOMOSAPIEN_NAME, USERS_ROLE_NAME)));
        _testPageEditedCorrectly(page, EasyList.build(page, PAGE_FRED_PUBLIC));
    }

    /**
     * Make sure that we can edit with multiple shares.
     */
    public void testEditWithMultipleShares()
    {
        navigation.login(USER_FRED);

        final Set<TestSharingPermission> permissions = new HashSet<TestSharingPermission>();
        permissions.add(new ProjectTestSharingPermission(PROJECT_HOMOSAPIEN_ID, USERS_ROLE_ID, PROJECT_HOMOSAPIEN_NAME, USERS_ROLE_NAME));
        permissions.add(new ProjectTestSharingPermission(PROJECT_HOMOSAPIEN_ID, PROJECT_HOMOSAPIEN_NAME));
        permissions.add(new GroupTestSharingPermission(GROUP_JIRA_USERS));

        SharedEntityInfo page = new SharedEntityInfo(PAGE_FRED_PRIVATE);
        page.setSharingPermissions(permissions);
        _testPageEditedCorrectly(page, EasyList.build(page, PAGE_FRED_PUBLIC));
    }

    /**
     * Make sure a group that does not exist cant not be shared with
     */
    public void testEditWhereGroupDoesNotExist()
    {
        SharedEntityInfo page = new SharedEntityInfo(PAGE_EXISTS);
        page.setSharingPermissions(Collections.singleton(new GroupTestSharingPermission("thisGroupDoesNotExistInTheTestDataOrElseItWasVeryLucky")));
        _testPageNotEdited(page, PAGE_EXISTS, "Group: 'thisGroupDoesNotExistInTheTestDataOrElseItWasVeryLucky' does not exist.");
    }

    /**
     * Make sure a group that you are not a member of
     */
    public void testEditWhereYouAreNotAMemberOfGroup()
    {
        navigation.login(USER_FRED);
        SharedEntityInfo page = new SharedEntityInfo(PAGE_FRED_PRIVATE);
        page.setSharingPermissions(Collections.singleton(new GroupTestSharingPermission(GROUP_JIRA_DEVELOPERS)));
        _testPageNotEdited(page, PAGE_FRED_PRIVATE, "You do not have permission to share with Group: 'jira-developers'.");
    }

    /**
     * Make sure a group with an empty name
     */
    public void testEditWhereGroupNameIsEmpty()
    {
        SharedEntityInfo page = new SharedEntityInfo(PAGE_EXISTS);
        page.setSharingPermissions(Collections.singleton(new GroupTestSharingPermission("")));
        _testPageNotEdited(page, PAGE_EXISTS, "Group permission is not valid: Invalid group name ''.");
    }

    /**
     * Make sure you can't share with non existent project.
     */
    public void testCanEditProjectDoesNotExist()
    {
        navigation.login(USER_FRED);
        final SharedEntityInfo page = new SharedEntityInfo(PAGE_FRED_PRIVATE.getId(), "testCanEditProjectDoesNotExist", null, true, createProjectPermission(100, null));
        _testPageNotEdited(page, PAGE_FRED_PRIVATE, "Selected project does not exist.");
    }

    /**
     * Make sure you can't share with project you can't see.
     */
    public void testCanEditProjectCantSee()
    {
        navigation.login(USER_FRED);
        final SharedEntityInfo page = new SharedEntityInfo(PAGE_FRED_PRIVATE.getId(), "testCanEditProjectCantSee", null, true, createProjectPermission(PROJECT_HIDDENFROMFRED_ID, null));
        _testPageNotEdited(page, PAGE_FRED_PRIVATE, "You do not have permission to share with Project: 'hidden_from_fred'.");
    }

    /**
     * Make sure you can't share with non existent role..
     */
    public void testCanEditRoleDoesNotExist()
    {
        navigation.login(USER_FRED);
        final SharedEntityInfo page = new SharedEntityInfo(PAGE_FRED_PRIVATE.getId(), "testCanEditRoleDoesNotExist", null, true, createProjectPermission(PROJECT_HOMOSAPIEN_ID, null, 123, null));
        _testPageNotEdited(page, PAGE_FRED_PRIVATE, "Selected role does not exist.");
    }

    /**
     * Make sure you can't share with role you can't see.
     */
    public void testCanEditRoleCantSee()
    {
        navigation.login(USER_FRED);
        final SharedEntityInfo page = new SharedEntityInfo(PAGE_FRED_PRIVATE.getId(), "testCanEditRoleCantSee", null, true, createProjectPermission(PROJECT_HOMOSAPIEN_ID, null, ROLE_ADMIN_ID, null));
        _testPageNotEdited(page, PAGE_FRED_PRIVATE, "You do not have permission to share with Project: 'homosapien' Role: 'Administrators'.");
    }

    /**
     * Make sure it wont accept bad share types
     */
    public void testCantEditBadShareType()
    {
        final SharedEntityInfo page = new SharedEntityInfo(PAGE_EXISTS);
        page.setSharingPermissions(Collections.singleton(new SimpleTestSharingPermission("badType")));
        _testPageNotEdited(page, PAGE_EXISTS, "Share permission of type 'badType' is unknown.");
    }

    /**
     * Make sure it wont accept bad input of share parameters
     */
    public void testCantEditBadRoleNumber()
    {
        final SharedEntityInfo page = new SharedEntityInfo(PAGE_EXISTS);
        page.setSharingPermissions(Collections.singleton(new SimpleTestSharingPermission("project", String.valueOf(PROJECT_HOMOSAPIEN_ID), "abcroleid")));
        _testPageNotEdited(page, PAGE_EXISTS, "Project permission is not valid: Invalid role identifier 'abcroleid'.");
    }


    /**
     * Make sure a user cannot save shares without permission.
     */
    public void testUserCantSaveSharesWithoutPermission()
    {
        administration.removeGlobalPermission(PERMISSION_CREATE_SHARED, GROUP_SHARINGUSERS);

        final SharedEntityInfo page = new SharedEntityInfo(PAGE_EXISTS);
        page.setSharingPermissions(TestSharingPermissionUtils.createPublicPermissions());

        final SharedEntityInfo pageWithoutPermissions = new SharedEntityInfo(PAGE_EXISTS);
        pageWithoutPermissions.setSharingPermissions(null);
        _testPageNotEdited(page, pageWithoutPermissions, "You do not have permission to share. All shares are invalid.");
    }

    /**
     * Make sure that sharing is not available to people without permission. Also check they have permission to save
     * a page without sharing.
     */
    public void testUserCantSeeSharingOptionsWithoutPermission()
    {
        administration.removeGlobalPermission(PERMISSION_CREATE_SHARED, GROUP_SHARINGUSERS);

        SharedEntityInfo page = new SharedEntityInfo(PAGE_EXISTS);
        page.setName("testUserCantSeeSharingOptionsWithoutPermission");
        page.setDescription("Description of testUserCantSeeSharingOptionsWithoutPermission");
        gotoEditPage(page);


        tester.assertTextNotPresent("Shares:");
        XPathLocator locator = new XPathLocator(tester, "//div[@id = 'share_div']");
        assertNull(locator.getNode());

        _testPageEditedCorrectly(page, EasyList.build(page, PAGE_ADMINFAVOURITE));
    }

    /**
     * Make sure a user can still "remove" existing shares when they dont have permission anymore
     * to create any.  They can always fall back to a private share and still save in other words.
     */
    public void testEditWhenSharingPermissionhasBeenRemoved()
    {
        administration.removeGlobalPermission(PERMISSION_CREATE_SHARED, GROUP_SHARINGUSERS);

        SharedEntityInfo page = new SharedEntityInfo(PAGE_ADMINFAVOURITE);
        page.setName("testUserCantSeeSharingOptionsWithoutPermission");
        page.setDescription("Description of testUserCantSeeSharingOptionsWithoutPermission");
        page.setSharingPermissions(TestSharingPermissionUtils.createPrivatePermissions());

        _testPageEditedCorrectly(page, EasyList.build(PAGE_EXISTS, page));
    }

    /* =======================================*/

    private static Set<TestSharingPermission> createProjectPermission(final long projectId, final String projectName)
    {
        return Collections.<TestSharingPermission>singleton(new ProjectTestSharingPermission(projectId, projectName));
    }

    private static Set<TestSharingPermission> createProjectPermission(final long projectId, final String projectName, final long roleId, final String roleName)
    {
        return Collections.<TestSharingPermission>singleton(new ProjectTestSharingPermission(projectId, roleId, projectName, roleName));
    }


    private void validatePermissions(final SharedEntityInfo page)
    {
        gotoEditPage(page);
        assertEquals(page.getSharingPermissions(), parsePermissions());
    }


    private void _testPageEditedCorrectly(final SharedEntityInfo page, List expectedPageList)
    {
        navigation.dashboard().editPage(page);
        assertions.getDashboardAssertions().assertDashboardPages(expectedPageList, Dashboard.Table.FAVOURITE);

    }

    private void _testPageNotEdited(final SharedEntityInfo page, final String expectedText)
    {
        navigation.dashboard().editPage(page);

        assertions.getJiraFormAssertions().assertAuiFieldErrMsg(expectedText);
    }

    private void _testPageNotEdited(final SharedEntityInfo page, final SharedEntityInfo oldPage, final String expectedText)
    {
        _testPageNotEdited(page, expectedText);
        gotoEditPage(page);
        validateEditPageLooksLike(oldPage);
    }

    private void gotoEditPage(final SharedEntityInfo page)
    {
        tester.gotoPage("secure/EditPortalPage!default.jspa?pageId=" + page.getId());
    }

    private void validateEditPageLooksLike(final SharedEntityInfo page)
    {
        tester.assertFormElementEquals("portalPageName", page.getName());
        tester.assertFormElementEquals("portalPageDescription", page.getDescription());
        tester.assertFormElementEquals("favourite", String.valueOf(page.isFavourite()));
        assertEquals(page.getSharingPermissions(), parsePermissions());
    }

    /**
     * Return the current share permissions for the filter in the session.
     *
     * @return the permissions for the current filter.
     */

    private Set /*<TestSharingPermission>*/ parsePermissions()
    {
        final Locator xpath = new XPathLocator(tester, "//span[@id='shares_data']");
        final String value = xpath.getText();
        try
        {
            return TestSharingPermissionUtils.parsePermissions(value);
        }
        catch (TestJSONException e)
        {
            fail("Unable to parse shares: " + e.getMessage());
            return null;
        }
    }
}