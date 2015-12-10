package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.meterware.httpunit.WebResponse;

/**
 * A test that verifies that a SearchRequestView (e.g. XML/RSS) of a search filter responses with a 400 http error code if the JQL is invalid.
 *
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestInvalidSearchFilterSearchRequestView extends AbstractJqlFuncTest
{
    @Override
    protected void setUpTest()
    {
        administration.restoreData("TestInvalidSearchFilterSearchRequestView.xml");
    }

    public void testInvalidSearchFilterSearchRequestView() throws Exception
    {
        navigation.login(FRED_USERNAME);
        viewSearchRequestViewForSearchFilter(10053, 400, "A value with ID '10000' does not exist for the field 'ProjectPicker'.");
        viewSearchRequestViewForSearchFilter(10052, 400, "A value with ID '10000' does not exist for the field 'ProjectPicker'.");
        viewSearchRequestViewForSearchFilter(10051, 400, "A value with ID '10000' does not exist for the field 'ProjectPicker'.");
        viewSearchRequestViewForSearchFilter(10050, 400, "A value with ID '10000' does not exist for the field 'ProjectPicker'.");
        viewSearchRequestViewForSearchFilter(10047, 400, "A value with ID '10000' does not exist for the field 'Project'.");
        viewSearchRequestViewForSearchFilter(10046, 400, "A value with ID '10000' does not exist for the field 'Project'.");
        viewSearchRequestViewForSearchFilter(10045, 400, "A value with ID '10000' does not exist for the field 'Project'.");
        viewSearchRequestViewForSearchFilter(10044, 400, "A value with ID '10000' does not exist for the field 'Project'.");
    }

    private void viewSearchRequestViewForSearchFilter(final int filterId, final int responseCode, final String errorMessage)
    {
        try
        {
            // /sr/jira.issueviews:searchrequest-xml/10053/SearchRequest-10053.xml?tempMax=1000
            tester.getDialog().getWebClient().setExceptionsThrownOnErrorStatus(false);
            tester.gotoPage("/sr/jira.issueviews:searchrequest-xml/"+ filterId +"/SearchRequest-" + filterId + ".xml?tempMax=1000");
            final WebResponse response = tester.getDialog().getResponse();
            assertEquals(responseCode, response.getResponseCode());
            assertions.html().assertResponseContains(tester, errorMessage);
        }
        finally
        {
            tester.getDialog().getWebClient().setExceptionsThrownOnErrorStatus(true);
        }
    }
}
