package com.atlassian.jira.functest.framework.assertions;

import com.atlassian.jira.functest.framework.backdoor.Backdoor;
import com.atlassian.jira.functest.framework.backdoor.FilterInfo;
import com.atlassian.jira.functest.framework.backdoor.FiltersClient;
import com.atlassian.jira.functest.framework.navigator.NavigatorCondition;
import com.atlassian.jira.functest.framework.navigator.NavigatorSearch;
import com.atlassian.jira.functest.framework.sharing.SharedEntityInfo;
import com.atlassian.jira.functest.framework.util.FilteredSearcherGroup;
import com.atlassian.jira.functest.framework.util.IssueTableClient;
import com.atlassian.jira.functest.framework.util.SearchRendererValue;
import com.atlassian.jira.functest.framework.util.SearchResults;
import com.atlassian.jira.functest.framework.util.Searcher;
import com.atlassian.jira.functest.framework.util.SearchersClient;
import com.atlassian.jira.testkit.client.restclient.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Assertions using issue table backdoor
 *
 * @since v5.2
 */
public class IssueTableAssertions
{
    public static final String ORDER_BY_CLAUSE = " ORDER BY key DESC";

    private Backdoor backdoor;

    public IssueTableAssertions(Backdoor backdoor)
    {
        this.backdoor = backdoor;
    }

    /**
     * Runs the specified search WITH AN APPENDED ORDER BY CLAUSE.
     *
     * @param username user to search
     * @param jqlString the jql to search
     * @param issueKeys the issue keys to assert in the result
     */
    public void assertSearchWithResultsForUser(String username, String jqlString, String... issueKeys)
    {
        backdoor.issueTableClient().loginAs(username);
        assertSearchWithResults(jqlString, issueKeys);
    }

    /**
     * Runs the specified search WITH AN APPENDED ORDER BY CLAUSE.
     *
     * @param jqlString the jql to search
     * @param expectedIssueKeys the issue keys to assert in the result
     */
    public void assertSearchWithResults(String jqlString, String... expectedIssueKeys)
    {
        IssueTableClient.ClientIssueTableServiceOutcome issueTable = backdoor.issueTableClient().getIssueTable(jqlString + ORDER_BY_CLAUSE);
        final List<String> actualIssueKeys;

        final String table = issueTable.getIssueTable().getTable();
        if (table != null)
        {
            final Document tableDocument = Jsoup.parse(table);
            final Elements issuerows = tableDocument.getElementsByClass("issuerow");
            actualIssueKeys = new ArrayList<String>(issuerows.size());
            for (Element row : issuerows)
            {
                actualIssueKeys.add(row.dataset().get("issuekey"));
            }
        }
        else
        {
            actualIssueKeys = Collections.emptyList();
        }

        assertEquals(jqlString, Arrays.asList(expectedIssueKeys), actualIssueKeys);
    }

    public void assertSearchTooComplex(String jqlString)
    {
        assertSearchTooComplex("admin", jqlString);
    }


    public void assertSearchWithErrorForUser(String username, String jqlString, String error)
    {
        backdoor.issueTableClient().loginAs(username);
        assertSearchWithError(jqlString, error);
    }

    public void assertSearchWithError(Long filterId, String jqlString, String error)
    {
        Response response = backdoor.issueTableClient().getResponse(filterId, jqlString, null, null, true);
        if (response.statusCode == 200 || response.entity == null || response.entity.errorMessages == null
                || response.entity.errorMessages.isEmpty())
        {
            fail("Search did not fail or the failure happened in an unexpected way:" +
                    "\n\tfilterId=" + filterId +
                    "\n\tjqlString=" + jqlString +
                    "\n\tresponse=" + response + '\n');
        }
        assertEquals(error, response.entity.errorMessages.get(0));
    }

    public void assertSearchWithError(String jqlString, String error)
    {
        assertSearchWithError(null, jqlString, error);
    }
    public void assertSearchWithErrors(String jqlString, List<String> errors)
    {
        Response response = backdoor.issueTableClient().getResponse(null, jqlString, null, null, true);
        assertEquals(errors, response.entity.errorMessages);
    }

    public void assertSearchWithError(Long filter, String error)
    {
        assertSearchWithError(filter, null, error);
    }

    public void assertSearchWithWarningForUser(String username, String jqlString, String warning)
    {
        backdoor.issueTableClient().loginAs(username);
        assertSearchWithWarning(jqlString, warning);
    }

    public void assertSearchWithWarning(Long filterId, String jql, String warning)
    {
        IssueTableClient.ClientIssueTableServiceOutcome issueTable = backdoor.issueTableClient().getIssueTable(filterId, jql, null, null, true);
        assertEquals(warning, issueTable.warnings.get(0));
    }

    public void assertSearchWithWarning(String jqlString, String warning)
    {
        IssueTableClient.ClientIssueTableServiceOutcome issueTable = backdoor.issueTableClient().getIssueTable(jqlString);
        assertNotNull(jqlString, issueTable.warnings);
        assertEquals(jqlString, warning, issueTable.warnings.get(0));
    }

    public void assertSearchWithWarning(Long filterId, String warning)
    {
        IssueTableClient.ClientIssueTableServiceOutcome issueTable = backdoor.issueTableClient().getIssueTable(filterId);
        assertEquals(warning, issueTable.warnings.get(0));
    }

    public void assertSearchIsValid(Long filterId)
    {
        assertNull(backdoor.issueTableClient().getResponse(
                filterId, null, null, null, true).entity);

        IssueTableClient.ClientIssueTableServiceOutcome issueTable = backdoor.issueTableClient().getIssueTable(filterId);
        assertEquals(0, issueTable.warnings.size());
    }

    public void assertSimpleSearch(String user, long filter, NavigatorSearch expectedSearch)
    {
        FiltersClient filters = backdoor.filters();
        SearchersClient searchersClient = backdoor.searchersClient();

        if (user != null)
        {
            searchersClient.loginAs(user);
            filters.loginAs(user);
        }
        String filterJql = filters.getFilterJql(filter);
        SearchResults searchers = searchersClient.getSearchers(filterJql);
        Collection<SearchRendererValue> values = searchers.values.values();
        Collection<NavigatorCondition> conditions = expectedSearch.getConditions();
        Document doc = Jsoup.parse("");
        for (SearchRendererValue value : values)
        {
            doc.append(value.editHtml);
        }
        for (NavigatorCondition condition : conditions)
        {
            condition.assertSettings(doc);
        }
    }

    public void assertSearchersPresent(String... searchers)
    {
        List<String> searcherNames = getSearcherNames();

        for (String searcher : searchers)
        {
            assertTrue("Expected searchers to contain [" + searcher + "]", searcherNames.contains(searcher));
        }
    }

    private List<String> getSearcherNames()
    {
        SearchResults searchersResult = backdoor.searchersClient().getSearchers("");
        List<FilteredSearcherGroup> groups = searchersResult.searchers.getGroups();
        List<String> searcherNames = new ArrayList<String>();
        for (FilteredSearcherGroup group : groups)
        {
            List<Searcher> searcherList = group.getSearchers();
            for (Searcher searcher : searcherList)
            {
                if (searcher.getShown())
                {
                    searcherNames.add(searcher.getName());
                }
            }
        }
        return searcherNames;
    }

    public void assertSearchersNotPresent(String... searchers)
    {
        List<String> searcherNames = getSearcherNames();

        for (String searcher : searchers)
        {
            assertFalse("Expected searchers NOT to contain [" + searcher + "]", searcherNames.contains(searcher));
        }
    }

    public void assertSimpleSearch(long filter, NavigatorSearch expectedSearch)
    {
       assertSimpleSearch(null, filter, expectedSearch);
    }

    public void assertSearchInfo(long filter, SharedEntityInfo info)
    {
        FilterInfo actualFilterInfo = backdoor.filters().getFilter(filter);
        String description = info.getDescription().equals("") ? null : info.getDescription();
        assertEquals("name is wrong", info.getName(), actualFilterInfo.name);
        assertEquals("Description is wrong", description, actualFilterInfo.description);
        assertEquals("Should have been favourited but wasn't", info.isFavourite(), actualFilterInfo.favourite);
    }

    public void assertSearchWithWarnings(String jql, List<String> warnings)
    {
        IssueTableClient.ClientIssueTableServiceOutcome issueTable = backdoor.issueTableClient().getIssueTable(jql);
        assertEquals(warnings, issueTable.warnings);
    }

    public void assertMaxErrors(String jqlString, int maxErrors)
    {
        Response response = backdoor.issueTableClient().getResponse(null, jqlString, null, null, true);
        assertEquals(maxErrors, response.entity.errorMessages.size());

    }

    public void assertFilterErrors(String user, int filterId, List errors) {
        Response response = backdoor.issueTableClient().loginAs(user).getResponse((long) filterId);
        assertEquals(errors, response.entity.errorMessages);
    }

    public void assertFilterError(String user, int filterId, String error) {
        List<String> errors = new ArrayList<String>();
        errors.add(error);
        assertFilterErrors(user, filterId, errors);
    }

    public void assertSearchTooComplex(String user, String jqlString)
    {
        Response searchersResponse = backdoor.searchersClient().loginAs(user).getSearchersResponse(jqlString);
        assertEquals(searchersResponse.statusCode, 400);
    }

}
