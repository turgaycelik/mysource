package com.atlassian.jira.issue.search;

import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.query.QueryImpl;

public class MockSearchRequest extends SearchRequest
{
    public static MockSearchRequest get(final String userName, final long id)
    {
        return new MockSearchRequest(userName, id);
    }

    public MockSearchRequest(final String userName, final Long id, final String name)
    {
        super(new QueryImpl(), new MockApplicationUser(userName), name, "desc", id, 0L);
    }

    public MockSearchRequest(final String userName, final Long id)
    {
        super(new QueryImpl(), new MockApplicationUser(userName), "name", "desc", id, 0L);
    }

    public MockSearchRequest(final String userName)
    {
        super(new QueryImpl(), new MockApplicationUser(userName), "name", "desc", null, 0L);
    }
}
