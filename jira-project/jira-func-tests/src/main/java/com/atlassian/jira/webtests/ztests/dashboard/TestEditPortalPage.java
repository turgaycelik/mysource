package com.atlassian.jira.webtests.ztests.dashboard;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.functest.framework.Dashboard;
import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.sharing.SharedEntityInfo;
import com.atlassian.jira.functest.framework.sharing.TestSharingPermissionUtils;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.util.List;

/**
 * Test the EditPortalPage action on professional and enterprise
 *
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.DASHBOARDS })
public class TestEditPortalPage extends FuncTestCase
{
    private static final SharedEntityInfo PAGE_FRED_PRIVATE = new SharedEntityInfo(10010L, "PrivateFredDashboard", "This is private to fred and should not be visible to anyone else.", true, TestSharingPermissionUtils.createPrivatePermissions());
    private static final SharedEntityInfo PAGE_FRED_PUBLIC = new SharedEntityInfo(10011L, "PublicFredDashboard", "This is a dashboard page that can be seen by everyone.", true, TestSharingPermissionUtils.createPublicPermissions());

    private static final SharedEntityInfo PAGE_EXISTS = new SharedEntityInfo(10012L, "Exists", null, true, TestSharingPermissionUtils.createPrivatePermissions());
    private static final SharedEntityInfo PAGE_ADMINNOTFAVOURITE = new SharedEntityInfo(10013L, "AdminNotFavourite", null, false, TestSharingPermissionUtils.createPublicPermissions());
    private static final SharedEntityInfo PAGE_ADMINFAVOURITE = new SharedEntityInfo(10014L, "AdminFavourite", null, true, TestSharingPermissionUtils.createPublicPermissions());

    private static final Long SYSTEM_DEFAULT_PAGE_ID = 10000L;

    private static final String FRED_USER_NAME = FRED_USERNAME;

    protected void setUpTest()
    {
        administration.restoreData("BaseProfessionalPortalPage.xml");
    }

    /**
     * Make sure the page handles not having a page passed to it.
     */
    public void testMissingPageId()
    {
        SharedEntityInfo page = new SharedEntityInfo(null, "testMissingPageId", null, true, null);
        _testCantEditPageWithFormError(page, "You must select a dashboard to edit.");
    }

    /**
     * Make sure you can't edit to something named the same thing under the same user.
     */
    public void testEditNameAlreadyExists()
    {
        SharedEntityInfo page = new SharedEntityInfo(PAGE_ADMINFAVOURITE.getId(), "Exists", null, true, null);
        _testPageNotEdited(page, PAGE_ADMINFAVOURITE, "Dashboard with same name already exists.");
    }

    /**
     * Make sure you can't save a page with a blank name.
     */
    public void testEditBlankName()
    {
        SharedEntityInfo page = new SharedEntityInfo(PAGE_EXISTS.getId(), "", null, true, null);
        _testPageNotEdited(page, PAGE_EXISTS, "You must specify a name to save the dashboard as.");
    }

    /**
     * Make sure you can't edit the system default dashboard page.
     */
    public void testEditSystemDefaultPage()
    {
        SharedEntityInfo page = new SharedEntityInfo(SYSTEM_DEFAULT_PAGE_ID, "testEditSystemDefaultPage", null, true, null);
        _testCantEditPageWithFormError(page, "You cannot edit the system dashboard.");
    }

    /**
     * Make sure you can't edit a page that you don't have permission to edit.
     */
    public void testNoPermissionToEdit()
    {
        navigation.login(FRED_USER_NAME);
        SharedEntityInfo page = new SharedEntityInfo(PAGE_EXISTS.getId(), "", null, true, null);
        // we dont distinguished between no page and page that is not yours. 
        _testCantEditPageWithFormError(page, "You must select a dashboard to edit.");
    }

    /**
     * Make sure you can't edit a page that is shared with you but you don't have permission is edit.
     */
    public void testNoPermissionToEditButPageIsShared()
    {
        navigation.login(FRED_USER_NAME);
        SharedEntityInfo page = new SharedEntityInfo(PAGE_ADMINFAVOURITE.getId(), "testNoPermissionToEditButPageIsShared", null, true, null);
        _testCantEditPageWithFormError(page, "You may only create, modify or delete dashboards that you own.");
    }

    /**
     * Make sure the page handles the case when a page does not exist.
     */
    public void testNoPageExists()
    {
        SharedEntityInfo page = new SharedEntityInfo(666L, "testNoPageExists", null, true, null);
        // we dont distinguished between no page and page that is not yours.
        _testCantEditPageWithFormError(page, "You must select a dashboard to edit.");
    }

    /**
     * Make sure the edit view reflect the initial state of the input page.
     */
    public void testEditViewReflectsData()
    {
        validateInitialState(PAGE_ADMINFAVOURITE);
        validateInitialState(PAGE_ADMINNOTFAVOURITE);
        navigation.login(FRED_USER_NAME);
        validateInitialState(PAGE_FRED_PRIVATE);
        validateInitialState(PAGE_FRED_PUBLIC);
    }

    /**
     * Make sure that the name and description can be changed. Also checks XSS problems do not exist.
     */
    public void testXSSNameAdnDescription()
    {
        SharedEntityInfo page = new SharedEntityInfo(PAGE_EXISTS.getId(), "<script>alert('IName')</script>", "<script>alert('IDescription')</script>", true, TestSharingPermissionUtils.createPrivatePermissions());
        _testPageEditedCorrectly(page, EasyList.build(page, PAGE_ADMINFAVOURITE));
    }

    /**
     * Make sure that the page can save with the same name as it began with.
     */
    public void testEditCanSaveItself()
    {
        SharedEntityInfo page = new SharedEntityInfo(PAGE_EXISTS);
        page.setDescription("A new description");
        _testPageEditedCorrectly(page, EasyList.build(page, PAGE_ADMINFAVOURITE));
    }

    /**
     * Make sure a page can be de-favourited.
     */
    public void testChangeFavouriteToFalse()
    {
        SharedEntityInfo page = new SharedEntityInfo(PAGE_ADMINFAVOURITE);
        page.setFavourite(false);
        _testPageEditedCorrectly(page, EasyList.build(PAGE_EXISTS));

        navigation.dashboard().navigateToMy();
        assertions.getDashboardAssertions().assertDashboardPages(EasyList.build(page, PAGE_ADMINNOTFAVOURITE, PAGE_EXISTS), Dashboard.Table.OWNED);
    }

    /**
     * Make sure that a page can be made favourite.
     */
    public void testChangeFavouriteToTrue()
    {
        SharedEntityInfo page = new SharedEntityInfo(PAGE_ADMINNOTFAVOURITE);
        page.setFavourite(true);
        _testPageEditedCorrectly(page, EasyList.build(PAGE_EXISTS, PAGE_ADMINFAVOURITE, page));

        navigation.dashboard().navigateToMy();
        assertions.getDashboardAssertions().assertDashboardPages(EasyList.build(PAGE_ADMINFAVOURITE, page, PAGE_EXISTS), Dashboard.Table.OWNED);
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
        validateInitialState(oldPage);
    }

    private void _testCantEditPageWithFormError(final SharedEntityInfo page, final String expectedText)
    {
        navigation.dashboard().editPage(page);
        assertions.getJiraFormAssertions().assertFormErrMsg(expectedText);
    }

    private void validateInitialState(final SharedEntityInfo page)
    {
        tester.gotoPage("secure/EditPortalPage!default.jspa?pageId=" + page.getId());
        tester.assertFormElementEquals("portalPageName", page.getName());
        tester.assertFormElementEquals("portalPageDescription", page.getDescription());
        tester.assertFormElementEquals("favourite", String.valueOf(page.isFavourite()));
        tester.assertFormElementEquals("pageId", String.valueOf(page.getId()));
    }

}
