package com.atlassian.jira.webtests.ztests.dashboard;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.meterware.httpunit.HttpUnitOptions;

/**
 * This Func test class tests the "Tab" that we show on the manage Dashborad screen.,
 * See JRA-15370.   
 *
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.DASHBOARDS })
public class TestManageDashboardChooseTab extends FuncTestCase
{
    protected void setUpTest()
    {
        administration.restoreData("TestManageDashboardChooseTab.xml");
        HttpUnitOptions.setScriptingEnabled(true);
    }

    protected void tearDownTest()
    {
        HttpUnitOptions.setScriptingEnabled(false);
    }

    public void testFavouritesTabIsShownOnNewSession() throws Exception
    {
        tester.gotoPage("secure/ConfigurePortalPages!default.jspa");
        // We don't have favourites, or our own Dashboards right now, so the system automatically creates a copy of the
        // System dashboard and makes it a favourite.
        // This means we should end up on the Favourites tab.
        assertFavouritesTabIsShowing();
    }

    public void testRememberMyTab() throws Exception
    {
        tester.gotoPage("secure/ConfigurePortalPages!default.jspa");
        // We don't have favourites, or our own Dashboards right now, so the system automatically creates a copy of the
        // System dashboard and makes it a favourite.
        // This means we should end up on the Favourites tab.
        assertFavouritesTabIsShowing();

        // Now Look at My tab
        navigation.clickLinkWithExactText("My");
        assertMyTabIsShowing();
        // If we go away and come back to Manage Dashboards, we should be back on My tab
        tester.clickLink("home_link");
        tester.gotoPage("secure/ConfigurePortalPages!default.jspa");
        assertMyTabIsShowing();
    }

    public void testRememberPopularTab() throws Exception
    {
        tester.gotoPage("secure/ConfigurePortalPages!default.jspa");
        // We don't have favourites, or our own Dashboards right now, so the system automatically creates a copy of the
        // System dashboard and makes it a favourite.
        // This means we should end up on the Favourites tab.
        assertFavouritesTabIsShowing();

        // Now Look at Popular tab
        tester.clickLinkWithText("Popular");
        assertTabIsShowing("Popular");
        // If we go away and come back to Manage Dashboards, we should be back on My tab
        tester.clickLink("home_link");
        tester.gotoPage("secure/ConfigurePortalPages!default.jspa");
        assertTabIsShowing("Popular");
    }

    public void testRememberSearchTab() throws Exception
    {
        tester.gotoPage("secure/ConfigurePortalPages!default.jspa");
        // We don't have favourites, or our own Dashboards right now, so the system automatically creates a copy of the
        // System dashboard and makes it a favourite.
        // This means we should end up on the Favourites tab.
        assertFavouritesTabIsShowing();

        // Now Look at Search tab
        navigation.dashboard().navigateToSearch();
        assertTabIsShowing("Search");
        // If we go away and come back to Manage Dashboards, we should be back on My tab
        tester.clickLink("home_link");
        tester.gotoPage("secure/ConfigurePortalPages!default.jspa");
        assertTabIsShowing("Search");
    }

    public void testRememberFavouritesTab() throws Exception
    {
        tester.gotoPage("secure/ConfigurePortalPages!default.jspa");
        // We don't have favourites, or our own Dashboards right now, so the system automatically creates a copy of the
        // System dashboard and makes it a favourite.
        // This means we should end up on the Favourites tab.
        assertFavouritesTabIsShowing();

        // Now Look at Popular tab
        tester.clickLinkWithText("Popular");
        assertTabIsShowing("Popular");
        // Now Look at Favourites tab
        tester.clickLinkWithText("Favourites");
        assertFavouritesTabIsShowing();
        // If we go away and come back to Manage Dashboards, we should be back on My tab
        tester.clickLink("home_link");
        tester.gotoPage("secure/ConfigurePortalPages!default.jspa");
        assertFavouritesTabIsShowing();
    }

    private void assertMyTabIsShowing()
    {
        assertTabIsShowing("My");
    }

    private void assertFavouritesTabIsShowing()
    {
        final Locator locator = new XPathLocator(tester, "//li[@class='active first']");
        text.assertTextPresent(locator, "Favourite");
    }

    private void assertTabIsShowing(final String tabName)
    {
        final Locator locator = new XPathLocator(tester, "//li[@class='active']");
        text.assertTextPresent(locator, tabName);
    }
}
