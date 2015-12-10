package com.atlassian.jira.webtests.ztests.project;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.restclient.Issue;
import com.atlassian.jira.testkit.client.restclient.SearchRequest;
import com.atlassian.jira.testkit.client.restclient.SearchResult;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

/**
 * @since v6.1
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL, Category.PROJECTS })
public class TestProjectKeyEditOnSearch extends FuncTestCase
{

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        backdoor.restoreBlankInstance();
    }

    public void testCanSearchProjectByHistoricalKeys()
    {
        final long projectId = backdoor.project().addProject("Test", "ABC", "admin");
        backdoor.issues().createIssue("ABC", "test issue");
        backdoor.project().editProjectKey(projectId, "XYZ");

        final SearchResult searchResult = backdoor.search().postSearch(new SearchRequest().jql("project = ABC"));
        assertThat(searchResult.issues, contains(issue("XYZ-1")));
    }

    public void testCanSearchProjectExcludingHistoricalKeys()
    {
        backdoor.project().addProject("Monkey 2", "TST", "admin");
        backdoor.issues().createIssue("TST", "test issue 1");

        final long projectId = backdoor.project().addProject("Test", "ABC", "admin");
        backdoor.issues().createIssue("ABC", "test issue 2");
        backdoor.project().editProjectKey(projectId, "XYZ");

        final SearchResult searchResult = backdoor.search().postSearch(new SearchRequest().jql("project != ABC"));
        assertThat(searchResult.issues, contains(issue("TST-1")));
        assertThat(searchResult.issues, not(contains(issue("XYZ-1"))));
        assertThat(searchResult.issues, not(contains(issue("ABC-1"))));
    }

    public void testCanSearchIssueByHistoricalKeys()
    {
        final long projectId = backdoor.project().addProject("Test", "ABC", "admin");
        backdoor.issues().createIssue("ABC", "test issue");
        backdoor.issues().createIssue("ABC", "test issue");
        backdoor.issues().createIssue("ABC", "test issue");
        backdoor.project().editProjectKey(projectId, "XYZ");

        {
            final SearchResult searchResult = backdoor.search().postSearch(new SearchRequest().jql("issue = ABC-1"));
            assertThat(searchResult.issues.size(), equalTo(1));
            assertThat(searchResult.issues, contains(issue("XYZ-1")));
        }

        {
            final SearchResult searchResult = backdoor.search().postSearch(new SearchRequest().jql("issue != ABC-1"));
            assertThat(searchResult.issues.size(), equalTo(2));
            assertThat(searchResult.issues, containsExpectedIssues());
        }

        {
            final SearchResult searchResult = backdoor.search().postSearch(new SearchRequest().jql("issue > ABC-1"));
            assertThat(searchResult.issues.size(), equalTo(2));
            assertThat(searchResult.issues, containsExpectedIssues());
        }
    }

    private Matcher<Iterable<Issue>> containsExpectedIssues()
    {
        return containsInAnyOrder(issue("XYZ-2"), issue("XYZ-3"));
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
