package com.atlassian.jira.portal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.gadgets.dashboard.Layout;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.sharing.SharedEntityAccessor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.util.collect.EnclosedIterable;
import com.atlassian.jira.util.collect.MapBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * A test case for the PortalPage store class
 *
 * @since v3.13
 */
public class TestOfBizPortalPageStore
{
    private static final String FRED = "tobpps_fred";
    private static final String MARY = "tobpps_mary";
    
    private ApplicationUser fred;
    private ApplicationUser mary;

    private static final class Table
    {
        static final String PORTAL_PAGE = PortalPage.ENTITY_TYPE.getName();
    }

    private OfBizDelegator delegator;
    private OfBizPortalPageStore store;

    @Before
    public void setUp() throws Exception
    {
        delegator = new MockOfBizDelegator();
        UserManager userManager = Mockito.mock(UserManager.class);

        MockComponentWorker mockComponentWorker = new MockComponentWorker();
        mockComponentWorker.init().addMock(OfBizDelegator.class, delegator).addMock(UserManager.class, userManager);
        
        fred = mockUser(FRED);
        Mockito.when(userManager.getUserByKeyEvenWhenUnknown(FRED)).thenReturn(fred);
        mary = mockUser(MARY);
        Mockito.when(userManager.getUserByKeyEvenWhenUnknown(MARY)).thenReturn(mary);
        
        store = new OfBizPortalPageStore(delegator, userManager);
    }
    
    private ApplicationUser mockUser(String key) {
        ApplicationUser result = Mockito.mock(ApplicationUser.class);
        Mockito.when(result.getKey()).thenReturn(key);
        return result;
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

        public List<Long> getIds()
        {
            return ids;
        }

        public boolean preserveOrder()
        {
            return preserverOrder;
        }

    }

    @Test
    public void test_get_Empty()
    {
        final EnclosedIterable<PortalPage> iterable = store.get(new RetrievalDescriptor(Collections.<Long> emptyList()));
        assertNotNull(iterable);
        assertTrue(iterable.isEmpty());
    }

    /**
     * Calling get with preserver order set to false means the returned objects can be any order but must all be there
     */
    @Test
    public void test_get_NoOrder()
    {
        addPPGV(10000, FRED, "page1", "page1 description");
        addPPGV(10001, FRED, "page2", "page2 description");
        addPPGV(10002, MARY, "page1", "page1 description");
        addPPGV(10003, MARY, "page2", "page2 description");

        final List<Long> expectedList = Lists.newArrayList(new Long(10003), new Long(10000), new Long(10001));
        final EnclosedIterable<PortalPage> iterable = store.get(new RetrievalDescriptor(expectedList, false));
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
    public void test_get_WithOrder()
    {
        addPPGV(10000, FRED, "page1", "page1 description");
        addPPGV(10001, FRED, "page2", "page2 description");
        addPPGV(10002, MARY, "page1", "page1 description");
        addPPGV(10003, MARY, "page2", "page2 description");

        final List<Long> expectedList = Lists.newArrayList(new Long(10003), new Long(10000), new Long(10001));

        final EnclosedIterable<PortalPage> iterable = store.get(new RetrievalDescriptor(expectedList, true));
        assertNotNull(iterable);
        assertEquals(3, iterable.size());
        //
        // order CAN be guaranteed here
        assertEquals(expectedList, toIdList(iterable));
    }

    @Test
    public void test_getAll()
    {
        EnclosedIterable<PortalPage> iterable = store.getAll();
        assertNotNull(iterable);
        assertTrue(iterable.isEmpty());

        addPPGV(10000, FRED, "page1", "page1 description");
        addPPGV(10001, FRED, "page2", "page2 description");
        addPPGV(10002, MARY, "page1", "page1 description");
        addPPGV(10003, MARY, "page2", "page2 description");

        iterable = store.getAll();
        assertNotNull(iterable);
        assertEquals(4, iterable.size());
        assertIterableHasAllIds(iterable, new long[] { 10000, 10001, 10002, 10003 });
    }

    @Test
    public void test_getAllOwnedPortalPages() throws Exception
    {
        Collection<PortalPage> portalPages = store.getAllOwnedPortalPages(fred);
        assertNotNull(portalPages);
        assertTrue(portalPages.isEmpty());

        addPPGV(10000, FRED, "page1", "page1 description");
        addPPGV(10001, FRED, "page2", "page2 description");
        addPPGV(10002, MARY, "page1", "page1 description");
        addPPGV(10003, MARY, "page2", "page2 description");

        portalPages = store.getAllOwnedPortalPages(fred);
        assertNotNull(portalPages);
        assertEquals(2, portalPages.size());
        assertPortalPageCollectionHasAllIds(portalPages, new long[] { 10000, 10001 });

    }

    
    @Test
    public void test_getPortalPageByOwnerAndName() throws Exception
    {
        PortalPage portalPage = store.getPortalPageByOwnerAndName(fred, "page1");
        assertNull(portalPage);

        addPPGV(10000, FRED, "page1", "page1 description");
        addPPGV(10001, FRED, "page2", "page2 description");
        addPPGV(10002, MARY, "page1", "page1 description");
        addPPGV(10003, MARY, "page2", "page2 description");

        portalPage = store.getPortalPageByOwnerAndName(fred, "pageX");
        assertNull(portalPage);

        portalPage = store.getPortalPageByOwnerAndName(fred, "page1");
        assertNotNull(portalPage);
        assertEquals(10000, portalPage.getId().longValue());

        portalPage = store.getPortalPageByOwnerAndName(mary, "pageX");
        assertNull(portalPage);

        portalPage = store.getPortalPageByOwnerAndName(mary, "page1");
        assertNotNull(portalPage);
        assertEquals(10002, portalPage.getId().longValue());
    }

    @Test
    public void test_getPortalPage() throws Exception
    {
        PortalPage portalPage = store.getPortalPage(new Long(10000));
        assertNull(portalPage);

        addPPGV(10000, FRED, "page1", "page1 description");
        addPPGV(10001, FRED, "page2", "page2 description");
        addPPGV(10002, MARY, "page1", "page1 description");
        addPPGV(10003, MARY, "page2", "page2 description");

        portalPage = store.getPortalPage(new Long(10004));
        assertNull(portalPage);

        portalPage = store.getPortalPage(new Long(10000));
        assertNotNull(portalPage);
        assertEquals(10000, portalPage.getId().longValue());
    }

    @Test
    public void test_create() throws Exception
    {
        addPPGV(10000, FRED, "page1", "page1 description");

        final PortalPage portalPage = PortalPage.name("page1").description("description 1").owner(fred).build();
        final PortalPage newPortalPage = store.create(portalPage);
        assertNotNull(newPortalPage);
        assertEquals("page1", newPortalPage.getName());
        assertEquals("description 1", newPortalPage.getDescription());
        assertEquals(fred, newPortalPage.getOwner());
    }

    @Test
    public void test_update() throws Exception
    {
        GenericValue gv = addPPGV(10000, FRED, "page1", "page1 description");
        PortalPage portalPage = createPortalPageFromGV(fred, gv);

        portalPage = PortalPage.portalPage(portalPage).name("newName").build();
        portalPage = store.update(portalPage);
        assertNotNull(portalPage);
        assertEquals("newName", portalPage.getName());
        assertEquals(fred, portalPage.getOwner());

        portalPage = PortalPage.portalPage(portalPage).description("newDesc").build();
        portalPage = store.update(portalPage);
        assertNotNull(portalPage);
        assertEquals("newName", portalPage.getName());
        assertEquals("newDesc", portalPage.getDescription());
        assertEquals(fred, portalPage.getOwner());

        // test that fav count is never updated via the store
        gv = addPPGV(10001, FRED, "page2", "page2 description");
        portalPage = createPortalPageFromGV(fred, gv);

        gv.set("favCount", 27L);
        gv.store();

        portalPage = PortalPage.portalPage(portalPage).name("newName").build();
        portalPage = PortalPage.portalPage(portalPage).description("newDesc").build();

        portalPage = store.update(portalPage);
        assertNotNull(portalPage);
        assertEquals("newName", portalPage.getName());
        assertEquals("newDesc", portalPage.getDescription());
        assertEquals(fred, portalPage.getOwner());

        // now after all that malarkey, make sure the database has the same favcount after update
        gv = delegator.findByPrimaryKey(Table.PORTAL_PAGE, ImmutableMap.<String, Long> of("id", new Long(10001)));
        assertNotNull(gv);
        assertEquals(new Long(27), gv.getLong("favCount"));
    }

    @Test
    public void test_updateSystemDefault() throws Exception
    {
        final GenericValue gv = addPPGV(10000, null, "page1", "page1 description");
        PortalPage defaultPage = PortalPage.id(gv.getLong("id")).name(gv.getString("pagename")).
                description(gv.getString("description")).layout(Layout.AA).version(0L).systemDashboard().build();

        defaultPage = PortalPage.portalPage(defaultPage).name("newName").build();
        final PortalPage newPage = store.update(defaultPage);
        assertNotNull(newPage);
        assertEquals("newName", newPage.getName());
        assertNull(newPage.getOwner());
        assertTrue(newPage.isSystemDefaultPortalPage());

    }

    @Test
    public void test_updateWithoutUser() throws Exception
    {
        final GenericValue gv = addPPGV(10000, null, "page1", "page1 description");
        PortalPage defaultPage = createPortalPageFromGV(null, gv);
        defaultPage = PortalPage.portalPage(defaultPage).name("newName").build();
        try
        {
            store.update(defaultPage);
            fail("Should have barfed on null user for a normal portal page");
        }
        catch (final IllegalArgumentException e)
        {}
    }

    @Test
    public void test_delete() throws Exception
    {
        final int targetId = 10000;
        final GenericValue gv = addPPGV(targetId, FRED, "page1", "page1 description");
        final PortalPage portalPage = createPortalPageFromGV(fred, gv);

        addPPGV(10001, FRED, "page2", "page2 description");
        addPPGV(10002, MARY, "page1", "page1 description");
        addPPGV(10003, MARY, "page2", "page2 description");

        store.delete(portalPage.getId());

        Collection<GenericValue> gvs = delegator.findAll(Table.PORTAL_PAGE);
        assertEquals(3, gvs.size());

        CollectionUtil.foreach(gvs, new Consumer<GenericValue>()
        {
            public void consume(final GenericValue gv)
            {
                if (gv.getLong("id").longValue() == targetId)
                {
                    fail("This should have been deleted : " + targetId);
                }
            }
        });

        store.delete(new Long(666));
        gvs = delegator.findAll(Table.PORTAL_PAGE);
        assertEquals(3, gvs.size());
    }

    private PortalPage createPortalPageFromGV(final ApplicationUser user, final GenericValue gv)
    {
        return PortalPage.id(gv.getLong("id")).name(gv.getString("pagename")).description(gv.getString("description")).
                owner(user).layout(Layout.AA).version(0L).build();
    }

    public void test_getSystemDefaultPortalPage() throws Exception
    {
        // by default there is no Dashboard in the system.
        PortalPage systemDefault = store.getSystemDefaultPortalPage();
        assertNull(systemDefault);
        //
        // but an upgrade tasks adds one
        delegator.createValue("PortalPage", MapBuilder.<String, Object>build("pagename", "dashboard", "sequence", new Long(0)));

        systemDefault = store.getSystemDefaultPortalPage();
        assertNotNull(systemDefault);
        assertNull(systemDefault.getOwner());
        assertEquals("dashboard", systemDefault.getName());
    }

    private void assertIterableHasAllIds(final EnclosedIterable<PortalPage> iterable, final long[] expectedIds)
    {
        assertEquals(expectedIds.length, iterable.size());
        final List<PortalPage> portalPages = new ArrayList<PortalPage>(expectedIds.length);
        iterable.foreach(new Consumer<PortalPage>()
        {
            public void consume(final PortalPage element)
            {
                portalPages.add(element);
            }
        });
        assertPortalPageCollectionHasAllIds(portalPages, expectedIds);
    }

    private void assertPortalPageCollectionHasAllIds(final Collection<PortalPage> portalPages, final long[] expectedIds)
    {
        final Set<Long> foundIds = new HashSet<Long>();
        for (final PortalPage portalPage : portalPages)
        {
            for (final long id : expectedIds)
            {
                if (portalPage.getId().longValue() == id)
                {
                    if (foundIds.contains(id))
                    {
                        fail("The portalPage id : " + id + " was already found in the colelction.  Ids must be unique");
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
            fail("Failed to find specified All PortalPage ids");
        }
    }

    /**
     * Make sure updating the favourite count works.
     */
    public void testAdjustFavouriteCount()
            throws OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        final int targetId = 10002;
        final GenericValue gv = addPPGV(targetId, FRED, "page1", "page1 description");

        PortalPage portalPage = createPortalPageFromGV(fred, gv);

        // the initial value should be zero.
        assertEquals(0, portalPage.getFavouriteCount().longValue());

        // increment the value +10.
        portalPage = store.adjustFavouriteCount(portalPage, 10);
        assertEquals(10, portalPage.getFavouriteCount().longValue());

        // decrement the value to 4.
        portalPage = store.adjustFavouriteCount(portalPage, -6);
        assertEquals(4, portalPage.getFavouriteCount().longValue());

        // decrement the value to 0.
        portalPage = store.adjustFavouriteCount(portalPage, -100);
        assertEquals(0, portalPage.getFavouriteCount().longValue());
    }

    private GenericValue addPPGV(final long id, final String username, final String pageName, final String description)
    {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", id);
        map.put("pagename", pageName);
        map.put("description", description);
        map.put("username", username);
        return UtilsForTests.getTestEntity(Table.PORTAL_PAGE, map);
    }

    private List<Long> toIdList(final EnclosedIterable<PortalPage> iterable)
    {
        assertNotNull(iterable);
        return CollectionUtil.transform(new EnclosedIterable.ListResolver<PortalPage>().get(iterable), new Function<PortalPage, Long>()
        {
            public Long get(final PortalPage page)
            {
                return page.getId();
            }
        });
    }
}
