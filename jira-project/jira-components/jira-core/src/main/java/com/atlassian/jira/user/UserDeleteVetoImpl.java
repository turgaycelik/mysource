package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.entity.Entity;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;

public class UserDeleteVetoImpl implements UserDeleteVeto
{
    private final OfBizDelegator ofBizDelegator;
    private final UserKeyService userKeyService;

    public UserDeleteVetoImpl(final OfBizDelegator ofBizDelegator, final UserKeyService userKeyService)
    {
        this.ofBizDelegator = ofBizDelegator;
        this.userKeyService = userKeyService;
    }

    @Override
    public boolean allowDeleteUser(User user)
    {
        String userKey = userKeyService.getKeyForUser(user);
        // Check for assignee
        long count = ofBizDelegator.getCountByAnd(Entity.Name.ISSUE, new FieldMap("assignee", userKey));
        if (count > 0)
            return false;
        // Check for reporter
        count = ofBizDelegator.getCountByAnd(Entity.Name.ISSUE, new FieldMap("reporter", userKey));
        if (count > 0)
            return false;
        // Check for comment author
        if (getCommentCountByAuthor(userKey) > 0)
            return false;
        // No important references - allow the delete
        return true;
    }

    @Override
    public long getCommentCountByAuthor(final ApplicationUser user)
    {
        return getCommentCountByAuthor(user.getKey());
    }

    private long getCommentCountByAuthor(final String userKey)
    {
        return ofBizDelegator.getCountByAnd(Entity.Name.COMMENT, new FieldMap("author", userKey));
    }
}
