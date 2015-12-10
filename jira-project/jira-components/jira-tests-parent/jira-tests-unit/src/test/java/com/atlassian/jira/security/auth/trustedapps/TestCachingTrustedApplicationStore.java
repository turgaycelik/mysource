package com.atlassian.jira.security.auth.trustedapps;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.atlassian.cache.CacheManager;
import com.atlassian.cache.memory.MemoryCacheManager;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class TestCachingTrustedApplicationStore
{
    private final CacheManager cacheManager = new MemoryCacheManager();

    class CountingTrustedApplicationStore extends MockTrustedApplicationStore
    {
        final AtomicInteger countGetAll = new AtomicInteger(0);
        final AtomicInteger countStore = new AtomicInteger(0);
        final AtomicInteger countRemove = new AtomicInteger(0);

        public CountingTrustedApplicationStore(final TrustedApplicationData... datas)
        {
            super(Arrays.asList(datas));
        }

        @Override
        public Set<TrustedApplicationData> getAll()
        {
            countGetAll.incrementAndGet();
            return super.getAll();
        }

        @Override
        public TrustedApplicationData store(final TrustedApplicationData data)
        {
            countStore.incrementAndGet();
            return super.store(data);
        }

        @Override
        public boolean delete(final long id)
        {
            countRemove.incrementAndGet();
            return super.delete(id);
        }
    }

    @Test
    public void testGetAllOnlyCalledOnce() throws Exception
    {
        final CountingTrustedApplicationStore countingStore = new CountingTrustedApplicationStore();

        final CachingTrustedApplicationStore cachedStore = new CachingTrustedApplicationStore(countingStore, cacheManager);
        assertNotNull(cachedStore.getAll());

        for (int i = 0; i < 100; i++)
        {
            assertEquals(0, cachedStore.getAll().size());
        }

        assertEquals(1, countingStore.countGetAll.get());
    }

    @Test
    public void testGetAllOnlyCalledTwiceWhenDataAdded() throws Exception
    {
        final CountingTrustedApplicationStore countingStore = new CountingTrustedApplicationStore();

        final CachingTrustedApplicationStore cachedStore = new CachingTrustedApplicationStore(countingStore, cacheManager);
        assertNotNull(cachedStore.getAll());
        assertEquals(0, cachedStore.getAll().size());
        assertEquals(1, countingStore.countGetAll.get());

        final TrustedApplicationData data = cachedStore.store(new MockTrustedApplicationData(0, "CONF", "confluence", 1000));
        assertNotNull(data);
        assertEquals(1, data.getId());

        assertNotNull(cachedStore.getAll());
        assertEquals(1, cachedStore.getAll().size());
        assertEquals(2, countingStore.countGetAll.get());
    }

    @Test
    public void testGetAllOnlyCalledTwiceWhenDataUpdated() throws Exception
    {
        final CountingTrustedApplicationStore countingStore = new CountingTrustedApplicationStore(
                new MockTrustedApplicationData(1, "CONF", "confluencing", 1000));

        final CachingTrustedApplicationStore cachedStore = new CachingTrustedApplicationStore(countingStore, cacheManager);
        assertNotNull(cachedStore.getAll());
        assertEquals(1, cachedStore.getAll().size());
        assertEquals(1, countingStore.countGetAll.get());

        cachedStore.store(new MockTrustedApplicationData(1, "CONF", "confluence", 7654));

        assertNotNull(cachedStore.getAll());
        assertEquals(1, cachedStore.getAll().size());
        assertEquals(2, countingStore.countGetAll.get());

        final TrustedApplicationData data = cachedStore.getById(1);
        assertEquals(1, data.getId());
        assertEquals("confluence", data.getName());
        assertEquals(7654, data.getTimeout());
    }

    @Test
    public void testGetAllOnlyCalledTwiceWhenDataRemoved() throws Exception
    {
        final CountingTrustedApplicationStore countingStore = new CountingTrustedApplicationStore(
                new MockTrustedApplicationData(1, "CONF", "confluencing", 1000));

        final CachingTrustedApplicationStore cachedStore = new CachingTrustedApplicationStore(countingStore, cacheManager);
        assertNotNull(cachedStore.getAll());
        assertEquals(1, cachedStore.getAll().size());
        assertEquals(1, countingStore.countGetAll.get());
        assertEquals(0, countingStore.countStore.get());

        cachedStore.delete(1);

        assertNotNull(cachedStore.getAll());
        assertEquals(0, cachedStore.getAll().size());
        assertEquals(2, countingStore.countGetAll.get());
        assertEquals(0, countingStore.countStore.get());
        assertEquals(1, countingStore.countRemove.get());

        assertNull(cachedStore.getByApplicationId("CONF"));
    }

    @Test
    public void testAlternateGetsReturnSameObject() throws Exception
    {
        final CountingTrustedApplicationStore countingStore = new CountingTrustedApplicationStore(
                new MockTrustedApplicationData(1, "CONF", "confluencing", 1000));

        final CachingTrustedApplicationStore cachedStore = new CachingTrustedApplicationStore(countingStore, cacheManager);

        final TrustedApplicationData info1 = cachedStore.getById(1);
        final TrustedApplicationData info2 = cachedStore.getByApplicationId("CONF");
        assertSame(info1, info2);
    }
}
