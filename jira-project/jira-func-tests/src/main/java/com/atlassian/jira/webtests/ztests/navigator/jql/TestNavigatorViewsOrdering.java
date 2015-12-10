package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.assertions.IssueNavigatorAssertions;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.navigator.ContainsIssueKeysCondition;
import com.atlassian.jira.functest.framework.navigator.SearchResultsCondition;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.util.ArrayList;
import java.util.List;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * @since v4.0.1
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestNavigatorViewsOrdering extends AbstractJqlFuncTest
{

    // JRA-19531 - order by's were dropped
    public void testPrintableViewFilterOrderByRespected() throws Exception
    {
        administration.restoreData("TestNavigatorViewsSorting.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        // load filter 'order asc'
        navigation.issueNavigator().loadFilter(10000);
        tester.gotoPage("/sr/jira.issueviews:searchrequest-printable/10000/SearchRequest-10000.html?tempMax=1000");
        // Verify the ordering is correct
        List<SearchResultsCondition> conditions = new ArrayList<SearchResultsCondition>();
        conditions.add(new ContainsIssueKeysCondition(assertions.getTextAssertions(), "HSP-3", "HSP-2", "HSP-1"));
        IssueNavigatorAssertions issueNavAssert = assertions.getIssueNavigatorAssertions();
        issueNavAssert.assertSearchResults(conditions);

        // load filter 'order desc'
        navigation.issueNavigator().loadFilter(10001);
        tester.gotoPage("/sr/jira.issueviews:searchrequest-printable/10001/SearchRequest-10001.html?tempMax=1000");

        // Verify the ordering is correct
        conditions = new ArrayList<SearchResultsCondition>();
        conditions.add(new ContainsIssueKeysCondition(assertions.getTextAssertions(), "HSP-1", "HSP-2", "HSP-3"));
        issueNavAssert.assertSearchResults(conditions);
    }
}
