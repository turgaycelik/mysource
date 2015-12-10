package com.atlassian.jira.issue.search.parameters.lucene;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;

import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

public class TestPermissionFilterGeneratorImpl extends MockControllerTestCase
{
    private ApplicationUser theAppUser;
    private User theUser;

    @Before
    public void setUp()
    {
        theAppUser = new MockApplicationUser("fred");
        theUser = theAppUser.getDirectoryUser();
    }

    @After
    public void tearDown()
    {
        theAppUser = null;
        theUser = null;
    }

    @Test
    public void testGetQueryNoCache() throws Exception
    {
        final PermissionsFilterCache cache = mockController.getMock(PermissionsFilterCache.class);
        final BooleanQuery generatedQuery = new BooleanQuery();

        expect(cache.getQuery(theUser)).andReturn(null);

        cache.storeQuery(generatedQuery, theUser);
        expectLastCall();

        replay();
        final PermissionQueryFactory permissionQueryFactory = new PermissionQueryFactory()
        {
            public Query getQuery(ApplicationUser searcher, int permissionId)
            {
                assertEquals(theAppUser, searcher);
                assertEquals(Permissions.BROWSE, permissionId);
                return generatedQuery;
            }
        };

        final PermissionsFilterGeneratorImpl generator = new PermissionsFilterGeneratorImpl(permissionQueryFactory)
        {
            @Override
            PermissionsFilterCache getCache()
            {
                return cache;
            }
        };

        final Query result = generator.getQuery(theUser);

        assertSame(generatedQuery, result);
    }

    @Test
    public void testGetQueryCached() throws Exception
    {
        final PermissionsFilterCache cache = mockController.getMock(PermissionsFilterCache.class);
        final BooleanQuery cachedQuery = new BooleanQuery();

        expect(cache.getQuery(theUser)).andReturn(cachedQuery);

        replay();

        final PermissionQueryFactory permissionQueryFactory = new PermissionQueryFactory()
        {
            public Query getQuery(ApplicationUser searcher, int permissionId)
            {
                fail("Should not be called as query was cached");
                return null;
            }
        };

        final PermissionsFilterGeneratorImpl generator = new PermissionsFilterGeneratorImpl(permissionQueryFactory)
        {
            @Override
            PermissionsFilterCache getCache()
            {
                return cache;
            }
        };

        final Query result = generator.getQuery(theUser);

        assertSame(cachedQuery, result);
    }
}
