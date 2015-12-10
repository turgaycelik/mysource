package com.atlassian.jira.webtests.ztests.navigator.jql.changehistory.reporter;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.navigator.jql.changehistory.AbstractChangeHistoryFuncTest;
import com.google.common.collect.Sets;

import java.util.Set;

/**
 * @since v4.4
 */
@WebTest ( { Category.FUNC_TEST, Category.JQL, Category.CHANGE_HISTORY })
public class TestReporterWasSearching extends AbstractChangeHistoryFuncTest
{
    private static final String FIELD_NAME = "reporter";
    private static final String[] ALL_ISSUES = { "HSP-9", "HSP-8", "HSP-7", "HSP-6", "HSP-5", "HSP-4", "HSP-3", "HSP-2", "HSP-1" };


    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        navigation.login(ADMIN_USERNAME);
    }

    public void testWasEmptySearch()
    {
        String[] issueKeys = {  "HSP-9" };
        super.assertWasEmptySearchReturnsEmptyValuesUsingEmptyKeyword(FIELD_NAME, issueKeys);
    }

    public void testWasNotEmptySearch()
    {
        String[] issueKeys = { "HSP-8", "HSP-7", "HSP-6", "HSP-5", "HSP-4", "HSP-3" , "HSP-2", "HSP-1"};
        super.assertWasNotEmptySearchReturnsNotEmptyValuesWithEmptyKeyword(FIELD_NAME, issueKeys);
    }

    public void testWasSearchUsingSingleValueOperandsReturnsExpectedValues()
    {
        super.assertWasSearchReturnsExpectedValues(FIELD_NAME, "admin", ALL_ISSUES);
        super.assertWasSearchReturnsExpectedValues(FIELD_NAME, "fred", "HSP-9");
        super.assertWasSearchReturnsExpectedValues(FIELD_NAME, "bob");
    }

    public void testWasSearchUsingListOperands()
    {
        Set users = Sets.newHashSet("fred", "admin");
        super.assertWasInSearchReturnsExpectedValues(FIELD_NAME, users, ALL_ISSUES);
        users = Sets.newHashSet("fred", "bob");
        super.assertWasInSearchReturnsExpectedValues(FIELD_NAME, users, "HSP-9");
    }

    public void testWasNotInSearchUsingListOperands()
    {
        String[] expected = { "HSP-8", "HSP-7", "HSP-6", "HSP-5", "HSP-4", "HSP-3" , "HSP-2", "HSP-1"};
        Set users = Sets.newHashSet("fred", "bob");
        super.assertWasNotInSearchReturnsExpectedValues(FIELD_NAME, users, expected);
    }


    public void testWasSearchUsingByPredicate()
    {
        String[] expected = { "HSP-9" };
        super.assertWasBySearchReturnsExpectedValues(FIELD_NAME, "fred", "admin", expected);
        expected = new String[] { };
        super.assertWasBySearchReturnsExpectedValues(FIELD_NAME, "admin", "fred", expected);
        super.assertWasBySearchUsingListOperandsReturnsExpectedValues(FIELD_NAME, "empty", Sets.newHashSet("fred", "admin"), "HSP-9");
    }

    public void testWasSearchUsingDuringPredicate()
    {
        super.assertWasDuringSearchReturnsExpectedValues(FIELD_NAME, "admin", "'2011/05/01'", "'2011/05/31'", ALL_ISSUES);
        super.assertWasDuringSearchReturnsExpectedValues(FIELD_NAME, "fred", "'2011/05/01'", "'2011/05/31'", "HSP-9");
    }

    public void testWasSearchUsingBeforePredicate()
    {
        String[] expected = { "HSP-4", "HSP-3", "HSP-2", "HSP-1" };
        super.assertWasBeforeSearchReturnsExpectedValues(FIELD_NAME, "admin", "'2010/05/01'", expected);
        super.assertWasBeforeSearchReturnsExpectedValues(FIELD_NAME, "fred", "'2011/05/21 10:55'", "HSP-9");
    }

    public void testWasSearchUsingAfterPredicate()
    {
        super.assertWasAfterSearchReturnsExpectedValues(FIELD_NAME, "admin", "'2011/05/01'", ALL_ISSUES);
        String[] expected = new String[] {  "HSP-8", "HSP-7", "HSP-6", "HSP-5","HSP-4", "HSP-3", "HSP-2", "HSP-1" };
        super.assertWasAfterSearchReturnsExpectedValues(FIELD_NAME, "admin", "'2011/06/03 10:55'", expected);
    }

    public void testWasSearchUsingOnPredicate()
    {
        String[] expected = { "HSP-8", "HSP-7", "HSP-6", "HSP-5", "HSP-4", "HSP-3", "HSP-2", "HSP-1" };
        super.assertWasOnSearchReturnsExpectedValues(FIELD_NAME, "admin", "'2011/05/01'", expected);
        expected = new String[] { "HSP-9"};
        super.assertWasOnSearchReturnsExpectedValues(FIELD_NAME, "fred", "'2011/06/01'", expected);
    }

    public void testWasSearchUsingLongOperandsIsInvalid()
    {
        // invalid user id
        String expectedError = "A value with ID '1' does not exist for the field 'reporter'.";
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
        // invalid operand  type
        String expectedError = "A value provided by the function 'currentLogin' is invalid for the field 'reporter'.";
        super.assertInvalidSearchProducesError(FIELD_NAME, "currentLogin()", "", expectedError);
    }
}