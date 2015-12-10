/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.plugin.searchrequestview.auth;

import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestParams;
import com.atlassian.jira.security.groups.DefaultGroupManager;
import com.atlassian.jira.user.MockGroup;
import com.atlassian.jira.user.MockUser;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestUserAuthorizer
{
    private User privilegedUser;
    private User unprivilegedUser;
    private Group group;
    private final AtomicBoolean delegateCalled = new AtomicBoolean();
    private final Authorizer delegate = new Authorizer()
    {
        @Override
        public Result isSearchRequestAuthorized(final User user, final SearchRequest searchRequest, final SearchRequestParams params)
        {
            delegateCalled.set(true);
            return Result.OK;
        }
    };
    private final SearchRequest request = null;
    private final SearchRequestParams params = null;

    @Test
    public void testPrivilegedUser()
    {
        Authorizer authorizer = new UserAuthorizer(group, delegate, new MockGroupManager(true, group));

        assertEquals(Authorizer.Result.OK, authorizer.isSearchRequestAuthorized(privilegedUser, request, params));
        assertFalse(delegateCalled.get());
    }

    @Test
    public void testUnprivilegedUser()
    {
        Authorizer authorizer = new UserAuthorizer(group, delegate, new MockGroupManager(false, group));
        // will be ok, but delegate called
        assertEquals(Authorizer.Result.OK, authorizer.isSearchRequestAuthorized(unprivilegedUser, request, params));
        assertTrue(delegateCalled.get());
    }

    @Test
    public void testNullUser()
    {
        Authorizer authorizer = new UserAuthorizer(group, delegate, new MockGroupManager(false, null));
        assertEquals(Authorizer.Result.OK, authorizer.isSearchRequestAuthorized(null, request, params));
        assertTrue(delegateCalled.get());
    }

    @Test
    public void testNullGroup()
    {
        try
        {
            new UserAuthorizer(null, delegate, new MockGroupManager(false, null));
            fail("should have thrown something");
        }
        catch (final Exception yay)
        {}
    }

    @Test
    public void testNullDelegate()
    {
        try
        {
            new UserAuthorizer(group, null, new MockGroupManager(false, null));
            fail("should have thrown something");
        }
        catch (final Exception yay)
        {}
    }

    @Before
    public void setUp()
    {
        privilegedUser = new MockUser("test");
        unprivilegedUser = new MockUser("nong");
        group = new MockGroup("test");

        delegateCalled.set(false);
    }

    private class MockGroupManager extends DefaultGroupManager
    {
        private boolean userIsInGroup;
        private Group group;

        public MockGroupManager(boolean userIsInGroup, final Group group)
        {
            super(null);
            this.userIsInGroup = userIsInGroup;
            this.group = group;
        }

        @Override
        public boolean isUserInGroup(final com.atlassian.crowd.embedded.api.User user, final com.atlassian.crowd.embedded.api.Group group)
        {
            return userIsInGroup;
        }

        @Override
        public Group getGroup(final String groupname)
        {
            return group;
        }
    }

}
