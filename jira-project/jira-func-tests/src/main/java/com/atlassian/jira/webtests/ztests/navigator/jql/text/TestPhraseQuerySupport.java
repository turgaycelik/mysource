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
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

/**
 * Responsible for holding test that verify that quoted phrase text searches return &quot;exact matches&quot; for the
 * query string.
 *
 * Currently, the definition of &quot;exact&quot; is that no stop-word removal/replacement and no stemming should be
 * performed for these type of searches. Consequently, the precision of the search is higher.
 *
 * @since v6.2.3
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestPhraseQuerySupport extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        backdoor.restoreBlankInstance();
    }

    public void testSearchingForAPhraseWithWordsSubjectToStemmingShouldNotMatchTheStemOfThoseWords() throws Exception
    {
        backdoor.project().addProject("Phrase Query Support Test", "PQST", "admin");
        backdoor.issues().createIssue("PQST", "This is a customized engine");
        backdoor.issues().createIssue("PQST", "Boeing custom engine");

        SearchResult searchResult = backdoor.search().postSearch(new SearchRequest().jql("project = PQST AND summary ~\"\\\"customized engine\\\"\""));
        assertThat(searchResult.issues, contains(issue("PQST-1")));
        assertThat(searchResult.issues, not(contains(issue("PQST-2"))));

        searchResult = backdoor.search().postSearch(new SearchRequest().jql("project = PQST AND summary ~\"\\\"custom engine\\\"\""));
        assertThat(searchResult.issues, not(contains(issue("PQST-1"))));
        assertThat(searchResult.issues, contains(issue("PQST-2")));
    }

    public void testSearchingForAPhraseWithStopWordsShouldOnlyMatchThatExactStopWord() throws Exception
    {
        backdoor.project().addProject("Phrase Query Support Test", "PQST", "admin");
        backdoor.issues().createIssue("PQST", "JIRA is a customized engine");

        SearchResult searchResult = backdoor.search().postSearch(new SearchRequest().jql("project = PQST AND summary ~\"\\\"JIRA customized\\\"\""));
        assertThat(searchResult.issues, not(contains(issue("PQST-1"))));

        searchResult = backdoor.search().postSearch(new SearchRequest().jql("project = PQST AND summary ~\"\\\"JIRA is customized\\\"\""));
        assertThat(searchResult.issues, not(contains(issue("PQST-1"))));

        searchResult = backdoor.search().postSearch(new SearchRequest().jql("project = PQST AND summary ~\"\\\"JIRA is an customized\\\"\""));
        assertThat(searchResult.issues, not(contains(issue("PQST-1"))));

        searchResult = backdoor.search().postSearch(new SearchRequest().jql("project = PQST AND summary ~\"\\\"JIRA are a customized\\\"\""));
        assertThat(searchResult.issues, not(contains(issue("PQST-1"))));

        searchResult = backdoor.search().postSearch(new SearchRequest().jql("project = PQST AND summary ~\"\\\"JIRA is a customized\\\"\""));
        assertThat(searchResult.issues, contains(issue("PQST-1")));

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
