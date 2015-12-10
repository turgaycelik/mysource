package com.atlassian.jira.propertyset;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.atlassian.jira.util.collect.MapBuilder;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.map.MapPropertySet;
import com.opensymphony.module.propertyset.memory.SerializablePropertySet;

import org.junit.Test;
import org.w3c.dom.Document;

import static com.google.common.collect.Maps.newHashMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class TestJiraCachingPropertySet
{
    private static final String TEST_KEY = "TEST";

    @Test
    public void testInitFailsWithNullPointer()
    {
        JiraCachingPropertySet jiraCachingPropertySet = new JiraCachingPropertySet();
        Map<String,Object> config = newHashMap();
        Map<String,Object> args = newHashMap();

        // test for issues with underlying PropertySet that we are caching:
        try
        {
            jiraCachingPropertySet.init(config, args);
            // this should throw an Exception - The inside Property set is missing.
            fail("init() should have thrown an Exception because there is no inner PropertySet.");
        }
        catch (NullPointerException ex)
        {
            // expected Exception - test the message
            assertEquals("Decorated property set is missing! Cannot initialise.", ex.getMessage());
        }
    }

    @SuppressWarnings ({ "unchecked" })
    @Test
    public void testInitWorks()
    {
        JiraCachingPropertySet jiraCachingPropertySet = new JiraCachingPropertySet();
        Map<String,Object> config = newHashMap();
        Map<String,Object> args = newHashMap();
        // now initialise with the underlying PropertySet available
        final MapPropertySet decoratedPropertySet = new MapPropertySet();
        decoratedPropertySet.init(config, args);
        args.put("PropertySet", decoratedPropertySet);
        // we just want to assert that this will run without breaking:
        jiraCachingPropertySet.init(config, args);
        assertEquals(0, jiraCachingPropertySet.getKeys().size());
    }

    @SuppressWarnings ({ "unchecked" })
    @Test
    public void testInitBulkLoad()
    {
        JiraCachingPropertySet jiraCachingPropertySet = new JiraCachingPropertySet();
        Map<String,Object> config = newHashMap();
        Map<String,Object> args = newHashMap();
        // test the bulkload function
        PropertySet underlyingPropertySet = createUnderlyingPropertySet();
        args.put("PropertySet", underlyingPropertySet);
        underlyingPropertySet.setString("fruit", "apple");
        // Firstly we leave bulkload as null
        jiraCachingPropertySet.init(config, args);
        underlyingPropertySet.setString("fruit", "banana");
        // bulkload should not have occured therefore the JiraCachingPropertySet should get new value.
        assertEquals("banana", jiraCachingPropertySet.getString("fruit"));
    }

    @SuppressWarnings ({ "unchecked" })
    @Test
    public void testInitBulkLoadFalse()
    {
        JiraCachingPropertySet jiraCachingPropertySet = new JiraCachingPropertySet();
        Map<String,Object> config = newHashMap();
        Map<String,Object> args = newHashMap();
        // now test with explicit false
        PropertySet underlyingPropertySet = createUnderlyingPropertySet();
        args.put("PropertySet", underlyingPropertySet);
        args.put("bulkload", Boolean.FALSE);
        underlyingPropertySet.setString("fruit", "apple");
        jiraCachingPropertySet.init(config, args);
        underlyingPropertySet.setString("fruit", "banana");
        // bulkload should not have occured therefore the JiraCachingPropertySet should get new value.
        assertEquals("banana", jiraCachingPropertySet.getString("fruit"));
    }

    @SuppressWarnings ({ "unchecked" })
    @Test
    public void testInitBulkLoadTrue()
    {
        JiraCachingPropertySet jiraCachingPropertySet = new JiraCachingPropertySet();
        Map<String,Object> config = newHashMap();
        Map<String,Object> args = newHashMap();
        // now test with explicit false
        PropertySet underlyingPropertySet = createUnderlyingPropertySet();
        args.put("PropertySet", underlyingPropertySet);

        // finally test with bulkload true
        args.put("bulkload", Boolean.TRUE);
        underlyingPropertySet.setString("fruit", "apple");
        jiraCachingPropertySet.init(config, args);
        underlyingPropertySet.setString("fruit", "banana");
        // bulkload should have occured therefore the JiraCachingPropertySet should already have old value cached
        assertEquals("apple", jiraCachingPropertySet.getString("fruit"));
    }

    @Test
    public void testBoolean() throws Exception
    {
        PropertySet underlyingPropertySet = createUnderlyingPropertySet();
        JiraCachingPropertySet cachingPropertySet = createJiraCachingPropertySet(underlyingPropertySet);

        // Set a string in the underlying PropertySet
        underlyingPropertySet.setBoolean(TEST_KEY, true);
        assertEquals(true, cachingPropertySet.getBoolean(TEST_KEY));
        // This should now be cached - change the underlying value directly
        underlyingPropertySet.setBoolean(TEST_KEY, false);
        // We should still get the cached value
        assertEquals(true, cachingPropertySet.getBoolean(TEST_KEY));
        // Set the value via JiraCachingPropertySet
        cachingPropertySet.setBoolean(TEST_KEY, false);
        // Now the cache is updated
        assertEquals(false, cachingPropertySet.getBoolean(TEST_KEY));
    }

    @Test
    public void testInt() throws Exception
    {
        PropertySet underlyingPropertySet = createUnderlyingPropertySet();
        JiraCachingPropertySet cachingPropertySet = createJiraCachingPropertySet(underlyingPropertySet);

        // Set a string in the underlying PropertySet
        underlyingPropertySet.setInt(TEST_KEY, 3);
        assertEquals(3, cachingPropertySet.getInt(TEST_KEY));
        // This should now be cached - change the underlying value directly
        underlyingPropertySet.setInt(TEST_KEY, 4);
        // We should still get the cached value
        assertEquals(3, cachingPropertySet.getInt(TEST_KEY));
        // Set the value via JiraCachingPropertySet
        cachingPropertySet.setInt(TEST_KEY, 5);
        // Now the cache is updated
        assertEquals(5, cachingPropertySet.getInt(TEST_KEY));
    }

    @Test
    public void testLong() throws Exception
    {
        PropertySet underlyingPropertySet = createUnderlyingPropertySet();
        JiraCachingPropertySet cachingPropertySet = createJiraCachingPropertySet(underlyingPropertySet);

        // Set a string in the underlying PropertySet
        underlyingPropertySet.setLong(TEST_KEY, 3000000000L);
        assertEquals(3000000000L, cachingPropertySet.getLong(TEST_KEY));
        // This should now be cached - change the underlying value directly
        underlyingPropertySet.setLong(TEST_KEY, 4000000000L);
        // We should still get the cached value
        assertEquals(3000000000L, cachingPropertySet.getLong(TEST_KEY));
        // Set the value via JiraCachingPropertySet
        cachingPropertySet.setLong(TEST_KEY, 5000000000L);
        // Now the cache is updated
        assertEquals(5000000000L, cachingPropertySet.getLong(TEST_KEY));
    }

    @Test
    public void testDouble() throws Exception
    {
        PropertySet underlyingPropertySet = createUnderlyingPropertySet();
        JiraCachingPropertySet cachingPropertySet = createJiraCachingPropertySet(underlyingPropertySet);

        // Set a string in the underlying PropertySet
        underlyingPropertySet.setDouble(TEST_KEY, 3.1);
        assertEquals(3.1, cachingPropertySet.getDouble(TEST_KEY), 0.0);
        // This should now be cached - change the underlying value directly
        underlyingPropertySet.setDouble(TEST_KEY, 4.1);
        // We should still get the cached value
        assertEquals(3.1, cachingPropertySet.getDouble(TEST_KEY), 0.0);
        // Set the value via JiraCachingPropertySet
        cachingPropertySet.setDouble(TEST_KEY, 5.1);
        // Now the cache is updated
        assertEquals(5.1, cachingPropertySet.getDouble(TEST_KEY), 0.0);
    }

    @Test
    public void testString() throws Exception
    {
        PropertySet underlyingPropertySet = createUnderlyingPropertySet();
        JiraCachingPropertySet cachingPropertySet = createJiraCachingPropertySet(underlyingPropertySet);

        // Set a string in the underlying PropertySet
        underlyingPropertySet.setString(TEST_KEY, "Rabbitohs");
        assertEquals("Rabbitohs", cachingPropertySet.getString(TEST_KEY));
        // This should now be cached - change the underlying value directly
        underlyingPropertySet.setString(TEST_KEY, "Roosters");
        // We should still get the cached value
        assertEquals("Rabbitohs", cachingPropertySet.getString(TEST_KEY));
        // Set the value via JiraCachingPropertySet
        cachingPropertySet.setString(TEST_KEY, "Roosters");
        // Now the cache is updated
        assertEquals("Roosters", cachingPropertySet.getString(TEST_KEY));
    }

    @Test
    public void testText() throws Exception
    {
        PropertySet underlyingPropertySet = createUnderlyingPropertySet();
        JiraCachingPropertySet cachingPropertySet = createJiraCachingPropertySet(underlyingPropertySet);

        // Set a string in the underlying PropertySet
        underlyingPropertySet.setText(TEST_KEY, "Rabbitohs");
        assertEquals("Rabbitohs", cachingPropertySet.getText(TEST_KEY));
        // This should now be cached - change the underlying value directly
        underlyingPropertySet.setText(TEST_KEY, "Roosters");
        // We should still get the cached value
        assertEquals("Rabbitohs", cachingPropertySet.getText(TEST_KEY));
        // Set the value via JiraCachingPropertySet
        cachingPropertySet.setText(TEST_KEY, "Roosters");
        // Now the cache is updated
        assertEquals("Roosters", cachingPropertySet.getText(TEST_KEY));
    }

    @Test
    public void testData() throws Exception
    {
        PropertySet underlyingPropertySet = createUnderlyingPropertySet();
        JiraCachingPropertySet cachingPropertySet = createJiraCachingPropertySet(underlyingPropertySet);

        final byte[] data1 = new byte[2];
        data1[1] = 4;
        final byte[] data2 = new byte[2];
        data1[1] = 5;
        // Set a value in the underlying PropertySet
        underlyingPropertySet.setData(TEST_KEY, data1);
        assertTrue(cachingPropertySet.getData(TEST_KEY) == data1);
        assertFalse(cachingPropertySet.getData(TEST_KEY) == data2);
        // This should now be cached - change the underlying value directly
        underlyingPropertySet.setData(TEST_KEY, data2);
        // We should still get the cached value
        assertTrue(cachingPropertySet.getData(TEST_KEY) == data1);
        assertFalse(cachingPropertySet.getData(TEST_KEY) == data2);
        // Set the value via JiraCachingPropertySet
        cachingPropertySet.setData(TEST_KEY, data2);
        // Now the cache is updated
        assertFalse(cachingPropertySet.getData(TEST_KEY) == data1);
        assertTrue(cachingPropertySet.getData(TEST_KEY) == data2);
    }

    @Test
    public void testDate() throws Exception
    {
        PropertySet underlyingPropertySet = createUnderlyingPropertySet();
        JiraCachingPropertySet cachingPropertySet = createJiraCachingPropertySet(underlyingPropertySet);

        // Set a value in the underlying PropertySet
        underlyingPropertySet.setDate(TEST_KEY, new Date(0));
        assertEquals(new Date(0), cachingPropertySet.getDate(TEST_KEY));
        // This should now be cached - change the underlying value directly
        underlyingPropertySet.setDate(TEST_KEY, new Date(3600000));
        // We should still get the cached value
        assertEquals(new Date(0), cachingPropertySet.getDate(TEST_KEY));
        // Set the value via JiraCachingPropertySet
        cachingPropertySet.setDate(TEST_KEY, new Date(3600000));
        // Now the cache is updated
        assertEquals(new Date(3600000), cachingPropertySet.getDate(TEST_KEY));
    }

    @Test
    public void testObject() throws Exception
    {
        PropertySet underlyingPropertySet = createUnderlyingPropertySet();
        JiraCachingPropertySet cachingPropertySet = createJiraCachingPropertySet(underlyingPropertySet);

        // Set a value in the underlying PropertySet
        underlyingPropertySet.setObject(TEST_KEY, "Rabbitohs");
        assertEquals("Rabbitohs", cachingPropertySet.getObject(TEST_KEY));
        // This should now be cached - change the underlying value directly
        underlyingPropertySet.setObject(TEST_KEY, "Roosters");
        // We should still get the cached value
        assertEquals("Rabbitohs", cachingPropertySet.getObject(TEST_KEY));
        // Set the value via JiraCachingPropertySet
        cachingPropertySet.setObject(TEST_KEY, "Roosters");
        // Now the cache is updated
        assertEquals("Roosters", cachingPropertySet.getObject(TEST_KEY));
    }

    @Test
    public void testProperties() throws Exception
    {
        PropertySet underlyingPropertySet = createUnderlyingPropertySet();
        JiraCachingPropertySet cachingPropertySet = createJiraCachingPropertySet(underlyingPropertySet);

        // Set a value in the underlying PropertySet
        Properties p1 = new Properties();
        p1.setProperty("a", "1");
        Properties p2 = new Properties();
        p2.setProperty("a", "2");

        underlyingPropertySet.setProperties(TEST_KEY, p1);
        assertTrue(cachingPropertySet.getProperties(TEST_KEY).equals(p1));
        assertFalse(cachingPropertySet.getProperties(TEST_KEY).equals(p2));
        // This should now be cached - change the underlying value directly
        underlyingPropertySet.setProperties(TEST_KEY, p2);
        // We should still get the cached value
        assertTrue(cachingPropertySet.getProperties(TEST_KEY).equals(p1));
        // Set the value via JiraCachingPropertySet
        cachingPropertySet.setProperties(TEST_KEY, p2);
        // Now the cache is updated
        assertTrue(cachingPropertySet.getProperties(TEST_KEY).equals(p2));
    }

    @Test
    public void testXML() throws Exception
    {
        PropertySet underlyingPropertySet = createUnderlyingPropertySet();
        JiraCachingPropertySet cachingPropertySet = createJiraCachingPropertySet(underlyingPropertySet);

        // Set a value in the underlying PropertySet
        final Document doc1 = mock(Document.class);
        final Document doc2 = mock(Document.class);

        underlyingPropertySet.setXML(TEST_KEY, doc1);
        assertSame(doc1, cachingPropertySet.getXML(TEST_KEY));
        assertNotSame(doc2, cachingPropertySet.getXML(TEST_KEY));
        // This should now be cached - change the underlying value directly
        underlyingPropertySet.setXML(TEST_KEY, doc2);
        // We should still get the cached value
        assertSame(doc1, cachingPropertySet.getXML(TEST_KEY));
        // Set the value via JiraCachingPropertySet
        cachingPropertySet.setXML(TEST_KEY, doc2);
        // Now the cache is updated
        assertSame(doc2, cachingPropertySet.getXML(TEST_KEY));
    }

    @Test
    public void testExists() throws Exception
    {
        PropertySet underlyingPropertySet = createUnderlyingPropertySet();
        JiraCachingPropertySet cachingPropertySet = createJiraCachingPropertySet(underlyingPropertySet);

        // Set a value in the underlying PropertySet
        underlyingPropertySet.setInt("A", 1);
        assertEquals(true, cachingPropertySet.exists("A"));
        assertEquals(false, cachingPropertySet.exists("B"));

        // Change the underlying values directly
        underlyingPropertySet.remove("A");
        underlyingPropertySet.setInt("B", 1);
        assertEquals(true, cachingPropertySet.exists("A"));
        assertEquals(false, cachingPropertySet.exists("B"));

        // Now Change the values via JiraCachingPropertySet
        cachingPropertySet.remove("A");
        cachingPropertySet.setInt("B", 1);
        assertEquals(false, cachingPropertySet.exists("A"));
        assertEquals(true, cachingPropertySet.exists("B"));
    }

    private static JiraCachingPropertySet createJiraCachingPropertySet(final PropertySet underlyingPropertySet)
    {
        JiraCachingPropertySet cachingPropertySet = new JiraCachingPropertySet();
        cachingPropertySet.init(null, MapBuilder.newBuilder().add("PropertySet", underlyingPropertySet).toMap());
        return cachingPropertySet;
    }

    static PropertySet createUnderlyingPropertySet()
    {
        PropertySet propertySet = new SerializablePropertySet();
        propertySet.init(null, null);
        return propertySet;
    }

    @Test
    public void clearingCacheOfUninitialisedInstanceShouldNotCauseNullPointerException()
    {
        new JiraCachingPropertySet().clearCache();
    }
}
