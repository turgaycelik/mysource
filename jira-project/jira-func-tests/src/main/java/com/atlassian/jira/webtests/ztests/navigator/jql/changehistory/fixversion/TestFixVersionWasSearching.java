package com.atlassian.jira.webtests.ztests.navigator.jql.changehistory.fixversion;

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
public class TestFixVersionWasSearching extends AbstractChangeHistoryFuncTest
{
    private static final String FIELD_NAME = "fixversion";
    private static final String[] ALL_ISSUES = { "HSP-13", "HSP-12", "HSP-11", "HSP-10", "HSP-9", "HSP-8", "HSP-7", "HSP-6", "HSP-5", "HSP-4", "HSP-3", "HSP-2", "HSP-1" };
    private static final String VERSION_1 = "'New Version 1'";
    private static final String VERSION_2 = "'New Version 4'";
    private static final String VERSION_3 = "'New Version 5'";

    @Override
    protected void setUpTest()
    {
        administration.restoreData("TestFixVersionWasSearching.xml");
        navigation.login(ADMIN_USERNAME);
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
    }

    public void testWasEmptySearch()
    {
        String[] issueKeys = { "HSP-12", "HSP-11", "HSP-10", "HSP-9", "HSP-8", "HSP-7", "HSP-6", "HSP-5", "HSP-4", "HSP-3", "HSP-2", "HSP-1" };
        super.assertWasEmptySearchReturnsEmptyValuesUsingEmptyKeyword(FIELD_NAME, issueKeys);
    }

    public void testWasNotEmptySearch()
    {
        String[] issueKeys = { "HSP-13" };
        super.assertWasNotEmptySearchReturnsNotEmptyValuesWithEmptyKeyword(FIELD_NAME, issueKeys);
    }

    public void testWasSearchUsingSingleValueOperandsReturnsExpectedValues()
    {
        super.assertWasSearchReturnsExpectedValues(FIELD_NAME, VERSION_1,  "HSP-13", "HSP-12", "HSP-11", "HSP-10", "HSP-1");
        super.assertWasSearchReturnsExpectedValues(FIELD_NAME, VERSION_2,  "HSP-12", "HSP-11", "HSP-10", "HSP-3");
        super.assertWasSearchReturnsExpectedValues(FIELD_NAME, VERSION_3,  "HSP-12", "HSP-11", "HSP-3");
    }

    public void testWasSearchUsingListOperands()
    {
        Set versions = Sets.newHashSet(VERSION_1, VERSION_2);
        super.assertWasInSearchReturnsExpectedValues(FIELD_NAME, versions, "HSP-13", "HSP-12", "HSP-11", "HSP-10", "HSP-3", "HSP-1");
        versions = Sets.newHashSet(VERSION_2, VERSION_3);
        super.assertWasInSearchReturnsExpectedValues(FIELD_NAME, versions, "HSP-12", "HSP-11", "HSP-10", "HSP-3");
    }

    public void testWasNotInSearchUsingListOperands()
    {
        Set versions = Sets.newHashSet(VERSION_1, VERSION_3);
        String[] expected = {  "HSP-9", "HSP-8", "HSP-7", "HSP-6", "HSP-5", "HSP-4", "HSP-2" };
        super.assertWasNotInSearchReturnsExpectedValues(FIELD_NAME, versions, expected);
    }


    public void testWasSearchUsingByPredicate()
    {
        String[] expected = { "HSP-13", "HSP-12", "HSP-11", "HSP-10", "HSP-1"};
        super.assertWasBySearchReturnsExpectedValues(FIELD_NAME, VERSION_1, "admin", expected);
        expected = new String[] {  };
        super.assertWasBySearchReturnsExpectedValues(FIELD_NAME, VERSION_2, "fred", expected);
        expected = new String[] { "HSP-9" };
        super.assertWasBySearchUsingListOperandsReturnsExpectedValues(FIELD_NAME, "empty", Sets.newHashSet("fred", "bob"), expected);
    }

    public void testWasSearchUsingDuringPredicate()
    {
        String[] expected = {  "HSP-1" };
        super.assertWasDuringSearchReturnsExpectedValues(FIELD_NAME, VERSION_1, "'2011/05/01'", "'2011/05/31'", expected);
    }

    public void testWasSearchUsingBeforePredicate()
    {
        String[] expected = { };
        super.assertWasBeforeSearchReturnsExpectedValues(FIELD_NAME, VERSION_2, "'2011/05/01'", expected);
        expected = new String[] {  "HSP-1" };
        super.assertWasBeforeSearchReturnsExpectedValues(FIELD_NAME, VERSION_1, "'2011/05/19 10:55'", expected);
    }

    public void testWasSearchUsingAfterPredicate()
    {
        String[] expected = {  "HSP-13", "HSP-12", "HSP-11", "HSP-10", "HSP-1" };
        super.assertWasAfterSearchReturnsExpectedValues(FIELD_NAME, VERSION_1, "'2011/05/01'", expected);
        expected = new String[] {  "HSP-13", "HSP-12", "HSP-11", "HSP-1"};
        super.assertWasAfterSearchReturnsExpectedValues(FIELD_NAME, VERSION_1, "'2011/08/26 14:29'", expected);
    }

    public void testWasSearchUsingOnPredicate()
    {
        String[] expected = { };
        super.assertWasOnSearchReturnsExpectedValues(FIELD_NAME, VERSION_1, "'2011/05/01'", expected);
        expected = new String[] {  "HSP-1" };
        super.assertWasOnSearchReturnsExpectedValues(FIELD_NAME, VERSION_1, "'2011/06/01'", expected);
    }

    public void testWasSearchUsingLongOperandsIsValid()
    {
        super.assertWasSearchReturnsExpectedValues(FIELD_NAME, "10000", "HSP-13", "HSP-12", "HSP-11", "HSP-10", "HSP-1");
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
        String expectedError = "Error in the JQL Query: Expecting either 'OR' or 'AND' but got 'at'. (line 1, character 28)";
        super.assertInvalidSearchProducesError(FIELD_NAME, "(fred, bob)", "at '10:55'", expectedError);
    }



//    public void testWasSearchUsingIncorrectFunctionIsInvalid()
//    {
//        // invalid function
//        String expectedError = "A value provided by the function 'currentLogin' is invalid for the field 'assignee'.";
//        super.assertInvalidSearchProducesError(FIELD_NAME, "currentLogin()", "", expectedError);
//    }


}