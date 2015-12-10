package com.atlassian.jira.issue.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.crowd.embedded.impl.IdentifierUtils;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.sharing.IndexableSharedEntity;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.SharedEntityAccessor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.DelegatingApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.MockUserManager;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.util.collect.EnclosedIterable;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * Test for {@link com.atlassian.jira.issue.search.OfBizSearchRequestStore} with real data.
 */

public class TestOfBizSearchRequestStoreData
{

    private static final class Table
    {
        static final String NAME = OfBizSearchRequestStore.Table.NAME;
    }

    private static final String USER = "testofbizsearchrequeststoredata_user";
    private static final String BOB = "testofbizsearchrequeststoredata_bob";

    @Rule
    public RuleChain mockitoMocksInContainer = MockitoMocksInContainer.forTest(this);

    @AvailableInContainer
    private final OfBizDelegator ofBizDelegator = new MockOfBizDelegator();

    @AvailableInContainer
    private final MockUserManager userManager = new MockUserManager();

    private SearchRequestStore store;

    @Mock
    private SearchService searchService;

    private final ApplicationUser user = new DelegatingApplicationUser(TestOfBizSearchRequestStoreData.USER, new MockUser(
            TestOfBizSearchRequestStoreData.USER));

    @Before
    public void setUp() throws Exception
    {
        userManager.addUser(user);
        store = new OfBizSearchRequestStore(ofBizDelegator, null, searchService, userManager)
        {
            @Override
            Query getSearchQueryFromGv(final GenericValue searchRequestGv)
            {
                return new QueryImpl();
            }
        };
    }

    @Test(expected = IllegalArgumentException.class)
    public void testContructorWithNullDelegator()
    {
        new OfBizSearchRequestStore(null, null, null, userManager);
        fail("Should not accept null delegator.");
    }

    @Test
    public void testGetAllOwnedSearchRequestsNullUser()
    {
        assertEquals(Collections.emptyList(), store.getAllOwnedSearchRequests((ApplicationUser) null));
    }

    @Test
    public void testGetAllOwnedSearchRequestsNoSRs()
    {
        assertEquals(Collections.emptyList(), store.getAllOwnedSearchRequests(user));
    }

    @Test
    public void testGetAllOwnedSearchRequestsNoMatched()
    {
        UtilsForTests.getTestEntity(Table.NAME, ImmutableMap.<String, Object> of("author", TestOfBizSearchRequestStoreData.BOB));
        UtilsForTests.getTestEntity(Table.NAME, ImmutableMap.<String, Object> of("author", TestOfBizSearchRequestStoreData.BOB));
        UtilsForTests.getTestEntity(Table.NAME, ImmutableMap.<String, Object> of("author", "nick"));

        assertEquals(Collections.emptyList(), store.getAllOwnedSearchRequests(user));
    }

    @Test
    public void testGetAllOwnedSearchRequestsAllMatched() throws Exception
    {
        final GenericValue gv1 = UtilsForTests.getTestEntity(Table.NAME,
                ImmutableMap.<String, Object> builder()
                        .put("author", IdentifierUtils.toLowerCase(TestOfBizSearchRequestStoreData.USER))
                        .put("name", "one")
                        .put("request", "<xml/>")
                        .put("description", "test desc")
                        .put("id", 12345L)
                        .put("favCount", 0L)
                        .build());
        final GenericValue gv2 = UtilsForTests.getTestEntity(Table.NAME,
                ImmutableMap.<String, Object> builder()
                        .put("author", IdentifierUtils.toLowerCase(TestOfBizSearchRequestStoreData.USER))
                        .put("name", "two")
                        .put("request", "<xml/>")
                        .put("description", "test desc")
                        .put("id", 23456L)
                        .put("favCount", 0L)
                        .build());
        final GenericValue gv3 = UtilsForTests.getTestEntity(Table.NAME,
                ImmutableMap.<String, Object> builder()
                        .put("author", IdentifierUtils.toLowerCase(TestOfBizSearchRequestStoreData.USER))
                        .put("name", "three")
                        .put("request", "<xml/>")
                        .put("description", "test desc")
                        .put("id", 34567L)
                        .put("favCount", 0L)
                        .build());

        final Collection<SearchRequest> expectedResults = Lists.newArrayList(getSearchRequestFromGV(gv1), getSearchRequestFromGV(gv3),
                getSearchRequestFromGV(gv2));

        final Collection<SearchRequest> result = store.getAllOwnedSearchRequests(user);
        assertEquals(expectedResults, result);
    }

    private SearchRequest getSearchRequestFromGV(final GenericValue srGv)
    {
        return new SearchRequest(new QueryImpl(), new MockApplicationUser(srGv.getString("author")), srGv.getString("name"),
                srGv.getString("description"), srGv.getLong("id"), srGv.getLong("favCount"));
    }

    @Test
    public void testGetAllOwnedSearchRequestsSomeMatched() throws Exception
    {
        final GenericValue gv1 = UtilsForTests.getTestEntity(Table.NAME,
                ImmutableMap.<String, Object> builder()
                        .put("author", IdentifierUtils.toLowerCase(TestOfBizSearchRequestStoreData.USER))
                        .put("name", "one")
                        .put("request", "<xml/>")
                        .put("description", "test desc")
                        .put("id", 12345L)
                        .put("favCount", 0L)
                        .build());
        UtilsForTests.getTestEntity(Table.NAME,
                ImmutableMap.<String, Object> builder()
                        .put("author", IdentifierUtils.toLowerCase(TestOfBizSearchRequestStoreData.BOB))
                        .put("name", "two")
                        .put("request", "<xml/>")
                        .put("description", "test desc")
                        .put("id", 34567L)
                        .put("favCount", 0L)
                        .build());
        final GenericValue gv3 = UtilsForTests.getTestEntity(Table.NAME,
                ImmutableMap.<String, Object> builder()
                        .put("author", IdentifierUtils.toLowerCase(TestOfBizSearchRequestStoreData.USER))
                        .put("name", "three")
                        .put("request", "<xml/>")
                        .put("description", "test desc")
                        .put("id", 23456L)
                        .put("favCount", 0L)
                        .build());

        final Collection<SearchRequest> expectedResults = Lists.newArrayList(getSearchRequestFromGV(gv1), getSearchRequestFromGV(gv3));

        final Collection<SearchRequest> result = store.getAllOwnedSearchRequests(user);
        assertEquals(expectedResults, result);
    }

    @Test
    public void testGetRequestByAuthorAndNameNullUserNullName()
    {
        final SearchRequest sr = store.getRequestByAuthorAndName(null, null);
        assertNull(sr);
    }

    @Test
    public void testGetRequestByAuthorAndNameNullName()
    {
        UtilsForTests.getTestEntity(Table.NAME,
                ImmutableMap.of("author", TestOfBizSearchRequestStoreData.USER, "name", "one", "request", "<xml/>"));
        final SearchRequest sr = store.getRequestByAuthorAndName(user, null);
        assertNull(sr);
    }

    @Test
    public void testGetRequestByAuthorAndNameUserHasNone()
    {
        UtilsForTests.getTestEntity(Table.NAME,
                ImmutableMap.of("author", TestOfBizSearchRequestStoreData.BOB, "name", "one", "request", "<xml/>"));
        UtilsForTests.getTestEntity(Table.NAME,
                ImmutableMap.of("author", TestOfBizSearchRequestStoreData.BOB, "name", "one", "request", "<xml/>"));

        final SearchRequest sr = store.getRequestByAuthorAndName(user, "one");
        assertNull(sr);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRequestByAuthorAndNameMultiMatch()
    {
        UtilsForTests.getTestEntity(Table.NAME,
                ImmutableMap.of("author", TestOfBizSearchRequestStoreData.USER, "name", "one", "request", "<xml/>"));
        UtilsForTests.getTestEntity(Table.NAME,
                ImmutableMap.of("author", TestOfBizSearchRequestStoreData.USER, "name", "one", "request", "<xml/>"));

        store.getRequestByAuthorAndName(user, "one");
        fail("Should fail with multiple in the database");
    }

    @Test
    public void testGetRequestByAuthorAndNameMultiMatchDiffUsers() throws Exception
    {
        final GenericValue gv1 = UtilsForTests.getTestEntity(Table.NAME,
                ImmutableMap.<String, Object> builder()
                        .put("author", IdentifierUtils.toLowerCase(TestOfBizSearchRequestStoreData.USER))
                        .put("name", "one")
                        .put("request", "<xml/>")
                        .put("description", "test desc")
                        .put("id", 12345L)
                        .put("favCount", 0L)
                        .build());
        UtilsForTests.getTestEntity(Table.NAME,
                ImmutableMap.<String, Object> builder()
                        .put("author", IdentifierUtils.toLowerCase(TestOfBizSearchRequestStoreData.BOB))
                        .put("name", "one")
                        .put("request", "<xml/>")
                        .put("description", "test desc")
                        .put("id", 23456L)
                        .put("favCount", 0L)
                        .build());

        final SearchRequest sr = store.getRequestByAuthorAndName(user, "one");
        assertEquals(getSearchRequestFromGV(gv1), sr);
    }

    @Test
    public void testGetRequestByAuthorAndNameUserHasMultiDifferent() throws Exception
    {
        final GenericValue gv1 = UtilsForTests.getTestEntity(Table.NAME,
                ImmutableMap.<String, Object> builder()
                        .put("author", IdentifierUtils.toLowerCase(TestOfBizSearchRequestStoreData.USER))
                        .put("name", "one")
                        .put("request", "<xml/>")
                        .put("description", "test desc")
                        .put("id", 12345L)
                        .put("favCount", 0L)
                        .build());
        UtilsForTests.getTestEntity(Table.NAME,
                ImmutableMap.<String, Object> builder()
                        .put("author", IdentifierUtils.toLowerCase(TestOfBizSearchRequestStoreData.USER))
                        .put("name", "two")
                        .put("request", "<xml/>")
                        .build());

        final SearchRequest sr = store.getRequestByAuthorAndName(user, "one");
        assertEquals(getSearchRequestFromGV(gv1), sr);
    }

    @Test
    public void testGetSearchRequestNullId()
    {
        final SearchRequest sr = store.getSearchRequest(null);
        assertNull(sr);
    }

    @Test
    public void testGetSearchRequestIDNoExist()
    {
        final SearchRequest sr = store.getSearchRequest(new Long(1));
        assertNull(sr);
    }

    @Test
    public void testGetSearchRequestIDNoExistSomeDo()
    {
        UtilsForTests.getTestEntity(Table.NAME, ImmutableMap.<String, Object> builder()
                .put("author", TestOfBizSearchRequestStoreData.USER)
                .put("name", "one")
                .put("request", "<xml/>")
                .build());
        final SearchRequest sr = store.getSearchRequest(new Long(986));
        assertNull(sr);
    }

    @Test
    public void testGetSearchRequestIDExists() throws Exception
    {
        final GenericValue gv1 = UtilsForTests.getTestEntity(
                Table.NAME,
                ImmutableMap.<String, Object> builder()
                        .put("author", TestOfBizSearchRequestStoreData.USER)
                        .put("name", "one")
                        .put("request", "<xml/>")
                        .put("description", "test desc")
                        .put("id", 12345L)
                        .put("favCount", 0L)
                        .build());
        final SearchRequest expectedSR = getSearchRequestFromGV(gv1);
        final SearchRequest sr = store.getSearchRequest(expectedSR.getId());
        assertEquals(expectedSR, sr);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveNullRequest()
    {
        store.create(null);
        fail("can not save null search request");
    }

    @Test
    public void testSaveHappy() throws Exception
    {
        final SearchRequest sr = new SearchRequest(new QueryImpl(null, null, "project = 123"), user, "one", "description one", null, 0L);
        final SearchRequest result = store.create(sr);
        assertNotNull(result);

        final List<GenericValue> stored = ofBizDelegator.findAll(Table.NAME);
        assertEquals(1, stored.size());
        final GenericValue storedGV = stored.iterator().next();
        assertEquals(sr.getName(), storedGV.getString("name"));
        assertEquals(sr.getDescription(), storedGV.getString("description"));
        assertEquals(new Long(0), storedGV.getLong("favCount"));

        final SearchRequest sr2 = new SearchRequest(new QueryImpl(null, null, "blah = blee"), user, "one222", "description one222", null,
                0L);
        final SearchRequest result2 = store.create(sr2);
        assertNotNull(result2);

        final List<GenericValue> stored2 = ofBizDelegator.findAll(Table.NAME);
        assertEquals(2, stored2.size());
        final Iterator<GenericValue> iterator = stored2.iterator();
        iterator.next();
        final GenericValue storedGV2 = iterator.next();
        assertEquals(sr2.getName(), storedGV2.getString("name"));
        assertEquals(sr2.getDescription(), storedGV2.getString("description"));
        assertEquals(new Long(0), storedGV.getLong("favCount"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateNullRequest()
    {
        store.update(null);
        fail("Can not update null request");
    }

    @Test
    public void testUpdateHappy() throws Exception
    {
        final GenericValue gv1 = UtilsForTests.getTestEntity(
                Table.NAME,
                ImmutableMap.<String, Object> builder()
                        .put("author", TestOfBizSearchRequestStoreData.USER)
                        .put("name", "one")
                        .put("description", "hello")
                        .put("request", "<xml/>")
                        .put("id", 12345L)
                        .put("favCount", 0L)
                        .build());
        final SearchRequest expectedSR = getSearchRequestFromGV(gv1);
        final SearchRequest sr = store.getSearchRequest(expectedSR.getId());

        sr.setName("new Name");
        sr.setDescription("new Description");
        sr.setQuery(new QueryImpl(null, null, "project = 1234"));
        assertEquals(new Long(0), sr.getFavouriteCount());

        final SearchRequest result = store.update(sr);
        assertNotNull(result);
        assertEquals("new Name", result.getName());
        assertEquals("new Description", result.getDescription());
        assertEquals(new Long(0), sr.getFavouriteCount());
    }

    @Test
    public void testUpdateChangedFavCount() throws Exception
    {
        final GenericValue gv1 = UtilsForTests.getTestEntity(
                Table.NAME,
                ImmutableMap.<String, Object> builder()
                        .put("author", TestOfBizSearchRequestStoreData.USER)
                        .put("name", "one")
                        .put("description", "hello")
                        .put("request", "<xml/>")
                        .put("id", 12345L)
                        .put("favCount", 0L)
                        .build());
        final SearchRequest expectedSR = getSearchRequestFromGV(gv1);
        final SearchRequest sr = store.getSearchRequest(expectedSR.getId());

        sr.setName("new Name");
        sr.setDescription("new Description");
        assertEquals(new Long(0), sr.getFavouriteCount());
        sr.setFavouriteCount(new Long(2));
        sr.setQuery(new QueryImpl(null, null, "project = 123"));
        gv1.store();

        final SearchRequest result = store.update(sr);
        assertNotNull(result);
        assertEquals("new Name", result.getName());
        assertEquals("new Description", result.getDescription());
    }

    @Test(expected = DataAccessException.class)
    public void testRemoveIdNotExist()
    {
        store.delete(new Long(133));
        fail("can not delete request where id does not exist");
    }

    @Test
    public void testRemoveHappy()
    {
        final GenericValue gv1 = UtilsForTests.getTestEntity(Table.NAME,
                ImmutableMap.of("author", TestOfBizSearchRequestStoreData.USER, "name", "one", "request", "<xml/>"));

        store.delete(gv1.getLong("id"));

        final List<GenericValue> stored = ofBizDelegator.findAll(Table.NAME);
        assertEquals(Collections.emptyList(), stored);
    }

    @Test
    public void testRemoveOneOfMany()
    {
        final GenericValue gv1 = UtilsForTests.getTestEntity(Table.NAME,
                ImmutableMap.of("author", TestOfBizSearchRequestStoreData.USER, "name", "one", "request", "<xml/>"));
        final GenericValue gv2 = UtilsForTests.getTestEntity(Table.NAME,
                ImmutableMap.of("author", TestOfBizSearchRequestStoreData.USER, "name", "one", "request", "<xml/>"));
        final GenericValue gv3 = UtilsForTests.getTestEntity(Table.NAME,
                ImmutableMap.of("author", TestOfBizSearchRequestStoreData.USER, "name", "one", "request", "<xml/>"));

        final List<GenericValue> expected = Lists.newArrayList(gv2, gv3);
        store.delete(gv1.getLong("id"));

        final List<GenericValue> stored = ofBizDelegator.findAll(Table.NAME);
        assertEquals(expected, stored);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetSearchRequestsForProjectNullProject()
    {
        store.getSearchRequests((Project) null);
        fail("can not get requests for null project");
    }

    /**
     * Make sure updating the favourite count works.
     * 
     * @throws SearchException
     *             throw up to indicate some form of error.
     */
    @Test
    public void testAdjustFavouriteCount() throws SearchException
    {
        final GenericValue srGv = UtilsForTests.getTestEntity(Table.NAME,
                ImmutableMap.of("author", TestOfBizSearchRequestStoreData.USER, "name", "one", "request", "<xml/>"));
        SearchRequest searchRequest = new SearchRequest(new QueryImpl(), user, "one", null, srGv.getLong("id"), 0L);

        // the initial value should be zero.
        assertEquals(0, searchRequest.getFavouriteCount().longValue());

        // increment the value +10.
        searchRequest = store.adjustFavouriteCount(searchRequest.getId(), 10);
        assertEquals(10, searchRequest.getFavouriteCount().longValue());

        // decrement the value to 4.
        searchRequest = store.adjustFavouriteCount(searchRequest.getId(), -6);
        assertEquals(4, searchRequest.getFavouriteCount().longValue());

        // decrement the value to 0.
        searchRequest = store.adjustFavouriteCount(searchRequest.getId(), -100);
        assertEquals(0, searchRequest.getFavouriteCount().longValue());
    }

    @Test
    public void testGetEmpty()
    {
        final EnclosedIterable<SearchRequest> iterable = store.get(new RetrievalDescriptor(Collections.<Long> emptyList()));
        assertNotNull(iterable);
        assertTrue(iterable.isEmpty());
    }

    /**
     * Calling get with preserver order set to false means the returned objects can be any order but must all be there
     */
    @Test
    public void testGetNoOrder()
    {
        addGV(10000, "fred", "sr1", "sr1 description");
        addGV(10001, "fred", "sr2", "sr2 description");
        addGV(10002, "mary", "sr1", "sr1 description");
        addGV(10003, "mary", "sr2", "sr2 description");

        final List<Long> expectedList = Lists.newArrayList(10003L, 10000L, 10001L);
        final EnclosedIterable<SearchRequest> iterable = store.get(new RetrievalDescriptor(expectedList, false));
        assertNotNull(iterable);
        assertEquals(3, iterable.size());
        //
        // order cant be guaranteed here
        assertIterableHasAllIds(iterable, new long[] { 10001, 10000, 10003 });
    }

    /**
     * Calling get with preserver order set to true means the returned objects MUST be in id list order
     */
    @Test
    public void testGetWithOrder()
    {
        addGV(10000, "fred", "sr1", "sr1 description");
        addGV(10001, "fred", "sr2", "sr2 description");
        addGV(10002, "mary", "sr1", "sr1 description");
        addGV(10003, "mary", "sr2", "sr2 description");

        final List<Long> expectedList = Lists.newArrayList(10003L, 10000L, 10001L);

        final EnclosedIterable<SearchRequest> iterable = store.get(new RetrievalDescriptor(expectedList, true));
        assertNotNull(iterable);
        assertEquals(3, iterable.size());
        //
        // order CAN be guaranteed here
        final List<Long> actualList = toIdList(iterable);
        assertEquals(expectedList, actualList);
    }

    @Test
    public void testGetAll()
    {
        EnclosedIterable<SearchRequest> iterable = store.getAll();
        assertNotNull(iterable);
        assertTrue(iterable.isEmpty());

        addGV(10000, "fred", "sr1", "sr1 description");
        addGV(10001, "fred", "sr2", "sr2 description");
        addGV(10002, "mary", "sr1", "sr1 description");
        addGV(10003, "mary", "sr2", "sr2 description");

        iterable = store.getAll();
        assertNotNull(iterable);
        assertEquals(4, iterable.size());
        assertIterableHasAllIds(iterable, new long[] { 10000, 10001, 10002, 10003 });
    }

    @Test
    public void testGetAllIndexableSharedEntities()
    {
        EnclosedIterable<IndexableSharedEntity<SearchRequest>> iterable = store.getAllIndexableSharedEntities();
        assertNotNull(iterable);
        assertTrue(iterable.isEmpty());

        addGV(10000, "fred", "sr1", "sr1 description");
        addGV(10001, "fred", "sr2", "sr2 description");
        addGV(10002, "mary", "sr1", "sr1 description");
        addGV(10003, "mary", "sr2", "sr2 description");

        iterable = store.getAllIndexableSharedEntities();
        assertNotNull(iterable);
        assertEquals(4, iterable.size());

        final long[] expectedIds = new long[] { 10000, 10001, 10002, 10003 };
        assertEquals(expectedIds.length, iterable.size());
        assertCollectionHasAllIds(new EnclosedIterable.ListResolver<IndexableSharedEntity<SearchRequest>>().get(iterable), expectedIds);
    }

    private GenericValue addGV(final long id, final String username, final String name, final String description)
    {
        final Map<String, Comparable<?>> map = new HashMap<String, Comparable<?>>();
        map.put("id", id);
        map.put("name", name);
        map.put("description", description);
        map.put("author", username);
        map.put("favCount", 0L);
        return UtilsForTests.getTestEntity(Table.NAME, map);
    }

    private void assertIterableHasAllIds(final EnclosedIterable<SearchRequest> iterable, final long[] expectedIds)
    {
        assertEquals(expectedIds.length, iterable.size());
        assertCollectionHasAllIds(new EnclosedIterable.ListResolver<SearchRequest>().get(iterable), expectedIds);
    }

    private void assertCollectionHasAllIds(final Collection<? extends SharedEntity> collection, final long[] expectedIds)
    {
        final Set<Long> foundIds = new HashSet<Long>();
        for (final SharedEntity sr : collection)
        {
            for (final long id : expectedIds)
            {
                if (sr.getId() == id)
                {
                    if (foundIds.contains(id))
                    {
                        fail("The id : " + id + " was already found in the collection.  Ids must be unique");
                    }
                    else
                    {
                        foundIds.add(id);
                    }
                }
            }
        }
        if (foundIds.size() != expectedIds.length)
        {
            fail("Failed to find specified all ids");
        }
    }

    private List<Long> toIdList(final EnclosedIterable<SearchRequest> iterable)
    {
        assertNotNull(iterable);
        return CollectionUtil.transform(new EnclosedIterable.ListResolver<SearchRequest>().get(iterable),
                new Function<SearchRequest, Long>()
                {
                    @Override
                    public Long get(final SearchRequest input)
                    {
                        return input.getId();
                    }
                });
    }

    private class RetrievalDescriptor implements SharedEntityAccessor.RetrievalDescriptor
    {

        private final List<Long> ids;

        private final boolean preserverOrder;

        RetrievalDescriptor(final List<Long> ids)
        {
            this(ids, false);
        }

        RetrievalDescriptor(final List<Long> ids, final boolean preserverOrder)
        {
            this.ids = ids;
            this.preserverOrder = preserverOrder;
        }

        @Override
        public List<Long> getIds()
        {
            return ids;
        }

        @Override
        public boolean preserveOrder()
        {
            return preserverOrder;
        }
    }
}
