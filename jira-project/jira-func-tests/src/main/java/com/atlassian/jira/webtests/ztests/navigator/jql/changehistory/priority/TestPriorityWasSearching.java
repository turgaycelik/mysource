package com.atlassian.jira.webtests.ztests.navigator.jql.changehistory.priority;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.navigator.jql.changehistory.AbstractChangeHistoryFuncTest;
import com.google.common.collect.Sets;

import java.util.Set;

/**
 * @since v4.4
 */
@WebTest ( { Category.FUNC_TEST, Category.JQL, Category.CHANGE_HISTORY })
public class TestPriorityWasSearching extends AbstractChangeHistoryFuncTest
{
    private static final String FIELD_NAME = "priority";
    private static final String[] ALL_ISSUES = { "HSP-9", "HSP-8", "HSP-7", "HSP-6", "HSP-5", "HSP-4", "HSP-3", "HSP-2", "HSP-1" };


    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        navigation.login(ADMIN_USERNAME);
    }

    public void testWasEmptySearch()
    {
        //priority can never be empty
        String[] issueKeys = { };
        super.assertWasEmptySearchReturnsEmptyValuesUsingEmptyKeyword(FIELD_NAME, issueKeys);
    }

    public void testWasNotEmptySearch()
    {
        super.assertWasNotEmptySearchReturnsNotEmptyValuesWithEmptyKeyword(FIELD_NAME, ALL_ISSUES);
    }

    public void testWasSearchUsingSingleValueOperandsReturnsExpectedValues()
    {
        super.assertWasSearchReturnsExpectedValues(FIELD_NAME, "major", ALL_ISSUES);
        super.assertWasSearchReturnsExpectedValues(FIELD_NAME, "minor", "HSP-7", "HSP-2");
        super.assertWasSearchReturnsExpectedValues(FIELD_NAME, "catastrophic", "HSP-7", "HSP-1");
    }

    public void testWasSearchUsingListOperands()
    {
        Set priorities = Sets.newHashSet("major", "minor");
        super.assertWasInSearchReturnsExpectedValues(FIELD_NAME, priorities, ALL_ISSUES);
        priorities = Sets.newHashSet("minor", "catastrophic");
        super.assertWasInSearchReturnsExpectedValues(FIELD_NAME, priorities, "HSP-7", "HSP-2", "HSP-1");
    }

    public void testWasNotInSearchUsingListOperands()
    {
        Set priorities = Sets.newHashSet("minor", "catastrophic");
        String[] expected = { "HSP-9", "HSP-8", "HSP-6", "HSP-5", "HSP-4", "HSP-3" };
        super.assertWasNotInSearchReturnsExpectedValues(FIELD_NAME, priorities, expected);
    }

    public void testWasSearchForRenamedConstant()
    {
        assertWasSearchReturnsExpectedValues(FIELD_NAME, "critical", "HSP-7");
        assertWasSearchReturnsExpectedValues(FIELD_NAME, "catastrophic", "HSP-7", "HSP-1");
    }


    public void testWasSearchUsingByPredicate()
    {
        String[] expected = { "HSP-8", "HSP-7", "HSP-6", "HSP-5", "HSP-4", "HSP-3", "HSP-2", "HSP-1" };
        super.assertWasBySearchReturnsExpectedValues(FIELD_NAME, "major", "admin", expected);
        expected = new String[] { "HSP-1" };
        super.assertWasBySearchReturnsExpectedValues(FIELD_NAME, "catastrophic", "fred", expected);
        super.assertWasBySearchUsingListOperandsReturnsExpectedValues(FIELD_NAME, "catastrophic", Sets.newHashSet("fred", "bob"), "HSP-1");
    }

    public void testWasSearchUsingDuringPredicate()
    {
        String[] expected = { "HSP-9", "HSP-8", "HSP-6", "HSP-5", "HSP-4", "HSP-3", "HSP-2", "HSP-1" };
        super.assertWasDuringSearchReturnsExpectedValues(FIELD_NAME, "major", "'2011/05/30'", "'2011/06/02'", expected);
    }

    public void testWasSearchUsingBeforePredicate()
    {
        super.assertWasBeforeSearchReturnsExpectedValues(FIELD_NAME, "major", "'2011/06/01'", ALL_ISSUES);
        String[] expected = { "HSP-4", "HSP-3", "HSP-2", "HSP-1" };
        super.assertWasBeforeSearchReturnsExpectedValues(FIELD_NAME, "major", "'2010/06/02'", expected);
    }

    public void testWasSearchUsingAfterPredicate()
    {
        super.assertWasAfterSearchReturnsExpectedValues(FIELD_NAME, "major", "'2011/05/01'", ALL_ISSUES);
        String[] expected = { "HSP-9", "HSP-8", "HSP-6", "HSP-5", "HSP-4", "HSP-3", "HSP-2", "HSP-1" };
        super.assertWasAfterSearchReturnsExpectedValues(FIELD_NAME, "major", "'2011/06/01 10:55'", expected);
    }

    public void testWasSearchUsingOnPredicate()
    {
        super.assertWasOnSearchReturnsExpectedValues(FIELD_NAME, "major", "'2011/05/01'", ALL_ISSUES);
        String[] expected = new String[] { "HSP-9", "HSP-8", "HSP-6", "HSP-5", "HSP-4", "HSP-3", "HSP-2", "HSP-1" };
        super.assertWasOnSearchReturnsExpectedValues(FIELD_NAME, "major", "'2011/06/01'", expected);
    }

    public void testWasSearchUsingLongOperandsIsInvalid()
    {
        // invalid id
        String expectedError = "A value with ID '7' does not exist for the field 'priority'.";
        super.assertInvalidSearchProducesError(FIELD_NAME, "7", "", expectedError);
    }

    public void testWasSearchUsingLongOperandsIsValid()
    {
        // valid id
        super.assertWasSearchReturnsExpectedValues(FIELD_NAME, "4", "HSP-7", "HSP-2");
    }

    public void testWasSearchUsingUnclosedListIsInvalid()
    {
        // invalid list
        String expectedError = "Error in the JQL Query: Expecting ')' before the end of the query.";
        super.assertInvalidSearchProducesError(FIELD_NAME, "(major, minor", "", expectedError);
    }

    public void testWasSearchUsingIcorrectPriorityIsInvalid()
    {
        // invalid list
        String expectedError = "The value 'urgent' does not exist for the field 'priority'.";
        super.assertInvalidSearchProducesError(FIELD_NAME, "urgent", "", expectedError);
    }

    public void testWasSearchUsingIncorrectPredicateIsInvalid()
    {
        // invalid predicate
        String expectedError = "Error in the JQL Query: Expecting either 'OR' or 'AND' but got 'at'. (line 1, character 29)";
        super.assertInvalidSearchProducesError(FIELD_NAME, "(major, minor)", "at '10:55'", expectedError);
    }

    public void testWasSearchUsingIncorrectFunctionIsInvalid()
    {
        // invalid operand  type
        String expectedError = "A value provided by the function 'currentLogin' is invalid for the field 'priority'.";
        super.assertInvalidSearchProducesError(FIELD_NAME, "currentLogin()", "", expectedError);
    }
}