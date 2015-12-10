package com.atlassian.jira.functest.framework.assertions;

import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.navigator.NavigatorSearch;
import com.atlassian.jira.functest.framework.navigator.SearchResultsCondition;
import com.atlassian.jira.functest.framework.sharing.SharedEntityInfo;
import net.sourceforge.jwebunit.WebTester;

import java.util.Arrays;

/**
 * Class that can be used to make assertions about the current state of the issue navigator.
 *
 * @since v3.13
 */
public interface IssueNavigatorAssertions
{
    /**
     * Assert that the navigator is currently configured like the passed search.
     *
     * @param search the passed search.
     * @param tester the web tester to test. It should be pointing to the configure navigator screen.
     */
    void assertSimpleSearch(NavigatorSearch search, WebTester tester);

    /**
     * Assert that the advanced navigator is a JQL expression that contains the passed snippets.
     *
     * @param tester the web tester to test. Is should be pointing to the configure advanced screen.
     * @param values the JQL snippets that we try to find.
     */
    public void assertAdvancedSearch(WebTester tester, String...values);

    /**
     * Assert that the navigator displays the correct description.
     *
     * @param info the filter state to assert.
     */
    void assertSearchInfo(SharedEntityInfo info);

    /**
     * Assert properties of the result set displayed in the Issue Navigator.
     *
     * @param conditions the conditions to be met. All must be satisfied.
     */
    void assertSearchResults(Iterable<? extends SearchResultsCondition> conditions);

    /**
     * Assert properties of the result set displayed in the Issue Navigator.
     *
     * @param conditions the conditions to be met. All must be satisfied.
     */
    void assertSearchResults(SearchResultsCondition... conditions);

    /**
     * Asserts that current issue navigator search results are empty.
     */
    void assertSearchResultsAreEmpty();

    /**
     * Asserts that the passed issues are displayed on the issue navigator. This method requires that every key be listed
     * or the assertion will fail. Its a shortcut for {@link #assertSearchResults(Iterable)}
     * using the {@link com.atlassian.jira.functest.framework.navigator.ContainsIssueKeysCondition} and the
     * {@link com.atlassian.jira.functest.framework.navigator.NumberOfIssuesCondition}.
     *
     * @param keys the keys of the issues that must appear on navigator. Every key must be entered. 
     */
    void assertExactIssuesInResults(String... keys);

    /**
     * Assert that the current results displayed in the issue navigator contain a list of issues in the specified
     * order.
     *
     * @param keys The keys of the issues that must appear in the issue navigator results. Must not be null or empty.
     */
    void assertSearchResultsContain(String... keys);

    /**
     * Assert that the current results displayed in the issue navigator do not contain the specified issues.
     *
     * @param keys The keys of the issues that must not appear in the issue navigator results. Must not be null or empty.
     */
    void assertSearchResultsDoNotContain(String... keys);

    /**
     * Assert that the passed errors appears on the advanced issue navigator. Only works for the the advanced view.
     *
     * @param errorMessages the error messages that should appear.
     */
    void assertJqlErrors(String ... errorMessages);

    /**
     * Assert that the passed warnings appears on the advanced issue navigator. Only works for the the advanced view.
     *
     * @param warningMessages the warning messages that should appear.
     */
    void assertJqlWarnings(String ... warningMessages);

    /**
     * Assert that the JQL query was too complex to fit in simple mode.
     */
    void assertJqlTooComplex();

    /**
     * Assert that there were no errors with submitted JQL query
     */
    void assertNoJqlErrors();

    /**
     * Executes a JQL query search and asserts that it fits in the simple filter form, and that the specified params are present and set.
     *
     * @param jqlQuery the query to execute
     * @param formParams the parameters of the form and their values that should be selected
     */
    void assertJqlFitsInFilterForm(final String jqlQuery, final FilterFormParam... formParams);

    /**
     * Represents selected parameters in the filter form (simple issue navigator).
     */
    public static class FilterFormParam
    {
        private String name;
        private String[] values;

        public FilterFormParam(final String name, final String... values)
        {
            this.name = name;
            this.values = values;
        }

        @Override
        public String toString()
        {
            return "FilterFormParam{" +
                    "name='" + name + '\'' +
                    ", values=" + (values == null ? null : Arrays.asList(values)) +
                    '}';
        }

        public String getName()
        {
            return name;
        }

        public String[] getValues()
        {
            return values;
        }
    }

    /**
     * Assert that the issue navigator is displaying with bug from, to and totalNumber (of)
     * @param from
     * @param to
     * @param of
     */
    public void assertIssueNavigatorDisplaying(Locator locator, String from, String to, String of);

}
