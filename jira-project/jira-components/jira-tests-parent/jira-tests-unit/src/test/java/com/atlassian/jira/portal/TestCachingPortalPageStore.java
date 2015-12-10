package com.atlassian.jira.portal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;

import com.google.common.collect.ImmutableList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;


/**
 * Test for the {@link CachingPortalPageStore}.
 *
 * @since 4.0
 */
@RunWith (MockitoJUnitRunner.class)
public class TestCachingPortalPageStore
{
    private static final Long SYSTEM_DEFAULT_ID = new Long(32);
    private static final Long PAGE1_ID = new Long(1);
    private static final Long PAGE2_ID = new Long(2);
    private static final Long PAGE_FRED_ID = new Long(3);

    private ApplicationUser user1;
    private ApplicationUser fred;

    @Mock
    private PortalPageStore delegateStore;


    private PortalPage page1;
    private PortalPage page2;
    private PortalPage pageFred;
    private PortalPage system;

    @Before
    public void setUp() throws Exception
    {
        user1 = new MockApplicationUser("admin");
        fred = new MockApplicationUser("fred");
        page1 = PortalPage.id(PAGE1_ID).name("Page 1").description("Page 1 Description").owner(user1).build();
        page2 = PortalPage.id(PAGE2_ID).name("Page 2").description("Page 2 Description").owner(user1).build();
        pageFred = PortalPage.id(PAGE_FRED_ID).name("Fred Page").description("Fred Page Description").owner(fred).build();
        system = PortalPage.id(SYSTEM_DEFAULT_ID).name("System Default").description("System Default Description").systemDashboard().build();

        when(delegateStore.getPortalPage(PAGE1_ID)).thenReturn(page1);
        when(delegateStore.getPortalPage(PAGE2_ID)).thenReturn(page2);
        when(delegateStore.getPortalPage(PAGE_FRED_ID)).thenReturn(pageFred);
    }

    @After
    public void tearDown() throws Exception
    {
        delegateStore = null;
        page1 = null;
        page2 = null;
        user1 = null;
        fred = null;
        system = null;
    }

    @Test
    public void testGetSystemDefaultPortalPage()
    {
        when(delegateStore.getSystemDefaultPortalPage()).thenReturn(system);

        when(delegateStore.getPortalPage(SYSTEM_DEFAULT_ID)).thenReturn(system);

        PortalPageStore store = createCachingStore();

        // This should read from database
        assertEqualsButNotSame(system, store.getSystemDefaultPortalPage());

        // This should be in cache now.
        assertEqualsButNotSame(system, store.getSystemDefaultPortalPage());
        assertEqualsButNotSame(system, store.getPortalPage(SYSTEM_DEFAULT_ID));
        assertEqualsButNotSame(system, store.getSystemDefaultPortalPage());
        assertEqualsButNotSame(system, store.getPortalPage(SYSTEM_DEFAULT_ID));
    }

    @Test
    public void testGetAllOwnedPortalPages()
    {
        final List expectedList = EasyList.build(page1, page2);
        final List expectedListFred = EasyList.build(pageFred);

        when(delegateStore.getAllOwnedPortalPages(user1.getKey())).thenReturn(expectedList);
        when(delegateStore.getAllOwnedPortalPages(fred.getKey())).thenReturn(expectedListFred);

        PortalPageStore store = createCachingStore();

        // This should read all values from datbase
        assertEqualsButNotSame(page1, store.getPortalPage(PAGE1_ID));
        assertEqualsButNotSame(expectedList, store.getAllOwnedPortalPages(user1));
        assertEqualsButNotSame(expectedListFred, store.getAllOwnedPortalPages(fred));

        //This should read from cache
        assertEqualsButNotSame(expectedList, store.getAllOwnedPortalPages(user1));
        assertEqualsButNotSame(expectedList, store.getAllOwnedPortalPages(user1));
        assertEqualsButNotSame(expectedListFred, store.getAllOwnedPortalPages(fred));
        assertEqualsButNotSame(expectedListFred, store.getAllOwnedPortalPages(fred));

        //These should have been placed in cache
        assertEqualsButNotSame(page1, store.getPortalPage(PAGE1_ID));
        assertEqualsButNotSame(page1, store.getPortalPage(PAGE1_ID));
        assertEqualsButNotSame(page2, store.getPortalPage(PAGE2_ID));
        assertEqualsButNotSame(page2, store.getPortalPage(PAGE2_ID));
        assertEqualsButNotSame(pageFred, store.getPortalPage(PAGE_FRED_ID));
        assertEqualsButNotSame(pageFred, store.getPortalPage(PAGE_FRED_ID));
    }


    @Test
    public void testGetAllOwnedPortalPagesNullFromDB()
    {
        when(delegateStore.getAllOwnedPortalPages(user1.getKey())).thenReturn(ImmutableList.<PortalPage>of());

        PortalPageStore store = createCachingStore();

        assertEqualsButNotSame(page1, store.getPortalPage(PAGE1_ID));

        assertEquals(0, store.getAllOwnedPortalPages(user1).size());

        assertEquals(0, store.getAllOwnedPortalPages(user1).size());

        assertEqualsButNotSame(page1, store.getPortalPage(PAGE1_ID));
        assertEqualsButNotSame(page1, store.getPortalPage(PAGE1_ID));

        assertEqualsButNotSame(page2, store.getPortalPage(PAGE2_ID));

        assertEqualsButNotSame(page2, store.getPortalPage(PAGE2_ID));

    }

    @Test
    public void testGetPortalPageByOwnerAndName()
    {
        when(delegateStore.getPortalPageByOwnerAndName(user1, "nick")).thenReturn(page1);
        when(delegateStore.getPortalPageByOwnerAndName(user1, "nick2")).thenReturn(null);

        PortalPageStore store = createCachingStore();

        assertSame(page1, store.getPortalPageByOwnerAndName(user1, "nick"));
        assertNull(store.getPortalPageByOwnerAndName(user1, "nick2"));
    }

    @Test
    public void testGetPortalPage()
    {
        when(delegateStore.getPortalPage(PAGE1_ID)).thenReturn(page1);
        final Long badId = new Long(999);
        when(delegateStore.getPortalPage(badId)).thenReturn(null);
        when(delegateStore.getPortalPage(badId)).thenReturn(null);
        when(delegateStore.getPortalPage(PAGE2_ID)).thenReturn(page2);


        PortalPageStore store = createCachingStore();

        // This should read through to the cache
        assertEqualsButNotSame(page1, store.getPortalPage(PAGE1_ID));

        //Both of these should go to the cache
        assertNull(store.getPortalPage(badId));
        assertNull(store.getPortalPage(badId));

        // This should read through to the cache
        assertEqualsButNotSame(page2, store.getPortalPage(PAGE2_ID));

        // These should be cached
        assertEqualsButNotSame(page1, store.getPortalPage(PAGE1_ID));
        assertEqualsButNotSame(page2, store.getPortalPage(PAGE2_ID));
        assertEqualsButNotSame(page1, store.getPortalPage(PAGE1_ID));
        assertEqualsButNotSame(page2, store.getPortalPage(PAGE2_ID));
    }

    @Test
    public void testCreate()
    {
        final List expectedList = EasyList.build(page1, page2);
        final List expectedListFred = EasyList.build(pageFred);

        //prime
        when(delegateStore.getAllOwnedPortalPages(user1.getKey())).thenReturn(expectedList);
        when(delegateStore.getAllOwnedPortalPages(fred.getKey())).thenReturn(expectedListFred);
        when(delegateStore.create(page1)).thenReturn(page1);
        when(delegateStore.getAllOwnedPortalPages(user1)).thenReturn(expectedList);

        PortalPageStore store = createCachingStore();

        // Prime cache
        store.getAllOwnedPortalPages(user1);
        store.getAllOwnedPortalPages(fred);

        assertEqualsButNotSame(page1, store.create(page1));

        //This should still be cached
        assertEqualsButNotSame(page1, store.getPortalPage(PAGE1_ID));
        assertEqualsButNotSame(page2, store.getPortalPage(PAGE2_ID));
        assertEqualsButNotSame(expectedListFred, store.getAllOwnedPortalPages(fred));

        //This should go back to database
        assertEqualsButNotSame(expectedList, store.getAllOwnedPortalPages(user1));

        //And now be cached
        assertEqualsButNotSame(expectedList, store.getAllOwnedPortalPages(user1));
        assertEqualsButNotSame(expectedListFred, store.getAllOwnedPortalPages(fred));
        assertEqualsButNotSame(page1, store.getPortalPage(PAGE1_ID));
        assertEqualsButNotSame(page2, store.getPortalPage(PAGE2_ID));
    }


    @Test
    public void testUpdate()
    {
        final List expectedList = EasyList.build(page1, page2);

        //prime
        when(delegateStore.getAllOwnedPortalPages(user1.getKey())).thenReturn(expectedList);
        when(delegateStore.update(page1)).thenReturn(page1);

        PortalPageStore store = createCachingStore();

        store.getAllOwnedPortalPages(user1);

        //This will update the db and return new one
        assertEqualsButNotSame(page1, store.update(page1));

        //This will hit the cache
        assertEqualsButNotSame(page1, store.getPortalPage(PAGE1_ID));
        assertEqualsButNotSame(page2, store.getPortalPage(PAGE2_ID));
        assertEqualsButNotSame(expectedList, store.getAllOwnedPortalPages(user1));
    }

    @Test
    public void testUpdateNullSystemDefaultReturn()
    {
        final List expectedList = EasyList.build(page1, page2, system);

        //prime
        when(delegateStore.getAllOwnedPortalPages(user1.getKey())).thenReturn(expectedList);
        when(delegateStore.getSystemDefaultPortalPage()).thenReturn(system);
        when(delegateStore.update(system)).thenReturn(null);
        when(delegateStore.getPortalPage(SYSTEM_DEFAULT_ID)).thenReturn(system);

        PortalPageStore store = createCachingStore();

        //Prime
        store.getAllOwnedPortalPages(user1);
        assertEqualsButNotSame(system, store.getSystemDefaultPortalPage());

        //This will update the db and return new one
        assertNull(store.update(system));
        assertEqualsButNotSame(system, store.getSystemDefaultPortalPage());

        //This will hit the cache
        assertEqualsButNotSame(page1, store.getPortalPage(PAGE1_ID));
        assertEqualsButNotSame(page2, store.getPortalPage(PAGE2_ID));
        assertEqualsButNotSame(system, store.getSystemDefaultPortalPage());
        assertEqualsButNotSame(system, store.getSystemDefaultPortalPage());
        assertEqualsButNotSame(expectedList, store.getAllOwnedPortalPages(user1));
    }

    @Test
    public void testUpdateNullReturn()
    {
        final List expectedList = EasyList.build(page1, page2);
        final List expectedListFred = EasyList.build(pageFred);

        //prime
        when(delegateStore.getAllOwnedPortalPages(user1.getKey())).thenReturn(expectedList);
        when(delegateStore.getAllOwnedPortalPages(fred.getKey())).thenReturn(expectedListFred);
        when(delegateStore.update(page1)).thenReturn(null);
        when(delegateStore.getPortalPage(PAGE1_ID)).thenReturn(page1);
        when(delegateStore.getAllOwnedPortalPages(user1)).thenReturn(expectedList);

        PortalPageStore store = createCachingStore();

        //Prime
        store.getAllOwnedPortalPages(user1);
        store.getAllOwnedPortalPages(fred);

        //This will update the db and return new one
        assertNull(store.update(page1));
        assertEqualsButNotSame(page1, store.getPortalPage(PAGE1_ID));
        assertEqualsButNotSame(expectedList, store.getAllOwnedPortalPages(user1));

        //This will hit the cache
        assertEqualsButNotSame(page1, store.getPortalPage(PAGE1_ID));
        assertEqualsButNotSame(page2, store.getPortalPage(PAGE2_ID));
        assertEqualsButNotSame(expectedList, store.getAllOwnedPortalPages(user1));
        assertEqualsButNotSame(pageFred, store.getPortalPage(PAGE_FRED_ID));
        assertEqualsButNotSame(expectedListFred, store.getAllOwnedPortalPages(fred));
    }


    @Test
    public void testAdjustFavouriteCount()
    {
        PortalPage page1a = PortalPage.id(PAGE1_ID).name("Page 1").description("Page 1 Description").owner(user1).build();
        PortalPage page1b = PortalPage.id(PAGE1_ID).name("Page 1").description("Page 1 Description").owner(user1).build();
        when(delegateStore.getPortalPage(PAGE1_ID))
                .thenReturn(page1a)
                .thenReturn(page1b);

        final List expectedList = EasyList.build(page1, page2);
        int adjustAmount = 54;

        PortalPage page1Adjusted = PortalPage.portalPage(page1).name("Adjusted filter").build();

        //prime
        when(delegateStore.getAllOwnedPortalPages(user1.getKey())).thenReturn(expectedList);
        when(delegateStore.adjustFavouriteCount(page1, adjustAmount)).thenReturn(page1Adjusted);

        PortalPageStore store = createCachingStore();

        store.getAllOwnedPortalPages(user1);

        //This will update the db and return new one
        assertEqualsButNotSame(page1Adjusted, store.adjustFavouriteCount(page1, adjustAmount));

        //This will hit the cache
        assertEqualsButNotSame(page1b, store.getPortalPage(PAGE1_ID));
        assertEqualsButNotSame(page2, store.getPortalPage(PAGE2_ID));
        assertEqualsButNotSame(EasyList.build(page1b, page2), store.getAllOwnedPortalPages(user1));
    }

    @Test
    public void testAdjustFavouriteCountSystemDefaultReturnNull()
    {
        final List expectedList = EasyList.build(page1, page2, system);
        final int adjustAmount = -1;

        //prime
        when(delegateStore.getAllOwnedPortalPages(user1.getKey())).thenReturn(expectedList);
        when(delegateStore.getSystemDefaultPortalPage()).thenReturn(system);
        when(delegateStore.adjustFavouriteCount(system, adjustAmount)).thenReturn(null);
        when(delegateStore.getPortalPage(SYSTEM_DEFAULT_ID)).thenReturn(system);

        PortalPageStore store = createCachingStore();

        //Prime
        store.getAllOwnedPortalPages(user1);
        assertEqualsButNotSame(system, store.getSystemDefaultPortalPage());

        //This will update the db and return new one
        assertNull(store.adjustFavouriteCount(system, adjustAmount));
        assertEqualsButNotSame(system, store.getSystemDefaultPortalPage());

        //This will hit the cache
        assertEqualsButNotSame(page1, store.getPortalPage(PAGE1_ID));
        assertEqualsButNotSame(page2, store.getPortalPage(PAGE2_ID));
        assertEqualsButNotSame(system, store.getSystemDefaultPortalPage());
        assertEqualsButNotSame(system, store.getSystemDefaultPortalPage());
        assertEqualsButNotSame(expectedList, store.getAllOwnedPortalPages(user1));
    }

    @Test
    public void testAdjustFavouriteCountReturnNull()
    {
        final List expectedList = EasyList.build(page1, page2);
        final List expectedListFred = EasyList.build(pageFred);
        final int adjustAmount = 3749;

        //prime
        when(delegateStore.getAllOwnedPortalPages(user1.getKey())).thenReturn(expectedList);
        when(delegateStore.getAllOwnedPortalPages(fred.getKey())).thenReturn(expectedListFred);
        when(delegateStore.adjustFavouriteCount(page1, adjustAmount)).thenReturn(null);
        when(delegateStore.getPortalPage(PAGE1_ID)).thenReturn(page1);
        when(delegateStore.getAllOwnedPortalPages(user1)).thenReturn(expectedList);

        PortalPageStore store = createCachingStore();

        //Prime
        store.getAllOwnedPortalPages(user1);
        store.getAllOwnedPortalPages(fred);

        //This will update the db and return new one
        assertNull(store.adjustFavouriteCount(page1, adjustAmount));
        assertEqualsButNotSame(page1, store.getPortalPage(PAGE1_ID));
        assertEqualsButNotSame(expectedList, store.getAllOwnedPortalPages(user1));

        //This will hit the cache
        assertEqualsButNotSame(page1, store.getPortalPage(PAGE1_ID));
        assertEqualsButNotSame(page2, store.getPortalPage(PAGE2_ID));
        assertEqualsButNotSame(expectedList, store.getAllOwnedPortalPages(user1));
        assertEqualsButNotSame(pageFred, store.getPortalPage(PAGE_FRED_ID));
        assertEqualsButNotSame(expectedListFred, store.getAllOwnedPortalPages(fred));
    }

    @Test
    public void testDelete()
    {
        final List expectedList = EasyList.build(page1, page2);
        final List expectedList2 = EasyList.build(page2);
        final List expectedListFred = EasyList.build(pageFred);

        //prime
        when(delegateStore.getAllOwnedPortalPages(user1.getKey()))
                .thenReturn(expectedList)
                .thenReturn(expectedList2);

        when(delegateStore.getAllOwnedPortalPages(fred.getKey())).thenReturn(expectedListFred);

        when(delegateStore.getPortalPage(PAGE1_ID)).thenReturn(page1);

        PortalPageStore store = createCachingStore();

        // Prime cache
        store.getAllOwnedPortalPages(user1);
        store.getAllOwnedPortalPages(fred);

        store.delete(PAGE1_ID);

        //This will have been remove from cache
        assertEqualsButNotSame(page1, store.getPortalPage(PAGE1_ID));
        //Ths will still be in the cache (but page1 removed from list)

        assertEqualsButNotSame(EasyList.build(page2), store.getAllOwnedPortalPages(user1));

        //These should still be in the cache
        assertEqualsButNotSame(expectedListFred, store.getAllOwnedPortalPages(fred));
        assertEqualsButNotSame(expectedListFred, store.getAllOwnedPortalPages(fred));

        assertEqualsButNotSame(page1, store.getPortalPage(PAGE1_ID));
        assertEqualsButNotSame(page2, store.getPortalPage(PAGE2_ID));
    }

    private void assertEqualsButNotSame(PortalPage expected, PortalPage actual)
    {
        //make sure we copy the correct class. We have to check both the direct and super classes because page1 and page2
        //are actually extensions from
        assertTrue(expected.getClass().getSuperclass().equals(actual.getClass()) || expected.getClass().equals(actual.getClass()));

        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getDescription(), actual.getDescription());
        assertEquals(expected.getOwner(), actual.getOwner());
    }

    private void assertEqualsButNotSame(Collection /*<PortalPage>*/ expectedCollection, Collection /*<PortalPage>*/ actualCollection)
    {
        assertEquals("Page lists have diferent size.", expectedCollection.size(), actualCollection.size());
        List expectedList = new ArrayList(expectedCollection);
        List actualList = new ArrayList(actualCollection);
        for (int i = 0; i < expectedList.size(); i++)
        {
            assertEqualsButNotSame((PortalPage) expectedList.get(i), (PortalPage) actualList.get(i));
        }
    }

    private PortalPageStore createCachingStore()
    {
        return new CachingPortalPageStore(delegateStore, new MemoryCacheManager());
    }

}
