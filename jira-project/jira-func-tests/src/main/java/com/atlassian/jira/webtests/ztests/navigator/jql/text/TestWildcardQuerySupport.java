package com.atlassian.jira.webtests.ztests.navigator.jql.text;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.restclient.Issue;
import com.atlassian.jira.testkit.client.restclient.SearchRequest;
import com.atlassian.jira.testkit.client.restclient.SearchResult;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Responsible for holding test that verify that wildcard operators on text searches using JQL return results
 * as expected by the user.
 *
 * @since v6.2.3
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestWildcardQuerySupport extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        backdoor.restoreBlankInstance();
    }

    public void testWildCardSearchMatchesZeroOrMoreCharactersInACamelCaseWord() throws Exception
    {
        backdoor.project().addProject("Wildcard Search Test", "WCST", "admin");
        backdoor.issues().createIssue("WCST", "KidVantage");

        SearchResult searchResult = backdoor.search().postSearch(new SearchRequest().jql("project = WCST AND summary ~\"kid*vantage\""));
        assertThat(searchResult.issues, contains(issue("WCST-1")));

        searchResult = backdoor.search().postSearch(new SearchRequest().jql("project = WCST AND summary ~\"k*antage\""));
        assertThat(searchResult.issues, contains(issue("WCST-1")));

        searchResult = backdoor.search().postSearch(new SearchRequest().jql("project = WCST AND summary ~\"kidvantage\""));
        assertThat(searchResult.issues, contains(issue("WCST-1")));

        searchResult = backdoor.search().postSearch(new SearchRequest().jql("project = WCST AND summary ~\"KidVantage\""));
        assertThat(searchResult.issues, contains(issue("WCST-1")));
    }

    public void testWildCardSearchMatchesZeroOrMoreCharactersInAJavaIdentifierCaseWord() throws Exception
    {
        backdoor.project().addProject("Wildcard Search Test", "WCST", "admin");
        backdoor.issues().createIssue("WCST", "premiumAdjustments");

        SearchResult searchResult = backdoor.search().postSearch(new SearchRequest().jql("project = WCST AND summary ~\"premiumadjustment\""));
        assertThat(searchResult.issues, contains(issue("WCST-1")));

        searchResult = backdoor.search().postSearch(new SearchRequest().jql("project = WCST AND summary ~\"premiumadjustmen*\""));
        assertThat(searchResult.issues, contains(issue("WCST-1")));

        searchResult = backdoor.search().postSearch(new SearchRequest().jql("project = WCST AND summary ~\"premiumadjustme*\""));
        assertThat(searchResult.issues, contains(issue("WCST-1")));

        searchResult = backdoor.search().postSearch(new SearchRequest().jql("project = WCST AND summary ~\"premiumadjustm*\""));
        assertThat(searchResult.issues, contains(issue("WCST-1")));

        searchResult = backdoor.search().postSearch(new SearchRequest().jql("project = WCST AND summary ~\"premiumadjust*\""));
        assertThat(searchResult.issues, contains(issue("WCST-1")));
    }

    public void testWildCardSearchMatchesZeroOrMoreCharactersInAJavaExceptionWord() throws Exception
    {
        backdoor.project().addProject("Wildcard Search Test", "WCST", "admin");
        backdoor.issues().createIssue("WCST", "SEHException");

        SearchResult searchResult = backdoor.search().postSearch(new SearchRequest().jql("project = WCST AND summary ~\"SEHExcept*\""));
        assertThat(searchResult.issues, contains(issue("WCST-1")));

        searchResult = backdoor.search().postSearch(new SearchRequest().jql("project = WCST AND summary ~\"SEHExcepti*\""));
        assertThat(searchResult.issues, contains(issue("WCST-1")));

        searchResult = backdoor.search().postSearch(new SearchRequest().jql("project = WCST AND summary ~\"SEHExceptio*\""));
        assertThat(searchResult.issues, contains(issue("WCST-1")));

        searchResult = backdoor.search().postSearch(new SearchRequest().jql("project = WCST AND summary ~\"SEHException*\""));
        assertThat(searchResult.issues, contains(issue("WCST-1")));

        searchResult = backdoor.search().postSearch(new SearchRequest().jql("project = WCST AND summary ~\"SEHException\""));
        assertThat(searchResult.issues, contains(issue("WCST-1")));
    }

    /**
     * This will check that we are able to match with wildcards the original (non-stemmed) word in the issue.
     * <p/>
     * Original: customize, customized, customizes, custom --> stem to: custom. A wildcard search should be able to
     * match both the original and the stem.
     */
    public void testWildCardSearchMatchesZeroOrMoreCharactersInAWordSubjectToStemming()
    {
        backdoor.project().addProject("Wildcard Search Test", "WCST", "admin");
        backdoor.issues().createIssue("WCST", "customize subject");
        backdoor.issues().createIssue("WCST", "customized subject");
        backdoor.issues().createIssue("WCST", "customizes searches");
        backdoor.issues().createIssue("WCST", "custom jira");

        SearchResult searchResult = backdoor.search().postSearch(new SearchRequest().jql("project = WCST AND summary ~\"custom*\""));
        assertThat(searchResult.issues.size(), equalTo(4));

        searchResult = backdoor.search().postSearch(new SearchRequest().jql("project = WCST AND summary ~\"customi*\""));
        assertThat(searchResult.issues.size(), equalTo(3));

        searchResult = backdoor.search().postSearch(new SearchRequest().jql("project = WCST AND summary ~\"customiz*\""));
        assertThat(searchResult.issues.size(), equalTo(3));

        searchResult = backdoor.search().postSearch(new SearchRequest().jql("project = WCST AND summary ~\"customize*\""));
        assertThat(searchResult.issues.size(), equalTo(3));

        searchResult = backdoor.search().postSearch(new SearchRequest().jql("project = WCST AND summary ~\"customized*\""));
        assertThat(searchResult.issues.size(), equalTo(1));

        searchResult = backdoor.search().postSearch(new SearchRequest().jql("project = WCST AND summary ~\"customizes*\""));
        assertThat(searchResult.issues.size(), equalTo(1));
    }

    public void testWildCardSearchCanMatchOneCharacterInAWordSubjectToStemming()
    {
        backdoor.project().addProject("Wildcard Search Test", "WCST", "admin");
        backdoor.issues().createIssue("WCST", "customize subject");
        backdoor.issues().createIssue("WCST", "customized subject");
        backdoor.issues().createIssue("WCST", "customizes searches");
        backdoor.issues().createIssue("WCST", "custom jira");

        final SearchResult searchResult =
                backdoor.search().postSearch(new SearchRequest().jql("project = WCST AND summary ~\"customize?\""));

        assertThat(searchResult.issues.size(), equalTo(2));
    }

    private BaseMatcher<Issue> issue(final String key)
    {
        return new BaseMatcher<Issue>()
        {

            @Override
            public boolean matches(final Object item)
            {
                return ((Issue) item).key.equals(key);
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("Issue with: key=").appendValue(key);
            }
        };
    }
}
