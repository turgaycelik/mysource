package com.atlassian.jira.propertyset;

import java.util.Date;
import java.util.Properties;

import com.atlassian.jira.local.MockControllerTestCase;

import com.opensymphony.module.propertyset.InvalidPropertyTypeException;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.memory.MemoryPropertySet;

import org.junit.Test;
import org.w3c.dom.Document;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestPropertySetCache extends MockControllerTestCase
{
    private static final String TEST_KEY = "TEST";
    
    @Test
    public void testBoolean() throws Exception
    {
        PropertySetCache propertySetCache = new PropertySetCache();
        // Unknown
        try
        {
            propertySetCache.getBoolean("apple");
            fail("Should throw NoValueCachedException.");
        }
        catch (PropertySetCache.NoValueCachedException e)
        {
            // Expected
        }
        assertEquals(null, propertySetCache.exists(TEST_KEY));

        // Set true
        propertySetCache.setBoolean(TEST_KEY, true);
        assertTrue(propertySetCache.getBoolean(TEST_KEY));
        assertEquals(Boolean.TRUE, propertySetCache.exists(TEST_KEY));

        // Set false
        propertySetCache.setBoolean(TEST_KEY, false);
        assertFalse(propertySetCache.getBoolean(TEST_KEY));
        assertEquals(Boolean.TRUE, propertySetCache.exists(TEST_KEY));

        // Removed
        propertySetCache.remove(TEST_KEY);
        // We should cached it doesn't exist, and therefore should return the default value
        assertFalse(propertySetCache.getBoolean(TEST_KEY));
        assertEquals(Boolean.FALSE, propertySetCache.exists(TEST_KEY));

        // Set a different type
        propertySetCache.setInt(TEST_KEY, 23);
        assertEquals(Boolean.TRUE, propertySetCache.exists(TEST_KEY));
        try
        {
            propertySetCache.getBoolean(TEST_KEY);
            fail("Should throw InvalidPropertyTypeException.");
        }
        catch (InvalidPropertyTypeException e)
        {
            // Expected
        }
    }

    @Test
    public void testInt() throws Exception
    {
        PropertySetCache propertySetCache = new PropertySetCache();
        // Unknown
        try
        {
            propertySetCache.getInt(TEST_KEY);
            fail("Should throw NoValueCachedException.");
        }
        catch (PropertySetCache.NoValueCachedException e)
        {
            // Expected
        }
        assertEquals(null, propertySetCache.exists(TEST_KEY));

        // Set the value
        propertySetCache.setInt(TEST_KEY, 3);
        assertEquals(3, propertySetCache.getInt(TEST_KEY));
        assertEquals(Boolean.TRUE, propertySetCache.exists(TEST_KEY));

        // Remove the value
        propertySetCache.remove(TEST_KEY);
        assertEquals(Boolean.FALSE, propertySetCache.exists(TEST_KEY));
        // We should cached it doesn't exist, and therefore should return the default value
        assertEquals(0, propertySetCache.getInt(TEST_KEY));

        // Set a different type
        propertySetCache.setBoolean(TEST_KEY, false);
        assertEquals(Boolean.TRUE, propertySetCache.exists(TEST_KEY));
        try
        {
            propertySetCache.getInt(TEST_KEY);
            fail("Should throw InvalidPropertyTypeException.");
        }
        catch (InvalidPropertyTypeException e)
        {
            // Expected
        }
    }

    @Test
    public void testLong() throws Exception
    {
        PropertySetCache propertySetCache = new PropertySetCache();
        // Unknown
        try
        {
            propertySetCache.getLong(TEST_KEY);
            fail("Should throw NoValueCachedException.");
        }
        catch (PropertySetCache.NoValueCachedException e)
        {
            // Expected
        }
        assertEquals(null, propertySetCache.exists(TEST_KEY));

        // Set the value
        propertySetCache.setLong(TEST_KEY, Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, propertySetCache.getLong(TEST_KEY));
        assertEquals(Boolean.TRUE, propertySetCache.exists(TEST_KEY));

        // Remove the value
        propertySetCache.remove(TEST_KEY);
        assertEquals(Boolean.FALSE, propertySetCache.exists(TEST_KEY));
        // We should cached it doesn't exist, and therefore should return the default value
        assertEquals(0, propertySetCache.getLong(TEST_KEY));

        // Set a different type
        propertySetCache.setBoolean(TEST_KEY, false);
        assertEquals(Boolean.TRUE, propertySetCache.exists(TEST_KEY));
        try
        {
            propertySetCache.getLong(TEST_KEY);
            fail("Should throw InvalidPropertyTypeException.");
        }
        catch (InvalidPropertyTypeException e)
        {
            // Expected
        }
    }

    @Test
    public void testDouble() throws Exception
    {
        PropertySetCache propertySetCache = new PropertySetCache();
        // Unknown
        try
        {
            propertySetCache.getDouble(TEST_KEY);
            fail("Should throw NoValueCachedException.");
        }
        catch (PropertySetCache.NoValueCachedException e)
        {
            // Expected
        }
        assertEquals(null, propertySetCache.exists(TEST_KEY));

        // Set the value
        propertySetCache.setDouble(TEST_KEY, 3.04);
        assertEquals(3.04, propertySetCache.getDouble(TEST_KEY), 0.0);
        assertEquals(Boolean.TRUE, propertySetCache.exists(TEST_KEY));

        // Remove the value
        propertySetCache.remove(TEST_KEY);
        assertEquals(Boolean.FALSE, propertySetCache.exists(TEST_KEY));
        // We should cached it doesn't exist, and therefore should return the default value
        assertEquals(0.0, propertySetCache.getDouble(TEST_KEY), 0.0);

        // Set a different type
        propertySetCache.setBoolean(TEST_KEY, false);
        assertEquals(Boolean.TRUE, propertySetCache.exists(TEST_KEY));
        try
        {
            propertySetCache.getDouble(TEST_KEY);
            fail("Should throw InvalidPropertyTypeException.");
        }
        catch (InvalidPropertyTypeException e)
        {
            // Expected
        }
    }

    @Test
    public void testString() throws Exception
    {
        PropertySetCache propertySetCache = new PropertySetCache();
        // Unknown
        try
        {
            propertySetCache.getString(TEST_KEY);
            fail("Should throw NoValueCachedException.");
        }
        catch (PropertySetCache.NoValueCachedException e)
        {
            // Expected
        }
        assertEquals(null, propertySetCache.exists(TEST_KEY));

        // Set the value
        propertySetCache.setString(TEST_KEY, "Apples");
        assertEquals("Apples", propertySetCache.getString(TEST_KEY));
        assertEquals(Boolean.TRUE, propertySetCache.exists(TEST_KEY));

        // Set the value to null
        propertySetCache.setString(TEST_KEY, null);
        assertEquals(null, propertySetCache.getString(TEST_KEY));
        // We are not really sure if the value is explicitly null, or missing, therefore we will defer to the underlying property set for the exists() method.
        assertEquals(null, propertySetCache.exists(TEST_KEY));

        // Remove the value
        propertySetCache.remove(TEST_KEY);
        assertEquals(Boolean.FALSE, propertySetCache.exists(TEST_KEY));
        // We should cached it doesn't exist, and therefore should return null
        assertEquals(null, propertySetCache.getString(TEST_KEY));

        // Set a different type
        propertySetCache.setBoolean(TEST_KEY, false);
        assertEquals(Boolean.TRUE, propertySetCache.exists(TEST_KEY));
        try
        {
            propertySetCache.getString(TEST_KEY);
            fail("Should throw InvalidPropertyTypeException.");
        }
        catch (InvalidPropertyTypeException e)
        {
            // Expected
        }
    }

    @Test
    public void testData() throws Exception
    {
        PropertySetCache propertySetCache = new PropertySetCache();
        // Unknown
        try
        {
            propertySetCache.getData(TEST_KEY);
            fail("Should throw NoValueCachedException.");
        }
        catch (PropertySetCache.NoValueCachedException e)
        {
            // Expected
        }
        assertEquals(null, propertySetCache.exists(TEST_KEY));

        // Set the value
        byte[] data = new byte[3];
        data[0] = 3;
        data[1] = 5;
        propertySetCache.setData(TEST_KEY, data);
        assertEquals(data, propertySetCache.getData(TEST_KEY));
        assertEquals(Boolean.TRUE, propertySetCache.exists(TEST_KEY));

        // Set the value to null
        propertySetCache.setData(TEST_KEY, null);
        assertEquals(null, propertySetCache.getData(TEST_KEY));
        // We are not really sure if the value is explicitly null, or missing, therefore we will defer to the underlying property set for the exists() method.
        assertEquals(null, propertySetCache.exists(TEST_KEY));

        // Remove the value
        propertySetCache.remove(TEST_KEY);
        assertEquals(Boolean.FALSE, propertySetCache.exists(TEST_KEY));
        // We should cached it doesn't exist, and therefore should return null
        assertEquals(null, propertySetCache.getData(TEST_KEY));

        // Set a different type
        propertySetCache.setBoolean(TEST_KEY, false);
        assertEquals(Boolean.TRUE, propertySetCache.exists(TEST_KEY));
        try
        {
            propertySetCache.getData(TEST_KEY);
            fail("Should throw InvalidPropertyTypeException.");
        }
        catch (InvalidPropertyTypeException e)
        {
            // Expected
        }
    }

    @Test
    public void testDate() throws Exception
    {
        PropertySetCache propertySetCache = new PropertySetCache();
        // Unknown
        try
        {
            propertySetCache.getDate(TEST_KEY);
            fail("Should throw NoValueCachedException.");
        }
        catch (PropertySetCache.NoValueCachedException e)
        {
            // Expected
        }
        assertEquals(null, propertySetCache.exists(TEST_KEY));

        // Set the value
        Date value = new Date(123);
        propertySetCache.setDate(TEST_KEY, value);
        assertEquals(value, propertySetCache.getDate(TEST_KEY));
        assertEquals(Boolean.TRUE, propertySetCache.exists(TEST_KEY));

        // Set the value to null
        propertySetCache.setDate(TEST_KEY, null);
        assertEquals(null, propertySetCache.getDate(TEST_KEY));
        // We are not really sure if the value is explicitly null, or missing, therefore we will defer to the underlying property set for the exists() method.
        assertEquals(null, propertySetCache.exists(TEST_KEY));

        // Remove the value
        propertySetCache.remove(TEST_KEY);
        assertEquals(Boolean.FALSE, propertySetCache.exists(TEST_KEY));
        // We should cached it doesn't exist, and therefore should return null
        assertEquals(null, propertySetCache.getDate(TEST_KEY));

        // Set a different type
        propertySetCache.setBoolean(TEST_KEY, false);
        assertEquals(Boolean.TRUE, propertySetCache.exists(TEST_KEY));
        try
        {
            propertySetCache.getDate(TEST_KEY);
            fail("Should throw InvalidPropertyTypeException.");
        }
        catch (InvalidPropertyTypeException e)
        {
            // Expected
        }
    }

    @Test
    public void testObject() throws Exception
    {
        PropertySetCache propertySetCache = new PropertySetCache();
        // Unknown
        try
        {
            propertySetCache.getObject(TEST_KEY);
            fail("Should throw NoValueCachedException.");
        }
        catch (PropertySetCache.NoValueCachedException e)
        {
            // Expected
        }
        assertEquals(null, propertySetCache.exists(TEST_KEY));

        // Set the value
        propertySetCache.setObject(TEST_KEY, "Apples");
        assertEquals("Apples", propertySetCache.getObject(TEST_KEY));
        assertEquals(Boolean.TRUE, propertySetCache.exists(TEST_KEY));

        // Set the value to null
        propertySetCache.setObject(TEST_KEY, null);
        assertEquals(null, propertySetCache.getObject(TEST_KEY));
        // We are not really sure if the value is explicitly null, or missing, therefore we will defer to the underlying property set for the exists() method.
        assertEquals(null, propertySetCache.exists(TEST_KEY));

        // Remove the value
        propertySetCache.remove(TEST_KEY);
        assertEquals(Boolean.FALSE, propertySetCache.exists(TEST_KEY));
        // We should cached it doesn't exist, and therefore should return null
        assertEquals(null, propertySetCache.getObject(TEST_KEY));

        // Set some different types - get Object should handle 'em all
        propertySetCache.setBoolean(TEST_KEY, true);
        assertEquals(Boolean.TRUE, propertySetCache.exists(TEST_KEY));
        assertEquals(Boolean.TRUE, propertySetCache.getObject(TEST_KEY));
        propertySetCache.setInt(TEST_KEY, 5);
        assertEquals(Boolean.TRUE, propertySetCache.exists(TEST_KEY));
        assertEquals(new Integer(5), propertySetCache.getObject(TEST_KEY));
    }

    @Test
    public void testProperties() throws Exception
    {
        PropertySetCache propertySetCache = new PropertySetCache();
        // Unknown
        try
        {
            propertySetCache.getProperties(TEST_KEY);
            fail("Should throw NoValueCachedException.");
        }
        catch (PropertySetCache.NoValueCachedException e)
        {
            // Expected
        }
        assertEquals(null, propertySetCache.exists(TEST_KEY));

        // Set the value
        Properties properties = new Properties();
        properties.setProperty("a", "1");
        propertySetCache.setProperties(TEST_KEY, properties);
        assertEquals(properties, propertySetCache.getProperties(TEST_KEY));
        assertEquals(Boolean.TRUE, propertySetCache.exists(TEST_KEY));

        // Set the value to null
        propertySetCache.setProperties(TEST_KEY, null);
        assertEquals(null, propertySetCache.getProperties(TEST_KEY));
        // We are not really sure if the value is explicitly null, or missing, therefore we will defer to the underlying property set for the exists() method.
        assertEquals(null, propertySetCache.exists(TEST_KEY));

        // Remove the value
        propertySetCache.remove(TEST_KEY);
        assertEquals(Boolean.FALSE, propertySetCache.exists(TEST_KEY));
        // We should cached it doesn't exist, and therefore should return null
        assertEquals(null, propertySetCache.getProperties(TEST_KEY));

        // Set a different type
        propertySetCache.setBoolean(TEST_KEY, false);
        assertEquals(Boolean.TRUE, propertySetCache.exists(TEST_KEY));
        try
        {
            propertySetCache.getProperties(TEST_KEY);
            fail("Should throw InvalidPropertyTypeException.");
        }
        catch (InvalidPropertyTypeException e)
        {
            // Expected
        }
    }

    @Test
    public void testText() throws Exception
    {
        PropertySetCache propertySetCache = new PropertySetCache();
        // Unknown
        try
        {
            propertySetCache.getText(TEST_KEY);
            fail("Should throw NoValueCachedException.");
        }
        catch (PropertySetCache.NoValueCachedException e)
        {
            // Expected
        }
        assertEquals(null, propertySetCache.exists(TEST_KEY));

        // Set the value
        propertySetCache.setText(TEST_KEY, "Apples");
        assertEquals("Apples", propertySetCache.getText(TEST_KEY));
        assertEquals(Boolean.TRUE, propertySetCache.exists(TEST_KEY));

        // Set the value to null
        propertySetCache.setText(TEST_KEY, null);
        assertEquals(null, propertySetCache.getText(TEST_KEY));
        // We are not really sure if the value is explicitly null, or missing, therefore we will defer to the underlying property set for the exists() method.
        assertEquals(null, propertySetCache.exists(TEST_KEY));

        // Remove the value
        propertySetCache.remove(TEST_KEY);
        assertEquals(Boolean.FALSE, propertySetCache.exists(TEST_KEY));
        // We should cached it doesn't exist, and therefore should return null
        assertEquals(null, propertySetCache.getText(TEST_KEY));

        // Set a different type
        propertySetCache.setBoolean(TEST_KEY, false);
        assertEquals(Boolean.TRUE, propertySetCache.exists(TEST_KEY));
        try
        {
            propertySetCache.getText(TEST_KEY);
            fail("Should throw InvalidPropertyTypeException.");
        }
        catch (InvalidPropertyTypeException e)
        {
            // Expected
        }
    }

    @Test
    public void testXml() throws Exception
    {
        PropertySetCache propertySetCache = new PropertySetCache();
        // Unknown
        try
        {
            propertySetCache.getXML(TEST_KEY);
            fail("Should throw NoValueCachedException.");
        }
        catch (PropertySetCache.NoValueCachedException e)
        {
            // Expected
        }
        assertEquals(null, propertySetCache.exists(TEST_KEY));

        // Set the value
        Document doc = getMock(Document.class);
        replay();
        propertySetCache.setXML(TEST_KEY, doc);
        assertEquals(doc, propertySetCache.getXML(TEST_KEY));
        assertEquals(Boolean.TRUE, propertySetCache.exists(TEST_KEY));

        // Set the value to null
        propertySetCache.setXML(TEST_KEY, null);
        assertEquals(null, propertySetCache.getXML(TEST_KEY));
        // We are not really sure if the value is explicitly null, or missing, therefore we will defer to the underlying property set for the exists() method.
        assertEquals(null, propertySetCache.exists(TEST_KEY));

        // Remove the value
        propertySetCache.remove(TEST_KEY);
        assertEquals(Boolean.FALSE, propertySetCache.exists(TEST_KEY));
        // We should cached it doesn't exist, and therefore should return null
        assertEquals(null, propertySetCache.getXML(TEST_KEY));

        // Set a different type
        propertySetCache.setBoolean(TEST_KEY, false);
        assertEquals(Boolean.TRUE, propertySetCache.exists(TEST_KEY));
        try
        {
            propertySetCache.getXML(TEST_KEY);
            fail("Should throw InvalidPropertyTypeException.");
        }
        catch (InvalidPropertyTypeException e)
        {
            // Expected
        }
        verify();
    }

    @Test
    public void testBulkLoad() throws Exception
    {
        // Set up a PropertySet with some values
        PropertySet source = new MemoryPropertySet();
        source.init(null, null);
        source.setBoolean("B", true);
        byte[] data = new byte[2];
        source.setData("Data", data);
        source.setDate("Date", new Date(666));
        source.setDouble("Do", 3.4);
        source.setInt("I", 42);
        source.setLong("L", 3000000000L);
        source.setObject("O", "OBJECT");
        final Properties properties = new Properties();
        properties.setProperty("a", "1");
        source.setProperties("P", properties);
        source.setString("S", "blah");
        source.setText("T", "text text");
        Document doc = getMock(Document.class);
        replay();
        source.setXML("X", doc);

        PropertySetCache propertySetCache = new PropertySetCache();
        propertySetCache.bulkLoad(source);
        assertEquals(true, propertySetCache.getBoolean("B"));
        assertEquals(data, propertySetCache.getData("Data"));
        assertEquals(new Date(666), propertySetCache.getDate("Date"));
        assertEquals(3.4, propertySetCache.getDouble("Do"), 0.0);
        assertEquals(42, propertySetCache.getInt("I"));
        assertEquals(3000000000L, propertySetCache.getLong("L"));
        assertEquals("OBJECT", propertySetCache.getObject("O"));
        assertSame(properties, propertySetCache.getProperties("P"));
        assertEquals("blah", propertySetCache.getString("S"));
        assertEquals("text text", propertySetCache.getText("T"));
        assertEquals(3.4, propertySetCache.getDouble("Do"), 0.0);
        assertSame(doc, propertySetCache.getXML("X"));
        try
        {
            propertySetCache.getBoolean("Z");
            fail("Should throw NoValueCachedException.");
        }
        catch (PropertySetCache.NoValueCachedException e)
        {
            // Expected
        }
    }

    @Test
    public void testExists() throws Exception
    {
        PropertySetCache propertySetCache = new PropertySetCache();
        // Unknown
        assertEquals(null, propertySetCache.exists(TEST_KEY));
        // set not exists
        propertySetCache.cacheExistance(TEST_KEY, false);
        assertEquals(Boolean.FALSE, propertySetCache.exists(TEST_KEY));
        // set exists
        propertySetCache.cacheExistance(TEST_KEY, true);
        assertEquals(Boolean.TRUE, propertySetCache.exists(TEST_KEY));
        // remove the property
        propertySetCache.remove(TEST_KEY);
        assertEquals(Boolean.FALSE, propertySetCache.exists(TEST_KEY));
    }

}
