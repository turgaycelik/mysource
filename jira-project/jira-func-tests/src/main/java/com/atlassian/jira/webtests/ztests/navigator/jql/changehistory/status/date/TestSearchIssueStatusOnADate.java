package com.atlassian.jira.webtests.ztests.navigator.jql.changehistory.status.date;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.navigator.jql.changehistory.AbstractChangeHistoryFuncTest;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * Responsible for verifying that a user is able to query all the issues that were in a particular status on an
 * specified date. <p/> <p>A date is defined as the date-time range from <em>[YYYY-MM-DD 00:00, YYYY-MM-DD
 * 23:59]</em>.</p> <p/> <p>The query can be specified as follows: <dl> <dt>ON <em>date_value</em></dt> <dd> <p>Defines
 * a date-time range from <em>[date_value 00:00, date_value 23:59]</em></p> <p>Example JQL query: <code>WAS resolved ON
 * '2011-05-25'</code></p> </dd> </dl> </p>
 *
 * @see <a href="https://jdog.atlassian.com/browse/JRADEV-3739">User Story [JRADEV-3739]</a>
 * @since v4.4
 */
@WebTest ( { Category.FUNC_TEST, Category.JQL, Category.CHANGE_HISTORY })
public class TestSearchIssueStatusOnADate extends AbstractChangeHistoryFuncTest
{
    @Override
    protected void setUpTest()
    {
        administration.restoreData("TestSearchIssueStatusOnADate.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
    }

    public void testShouldReturnIssuesThatWereInThatStatusGivenThatTheIssuesTransitionedToThatStatusDuringTheDate()
    {
        navigation.
                issueNavigator().createSearch
                (
                        "project=svo and status was 'In Progress' ON '2009-12-12' order by key"
                );
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("SVO-1", "SVO-2", "SVO-3");
    }

    public void testReturnsIssuesThatWereInTheStatusGivenThatTheyTransitionedToThatStatusPriorToTheDate()
    {
        navigation.
                issueNavigator().createSearch
                (
                        "project=svo and status was 'In Progress' ON '2010-12-12' order by key"
                );
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("SVO-3", "SVO-4", "SVO-5");
    }

    public void testReturnsIssuesThatWereInTheStatusGivenThatTheyHaveNotTransitionedFromTheInitialStatusValue()
    {
        navigation.
                issueNavigator().createSearch
                (
                        "project=svo and status was 'Open' ON '2008-12-12' order by key"
                );
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("SVO-6");
    }

    public void testDoesNotReturnIssuesThatWereNotInTheStatusGivenThatTheyTransitionedToThatStatusOnADifferentDate()
    {
        navigation.
                issueNavigator().createSearch
                (
                        "project=svo and status was 'Closed' ON '2010-12-12' order by key"
                );
        assertions.getIssueNavigatorAssertions().assertSearchResultsDoNotContain("SVO-7", "SVO-8");
    }

    public void testDoesNotReturnIssuesThatWereNeverInThatStatus()
    {
        navigation.
                issueNavigator().createSearch
                (
                        "project=svo and status was 'Closed' ON '2010-12-12' order by key"
                );
        assertions.getIssueNavigatorAssertions().assertSearchResultsDoNotContain("SVO-9", "SVO-10", "SVO-11");
    }

    public void testReturnsIssuesThatWereSetToThatStatusWithMoreThanOneDateValue()
    {
        // control assertion
        navigation.
                issueNavigator().createSearch
                (
                        "project=svo and status was Closed on '2010-12-10' order by key"
                );
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("SVO-7", "SVO-17");

        // control assertion
        navigation.
                issueNavigator().createSearch
                (
                        "project=svo and status was Closed on ('2010-12-10') order by key"
                );
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("SVO-7", "SVO-17");

        navigation.
                issueNavigator().createSearch
                (
                        "project=svo and status was Closed on ('2010-12-10','2010-12-11') order by key"
                );
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("SVO-7", "SVO-16", "SVO-17");

        navigation.
                issueNavigator().createSearch
                (
                        "project=svo and status was Closed on ('2010-12-12', '2010-12-12') order by key"
                );
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("SVO-17");

        navigation.
                issueNavigator().createSearch
                (
                        "project=svo and status was Closed on ('2010-12-12', '2010-12-13') order by key"
                );
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("SVO-8", "SVO-17");

        navigation.
                issueNavigator().createSearch
                (
                        "project=svo and status was Closed on ('2010-12-10', '2010-12-12', '2010-12-13') order by key"
                );
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("SVO-7", "SVO-8", "SVO-17");

        // there is no data on 2010-12-19 but since its an OR it doesnt matter
        navigation.
                issueNavigator().createSearch
                (
                        "project=svo and status was Closed on ('2010-12-19', '2010-12-10', '2010-12-12', '2010-12-13') order by key"
                );
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("SVO-7", "SVO-8", "SVO-17");

    }

    public void testReturnsIssuesThatWereSetToThatStatusAtOrJustAfterTheBeginningOfTheDateOrAtOrJustAfterTheEndOfTheDate()
    {
        navigation.
                issueNavigator().createSearch
                (
                        "project=svo and status was 'Resolved' ON '2010-12-12' order by key"
                );
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("SVO-12", "SVO-13", "SVO-14");
    }

    public void testDoesNotReturnIssuesThatWereInThatStatusJustBeforeOrAfterTheEndOfTheDate()
    {
        navigation.
                issueNavigator().createSearch
                (
                        "project=svo and status was 'Reopened' ON '2010-12-12' order by key"
                );
        assertions.getIssueNavigatorAssertions().assertSearchResultsDoNotContain("SVO-17");

        navigation.
                issueNavigator().createSearch
                (
                        "project=svo and status was 'Reopened' ON '2010-12-16' order by key"
                );

        assertions.getIssueNavigatorAssertions().assertSearchResultsDoNotContain("SVO-18");
    }
}
