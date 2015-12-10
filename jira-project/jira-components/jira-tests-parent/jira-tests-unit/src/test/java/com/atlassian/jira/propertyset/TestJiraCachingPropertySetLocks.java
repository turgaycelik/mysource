package com.atlassian.jira.propertyset;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import javax.annotation.Nonnull;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetSchema;

import org.dom4j.dom.DOMDocument;
import org.junit.Test;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class TestJiraCachingPropertySetLocks
{
    @Test
    public void testSetBooleanLocks() throws Exception
    {
        final MockLock lock = new MockLock();
        final JiraCachingPropertySet propertySet = new JiraCachingPropertySet(lock);
        init(propertySet);
        lock.assertLockedCount(1);

        // The first get will miss the cache and should lock
        assertFalse(propertySet.getBoolean("test"));
        lock.assertLockedCount(2);

        propertySet.setBoolean("test", true);
        lock.assertLockedCount(3);

        // The value is now cached, so no lock is needed.
        assertTrue(propertySet.getBoolean("test"));
        lock.assertLockedCount(3);
    }

    @Test
    public void testGetSetDataLocks() throws Exception
    {
        final MockLock lock = new MockLock();
        final JiraCachingPropertySet propertySet = new JiraCachingPropertySet(lock);
        init(propertySet);

        // The first get will miss the cache and should lock
        assertNull(propertySet.getData("test"));
        lock.assertLockedCount(2);

        propertySet.setData("test", "roger".getBytes());
        lock.assertLockedCount(3);

        // The value is now cached, so no lock is needed.
        assertTrue(Arrays.equals("roger".getBytes(), propertySet.getData("test")));
        lock.assertLockedCount(3);
    }

    @Test
    public void testGetSetDateLocks() throws Exception
    {
        final MockLock lock = new MockLock();
        final JiraCachingPropertySet propertySet = new JiraCachingPropertySet(lock);
        init(propertySet);

        // The first get will miss the cache and should lock
        assertNull(propertySet.getDate("test"));
        lock.assertLockedCount(2);

        propertySet.setDate("test", new Date(100000));
        lock.assertLockedCount(3);

        // The value is now cached, so no lock is needed.
        assertEquals(new Date(100000), propertySet.getDate("test"));
        lock.assertLockedCount(3);
    }

    @Test
    public void testGetSetDoubleLocks() throws Exception
    {
        final MockLock lock = new MockLock();
        final JiraCachingPropertySet propertySet = new JiraCachingPropertySet(lock);
        init(propertySet);

        // The first get will miss the cache and should lock
        assertEquals(0.0, propertySet.getDouble("test"), 0.0);
        lock.assertLockedCount(2);

        propertySet.setDouble("test", new Double(123.456).doubleValue());
        lock.assertLockedCount(3);

        assertEquals(123.456, propertySet.getDouble("test"), 0.0);
        // The value is now cached, so no lock is needed.
        lock.assertLockedCount(3);
    }

    @Test
    public void testGetSetIntLocks() throws Exception
    {
        final MockLock lock = new MockLock();
        final JiraCachingPropertySet propertySet = new JiraCachingPropertySet(lock);
        init(propertySet);

        // The first get will miss the cache and should lock
        assertEquals(0, propertySet.getInt("test"));
        lock.assertLockedCount(2);

        propertySet.setInt("test", new Integer(890).intValue());
        lock.assertLockedCount(3);

        assertEquals(new Integer(890).intValue(), propertySet.getInt("test"));
        // The value is now cached, so no lock is needed.
        lock.assertLockedCount(3);
    }

    @Test
    public void testGetSetLongLocks() throws Exception
    {
        final MockLock lock = new MockLock();
        final JiraCachingPropertySet propertySet = new JiraCachingPropertySet(lock);
        init(propertySet);

        // The first get will miss the cache and should lock
        assertEquals(0, propertySet.getLong("test"));
        lock.assertLockedCount(2);

        propertySet.setLong("test", new Long(890).longValue());
        lock.assertLockedCount(3);

        assertEquals(new Long(890).longValue(), propertySet.getLong("test"));
        // The value is now cached, so no lock is needed.
        lock.assertLockedCount(3);
    }

    @Test
    public void testGetSetObjectLocks() throws Exception
    {
        final MockLock lock = new MockLock();
        final JiraCachingPropertySet propertySet = new JiraCachingPropertySet(lock);
        init(propertySet);

        // The first get will miss the cache and should lock
        assertNull(propertySet.getObject("test"));
        lock.assertLockedCount(2);

        propertySet.setObject("test", new Integer(891));
        lock.assertLockedCount(3);

        assertEquals(891, propertySet.getObject("test"));
        // The value is now cached, so no lock is needed.
        lock.assertLockedCount(3);
    }

    @Test
    public void testGetSetPropertiesLocks() throws Exception
    {
        final MockLock lock = new MockLock();
        final JiraCachingPropertySet propertySet = new JiraCachingPropertySet(lock);
        init(propertySet);

        // The first get will miss the cache and should lock
        assertNull(propertySet.getProperties("test"));
        lock.assertLockedCount(2);

        propertySet.setProperties("test", new Properties());
        lock.assertLockedCount(3);

        assertEquals(new Properties(), propertySet.getProperties("test"));
        // The value is now cached, so no lock is needed.
        lock.assertLockedCount(3);
    }

    @Test
    public void testGetSetXmlLocks() throws Exception
    {
        final MockLock lock = new MockLock();
        final JiraCachingPropertySet propertySet = new JiraCachingPropertySet(lock);
        init(propertySet);

        // The first get will miss the cache and should lock
        assertNull(propertySet.getXML("test"));
        lock.assertLockedCount(2);

        propertySet.setXML("test", new DOMDocument());
        lock.assertLockedCount(3);

        // we don't really care about the fidelity of the document (they don't support equals())
        // just that the lock was called
        assertEquals(new DOMDocument().getAttributes(), propertySet.getXML("test").getAttributes());
        // The value is now cached, so no lock is needed.
        lock.assertLockedCount(3);
    }

    @Test
    public void testGetSetSchemaDoesNotLock() throws Exception
    {
        final MockLock lock = new MockLock();
        final JiraCachingPropertySet propertySet = new JiraCachingPropertySet(lock);
        init(propertySet);
        lock.assertLockedCount(1);

        final PropertySetSchema propertySetSchema = new PropertySetSchema();
        propertySet.setSchema(propertySetSchema);
        lock.assertLockedCount(1);

        assertSame(propertySetSchema, propertySet.getSchema());
        lock.assertLockedCount(1);
    }

    @Test
    public void testGetSetStringLocks() throws Exception
    {
        final MockLock lock = new MockLock();
        final JiraCachingPropertySet propertySet = new JiraCachingPropertySet(lock);
        init(propertySet);

        // The first get will miss the cache and should lock
        assertNull(propertySet.getString("test"));
        lock.assertLockedCount(2);

        propertySet.setString("test", "fred");
        lock.assertLockedCount(3);

        assertEquals("fred", propertySet.getString("test"));
        // The value is now cached, so no lock is needed.
        lock.assertLockedCount(3);
    }

    @Test
    public void testGetSetTextLocks() throws Exception
    {
        final MockLock lock = new MockLock();
        final JiraCachingPropertySet propertySet = new JiraCachingPropertySet(lock);
        init(propertySet);

        // The first get will miss the cache and should lock
        assertNull(propertySet.getText("test"));
        lock.assertLockedCount(2);

        propertySet.setText("test", "some text");
        lock.assertLockedCount(3);

        assertEquals("some text", propertySet.getText("test"));
        // The value is now cached, so no lock is needed.
        lock.assertLockedCount(3);
    }

    @Test
    public void testGetTypeLocks() throws Exception
    {
        final MockLock lock = new MockLock();
        final JiraCachingPropertySet propertySet = new JiraCachingPropertySet(lock);
        init(propertySet);

        propertySet.setText("test", "some text");
        lock.assertLockedCount(2);

        assertEquals(PropertySet.TEXT, propertySet.getType("test"));
        lock.assertLockedCount(3);
    }

    @Test
    public void testGetKeysDoesNotLock() throws Exception
    {
        final MockLock lock = new MockLock();
        final JiraCachingPropertySet propertySet = new JiraCachingPropertySet(lock);
        init(propertySet);

        propertySet.setText("test", "some text");
        lock.assertLockedCount(2);

        assertEquals(newArrayList("test"), propertySet.getKeys());
        lock.assertLockedCount(2);

        assertEquals(newArrayList("test"), propertySet.getKeys(PropertySet.TEXT));
        lock.assertLockedCount(2);

        assertEquals(newArrayList("test"), propertySet.getKeys("tes"));
        lock.assertLockedCount(2);

        assertEquals(newArrayList("test"), propertySet.getKeys("tes", PropertySet.TEXT));
        lock.assertLockedCount(2);
    }

    @Test
    public void testIsSettableDoesNotLock() throws Exception
    {
        final MockLock lock = new MockLock();
        final JiraCachingPropertySet propertySet = new JiraCachingPropertySet(lock);
        init(propertySet);
        lock.assertLockedCount(1);

        assertTrue(propertySet.isSettable("test"));
        lock.assertLockedCount(1);
    }

    @Test
    public void testSupportsTypeDoesNotLock() throws Exception
    {
        final MockLock lock = new MockLock();
        final JiraCachingPropertySet propertySet = new JiraCachingPropertySet(lock);
        init(propertySet);
        lock.assertLockedCount(1);

        assertTrue(propertySet.supportsType(PropertySet.STRING));
        lock.assertLockedCount(1);
    }

    @Test
    public void testSupportsTypesDoesNotLock() throws Exception
    {
        final MockLock lock = new MockLock();
        final JiraCachingPropertySet propertySet = new JiraCachingPropertySet(lock);
        init(propertySet);
        lock.assertLockedCount(1);

        assertTrue(propertySet.supportsTypes());
        lock.assertLockedCount(1);
    }

    @Test
    public void testInitLocks() throws Exception
    {
        final MockLock lock = new MockLock();
        final JiraCachingPropertySet propertySet = new JiraCachingPropertySet(lock);
        init(propertySet);
        lock.assertLockedCount(1);
    }

    @Test
    public void testGetAsTypeDoesLessLocks() throws Exception
    {
        final MockLock lock = new MockLock();
        final JiraCachingPropertySet propertySet = new JiraCachingPropertySet(lock);
        init(propertySet);

        lock.assertLockedCount(1);
        // The first time we call, getType() will do a lock, but nothing else.
        assertNull(propertySet.getAsActualType("test"));
        lock.assertLockedCount(2);

        propertySet.setAsActualType("test", Boolean.TRUE);
        lock.assertLockedCount(3);

        assertEquals(Boolean.TRUE, propertySet.getAsActualType("test"));
        // Now we can get from cache, and should get no locks for getBoolean, but we get a lock from getType() the first time
        lock.assertLockedCount(4);

        assertEquals(Boolean.TRUE, propertySet.getAsActualType("test"));
        // but getType is cached the second time
        lock.assertLockedCount(4);

        assertEquals(Boolean.TRUE, propertySet.getAsActualType("test"));
        // and 3rd time etc
        lock.assertLockedCount(4);
    }

    private static void init(final JiraCachingPropertySet propertySet)
    {
        final Map<?, ?> config = newHashMap();
        final Map<String, Object> args = newHashMap();
        // now test with explicit false
        final PropertySet underlyingPropertySet = TestJiraCachingPropertySet.createUnderlyingPropertySet();
        args.put("PropertySet", underlyingPropertySet);
        propertySet.init(config, args);
    }

    static class MockLock implements Lock
    {
        int locked, unlocked;

        public void lock()
        {
            locked++;
        }

        public void lockInterruptibly() throws InterruptedException
        {}

        @Nonnull
        @Override
        public Condition newCondition()
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        public boolean tryLock()
        {
            return false;
        }

        public boolean tryLock(final long time, @Nonnull final TimeUnit unit) throws InterruptedException
        {
            return false;
        }

        public void unlock()
        {
            unlocked++;
        }

        public void assertLockedCount(final int count)
        {
            assertEquals(count, locked);
            assertEquals(count, unlocked);
        }
    }
}
