package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.assertions.IssueNavigatorAssertions;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.util.SearchRendererValueResults;
import com.atlassian.jira.functest.framework.util.SearchResults;
import com.atlassian.jira.testkit.client.restclient.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Some common methods for the tests which are testing Jql.
 *
 * @since v4.0
 */
public abstract class AbstractJqlFuncTest extends FuncTestCase
{
    void assertFitsFilterForm(final String jqlQuery, final IssueNavigatorAssertions.FilterFormParam... formParams)
    {
        Response searchersResponse = backdoor.searchersClient().getSearchersResponse(jqlQuery);
        assertEquals(200, searchersResponse.statusCode);
//        assertions.getIssueNavigatorAssertions().assertJqlFitsInFilterForm(jqlQuery, formParams);
    }

    void assertTooComplex(final String jqlQuery)
    {
        Response searchersResponse = backdoor.searchersClient().getSearchersResponse(jqlQuery);
        assertEquals(400, searchersResponse.statusCode);
        assertEquals("jqlTooComplex", searchersResponse.entity.errorMessages.get(0));
    }

    void assertInvalidContext(final String jqlQuery)
    {
        SearchResults searchers = backdoor.searchersClient().getSearchers(jqlQuery);
        SearchRendererValueResults values = searchers.values;
        boolean invalid = false;
        for (String s : values.keySet())
        {
            if (!values.get(s).validSearcher) {
                invalid = true;
            }
        }
        assertTrue("Expected invalid searcher", invalid);
    }

    void assertInvalidValue(final String jqlQuery)
    {

        SearchResults searchers = backdoor.searchersClient().getSearchers(jqlQuery);
        SearchRendererValueResults values = searchers.values;
        boolean invalid = false;
        for (String s : values.keySet())
        {
            Document editHtml = Jsoup.parse(values.get(s).editHtml);
            if (editHtml.getElementsByClass("invalid_sel").size() > 0) {
                invalid = true;
                break;
            }
        }
        assertTrue("Expected invalid value", invalid);
    }

    void assertFilterFormValue(IssueNavigatorAssertions.FilterFormParam formParam)
    {
        tester.setWorkingForm("issue-filter");
        assertSameElements(formParam.getValues(), tester.getDialog().getForm().getParameterValues(formParam.getName()));
    }

    private static void assertSameElements(String[] a, String[] b)
    {
        Set<String> as = (a == null || a.length == 0) ? null : new HashSet<String>(Arrays.asList(a));
        Set<String> bs = (b == null || b.length == 0) ? null : new HashSet<String>(Arrays.asList(b));
        assertEquals(as, bs);
    }

    void assertEmptyIssues()
    {
        assertions.getIssueNavigatorAssertions().assertSearchResultsAreEmpty();
    }

    void assertIssues(final String... keys)
    {
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults(keys);
    }

    protected void executeIssueNavigatorURL(final IssueNavigatorNavigation.NavigatorEditMode startEditMode, final IssueNavigatorNavigation.NavigatorEditMode expectedEditMode, final boolean resetQuery, final String... urlParameter)
            throws UnsupportedEncodingException
    {
        if (navigation.issueNavigator().getCurrentEditMode() != startEditMode)
        {
            navigation.issueNavigator().displayAllIssues();
            navigation.issueNavigator().gotoEditMode(startEditMode);
            assertEquals(startEditMode, navigation.issueNavigator().getCurrentEditMode());
        }

        String params = encodeQueryString(urlParameter);

        if (resetQuery)
        {
            params = params.concat("&reset=true");
        }
        else
        {
            params = params.concat("&addParams=true");
        }

        System.out.println("URL=" + "secure/IssueNavigator.jspa?" + params + "&runQuery=true");

        tester.gotoPage("secure/IssueNavigator.jspa?" + params + "&runQuery=true");
        assertEquals(expectedEditMode, navigation.issueNavigator().getCurrentEditMode());
    }

    protected String encodeQueryString(final String... urlParameter) throws UnsupportedEncodingException
    {
        StringBuilder params = new StringBuilder();
        for (String parameter : urlParameter)
        {
            final int index = parameter.indexOf("=");
            final String parameterName = parameter.substring(0, index);
            final String parameterValue = parameter.substring(index + 1);

            params.append("&").append(parameterName).append("=").append(URLEncoder.encode(parameterValue, "UTF8"));
        }
        return params.substring(1);
    }

    protected void assertSearchWithResultsForUser(String username, String jqlString, String... issueKeys)
    {
        issueTableAssertions.assertSearchWithResultsForUser(username, jqlString, issueKeys);
    }

    protected void assertSearchWithResults(String jqlString, String... issueKeys)
    {
        issueTableAssertions.assertSearchWithResults(jqlString, issueKeys);
    }

    /**
     * Runs the specified search exactly as passed in - it is your responsibility to provide an order by cluase in the
     * query if ordering is important.
     *
     * @param jqlString the jql to search
     * @param issueKeys the issue keys to assert in the result
     */
    protected void assertExactSearchWithResults(String jqlString, String... issueKeys)
    {
        navigation.issueNavigator().createSearch(jqlString);
        assertIssues(issueKeys);
    }

    protected void assertSearchWithErrorForUser(String username, String jqlString, String error)
    {
        issueTableAssertions.assertSearchWithErrorForUser(username, jqlString, error);
    }

    protected void assertSearchWithError(String jqlString, String error)
    {
        issueTableAssertions.assertSearchWithError(jqlString, error);
    }

    protected void assertSearchWithWarningForUser(String username, String jqlString, String warning)
    {
        issueTableAssertions.assertSearchWithWarningForUser(username, jqlString, warning);
    }

    protected void assertSearchWithWarning(String jqlString, String warning)
    {
        issueTableAssertions.assertSearchWithWarning(jqlString, warning);
    }

    protected static IssueNavigatorAssertions.FilterFormParam createFilterFormParam(final String name, final String... values)
    {
        return new IssueNavigatorAssertions.FilterFormParam(name, values);
    }

    protected void assertJqlQueryInTextArea(final String expectedJQL)
    {
        final XPathLocator locator = new XPathLocator(tester, "//textarea[@id='jqltext']");
        text.assertTextPresent(locator, expectedJQL);
    }
}
