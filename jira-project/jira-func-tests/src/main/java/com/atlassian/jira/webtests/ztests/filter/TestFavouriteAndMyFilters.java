package com.atlassian.jira.webtests.ztests.filter;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.admin.user.DeleteUserPage;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.parser.filter.FilterItem;
import com.atlassian.jira.functest.framework.parser.filter.FilterList;
import com.atlassian.jira.functest.framework.parser.filter.FilterParser;
import com.atlassian.jira.functest.framework.parser.filter.WebTestSharePermission;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Test the Favourites and My view of the ManageFilter pages.
 */
@WebTest ({ Category.FUNC_TEST, Category.FILTERS })
public class TestFavouriteAndMyFilters extends FuncTestCase
{
    public static final String PAGE_NAVIGATION = ".aui-page-panel-nav > .vertical.tabs";

    private static final List NO_OPERATIONS = emptyList();
    private static final List<WebTestSharePermission> GLOBAL_SHARE =
            ImmutableList.of(new WebTestSharePermission(WebTestSharePermission.GLOBAL_TYPE, null, null));
    private static final List<WebTestSharePermission> GROUP_DEVELOPERS_SHARE =
            ImmutableList.of(new WebTestSharePermission(WebTestSharePermission.GROUP_TYPE, "jira-developers", null));

    private static final String JOE = "joe";
    private static final String ADMIN_LONG = "admin (admin)";
    private static final String JOE_LONG = "joe (joe)";
    private static final String NOBODY = "nobody";
    private static final String NOBODY_LONG = "nobody (nobody)";

    private static final List<String> OPERATIONS = ImmutableList.of("Edit", "Delete");

    static final FilterItem FILTER_10000 = new FilterItem(10000L, "All", "", ADMIN_LONG, Collections.<WebTestSharePermission>emptyList(), false, 1L, OPERATIONS, 0L);
    static final FilterItem FILTER_10001 = new FilterItem(10001L, "All My", "yadayada", JOE_LONG, GLOBAL_SHARE, true, 2L, OPERATIONS, 3L);
    static final FilterItem FILTER_10010 = new FilterItem(10010L, "Nick", "", ADMIN_LONG, GLOBAL_SHARE, false, 4L, OPERATIONS, 0L);
    static final FilterItem FILTER_10020 = new FilterItem(10020L, "New Features", "", ADMIN_LONG, GROUP_DEVELOPERS_SHARE, true, 0L, OPERATIONS, 1L);
    static final FilterItem FILTER_10030 = new FilterItem(10030L, "Nobody's All", "Yadyayda", NOBODY_LONG, Collections.<WebTestSharePermission>emptyList(), true, 0L, OPERATIONS, 1L);

    private static final int SHARE_FILTERS = 22;

    protected void setUpTest()
    {
        administration.restoreData("TestDeleteUserForFiltersAndSubscriptions.xml");
    }

    public void testStickyTabs()
    {
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        tester.gotoPage("secure/ManageFilters.jspa");
        assertions.getLinkAssertions().assertLinkNotPresentWithExactText("//ul[@id='filter_type_table']/li", "Favourite");

        navigation.manageFilters().myFilters();
        tester.assertTextPresent("My");
        assertions.getLinkAssertions().assertLinkNotPresentWithExactText("//ul[@id='filter_type_table']/li", "My");
        tester.gotoPage("secure/ManageFilters.jspa");
        assertions.getLinkAssertions().assertLinkNotPresentWithExactText("//ul[@id='filter_type_table']/li", "My");

        navigation.manageFilters().goToDefault();
        tester.assertTextPresent("Favourite");
        assertions.getLinkAssertions().assertLinkNotPresentWithExactText("//ul[@id='filter_type_table']/li", "Favourite");
        tester.gotoPage("secure/ManageFilters.jspa");
        assertions.getLinkAssertions().assertLinkNotPresentWithExactText("//ul[@id='filter_type_table']/li", "Favourite");
    }

    /**
     * Test to make sure the correct operations are available.
     */
    public void testOperations()
    {
        _testInitAnonymousScreen();
        _testInitNobodyScreen();
        _testInitJoeScreen();
        _testInitAdminScreen();

        administration.removeGlobalPermission(SHARE_FILTERS, "jira-users");

        _testInitAnonymousScreen();
        _testInitAdminScreen();
        _testInitNobodyScreen();

        navigation.login(JOE, JOE);
        navigation.manageFilters().goToDefault();

        assertNavigatorTabsPresent();

        final FilterList filters = parse.filter().parseFilterList(FilterParser.TableId.FAVOURITE_TABLE);
        final List filterList = filters.getFilterItems();

        final FilterItem filterItem = noFavCount(makeMine(setSubCount(setFavCount(noOps(FILTER_10001), 3), 2)));
        filterItem.setOperations(ImmutableList.of("Edit", "Delete"));

        final List<FilterItem> compareList = ImmutableList.of(filterItem);
        assertEquals(compareList, filterList);
    }

    /**
     * Make sure that deleting user's behaves correctly with favourites.
     */
    public void testDeleteUser()
    {
        // test admin init screen
        _testInitAnonymousScreen();
        _testInitAdminScreen();
        _testInitJoeScreen();
        _testInitNobodyScreen();

        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        _testDeleteConfirm(ADMIN_USERNAME, 1, 2, 0);
        _testDeleteConfirm(JOE, 1, 1, 1);
        _testDeleteConfirm(NOBODY, 1, 0, 0);

        tester.submit("Delete");

        _testJoeScreenAfterNobodyDelete();
        navigation.manageFilters().addFavourite(10010);
        _testAdminScreenAfterNobodyDelete();
        _testAnonymousScreenAfterNobodyDelete();

        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        navigation.manageFilters().removeFavourite(10001);
        navigation.manageFilters().allFilters();

        assertNavigatorTabsPresent();

        final FilterList filters = parse.filter().parseFilterList(FilterParser.TableId.SEARCH_TABLE);

        final List<FilterItem> compareList =
                ImmutableList.of(
                        makeMine(setSubCount(setFavCount(noOps(makeNotFav(FILTER_10000)), 0), 1)),
                        setSubCount(setFavCount(noOps(makeNotFav(FILTER_10001)), 1), 1),
                        makeMine(setSubCount(setFavCount(noOps(FILTER_10020), 1), 0)),
                        makeMine(setSubCount(setFavCount(noOps(FILTER_10010), 1), 4))
                );

        assertEquals(compareList, filters.getFilterItems());

        _testDeleteConfirm(JOE, 5, 1, 0);
        tester.submit("Delete");

        _testAdminScreenAfterJoeDelete();
        _testAnonymousScreenAfterJoeDelete();
    }

    /**
     * Make sure that deleting filters works correctly.
     */
    public void testDeleteFilter()
    {
        // test admin init screen
        _testInitAnonymousScreen();
        _testInitAdminScreen();
        _testInitJoeScreen();
        _testInitNobodyScreen();

        //anonymous should not be able to delete the filter.
        _testDeleteFilterPermission(10020);

        //nobody should not be able to delete the filter.
        navigation.login(NOBODY, NOBODY);
        _testDeleteFilterPermission(10020);

        //make sure that admin can delete the filter.
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        _testDeleteFilterConfirm(10020, 0, 0);

        //make sure that JOE can delete his filter.
        navigation.login(JOE, JOE);
        _testDeleteFilterConfirm(10001, 2, 2);

        navigation.manageFilters().goToDefault();
        assertTrue(parse.filter().parseFilterList(FilterParser.TableId.FAVOURITE_TABLE).isEmpty());
        navigation.manageFilters().myFilters();
        assertNavigatorTabsPresent();
        assertTrue(parse.filter().parseFilterList(FilterParser.TableId.OWNED_TABLE).isEmpty());

        //make sure that joe cannot delete this filter.
        _testDeleteFilterPermission(10000);

        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);

        //make sure that the favourited filter no longer exists once it is deleted by JOE.
        navigation.manageFilters().goToDefault();

        assertNavigatorTabsPresent();

        text.assertTextNotPresent(locator.page(), "All My");

        navigation.logout();
        navigation.manageFilters().goToDefault();

        //anonymous should only have one filter left.
        checkAnonymousScreen(Collections.singletonList(noOps(setSubCount(setIssueCount(FILTER_10010, 0), 0))));

        //lets delete the final shared filter for anonymous.
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        _testDeleteFilterConfirm(10010, 0, 4);

        //anonymous should no longer have any filters.
        checkAnonymousScreen(null);
        text.assertTextPresent(locator.page(), "Your search criteria did not match any filters.");
    }

    /**
     * Make sure that the passed filter cannot be deleted.
     *
     * @param filterId the filter to be deleted.
     */
    private void _testDeleteFilterPermission(long filterId)
    {
        tester.gotoPage("secure/DeleteFilter!default.jspa?filterId=" + filterId);

        text.assertTextPresent(locator.page(), "You do not have permission to delete this filter or this filter may not exist.");
        text.assertTextNotPresent(locator.page(), "Users who have nominated this filter as a favourite");
        text.assertTextNotPresent(locator.page(), "Subscriptions attached to this filter");
        text.assertTextNotPresent(locator.page(), "Deleting this filter will delete all the subscriptions");

        tester.assertButtonNotPresent("Delete");
    }


    /**
     * Make sure the passed user can be deleted. The method also checks that the user is warned about
     * any current subscriptions and other users that have it favourited.
     *
     * @param filterId          the filter to be deleted.
     * @param favouriteCount    the number of favourites the user should be warned about.
     * @param subscriptionCount the number of subscriptions that the user should be warned about.
     */
    private void _testDeleteFilterConfirm(long filterId, long favouriteCount, long subscriptionCount)
    {
        tester.gotoPage("secure/DeleteFilter!default.jspa?filterId=" + filterId + "&returnUrl=ManageFilters.jspa");
        Locator locator = new WebPageLocator(tester);

        List<String> messages = new ArrayList<String>();
        if (favouriteCount == 1)
        {
            messages.add("There is 1 other person who has added this filter as a favourite.");
        }
        else if(favouriteCount > 1)
        {
            messages.add("There are " + favouriteCount + " other people who have added this filter as a favourite.");            
        }
        else
        {
            text.assertTextNotPresent(locator, "Users who have nominated this filter as a favourite");
        }

        if (subscriptionCount == 1)
        {
            messages.add("There is 1 subscription attached to this filter.");
        }
        else if (subscriptionCount > 1)
        {
            messages.add("There are " + subscriptionCount + " subscriptions attached to this filter. ");
        }
        else
        {
            text.assertTextPresent(locator, "Deleting this filter will not alter any subscriptions, as there are 0 subscriptions associated with it.");
            text.assertTextNotPresent(locator, "Subscriptions attached to this filter");
            text.assertTextNotPresent(locator, "Deleting this filter will delete all the subscriptions");
        }

        if (!messages.isEmpty())
        {
            text.assertTextSequence(locator, messages.toArray(new String[messages.size()]));
        }

        tester.submit("Delete");
    }

    private void _testDeleteConfirm(String user, int row, int shared, int favs)
    {
        navigation.gotoAdmin();
        DeleteUserPage deleteUserPage = navigation.gotoPageWithParams(DeleteUserPage.class, DeleteUserPage.generateDeleteQueryParameters(user));
        assertThat(deleteUserPage.getNumberFromWarningFieldNamed(DeleteUserPage.SHARED_FILTERS), equalTo("" + shared));
        assertThat(deleteUserPage.getNumberFromWarningFieldNamedNoLink(DeleteUserPage.FAVORITED_FILTERS), equalTo("" + favs));
    }

    /**
     * Check the anonymous user's manage filters screen is correct.
     *
     * @param expectedFilters the filters that the user should see.
     */
    private void checkAnonymousScreen(List /*<FilterItems>*/ expectedFilters)
    {
        navigation.logout();
        navigation.manageFilters().goToDefault();

        assertNavigatorTabsPresent();

        //make sure that only the "All Filters" tab exists.
        text.assertTextPresent(locator.css(PAGE_NAVIGATION), "Search");
        text.assertTextPresent(locator.css(PAGE_NAVIGATION), "Popular");
        text.assertTextNotPresent(locator.css(PAGE_NAVIGATION), "Favourite");
        text.assertTextNotPresent(locator.css(PAGE_NAVIGATION), "My");

        navigation.manageFilters().allFilters();

        //parse the filters from the expected list.
        FilterList filterList = parse.filter().parseFilterList(FilterParser.TableId.SEARCH_TABLE);
        List actualFilters = null;
        if (filterList != null)
        {
            actualFilters = filterList.getFilterItems();
        }
        assertEquals(expectedFilters, actualFilters);

        //make sure the other tables do not exist.
        assertNull(parse.filter().parseFilterList(FilterParser.TableId.FAVOURITE_TABLE));
        assertNull(parse.filter().parseFilterList(FilterParser.TableId.OWNED_TABLE));
    }

    /**
     * Make sure that all the navigator tabs are available.
     */
    private void assertNavigatorTabsPresent()
    {
        //Nav tabs no longer appear on manage filters page
//        XPathLocator locator = new XPathLocator(tester, "//ul[@id='filterFormHeader']");
//
//        //these tabs should be visible.
//        text.assertTextPresent(locator, "Manage");
//        text.assertTextPresent(locator, "New");
//        text.assertTextPresent(locator, "View");
    }

    private void _testInitAnonymousScreen()
    {
        final List<FilterItem> compareList =
                CollectionBuilder.newBuilder(
                        noOps(makeNotFav(setIssueCount(setSubCount(FILTER_10001, 0), 0))),
                        noOps(setIssueCount(setSubCount(FILTER_10010, 0), 0))
                ).asList();

        checkAnonymousScreen(compareList);
    }

    private void _testAnonymousScreenAfterNobodyDelete()
    {
        final List<FilterItem> compareList =
                ImmutableList.of(
                        noOps(makeNotFav(setFavCount(setIssueCount(setSubCount(FILTER_10001, 0), 0), 2))),
                        noOps(setFavCount(setIssueCount(setSubCount(FILTER_10010, 0), 0), 1))
                );

        checkAnonymousScreen(compareList);
    }

    private void _testAnonymousScreenAfterJoeDelete()
    {
        final List<FilterItem> compareList =
                ImmutableList.of(noOps(setFavCount(setIssueCount(setSubCount(FILTER_10010, 0), 0), 0)));

        checkAnonymousScreen(compareList);
    }

    private void _testInitNobodyScreen()
    {
        FilterList filters;
        List filterList;
        List<FilterItem> compareList;

        navigation.login(NOBODY, NOBODY);
        navigation.manageFilters().goToDefault();

        assertNavigatorTabsPresent();

        filters = parse.filter().parseFilterList(FilterParser.TableId.FAVOURITE_TABLE);
        filterList = filters.getFilterItems();

        compareList = new ArrayList<FilterItem>();
        compareList.add(noFavCount(setSubCount(noOps(FILTER_10001), 0)));
        compareList.add(noFavCount(makeMine(FILTER_10030)));

        assertEquals(compareList, filterList);

        navigation.manageFilters().myFilters();
        filters = parse.filter().parseFilterList(FilterParser.TableId.OWNED_TABLE);
        compareList = new ArrayList<FilterItem>();
        compareList.add(noFavCount(noAuthor(FILTER_10030)));

        assertEquals(compareList, filters.getFilterItems());

        navigation.manageFilters().allFilters();
        filters = parse.filter().parseFilterList(FilterParser.TableId.SEARCH_TABLE);
        filterList = filters.getFilterItems();

        compareList = new ArrayList<FilterItem>();
        compareList.add(setSubCount(noOps(FILTER_10001), 0));
        compareList.add(noOps(makeNotFav(FILTER_10020)));
        compareList.add(setSubCount(noOps(FILTER_10010), 2));
        compareList.add(makeMine(noOps(FILTER_10030)));

        assertEquals(compareList, filterList);
    }

    private FilterItem setIssueCount(final FilterItem filter, int count)
    {
        FilterItem filterItem1 = filter.cloneFilter();
        filterItem1.setIssues(count);
        return filterItem1;
    }

    private FilterItem setSubCount(final FilterItem filter, int count)
    {
        FilterItem filterItem1 = filter.cloneFilter();
        filterItem1.setSubscriptions(count);
        return filterItem1;

    }

    private FilterItem makeFav(final FilterItem filter)
    {
        FilterItem filterItem1 = filter.cloneFilter();
        filterItem1.setFav(true);
        return filterItem1;
    }

    private FilterItem makeNotFav(final FilterItem filter)
    {
        FilterItem filterItem1 = filter.cloneFilter();
        filterItem1.setFav(false);
        return filterItem1;
    }

    private FilterItem setFavCount(final FilterItem filter, int count)
    {
        FilterItem item = filter.cloneFilter();
        item.setFavCount(count);
        return item;
    }

    private FilterItem noFavCount(final FilterItem filter)
    {
        FilterItem item = filter.cloneFilter();
        item.setFavCount(0);
        return item;
    }

    private FilterItem noOps(final FilterItem filter)
    {
        final FilterItem filterItem = filter.cloneFilter();
        filterItem.setOperations(NO_OPERATIONS);
        return filterItem;
    }

    private FilterItem noAuthor(final FilterItem filterItem1)
    {
        final FilterItem filterItem;
        filterItem = filterItem1.cloneFilter();
        filterItem.setAuthor(null);
        return filterItem;
    }

    private FilterItem makeMine(FilterItem filter)
    {
//        final FilterItem filterItem = filter.cloneFilter();
//        filterItem.setAuthor("Me");
//        return filterItem;
        return filter;
    }

    private void _testInitJoeScreen()
    {
        FilterList filters;
        List filterList;
        List<FilterItem> compareList;

        navigation.login(JOE, JOE);
        navigation.manageFilters().goToDefault();

        assertNavigatorTabsPresent();

        filters = parse.filter().parseFilterList(FilterParser.TableId.FAVOURITE_TABLE);
        filterList = filters.getFilterItems();

        compareList = new ArrayList<FilterItem>();
        compareList.add(noFavCount(makeMine(FILTER_10001)));

        assertEquals(compareList, filterList);

        navigation.manageFilters().myFilters();
        filters = parse.filter().parseFilterList(FilterParser.TableId.OWNED_TABLE);
        filterList = filters.getFilterItems();

        compareList = new ArrayList<FilterItem>();
        compareList.add(noFavCount(noAuthor(FILTER_10001)));

        assertEquals(compareList, filterList);

        navigation.manageFilters().allFilters();
        filters = parse.filter().parseFilterList(FilterParser.TableId.SEARCH_TABLE);
        filterList = filters.getFilterItems();

        compareList = new ArrayList<FilterItem>();
        compareList.add(noOps(makeMine(FILTER_10001)));
        compareList.add(noOps(setSubCount(FILTER_10010, 2)));

        assertEquals(compareList, filterList);
    }

    private void _testInitAdminScreen()
    {
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        navigation.manageFilters().goToDefault();

        assertNavigatorTabsPresent();

        FilterList filters = parse.filter().parseFilterList(FilterParser.TableId.FAVOURITE_TABLE);
        List filterList = filters.getFilterItems();

        List<FilterItem> compareList = new ArrayList<FilterItem>();
        compareList.add(noFavCount(setSubCount(noOps(FILTER_10001), 1)));
        compareList.add(noFavCount(makeMine(FILTER_10020)));

        assertEquals(compareList, filterList);

        navigation.manageFilters().myFilters();
        filters = parse.filter().parseFilterList(FilterParser.TableId.OWNED_TABLE);
        assertFalse(filters.containsColumn("Author"));
        filterList = filters.getFilterItems();

        compareList = new ArrayList<FilterItem>();
        compareList.add(noFavCount(noAuthor(FILTER_10000)));
        compareList.add(noFavCount(noAuthor(FILTER_10020)));
        compareList.add(noFavCount(noAuthor(FILTER_10010)));

        assertEquals(compareList, filterList);

        navigation.manageFilters().allFilters();
        filters = parse.filter().parseFilterList(FilterParser.TableId.SEARCH_TABLE);
        filterList = filters.getFilterItems();

        compareList = new ArrayList<FilterItem>();
        compareList.add(noOps(makeMine(FILTER_10000)));
        compareList.add(setSubCount(noOps(FILTER_10001), 1));
        compareList.add(noOps(makeMine(FILTER_10020)));
        compareList.add(noOps(makeMine(FILTER_10010)));

        assertEquals(compareList, filterList);
    }

    private void _testAdminScreenAfterNobodyDelete()
    {
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        navigation.manageFilters().goToDefault();

        assertNavigatorTabsPresent();

        FilterList filters = parse.filter().parseFilterList(FilterParser.TableId.FAVOURITE_TABLE);
        List filterList = filters.getFilterItems();

        List<FilterItem> compareList = new ArrayList<FilterItem>();
        compareList.add(noFavCount(setSubCount(noOps(FILTER_10001), 1)));
        compareList.add(noFavCount(makeMine(FILTER_10020)));

        assertEquals(compareList, filterList);

        navigation.manageFilters().myFilters();
        filters = parse.filter().parseFilterList(FilterParser.TableId.OWNED_TABLE);
        assertFalse(filters.containsColumn("Author"));
        filterList = filters.getFilterItems();

        compareList = new ArrayList<FilterItem>();
        compareList.add(noFavCount(noAuthor(FILTER_10000)));
        compareList.add(noFavCount(noAuthor(FILTER_10020)));
        compareList.add(noFavCount(noAuthor(FILTER_10010)));

        assertEquals(compareList, filterList);

        navigation.manageFilters().allFilters();
        filters = parse.filter().parseFilterList(FilterParser.TableId.SEARCH_TABLE);
        filterList = filters.getFilterItems();

        compareList = new ArrayList<FilterItem>();
        compareList.add(noOps(makeMine(FILTER_10000)));
        compareList.add(setFavCount(setSubCount(noOps(FILTER_10001), 1), 2));
        compareList.add(noOps(makeMine(FILTER_10020)));
        compareList.add(setFavCount(noOps(makeMine(FILTER_10010)), 1));

        assertEquals(compareList, filterList);
    }

    private void _testAdminScreenAfterJoeDelete()
    {
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        navigation.manageFilters().goToDefault();

        assertNavigatorTabsPresent();

        FilterList filters = parse.filter().parseFilterList(FilterParser.TableId.FAVOURITE_TABLE);
        List filterList = filters.getFilterItems();

        List<FilterItem> compareList = new ArrayList<FilterItem>();
        compareList.add(noFavCount(makeMine(FILTER_10020)));

        assertEquals(compareList, filterList);

        navigation.manageFilters().myFilters();
        filters = parse.filter().parseFilterList(FilterParser.TableId.OWNED_TABLE);
        assertFalse(filters.containsColumn("Author"));
        filterList = filters.getFilterItems();

        compareList = new ArrayList<FilterItem>();
        compareList.add(noFavCount(noAuthor(FILTER_10000)));
        compareList.add(noFavCount(noAuthor(FILTER_10020)));
        compareList.add(noFavCount(setSubCount(noAuthor(FILTER_10010), 3)));

        assertEquals(compareList, filterList);

        navigation.manageFilters().allFilters();
        filters = parse.filter().parseFilterList(FilterParser.TableId.SEARCH_TABLE);
        filterList = filters.getFilterItems();

        compareList = new ArrayList<FilterItem>();
        compareList.add(noOps(makeMine(FILTER_10000)));
        compareList.add(noOps(makeMine(FILTER_10020)));
        compareList.add(noOps(makeMine(setSubCount(FILTER_10010, 3))));

        assertEquals(compareList, filterList);
    }

    private void _testJoeScreenAfterNobodyDelete()
    {
        FilterList filters;
        List filterList;
        List<FilterItem> compareList;

        navigation.login(JOE, JOE);
        navigation.manageFilters().goToDefault();

        assertNavigatorTabsPresent();

        filters = parse.filter().parseFilterList(FilterParser.TableId.FAVOURITE_TABLE);
        filterList = filters.getFilterItems();

        compareList = new ArrayList<FilterItem>();
        compareList.add(noFavCount(makeMine(FILTER_10001)));

        assertEquals(compareList, filterList);

        navigation.manageFilters().myFilters();
        filters = parse.filter().parseFilterList(FilterParser.TableId.OWNED_TABLE);
        filterList = filters.getFilterItems();

        compareList = new ArrayList<FilterItem>();
        compareList.add(noFavCount(noAuthor(FILTER_10001)));

        assertEquals(compareList, filterList);

        navigation.manageFilters().allFilters();
        filters = parse.filter().parseFilterList(FilterParser.TableId.SEARCH_TABLE);
        filterList = filters.getFilterItems();

        compareList = new ArrayList<FilterItem>();
        compareList.add(setFavCount(noOps(makeMine(FILTER_10001)), 2));
        compareList.add(noOps(setSubCount(FILTER_10010, 2)));

        assertEquals(compareList, filterList);
    }
}
