package com.atlassian.jira.issue.search.searchers;

import com.atlassian.jira.issue.fields.SearchableField;

/**
 * An implementation an IssueSearcher for testing.
 *
 * @since v4.0
 */
public class MockSystemSearcher extends MockIssueSearcher<SearchableField>
{
    public MockSystemSearcher(final String id)
    {
        super(id);
    }
}
