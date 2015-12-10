package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.admin.TimeTracking;
import com.atlassian.jira.functest.framework.assertions.IssueNavigatorAssertions;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.restclient.Response;

/**
 * Test switching from the advanced JQL view to the basic editing view
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestAdvancedSimple extends AbstractJqlFuncTest
{
    // errors with the JQL then switch to basic
    public void testJQLErrors() throws Exception
    {
        administration.restoreBlankInstance();
        administration.timeTracking().enable(TimeTracking.Mode.LEGACY);

        assertInvalidJqlAndSwitchToBasicDoesntFit("project = INVALID");
        assertInvalidJqlAndSwitchToBasicDoesntFit("issuetype = INVALID");
        assertInvalidJqlAndSwitchToBasic("text ~ \"*INVALID\"", "Invalid start character '*'", createFilterFormParam("text", "*INVALID"));
        assertInvalidJqlAndSwitchToBasicDoesntFit("status = INVALID");
        assertInvalidJqlAndSwitchToBasicDoesntFit("resolution = INVALID");
        assertInvalidJqlAndSwitchToBasicDoesntFit("priority = INVALID");
        assertInvalidJqlAndSwitchToBasic("created >= INVALID AND created <= INVALID", "Invalid date format. Please enter the date in the format", createFilterFormParam("created:after", "INVALID"), createFilterFormParam("created:before", "INVALID"));
        assertInvalidJqlAndSwitchToBasic("updated >= INVALID AND updated <= INVALID", "Invalid date format. Please enter the date in the format", createFilterFormParam("updated:after", "INVALID"), createFilterFormParam("updated:before", "INVALID"));
        assertInvalidJqlAndSwitchToBasic("due >= INVALID AND due <= INVALID", "Invalid date format. Please enter the date in the format", createFilterFormParam("duedate:after", "INVALID"), createFilterFormParam("duedate:before", "INVALID"));
        assertInvalidJqlAndSwitchToBasic("resolved >= INVALID AND resolved <= INVALID", "Invalid date format. Please enter the date in the format", createFilterFormParam("resolutiondate:after", "INVALID"), createFilterFormParam("resolutiondate:before", "INVALID"));
        assertInvalidJqlAndSwitchToBasic("workratio >= INVALID AND workratio <= INVALID", "The min limit must be specified using an integer", createFilterFormParam("workratio:min", "INVALID"), createFilterFormParam("workratio:max", "INVALID"));
    }

    public void testIdsAreTooComplex() throws Exception
    {
        administration.restoreData("TestSwitchingWithOneProject.xml");
        assertFitsFilterForm("fixVersion = \"New Version 1\"", createFilterFormParam("fixfor", "New Version 1"));
        assertTooComplex("fixVersion = 10000");

        assertFitsFilterForm("affectedVersion = \"New Version 1\"", createFilterFormParam("affectedVersion", "New Version 1"));
        assertTooComplex("affectedVersion = 10000");

        assertFitsFilterForm("component = \"New Component 1\"", createFilterFormParam("component", "New Component 1"));
        assertTooComplex("component = 10000");
    }
    
    private void assertInvalidJqlAndSwitchToBasic(final String invalidJqlQuery, String errorMessage, IssueNavigatorAssertions.FilterFormParam... params)
    {
        Response searchersResponse = backdoor.searchersClient().getSearchersResponse(invalidJqlQuery);
        assertEquals(200, searchersResponse.statusCode);
    }



    // given an invalid JQL query, execute it, verify there were errors, switch to
    // basic view, and make sure no form elements are filled in
    private void assertInvalidJqlAndSwitchToBasicDoesntFit(final String invalidJqlQuery)
    {
        assertTooComplex(invalidJqlQuery);
    }
}
