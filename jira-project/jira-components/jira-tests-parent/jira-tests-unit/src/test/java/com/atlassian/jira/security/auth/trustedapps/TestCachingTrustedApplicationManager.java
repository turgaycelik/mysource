package com.atlassian.jira.security.auth.trustedapps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.MockUser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class TestCachingTrustedApplicationManager
{
    public static void main(final String[] args)
    {
        final Collection<TrustedApplicationData> datas = new ArrayList<TrustedApplicationData>();
        datas.add(new MockTrustedApplicationData(1, "applicationId", "name", 0, "192.168.0.*", "urlMatch"));
        datas.add(new MockTrustedApplicationData(2, "anotherApplicationId", "name", 0, "192.168.0.*", "urlMatch"));

        //        TrustedApplicationManager manager = new DefaultTrustedApplicationManager(new MockTrustedApplicationStore(datas));
        final TrustedApplicationManager manager = new CachingTrustedApplicationManager(new DefaultTrustedApplicationManager(
            new MockTrustedApplicationStore(datas)), new MemoryCacheManager());

        // warm up pass
        for (int i = 0; i < 100000; i++)
        {
            manager.get(1);
        }

        final long start = System.currentTimeMillis();
        for (int i = 0; i < 10000000; i++)
        {
            manager.get(1);
        }

        final long end = System.currentTimeMillis();
        System.out.println("Test took: " + (end - start));
    }

    class CountingTrustedApplicationManager extends MockTrustedApplicationManager
    {
        final AtomicInteger countGetAll = new AtomicInteger(0);
        final AtomicInteger countStore = new AtomicInteger(0);
        final AtomicInteger countRemove = new AtomicInteger(0);

        public CountingTrustedApplicationManager(final TrustedApplicationInfo... datas)
        {
            super(datas);
        }

        @Override
        public Set<TrustedApplicationInfo> getAll()
        {
            countGetAll.incrementAndGet();
            return super.getAll();
        }

        @Override
        public TrustedApplicationInfo store(final User user, final TrustedApplicationInfo data)
        {
            countStore.incrementAndGet();
            return super.store(user, data);
        }

        @Override
        public TrustedApplicationInfo store(final String user, final TrustedApplicationInfo data)
        {
            countStore.incrementAndGet();
            return super.store(user, data);
        }

        @Override
        public boolean delete(final User user, final long id)
        {
            countRemove.incrementAndGet();
            return super.delete(user, id);
        }
    }

    @Test
    public void testGetAllOnlyCalledOnce() throws Exception
    {
        final CountingTrustedApplicationManager countingManager = new CountingTrustedApplicationManager();

        final CachingTrustedApplicationManager cachedManager =
                new CachingTrustedApplicationManager(countingManager, new MemoryCacheManager());
        assertNotNull(cachedManager.getAll());

        for (int i = 0; i < 100; i++)
        {
            assertEquals(0, cachedManager.getAll().size());
        }

        assertEquals(1, countingManager.countGetAll.get());
    }

    @Test
    public void testGetAllOnlyCalledTwiceWhenDataAdded() throws Exception
    {
        final CountingTrustedApplicationManager countingManager = new CountingTrustedApplicationManager();

        final CachingTrustedApplicationManager cachedManager =
                new CachingTrustedApplicationManager(countingManager, new MemoryCacheManager());
        assertNotNull(cachedManager.getAll());
        assertEquals(0, cachedManager.getAll().size());
        assertEquals(1, countingManager.countGetAll.get());

        final TrustedApplicationInfo data = cachedManager.store(getUser(), new MockTrustedApplicationInfo(0, "CONF", "confluence", 1000));
        assertNotNull(data);
        assertEquals(1, data.getNumericId());

        assertNotNull(cachedManager.getAll());
        assertEquals(1, cachedManager.getAll().size());
        assertEquals(2, countingManager.countGetAll.get());
    }

    @Test
    public void testGetAllOnlyCalledThriceWhenDataUpdated() throws Exception
    {
        final CountingTrustedApplicationManager countingManager = new CountingTrustedApplicationManager(
            new MockTrustedApplicationInfo(1, "CONF", "confluencing", 1000));

        final CachingTrustedApplicationManager cachedManager =
                new CachingTrustedApplicationManager(countingManager, new MemoryCacheManager());
        assertNotNull(cachedManager.getAll());
        assertEquals(1, cachedManager.getAll().size());
        assertEquals(1, countingManager.countGetAll.get());

        cachedManager.store(getUser(), new MockTrustedApplicationInfo(1, "CONF", "confluence", 7654));

        assertNotNull(cachedManager.getAll());
        assertEquals(1, cachedManager.getAll().size());
        assertEquals(2, countingManager.countGetAll.get());

        TrustedApplicationInfo data = cachedManager.get(1);
        assertEquals(1, data.getNumericId());
        assertEquals("confluence", data.getName());
        assertEquals(7654, data.getTimeout());

        assertEquals(2, countingManager.countGetAll.get());

        cachedManager.store("sysadmin", new MockTrustedApplicationInfo(2, "BONF", "bonfine", 7652));

        assertNotNull(cachedManager.getAll());
        assertEquals(2, cachedManager.getAll().size());
        assertEquals(3, countingManager.countGetAll.get());

        TrustedApplicationInfo data2 = cachedManager.get(2);
        assertEquals(2, data2.getNumericId());
        assertEquals("bonfine", data2.getName());
        assertEquals(7652, data2.getTimeout());

        assertEquals(3, countingManager.countGetAll.get());
    }

    @Test
    public void testGetAllOnlyCalledTwiceWhenDataRemoved() throws Exception
    {
        final CountingTrustedApplicationManager countingManager = new CountingTrustedApplicationManager(
            new MockTrustedApplicationInfo(1, "CONF", "confluencing", 1000));

        final CachingTrustedApplicationManager cachedManager =
                new CachingTrustedApplicationManager(countingManager, new MemoryCacheManager());
        assertNotNull(cachedManager.getAll());
        assertEquals(1, cachedManager.getAll().size());
        assertEquals(1, countingManager.countGetAll.get());
        assertEquals(0, countingManager.countStore.get());

        cachedManager.delete(getUser(), 1);

        assertNotNull(cachedManager.getAll());
        assertEquals(0, cachedManager.getAll().size());
        assertEquals(2, countingManager.countGetAll.get());
        assertEquals(0, countingManager.countStore.get());
        assertEquals(1, countingManager.countRemove.get());

        assertNull(cachedManager.get("CONF"));
    }

    @Test
    public void testAlternateGetsReturnSameObject() throws Exception
    {
        final CountingTrustedApplicationManager countingManager = new CountingTrustedApplicationManager(
            new MockTrustedApplicationInfo(1, "CONF", "confluencing", 1000));

        final CachingTrustedApplicationManager cachedManager =
                new CachingTrustedApplicationManager(countingManager, new MemoryCacheManager());

        final TrustedApplicationInfo info1 = cachedManager.get(1);
        final TrustedApplicationInfo info2 = cachedManager.get("CONF");
        assertSame(info1, info2);
    }

    private User getUser()
    {
        return new MockUser("TestCachingTrustedApplicationManager");
    }
}
