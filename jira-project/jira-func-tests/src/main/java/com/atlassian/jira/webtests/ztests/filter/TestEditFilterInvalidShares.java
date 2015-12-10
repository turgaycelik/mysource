package com.atlassian.jira.webtests.ztests.filter;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.navigator.IssueTypeCondition;
import com.atlassian.jira.functest.framework.navigator.NavigatorSearch;
import com.atlassian.jira.functest.framework.navigator.NavigatorSearchBuilder;
import com.atlassian.jira.functest.framework.sharing.GlobalTestSharingPermission;
import com.atlassian.jira.functest.framework.sharing.GroupTestSharingPermission;
import com.atlassian.jira.functest.framework.sharing.ProjectTestSharingPermission;
import com.atlassian.jira.functest.framework.sharing.SharedEntityInfo;
import com.atlassian.jira.functest.framework.sharing.TestSharingPermission;
import com.atlassian.jira.functest.framework.sharing.TestSharingPermissionUtils;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.framework.util.json.TestJSONException;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Test for invalid share permissions.
 *
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.FILTERS })
public class TestEditFilterInvalidShares extends FuncTestCase
{
    private static final Pattern FILTER_PATTERN = Pattern.compile("filter=(\\d+)");

    private static final String NO_SHARE_USER = "user_cant_share_filters";
    private static final String SHARE_USER = "user_can_share_filters";

    private static final int GROUP_FILTER_ID = 10020;
    private static final SharedEntityInfo GROUP_FILTER_INFO = new SharedEntityInfo("Group Removed Search", "A search where the group share will be removed.", true, null);

    private static final int PROJECT_FILTER_ID = 10021;
    private static final SharedEntityInfo PROJECT_FILTER_INFO = new SharedEntityInfo("Browse permission Removed", "A search where the browse permission has been removed.", true, null);

    private static final int ROLE_FILTER_ID = 10022;
    private static final SharedEntityInfo ROLE_FILTER_INFO = new SharedEntityInfo("Remove From Role Filter", null, true, null);

    private static final int REMOVE_FILTER_ID = 10023;
    private static final SharedEntityInfo REMOVE_FILTER_INFO = new SharedEntityInfo("Remove Share Permission", null, true, null);

    private static final int MULTIPLE_FILTER_ID = 10024;
    private static final SharedEntityInfo MULTIPLE_FILTER_INFO = new SharedEntityInfo("Multiple Invalid Shares", null, true, null);

    private static final NavigatorSearch SHARE_SEARCH;
    private static final NavigatorSearch NOSHARE_SEARCH;
    private static final GroupTestSharingPermission GROUP_SHARE = new GroupTestSharingPermission("jira-users");
    private static final String EDITSHARES_LINK_ID = "filtereditshares";

    static
    {
        NavigatorSearchBuilder builder = new NavigatorSearchBuilder();
        builder.addIssueType(IssueTypeCondition.IssueType.BUG).addIssueType(IssueTypeCondition.IssueType.IMPROVEMENT);
        builder.addQueryString("SimpleTest");

        SHARE_SEARCH = builder.createSearch();

        builder = new NavigatorSearchBuilder();
        builder.addIssueType(IssueTypeCondition.IssueType.BUG).addProject("deleteme (DEL)");

        NOSHARE_SEARCH = builder.createSearch();



    }

    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("sharedfilters/TestInvalidFilterSharing.xml");

    }

    /**
     * Test to check that you can't save filter if you are removed from its associated group.
     */
    public void testEditFilterRemoveGroup()
    {
        navigation.login(SHARE_USER);

        navigation.issueNavigator().editFilter(GROUP_FILTER_ID);

        text.assertRegexMatch(new WebPageLocator(tester), "You do not have permission to share with Group\\: '.+'\\.");

        tester.submit("Save");
        text.assertRegexMatch(new WebPageLocator(tester), "You do not have permission to share with Group\\: '.+'\\.");
        assertEditScreenCorrect(GROUP_FILTER_INFO, ImmutableSet.of(new GroupTestSharingPermission("group_delete_me")));

        long id = editFilter(GROUP_FILTER_ID, GROUP_FILTER_INFO, Collections.<TestSharingPermission>emptySet());
        assertEquals(GROUP_FILTER_ID, id);

        assertSearchEditCorrectly(id, SHARE_USER, GROUP_FILTER_INFO, SHARE_SEARCH, Collections.<TestSharingPermission>emptySet());
    }

    /**
     * Test to make sure you can no longer share with a project that you don't have browse permission with.
     */
    public void testEditFilterRemoveBrowse()
    {
        navigation.login(SHARE_USER);

        navigation.issueNavigator().editFilter(PROJECT_FILTER_ID);

        text.assertRegexMatch(new WebPageLocator(tester), "You do not have permission to share with Project\\: '.+'\\.");

        tester.submit("Save");
        text.assertRegexMatch(new WebPageLocator(tester), "You do not have permission to share with Project\\: '.+'\\.");
        assertEditScreenCorrect(PROJECT_FILTER_INFO, ImmutableSet.of(new ProjectTestSharingPermission(10002)));

        final Set<? extends TestSharingPermission> pub = ImmutableSet.of(GlobalTestSharingPermission.GLOBAL_PERMISSION);

        long id = editFilter(PROJECT_FILTER_ID, PROJECT_FILTER_INFO, pub);
        assertEquals(PROJECT_FILTER_ID, id);

        assertSearchEditCorrectly(id, SHARE_USER,  PROJECT_FILTER_INFO, SHARE_SEARCH, pub);
    }

    /**
     * Test to make sure you can no longer share with a role that you no longer belong to.
     */
    public void testEditFilterRemoveRole()
    {
        final Set<TestSharingPermission> rolePerms = new HashSet<TestSharingPermission>();
        rolePerms.add(new ProjectTestSharingPermission(10000, 10002));
        rolePerms.add(new ProjectTestSharingPermission(10000, 10001));

        navigation.login(SHARE_USER);

        navigation.issueNavigator().editFilter(ROLE_FILTER_ID);

        text.assertRegexMatch(new WebPageLocator(tester), "You do not have permission to share with Project\\: '.+'\\ Role\\: '.+'\\.");

        tester.submit("Save");
        text.assertRegexMatch(new WebPageLocator(tester), "You do not have permission to share with Project\\: '.+'\\ Role\\: '.+'\\.");
        assertEditScreenCorrect(ROLE_FILTER_INFO, rolePerms);

        long id = editFilter(ROLE_FILTER_ID, ROLE_FILTER_INFO, ImmutableSet.of(GROUP_SHARE));
        assertEquals(ROLE_FILTER_ID, id);

        assertSearchEditCorrectly(id, SHARE_USER, ROLE_FILTER_INFO, SHARE_SEARCH, ImmutableSet.of(GROUP_SHARE));
    }

    /**
     * Test to see you can no longer share when you don't have global sharing permission.
     */
    public void testEditFilterGlobalShareRemove()
    {
        navigation.login(NO_SHARE_USER);

        navigation.issueNavigator().editFilter(REMOVE_FILTER_ID);

        text.assertTextPresent(new WebPageLocator(tester), "You do not have permission to share. All shares are invalid.");

        tester.submit("Save");
        text.assertTextPresent(new WebPageLocator(tester), "You do not have permission to share. All shares are invalid.");
        assertEditScreenCorrect(REMOVE_FILTER_INFO, ImmutableSet.of(GlobalTestSharingPermission.GLOBAL_PERMISSION));

        long id = editFilter(REMOVE_FILTER_ID, REMOVE_FILTER_INFO, Collections.<TestSharingPermission>emptySet());
        assertEquals(REMOVE_FILTER_ID, id);

        assertSearchEditCorrectly(id, NO_SHARE_USER, REMOVE_FILTER_INFO, NOSHARE_SEARCH, null);
    }

    /**
     * Test to see if multiple errors are handled.
     */
    public void testEditFilterMultipleProblems()
    {
        navigation.login(SHARE_USER);

        final List<TestSharingPermission> shares = new LinkedList<TestSharingPermission>();
        shares.add(new GroupTestSharingPermission("group_delete_me"));
        shares.add(new ProjectTestSharingPermission(10000, 10001));
        shares.add(new ProjectTestSharingPermission(10000));

        final SharedEntityInfo testinfo = new SharedEntityInfo("testEditFilterMultipleProblems", "description", false, null);

        navigation.issueNavigator().editFilter(MULTIPLE_FILTER_ID);
        text.assertTextPresent(new WebPageLocator(tester), "You do not have permission to share");
        tester.submit("Save");
        text.assertTextPresent(new WebPageLocator(tester), "You do not have permission to share");
        assertEditScreenCorrect(MULTIPLE_FILTER_INFO, new HashSet<TestSharingPermission>(shares));

        shares.remove(0);

        editFilterNoId(MULTIPLE_FILTER_ID, testinfo, new HashSet<TestSharingPermission>(shares));
        text.assertTextPresent(new WebPageLocator(tester), "You do not have permission to share");
        assertEditScreenCorrect(testinfo, new HashSet<TestSharingPermission>(shares));

        shares.remove(0);

        long id = editFilter(MULTIPLE_FILTER_ID, MULTIPLE_FILTER_INFO, new HashSet<TestSharingPermission>(shares));
        assertEquals(MULTIPLE_FILTER_ID, id);

        assertSearchEditCorrectly(id, SHARE_USER, MULTIPLE_FILTER_INFO, SHARE_SEARCH, new HashSet<TestSharingPermission>(shares));
    }

    /**
     * Edit the filter directly using a GET. This gets around the problem where JWebUnit cannot change hidden
     * fields.
     *
     * @param id          the id of the filter to save.
     * @param name        the name of the search.
     * @param description the description of the search.
     * @param favourite   should the filter be saved as a favourite.
     * @param permissions the permissions to save.
     * @return the identifier of the search just edited.
     */
    private long editFilter(final long id, final String name, final String description, final boolean favourite,
            final Set<? extends TestSharingPermission> permissions)
    {
        editFilterNoId(id, name, description, favourite, permissions);
        return getFilterIdAfterEdit();
    }

    /**
     * Edit the filter directly using a GET. This gets around the problem where JWebUnit cannot change hidden
     * fields.
     *
     * @param id          the id of the filter to save.
     * @param info        the information used to save the search.
     * @param permissions the permissions to save.
     * @return the identifier of the search just edited.
     */
    private long editFilter(final long id, final SharedEntityInfo info, final Set<? extends TestSharingPermission> permissions)
    {
        return editFilter(id, info.getName(), info.getDescription(), info.isFavourite(), permissions);
    }

    /**
     * Edit the filter directly using a GET. This gets around the problem where JWebUnit cannot change hidden
     * fields.
     *
     * @param id          the id of the filter to save.
     * @param info        the information used to save the search.
     * @param permissions the permissions to save.
     */
    private void editFilterNoId(final long id, final SharedEntityInfo info, final Set<? extends TestSharingPermission> permissions)
    {
        editFilterNoId(id, info.getName(), info.getDescription(), info.isFavourite(), permissions);
    }

    /**
     * Edit the filter directly using a GET. This gets around the problem where JWebUnit cannot change hidden
     * fields.
     *
     * @param id          the id of the filter to save.
     * @param name        the name of the search.
     * @param description the description of the search.
     * @param favourite   should the filter be saved as a favourite.
     * @param permissions the permissions to save.
     */
    private void editFilterNoId(final long id, final String name, final String description, final boolean favourite,
            final Set <? extends TestSharingPermission> permissions)
    {
        tester.gotoPage(createEditUrl(id, name, description, favourite, permissions));
    }

    /**
     * Create the URL that can be used to perform a filter edit.
     *
     * @param id          the id of the filter to save.
     * @param name        the name of the search.
     * @param description the description of the search.
     * @param favourite   should the filter be saved as a favourite.
     * @param permissions the permissions to save.
     * @return the URL for the filter save.
     */
    private String createEditUrl(final long id, final String name, final String description,
            final boolean favourite, final Set<? extends TestSharingPermission> permissions)
    {
        StringBuilder buffer = new StringBuilder("secure/EditFilter.jspa?submit=Save&filterId=" + id);
        if (!StringUtils.isBlank(name))
        {
            buffer.append("&filterName=").append(encode(name));
        }
        if (!StringUtils.isBlank(description))
        {
            buffer.append("&filterDescription=").append(encode(description));
        }
        if (permissions != null)
        {
            buffer.append("&shareValues=").append(encode(TestSharingPermissionUtils.createJsonString(permissions)));
        }
        buffer.append("&favourite=").append(String.valueOf(favourite));
        return page.addXsrfToken(buffer.toString());
    }

    /**
     * HTML encode the argument.
     *
     * @param data string to encode.
     * @return the encoded string.
     */
    private String encode(String data)
    {
        try
        {
            return URLEncoder.encode(data, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parse the URL after the filter has been edited to find the filter id.
     *
     * @return the filter
     */
    private long getFilterIdAfterEdit()
    {
        URL url = tester.getDialog().getResponse().getURL();
        if (url.getQuery() == null)
        {
            fail("Unable to save filter: Not redirected to navigator.");
        }
        else
        {
            if (url.getPath() == null || !url.getPath().contains("/issues"))
            {
                fail("Unable to save filter: Not redirected to navigator.");
            }
            else
            {
                Matcher matcher = FILTER_PATTERN.matcher(url.getQuery());
                if (matcher.find())
                {
                    return Long.parseLong(matcher.group(1));
                }
                else
                {
                    fail("Unable to save filter: Not redirected to navigator.");
                }
            }
        }
        return Long.MIN_VALUE;
    }

    /**
     * Assert that the current search matches the passed search.
     *
     * @param info                the search information to check.
     * @param expectedSearch      the search to check.
     * @param expectedPermissions the permissions that should be associated with the search.
     */
    private void assertSearchEditCorrectly(final long filter, String user, final SharedEntityInfo info, final NavigatorSearch expectedSearch,
            final Set <? extends TestSharingPermission> expectedPermissions)
    {
        issueTableAssertions.assertSimpleSearch(user, filter, expectedSearch);
        issueTableAssertions.assertSearchInfo(filter, info);

        //we need to goto this page to see the shares.
        tester.gotoPage("secure/EditFilter!default.jspa");
        assertEquals("Expected and actual permissions did not match.", expectedPermissions, parsePermissions());
    }

    /**
     * Assert that the page contains the correct values on error.
     *
     * @param expectedInfo the information that should currently be on the edit screen.
     * @param expectedPermissions the permission that should be one the screen.
     */
    private void assertEditScreenCorrect(final SharedEntityInfo expectedInfo,
            final Set <? extends TestSharingPermission> expectedPermissions)
    {
        tester.assertFormElementEquals("filterName", expectedInfo.getName());
        tester.assertFormElementEquals("filterDescription", expectedInfo.getDescription());
        tester.assertFormElementEquals("favourite", String.valueOf(expectedInfo.isFavourite()));
        assertEquals(expectedPermissions, parsePermissions());
    }


    /**
     * Return the current share permissions for the filter in the session.
     *
     * @return the permissions for the current filter.
     */
    private Set <TestSharingPermission> parsePermissions()
    {
        Locator xpath = new XPathLocator(tester, "//span[@id='shares_data']");
        String value = xpath.getText();
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
