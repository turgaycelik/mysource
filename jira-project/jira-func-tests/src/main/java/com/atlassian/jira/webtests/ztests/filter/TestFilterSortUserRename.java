package com.atlassian.jira.webtests.ztests.filter;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.meterware.httpunit.WebTable;

/**
 * Test to ensure that a user's name is indexed rather than their full name.
 *
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.FILTERS })
public class TestFilterSortUserRename extends FuncTestCase
{
    public void testRenameUserSortChanges()
    {
        administration.restoreData("sharedfilters/TestFilterSharing.xml");
        navigation.manageFilters().searchFilters();
        tester.setFormElement("searchName", "");
        tester.submit("Search");
        tester.clickLink("filter_sort_owner");

        WebTable mf_browse = tester.getDialog().getWebTableBySummaryOrId("mf_browse");
        assertTrue("Cell (1, 0) in table 'mf_browse' should be '", mf_browse.getCellAsText(1, 0).trim().contains("All Projects All Issues"));
        assertTrue("Cell (2, 0) in table 'mf_browse' should be '", mf_browse.getCellAsText(2, 0).trim().contains("PublicBugShare"));

        // edit the user
        navigation.gotoAdminSection("user_browser");
        tester.clickLink("edituser_link_user_can_share_filters");
        tester.setFormElement("fullName", "Aamon Buchanan");
        tester.submit("Update");

        //go back and search again
        navigation.issueNavigator().displayAllIssues();
        navigation.manageFilters().searchFilters();
        tester.submit("Search");
        tester.clickLink("filter_sort_owner");

        mf_browse = tester.getDialog().getWebTableBySummaryOrId("mf_browse");
        assertTrue("Cell (1, 0) in table 'mf_browse' should be '", mf_browse.getCellAsText(1, 0).trim().contains("PublicBugShare"));
        assertTrue("Cell (2, 0) in table 'mf_browse' should be '", mf_browse.getCellAsText(2, 0).trim().contains("All Projects All Issues"));
    }
}
