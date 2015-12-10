package com.atlassian.jira.webtests.ztests.navigator.jql.changehistory.status.daterange;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.navigator.jql.changehistory.AbstractChangeHistoryFuncTest;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * Responsible for verifying that a user is able to query all the issues that were in a particular status after a date.
 *
 * @see <a href="https://jdog.atlassian.com/browse/JRADEV-3740">User Story [JRADEV-3740]</a>
 * @since v4.4
 */
@WebTest ( { Category.FUNC_TEST, Category.JQL, Category.CHANGE_HISTORY })
public class TestSearchIssueStatusAfterADate extends AbstractChangeHistoryFuncTest
{

    private static final String FIELD_NAME = "status";

    @Override
    protected void setUpTest()
    {
        administration.restoreData("TestSearchIssueStatusAfterADate.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
    }

    public void testInvalidAfterInput()
    {
        String expectedError = "The AFTER predicate must be supplied with only 1 date value.";
        super.assertInvalidSearchProducesError(FIELD_NAME, "Closed", "AFTER ('2011-07-01','2011-07-01')", expectedError);
        super.assertInvalidSearchProducesError(FIELD_NAME, "Closed", "AFTER ('2011-07-01','2011-07-03','2012-07-03')", expectedError);
    }


    /**
     * Verifies that issues that transitioned to the state specified in the query after the date are included in the
     * results.
     */
    public void testReturnsTheIssuesThatWereInTheStatusAndTransitionedToThatStatusAfterTheDate()
    {
        navigation.issueNavigator().createSearch("project=sht and status was resolved after '2011-05-25 09:29' order by key");
        assertions.getIssueNavigatorAssertions().assertSearchResultsContain("SHT-4", "SHT-5", "SHT-10");
    }

    /**
     * Verifies that issues that transitioned to the state specified in the query before the date are included in the
     * results given that they remained in that status after the date specified in the query.
     */
    public void testReturnsTheIssuesThatWereInTheStatusAndTransitionedToThatStatusBeforeTheDate()
    {
        navigation.issueNavigator().createSearch("project=sht and status was resolved after '2011-05-25 09:29' order by key");
        assertions.getIssueNavigatorAssertions().assertSearchResultsContain("SHT-1", "SHT-2");
    }

    /**
     * Verifies that issues that never transitioned to the state specified in the query are not include in the results.
     */
    public void testDoesNotReturnIssuesThatWereNeverInTheSpecifiedStatus()
    {
        navigation.issueNavigator().createSearch("project=sht and status was resolved after '2011-05-25 09:29' order by key");
        assertions.getIssueNavigatorAssertions().assertSearchResultsDoNotContain("SHT-6", "SHT-7");
    }

    /**
     * Verifies that issues that transitioned to the state specified in the query at some moment in their history but
     * were not in that state after the specifed time are not included in the results.
     */
    public void testDoesNotReturnIssuesThatWereNotInTheStatusGivenThatTheIssuesWereInThatStatusAtADateOutsideTheRange()
    {
        navigation.issueNavigator().createSearch("project=sht and status was resolved after '2011-05-25 09:29' order by key");
        assertions.getIssueNavigatorAssertions().assertSearchResultsDoNotContain("SHT-6", "SHT-7");
    }

    /**
     * Verifies that issues that transitioned to the state specified in the query just at the specified date are not
     * included in the results, therefore confirming that the range for AFTER searches is exclusive.
     */
    public void testDoesNotReturnIssuesThatTransitionedToTheSpecifiedStatusAtTheDate()
    {
        navigation.issueNavigator().createSearch("project=sht and status was resolved after '2011-05-25 09:29' order by key");
        assertions.getIssueNavigatorAssertions().assertSearchResultsDoNotContain("SHT-3");
    }
}
