package com.atlassian.jira.webtests.ztests.navigator.jql.changehistory.changed;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.navigator.jql.AbstractJqlFuncTest;
import com.google.common.collect.Sets;

import java.util.Iterator;
import java.util.Set;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * @since v4.4
 */
@WebTest ( { Category.FUNC_TEST, Category.JQL, Category.CHANGE_HISTORY })
public class TestChangedSearching extends AbstractJqlFuncTest
{
    private static final String FIELD_NAME = "fixversion";
    private static final String[] ALL_ISSUES = { "HSP-13", "HSP-12", "HSP-11", "HSP-10", "HSP-9", "HSP-8", "HSP-7", "HSP-6", "HSP-5", "HSP-4", "HSP-3", "HSP-2", "HSP-1" };
    private static final String VERSION_1 = "'New Version 1'";
    private static final String VERSION_2 = "'New Version 4'";
    private static final String VERSION_3 = "'New Version 5'";

    @Override
    protected void setUpTest()
    {
        administration.restoreData("TestChangeSearching.xml");
        navigation.login(ADMIN_USERNAME);
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
    }

    public void testChangedEmptySearch()
    {
        String[] issueKeys = { "HSP-12", "HSP-11", "HSP-10", "HSP-3", "HSP-1" };
        String jqlQuery = "fixVersion changed";
        assertSearchWithResults(jqlQuery, issueKeys);
    }

    public void testChangedNotEmptySearch()
    {
        String[] issueKeys = { "HSP-13", "HSP-9", "HSP-8", "HSP-7", "HSP-6", "HSP-5", "HSP-4", "HSP-2" };
        String jqlQuery = "not fixVersion changed";
        assertSearchWithResults(jqlQuery, issueKeys);
    }

    public void testChangedFromSearchUsingSingleValueOperandsReturnsExpectedValues()
    {
        assertChangedFromSearchReturnsExpectedValues(FIELD_NAME, VERSION_1, "HSP-12", "HSP-11", "HSP-10");
        assertChangedFromSearchReturnsExpectedValues(FIELD_NAME, VERSION_2, "HSP-12", "HSP-11", "HSP-10");
        assertChangedFromSearchReturnsExpectedValues(FIELD_NAME, VERSION_3);
        assertChangedFromSearchReturnsExpectedValues(FIELD_NAME, "EMPTY", "HSP-12", "HSP-11", "HSP-3", "HSP-1");
    }

    public void testChangedToSearchUsingSingleValueOperandsReturnsExpectedValues()
    {
        assertChangedToSearchReturnsExpectedValues(FIELD_NAME, VERSION_1, "HSP-12", "HSP-11", "HSP-10", "HSP-1");
        assertChangedToSearchReturnsExpectedValues(FIELD_NAME, VERSION_2, "HSP-12", "HSP-11", "HSP-10", "HSP-3");
        assertChangedToSearchReturnsExpectedValues(FIELD_NAME, VERSION_3, "HSP-12", "HSP-11", "HSP-3");
        assertChangedToSearchReturnsExpectedValues(FIELD_NAME, "Empty", "HSP-12", "HSP-11", "HSP-10");

    }

//    public void testChangedSearchUsingListOperands()
//    {
//        Set versions = Sets.newHashSet(VERSION_1, VERSION_2);
//        assertChangedInSearchReturnsExpectedValues(FIELD_NAME, versions, "HSP-13", "HSP-12", "HSP-11", "HSP-10", "HSP-3", "HSP-1");
//        versions = Sets.newHashSet(VERSION_2, VERSION_3);
//        assertChangedInSearchReturnsExpectedValues(FIELD_NAME, versions, "HSP-12", "HSP-11", "HSP-10", "HSP-3");
//    }


    public void testChangedSearchUsingByPredicate()
    {
        String[] expected = { "HSP-12", "HSP-11", "HSP-10", "HSP-1"};
        assertChangedBySearchReturnsExpectedValues(FIELD_NAME, VERSION_1, "admin", expected);
                assertChangedBySearchUsingListOperandsReturnsExpectedValues(FIELD_NAME, VERSION_1, Sets.newHashSet("fred", "admin"), expected);
        expected = new String[] {  };
        assertChangedBySearchReturnsExpectedValues(FIELD_NAME, VERSION_2, "fred", expected);
        assertChangedBySearchUsingListOperandsReturnsExpectedValues(FIELD_NAME, "empty", Sets.newHashSet("fred", "bob"), expected);
    }

    public void testChangedSearchUsingDuringPredicate()
    {
        String[] expected = {  "HSP-1" };
        assertChangedDuringSearchReturnsExpectedValues(FIELD_NAME, VERSION_1, "'2011/05/01'", "'2011/05/31'", expected);
        expected = new String[]{ "HSP-12", "HSP-11", "HSP-10", "HSP-1"};
        assertChangedDuringSearchReturnsExpectedValues(FIELD_NAME, VERSION_1, "'2011/05/01'", "'2011/08/31'", expected);
        expected = new String[]{ "HSP-12", "HSP-11", "HSP-10"};
        assertChangedDuringSearchReturnsExpectedValues(FIELD_NAME, VERSION_1, "'2011/06/01'", "'2011/08/31'", expected);

    }

    public void testChangedSearchUsingBeforePredicate()
    {
        String[] expected = { };
        assertChangedBeforeSearchReturnsExpectedValues(FIELD_NAME, VERSION_2, "'2011/05/01'", expected);
        expected = new String[] {  "HSP-1" };
        assertChangedBeforeSearchReturnsExpectedValues(FIELD_NAME, VERSION_1, "'2011/07/01 10:55'", expected);
    }

    public void testChangedSearchUsingAfterPredicate()
    {
        String[] expected = {  "HSP-12", "HSP-11", "HSP-10" , "HSP-1"};
        assertChangedAfterSearchReturnsExpectedValues(FIELD_NAME, VERSION_1, "'2011/05/01'", expected);
        expected = new String[] {  "HSP-12", "HSP-11"};
        assertChangedAfterSearchReturnsExpectedValues(FIELD_NAME, VERSION_1, "'2011/08/26 14:29'", expected);
    }

    public void testChangedSearchUsingOnPredicate()
    {
        String[] expected = { };
        assertChangedOnSearchReturnsExpectedValues(FIELD_NAME, VERSION_1, "'2011/05/01'", expected);
        expected = new String[] {  "HSP-1" };
        assertChangedOnSearchReturnsExpectedValues(FIELD_NAME, VERSION_1, "'2011/05/18'", expected);
    }

    public void testChangedSearchUsingLongOperandsIsValid()
    {
        assertChangedFromSearchReturnsExpectedValues(FIELD_NAME, "10000", "HSP-12", "HSP-11", "HSP-10");
    }

    public void testChangedSearchUsingUnclosedListIsInvalid()
    {
        // invalid list
        String expectedError = "Error in the JQL Query: Expecting ')' before the end of the query.";
        assertInvalidSearchProducesError(FIELD_NAME, "(fred, bob", "", expectedError);
    }


    //TODO Fix the parsing here to show error message
//    public void testChangedSearchUsingIncorrectPredicateIsInvalid()
//    {
//        // invalid predicate
//        String expectedError = "Error in the JQL Query: Expecting either 'OR' or 'AND' but got 'at'. (line 1, character 28)";
//        assertInvalidSearchProducesError(FIELD_NAME, "(fred, bob)", "at '10:55'", expectedError);
//    }

 
    protected void assertChangedFromSearchReturnsExpectedValues(String fieldName, String value, String... issueKeys)
    {
        String jqlQuery = String.format("%s changed from %s", fieldName, value);
        assertSearchWithResults(jqlQuery, issueKeys);
    }

    protected void assertChangedToSearchReturnsExpectedValues(String fieldName, String value, String... issueKeys)
    {
        String jqlQuery = String.format("%s changed to %s", fieldName, value);
        assertSearchWithResults(jqlQuery, issueKeys);
    }

    protected void assertChangedBySearchReturnsExpectedValues(String fieldName, String value, String actioner, String... issueKeys)
    {
        String jqlQuery = String.format("%s changed to %s by %s", fieldName, value, actioner);
        assertSearchWithResults(jqlQuery, issueKeys);
    }

    protected void assertChangedBySearchUsingListOperandsReturnsExpectedValues(String fieldName, String value, Set<String> actioners, String... issueKeys)
    {
        String list = buildListString(actioners);
        String jqlQuery = String.format("%s changed to %s by %s", fieldName, value, list);
        assertSearchWithResults(jqlQuery, issueKeys);
    }

    protected void assertChangedDuringSearchReturnsExpectedValues(String fieldName, String value, String from, String to, String... issueKeys)
    {
        String list = String.format("(%s,%s)", from, to);
        String jqlQuery = String.format("%s changed to %s during %s", fieldName, value, list);
        assertSearchWithResults(jqlQuery, issueKeys);
    }

    protected void assertChangedBeforeSearchReturnsExpectedValues(String fieldName, String value, String date, String... issueKeys)
    {
        String jqlQuery = String.format("%s changed to %s before %s", fieldName, value, date);
        assertSearchWithResults(jqlQuery, issueKeys);
    }

    protected void assertChangedAfterSearchReturnsExpectedValues(String fieldName, String value, String date, String... issueKeys)
    {
        String jqlQuery = String.format("%s changed to %s after %s", fieldName, value, date);
        assertSearchWithResults(jqlQuery, issueKeys);
    }

    protected void assertChangedOnSearchReturnsExpectedValues(String fieldName, String value, String date, String... issueKeys)
    {
        String jqlQuery = String.format("%s changed to %s on %s", fieldName, value, date);
        assertSearchWithResults(jqlQuery, issueKeys);
    }

     protected void assertInvalidSearchProducesError(String fieldName, String value, String whereClause, String error)
    {
        String jqlQuery = String.format("%s changed %s %s", fieldName, value, whereClause);
        assertSearchWithError(jqlQuery, error);
    }

    private String buildListString(Set<String> listValues)
    {
        final StringBuilder list = new StringBuilder();
        if (listValues != null)
        {
            list.append('(');
            Iterator<String> iter = listValues.iterator();
            while (iter.hasNext())
            {
                list.append(iter.next());
                if (iter.hasNext())
                {
                    list.append(',');
                }
            }
            list.append(')');

        }
        return list.toString();
    }
    

//    public void testChangedSearchUsingIncorrectFunctionIsInvalid()
//    {
//        // invalid function
//        String expectedError = "A value provided by the function 'currentLogin' is invalid for the field 'assignee'.";
//        assertInvalidSearchProducesError(FIELD_NAME, "currentLogin()", "", expectedError);
//    }


}