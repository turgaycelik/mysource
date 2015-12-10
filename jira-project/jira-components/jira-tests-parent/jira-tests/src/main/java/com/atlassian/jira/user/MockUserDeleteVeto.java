package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.api.User;

/**
 * @since v6.1
 */
public class MockUserDeleteVeto implements UserDeleteVeto
{
    private boolean defaultAllow = true;
    private int defaultCommentCount;

    @Override
    public boolean allowDeleteUser(final User user)
    {
        return defaultAllow;
    }

    @Override
    public long getCommentCountByAuthor(final ApplicationUser user)
    {
        return defaultCommentCount;
    }

    public void setDefaultAllow(final boolean defaultAllow)
    {
        this.defaultAllow = defaultAllow;
    }

    public MockUserDeleteVeto setDefaultCommentCount(final int defaultCommentCount)
    {
        this.defaultCommentCount = defaultCommentCount;
        return this;
    }
}
