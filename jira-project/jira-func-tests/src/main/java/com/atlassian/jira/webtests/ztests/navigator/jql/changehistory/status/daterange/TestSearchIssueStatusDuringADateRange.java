package com.atlassian.jira.webtests.ztests.navigator.jql.changehistory.status.daterange;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.navigator.jql.changehistory.AbstractChangeHistoryFuncTest;
import org.junit.Ignore;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * Responsible for verifying that a user is able to query all the issues that were in a particular status before a date.
 *
 * @see <a href="https://jdog.atlassian.com/browse/JRADEV-3740">User Story [JRADEV-3740]</a>
 * @since v4.4
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL, Category.CHANGE_HISTORY })
public class TestSearchIssueStatusDuringADateRange extends AbstractChangeHistoryFuncTest
{
    private static final String FIELD_NAME = "status";

    @Override
    protected void setUpTest()
    {
        administration.restoreData("TestSearchIssueStatusDuringADateRange.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
    }

    public void testInvalidDuringInput()
    {
        String expectedError = "The DURING predicate must be supplied with exactly 2 date values.";
        super.assertInvalidSearchProducesError(FIELD_NAME, "Closed", "DURING ('2011-07-01')", expectedError);
        super.assertInvalidSearchProducesError(FIELD_NAME, "Closed", "DURING ('2011-07-01','2011-07-03','2012-07-03')", expectedError);
    }

    public void testReturnsIssuesThatWereInTheStatusGivenThatTheyTransitionedToThatStatusDuringTheDateRange()
    {
        navigation.
                issueNavigator().createSearch
                (
                        "project=svd and status was 'In Progress' DURING('2009-06-10','2009-08-12') order by key"
                );
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("SVD-1","SVD-3","SVD-5");
    }

    public void testReturnsIssuesThatWereInTheStatusGivenThatTheyTransitionedToThatStatusPriorToTheDateRange()
    {
        navigation.
                issueNavigator().createSearch
                (
                        "project=svd and status was 'In Progress' DURING('2010-06-10','2010-08-12') order by key"
                );
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("SVD-2","SVD-4","SVD-5");
    }

    public void testReturnsIssuesThatWereInTheStatusGivenThatTheyHaveNotTransitionedFromTheInitialStatusValue()
    {
        navigation.
                issueNavigator().createSearch
                (
                        "project=svd and status was 'Open' DURING('2008-12-01','2008-12-31') order by key"
                );
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("SVD-6");
    }

    public void testDoesNotReturnIssuesThatWereNotInTheStatusGivenThatTheyTransitionedToThatStatusOutsideOfTheDateRange()
    {
        navigation.
                issueNavigator().createSearch
                (
                        "project=svd and status was 'Closed' DURING('2010-06-10','2010-08-12') order by key"
                );
        assertions.getIssueNavigatorAssertions().assertSearchResultsDoNotContain("SVD-7", "SVD-8");
    }

    public void testDoesNotReturnIssuesThatWereNeverInThatStatus()
    {
        navigation.
                issueNavigator().createSearch
                (
                        "project=svd and status was 'Closed' DURING('2010-06-10','2010-08-12') order by key"
                );
        assertions.getIssueNavigatorAssertions().assertSearchResultsDoNotContain("SVD-9","SVD-10", "SVD-11");
    }

    public void testReturnsIssuesThatTransitionedToThatStatusAtOrJustAfterTheBeginningOfTheDateRange()
    {
        navigation.
                issueNavigator().createSearch
                (
                        "project=svd and status was 'Resolved' DURING('2010-06-14 05:25','2010-07-12') order by key"
                );
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("SVD-12","SVD-13");
    }

    public void testReturnsIssuesThatTransitionedToThatStatusAtOrJustBeforeTheEndOfTheDateRange()
    {
        navigation.
                issueNavigator().createSearch
                (
                        "project=svd and status was 'Closed' DURING('2010-07-12', '2010-08-12 08:12') order by key"
                );
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("SVD-14","SVD-15");
    }

    @Ignore("This does not work at the moment -- The bug is being tracked @ JRADEV-6108")
    public void testDoesNotReturnIssuesThatWereInThatStatusJustBeforeOrAfterTheEndOfTheDateRange()
    {
        navigation.
                issueNavigator().createSearch
                (
                        "project=svd and status was 'Resolved' DURING('2010-06-10 05:25', '2010-08-12 08:12') order by key"
                );
        assertions.getIssueNavigatorAssertions().assertSearchResultsDoNotContain("SVD-16","SVD-17");
    }
}
