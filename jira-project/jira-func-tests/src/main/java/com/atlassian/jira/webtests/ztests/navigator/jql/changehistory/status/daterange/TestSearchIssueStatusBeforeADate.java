package com.atlassian.jira.webtests.ztests.navigator.jql.changehistory.status.daterange;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.navigator.jql.changehistory.AbstractChangeHistoryFuncTest;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * Responsible for verifying that a user is able to query all the issues that were in a particular status bafore a date.
 *
 * @see <a href="https://jdog.atlassian.com/browse/JRADEV-3740">User Story [JRADEV-3740]</a>
 * @since v4.4
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL, Category.CHANGE_HISTORY })
public class TestSearchIssueStatusBeforeADate extends AbstractChangeHistoryFuncTest
{

    private static final String FIELD_NAME = "status";

    @Override
    protected void setUpTest()
    {
        administration.restoreData("TestSearchIssueStatusBeforeADate.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
    }

    public void testInvalidBeforeInput()
    {
        String expectedError = "The BEFORE predicate must be supplied with only 1 date value.";
        super.assertInvalidSearchProducesError(FIELD_NAME, "Closed", "BEFORE ('2011-07-01','2011-07-01')", expectedError);
        super.assertInvalidSearchProducesError(FIELD_NAME, "Closed", "BEFORE ('2011-07-01','2011-07-03','2012-07-03')", expectedError);
    }


    public void testReturnsTheIssuesThatWereInTheStatusGivenThatTheIssuesTransitionedInAndOutOfTheStatusBeforeTheDate()
    {
        navigation.issueNavigator().createSearch("project=svb and status was 'in progress' before '2011-02-21 10:30' order by key");
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("SVB-1");
    }

    public void testReturnsTheIssuesThatWereInTheStatusGivenThatTheIssuesTransitionedDirectlyToTheStatusBeforeTheDate()
    {
        navigation.issueNavigator().createSearch("project=svb and status was 'resolved' before '2011-02-21 11:30' order by key");
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("SVB-3", "SVB-4", "SVB-5");
    }

    public void testDoesNotReturnIssuesThatWereNeverInThatStatus()
    {
        navigation.issueNavigator().createSearch("project=svb and status was 'closed' before '2011-02-21 10:30' order by key");
    }

    public void testDoesNotReturnIssuesThatTransitionedToTheSpecifiedStatusAtTheDate()
    {
        navigation.issueNavigator().createSearch("project=svb and status was 'resolved' before '2011-02-21 10:30' order by key");
        assertions.getIssueNavigatorAssertions().assertSearchResultsDoNotContain("SVB-4");
    }

    public void testDoesNotReturnIssuesThatTransitionedToTheSpecifiedStatuesJustAfterTheDate()
    {
        navigation.issueNavigator().createSearch("project=svb and status was 'resolved' before '2011-02-21 10:30' order by key");
        assertions.getIssueNavigatorAssertions().assertSearchResultsDoNotContain("SVB-5");
    }

    public void testReturnsIssuesThatTransitionedToTheSpecifiedStatusJustBeforeTheDate()
    {
        navigation.issueNavigator().createSearch("project=svb and status was 'resolved' before '2011-02-21 10:30' order by key");
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("SVB-3");
    }

    public void testReturnsIssuesThatTransitionedToTheSpecifiedStatusBeforeTheDateWhenWeHaveOneOrMoreStatii()
    {
        navigation.issueNavigator().createSearch("project=svb and status was in ('Closed') before '2011-03-29' order by key");
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("SVB-2");

        navigation.issueNavigator().createSearch("project=svb and status was in ('In Progress', 'Closed') before '2011-03-29' order by key");
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("SVB-1","SVB-2");

        // we can have multiple values here
        navigation.issueNavigator().createSearch("project=svb and status was in ('In Progress', 'Closed', 'Resolved') before '2011-03-29' order by key");
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("SVB-1","SVB-2","SVB-3","SVB-4","SVB-5");
    }
}
