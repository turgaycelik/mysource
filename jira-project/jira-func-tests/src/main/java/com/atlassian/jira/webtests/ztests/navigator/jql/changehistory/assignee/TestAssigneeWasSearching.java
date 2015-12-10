package com.atlassian.jira.webtests.ztests.navigator.jql.changehistory.assignee;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.navigator.jql.changehistory.AbstractChangeHistoryFuncTest;
import com.google.common.collect.Sets;

import java.util.Set;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * @since v4.4
 */
@WebTest ( { Category.FUNC_TEST, Category.JQL, Category.CHANGE_HISTORY })
public class TestAssigneeWasSearching extends AbstractChangeHistoryFuncTest
{
    private static final String FIELD_NAME = "assignee";
    private static final String[] ALL_ISSUES = { "HSP-9", "HSP-8", "HSP-7", "HSP-6", "HSP-5", "HSP-4", "HSP-3", "HSP-2", "HSP-1" };


    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        navigation.login(ADMIN_USERNAME);
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
    }

    public void testWasEmptySearch()
    {
        String[] issueKeys = { "HSP-5", "HSP-2", "HSP-1" };
        super.assertWasEmptySearchReturnsEmptyValuesUsingEmptyKeyword(FIELD_NAME, issueKeys);
    }

    public void testWasNotEmptySearch()
    {
        String[] issueKeys = { "HSP-9", "HSP-8", "HSP-7", "HSP-6", "HSP-4", "HSP-3" };
        super.assertWasNotEmptySearchReturnsNotEmptyValuesWithEmptyKeyword(FIELD_NAME, issueKeys);
    }

    public void testWasSearchUsingSingleValueOperandsReturnsExpectedValues()
    {
        super.assertWasSearchReturnsExpectedValues(FIELD_NAME, "admin", ALL_ISSUES);
        super.assertWasSearchReturnsExpectedValues(FIELD_NAME, "fred", "HSP-9", "HSP-5", "HSP-4");
        super.assertWasSearchReturnsExpectedValues(FIELD_NAME, "bob", "HSP-2");
    }

    public void testWasSearchUsingListOperands()
    {
        Set users = Sets.newHashSet("fred", "admin");
        super.assertWasInSearchReturnsExpectedValues(FIELD_NAME, users, ALL_ISSUES);
        users = Sets.newHashSet("fred", "bob");
        super.assertWasInSearchReturnsExpectedValues(FIELD_NAME, users, "HSP-9", "HSP-5", "HSP-4", "HSP-2");
    }

    public void testWasNotInSearchUsingListOperands()
    {
        Set users = Sets.newHashSet("fred", "bob");
        String[] expected = { "HSP-8", "HSP-7", "HSP-6", "HSP-3", "HSP-1" };
        super.assertWasNotInSearchReturnsExpectedValues(FIELD_NAME, users, expected);
    }


    public void testWasSearchUsingByPredicate()
    {
        String[] expected = { "HSP-9", "HSP-5", "HSP-4" };
        super.assertWasBySearchReturnsExpectedValues(FIELD_NAME, "fred", "admin", expected);
        expected = new String[] { "HSP-9" };
        super.assertWasBySearchReturnsExpectedValues(FIELD_NAME, "admin", "fred", expected);
        super.assertWasBySearchUsingListOperandsReturnsExpectedValues(FIELD_NAME, "empty", Sets.newHashSet("fred", "bob"), "HSP-2", "HSP-1");
    }

    public void testWasSearchUsingDuringPredicate()
    {
        String[] expected = { "HSP-9", "HSP-8", "HSP-7", "HSP-6", "HSP-5", "HSP-3", "HSP-2", "HSP-1" };
        super.assertWasDuringSearchReturnsExpectedValues(FIELD_NAME, "admin", "'2011/05/01'", "'2011/05/31'", expected);
    }

    public void testWasSearchUsingBeforePredicate()
    {
        String[] expected = { "HSP-4", "HSP-3", "HSP-2", "HSP-1" };
        super.assertWasBeforeSearchReturnsExpectedValues(FIELD_NAME, "admin", "'2011/05/01'", expected);
        expected = new String[] { "HSP-5", "HSP-4", "HSP-3", "HSP-2", "HSP-1" };
        super.assertWasBeforeSearchReturnsExpectedValues(FIELD_NAME, "admin", "'2011/05/01 10:55'", expected);
    }

    public void testWasSearchUsingAfterPredicate()
    {
        String[] expected = { "HSP-9", "HSP-8", "HSP-7", "HSP-6", "HSP-5", "HSP-3", "HSP-2", "HSP-1" };
        super.assertWasAfterSearchReturnsExpectedValues(FIELD_NAME, "admin", "'2011/05/01'", expected);
        expected = new String[] { "HSP-9", "HSP-8", "HSP-7", "HSP-6", "HSP-3", "HSP-2", "HSP-1" };
        super.assertWasAfterSearchReturnsExpectedValues(FIELD_NAME, "admin", "'2011/06/01 10:55'", expected);
    }

    public void testWasSearchUsingOnPredicate()
    {
        String[] expected = { "HSP-9", "HSP-8", "HSP-7", "HSP-6", "HSP-5", "HSP-3", "HSP-2", "HSP-1" };
        super.assertWasOnSearchReturnsExpectedValues(FIELD_NAME, "admin", "'2011/05/01'", expected);
        expected = new String[] { "HSP-9", "HSP-8", "HSP-7", "HSP-6", "HSP-3", "HSP-2", "HSP-1" };
        super.assertWasOnSearchReturnsExpectedValues(FIELD_NAME, "admin", "'2011/06/01'", expected);
    }

    public void testWasSearchUsingLongOperandsIsInvalid()
    {
        // invalid operand  type
        String expectedError = "A value with ID '1' does not exist for the field 'assignee'.";
        super.assertInvalidSearchProducesError(FIELD_NAME, "1", "", expectedError);
    }

    public void testWasSearchUsingUnclosedListIsInvalid()
    {
        // invalid list
        String expectedError = "Error in the JQL Query: Expecting ')' before the end of the query.";
        super.assertInvalidSearchProducesError(FIELD_NAME, "(fred, bob", "", expectedError);
    }

    public void testWasSearchUsingIncorrectPredicateIsInvalid()
    {
        // invalid predicate
        String expectedError = "Error in the JQL Query: Expecting either 'OR' or 'AND' but got 'at'. (line 1, character 26)";
        super.assertInvalidSearchProducesError(FIELD_NAME, "(fred, bob)", "at '10:55'", expectedError);
    }

    public void testWasSearchUsingIncorrectFunctionIsInvalid()
    {
        // invalid function
        String expectedError = "A value provided by the function 'currentLogin' is invalid for the field 'assignee'.";
        super.assertInvalidSearchProducesError(FIELD_NAME, "currentLogin()", "", expectedError);
    }

      public void testWasInAcceptsListFunctions(){
        // JRADEV-6413 membersOf causes stack trace
          super.assertWasInListFunctionReturnsxpectedValues(FIELD_NAME, "membersOf('jira-users')", ALL_ISSUES);
          super.assertWasInListFunctionReturnsxpectedValues(FIELD_NAME, "membersOf('jira-administrators')", ALL_ISSUES);


    }
}