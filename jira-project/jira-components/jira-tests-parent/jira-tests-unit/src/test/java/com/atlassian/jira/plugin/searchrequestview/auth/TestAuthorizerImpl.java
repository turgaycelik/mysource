package com.atlassian.jira.plugin.searchrequestview.auth;

import java.util.concurrent.atomic.AtomicReference;

import com.atlassian.core.test.util.DuckTypeProxy;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.security.groups.DefaultGroupManager;
import com.atlassian.jira.user.MockGroup;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestAuthorizerImpl
{
    private final SearchProvider searchProvider = (SearchProvider) DuckTypeProxy.getProxy(SearchProvider.class, new Object());
    private final AtomicReference<String> groupName = new AtomicReference<String>();
    private final AtomicReference<String> count = new AtomicReference<String>();
    private final Object props = new Object()
    {
        @SuppressWarnings("unused")
        public String getDefaultBackedString(final String string)
        {
            if (!"jira.search.views.max.limit".equals(string))
            {
                return groupName.get();
            }
            return count.get();
        }
    };
    ApplicationProperties properties = (ApplicationProperties) DuckTypeProxy.getProxy(ApplicationProperties.class, props);

    @Before
    public void setUp() throws Exception
    {
        groupName.set(null);
        count.set("10");
    }

    @Test
    public void testNullSearchProviderCtor() throws Exception
    {
        try
        {
            new AuthorizerImpl(null, properties, new MockGroupManager(false, null));
            fail("Should have thrown ex");
        }
        catch (final RuntimeException yay)
        {}
    }

    @Test
    public void testNullPropertiesCtor() throws Exception
    {
        try
        {
            new AuthorizerImpl(searchProvider, null, new MockGroupManager(false, null));
            fail("Should have thrown ex");
        }
        catch (final RuntimeException yay)
        {}
    }

    @Test
    public void testNulluserManagerCtor() throws Exception
    {
        try
        {
            new AuthorizerImpl(searchProvider, properties, null);
            fail("Should have thrown ex");
        }
        catch (final RuntimeException yay)
        {}
    }

    @Test
    public void testUnknownGroup() throws Exception
    {
        final String name = "blah-de-blah-de-blah";
        groupName.set(name);
        try
        {
            new AuthorizerImpl(searchProvider, properties, new MockGroupManager(false, null));
        }
        catch (final RuntimeException e)
        {
            assertTrue(e.getMessage().contains(name));
        }
    }

    @Test
    public void testKnownGroup() throws Exception
    {
        final String name = "i-am-a-teapot";
        groupName.set(name);
        final Group theGroup = new MockGroup(name);

        final AuthorizerImpl authorizer = new AuthorizerImpl(searchProvider, properties, new MockGroupManager(true, theGroup));
        final Authorizer delegate = authorizer.getDelegate();
        assertTrue(delegate instanceof UserAuthorizer);
        final UserAuthorizer userAuthorizer = (UserAuthorizer) delegate;
        assertEquals(theGroup, userAuthorizer.getGroup());
    }

    @Test
    public void testNullGroupDoesntCreateUserAuthorizer() throws Exception
    {
        groupName.set(null);

        final AuthorizerImpl authorizer = new AuthorizerImpl(searchProvider, properties, new MockGroupManager(false, null));
        final Authorizer delegate = authorizer.getDelegate();
        assertFalse(delegate instanceof UserAuthorizer);
    }

    @Test
    public void testCrapGroupDoesntCreateUserAuthorizer() throws Exception
    {
        groupName.set("i-am-not-a-teapot-i-am-a-mug");

        final AuthorizerImpl authorizer = new AuthorizerImpl(searchProvider, properties, new MockGroupManager(false, null));
        final Authorizer delegate = authorizer.getDelegate();
        assertFalse(delegate instanceof UserAuthorizer);
    }

    @Test
    public void testSizeAuthorizerCreated() throws Exception
    {
        groupName.set(null);

        final AuthorizerImpl authorizer = new AuthorizerImpl(searchProvider, properties, new MockGroupManager(false, null));
        final Authorizer delegate = authorizer.getDelegate();
        assertTrue(delegate instanceof SearchResultSizeAuthorizer);
        final SearchResultSizeAuthorizer searchResultSizeAuthorizer = (SearchResultSizeAuthorizer) delegate;
        assertEquals(10, searchResultSizeAuthorizer.getMaxAllowed());
    }

    @Test
    public void testNoLimit() throws Exception
    {
        count.set(null);

        final AuthorizerImpl authorizer = new AuthorizerImpl(searchProvider, properties, new MockGroupManager(false, null));
        final Authorizer delegate = authorizer.getDelegate();
        assertSame(Authorizer.ALWAYS, delegate);
    }

    @Test
    public void testCtor() throws Exception
    {
        new AuthorizerImpl(searchProvider, properties, new MockGroupManager(false, null));
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
        public boolean isUserInGroup(final User user, final com.atlassian.crowd.embedded.api.Group group)
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