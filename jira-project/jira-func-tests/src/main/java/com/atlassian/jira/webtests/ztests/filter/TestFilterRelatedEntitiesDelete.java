package com.atlassian.jira.webtests.ztests.filter;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.admin.user.DeleteUserPage;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.navigation.FilterNavigation;
import com.atlassian.jira.functest.framework.parser.filter.FilterItem;
import com.atlassian.jira.functest.framework.parser.filter.FilterList;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.util.List;

/**
 * Test the deleting of entities realted to fitlers and ensure that shares disspear
 *
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.FILTERS })
public class TestFilterRelatedEntitiesDelete extends FuncTestCase
{
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("sharedfilters/TestFilterRelatedEntitiesDelete.xml");
    }

    /**
     * Deletes a group and make sure its shares are gone
     */
    public void testEntityDelete()
    {
        final FilterNavigation filterNavigation = navigation.manageFilters();

        //
        // delete a group and make sure the filter sharing is cleaned up
        tester.gotoPage("secure/admin/user/DeleteGroup!default.jspa?name=group_delete_me");
        tester.submit("Delete");

        filterNavigation.myFilters();
        FilterList filterList = parse.filter().parseFilterList("mf_owned");
        assertFilterSharingIsPrivate(filterList, "shared with group");

        //
        // delete a project role and make sure the filter sharing is cleaned up
        tester.gotoPage("secure/project/DeleteProjectRole!default.jspa?id=10003"); // role_delete_me
        text.assertTextPresent(new WebPageLocator(tester), "role_delete_me");
        tester.submit("Delete");

        filterNavigation.myFilters();
        filterList = parse.filter().parseFilterList("mf_owned");
        assertFilterSharingIsPrivate(filterList, "shared  with project role"); // oops space deliberate

        //
        // delete a project and make sure the filter sharing is cleaned up
        tester.gotoPage("secure/project/DeleteProject!default.jspa?pid=10001&returnUrl=ViewProjects.jspa"); // deleteme project
        text.assertTextPresent(new WebPageLocator(tester), "deleteme");
        tester.submit("Delete");

        filterNavigation.myFilters();
        filterList = parse.filter().parseFilterList("mf_owned");
        assertFilterSharingIsPrivate(filterList, "shared with project");
    }

    public void testUserDelete() throws Exception
    {
        final FilterNavigation filterNavigation = navigation.manageFilters();
        //
        // the data is loaded so that admin has a favourited filter created by user_can_share_filters
        filterNavigation.goToDefault();
        assertFilterIsInList(parse.filter().parseFilterList("mf_favourites"), "shared by user_can_share_filters");

        navigation.gotoAdmin();
        DeleteUserPage deleteUserPage = navigation.gotoPageWithParams(DeleteUserPage.class, DeleteUserPage.generateDeleteQueryParameters("user_can_share_filters"));
        deleteUserPage.clickDeleteUser();

        filterNavigation.goToDefault();
        assertFilterIsNotInList(parse.filter().parseFilterList("mf_favourites"), "shared by user_can_share_filters");
    }

    private void assertFilterSharingIsPrivate(FilterList filterList, String filterName)
    {
        final List<FilterItem> filterItems = filterList.getFilterItems();
        for (FilterItem filterItem : filterItems)
        {
            final String name = filterItem.getName();
            if (name.equals(filterName))
            {
                // ensure it has private sharing
                List sharing = filterItem.getSharing();
                assertEquals(0, sharing.size());
                return;
            }
        }
        fail("Couldnt find named fitler in list : " + filterName);
    }

    private void assertFilterIsNotInList(FilterList filterList, String filterName)
    {
        final List<FilterItem> filterItems = filterList.getFilterItems();
        for (FilterItem filterItem : filterItems)
        {
            if (filterItem.getName().equals(filterName))
            {
                fail("This filter '" + filterName + "' should not exist in the filter list");
            }
        }
    }

    private void assertFilterIsInList(FilterList filterList, String filterName)
    {
        final List<FilterItem> filterItems = filterList.getFilterItems();
        for (FilterItem filterItem : filterItems)
        {
            if (filterItem.getName().equals(filterName))
            {
                return;
            }
        }
        fail("Failed to find filter in list : " + filterName);
    }

}
