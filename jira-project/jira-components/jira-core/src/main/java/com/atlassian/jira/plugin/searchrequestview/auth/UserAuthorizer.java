/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.plugin.searchrequestview.auth;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestParams;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.util.dbc.Null;

class UserAuthorizer implements Authorizer
{
    // --------------------------------------------------------------------------------------------------------- members

    private final Group allowedGroup;
    private final Authorizer delegate;
    private GroupManager groupManager;

    // ----------------------------------------------------------------------------------------------------------- ctors

    public UserAuthorizer(final Group allowedGroup, final Authorizer delegate, final GroupManager groupManager)
    {
        Null.not("allowedGroup", allowedGroup);
        Null.not("delegate", delegate);
        Null.not("groupManager", groupManager);

        this.allowedGroup = allowedGroup;
        this.delegate = delegate;
        this.groupManager = groupManager;
    }

    // --------------------------------------------------------------------------------------------------------- methods

    public Result isSearchRequestAuthorized(User user, SearchRequest searchRequest, SearchRequestParams params)
    {
        // secondly, does there user have the right priv?
        if ((user != null) && groupManager.isUserInGroup(user, allowedGroup))
        {
            return Result.OK;
        }
        return delegate.isSearchRequestAuthorized(user, searchRequest, params);
    }

    Authorizer getDelegate()
    {
        return delegate;
    }

    Group getGroup()
    {
        return allowedGroup;
    }
}