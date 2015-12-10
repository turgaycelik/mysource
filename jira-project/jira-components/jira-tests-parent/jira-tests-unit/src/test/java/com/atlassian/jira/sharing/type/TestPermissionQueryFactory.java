package com.atlassian.jira.sharing.type;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.security.groups.MockGroupManager;
import com.atlassian.jira.sharing.index.PermissionQueryFactory;
import com.atlassian.jira.sharing.search.SharedEntitySearchParameters;
import com.atlassian.jira.sharing.search.SharedEntitySearchParametersBuilder;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockUser;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * A test case for PermissionQueryFactory
 *
 * @since v3.13
 */
public class TestPermissionQueryFactory
{
    private User admin;
    private MockGroupManager mockGroupManager = new MockGroupManager();

    @Before
    public void setUp()
    {
        new MockComponentWorker().init();
        admin = new MockUser("admin");
        // Set up the mock GroupManager
        mockGroupManager.addMember("group1", "admin");
        mockGroupManager.addMember("group2", "admin");
        ComponentAccessor.initialiseWorker(new MockComponentWorker());
    }

    @After
    public void tearDown()
    {
        admin = null;
        mockGroupManager = null;
    }

    @Test
    public void testCreate_WithoutUser()
    {
        final SharedEntitySearchParameters expectedSearchParameters = new SharedEntitySearchParametersBuilder().toSearchParameters();

        PermissionQueryFactory permissionQueryFactory = new PermissionQueryFactory(new MockProjectShareQueryFactory(), null);
        try
        {
            final Query query = permissionQueryFactory.create(expectedSearchParameters);
            fail("UnsupportedOperationException should have been thrown");
        }
        catch (UnsupportedOperationException ignored)
        {
        }
    }

    @Test
    public void testCreate()
    {
        final SharedEntitySearchParameters expectedSearchParameters = new SharedEntitySearchParametersBuilder().toSearchParameters();

        PermissionQueryFactory permissionQueryFactory = new PermissionQueryFactory(new MockProjectShareQueryFactory(), mockGroupManager);
        final Query query = permissionQueryFactory.create(expectedSearchParameters, admin);
        assertNotNull(query);
        assertEquals("owner:admin shareTypeGlobal:true shareTypeGroup:group1 shareTypeGroup:group2 MockProjectShareQueryFactory:admin", query.toString());
    }

    @Test
    public void testCreate_withNullUser()
    {
        final SharedEntitySearchParameters expectedSearchParameters = new SharedEntitySearchParametersBuilder().toSearchParameters();

        PermissionQueryFactory permissionQueryFactory = new PermissionQueryFactory(new MockProjectShareQueryFactory(), mockGroupManager);
        final Query query = permissionQueryFactory.create(expectedSearchParameters, null);
        assertNotNull(query);
        assertEquals("shareTypeGlobal:true MockProjectShareQueryFactory:null", query.toString());
    }

    private class MockProjectShareQueryFactory extends ProjectShareQueryFactory
    {
        private MockProjectShareQueryFactory()
        {
            super(null);
        }

        @Override
        public Term[] getTerms(final ApplicationUser user)
        {
            return new Term[] { new Term("MockProjectShareQueryFactory", user != null ? user.getKey() : "null") };
        }
    }
}
