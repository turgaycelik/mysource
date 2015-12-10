package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.meterware.httpunit.WebResponse;

/**
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestQueryTooComplex extends FuncTestCase
{
    private static final int CLAUSES = 70000;

    private String reallyBloodyLongQuery;
    private String statusjQlQuery;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        reallyBloodyLongQuery = createLongQuery();
        statusjQlQuery="status was Open";
    }

    private String createLongQuery()
    {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("comment ~ monkey");
        for (int i = 0; i < CLAUSES; ++i)
        {
            stringBuilder.append(" and comment ~ ").append(i);
        }
        return stringBuilder.toString();
    }

    public void testSearchRequestView() throws Exception
    {
        administration.restoreData("TestQueryTooComplex.xml");
        viewSearchRequestViewForSearchFilter(10000, 400, "One of the clauses in your search matches too many results. If a clause returns too many results, the entire search will fail. Please try refining or removing the clauses in your search and run it again.");
    }

    private void viewSearchRequestViewForSearchFilter(int filterId, int responseCode, String errorMessage)
    {
        try
        {
            tester.getDialog().getWebClient().setExceptionsThrownOnErrorStatus(false);
            tester.gotoPage("/sr/jira.issueviews:searchrequest-xml/"+ filterId +"/SearchRequest-" + filterId + ".xml?tempMax=1000");
            final WebResponse response = tester.getDialog().getResponse();
            assertEquals(responseCode, response.getResponseCode());
            tester.assertTextPresent(errorMessage);

        }
        finally
        {
            tester.getDialog().getWebClient().setExceptionsThrownOnErrorStatus(true);
        }
    }

    public void testChangeHistoryJqlTooComplex() throws Exception
      {
          administration.restoreBlankInstance();
          issueTableAssertions.assertSearchTooComplex(statusjQlQuery);
      }

    
}
