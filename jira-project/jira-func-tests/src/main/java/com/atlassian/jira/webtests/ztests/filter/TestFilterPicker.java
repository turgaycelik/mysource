package com.atlassian.jira.webtests.ztests.filter;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.parser.filter.FilterItem;
import com.atlassian.jira.functest.framework.parser.filter.FilterList;
import com.atlassian.jira.functest.framework.parser.filter.FilterParser;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.framework.util.dom.DomKit;
import com.google.common.collect.ImmutableList;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import java.util.List;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * Tests the FilterPicker web component.
 * At the moment the only usage of this is through configuration of certain Portlets that allow the user to
 * select a filter for the portlet configuration.
 *
 * {@see com.atlassian.jira.webtest.selenium.filters.TestFilterPicker}
 *
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.FILTERS })
public class TestFilterPicker extends FuncTestCase
{
    protected void setUpTest()
    {
        administration.restoreBlankInstance();
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
    }

    public void testNoSearchYet()
    {
        // no search yet
        navigation.filterPickerPopup().searchFilters();

        //search with no results
        navigation.filterPickerPopup().findFilters("zz");
        tester.assertTextPresent("Your search criteria did not match any filters.");
    }

    public void testTabStickinessNotStandard()
    {
        // since there are no filters in favourites we should go to search
        navigation.filterPickerPopup().goToDefault();
        assertTabState("Search", "Popular", "Favourite");

        // add a favourite filter so now we default to favourite
        navigation.manageFilters().goToDefault();
        backdoor.filters().createFilter("", "Vic 20", true);

        // now favourites should be default
        navigation.filterPickerPopup().goToDefault();
        assertTabState("Favourite", "Search", "Popular");


        navigation.filterPickerPopup().popularFilters();
        assertTabState("Popular", "Favourite", "Search");

        navigation.filterPickerPopup().searchFilters();
        assertTabState("Search", "Popular", "Favourite");

        // now check we can go to from each tab to every other tab
        assertTabNavigation("Search", "Favourite", "Popular");
        assertTabNavigation("Favourite", "Popular", "Search");
        assertTabNavigation("Popular", "Search", "Favourite");
        assertTabNavigation("Search", "Popular", "Favourite");
        assertTabNavigation("Popular", "Favourite", "Search");
        assertTabNavigation("Favourite", "Search", "Popular");
    }

    /**
     * Checks that the filters visible in the different tabs for different users matches expectations.
     */
    public void testFilterViews()
    {
        administration.restoreData("TestDeleteUserForFiltersAndSubscriptions.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        navigation.filterPickerPopup().goToDefault();

        final List<FilterItem> expectedFavourites = navigation.filterPickerPopup().sanitiseFavouriteFilterItems
                (
                        ImmutableList.of(TestFavouriteAndMyFilters.FILTER_10001, TestFavouriteAndMyFilters.FILTER_10020)
                );

        FilterList filters = parse.filter().parseFilterList(FilterParser.TableId.FAVOURITE_TABLE);
        List filterList = filters.getFilterItems();
        assertEquals(expectedFavourites, filterList);

        navigation.filterPickerPopup().popularFilters();
        List<FilterItem> expectedPopulars = navigation.filterPickerPopup().sanitiseSearchFilterItems
                (
                        ImmutableList.of
                                (
                                        TestFavouriteAndMyFilters.FILTER_10001, TestFavouriteAndMyFilters.FILTER_10020,
                                        TestFavouriteAndMyFilters.FILTER_10000, TestFavouriteAndMyFilters.FILTER_10010
                                )
                );

        filters = parse.filter().parseFilterList(FilterParser.TableId.POPULAR_TABLE);
        filterList = filters.getFilterItems();
        assertEquals(expectedPopulars, filterList);

        // order is important here. we should have ascending sort by name by default
        List<FilterItem> compareList = navigation.filterPickerPopup().sanitiseSearchFilterItems
                (
                        ImmutableList.of
                                (
                                        TestFavouriteAndMyFilters.FILTER_10000, TestFavouriteAndMyFilters.FILTER_10001,
                                        TestFavouriteAndMyFilters.FILTER_10020, TestFavouriteAndMyFilters.FILTER_10010
                                )
                );

        navigation.filterPickerPopup().allFilters();
        filters = parse.filter().parseFilterList(FilterParser.TableId.SEARCH_TABLE);
        filterList = filters.getFilterItems();
        assertEquals(compareList, filterList);
    }    

    /**
     * Asserts that we can navigate from the current tab to the other tab
     *
     * @param currentlyOnTab the tab we are on
     * @param navigateToTab  the tab we navigate to
     * @param notOnTab       the tab we are not on and not navigating to (needed)
     */
    private void assertTabNavigation(final String currentlyOnTab, final String navigateToTab, final String notOnTab)
    {
        assertTabState(currentlyOnTab, navigateToTab, notOnTab);
        tester.clickLinkWithText(navigateToTab);
        assertTabState(navigateToTab, currentlyOnTab, notOnTab);
    }

    /**
     * Filter picker shouldn't let you have side-effects on filters - it's just for picking a filter.
     */
    public void testNoOperationsColumnsPresent()
    {
        navigation.manageFilters().goToDefault();
        backdoor.filters().createFilter("", "metafilter", true);
        navigation.filterPickerPopup().goToDefault();

        tester.assertTextPresent("metafilter");
        tester.assertTextNotPresent("Operations");
        tester.assertTextNotPresent("Subscriptions");
        tester.assertTextNotPresent("Edit");
        tester.assertTextNotPresent("Delete");
        tester.assertTextNotPresent("Columns");
    }

    public void testAnonymousUser()
    {
        navigation.logout();
        navigation.filterPickerPopup().goToDefault();
        tester.assertTextPresent("emember my login on this computer");
        tester.assertTextPresent("sername");
        tester.assertTextPresent("assword");
        tester.assertTextPresent("Log In");
    }

    private void assertTabState(String onTab, String notOn1, String notOn2)
    {
        tester.assertTextPresent(onTab);
        tester.assertLinkNotPresentWithText(onTab);
        tester.assertLinkPresentWithText(notOn1);
        tester.assertLinkPresentWithText(notOn2);
        tester.assertTextNotPresent("My"); // this tab is not visible in filterpicker popup
    }

    public void testProjectTabNonEnterprise()
    {
        navigation.filterPickerPopup().projects();
        final String[] expectedNoCatRows = {
                "Project Key Project Lead",
                "homosapien project for homosapiens HSP " + ADMIN_FULLNAME + " (admin)",
                "monkey project for monkeys MKY " + ADMIN_FULLNAME + " (admin)"
        };
        assertTableRows(expectedNoCatRows, "//table[@id='nocat_projects']//tr");
        tester.assertTextNotPresent("Category");
    }

    public void testProjectTabCategories()
    {
        administration.restoreData("TestFilterPickerManyProjectsInCategories.xml");
        navigation.filterPickerPopup().projects();
        // first check that the order and basic content of the category tables is correct
        Locator pageLocator = new WebPageLocator(tester);
        text.assertTextSequence(pageLocator.getHTML(), new String[] {
                "Projects",
                "Category", ":", "sideview", "Project", "Key", "Project Lead",
                "Category", ":", "topdown", "Project", "Key", "Project Lead",
                "Project", "Key", "Project Lead" });

        final String[] expectedSideViewRows = {
                "Category : sideview",
                "Project Key Project Lead",
                "Dig Dug DIG " + ADMIN_FULLNAME + " (admin)",
                "Moon Patrol Jump your six-wheeled buggy over ditches & rocks and shoot stuff for points. MNP " + ADMIN_FULLNAME + " (admin)",
                "Space Invaders SPV " + ADMIN_FULLNAME + " (admin)"
        };
        assertTableRows(expectedSideViewRows, "//table[@id='cat_10011_projects']//tr");

        final String[] expectedTopDownRows = {
                "Category : topdown",
                "Project Key Project Lead",
                "Ladybug LDB " + ADMIN_FULLNAME + " (admin)",
                "Pacman Eat power pills. PAC " + ADMIN_FULLNAME + " (admin)"
        };
        assertTableRows(expectedTopDownRows, "//table[@id='cat_10010_projects']//tr");

        final String[] expectedNoCatRows = {
                "Project Key Project Lead",
                "Star Wars classic vector game STAR " + ADMIN_FULLNAME + " (admin)"
        };
        assertTableRows(expectedNoCatRows, "//table[@id='nocat_projects']//tr");

        XPathLocator cancelButtonLocator = new XPathLocator(tester, "//button[@onclick='window.close();']");
        assertEquals("Cancel", ((Text) cancelButtonLocator.getNode().getFirstChild()).getData());
    }

    public void testNoProjects()
    {
        administration.restoreData("EmptyJira.xml");
        navigation.filterPickerPopup().projects();
        tester.assertTextNotPresent("AMIGO");
        tester.assertTextNotPresent("Category");
        tester.assertTextPresent("There are no projects created.");
    }

    private void assertTableRows(final String[] expectedRows, final String xpathToTableRows)
    {
        final XPathLocator rows = new XPathLocator(tester, xpathToTableRows);
        final Node[] nodes = rows.getNodes();
        assertNodesAsText(expectedRows, nodes);
    }

    private void assertNodesAsText(final String[] strings, final Node[] trs)
    {
        for (int i = 0; i < strings.length; i++)
        {
            if (i >= trs.length)
            {
                fail("ran out of nodes to check at position " + i);
            }
            assertEquals(strings[i], DomKit.getCollapsedText(trs[i]));
        }
        if (trs.length > strings.length)
        {
            fail("leftover nodes");
        }
    }

}
