package com.atlassian.jira.issue.search;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.sharing.IndexableSharedEntity;
import com.atlassian.jira.sharing.SharedEntityAccessor.RetrievalDescriptor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.MockGroup;
import com.atlassian.jira.user.util.MockUserManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.collect.EnclosedIterable;
import com.atlassian.jira.util.collect.MockCloseableIterable;
import com.atlassian.query.QueryImpl;

import com.google.common.collect.ImmutableList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

/**
 * Test for {@link CachingSearchRequestStore}.
 *
 * @since v3.13
 */
@RunWith(MockitoJUnitRunner.class)
public class TestCachingSearchRequestStore
{
    @Mock
    private SearchRequestStore deletegateStore;
    private SearchRequestStore cachingStore;

    private ApplicationUser adminUser;
    private ApplicationUser fredUser;
    private Group group;

    private SearchRequest searchRequest1;
    private SearchRequest searchRequest2;
    private SearchRequest searchRequest3;
    private SearchRequest searchRequest4;

    @Before
    public void setUp() throws Exception
    {
        cachingStore = new CachingSearchRequestStore(deletegateStore, new MemoryCacheManager());

        adminUser = new MockApplicationUser("admin");
        group = new MockGroup("admin");
        fredUser = new MockApplicationUser("fredUser");
        MockUserManager userManager = new MockUserManager();
        userManager.addUser(adminUser);
        userManager.addUser(fredUser);

        new MockComponentWorker().addMock(UserManager.class, userManager).init();

        searchRequest1 = new SearchRequest(new QueryImpl(), adminUser, null, null, 1L, 0L);
        searchRequest2 = new SearchRequest(new QueryImpl(), adminUser, null, null, 2L, 0L);
        searchRequest3 = new SearchRequest(new QueryImpl(), adminUser, null, null, 3L, 0L);
        searchRequest4 = new SearchRequest(new QueryImpl(), fredUser, null, null, 4L, 0L);

        when(deletegateStore.getSearchRequest(1L)).thenReturn(searchRequest1);
        when(deletegateStore.getSearchRequest(2L)).thenReturn(searchRequest2);
        when(deletegateStore.getSearchRequest(3L)).thenReturn(searchRequest3);
        when(deletegateStore.getSearchRequest(4L)).thenReturn(searchRequest4);
    }

    @After
    public void tearDown() throws Exception
    {

    }

    /**
     * This should be a simple pass through.
     */
    @Test
    public void testGet()
    {
        final MockCloseableIterable<SearchRequest> expectedValue = new MockCloseableIterable<SearchRequest>(ImmutableList.<SearchRequest>of());

        MockRetrievalDescriptor expectedRetrievalDescriptor = new MockRetrievalDescriptor();

        when(deletegateStore.get(expectedRetrievalDescriptor)).thenReturn(expectedValue);

        assertSame(expectedValue, cachingStore.get(expectedRetrievalDescriptor));
        assertSame(expectedValue, cachingStore.get(expectedRetrievalDescriptor));
        assertSame(expectedValue, cachingStore.get(expectedRetrievalDescriptor));
    }

    /**
     * This should be a simple pass through.
     */
    @Test
    public void testAll()
    {
        final MockCloseableIterable<SearchRequest> expectedValue = new MockCloseableIterable<SearchRequest>(Collections.<SearchRequest>emptyList());

        when(deletegateStore.getAll()).thenReturn(expectedValue);
        assertSame(expectedValue, cachingStore.getAll());
        assertSame(expectedValue, cachingStore.getAll());
    }

    /**
     * This should be a simple pass through.
     */
    @Test
    public void testGetAllIndexableSharedEntities()
    {
        final MockCloseableIterable<IndexableSharedEntity<SearchRequest>> expectedValue =
                new MockCloseableIterable<IndexableSharedEntity<SearchRequest>>(Collections.<IndexableSharedEntity<SearchRequest>>emptyList());

        when(deletegateStore.getAllIndexableSharedEntities()).thenReturn(expectedValue);

        assertSame(expectedValue, cachingStore.getAllIndexableSharedEntities());
        assertSame(expectedValue, cachingStore.getAllIndexableSharedEntities());
    }

    /**
     * Check that we can cache the owned search.
     */
    @Test
    public void testGetAllOwnedSearchRequest()
    {
        final Collection<SearchRequest> expectedRequests = EasyList.build(searchRequest1, searchRequest2, searchRequest3);

        when(deletegateStore.getAllOwnedSearchRequests(adminUser.getKey())).thenReturn(expectedRequests);

        //put a value in the cache for the test.
        cachingStore.getSearchRequest(searchRequest1.getId());
        cachingStore.getSearchRequest(searchRequest4.getId());

        //this first call should not be cached and should delegate to getAllOwnedSearchRequests.
        assertEqualsNotSame(expectedRequests, cachingStore.getAllOwnedSearchRequests(adminUser));

        //these calls will now be cached.
        assertEqualsNotSame(expectedRequests, cachingStore.getAllOwnedSearchRequests(adminUser));
        assertEqualsNotSame(expectedRequests, cachingStore.getAllOwnedSearchRequests(adminUser));
        assertEqualsNotSame(expectedRequests, cachingStore.getAllOwnedSearchRequests(adminUser));

        //these should not also be cached.
        assertEqualsNotSame(searchRequest2, cachingStore.getSearchRequest(searchRequest2.getId()));
        assertEqualsNotSame(searchRequest3, cachingStore.getSearchRequest(searchRequest3.getId()));

    }

    /**
     * Check that we can get the owned search when a null value from the store is returned.
     */
    @Test
    public void testGetAllOwnedSearchRequestNullList()
    {
        when(deletegateStore.getAllOwnedSearchRequests(adminUser)).thenReturn(ImmutableList.<SearchRequest>of());

        assertEquals(0, cachingStore.getAllOwnedSearchRequests(adminUser).size());
        assertEquals(0, cachingStore.getAllOwnedSearchRequests(adminUser).size());
        assertEquals(0, cachingStore.getAllOwnedSearchRequests(adminUser).size());

    }

    /**
     * Check that exception is propgated and the results is not stored.
     */
    @Test
    public void testGetAllOwnedSearchRequestError()
    {
        when(deletegateStore.getAllOwnedSearchRequests(adminUser.getKey()))
                .thenThrow(new RuntimeException("testGetAllOwnedSearchRequestError"))
                .thenReturn(ImmutableList.<SearchRequest>of());

        try
        {
            cachingStore.getAllOwnedSearchRequests(adminUser);
            fail("Expecting an error.");
        }
        catch (RuntimeException expected)
        {
        }

        assertEquals(0, cachingStore.getAllOwnedSearchRequests(adminUser).size());
    }

    /**
     * This should be a simple call through.
     */
    @Test
    public void testGetRequestByAuthorAndName()
    {
        when(deletegateStore.getRequestByAuthorAndName(adminUser, "struff")).thenReturn(searchRequest1);

        when(deletegateStore.getRequestByAuthorAndName(null, "abc")).thenReturn(null);

        assertSame(searchRequest1, cachingStore.getRequestByAuthorAndName(adminUser, "struff"));
        assertNull(cachingStore.getRequestByAuthorAndName(null, "abc"));
    }

    /**
     * Test the we can get a particular request when it exists.
     */
    @Test
    public void testGetSearchRequest()
    {
        //this should call through because it is not in the cache.
        assertEqualsNotSame(searchRequest1, cachingStore.getSearchRequest(searchRequest1.getId()));

        //this should call through because it is not in the cache.
        assertEqualsNotSame(searchRequest2, cachingStore.getSearchRequest(searchRequest2.getId()));

        //these calls should be cached.
        assertEqualsNotSame(searchRequest1, cachingStore.getSearchRequest(searchRequest1.getId()));
        assertEqualsNotSame(searchRequest1, cachingStore.getSearchRequest(searchRequest1.getId()));
        assertEqualsNotSame(searchRequest1, cachingStore.getSearchRequest(searchRequest1.getId()));
        assertEqualsNotSame(searchRequest2, cachingStore.getSearchRequest(searchRequest2.getId()));
        assertEqualsNotSame(searchRequest2, cachingStore.getSearchRequest(searchRequest2.getId()));
        assertEqualsNotSame(searchRequest2, cachingStore.getSearchRequest(searchRequest2.getId()));
        assertEqualsNotSame(searchRequest2, cachingStore.getSearchRequest(searchRequest2.getId()));

        //this call should be direct through.
        assertEqualsNotSame(searchRequest4, cachingStore.getSearchRequest(searchRequest4.getId()));

        //these should be cached.
        assertEqualsNotSame(searchRequest4, cachingStore.getSearchRequest(searchRequest4.getId()));
        assertEqualsNotSame(searchRequest4, cachingStore.getSearchRequest(searchRequest4.getId()));
        assertEqualsNotSame(searchRequest4, cachingStore.getSearchRequest(searchRequest4.getId()));
        assertEqualsNotSame(searchRequest2, cachingStore.getSearchRequest(searchRequest2.getId()));
        assertEqualsNotSame(searchRequest1, cachingStore.getSearchRequest(searchRequest1.getId()));
    }

    /**
     * Test the we can't get a particular request when it does exist. We don't
     */
    @Test
    public void testGetSearchRequestDoesNotExist()
    {
        when(deletegateStore.getSearchRequest(5L)).thenReturn(null);

        assertNull(null, cachingStore.getSearchRequest(5L));

        assertEqualsNotSame(searchRequest2, cachingStore.getSearchRequest(searchRequest2.getId()));

        assertEqualsNotSame(searchRequest2, cachingStore.getSearchRequest(searchRequest2.getId()));
        assertEqualsNotSame(searchRequest2, cachingStore.getSearchRequest(searchRequest2.getId()));
        assertEqualsNotSame(searchRequest2, cachingStore.getSearchRequest(searchRequest2.getId()));
        assertEqualsNotSame(searchRequest2, cachingStore.getSearchRequest(searchRequest2.getId()));

        assertEqualsNotSame(searchRequest4, cachingStore.getSearchRequest(searchRequest4.getId()));

        assertEqualsNotSame(searchRequest4, cachingStore.getSearchRequest(searchRequest4.getId()));
        assertEqualsNotSame(searchRequest4, cachingStore.getSearchRequest(searchRequest4.getId()));
        assertEqualsNotSame(searchRequest4, cachingStore.getSearchRequest(searchRequest4.getId()));
        assertEqualsNotSame(searchRequest2, cachingStore.getSearchRequest(searchRequest2.getId()));

        //this will not be cached.
        assertNull(cachingStore.getSearchRequest(5L));

    }

    /**
     * Test the we can't get a particular request when it does exist. We don't
     */
    @Test
    public void testGetSearchRequestRuntimeException()
    {
        when(deletegateStore.getSearchRequest(5L)).thenThrow(new RuntimeException());

        try
        {
            cachingStore.getSearchRequest(5L);
            fail("Expecting an exception to be thrown by the store.");
        }
        catch (RuntimeException expected)
        {
            //ignored.
        }

        //this should call through because it is not in the cache.
        assertEqualsNotSame(searchRequest2, cachingStore.getSearchRequest(searchRequest2.getId()));

        //this should call through because it is not in the cache.
        assertEqualsNotSame(searchRequest1, cachingStore.getSearchRequest(searchRequest1.getId()));

        //these calls should be cached.
        assertEqualsNotSame(searchRequest2, cachingStore.getSearchRequest(searchRequest2.getId()));
        assertEqualsNotSame(searchRequest2, cachingStore.getSearchRequest(searchRequest2.getId()));
        assertEqualsNotSame(searchRequest2, cachingStore.getSearchRequest(searchRequest2.getId()));
        assertEqualsNotSame(searchRequest2, cachingStore.getSearchRequest(searchRequest2.getId()));
        assertEqualsNotSame(searchRequest1, cachingStore.getSearchRequest(searchRequest1.getId()));
        assertEqualsNotSame(searchRequest1, cachingStore.getSearchRequest(searchRequest1.getId()));
        assertEqualsNotSame(searchRequest1, cachingStore.getSearchRequest(searchRequest1.getId()));

    }

    /**
     * Does a created search request clear the old cached value (null)
     */
    @Test
    public void testCreate()
    {
        SearchRequest searchRequest5 = new SearchRequest(new QueryImpl(), adminUser, null, null, 5L, 0L);

        when(deletegateStore.getSearchRequest(5L))
                .thenReturn(null)
                .thenReturn(searchRequest5);
        when(deletegateStore.create(searchRequest5))
                .thenReturn(searchRequest5);

        assertNull(cachingStore.getSearchRequest(5L));

        //add the search request. Should now be in the cache.
        assertEqualsNotSame(searchRequest5, cachingStore.create(searchRequest5));

        assertEqualsNotSame(searchRequest5, cachingStore.getSearchRequest(5L));
        assertEqualsNotSame(searchRequest5, cachingStore.getSearchRequest(5L));
    }

    /**
     * Test the update when the page is not in the cache.
     */
    @Test
    public void testUpdateNotInCache()
    {
        SearchRequest searchRequest5 = new SearchRequest(new QueryImpl(), adminUser, null, null, 5L, 0L);
        SearchRequest searchRequest5b = new SearchRequest(new QueryImpl(), adminUser, null, null, 5L, 2L);

        when(deletegateStore.getSearchRequest(5L))
                .thenReturn(searchRequest5)
                .thenReturn(searchRequest5b);

        when(deletegateStore.update(searchRequest5)).thenReturn(searchRequest5b);

        assertEqualsNotSame(searchRequest5, cachingStore.getSearchRequest(searchRequest5.getId()));
        //add the search request. Should now be in the cache.
        assertEqualsNotSame(searchRequest5b, cachingStore.update(searchRequest5));

        assertEqualsNotSame(searchRequest5b, cachingStore.getSearchRequest(searchRequest5.getId()));
        assertEqualsNotSame(searchRequest5b, cachingStore.getSearchRequest(searchRequest5.getId()));

    }


    /**
     * Check what happens when the delegate store fails with null return.
     */
    @Test
    public void testUpdateFailsWithException()
    {
        SearchRequest searchRequest5 = new SearchRequest(new QueryImpl(), adminUser, null, null, 5L, 0L);
        SearchRequest searchRequest5b = new SearchRequest(new QueryImpl(), adminUser, null, null, 5L, 2L);

        when(deletegateStore.getSearchRequest(5L))
                .thenReturn(searchRequest5)
                .thenReturn(searchRequest5b);

        when(deletegateStore.update(searchRequest5)).thenThrow(new RuntimeException("testUpdateFailsWithException"));

        assertEqualsNotSame(searchRequest5, cachingStore.getSearchRequest(searchRequest5.getId()));

        try
        {
            assertEqualsNotSame(searchRequest5b, cachingStore.update(searchRequest5));
            fail("This exception should be thrown.");
        }
        catch (RuntimeException expected)
        {
        }

        assertEqualsNotSame(searchRequest5b, cachingStore.getSearchRequest(searchRequest5.getId()));
    }

    /**
     * Check what happens when try to save a search whose user has changed.
     */
    @Test
    public void testUpdateChangedUserName()
    {
        SearchRequest expectedRequest = new SearchRequest(searchRequest1);
        expectedRequest.setOwner(fredUser);

        when(deletegateStore.getAllOwnedSearchRequests(adminUser.getKey()))
                .thenReturn(EasyList.build(searchRequest1, searchRequest2, searchRequest3))
                .thenReturn(EasyList.build(searchRequest2, searchRequest3));

        when(deletegateStore.getAllOwnedSearchRequests(fredUser.getKey()))
                .thenReturn(EasyList.build(searchRequest4))
                .thenReturn(EasyList.build(searchRequest1, searchRequest4));

        when(deletegateStore.update(expectedRequest))
                .thenReturn(expectedRequest);

        assertEqualsNotSame(EasyList.build(searchRequest1, searchRequest2, searchRequest3), cachingStore.getAllOwnedSearchRequests(adminUser));
        assertEqualsNotSame(EasyList.build(searchRequest4), cachingStore.getAllOwnedSearchRequests(fredUser));

        //this call should work.
        assertEqualsNotSame(expectedRequest, cachingStore.update(expectedRequest));

        //all of these calls should be cache.
        assertEqualsNotSame(searchRequest2, cachingStore.getSearchRequest(searchRequest2.getId()));
        assertEqualsNotSame(searchRequest3, cachingStore.getSearchRequest(searchRequest3.getId()));
        assertEqualsNotSame(searchRequest4, cachingStore.getSearchRequest(searchRequest4.getId()));
        assertEqualsNotSame(searchRequest1, cachingStore.getSearchRequest(expectedRequest.getId()));

        assertEqualsNotSame(EasyList.build(searchRequest2, searchRequest3), cachingStore.getAllOwnedSearchRequests(adminUser));
        assertEqualsNotSame(EasyList.build(searchRequest1, searchRequest4), cachingStore.getAllOwnedSearchRequests(fredUser));

    }

    /**
     * Make sure that the adjust favourite count works when entity is in the cache.
     */
    @Test
    public void testAdjustFavouriteCountInCache()
    {
        SearchRequest searchRequest5 = new SearchRequest(new QueryImpl(), adminUser, null, null, 5L, 0L);
        SearchRequest searchRequest5b = new SearchRequest(new QueryImpl(), adminUser, null, null, 5L, 2L);

        when(deletegateStore.getSearchRequest(5L))
                .thenReturn(searchRequest5)
                .thenReturn(searchRequest5b);

        when(deletegateStore.adjustFavouriteCount(searchRequest5.getId(), 2))
                .thenReturn(searchRequest5b);

        assertEqualsNotSame(searchRequest5, cachingStore.getSearchRequest(searchRequest5.getId()));
        //adjust the search request.
        assertEqualsNotSame(searchRequest5b, cachingStore.adjustFavouriteCount(searchRequest5.getId(), 2));

        //all of these calls should be cached.
        assertEqualsNotSame(searchRequest5b, cachingStore.getSearchRequest(searchRequest5.getId()));
        assertEqualsNotSame(searchRequest5b, cachingStore.getSearchRequest(searchRequest5.getId()));
    }

    /**
     * Make sure we can delete a search request that is in the cache.
     */
    @Test
    public void testDeleteInCache()
    {
        SearchRequest searchRequest5 = new SearchRequest(new QueryImpl(), adminUser, null, null, 5L, 0L);

        when(deletegateStore.getAllOwnedSearchRequests(adminUser.getKey()))
                .thenReturn(EasyList.build(searchRequest1, searchRequest2, searchRequest5))
                .thenReturn(EasyList.build(searchRequest1, searchRequest2));


        when(deletegateStore.getSearchRequest(5L))
                .thenReturn(searchRequest5)
                .thenReturn(null);

        assertEqualsNotSame(searchRequest1, cachingStore.getSearchRequest(searchRequest1.getId()));
        assertEqualsNotSame(searchRequest5, cachingStore.getSearchRequest(searchRequest5.getId()));
        assertEqualsNotSame(EasyList.build(searchRequest1, searchRequest2, searchRequest5), cachingStore.getAllOwnedSearchRequests(adminUser));

        //execute the tests.
        cachingStore.delete(searchRequest5.getId());

        //these should be cached.
        assertEqualsNotSame(searchRequest1, cachingStore.getSearchRequest(searchRequest1.getId()));
        assertNull(cachingStore.getSearchRequest(searchRequest5.getId()));
        assertEqualsNotSame(EasyList.build(searchRequest1, searchRequest2), cachingStore.getAllOwnedSearchRequests(adminUser));

        assertEqualsNotSame(searchRequest2, cachingStore.getSearchRequest(searchRequest2.getId()));
    }

    /**
     * This should be a straight call through.
     */
    @Test
    public void testGetSearchRequestsProject()
    {
        final EnclosedIterable<SearchRequest> expectedIterable =
                new MockCloseableIterable<SearchRequest>(EasyList.build(searchRequest1, searchRequest4));
        final Project project = new MockProject();

        when(deletegateStore.getSearchRequests(project))
            .thenReturn(expectedIterable);

        assertEquals(expectedIterable, cachingStore.getSearchRequests(project));
    }

    /**
     * This should be a straight call through.
     */
    @Test
    public void testGetSearchRequestsGroup()
    {
        final EnclosedIterable<SearchRequest> expectedIterable =
                new MockCloseableIterable<SearchRequest>(EasyList.build(searchRequest4, searchRequest2));

        when(deletegateStore.getSearchRequests(group))
            .thenReturn(expectedIterable);

        assertEquals(expectedIterable, cachingStore.getSearchRequests(group));
    }

    private void assertEqualsNotSame(final Collection<?> expectedCollection, final Collection<?> actualCollection)
    {
        //we can't use equals here because we can't be sure of return order.
        assertTrue("Collections where not of the correct size.", expectedCollection.size() == actualCollection.size());
        assertTrue("Collections did not contain the same elements.", expectedCollection.containsAll(actualCollection));

        for (Iterator expectedIterator = expectedCollection.iterator(), actualIterator = actualCollection.iterator(); expectedIterator.hasNext();)
        {
            assertNotSame(expectedIterator.next(), actualIterator.next());
        }
    }

    private void assertEqualsNotSame(final SearchRequest expectedRequest, final SearchRequest actualRequest)
    {
        assertNotSame(expectedRequest, actualRequest);
        assertEquals(expectedRequest, actualRequest);
    }

    private static class MockRetrievalDescriptor implements RetrievalDescriptor
    {
        public List<Long> getIds()
        {
            return null;
        }

        public boolean preserveOrder()
        {
            return false;
        }
    }
}
