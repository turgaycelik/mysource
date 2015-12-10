package com.atlassian.jira.webtests.ztests.dashboard;

import com.atlassian.jira.functest.framework.Dashboard;
import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.dashboard.DashboardPageInfo;
import com.atlassian.jira.functest.framework.locator.TableLocator;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.sharing.ProjectTestSharingPermission;
import com.atlassian.jira.functest.framework.sharing.TestSharingPermission;
import com.atlassian.jira.functest.framework.sharing.TestSharingPermissionUtils;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import junit.framework.AssertionFailedError;
import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * A functional test of the simple dashboard page searching.
 *
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.DASHBOARDS })
public class TestSearchDashboardPages extends FuncTestCase
{
    private static final String NEXT_LINK_TEXT = "Next";
    private static final String PREVIOUS_LINK_TEXT = "Previous";

    private static final int PAGE_SIZE = 20;

    private static final String GROUP_SHAREWITH = "sharewith-group";
    private static final String GROUP_ALICE = "alicegroup";

    private static final int PROJECT_HOMOSAPIEN = 10000;
    private static final String PROJECT_HOMOSAPIEN_NAME = "homosapien";
    private static final int PROJECT_MONKEY = 10001;
    private static final String PROJECT_MONKEY_NAME = "monkey";


    private static final int ROLE_USERS = 10000;
    private static final int ROLE_DEVELOPERS = 10001;
    private static final int ROLE_ADMINISTRATORS = 10002;

    private static final String ROLE_USERS_NAME = "Users";
    private static final String ROLE_DEVELOPERS_NAME = "Developers";

    private static final String USER_ALICE = "alice";
    private static final String USER_ADMIN = ADMIN_USERNAME;
    private static final String USER_FRED = FRED_USERNAME;

    private static final DashboardPageInfo PAGE_ALICE_GROUP = new DashboardPageInfo(10040L, "alicegroup", null, false, TestSharingPermissionUtils.createGroupPermissions(GROUP_SHAREWITH), USER_ALICE, 1, null);
    private static final DashboardPageInfo PAGE_ALICE_ROLE = new DashboardPageInfo(10060L, "alice role", null, true, Collections.<TestSharingPermission>singleton(new ProjectTestSharingPermission(PROJECT_HOMOSAPIEN, ROLE_DEVELOPERS, PROJECT_HOMOSAPIEN_NAME, ROLE_DEVELOPERS_NAME)), USER_ALICE, 1, null);
    private static final DashboardPageInfo PAGE_FRED_PROJECT = new DashboardPageInfo(10042L, "fredproject", null, true, Collections.<TestSharingPermission>singleton(new ProjectTestSharingPermission(PROJECT_HOMOSAPIEN, PROJECT_HOMOSAPIEN_NAME)), USER_FRED, 3, null);
    private static final DashboardPageInfo PAGE_FRED_ROLE = new DashboardPageInfo(10043L, "fred role", null, false, Collections.<TestSharingPermission>singleton(new ProjectTestSharingPermission(PROJECT_HOMOSAPIEN, PROJECT_HOMOSAPIEN_NAME)), USER_FRED, 1, null);
    private static final DashboardPageInfo PAGE_FRED_PROJECT_MONEY = new DashboardPageInfo(10116L, "fredmonkey", "fredmonkey description", false, Collections.<TestSharingPermission>singleton(new ProjectTestSharingPermission(PROJECT_MONKEY, PROJECT_MONKEY_NAME)), USER_FRED, 1, null);
    private static final DashboardPageInfo PAGE_ADMIN_PRIVATE = new DashboardPageInfo(10050L, "privateadmin", null, false, TestSharingPermissionUtils.createPrivatePermissions(), ADMIN_USERNAME, 0, null);
    private static final DashboardPageInfo PAGE_SYSTEM_DEFAULT = new DashboardPageInfo(null, "System Dashboard", null, false, TestSharingPermissionUtils.createPublicPermissions(), "System", 0, null);
    private static final DashboardPageInfo PAGE_ADMIN_AZ = new DashboardPageInfo(10010L, "a", "z", false, TestSharingPermissionUtils.createPublicPermissions(), ADMIN_USERNAME, 0, null);


    protected void setUpTest()
    {
        administration.restoreData("sharedpages/SimpleDashboardSearch.xml");
    }

    /**
     * Check the initial state of the form.
     */
    public void testInitialState()
    {
        navigation.dashboard().navigateToSearch();

        tester.assertFormElementEquals("searchName", "");
        tester.assertFormElementEquals("searchOwnerUserName", "");
        tester.assertFormElementEquals("searchShareType", "any");
    }

    /**
     * Check that we can find all pages.
     */
    public void testAllSearch()
    {
        List<DashboardPageInfo> pages = new ArrayList<DashboardPageInfo>();

        addAdminPages(pages);

        //find all the pages.
        executeSimpleSearch(null, null);
        assertSearchResults(pages);
    }

    /**
     * Check that a search through the name works correctly.
     */
    public void testNameSearch()
    {
        List<DashboardPageInfo> pages = new ArrayList<DashboardPageInfo>();

        pages.add(PAGE_ADMIN_AZ);
        pages.add(PAGE_FRED_PROJECT);
        pages.add(PAGE_FRED_ROLE);
        pages.add(PAGE_FRED_PROJECT_MONEY);
        pages.add(new DashboardPageInfo(10035L, "z", null, false, TestSharingPermissionUtils.createPublicPermissions(), ADMIN_USERNAME, 0, null));

        //find all the pages.
        executeSimpleSearch("fred* z*", null);
        assertSearchResults(pages);
    }

    /**
     * Check that a search that returns no results works.
     */
    public void testNameSearchNoResults()
    {
        executeSimpleSearch("zoobars", null);
        assertNoMatch();
    }

    /**
     * Check that we can search by the user.
     */
    public void testUserSearch()
    {
        List<DashboardPageInfo> pages = new ArrayList<DashboardPageInfo>();
        addAdminPublic(pages);
        pages.add(PAGE_ADMIN_PRIVATE);

        executeSimpleSearch(null, ADMIN_USERNAME);
        assertSearchResults(pages);

        //JRA-26441: User names are now case-insensitive.
        executeSimpleSearch(null, ADMIN_USERNAME.toUpperCase(Locale.ENGLISH));
        assertSearchResults(pages);
    }

    /**
     * Make sure we get an error when the user does not exist.
     */
    public void testUserNotExistSearch()
    {
        executeSimpleSearch(null, "userWhoDoesNotExit");
        assertions.getJiraFormAssertions().assertFieldErrMsg("The user 'userWhoDoesNotExit' does not exist.");
    }

    /**
     * Make sure we can combine user and name search.
     */
    public void testNameAndUserSearch()
    {
        List<DashboardPageInfo> pages = new ArrayList<DashboardPageInfo>();

        pages.add(PAGE_FRED_PROJECT);
        pages.add(PAGE_FRED_ROLE);
        pages.add(PAGE_FRED_PROJECT_MONEY);

        executeSimpleSearch("fred* z*", FRED_USERNAME);
        assertSearchResults(pages);
    }

    public void testGroupSearch()
    {
        List<DashboardPageInfo> pages = new ArrayList<DashboardPageInfo>();
        pages.add(PAGE_ALICE_GROUP);

        //find all the pages.
        executeGroupSearch(null, null, GROUP_SHAREWITH);
        assertSearchResults(pages);
    }

    public void testGroupSearchNoMatch()
    {
        executeGroupSearch(null, null, "jira-users");
        assertNoMatch();
    }

    public void testGroupAndNameSearch()
    {
        //find all the pages.
        executeGroupSearch(FRED_USERNAME, null, GROUP_SHAREWITH);
        assertNoMatch();
    }

    /**
     * Try and search for a group that does not exist.
     */
    public void testGroupDoesNotExist()
    {
        //need to pass in the direct URL because HTTPUnit does not allow us to set drop down to other values.
        tester.gotoPage("secure/ConfigurePortalPages!default.jspa?view=search&searchName=&searchOwnerUserName=&searchShareType=group&groupShare=somerandomgroup&Search=Search");

        assertions.getJiraFormAssertions().assertFieldErrMsg("Group: 'somerandomgroup' does not exist.");
    }

    /**
     * Try and search for a group that you are not a member of.
     */
    public void testGroupNotMember()
    {
        navigation.login(USER_ALICE);
        tester.gotoPage("secure/ConfigurePortalPages!default.jspa?view=search&searchName=&searchOwnerUserName=&searchShareType=group&groupShare=jira-developers&Search=Search");

        assertions.getJiraFormAssertions().assertFieldErrMsg("You are not a member of Group: 'jira-developers'.");
    }

    /**
     * Test to see what happens when matching exactly 20 pages.
     */
    public void testGroupExact()
    {
        navigation.login(USER_ALICE);

        List<DashboardPageInfo> pages = new ArrayList<DashboardPageInfo>();
        addDashboardPages(pages, "alicegroup", 'a', 't', 10070, USER_ALICE, TestSharingPermissionUtils.createGroupPermissions(GROUP_ALICE));

        executeGroupSearch(null, null, GROUP_ALICE);

        assertSearchResults(pages);
    }

    /**
     * Look for shares associated with a project.
     */
    public void testProjectSearch()
    {
        List<DashboardPageInfo> pages = new ArrayList<DashboardPageInfo>();
        pages.add(PAGE_FRED_PROJECT);
        pages.add(PAGE_FRED_ROLE);

        executeProjectSearch(null, null, PROJECT_HOMOSAPIEN, -1);
        assertSearchResults(pages);
    }

    /**
     * Check a project search from Alice's perspective.
     */
    public void testProjectSearchAlice()
    {
        navigation.login(USER_ALICE);

        Set<TestSharingPermission> permissions = new HashSet<TestSharingPermission>();
        permissions.add(new ProjectTestSharingPermission(PROJECT_HOMOSAPIEN, PROJECT_HOMOSAPIEN_NAME));
        permissions.add(new ProjectTestSharingPermission(PROJECT_HOMOSAPIEN, ROLE_USERS, PROJECT_HOMOSAPIEN_NAME, ROLE_USERS_NAME));

        List<DashboardPageInfo> pages = new ArrayList<DashboardPageInfo>();
        DashboardPageInfo pageInfo = new DashboardPageInfo(PAGE_FRED_PROJECT);
        pageInfo.setFavourite(true);
        pages.add(pageInfo);

        pageInfo = new DashboardPageInfo(PAGE_FRED_ROLE);
        pageInfo.setFavourite(true);
        pageInfo.setSharingPermissions(permissions);
        pages.add(pageInfo);
        
        pages.add(PAGE_ALICE_ROLE);

        executeProjectSearch(null, null, PROJECT_HOMOSAPIEN, -1);
        assertSearchResults(pages);
    }

    /**
     * Look to make sure no match works.
     */
    public void testProjectSearchNoMatch()
    {
        executeProjectSearch("alice in wonderland", null, PROJECT_HOMOSAPIEN, -1);
        assertNoMatch();
    }

    /**
     * Execute a complex query with project.
     */
    public void testProjectSearchWithAuthorAndName()
    {
        List<DashboardPageInfo> pages = new ArrayList<DashboardPageInfo>();
        pages.add(PAGE_FRED_ROLE);

        executeProjectSearch("role", FRED_USERNAME, PROJECT_HOMOSAPIEN, -1);
        assertSearchResults(pages);
    }

    /**
     * Check what happens when trying to search object that does not exist.
     */
    public void testProjectSearchOnProjectThatDoesNotExist()
    {
        tester.gotoPage("secure/ConfigurePortalPages!default.jspa?view=search&searchName=&searchOwnerUserName=&searchShareType=project&groupShare=jira-administrators&projectShare=10006&roleShare=&Search=Search");

        assertions.getJiraFormAssertions().assertFieldErrMsg("The project with identifier '10,006' does not exist.");
    }

    /**
     * Check what happens when you try to search project you don't have permission to see.
     */
    public void testProjectSearchNoPermission()
    {
        navigation.login(USER_ALICE);

        tester.gotoPage("secure/ConfigurePortalPages!default.jspa?view=search&searchName=&searchOwnerUserName=&searchShareType=project&groupShare=jira-administrators&projectShare=10001&roleShare=&Search=Search");

        assertions.getJiraFormAssertions().assertFieldErrMsg("You do not have permission to view the project.");
    }

    /**
     * Check paging to ensure project settings are transferred.
     */
    public void testProjectSearchPaging()
    {
        navigation.login(USER_FRED);

        final List<DashboardPageInfo> pages = new ArrayList<DashboardPageInfo>();

        final DashboardPageInfo info = new DashboardPageInfo(PAGE_FRED_PROJECT_MONEY);
        info.setFavourite(true);
        pages.add(info);
        addDashboardPages(pages, "fredmonkeyrole", 'a', 'z', 10090, USER_FRED, TestSharingPermissionUtils.createProjectPermissions(PROJECT_MONKEY, ROLE_USERS, PROJECT_MONKEY_NAME, ROLE_USERS_NAME));

        executeProjectSearch(null, null, PROJECT_MONKEY, -1);

        assertSearchResults(pages);
    }

    /**
     * Check what happens when passed a bad project argument.
     */
    public void testProjectBadArgument()
    {
        tester.gotoPage("secure/ConfigurePortalPages!default.jspa?view=search&searchName=&searchOwnerUserName=&searchShareType=project&groupShare=jira-administrators&projectShare=10000a&roleShare=10001&Search=Search");

        assertions.getJiraFormAssertions().assertFieldErrMsg("Illegal search parameters.");
    }

    /**
     * Make sure a role match works correctly.
     */
    public void testRoleSearch()
    {
        navigation.login(USER_ALICE);

        Set<TestSharingPermission> permissions = new HashSet<TestSharingPermission>();
        permissions.add(new ProjectTestSharingPermission(PROJECT_HOMOSAPIEN, PROJECT_HOMOSAPIEN_NAME));
        permissions.add(new ProjectTestSharingPermission(PROJECT_HOMOSAPIEN, ROLE_USERS, PROJECT_HOMOSAPIEN_NAME, ROLE_USERS_NAME));

        List<DashboardPageInfo> pages = new ArrayList<DashboardPageInfo>();

        DashboardPageInfo pageInfo = new DashboardPageInfo(PAGE_FRED_ROLE);
        pageInfo.setSharingPermissions(permissions);
        pageInfo.setFavourite(true);

        pages.add(pageInfo);

        executeProjectSearch(null, null, PROJECT_HOMOSAPIEN, ROLE_USERS);
        assertSearchResults(pages);
    }

    /**
     * Make sure that matching no results works.
     */
    public void testRoleSearchNoResults()
    {
        executeProjectSearch(null, null, PROJECT_HOMOSAPIEN, ROLE_ADMINISTRATORS);
        assertNoMatch();
    }

    /**
     * Make sure that complex match also works.
     */
    public void testRoleSearchComplex()
    {
        navigation.login(USER_ALICE);

        List<DashboardPageInfo> pages = new ArrayList<DashboardPageInfo>();
        pages.add(PAGE_ALICE_ROLE);

        executeProjectSearch("role bad", "alice", PROJECT_HOMOSAPIEN, ROLE_DEVELOPERS);
        assertSearchResults(pages);
    }

    /**
     * Make sure that matching no results works.
     */
    public void testRoleSearchComplexNoResults()
    {
        navigation.login(USER_ALICE);

        executeProjectSearch("role", "alice", PROJECT_HOMOSAPIEN, ROLE_USERS);
        assertNoMatch();
    }

    /**
     * Check paging to ensure project settings are transferred.
     */
    public void testRolePaging()
    {
        navigation.login(USER_FRED);

        List<DashboardPageInfo> pages = new ArrayList<DashboardPageInfo>();

        addDashboardPages(pages, "fredmonkeyrole", 'a', 'z', 10090, USER_FRED, TestSharingPermissionUtils.createProjectPermissions(PROJECT_MONKEY, ROLE_USERS, PROJECT_MONKEY_NAME, ROLE_USERS_NAME));

        executeProjectSearch(null, null, PROJECT_MONKEY, ROLE_USERS);

        assertSearchResults(pages);
    }

    /**
     * Check what happens when role does not exist.
     */
    public void testRoleRoleDoesNotExist()
    {
        tester.gotoPage("secure/ConfigurePortalPages!default.jspa?view=search&searchName=&searchOwnerUserName=&searchShareType=project&groupShare=jira-administrators&projectShare=10000&roleShare=100222&Search=Search");

        assertions.getJiraFormAssertions().assertFieldErrMsg("The project role with identifier '100,222' does not exist.");
    }

    /**
     * Check what happens when not member of the role.
     */
    public void testRoleNotMember()
    {
        tester.gotoPage("secure/ConfigurePortalPages!default.jspa?view=search&searchName=&searchOwnerUserName=&searchShareType=project&groupShare=jira-administrators&projectShare=10000&roleShare=10001&Search=Search");

        assertions.getJiraFormAssertions().assertFieldErrMsg("You are not in the specified project role.");
    }

    /**
     * Check what happens with invalid role. The bad role should just be ignored.
     */
    public void testRoleIllegalRole()
    {
        tester.gotoPage("secure/ConfigurePortalPages!default.jspa?view=search&searchName=&searchOwnerUserName=&searchShareType=project&groupShare=jira-administrators&projectShare=10000&roleShare=10001s&Search=Search");

        List<DashboardPageInfo> pages = new ArrayList<DashboardPageInfo>();
        pages.add(PAGE_FRED_PROJECT);
        pages.add(PAGE_FRED_ROLE);

        assertSearchResults(pages);
    }

    /**
     * Make sure that clicking the link restarts the search.
     */
    public void testSortRestart()
    {
        executeSimpleSearch(null, null);

        //move to the second page.
        tester.clickLinkWithText(NEXT_LINK_TEXT);

        //sort by popularity.
        tester.clickLink("page_sort_popularity");

        List<DashboardPageInfo> pages = new ArrayList<DashboardPageInfo>();
        addAdminPages(pages);
        Collections.sort(pages, createPopularComparator(true));

        //make sure we end up at the start page with the correct values.
        checkNext(pages);
    }

    private void executeProjectSearch(final String name, final String author, final int projectId, final int roleId)
    {
        setSimpleSearch(name, author);

        tester.setFormElement("searchShareType", "project");
        tester.setFormElement("projectShare", String.valueOf(projectId));
        if (roleId >= 0)
        {
            tester.setFormElement("roleShare", String.valueOf(roleId));
        }

        tester.submit("Search");
    }

    private void executeGroupSearch(final String text, final String userName, final String group)
    {
        setSimpleSearch(text, userName);

        tester.setFormElement("searchShareType", "group");
        tester.setFormElement("groupShare", group);

        tester.submit("Search");
    }

    private void executeSimpleSearch(final String text, final String userName)
    {
        setSimpleSearch(text, userName);

        tester.submit("Search");
    }

    private void setSimpleSearch(final String text, final String userName)
    {
        navigation.dashboard().navigateToSearch();
        tester.setWorkingForm("pageSearchForm");

        if (StringUtils.isNotBlank(text))
        {
            tester.setFormElement("searchName", text);
        }

        if (StringUtils.isNotBlank(userName))
        {
            tester.setFormElement("searchOwnerUserName", userName);
        }
    }

    private Comparator<DashboardPageInfo> createPopularComparator(boolean reverse)
    {
        Comparator<DashboardPageInfo> authorComparator = PopularComparator.POPULAR_COMPARATOR;
        if (reverse)
        {
            authorComparator = ComparatorUtils.reversedComparator(authorComparator);
        }
        return ComparatorUtils.chainedComparator(authorComparator, NameComparator.NAME_COMPARATOR);
    }

    private Comparator<DashboardPageInfo> createAuthorComparator(boolean reverse)
    {
        Comparator<DashboardPageInfo> authorComparator = AuthorComparator.AUTHOR_COMPARATOR;
        if (reverse)
        {
            authorComparator = ComparatorUtils.reversedComparator(authorComparator);
        }
        return ComparatorUtils.chainedComparator(authorComparator, NameComparator.NAME_COMPARATOR);
    }

    private void addAdminPublic(final List<DashboardPageInfo> pages)
    {
        addAdminPublicDashboardPages(pages, "", 'b', 'z', 10011);
        pages.add(PAGE_ADMIN_AZ);
    }

    private void addAdminPages(final List<DashboardPageInfo> pages)
    {
        addAdminPublic(pages);
        pages.add(PAGE_ALICE_GROUP);
        pages.add(PAGE_FRED_PROJECT);
        pages.add(PAGE_FRED_ROLE);
        pages.add(PAGE_ADMIN_PRIVATE);
        pages.add(PAGE_SYSTEM_DEFAULT);
        pages.add(PAGE_FRED_PROJECT_MONEY);
    }

    private void addAdminPublicDashboardPages(final List<DashboardPageInfo> items, final String name,
            final char startChar, final char endChar, final long initialId)
    {
        addDashboardPages(items, name, startChar, endChar, initialId, USER_ADMIN, TestSharingPermissionUtils.createPublicPermissions());
    }

    private void addDashboardPages(final List<DashboardPageInfo> items, final String name, final char startChar, final char endChar,
            final long initialId, final String user, final Set<TestSharingPermission> shares)
    {
        long currentId = initialId;

        for (char currentChar = startChar; currentChar <= endChar; currentChar++)
        {
            items.add(new DashboardPageInfo(currentId, name + currentChar, null, false, shares, user, 0, null));
            currentId++;
        }
    }

    private void assertNoMatch()
    {
        text.assertTextPresent(new WebPageLocator(tester), "Your search criteria did not match any dashboards.");
        TableLocator locator = new TableLocator(tester, Dashboard.Table.SEARCH.getTableId());
        assertNull("Search results should not be present.", locator.getNode());
    }

    /**
     * Iterate across the search results in the different search orders and make sure they are correct.
     *
     * @param expectedItems the items that should be on the search results.
     */
    private void assertSearchResults(final List<DashboardPageInfo> expectedItems)
    {
        //check the ascending table name.
        Collections.sort(expectedItems, NameComparator.NAME_COMPARATOR);

        checkNext(expectedItems);
        checkPrevious(expectedItems);

        //we should now change to descending.
        tester.clickLink("page_sort_name");

        Collections.sort(expectedItems, ComparatorUtils.reversedComparator(NameComparator.NAME_COMPARATOR));

        checkNext(expectedItems);
        checkPrevious(expectedItems);

        //we should now change author ascending.
        tester.clickLink("page_sort_owner");

        Collections.sort(expectedItems, createAuthorComparator(false));
        checkNext(expectedItems);
        checkPrevious(expectedItems);

        //we should now have descending order.
        tester.clickLink("page_sort_owner");
        Collections.sort(expectedItems, createAuthorComparator(true));
        checkNext(expectedItems);
        checkPrevious(expectedItems);

        //we should now change popularity descending.
        tester.clickLink("page_sort_popularity");

        Collections.sort(expectedItems, createPopularComparator(true));
        checkNext(expectedItems);
        checkPrevious(expectedItems);

        //we should now have popularity order ascending.
        tester.clickLink("page_sort_popularity");

        Collections.sort(expectedItems, createPopularComparator(false));
        checkNext(expectedItems);
        checkPrevious(expectedItems);
    }


    /**
     * Given the passed pages, move forward through the current search results and ensure that
     * the items are displayed correctly.
     *
     * @param items the pages to check.
     */
    private void checkNext(final List<DashboardPageInfo> items)
    {
        final int iterations = items.size() / PAGE_SIZE;
        final int remainder = items.size() % PAGE_SIZE;

        if (iterations == 0 || items.size() == PAGE_SIZE)
        {
            //there is not paging, then just check the results.
            tester.assertLinkNotPresentWithText(PREVIOUS_LINK_TEXT);
            tester.assertLinkNotPresentWithText(NEXT_LINK_TEXT);

            assertPageResults("All filters not on first screen.", items);
        }
        else
        {
            for (int i = 1; i <= iterations; i++)
            {
                final int startPos = (i - 1) * PAGE_SIZE;
                final int endPos = i * PAGE_SIZE;

                final List<DashboardPageInfo> expectedItems = items.subList(startPos, endPos);

                assertPageResults("Page " + i + " did not contain expected pages.", expectedItems);

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

                final List<DashboardPageInfo> expectedItems = items.subList(startPos, items.size());

                assertPageResults("Last page did not contain expected pages.", expectedItems);
            }
        }
    }


    /**
     * Given the passed pages, backward forward through the current search results and ensure that
     * the items are displayed correctly.
     *
     * @param items the pages to check.
     */
    private void checkPrevious(final List<DashboardPageInfo> items)
    {
        final int iterations = items.size() / PAGE_SIZE;
        final int remainder = items.size() % PAGE_SIZE;

        if (iterations == 0 || items.size() == PAGE_SIZE)
        {
            //there is not paging, then just check the results.
            tester.assertLinkNotPresentWithText(PREVIOUS_LINK_TEXT);
            tester.assertLinkNotPresentWithText(NEXT_LINK_TEXT);

            assertPageResults("All filters not on first screen.", items);
        }
        else
        {
            if (remainder != 0)
            {
                tester.assertLinkNotPresentWithText(NEXT_LINK_TEXT);

                final int startPos = iterations * PAGE_SIZE;

                tester.assertTextPresent("" + (startPos + 1) + " - " + items.size());

                final List<DashboardPageInfo> expectedItems = items.subList(startPos, items.size());
                assertPageResults("Last page did not contain expected pages.", expectedItems);

                tester.clickLinkWithText(PREVIOUS_LINK_TEXT);
            }

            for (int i = iterations; i > 0; i--)
            {
                final int startPos = (i - 1) * PAGE_SIZE;
                final int endPos = i * PAGE_SIZE;

                final List<DashboardPageInfo> expectedItems = items.subList(startPos, endPos);

                assertPageResults("Page " + i + " did not contain expected pages.", expectedItems);

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

    private void assertPageResults(final String errorMessage, final List<DashboardPageInfo> items)
    {
        try
        {
            assertions.getDashboardAssertions().assertDashboardPages(items, Dashboard.Table.SEARCH);
        }
        catch (AssertionFailedError e)
        {

            if (errorMessage != null)
            {
                AssertionFailedError error = new AssertionFailedError(errorMessage);
                error.initCause(e);

                throw error;
            }
            else
            {
                throw e;
            }
        }
    }

    private static class AuthorComparator implements Comparator<DashboardPageInfo>
    {
        public static final AuthorComparator AUTHOR_COMPARATOR = new AuthorComparator();

        private static String getName(final String authorName)
        {
            if ("System".equals(authorName))
            {
                return null;
            }
            else
            {
                return authorName;
            }
        }

        public int compare(final DashboardPageInfo o1, final DashboardPageInfo o2)
        {

            String author1 = getName(o1.getOwner());
            String author2 = getName(o2.getOwner());

            if (author1 == null)
            {
                if (author2 == null)
                {
                    return 0;
                }
                else
                {
                    return 1;
                }
            }
            else if (author2 == null)
            {
                return -1;
            }
            else
            {
                return author1.compareTo(author2);
            }
        }
    }

    private static class PopularComparator implements Comparator<DashboardPageInfo>
    {
        public static final PopularComparator POPULAR_COMPARATOR = new PopularComparator();

        public int compare(final DashboardPageInfo item1, final DashboardPageInfo item2)
        {
            return (int) (item1.getFavCount().longValue() - item2.getFavCount().longValue());
        }
    }

    private static class NameComparator implements Comparator<DashboardPageInfo>
    {
        public static final NameComparator NAME_COMPARATOR = new NameComparator();

        public int compare(final DashboardPageInfo item1, final DashboardPageInfo item2)
        {
            return item1.getName().compareToIgnoreCase(item2.getName());
        }
    }
}
