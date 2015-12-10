package com.atlassian.jira.webtests.ztests.filter;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.navigation.FilterNavigation;
import com.atlassian.jira.functest.framework.parser.filter.FilterItem;
import com.atlassian.jira.functest.framework.parser.filter.FilterList;
import com.atlassian.jira.functest.framework.parser.filter.FilterParser;
import com.atlassian.jira.functest.framework.parser.filter.WebTestSharePermission;
import com.atlassian.jira.functest.framework.sharing.GroupTestSharingPermission;
import com.atlassian.jira.functest.framework.sharing.ProjectTestSharingPermission;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;

/**
 * Test the search filters functionality when searching occurs with specific ShareTypes.
 *
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.FILTERS })
public class TestSearchFiltersShareType extends FuncTestCase
{
    private static final String JIRA_ADMINISTRATORS_GROUP = "jira-administrators";
    private static final String JIRA_DEVELOPERS_GROUP = "jira-developers";

    private static final Long HOMOSAPIEN_PROJECT_ID = 10000L;
    private static final Long MONKEY_PROJECT_ID = 10001L;
    private static final String MONKEY_PROJECT_NAME = "monkey";

    private static final Long NAGGERS_ROLE_ID = 10010L;
    private static final String NAGGERS_ROLE_NAME = "Naggers";
    private static final String ADMIN_USER_STRING = ADMIN_FULLNAME + " (admin)";
    private static final String WIFE_USER_STRING = "wife the nagger (wife)";

    private static final String JIRA_USERS_GROUP = "jira-users";

    private static final int PAGE_SIZE = 20;

    private static final String NEXT_LINK_TEXT = "Next >>";
    private static final String PREVIOUS_LINK_TEXT = "<< Previous";

    private static final FilterItem ADMIN_GROUP_FILTER = new FilterItem(10080L, "developer", "", "developer the great and wise (developer)",
            ImmutableList.of(new WebTestSharePermission(GroupTestSharingPermission.TYPE, JIRA_DEVELOPERS_GROUP, null)),
            Boolean.FALSE, 0L, Collections.<String>emptyList(), 1L);

    private static final FilterItem WIFE_GROUP_FILTER = new FilterItem(10080L, "developer", "", "developer the great and wise (developer)",
            ImmutableList.of(new WebTestSharePermission(ProjectTestSharingPermission.TYPE, MONKEY_PROJECT_NAME, NAGGERS_ROLE_NAME)),
            Boolean.FALSE, 0L, Collections.<String>emptyList(), 1L);
    private FilterNavigation[] filterNavigationScreens;

    protected void setUpTest()
    {
        super.setUpTest();

        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        administration.restoreData("sharedfilters/TestBrowseFiltersShareType.xml");
        filterNavigationScreens = new FilterNavigation[] { navigation.manageFilters(), navigation.filterPickerPopup() };
    }

    protected void tearDownTest()
    {
        navigation.logout();
    }

    /**
     * Finding elements as anonymous should not allow you to select share type options, we always search for things they
     * can see. Any ShareType parameters are ignored but remembered.
     */
    public void testFindUsingAnonymous()
    {
        final List <FilterItem> expectedAdminItems = newArrayList();
        addGroupFilters(expectedAdminItems, "groupusers", 'a', 'e', 10040, JIRA_USERS_GROUP);

        executeGroupSearch(JIRA_USERS_GROUP, null, ADMIN_USERNAME, navigation.manageFilters());

        checkSearchResults(expectedAdminItems);

        navigation.logout();

        //the anonymous user should only see global items.
        final List <FilterItem> expectedAnonymousItems = newArrayList();
        addPublicFilters(expectedAnonymousItems, "", 'a', 'z', 10005);

        searchAll();
        checkSearchResults(expectedAnonymousItems);

        //the admin should see with the group conditions.
        navigation.login(ADMIN_USERNAME);
        executeGroupSearch(JIRA_USERS_GROUP, null, ADMIN_USERNAME, navigation.manageFilters());
        checkSearchResults(expectedAdminItems);
    }

    public void testFindAdminGroupsNoResults()
    {
        for (FilterNavigation aFilterNavigationScreen : filterNavigationScreens)
        {
            testFindAdminGroupsNoResults(aFilterNavigationScreen);
        }
    }

    public void testFindDeveloperGroups()
    {
        for (FilterNavigation aFilterNavigationScreen : filterNavigationScreens)
        {
            testFindDeveloperGroups(aFilterNavigationScreen);
        }
    }

    public void testFindDeveloperWithConditions()
    {
        for (FilterNavigation aFilterNavigationScreen : filterNavigationScreens)
        {
            testFindDeveloperWithConditions(aFilterNavigationScreen);
        }
    }

    public void testFindUsersGroups()
    {
        for (FilterNavigation aFilterNavigationScreen : filterNavigationScreens)
        {
            testFindUsersGroups(aFilterNavigationScreen);
        }
    }

    public void testFindHomosapienProjectNoResults()
    {
        for (FilterNavigation aFilterNavigationScreen : filterNavigationScreens)
        {
            testFindHomosapienProjectNoResults(aFilterNavigationScreen);
        }
    }

    public void testFindMonkeyProjectAsAdmin()
    {
        for (FilterNavigation aFilterNavigationScreen : filterNavigationScreens)
        {
            testFindMonkeyProjectAsAdmin(aFilterNavigationScreen);
        }
    }

    public void testFindMonkeyProjectAsWife()
    {
        for (FilterNavigation aFilterNavigationScreen : filterNavigationScreens)
        {
            testFindMonkeyProjectAsWife(aFilterNavigationScreen);
        }
    }

    public void testFindMonkeyProjectWithRoleNaggersAsWife()
    {
        for (FilterNavigation aFilterNavigationScreen : filterNavigationScreens)
        {
            testFindMonkeyProjectWithRoleNaggersAsWife(aFilterNavigationScreen);
        }
    }

    public void testFindMonkeyProjectWithRoleNaggersAsWifeAndConditions()
    {
        for (FilterNavigation aFilterNavigationScreen : filterNavigationScreens)
        {
            testFindMonkeyProjectWithRoleNaggersAsWifeAndConditions(aFilterNavigationScreen);
        }
    }

    /**
     * Look for filters shared with a group. There should be no shares.
     *
     * @param filterNavigation the FilterNavigation to test with.
     */
    public void testFindAdminGroupsNoResults(FilterNavigation filterNavigation)
    {
        executeGroupSearch(JIRA_ADMINISTRATORS_GROUP, null, null, filterNavigation);

        assertTrue(parseBrowse().isEmpty());
    }

    /**
     * Finding developers should return 26 results only.
     *
     * @param filterNavigation the FilterNavigation to test with.
     */
    public void testFindDeveloperGroups(FilterNavigation filterNavigation)
    {
        List <FilterItem> expectedItems = newArrayList();
        expectedItems.add(ADMIN_GROUP_FILTER);
        addGroupFilters(expectedItems, "groupdevs", 'a', 'z', 10045, JIRA_DEVELOPERS_GROUP);
        expectedItems = filterNavigation.sanitiseSearchFilterItems(expectedItems);
        executeGroupSearch(JIRA_DEVELOPERS_GROUP, null, null, filterNavigation);
        checkSearchResults(expectedItems);
    }

    /**
     * Finding with extra conditions. Only expect one result.
     *
     * @param filterNavigation the FilterNavigation to test with.
     */
    public void testFindDeveloperWithConditions(FilterNavigation filterNavigation)
    {
        executeGroupSearch(JIRA_DEVELOPERS_GROUP, "developer", "developer", filterNavigation);
        List<FilterItem> expectedItems = newArrayList(ADMIN_GROUP_FILTER);
        expectedItems = filterNavigation.sanitiseSearchFilterItems(expectedItems);
        checkSearchResults(expectedItems);
    }

    /**
     * Finding filters shared with group should return 5 results only.
     *
     * @param filterNavigation the FilterNavigation to test with.
     */
    public void testFindUsersGroups(FilterNavigation filterNavigation)
    {
        List <FilterItem> expectedItems = newArrayList();
        addGroupFilters(expectedItems, "groupusers", 'a', 'e', 10040, JIRA_USERS_GROUP);

        executeGroupSearch(JIRA_USERS_GROUP, null, null, filterNavigation);
        expectedItems = filterNavigation.sanitiseSearchFilterItems(expectedItems);
        checkSearchResults(expectedItems);
    }

    /**
     * Test to see that an empty search works when looking for projects.
     *
     * @param filterNavigation the FilterNavigation to test with.
     */
    public void testFindHomosapienProjectNoResults(FilterNavigation filterNavigation)
    {
        executeProjectSearch(HOMOSAPIEN_PROJECT_ID, null, null, null, filterNavigation);

        assertTrue(parseBrowse().isEmpty());
    }

    /**
     * Finding filters shared with project 'monkey' should return 26 results.
     *
     * @param filterNavigation the FilterNavigation to test with.
     */
    public void testFindMonkeyProjectAsAdmin(FilterNavigation filterNavigation)
    {
        List <FilterItem> expectedItems = newArrayList();
        addProjectFilters(expectedItems, "projectmonkey", 'a', 'z', 10090, ADMIN_USER_STRING, MONKEY_PROJECT_NAME, null);

        executeProjectSearch(MONKEY_PROJECT_ID, null, null, null, filterNavigation);
        expectedItems = filterNavigation.sanitiseSearchFilterItems(expectedItems);
        checkSearchResults(expectedItems);
    }

    /**
     * Finding filters shared with project 'monkey' should return 36 results as wife can see more roles.
     *
     * @param filterNavigation the FilterNavigation to test with.
     */
    public void testFindMonkeyProjectAsWife(FilterNavigation filterNavigation)
    {
        navigation.logout();
        navigation.login("wife", "wife");

        List <FilterItem> expectedItems = newArrayList();
        addProjectFilters(expectedItems, "projectmonkey", 'a', 'z', 10090, ADMIN_USER_STRING, MONKEY_PROJECT_NAME, null);
        addProjectFilters(expectedItems, "naggers", 'a', 'j', 10120, WIFE_USER_STRING, MONKEY_PROJECT_NAME, NAGGERS_ROLE_NAME);
        expectedItems.add(WIFE_GROUP_FILTER);

        expectedItems = filterNavigation.sanitiseSearchFilterItems(expectedItems);
        executeProjectSearch(MONKEY_PROJECT_ID, null, null, null, filterNavigation);
        checkSearchResults(expectedItems);
    }

    /**
     * Finding filters shared with project 'monkey'  and role 'naggers' should return 11.
     *
     * @param filterNavigation the FilterNavigation to test with.
     */
    public void testFindMonkeyProjectWithRoleNaggersAsWife(FilterNavigation filterNavigation)
    {
        navigation.logout();
        navigation.login("wife", "wife");

        List <FilterItem> expectedItems = newArrayList();
        addProjectFilters(expectedItems, "naggers", 'a', 'j', 10120, WIFE_USER_STRING, MONKEY_PROJECT_NAME, NAGGERS_ROLE_NAME);
        expectedItems.add(WIFE_GROUP_FILTER);
        expectedItems = filterNavigation.sanitiseSearchFilterItems(expectedItems);
        executeProjectSearch(MONKEY_PROJECT_ID, NAGGERS_ROLE_ID, null, null, filterNavigation);

        checkSearchResults(expectedItems);
    }

    /**
     * Finding filters shared with project 'monkey' and owned by 'wife' should return 10 issues.
     *
     * @param filterNavigation the FilterNavigation to test with.
     */
    public void testFindMonkeyProjectWithRoleNaggersAsWifeAndConditions(FilterNavigation filterNavigation)
    {
        navigation.logout();
        navigation.login("wife", "wife");

        List <FilterItem> expectedItems = newArrayList();
        addProjectFilters(expectedItems, "naggers", 'a', 'j', 10120, WIFE_USER_STRING, MONKEY_PROJECT_NAME, NAGGERS_ROLE_NAME);
        expectedItems = filterNavigation.sanitiseSearchFilterItems(expectedItems);

        executeProjectSearch(MONKEY_PROJECT_ID, NAGGERS_ROLE_ID, "naggers*", "wife", filterNavigation);

        checkSearchResults(expectedItems);
    }

    private void searchAll()
    {
        navigation.manageFilters().searchFilters();
        tester.setWorkingForm("filterSearchForm");
        tester.submit("Search");
    }

    /**
     * Iterate across the search results in the different search orders and make sure they are correct.
     *
     * @param expectedItems the items that should be on the search results.
     */
    private void checkSearchResults(final List<FilterItem> expectedItems)
    {
        //check the ascending table name.
        Collections.sort(expectedItems, NameComparator.NAME_COMPARATOR);

        checkNext(expectedItems);
        checkPrevious(expectedItems);

        //we should now change to descending.
        tester.clickLink("filter_sort_name");

        Collections.sort(expectedItems, Ordering.from(NameComparator.NAME_COMPARATOR).reverse());

        checkNext(expectedItems);
        checkPrevious(expectedItems);

        //we should now change author ascending.
        tester.clickLink("filter_sort_owner");

        Collections.sort(expectedItems, createAuthorComparator(false));
        checkNext(expectedItems);
        checkPrevious(expectedItems);

        //we should now have descending order.
        tester.clickLink("filter_sort_owner");

        Collections.sort(expectedItems, createAuthorComparator(true));
        checkNext(expectedItems);
        checkPrevious(expectedItems);

        //we should now change popularity descending.
        tester.clickLink("filter_sort_popularity");

        Collections.sort(expectedItems, createPopularComparator(true));
        checkNext(expectedItems);
        checkPrevious(expectedItems);

        //we should now have popularity order ascending.
        tester.clickLink("filter_sort_popularity");

        Collections.sort(expectedItems, createPopularComparator(false));
        checkNext(expectedItems);
        checkPrevious(expectedItems);
    }

    private Comparator<FilterItem> createAuthorComparator(boolean reverse)
    {
        Comparator<FilterItem> authorComparator = AuthorComparator.AUTHOR_COMPARATOR;
        if (reverse)
        {
            authorComparator = Ordering.from(authorComparator).reverse();
        }
        return Ordering.from(authorComparator).compound(NameComparator.NAME_COMPARATOR);
    }

    private Comparator<FilterItem> createPopularComparator(boolean reverse)
    {
        Comparator<FilterItem> authorComparator = PopularComparator.POPULAR_COMPARATOR;
        if (reverse)
        {
            authorComparator = Ordering.from(authorComparator).reverse();
        }
        return Ordering.from(authorComparator).compound(NameComparator.NAME_COMPARATOR);
    }

    private void addProjectFilters(final List <FilterItem> items, final String name, final char startChar, final char endChar,
            final long initialId, final String userName, final String project, final String roleId)
    {
        WebTestSharePermission permission = new WebTestSharePermission(ProjectTestSharingPermission.TYPE, project, roleId);
        addFilters(items, name, startChar, endChar, initialId, userName, Collections.singletonList(permission));
    }

    private void addGroupFilters(final List <FilterItem> items, final String name, final char startChar, final char endChar,
            final long initialId, final String groupName)
    {
        WebTestSharePermission permission = new WebTestSharePermission(GroupTestSharingPermission.TYPE, groupName, null);
        addFilters(items, name, startChar, endChar, initialId, ADMIN_USER_STRING, Collections.singletonList(permission));
    }

    private void addPublicFilters(final List <FilterItem> items, final String name, final char startChar, final char endChar,
            final long initialId)
    {
        WebTestSharePermission permission = new WebTestSharePermission(WebTestSharePermission.GLOBAL_TYPE, null, null);
        addFilters(items, name, startChar, endChar, initialId, ADMIN_USER_STRING, Collections.singletonList(permission));
    }

    private void addFilters(final List <FilterItem> items, final String name, final char startChar, final char endChar,
            final long initialId, final String user, final List<WebTestSharePermission> shares)
    {
        long currentId = initialId;

        for (char currentChar = startChar; currentChar <= endChar; currentChar++)
        {
            items.add(new FilterItem(currentId, name + currentChar, "", user, shares,
                    Boolean.FALSE, 0L, Collections.<String>emptyList(), 0L));
            currentId++;
        }
    }

    private void executeProjectSearch(final Long projectId, final Long roleId,
            final String text, final String userName, FilterNavigation filterNavigation)
    {
        filterNavigation.searchFilters();
        tester.setWorkingForm("filterSearchForm");

        fillInOtherParameters(text);

        tester.setFormElement("searchShareType", "project");
        tester.setFormElement("projectShare", projectId.toString());
        tester.setFormElement("roleShare", roleId == null ? "" : roleId.toString());

        tester.submit("Search");
    }

    private void executeGroupSearch(final String groupName, final String text, final String userName, FilterNavigation filterNavigation)
    {
        filterNavigation.searchFilters();
        tester.setWorkingForm("filterSearchForm");

        fillInOtherParameters(text);

        tester.setFormElement("searchShareType", "group");
        tester.setFormElement("groupShare", groupName);

        tester.submit("Search");
    }

    private void fillInOtherParameters(final String text)
    {
        if (StringUtils.isNotBlank(text))
        {
            tester.setFormElement("searchName", text);
        }
    }

    /**
     * Given the passed filter items, move forward through the current search results and ensure that
     * the items are displayed correctly.
     *
     * @param items the filter items to check.
     */
    private void checkNext(List <FilterItem> items)
    {
        final int iterations = items.size() / PAGE_SIZE;
        final int remainder = items.size() % PAGE_SIZE;

        if (iterations == 0 || PAGE_SIZE == items.size())
        {
            //there is not paging, then just check the results.
            tester.assertLinkNotPresentWithText(PREVIOUS_LINK_TEXT);
            tester.assertLinkNotPresentWithText(NEXT_LINK_TEXT);

            assertEquals("All filters not on first screen.", items, parseBrowse());
        }
        else
        {
            for (int i = 1; i <= iterations; i++)
            {
                final int startPos = (i - 1) * PAGE_SIZE;
                final int endPos = i * PAGE_SIZE;
                
                final List expectedItems = items.subList(startPos, endPos);

                final List list = parseBrowse();

                assertions.assertEquals("Page " + i + " did not contain expected items.", expectedItems, list);

                if (i == 1)
                {
                    tester.assertLinkNotPresentWithText(PREVIOUS_LINK_TEXT);
                }
                else
                {
                    tester.assertLinkPresentWithText(PREVIOUS_LINK_TEXT);
                }

                tester.assertTextPresent("" + (startPos + 1) + " - " + endPos);

                if (remainder == 0 && i == iterations)
                {
                    tester.assertLinkNotPresent(NEXT_LINK_TEXT);
                }
                else
                {
                    tester.assertLinkPresentWithText(NEXT_LINK_TEXT);
                    tester.clickLinkWithText(NEXT_LINK_TEXT);
                }
            }

            if (remainder != 0)
            {
                tester.assertLinkPresentWithText(PREVIOUS_LINK_TEXT);
                tester.assertLinkNotPresentWithText(NEXT_LINK_TEXT);

                final int startPos = iterations * PAGE_SIZE;

                tester.assertTextPresent("" + (startPos + 1) + " - " + items.size());

                final List expectedItems = items.subList(startPos, items.size());
                assertEquals("Last page did not contain expected items.", expectedItems, parseBrowse());
            }
        }
    }

    /**
     * Given the passed filter items, backward forward through the current search results and ensure that
     * the items are displayed correctly.
     *
     * @param items the filter items to check.
     */
    private void checkPrevious(final List items)
    {
        final int iterations = items.size() / PAGE_SIZE;
        final int remainder = items.size() % PAGE_SIZE;

        if (iterations == 0 || items.size() == PAGE_SIZE)
        {
            //there is not paging, then just check the results.
            tester.assertLinkNotPresentWithText(PREVIOUS_LINK_TEXT);
            tester.assertLinkNotPresentWithText(NEXT_LINK_TEXT);

            assertEquals("All filters not on first screen.", items, parseBrowse());
        }
        else
        {
            if (remainder != 0)
            {
                tester.assertLinkNotPresentWithText(NEXT_LINK_TEXT);

                final int startPos = iterations * PAGE_SIZE;

                tester.assertTextPresent("" + (startPos + 1) + " - " + items.size());

                final List expectedItems = items.subList(startPos, items.size());
                assertEquals("Last page did not contain expected items.", expectedItems, parseBrowse());

                tester.clickLinkWithText(PREVIOUS_LINK_TEXT);
            }

            for (int i = iterations; i > 0; i--)
            {
                final int startPos = (i - 1) * PAGE_SIZE;
                final int endPos = i * PAGE_SIZE;
                
                final List expectedItems = items.subList(startPos, endPos);

                assertEquals("Page " + i + " did not contain expected items.", expectedItems, parseBrowse());

                tester.assertTextPresent("" + (startPos + 1) + " - " + endPos);

                if (i == 1)
                {
                    tester.assertLinkNotPresentWithText(PREVIOUS_LINK_TEXT);
                }
                else
                {
                    tester.clickLinkWithText(PREVIOUS_LINK_TEXT);
                }

                if (remainder == 0 && i == iterations)
                {
                    tester.assertLinkNotPresent(NEXT_LINK_TEXT);
                }
                else
                {
                    tester.assertLinkPresentWithText(NEXT_LINK_TEXT);
                }
            }
        }
    }

    private List <FilterItem> parseBrowse()
    {
        final FilterList list = parse.filter().parseFilterList(FilterParser.TableId.SEARCH_TABLE);
        if (list == null)
        {
            return emptyList();
        }
        else
        {
            final List<FilterItem> items = list.getFilterItems();
            if (items == null)
            {
                return emptyList();
            }
            else
            {
                return items;
            }
        }
    }

    private static class AuthorComparator implements Comparator<FilterItem>
    {
        public static final AuthorComparator AUTHOR_COMPARATOR = new AuthorComparator();
        private static final Pattern NAME_REGEX = Pattern.compile("\\((.*)\\)");

        private static String getName(final String authorName)
        {
            final Matcher matcher = NAME_REGEX.matcher(authorName);
            if (matcher.find())
            {
                return matcher.group(1);
            }
            else
            {
                return authorName;
            }
        }

        @Override
        public int compare(FilterItem item1, FilterItem item2)
        {
            String author1 = getName(item1.getAuthor());
            String author2 = getName(item2.getAuthor());

            return author1.compareTo(author2);
        }
    }

    private static class PopularComparator implements Comparator<FilterItem>
    {
        public static final PopularComparator POPULAR_COMPARATOR = new PopularComparator();

        @Override
        public int compare(FilterItem item1, FilterItem item2)
        {
            return item1.getFavCount().compareTo(item2.getFavCount());
        }
    }

    private static class NameComparator implements Comparator<FilterItem>
    {
        public static final NameComparator NAME_COMPARATOR = new NameComparator();

        @Override
        public int compare(FilterItem item1, FilterItem item2)
        {
            return item1.getName().compareTo(item2.getName());
        }
    }
}
