package com.atlassian.jira.webtests.ztests.dashboard;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

@WebTest ({ Category.FUNC_TEST, Category.DASHBOARDS })
public class TestDashboardDeleteConfirm extends FuncTestCase
{
    private static final int EXISTING_DASHBOARD_ID = 10010;
    private static final int NOT_EXISTING_DASHBOARD_ID = 666;

    protected void setUpTest()
    {
        administration.restoreData("TestDeleteConfirmDashboard.xml");
    }

    // specifically test the dashboards listed on the restore default dashboard page
    public void testRestoreDefaultDashboardPortalPagesFavouritedByOthers() throws Exception
    {
        navigation.dashboard().navigateToMy();
        tester.clickLink("restore_defaults");

        // initially, 2 dashboards are listed:
        // - 3rd Dashboard (not favourited by admin)
        // - Dashboard for Administrator (favourited by admin)
        Locator locator = new XPathLocator(tester, "//span[@id='othersFavouritedPortalPages']");
        text.assertTextSequence(locator, new String[] {"3rd Dashboard", "(2 Users)"});
        text.assertTextSequence(locator, new String[] { "Dashboard for " + ADMIN_FULLNAME, "(1 User)"});

        // add 3rd Dashboard as a favourite for admin - check to see that the user count doesn't change
        // unfavourite Dashboard for Administrator - check to see that the user count doesn't change
        navigation.dashboard().favouriteDashboard(10031);
        navigation.dashboard().unFavouriteDashboard(10010);
        navigation.dashboard().navigateToMy();
        tester.clickLink("restore_defaults");
        locator = new XPathLocator(tester, "//span[@id='othersFavouritedPortalPages']");
        text.assertTextSequence(locator, new String[] {"3rd Dashboard", "(2 Users)"});
        text.assertTextSequence(locator, new String[] { "Dashboard for " + ADMIN_FULLNAME, "(1 User)"});

        // favourite Dashboard for Administrator with another user - check that the count increases
        navigation.logout();
        navigation.login("user3");
        navigation.dashboard().favouriteDashboard(10010);
        navigation.logout();
        navigation.login(ADMIN_USERNAME);

        navigation.dashboard().navigateToMy();
        tester.clickLink("restore_defaults");
        locator = new XPathLocator(tester, "//span[@id='othersFavouritedPortalPages']");
        text.assertTextSequence(locator, new String[] {"3rd Dashboard", "(2 Users)"});
        text.assertTextSequence(locator, new String[] { "Dashboard for " + ADMIN_FULLNAME, "(2 Users)"});

        // delete favourited dashboards - check that no message is displayed
        navigation.dashboard().navigateToMy();
        tester.clickLink("delete_3");
        tester.submit("Delete");
        navigation.dashboard().navigateToMy();
        tester.clickLink("delete_1");
        tester.submit("Delete");

        navigation.dashboard().navigateToMy();
        tester.clickLink("restore_defaults");
        locator = new XPathLocator(tester, "//span[@id='othersFavouritedPortalPages']");
        assertNull(locator.getNode());
    }

    public void testDeleteConfirm()
    {
        navigation.dashboard().navigateToMy();
        tester.clickLink("restore_defaults");

        Locator locator = new XPathLocator(tester, "//span[@id='othersFavouritedPortalPages']");
        text.assertTextSequence(locator, new String[] {"3rd Dashboard", "(2 Users)"});
        text.assertTextSequence(locator, new String[] { "Dashboard for " + ADMIN_FULLNAME, "(1 User)"});

        navigation.dashboard().navigateToMy();

        tester.clickLink("delete_3");
        locator = new XPathLocator(tester, "//p[@id='otherFavouriteCount']");
        assertEquals("There is 1 other person who has added this dashboard as a favourite.", locator.getText().trim());
        tester.submit("Delete");

        navigation.dashboard().navigateToMy();
        tester.clickLink("restore_defaults");
        locator = new XPathLocator(tester, "//span[@id='othersFavouritedPortalPages']");
        text.assertTextSequence(locator, new String[] {"3rd Dashboard", "(2 Users)"});

        navigation.dashboard().navigateToMy();

        tester.clickLink("delete_0");

        locator = new XPathLocator(tester, "//p[@id='otherFavouriteCount']");
        assertNull(locator.getNode());
        tester.submit("Delete");

        navigation.dashboard().navigateToMy();
        tester.clickLink("restore_defaults");
        locator = new XPathLocator(tester, "//span[@id='othersFavouritedPortalPages']");
        text.assertTextSequence(locator, new String[] {"3rd Dashboard", "(2 Users)"});
        navigation.dashboard().navigateToMy();
        tester.clickLink("delete_1");

        locator = new XPathLocator(tester, "//p[@id='otherFavouriteCount']");
        assertNull(locator.getNode());
        tester.submit("Delete");

        navigation.dashboard().navigateToMy();
        tester.clickLink("restore_defaults");
        locator = new XPathLocator(tester, "//span[@id='othersFavouritedPortalPages']");
        text.assertTextSequence(locator, new String[] {"3rd Dashboard", "(2 Users)"});

        navigation.dashboard().navigateToMy();
        tester.clickLink("delete_0");
        locator = new XPathLocator(tester, "//p[@id='otherFavouriteCount']");
        assertEquals("There are 2 other people who have added this dashboard as a favourite.", locator.getText().trim());
        tester.submit("Delete");

        navigation.dashboard().navigateToMy();
        tester.clickLink("restore_defaults");
        locator = new XPathLocator(tester, "//span[@id='othersFavouritedPortalPages']");
        assertNull(locator.getNode());
    }

    public void testDeleteConfirmPlainPage()
    {
        shouldNotDeleteDashboardGivenCancelOnConfirmation();
        shouldDeleteDashboardGivenSubmitOnConfirmation();
        shouldDisplayErrorGivenNotExistingDashboard();
    }

    private void shouldNotDeleteDashboardGivenCancelOnConfirmation()
    {
        goToPlainDeleteDashboardConfirm(EXISTING_DASHBOARD_ID);
        assertDeleteConfirmNoError();
        cancelDeleteConfirmation();
        assertOnManageDashboards();
        assertDashboardExists(EXISTING_DASHBOARD_ID);
    }

    private void shouldDeleteDashboardGivenSubmitOnConfirmation()
    {
        goToPlainDeleteDashboardConfirm(EXISTING_DASHBOARD_ID);
        assertDeleteConfirmNoError();
        submitDeleteConfirmation();
        assertOnManageDashboards();
        assertDashboardDoesNotExist(EXISTING_DASHBOARD_ID);

    }

    private void shouldDisplayErrorGivenNotExistingDashboard()
    {
        goToPlainDeleteDashboardConfirm(NOT_EXISTING_DASHBOARD_ID);
        assertDeleteConfirmError("Dashboard does not exist.");
    }
    private void goToPlainDeleteDashboardConfirm(int dashboardId)
    {
        navigation.gotoPage("secure/DeletePortalPage!default.jspa?pageId=" + dashboardId);
    }

    private void assertDeleteConfirmNoError()
    {
        assertions.assertNodeHasText(new CssLocator(tester, ".aui-message.info"), "Confirm that you want to delete this Dashboard.");
        assertions.assertNodeByIdExists("delete-portal-page-submit");
        assertions.assertNodeByIdExists("delete-portal-page-cancel");
    }

    private void assertDeleteConfirmError(String errorMsg)
    {
        assertions.assertNodeHasText(new CssLocator(tester, ".aui-message.error"), errorMsg);
        assertions.assertNodeByIdDoesNotExist("delete-portal-page-submit");
        assertions.assertNodeByIdDoesNotExist("delete-portal-page-cancel");
    }

    private void cancelDeleteConfirmation()
    {
        tester.clickLink("delete-portal-page-cancel");
    }

    private void submitDeleteConfirmation()
    {
        tester.submit("Delete");
    }

    private void assertOnManageDashboards()
    {
        assertions.assertNodeHasText("//header//h1", "Manage Dashboards");
    }

    private void assertDashboardExists(int dashboardId)
    {
        assertions.assertNodeExists(dashboardRowLocator(dashboardId));
    }

    private void assertDashboardDoesNotExist(int dashboardId)
    {
        assertions.assertNodeDoesNotExist(dashboardRowLocator(dashboardId));
    }

    private String dashboardRowLocator(int dashboardId)
    {
        return String.format("//tr[@id='pp_%d']", dashboardId);
    }
}
