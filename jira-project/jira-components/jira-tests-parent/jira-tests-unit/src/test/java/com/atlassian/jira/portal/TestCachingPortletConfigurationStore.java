package com.atlassian.jira.portal;

import java.util.Collections;
import java.util.List;

import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.gadgets.dashboard.Color;
import com.atlassian.jira.util.collect.MapBuilder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.opensymphony.module.propertyset.PropertySet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test for the {@link PortletConfigurationStore}.
 *
 * @since 4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestCachingPortletConfigurationStore
{
    private static final Long CONFIG1_ID = 10L;
    private static final Long PAGE1_ID = 99L;
    private static final Long CONFIG2_ID = 11L;
    private static final Long CONFIG3_ID = 3L;
    private static final Long PAGE2_ID = 991L;
    private static final Long CONFIG1_ID_BAD = -23L;

    @Mock
    private PortletConfigurationStore delegateStore;
    private PortletConfiguration config1;
    private PortletConfiguration config2;

    @Before
    public void setUp() throws Exception
    {
        config1 = new PortletConfigurationImpl(CONFIG1_ID, PAGE1_ID, 1, 2, null, null, Collections.<String, String>emptyMap());
        config2 = new PortletConfigurationImpl(CONFIG2_ID, PAGE2_ID, 1, 2, null, null, Collections.<String, String>emptyMap());
    }

    /**
     * Make sure the cache returns the correct portlet configuration.
     */
    @Test
    public void testGetByPortletId()
    {
        when(delegateStore.getByPortletId(CONFIG1_ID)).thenReturn(config1);
        when(delegateStore.getByPortletId(CONFIG2_ID)).thenReturn(config2);

        PortletConfigurationStore store = createCachingStore();

        //this should call through.
        assertEqualsButNotSame(config1, store.getByPortletId(CONFIG1_ID));

        //this should call through.
        assertEqualsButNotSame(config2, store.getByPortletId(CONFIG2_ID));

        //these should be cached.
        assertEqualsButNotSame(config1, store.getByPortletId(CONFIG1_ID));
        assertEqualsButNotSame(config1, store.getByPortletId(CONFIG1_ID));
        assertEqualsButNotSame(config2, store.getByPortletId(CONFIG2_ID));
        assertEqualsButNotSame(config1, store.getByPortletId(CONFIG1_ID));
        assertEqualsButNotSame(config2, store.getByPortletId(CONFIG2_ID));
        assertEqualsButNotSame(config1, store.getByPortletId(CONFIG1_ID));
    }

    /**
     * Make sure it can handle invalid portletIds
     */
    @Test
    public void testGetByPortletIdBadId()
    {
        when(delegateStore.getByPortletId(CONFIG1_ID_BAD)).thenReturn(null);

        PortletConfigurationStore store = createCachingStore();

        //this should call through.
        assertNull(store.getByPortletId(CONFIG1_ID_BAD));
        assertNull(store.getByPortletId(CONFIG1_ID_BAD));
    }

    /**
     * Does the delete work. A delete should clear the cache of any related entities, including the page cache.
     */
    @Test
    public void testDelete()
    {
        when(delegateStore.getByPortletId(CONFIG1_ID)).thenReturn(config1);
        when(delegateStore.getByPortletId(CONFIG2_ID)).thenReturn(config2);

        PortletConfigurationImpl config3 = new PortletConfigurationImpl(CONFIG3_ID, PAGE1_ID, 1, 2, null, null, Collections.<String, String>emptyMap());
        final List<PortletConfiguration> expectedList = ImmutableList.of(config2);

        when(delegateStore.getByPortalPage(PAGE1_ID))
                .thenReturn(ImmutableList.<PortletConfiguration>of(config3, config2))
                .thenReturn(ImmutableList.<PortletConfiguration>of(config2));
        when(delegateStore.getByPortletId(CONFIG2_ID)).thenReturn(config2);
        when(delegateStore.getByPortletId(CONFIG3_ID))
                .thenReturn(config3)
                .thenReturn(null);

        delegateStore.delete(config1);

        when(delegateStore.getByPortalPage(PAGE1_ID)).thenReturn(expectedList);

        PortletConfigurationStore store = createCachingStore();

        //prime the cache.
        store.getByPortalPage(PAGE1_ID);
        store.getByPortletId(CONFIG3_ID);

        store.delete(config3);

        //this should call through.
        assertNull(store.getByPortletId(CONFIG3_ID));

        //these should be cached.
        assertEqualsButNotSame(config1, store.getByPortletId(CONFIG1_ID));
        assertEqualsButNotSame(config1, store.getByPortletId(CONFIG1_ID));
        assertEqualsButNotSame(config2, store.getByPortletId(CONFIG2_ID));
        assertEqualsButNotSame(config1, store.getByPortletId(CONFIG1_ID));
        assertEqualsButNotSame(config2, store.getByPortletId(CONFIG2_ID));
        assertEqualsButNotSame(config1, store.getByPortletId(CONFIG1_ID));

        //this should call through.
        assertEqualsButNotSame(expectedList, store.getByPortalPage(PAGE1_ID));

        //this should be cached.
        assertEqualsButNotSame(expectedList, store.getByPortalPage(PAGE1_ID));

        verify(delegateStore).delete(config1);
    }
    /**

     * Does the store work. A store should clear the cache of any related entities, including the page cache.
     */
    @Test
    public void testStore()
    {
        when(delegateStore.getByPortletId(CONFIG1_ID)).thenReturn(config1);
        when(delegateStore.getByPortletId(CONFIG2_ID))
                .thenReturn(null)
                .thenReturn(config2);

        when(delegateStore.getByPortalPage(PAGE1_ID))
                .thenReturn(ImmutableList.<PortletConfiguration>of(config1))
                .thenReturn(ImmutableList.<PortletConfiguration>of(config1));

        when(delegateStore.getByPortalPage(PAGE2_ID))
                .thenReturn(ImmutableList.<PortletConfiguration>of())
                .thenReturn(ImmutableList.<PortletConfiguration>of(config2));

        final List expectedList = EasyList.build(config1);
        final List expectedList2 = EasyList.build(config2);

        PortletConfigurationStore store = createCachingStore();

        assertEqualsButNotSame(expectedList, store.getByPortalPage(PAGE1_ID));
        assertEquals(0, store.getByPortalPage(PAGE2_ID).size());
        assertNull(store.getByPortletId(CONFIG2_ID));

        store.store(config2);

        //these should be cached.
        assertEqualsButNotSame(config2, store.getByPortletId(CONFIG2_ID));
        assertEqualsButNotSame(config2, store.getByPortletId(CONFIG2_ID));
        assertEqualsButNotSame(config1, store.getByPortletId(CONFIG1_ID));
        assertEqualsButNotSame(config1, store.getByPortletId(CONFIG1_ID));

        //this should still be cached because PAGE1 did not contain the portlet configuration
        //that was deleted.
        assertEqualsButNotSame(expectedList, store.getByPortalPage(PAGE1_ID));
        assertEqualsButNotSame(expectedList2, store.getByPortalPage(PAGE2_ID));

        verify(delegateStore).store(config2);
    }

    /**
     * Tests moving a portlet from one page to another.  Both page ids should be removed from cache.
     */
    @Test
    public void testMove()
    {
        final List<PortletConfiguration> expectedList = ImmutableList.of(config1, config2);
        final List<PortletConfiguration> expectedList2 = ImmutableList.of(config2);

        when(delegateStore.getByPortletId(CONFIG1_ID)).thenReturn(config1);
        when(delegateStore.getByPortletId(CONFIG2_ID)).thenReturn(config2);

        when(delegateStore.getByPortalPage(PAGE1_ID))
                .thenReturn(expectedList);
        when(delegateStore.getByPortalPage(PAGE2_ID))
                .thenReturn(ImmutableList.<PortletConfiguration>of())
                .thenReturn(expectedList2);


        PortletConfigurationStore store = createCachingStore();

        assertEqualsButNotSame(expectedList, store.getByPortalPage(PAGE1_ID));
        assertEqualsButNotSame(Collections.EMPTY_LIST, store.getByPortalPage(PAGE2_ID));

        store.store(config2);

        //these should be cached.
        assertEqualsButNotSame(expectedList, store.getByPortalPage(PAGE1_ID));
        assertEqualsButNotSame(expectedList2, store.getByPortalPage(PAGE2_ID));

        verify(delegateStore).store(config2);
    }

    @Test
    public void testGetByPortalPage()
    {
        final List<PortletConfiguration> expectedList = ImmutableList.of(config1, config2);

        when(delegateStore.getByPortletId(CONFIG1_ID)).thenReturn(config1);
        when(delegateStore.getByPortletId(CONFIG2_ID)).thenReturn(config2);

        when(delegateStore.getByPortalPage(PAGE1_ID)).thenReturn(expectedList);

        PortletConfigurationStore store = createCachingStore();

        //prime the cache.
        store.getByPortletId(CONFIG1_ID);

        //this should call through to the store.
        assertEqualsButNotSame(expectedList, store.getByPortalPage(PAGE1_ID));

        //this should be cached.
        assertEqualsButNotSame(config2, store.getByPortletId(CONFIG2_ID));

        assertEqualsButNotSame(expectedList, store.getByPortalPage(PAGE1_ID));
        assertEqualsButNotSame(expectedList, store.getByPortalPage(PAGE1_ID));
        assertEqualsButNotSame(expectedList, store.getByPortalPage(PAGE1_ID));
        assertEqualsButNotSame(expectedList, store.getByPortalPage(PAGE1_ID));

    }

    @Test
    public void testGetByPortalPageNullFromDB()
    {
        List<PortletConfiguration> expectedList = ImmutableList.of();

        when(delegateStore.getByPortletId(CONFIG1_ID)).thenReturn(null);
        when(delegateStore.getByPortletId(CONFIG2_ID)).thenReturn(config2);

        when(delegateStore.getByPortalPage(PAGE1_ID)).thenReturn(expectedList);

        PortletConfigurationStore store = createCachingStore();

        //prime the cache.
        store.getByPortletId(CONFIG1_ID);

        //this should call through to the store.
        assertEqualsButNotSame(expectedList, store.getByPortalPage(PAGE1_ID));

        //this should be cached.
        assertEqualsButNotSame(expectedList, store.getByPortalPage(PAGE1_ID));
        assertEqualsButNotSame(expectedList, store.getByPortalPage(PAGE1_ID));
        assertEqualsButNotSame(expectedList, store.getByPortalPage(PAGE1_ID));
        assertEqualsButNotSame(expectedList, store.getByPortalPage(PAGE1_ID));
    }

    @Test
    public void testGetByPortalPageStoreReturn()
    {
        final List<PortletConfiguration> expectedList = ImmutableList.of(config1, config2);

        when(delegateStore.getByPortletId(CONFIG1_ID)).thenReturn(config1);
        when(delegateStore.getByPortletId(CONFIG2_ID)).thenReturn(config2);

        when(delegateStore.getByPortalPage(PAGE1_ID)).thenReturn(expectedList);

        PortletConfigurationStore store = createCachingStore();

        //prime the cache.
        store.getByPortletId(CONFIG1_ID);

        //this should call through to the store.
        assertEqualsButNotSame(expectedList, store.getByPortalPage(PAGE1_ID));

        //this should be cached.
        assertEqualsButNotSame(expectedList, store.getByPortalPage(PAGE1_ID));
        assertEqualsButNotSame(expectedList, store.getByPortalPage(PAGE1_ID));
        assertEqualsButNotSame(expectedList, store.getByPortalPage(PAGE1_ID));
        assertEqualsButNotSame(expectedList, store.getByPortalPage(PAGE1_ID));
    }

    @Test
    public void testUpdateGadgetColor()
    {
        PortletConfigurationImpl config1b = new PortletConfigurationImpl(CONFIG1_ID, PAGE1_ID, 1, 2, null, Color.color3, Collections.<String, String>emptyMap());

        when(delegateStore.getByPortletId(CONFIG1_ID))
                .thenReturn(config1)
                .thenReturn(config1b);

        PortletConfigurationStore store = createCachingStore();
        store.getByPortletId(CONFIG1_ID);

        //then change color.  The cached entry should have been removed
        store.updateGadgetColor(CONFIG1_ID, Color.color3);

        //this will be the second call to the delegate store.
        PortletConfiguration configx = store.getByPortletId(CONFIG1_ID);
        assertEquals(Color.color3, configx.getColor());

        verify(delegateStore).updateGadgetColor(CONFIG1_ID, Color.color3);
    }

    @Test
    public void testUpdateGadgetUserPrefs()
    {
        PortletConfigurationImpl config1b = new PortletConfigurationImpl(CONFIG1_ID, PAGE1_ID, 1, 2, null, Color.color3, ImmutableMap.of("XX", "YY"));

        when(delegateStore.getByPortletId(CONFIG1_ID))
                .thenReturn(config1)
                .thenReturn(config1b);

        PortletConfigurationStore store = createCachingStore();
        store.getByPortletId(CONFIG1_ID);

        //then change user prefs.  The cached entry should have been removed
        store.updateUserPrefs(CONFIG1_ID, MapBuilder.<String, String>newBuilder().add("pref1", "value1").toMap());

        //this will be the second call to the delegate store.
        PortletConfiguration configx = store.getByPortletId(CONFIG1_ID);
        assertEquals(ImmutableMap.of("XX", "YY"), configx.getUserPrefs());

        verify(delegateStore).updateUserPrefs(CONFIG1_ID, MapBuilder.<String, String>newBuilder().add("pref1", "value1").toMap());
    }

    @Test
    public void testUpdateGadgetPositionCached()
    {
        PortletConfigurationImpl config1b = new PortletConfigurationImpl(CONFIG1_ID, PAGE2_ID, 0, 3, null, null, Collections.<String, String>emptyMap());

        when(delegateStore.getByPortletId(CONFIG1_ID))
                .thenReturn(config1)
                .thenReturn(config1b);
        when(delegateStore.getByPortalPage(PAGE1_ID))
                .thenReturn(ImmutableList.of(config1))
                .thenReturn(ImmutableList.<PortletConfiguration>of());
        when(delegateStore.getByPortalPage(PAGE2_ID))
                .thenReturn(ImmutableList.of(config2))
                .thenReturn(ImmutableList.of(config1, config2));

        PortletConfigurationStore store = createCachingStore();

        //first warm up the cache
        store.getByPortletId(CONFIG1_ID);
        store.getByPortalPage(PAGE1_ID);
        store.getByPortalPage(PAGE2_ID);
        //second time round all should come from the cache
        store.getByPortletId(CONFIG1_ID);
        store.getByPortalPage(PAGE1_ID);
        store.getByPortalPage(PAGE2_ID);

        //now move the gadget to another dashboard, all caches should have been cleared
        store.updateGadgetPosition(CONFIG1_ID, 0, 2, PAGE2_ID);

        store.getByPortletId(CONFIG1_ID);
        store.getByPortalPage(PAGE1_ID);
        store.getByPortalPage(PAGE2_ID);

        verify(delegateStore).updateGadgetPosition(CONFIG1_ID, 0, 2, PAGE2_ID);

    }

    private void assertEqualsButNotSame(List<PortletConfiguration> expectedList, List<PortletConfiguration> actualList)
    {
        assertEquals("Configuration lists have diferent size.", expectedList.size(), actualList.size());
        for (int i = 0; i < expectedList.size(); i++)
        {
            assertEqualsButNotSame((PortletConfiguration) expectedList.get(i), (PortletConfiguration) actualList.get(i));
        }
    }

    private void assertEqualsButNotSame(PortletConfiguration configuration1, PortletConfiguration configuration2)
    {
        assertNotSame(configuration1, configuration2);

        assertEquals(configuration1.getColumn(), configuration2.getColumn());
        assertEquals(configuration1.getId(), configuration2.getId());
        assertEquals(configuration1.getDashboardPageId(), configuration2.getDashboardPageId());
        assertEquals(configuration1.getRow(), configuration2.getRow());
    }

    private PortletConfigurationStore createCachingStore()
    {

        return new CachingPortletConfigurationStore(delegateStore, new MemoryCacheManager())
        {
            ///CLOVER:OFF
            PropertySet clonePropertySet(PropertySet srcPropertySet)
            {
                return srcPropertySet;
            }
        };
    }

}
