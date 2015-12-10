package com.atlassian.jira.bc.issue.search;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.MockJiraServiceContext;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.mock.issue.MockIssue;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestDefaultIssuePickerSearchService
{
    private static final String JIRA_108 = "JRA-108";
    private static final String SUMMARY_1 = "Summary";
    private static final String JIRA_234 = "JIRA-234";
    private static final String SUMMARY_234 = "Summary 234";

    @Test
    public void testNoIssues()
    {
        final IssuePickerSearchProvider provider = getProvider(Collections.<Issue>emptyList());

        final DefaultIssuePickerSearchService service = DefaultIssuePickerSearchService.create(provider);

        final IssuePickerSearchService.IssuePickerParameters params = new IssuePickerSearchService.IssuePickerParameters(null, null, null, null, true,
            true, 50);

        JiraServiceContext ctx = new MockJiraServiceContext();

        final Collection<IssuePickerResults> issueResults = service.getResults(ctx, params);
        assertNotNull(issueResults);
        assertEquals(1, issueResults.size());

        final IssuePickerResults results = issueResults.iterator().next();
        assertNotNull(results);
        assertEquals(0, results.getIssues().size());
    }

    @Test
    public void testOneProviderWithIssues()
    {
        final IssuePickerSearchProvider provider = getProvider(Arrays.asList(getMockIssue(JIRA_108, SUMMARY_1)));

        final DefaultIssuePickerSearchService service = DefaultIssuePickerSearchService.create(provider);
        final IssuePickerSearchService.IssuePickerParameters params = new IssuePickerSearchService.IssuePickerParameters(null, null, null, null, true,
            true, 50);

        final Collection<IssuePickerResults> issueResults = service.getResults(new MockJiraServiceContext(), params);
        assertNotNull(issueResults);
        assertEquals(1, issueResults.size());

        final IssuePickerResults results = issueResults.iterator().next();
        assertNotNull(results);
        assertEquals(1, results.getIssues().size());

        final Issue issue = results.getIssues().iterator().next();
        assertEquals(JIRA_108, issue.getKey());

    }

    @Test
    public void testOneProviderWithDuplicate()
    {
        final IssuePickerSearchProvider provider = getProvider(Arrays.asList(getMockIssue(JIRA_108, SUMMARY_1), getMockIssue(JIRA_108, SUMMARY_1)));

        final DefaultIssuePickerSearchService service = DefaultIssuePickerSearchService.create(provider);

        final IssuePickerSearchService.IssuePickerParameters params = new IssuePickerSearchService.IssuePickerParameters(null, null, null, null, true,
            true, 50);

        final Collection<IssuePickerResults> issueResults = service.getResults(new MockJiraServiceContext(), params);
        assertNotNull(issueResults);
        assertEquals(1, issueResults.size());

        final IssuePickerResults results = issueResults.iterator().next();
        assertNotNull(results);
        assertEquals(2, results.getIssues().size());

        final Issue issue = results.getIssues().iterator().next();
        assertEquals(JIRA_108, issue.getKey());

    }

    @Test
    public void testMultipleProviderWithDuplicateIssues()
    {
        final IssuePickerSearchProvider provider1 = getProvider(Collections.singletonList(getMockIssue(JIRA_108, SUMMARY_1)));
        final IssuePickerSearchProvider provider2 = getProvider(Collections.singletonList(getMockIssue(JIRA_108, SUMMARY_1)));

        final DefaultIssuePickerSearchService service = DefaultIssuePickerSearchService.create(provider1, provider2);

        final IssuePickerSearchService.IssuePickerParameters params = new IssuePickerSearchService.IssuePickerParameters(null, null, null, null, true,
            true, 50);

        final Collection<IssuePickerResults> issueResults = service.getResults(new MockJiraServiceContext(), params);
        assertNotNull(issueResults);
        assertEquals(2, issueResults.size());

        final IssuePickerResults results = issueResults.iterator().next();
        assertNotNull(results);
        assertEquals(1, results.getIssues().size());

        final Issue issue = results.getIssues().iterator().next();
        assertEquals(JIRA_108, issue.getKey());
    }

    @Test
    public void testMultipleProviderWithUniqueIssues()
    {
        final IssuePickerSearchProvider provider1 = getProvider(Collections.singletonList(getMockIssue(JIRA_108, SUMMARY_1)));
        final IssuePickerSearchProvider provider2 = getProvider(Collections.singletonList(getMockIssue(JIRA_234, SUMMARY_234)));

        final DefaultIssuePickerSearchService service = DefaultIssuePickerSearchService.create(provider1, provider2);
        final IssuePickerSearchService.IssuePickerParameters params = new IssuePickerSearchService.IssuePickerParameters(null, null, null, null, true,
            true, 50);

        final Collection<IssuePickerResults> issueResults = service.getResults(new MockJiraServiceContext(), params);
        assertNotNull(issueResults);
        assertEquals(2, issueResults.size());

        final Iterator<IssuePickerResults> ipResultsIterator = issueResults.iterator();
        IssuePickerResults results = ipResultsIterator.next();
        assertNotNull(results);
        assertEquals(1, results.getIssues().size());

        Issue issue = results.getIssues().iterator().next();
        assertEquals(JIRA_108, issue.getKey());

        results = ipResultsIterator.next();
        assertNotNull(results);
        assertEquals(1, results.getIssues().size());

        issue = results.getIssues().iterator().next();
        assertEquals(JIRA_234, issue.getKey());
    }

    @Test
    public void testProviderDoesNotSupportParameters() throws Exception
    {
        final IssuePickerSearchProvider provider1 = getProvider(Collections.singletonList(getMockIssue(JIRA_108, SUMMARY_1)));
        final IssuePickerSearchProvider provider2 = getDisabledProvider();

        final DefaultIssuePickerSearchService service = DefaultIssuePickerSearchService.create(provider1, provider2);
        final IssuePickerSearchService.IssuePickerParameters params = new IssuePickerSearchService.IssuePickerParameters(null, null, null, null, true,
            true, 50);

        final Collection<IssuePickerResults> issueResults = service.getResults(new MockJiraServiceContext(), params);
        assertNotNull(issueResults);
        assertEquals(1, issueResults.size());

        final Iterator<IssuePickerResults> ipResultsIterator = issueResults.iterator();
        IssuePickerResults results = ipResultsIterator.next();
        assertNotNull(results);
        assertEquals(1, results.getIssues().size());

        Issue issue = results.getIssues().iterator().next();
        assertEquals(JIRA_108, issue.getKey());
    }

    private Issue getMockIssue(final String key, final String summary)
    {
        return new MockIssue()
        {

            @Override
            public String getKey()
            {
                return key;
            }

            @Override
            public String getSummary()
            {
                return summary;
            }
        };
    }

    private static IssuePickerSearchProvider getProvider(final Collection<Issue> issues)
    {
        return new IssuePickerSearchProvider()
        {
            public IssuePickerResults getResults(final JiraServiceContext context, final IssuePickerSearchService.IssuePickerParameters issuePickerParams, final int maxIssueCount)
            {
                return new IssuePickerResults(issues, issues.size(), Collections.<String>emptyList(), Collections.<String>emptyList(), "Current Search", "current");
            }

            public boolean handlesParameters(final User searcher, final IssuePickerSearchService.IssuePickerParameters issuePickerParams)
            {
                return true;
            }
        };
    }

    private static IssuePickerSearchProvider getDisabledProvider()
    {
        return new IssuePickerSearchProvider()
        {
            public IssuePickerResults getResults(final JiraServiceContext context, final IssuePickerSearchService.IssuePickerParameters issuePickerParams, final int issueRemaining)
            {
                throw new UnsupportedOperationException();
            }

            public boolean handlesParameters(final User searcher, final IssuePickerSearchService.IssuePickerParameters issuePickerParams)
            {
                return false;
            }
        };
    }
}
