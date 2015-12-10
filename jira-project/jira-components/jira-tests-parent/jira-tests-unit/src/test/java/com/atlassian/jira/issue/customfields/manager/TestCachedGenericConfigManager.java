package com.atlassian.jira.issue.customfields.manager;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.atlassian.cache.memory.MemoryCacheManager;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class TestCachedGenericConfigManager
{
    @Test
    public void testCreate()
    {
        final AtomicReference<Object> createdReference = new AtomicReference<Object>();
        final Object retrievedFirst = new Object();
        final Object retrievedSecond = new Object();
        final AtomicReference<Object> retrievedReference = new AtomicReference<Object>(retrievedFirst);

        GenericConfigManager manager;
        {
            final MockGenericConfigManager mock = new MockGenericConfigManager()
            {
                @Override
                public void create(final String dataType, final String key, final Object obj)
                {
                    createdReference.set(obj);
                }

                @Override
                public Object retrieve(final String dataType, final String key)
                {
                    return retrievedReference.get();
                }
            };

            manager = new CachedGenericConfigManager(mock, new MemoryCacheManager());
        }

        final Object created = new Object();
        manager.create("dataType", "key", created);

        assertSame("should be passed through", created, createdReference.get());
        assertSame(retrievedFirst, manager.retrieve("dataType", "key"));
        retrievedReference.set(retrievedSecond);
        assertSame("should still be cached", retrievedFirst, manager.retrieve("dataType", "key"));
        manager.create("dataType", "key", created);
        assertSame("create should have cleared so we should now get the second reference", retrievedSecond, manager.retrieve("dataType", "key"));
    }

    @Test
    public void testUpdate()
    {
        final AtomicReference<Object> updatedReference = new AtomicReference<Object>();
        final Object retrievedFirst = new Object();
        final Object retrievedSecond = new Object();
        final AtomicReference<Object> retrievedReference = new AtomicReference<Object>(retrievedFirst);

        GenericConfigManager manager;
        {
            final MockGenericConfigManager mock = new MockGenericConfigManager()
            {
                @Override
                public void update(final String dataType, final String key, final Object obj)
                {
                    updatedReference.set(obj);
                }

                @Override
                public Object retrieve(final String dataType, final String key)
                {
                    return retrievedReference.get();
                }
            };

            manager = new CachedGenericConfigManager(mock,  new MemoryCacheManager());
        }

        final Object updated = new Object();
        manager.update("dataType", "key", updated);

        assertSame("should be passed through", updated, updatedReference.get());
        assertSame(retrievedFirst, manager.retrieve("dataType", "key"));
        retrievedReference.set(retrievedSecond);
        assertSame("should still be cached", retrievedFirst, manager.retrieve("dataType", "key"));
        manager.update("dataType", "key", updated);
        assertSame("create should have cleared so we should now get the second reference", retrievedSecond, manager.retrieve("dataType", "key"));
    }

    @Test
    public void testRetrieveAndRemove()
    {
        final AtomicInteger retrieveCount = new AtomicInteger();
        final Object retrievedFirst = new Object();
        final Object retrievedSecond = new Object();
        final AtomicReference<Object> retrievedReference = new AtomicReference<Object>(retrievedFirst);

        GenericConfigManager manager;
        {
            final MockGenericConfigManager mock = new MockGenericConfigManager()
            {
                @Override
                public Object retrieve(final String dataType, final String key)
                {
                    retrieveCount.getAndIncrement();
                    return retrievedReference.get();
                }
            };

            manager = new CachedGenericConfigManager(mock,  new MemoryCacheManager());
        }

        manager.remove("dataType", "key");
        assertEquals("shouldn't have called retrieve yet", 0, retrieveCount.get());
        assertSame(retrievedFirst, manager.retrieve("dataType", "key"));
        retrievedReference.set(retrievedSecond);
        assertSame("should still be cached", retrievedFirst, manager.retrieve("dataType", "key"));
        manager.remove("dataType", "key");
        assertEquals("retrieve count incorrect", 1, retrieveCount.get());
        assertSame("create should have cleared so we should now get the second reference", retrievedSecond, manager.retrieve("dataType", "key"));
        assertEquals("retrieve count incorrect", 2, retrieveCount.get());
        manager.retrieve("dataType", "key");
        manager.retrieve("dataType", "key");
        manager.retrieve("dataType", "key");
        assertEquals("retrieve count incorrect", 2, retrieveCount.get());
        manager.remove("dataType", "key");
        retrievedReference.set(null);
        assertEquals("retrieve count incorrect", 2, retrieveCount.get());
        assertNull("null not returned", manager.retrieve("dataType", "key"));
        assertEquals("retrieve count incorrect", 3, retrieveCount.get());
        assertNull("null not returned", manager.retrieve("dataType", "key"));
        assertNull("null not returned", manager.retrieve("dataType", "key"));
        assertEquals("retrieve count incorrect - null not cached", 3, retrieveCount.get());
        retrievedReference.set(retrievedFirst);
        manager.retrieve("dataType", "key");
        manager.retrieve("dataType2", "key");
        manager.retrieve("dataType", "key");
        manager.retrieve("dataType", "key2");
        assertEquals("retrieve count incorrect - should have missed cache twice", 5, retrieveCount.get());
        assertNull("null not returned for main key", manager.retrieve("dataType", "key"));
    }

    class MockGenericConfigManager implements GenericConfigManager
    {
        public void create(final String dataType, final String key, final Object obj)
        {}

        public void update(final String dataType, final String key, final Object obj)
        {}

        public void remove(final String dataType, final String key)
        {}

        public Object retrieve(final String dataType, final String key)
        {
            return null;
        }
    }
}
