package com.atlassian.jira.favourites;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;

import org.easymock.MockControl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test for @{link TestCachingFavouritesStore}.
 *
 * @since 4.0.
 */
public class TestCachingFavouritesStore
{
    private ApplicationUser testuser;
    private MockControl favStoreCtrl;

    private FavouritesStore favStore;
    private PortalPage entity1;
    private static final Long ENTITY1_ID = new Long(10 );
    private static final Long ENTITY2_ID = new Long(11);
    private PortalPage entity2;

    @Before
    public void setUp() throws Exception
    {
        testuser = new MockApplicationUser("testUser");
        favStoreCtrl = MockControl.createStrictControl(FavouritesStore.class);
        favStore = (FavouritesStore) favStoreCtrl.getMock();

        entity1 = PortalPage.id(ENTITY1_ID).name("page1").owner(testuser).build();
        entity2 = PortalPage.id(ENTITY2_ID).name("page2").owner(testuser).build();
    }

    @Test
    public void testAddFavouriteHappy()
    {
        List expectedPrimeList = EasyList.build(ENTITY1_ID);
        List expectedResultList = EasyList.build(ENTITY1_ID, ENTITY2_ID);

        favStore.getFavouriteIds(testuser.getKey(), PortalPage.ENTITY_TYPE);
        favStoreCtrl.setReturnValue(expectedPrimeList);

        favStore.addFavourite(testuser, entity1);
        favStoreCtrl.setReturnValue(true);

        favStore.getFavouriteIds(testuser.getKey(), PortalPage.ENTITY_TYPE);
        favStoreCtrl.setReturnValue(expectedResultList);

        FavouritesStore store = createFavouriteStore();

        Collection ids = store.getFavouriteIds(testuser, PortalPage.ENTITY_TYPE);
        assertEquals(expectedPrimeList, ids);

        assertTrue(store.addFavourite(testuser, entity1));
        assertEquals(expectedResultList, store.getFavouriteIds(testuser, PortalPage.ENTITY_TYPE));
        assertEquals(expectedResultList, store.getFavouriteIds(testuser, PortalPage.ENTITY_TYPE));

        verifyMocks();
    }

    @Test
    public void testAddFavouriteFalse()
    {
        List expectedPrimeList = EasyList.build(ENTITY1_ID);
        List expectedResultList = EasyList.build(ENTITY1_ID, ENTITY2_ID);

        favStore.getFavouriteIds(testuser.getKey(), PortalPage.ENTITY_TYPE);
        favStoreCtrl.setReturnValue(expectedPrimeList);

        favStore.addFavourite(testuser, entity1);
        favStoreCtrl.setReturnValue(false);

        favStore.getFavouriteIds(testuser.getKey(), PortalPage.ENTITY_TYPE);
        favStoreCtrl.setReturnValue(expectedResultList);

        FavouritesStore store = createFavouriteStore();

        Collection ids = store.getFavouriteIds(testuser, PortalPage.ENTITY_TYPE);
        assertEquals(expectedPrimeList, ids);

        assertFalse(store.addFavourite(testuser, entity1));
        assertEquals(expectedResultList, store.getFavouriteIds(testuser, PortalPage.ENTITY_TYPE));
        assertEquals(expectedResultList, store.getFavouriteIds(testuser, PortalPage.ENTITY_TYPE));

        verifyMocks();
    }

    @Test
    public void testRemoveFavouriteHappy()
    {
        List expectedPrimeList = EasyList.build(ENTITY1_ID);
        List expectedResultList = EasyList.build(ENTITY1_ID, ENTITY2_ID);

        favStore.getFavouriteIds(testuser.getKey(), PortalPage.ENTITY_TYPE);
        favStoreCtrl.setReturnValue(expectedPrimeList);

        favStore.removeFavourite(testuser, entity1);
        favStoreCtrl.setReturnValue(true);

        favStore.getFavouriteIds(testuser.getKey(), PortalPage.ENTITY_TYPE);
        favStoreCtrl.setReturnValue(expectedResultList);

        FavouritesStore store = createFavouriteStore();

        Collection ids = store.getFavouriteIds(testuser, PortalPage.ENTITY_TYPE);
        assertEquals(expectedPrimeList, ids);

        assertTrue(store.removeFavourite(testuser, entity1));
        assertEquals(expectedResultList, store.getFavouriteIds(testuser, PortalPage.ENTITY_TYPE));
        assertEquals(expectedResultList, store.getFavouriteIds(testuser, PortalPage.ENTITY_TYPE));

        verifyMocks();
    }

    @Test
    public void testRemoveFavouriteFalse()
    {
        List expectedPrimeList = EasyList.build(ENTITY1_ID);
        List expectedResultList = EasyList.build(ENTITY1_ID, ENTITY2_ID);

        favStore.getFavouriteIds(testuser.getKey(), PortalPage.ENTITY_TYPE);
        favStoreCtrl.setReturnValue(expectedPrimeList);

        favStore.removeFavourite(testuser, entity1);
        favStoreCtrl.setReturnValue(false);

        favStore.getFavouriteIds(testuser.getKey(), PortalPage.ENTITY_TYPE);
        favStoreCtrl.setReturnValue(expectedResultList);

        FavouritesStore store = createFavouriteStore();

        Collection ids = store.getFavouriteIds(testuser, PortalPage.ENTITY_TYPE);
        assertEquals(expectedPrimeList, ids);

        assertFalse(store.removeFavourite(testuser, entity1));
        assertEquals(expectedResultList, store.getFavouriteIds(testuser, PortalPage.ENTITY_TYPE));
        assertEquals(expectedResultList, store.getFavouriteIds(testuser, PortalPage.ENTITY_TYPE));

        verifyMocks();
    }


    @Test
    public void testIsFavouriteHappy()
    {
        List expectedPrimeList = EasyList.build(ENTITY1_ID);

        favStore.getFavouriteIds(testuser.getKey(), PortalPage.ENTITY_TYPE);
        favStoreCtrl.setReturnValue(expectedPrimeList);

        FavouritesStore store = createFavouriteStore();

        assertTrue(store.isFavourite(testuser, entity1));
        assertTrue(store.isFavourite(testuser, entity1));
        assertFalse(store.isFavourite(testuser, entity2));

        verifyMocks();
    }

    @Test
    public void testIsFavouriteNullEntry()
    {
        favStore.getFavouriteIds(testuser.getKey(), PortalPage.ENTITY_TYPE);
        favStoreCtrl.setReturnValue(null);

        FavouritesStore store = createFavouriteStore();

        assertFalse(store.isFavourite(testuser, entity1));
        assertFalse(store.isFavourite(testuser, entity1));
        assertFalse(store.isFavourite(testuser, entity2));

        verifyMocks();
    }

    @Test
    public void testGetFavouriteIdsHappy()
    {
        final List expectedSearchRequestIds = EasyList.build(new Long(7987), new Long(Integer.MIN_VALUE << 3));
        final List expectedPortalPageIds = EasyList.build(new Long(79), new Long(Integer.MIN_VALUE << 4));

        favStore.getFavouriteIds(testuser.getKey(), SearchRequest.ENTITY_TYPE);
        favStoreCtrl.setReturnValue(expectedSearchRequestIds);

        favStore.getFavouriteIds(testuser.getKey(), PortalPage.ENTITY_TYPE);
        favStoreCtrl.setReturnValue(expectedPortalPageIds);

        FavouritesStore store = createFavouriteStore();

        assertEquals(expectedSearchRequestIds, store.getFavouriteIds(testuser, SearchRequest.ENTITY_TYPE));

        //these calls should be caches.
        assertEquals(expectedSearchRequestIds, store.getFavouriteIds(testuser, SearchRequest.ENTITY_TYPE));
        assertEquals(expectedSearchRequestIds, store.getFavouriteIds(testuser, SearchRequest.ENTITY_TYPE));
        assertEquals(expectedSearchRequestIds, store.getFavouriteIds(testuser, SearchRequest.ENTITY_TYPE));

        store.getFavouriteIds(testuser, SearchRequest.ENTITY_TYPE);

        assertEquals(expectedPortalPageIds, store.getFavouriteIds(testuser, PortalPage.ENTITY_TYPE));

        //these calls should be caches.
        assertEquals(expectedPortalPageIds, store.getFavouriteIds(testuser, PortalPage.ENTITY_TYPE));
        assertEquals(expectedPortalPageIds, store.getFavouriteIds(testuser, PortalPage.ENTITY_TYPE));

        verifyMocks();
    }

    @Test
    public void testGetFavouriteIdNullFromStore()
    {

        favStore.getFavouriteIds(testuser.getKey(), SearchRequest.ENTITY_TYPE);
        favStoreCtrl.setReturnValue(null);

        FavouritesStore store = createFavouriteStore();

        assertEquals(Collections.EMPTY_LIST, store.getFavouriteIds(testuser, SearchRequest.ENTITY_TYPE));

        //these calls should be caches.
        assertEquals(Collections.EMPTY_LIST, store.getFavouriteIds(testuser, SearchRequest.ENTITY_TYPE));
        assertEquals(Collections.EMPTY_LIST, store.getFavouriteIds(testuser, SearchRequest.ENTITY_TYPE));
        assertEquals(Collections.EMPTY_LIST, store.getFavouriteIds(testuser, SearchRequest.ENTITY_TYPE));

        verifyMocks();
    }

    @Test
    public void testremoveFavouritesForUserHappy()
    {
        List expectedPrimeList = EasyList.build(ENTITY1_ID);
        List expectedResultList = EasyList.build(ENTITY1_ID, ENTITY2_ID);

        favStore.getFavouriteIds(testuser.getKey(), PortalPage.ENTITY_TYPE);
        favStoreCtrl.setReturnValue(expectedPrimeList);

        favStore.removeFavouritesForUser(testuser, PortalPage.ENTITY_TYPE);

        favStore.getFavouriteIds(testuser.getKey(), PortalPage.ENTITY_TYPE);
        favStoreCtrl.setReturnValue(expectedResultList);

        FavouritesStore store = createFavouriteStore();

        Collection ids = store.getFavouriteIds(testuser, PortalPage.ENTITY_TYPE);
        assertEquals(expectedPrimeList, ids);

        store.removeFavouritesForUser(testuser, PortalPage.ENTITY_TYPE);

        assertEquals(expectedResultList, store.getFavouriteIds(testuser, PortalPage.ENTITY_TYPE));
        assertEquals(expectedResultList, store.getFavouriteIds(testuser, PortalPage.ENTITY_TYPE));

        verifyMocks();
    }

    @Test
    public void testremoveFavouritesForUserException()
    {
        List expectedPrimeList = EasyList.build(ENTITY1_ID);
        List expectedResultList = EasyList.build(ENTITY1_ID, ENTITY2_ID);

        favStore.getFavouriteIds(testuser.getKey(), PortalPage.ENTITY_TYPE);
        favStoreCtrl.setReturnValue(expectedPrimeList);

        favStore.removeFavouritesForUser(testuser, PortalPage.ENTITY_TYPE);
        favStoreCtrl.setThrowable(new RuntimeException("blash"));

        favStore.getFavouriteIds(testuser.getKey(), PortalPage.ENTITY_TYPE);
        favStoreCtrl.setReturnValue(expectedResultList);

        FavouritesStore store = createFavouriteStore();

        Collection ids = store.getFavouriteIds(testuser, PortalPage.ENTITY_TYPE);
        assertEquals(expectedPrimeList, ids);


        try
        {
            store.removeFavouritesForUser(testuser, PortalPage.ENTITY_TYPE);
            fail("Expected exception to be thrown.");
        }
        catch (Exception ignored)
        {
        }

        assertEquals(expectedResultList, store.getFavouriteIds(testuser, PortalPage.ENTITY_TYPE));
        assertEquals(expectedResultList, store.getFavouriteIds(testuser, PortalPage.ENTITY_TYPE));

        verifyMocks();
    }

    @Test
    public void testRemoveFavouritesForEntity()
    {
        ApplicationUser userBad = new MockApplicationUser("userBad");
        ApplicationUser userBad2 = new MockApplicationUser("userBad2");

        List<Long> portalList1 = EasyList.build(10L);
        List<Long> portalList2 = EasyList.build(10L, 11L);
        List<Long> portalList3 = EasyList.build(9L, 11L);

        List<Long> searchRequest1 = EasyList.build(10L);

        favStore.getFavouriteIds(testuser.getKey(), PortalPage.ENTITY_TYPE);
        favStoreCtrl.setReturnValue(portalList1);

        favStore.getFavouriteIds(userBad.getKey(), PortalPage.ENTITY_TYPE);
        favStoreCtrl.setReturnValue(portalList2);

        favStore.getFavouriteIds(userBad2.getKey(), PortalPage.ENTITY_TYPE);
        favStoreCtrl.setReturnValue(portalList3);

        favStore.getFavouriteIds(testuser.getKey(), SearchRequest.ENTITY_TYPE);
        favStoreCtrl.setReturnValue(searchRequest1);

        favStore.removeFavouritesForEntity(entity1);

        // After the remove the cache should be clear and everything need reloading
        favStore.getFavouriteIds(testuser.getKey(), PortalPage.ENTITY_TYPE);
        favStoreCtrl.setReturnValue(portalList1);

        favStore.getFavouriteIds(userBad.getKey(), PortalPage.ENTITY_TYPE);
        favStoreCtrl.setReturnValue(portalList2);

        favStore.getFavouriteIds(userBad2.getKey(), PortalPage.ENTITY_TYPE);
        favStoreCtrl.setReturnValue(portalList3);

        favStore.getFavouriteIds(testuser.getKey(), SearchRequest.ENTITY_TYPE);
        favStoreCtrl.setReturnValue(searchRequest1);

        FavouritesStore store = createFavouriteStore();

        store.getFavouriteIds(testuser, PortalPage.ENTITY_TYPE);
        store.getFavouriteIds(userBad, PortalPage.ENTITY_TYPE);
        store.getFavouriteIds(userBad2, PortalPage.ENTITY_TYPE);
        store.getFavouriteIds(testuser, SearchRequest.ENTITY_TYPE);

        store.removeFavouritesForEntity(entity1);

        store.getFavouriteIds(testuser, PortalPage.ENTITY_TYPE);
        store.getFavouriteIds(userBad, PortalPage.ENTITY_TYPE);
        store.getFavouriteIds(userBad2, PortalPage.ENTITY_TYPE);
        store.getFavouriteIds(testuser, SearchRequest.ENTITY_TYPE);

        verifyMocks();

    }


    @Test
    public void testrUpdateSequenceHappy()
    {
        List expectedPrimeList = EasyList.build(ENTITY1_ID);
        List expectedResultList = EasyList.build(ENTITY1_ID, ENTITY2_ID);
        List expectedSequenceList = EasyList.build(entity1);

        favStore.getFavouriteIds(testuser.getKey(), PortalPage.ENTITY_TYPE);
        favStoreCtrl.setReturnValue(expectedPrimeList);

        favStore.updateSequence(testuser, expectedSequenceList);

        favStore.getFavouriteIds(testuser.getKey(), PortalPage.ENTITY_TYPE);
        favStoreCtrl.setReturnValue(expectedResultList);

        FavouritesStore store = createFavouriteStore();

        Collection ids = store.getFavouriteIds(testuser, PortalPage.ENTITY_TYPE);
        assertEquals(expectedPrimeList, ids);

        store.updateSequence(testuser, expectedSequenceList);

        assertEquals(expectedResultList, store.getFavouriteIds(testuser.getKey(), PortalPage.ENTITY_TYPE));
        assertEquals(expectedResultList, store.getFavouriteIds(testuser.getKey(), PortalPage.ENTITY_TYPE));

        verifyMocks();
    }

    @Test
    public void testrUpdateSequenceEmptyList()
    {
        List expectedPrimeList = EasyList.build(ENTITY1_ID);
        List expectedSequenceList = EasyList.build();

        favStore.getFavouriteIds(testuser.getKey(), PortalPage.ENTITY_TYPE);
        favStoreCtrl.setReturnValue(expectedPrimeList);

        favStore.updateSequence(testuser, expectedSequenceList);

        FavouritesStore store = createFavouriteStore();

        Collection ids = store.getFavouriteIds(testuser, PortalPage.ENTITY_TYPE);
        assertEquals(expectedPrimeList, ids);

        store.updateSequence(testuser, expectedSequenceList);

        assertEquals(expectedPrimeList, store.getFavouriteIds(testuser.getKey(), PortalPage.ENTITY_TYPE));
        assertEquals(expectedPrimeList, store.getFavouriteIds(testuser.getKey(), PortalPage.ENTITY_TYPE));

        verifyMocks();
    }


    private void verifyMocks()
    {
        favStoreCtrl.verify();
    }

    private FavouritesStore createFavouriteStore()
    {
        favStoreCtrl.replay();

        return new CachingFavouritesStore(favStore, new MemoryCacheManager());
    }
}
