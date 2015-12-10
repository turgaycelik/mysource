package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.restclient.Issue;
import com.atlassian.jira.testkit.client.restclient.Response;
import com.atlassian.jira.testkit.client.restclient.SearchClient;
import com.atlassian.jira.testkit.client.restclient.SearchRequest;
import com.atlassian.jira.testkit.client.restclient.SearchResult;
import com.google.common.collect.Sets;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.atlassian.jira.testkit.client.restclient.matcher.ContainsStringThatStartsWith.containsStringThatStartsWith;
import static java.lang.Math.min;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

/**
 * Tests for the issue search functionality.
 *
 * @since v4.3
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestSearchResource extends RestFuncTest
{
    private static final String HSP = "HSP";
    private static final String MKY = "MKY";
    private static final String HSP_1 = HSP + "-1";
    private static final String HSP_5 = HSP + "-5";

    private SearchClient searchClient;

    public void testSearchShouldFilterOutIssuesFromNonBrowseableProjects() throws Exception
    {
        //as admin they can see all 
        Response<SearchResult> response = searchClient.postSearchResponse(new SearchRequest().jql(""));
        SearchResult allResults = response.body;
        assertThat(allResults.total, equalTo(9));

        // HSP is not viewable by anonymous
        response = searchClient.anonymous().postSearchResponse(new SearchRequest().jql("project = " + HSP));
        assertEquals(400, response.statusCode);
        assertEquals(1, response.entity.errorMessages.size());
        assertThat(response.entity.errorMessages, containsStringThatStartsWith("The value 'HSP' does not exist for the field 'project'."));

        // MKY is "public"
        response = searchClient.anonymous().postSearchResponse(new SearchRequest().jql("project = " + MKY));
        SearchResult mkyResults = response.body;
        assertThat(mkyResults.total, equalTo(4));
        assertThat(mkyResults.issues.size(), equalTo(4));

        // this should be identical to the above
        response = searchClient.anonymous().postSearchResponse(new SearchRequest().jql(""));
        SearchResult anonResults = response.body;
        assertThat(anonResults.issues, equalTo(mkyResults.issues));
    }

    public void testSearchMaxResultsShouldDefaultTo50() throws Exception
    {
        SearchResult results = searchClient.postSearch(new SearchRequest());
        assertThat(results.maxResults, equalTo(50));
    }

    public void testSearchMaxResultsIsNotAllowedToExceed1000() throws Exception
    {
        SearchResult results = searchClient.postSearch(new SearchRequest().maxResults(2000));
        assertThat(results.maxResults, equalTo(1000));
    }

    public void testSearchShouldReturnPagesWithAtMostThreeIssues() throws Exception
    {
        final int issueCount = 9;
        final int pageSize = 3;

        int startAt;
        int pageNum = 0;
        Set<Issue> keysReturnedInPreviousPage = Sets.newHashSet();

        while ((startAt = (pageSize * pageNum++)) < issueCount)
        {
            SearchResult page = searchClient.postSearch(new SearchRequest().jql("order by key").maxResults(pageSize).startAt(startAt));
            assertThat(page.startAt, equalTo(startAt));
            assertThat(page.total, equalTo(issueCount));
            assertThat(page.issues.size(), equalTo(min(pageSize, issueCount - startAt)));
            assertTrue(Sets.intersection(keysReturnedInPreviousPage, Sets.newHashSet(page.issues)).isEmpty());
            keysReturnedInPreviousPage.addAll(page.issues);
        }
    }

    public void testSearchShouldReturnPagesWithAtMaxReturnZero() throws Exception
    {
        final int issueCount = 9;
        int startAt = 3;
        final int pageSize = 0;

        SearchResult page = searchClient.postSearch(new SearchRequest().jql("order by key").maxResults(pageSize).startAt(startAt));
        assertThat(page.startAt, equalTo(startAt));
        assertThat(page.total, equalTo(issueCount));
        assertTrue(page.issues.isEmpty());
    }

    public void testSearchStartAtAndMaxResultsShouldHaveDefaultValues() throws Exception
    {
        SearchResult results = searchClient.postSearch(new SearchRequest());
        assertThat(results.startAt, equalTo(0));
        assertThat(results.maxResults, CoreMatchers.notNullValue());
    }

    public void testSearchWithBadJqlShouldReturnStatusCode400() throws Exception
    {
        Response results = searchClient.postSearchResponse(new SearchRequest().jql("zomg!!!11111"));

        assertThat(results.statusCode, equalTo(400));
        assertThat(results.entity.errorMessages, containsStringThatStartsWith("Error in the JQL Query:"));
    }

    public void testSearchWithNoJqlShouldReturnAllIssues() throws Exception
    {
        SearchRequest searchRequest = new SearchRequest().jql(null);

        assertFalse(searchClient.getSearch(searchRequest).issues.isEmpty());
        assertFalse(searchClient.postSearch(searchRequest).issues.isEmpty());
    }

    public void testSearchUsingGetReturnsTheSameAsUsingPost() throws Exception
    {
        SearchRequest aSearch = new SearchRequest().fields("summary", "status").expand("names", "schema");

        SearchResult postResults = searchClient.postSearch(aSearch);
        SearchResult getResults = searchClient.getSearch(aSearch);
        assertThat(getResults, equalTo(postResults));
    }

    public void testSearchShouldRespectFieldsQueryParam() throws Exception
    {
        // Restrict the list of issue fields we want to see in the result
        final List<String> fieldsToInclude = Arrays.asList("summary,status,assignee");
        final SearchRequest request = new SearchRequest().jql("key = " + HSP_5).fields(fieldsToInclude);
        final SearchResult result = searchClient.getSearch(request);

        assertEquals(1, result.issues.size());
        final Issue.Fields fields = result.issues.get(0).fields;

        // Fields we are expecting
        assertNotNull(fields.get("summary"));
        assertNotNull(fields.get("status"));
        assertNotNull(fields.get("assignee"));

        // Fields we are not expecting
        final Set<String> idSet = fields.idSet();
        idSet.remove("summary");
        idSet.remove("status");
        idSet.remove("assignee");

        for (final String id : idSet)
        {
            assertNull(fields.get(id));
        }
    }

    public void testSearchShouldRespectFieldsQueryParamForCustomFields() throws Exception
    {
        // Restrict the list of issue fields we want to see in the result
        final List<String> fieldsToInclude = Arrays.asList("summary,status,assignee,customfield_10001");
        final SearchRequest request = new SearchRequest().jql("key = " + HSP_5).fields(fieldsToInclude);
        final SearchResult result = searchClient.getSearch(request);

        assertEquals(1, result.issues.size());
        final Issue.Fields fields = result.issues.get(0).fields;

        // Fields we are expecting
        assertNotNull(fields.get("summary"));
        assertNotNull(fields.get("status"));
        assertNotNull(fields.get("assignee"));
        assertNotNull(fields.get("customfield_10001"));

        // Fields we are not expecting
        final Set<String> idSet = fields.idSet();
        idSet.remove("summary");
        idSet.remove("status");
        idSet.remove("assignee");
        idSet.remove("customfield_10001");

        for (final String id : idSet)
        {
            assertNull(fields.get(id));
        }
    }

    public void testNamesAndSchemaShouldBeAtTheTopLevelOfTheSearchResource() throws Exception
    {
        List<String> fields = Arrays.asList("summary");
        List<String> expand = Arrays.asList("names", "schema");

        SearchRequest search = new SearchRequest().fields(fields).expand(expand);
        SearchResult results = searchClient.getSearch(search);

        for (String field : expand)
        {
            assertThat(results.expand, Matchers.containsString(field));
        }

        assertNotNull(results.names);
        assertNotNull(results.schema);
        for (String field : fields)
        {
            assertNotNull(results.names.get(field));
            assertNotNull(results.schema.get(field));
        }

        for (Issue issue : results.issues)
        {
            assertNull(issue.names);
            assertNull(issue.schema);
        }
    }

    public void testQueryValidationCanBeDisabled() throws Exception
    {
        final String hsp1 = "HSP-1"; // exists
        final String hsp123 = "HSP-123"; // doesn't exist
        final String query = String.format("key in (%s, %s)", hsp1, hsp123);

        Response validateResults = searchClient.getSearchResponse(new SearchRequest().jql(query));
        assertThat(validateResults.statusCode, equalTo(400));

        SearchResult noValidateResults = searchClient.getSearch(new SearchRequest().jql(query).validateQuery(false));
        assertThat(noValidateResults.issues, containsIssueWithKey("HSP-1"));
        assertThat(noValidateResults.issues, not(containsIssueWithKey("HSP-123")));
    }

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestIssueSearch.xml");
        searchClient = new SearchClient(getEnvironmentData());
    }

    private static ContainsIssueMatcher containsIssueWithKey(String issueKey)
    {
        return new ContainsIssueMatcher(issueKey);
    }

    static class ContainsIssueMatcher extends TypeSafeMatcher<List<Issue>>
    {
        private final String issueKey;

        public ContainsIssueMatcher(String issueKey)
        {
            this.issueKey = issueKey;
        }


        @Override
        protected boolean matchesSafely(final List<Issue> issues)
        {
            for (Issue issue : issues)
            {
                if (issueKey.equals(issue.key))
                {
                    return true;
                }
            }

            return false;
        }

        @Override
        public void describeTo(final Description description)
        {
            description.appendText("a list of Issue containing " + issueKey);
        }
    }
}
