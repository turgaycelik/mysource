package com.atlassian.jira.issue.search;

import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;

/**
 * A mock search request to use when testing JQL code.
 *
 * @since v4.0
 */
public class MockJqlSearchRequest extends SearchRequest
{
    public MockJqlSearchRequest(final Long id, final Query query)
    {
        super((query == null) ? new QueryImpl() : query, new MockApplicationUser("admin"), "mock sr", "mock sr desc", id, 0L);
    }
}
