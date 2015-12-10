package com.atlassian.jira.webtests.ztests.navigator.jql.changehistory;

import com.atlassian.jira.webtests.ztests.navigator.jql.AbstractJqlFuncTest;

import java.util.Iterator;
import java.util.Set;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * @since v4.4
 */
public abstract class AbstractChangeHistoryFuncTest extends AbstractJqlFuncTest
{
    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestChangeHistorySearch.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
    }

    protected void assertWasEmptySearchReturnsEmptyValuesUsingEmptyKeyword(String fieldName, String... issueKeys)
    {
        String jqlQuery = String.format("%s was EMPTY", fieldName);
        super.assertSearchWithResults(jqlQuery, issueKeys);

    }

    protected void assertWasNotEmptySearchReturnsNotEmptyValuesWithEmptyKeyword(String fieldName, String... issueKeys)
    {
        String jqlQuery = String.format("%s was not EMPTY", fieldName);
        super.assertSearchWithResults(jqlQuery, issueKeys);
    }

    protected void assertWasSearchForRenamedConstantFindsOldName(String fieldName, String oldValue, String newValue, String... issueKeys)
    {
        String jqlQuery = String.format("%s was %s", fieldName, oldValue);
        super.assertSearchWithResults(jqlQuery, issueKeys);
        jqlQuery = String.format("%s was %s", fieldName, newValue);
        super.assertSearchWithResults(jqlQuery, issueKeys);
    }

    protected void assertWasNotEmptySearchReturnsEmptyValuesUsingEmptyAlias(String fieldName, String emptyAlias, String... issueKeys)
    {
        String jqlQuery = String.format("%s was not %s", fieldName, emptyAlias);
        super.assertSearchWithResults(jqlQuery, issueKeys);
    }

    protected void assertWasSearchReturnsExpectedValues(String fieldName, String value, String... issueKeys)
    {
        String jqlQuery = String.format("%s was %s", fieldName, value);
        super.assertSearchWithResults(jqlQuery, issueKeys);
    }

    protected void assertWasInSearchReturnsExpectedValues(String fieldName, Set<String> listValues, String... issueKeys)
    {
        String list = buildListString(listValues);
        String jqlQuery = String.format("%s was in %s", fieldName, list);
        super.assertSearchWithResults(jqlQuery, issueKeys);
    }

    protected void assertWasNotInSearchReturnsExpectedValues(String fieldName, Set<String> listValues, String... issueKeys)
    {
        String list = buildListString(listValues);
        String jqlQuery = String.format("%s was not in %s", fieldName, list);
        super.assertSearchWithResults(jqlQuery, issueKeys);
    }

    protected void assertWasBySearchReturnsExpectedValues(String fieldName, String value, String actioner, String... issueKeys)
    {
        String jqlQuery = String.format("%s was %s by %s", fieldName, value, actioner);
        super.assertSearchWithResults(jqlQuery, issueKeys);
    }

    protected void assertWasBySearchUsingListOperandsReturnsExpectedValues(String fieldName, String value, Set<String> actioners, String... issueKeys)
    {
        String list = buildListString(actioners);
        String jqlQuery = String.format("%s was %s by %s", fieldName, value, list);
        super.assertSearchWithResults(jqlQuery, issueKeys);
    }

    protected void assertWasDuringSearchReturnsExpectedValues(String fieldName, String value, String from, String to, String... issueKeys)
    {
        String list = String.format("(%s,%s)", from, to);
        String jqlQuery = String.format("%s was %s during %s", fieldName, value, list);
        super.assertSearchWithResults(jqlQuery, issueKeys);
    }

    protected void assertWasBeforeSearchReturnsExpectedValues(String fieldName, String value, String date, String... issueKeys)
    {
        String jqlQuery = String.format("%s was %s before %s", fieldName, value, date);
        super.assertSearchWithResults(jqlQuery, issueKeys);
    }

    protected void assertWasAfterSearchReturnsExpectedValues(String fieldName, String value, String date, String... issueKeys)
    {
        String jqlQuery = String.format("%s was %s after %s", fieldName, value, date);
        super.assertSearchWithResults(jqlQuery, issueKeys);
    }

    protected void assertWasOnSearchReturnsExpectedValues(String fieldName, String value, String date, String... issueKeys)
    {
        String jqlQuery = String.format("%s was %s on %s", fieldName, value, date);
        super.assertSearchWithResults(jqlQuery, issueKeys);
    }

    protected void assertWasInListFunctionReturnsxpectedValues(String fieldName, String function, String... issueKeys)
    {
        String jqlQuery = String.format("%s was in %s", fieldName, function);
        super.assertSearchWithResults(jqlQuery, issueKeys);
    }

    protected void assertInvalidSearchProducesError(String fieldName, String value, String whereClause, String error)
    {
        String jqlQuery = String.format("%s was %s %s", fieldName, value, whereClause);
        super.assertSearchWithError(jqlQuery, error);
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

}
