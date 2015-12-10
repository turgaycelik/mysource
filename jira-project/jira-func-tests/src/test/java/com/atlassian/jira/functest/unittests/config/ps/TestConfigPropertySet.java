package com.atlassian.jira.functest.unittests.config.ps;

import com.atlassian.jira.functest.config.ps.ConfigPropertySet;
import com.atlassian.jira.functest.config.ps.ConfigPropertySetEntry;
import junit.framework.TestCase;

import java.math.BigInteger;
import java.util.Collections;

/**
 * Test for {@link com.atlassian.jira.functest.config.ps.ConfigPropertySet}.
 *
 * @since v4.1
 */
public class TestConfigPropertySet extends TestCase
{
    public void testCotr() throws Exception
    {
        ConfigPropertySet set = new ConfigPropertySet("name", 7L);
        assertEquals("name", set.getEntityName());
        assertEquals(7L, (long) set.getEntityId());
        assertEquals(Collections.<String, ConfigPropertySetEntry>emptyMap(), set.entryMap());

        set = new ConfigPropertySet();
        assertNull(set.getEntityName());
        assertNull(set.getEntityId());
        assertEquals(Collections.<String, ConfigPropertySetEntry>emptyMap(), set.entryMap());
    }

    public void testStringProperty() throws Exception
    {
        checkTextProperty(new PropertySetter<String>()
        {
            public boolean set(final ConfigPropertySet s, final String name, final String value)
            {
                return s.setStringProperty(name, value);
            }
        });
    }

    public void testTextProperty() throws Exception
    {
        checkTextProperty(new PropertySetter<String>()
        {
            public boolean set(final ConfigPropertySet s, final String name, final String value)
            {
                return s.setTextProperty(name, value);
            }
        });
    }

    public void testIntegerProperty() throws Exception
    {
        final String propertyName = "test";
        Integer value = null;

        ConfigPropertySet set = new ConfigPropertySet("name", 7L);
        assertFalse(set.setIntegerProperty(propertyName, value));
        assertPropertyDoesNotExist(set, propertyName);

        value = 1;
        assertFalse(set.setIntegerProperty(propertyName, value));
        assertInteger(set, propertyName, value);

        value = 0;
        assertTrue(set.setIntegerProperty(propertyName, value));
        assertInteger(set, propertyName, value);

        assertTrue(set.removeProperty(propertyName));
        assertPropertyDoesNotExist(set, propertyName);
        assertFalse(set.removeProperty(propertyName));

        value = Integer.MAX_VALUE;
        assertFalse(set.setIntegerProperty(propertyName, value));
        assertInteger(set, propertyName, value);

        assertTrue(set.setIntegerProperty(propertyName, null));
        assertPropertyDoesNotExist(set, propertyName);
        assertFalse(set.setIntegerProperty(propertyName, null));

        value = Integer.MIN_VALUE;
        assertFalse(set.setIntegerProperty(propertyName, value));
        assertInteger(set, propertyName, value);
    }

    public void testLongProperty() throws Exception
    {
        final String propertyName = "test";
        Long value = null;

        ConfigPropertySet set = new ConfigPropertySet("name", 7L);
        assertFalse(set.setLongProperty(propertyName, value));
        assertPropertyDoesNotExist(set, propertyName);

        value = 1L;
        assertFalse(set.setLongProperty(propertyName, value));
        assertLong(set, propertyName, value);

        value = 0L;
        assertTrue(set.setLongProperty(propertyName, value));
        assertLong(set, propertyName, value);

        assertTrue(set.removeProperty(propertyName));
        assertPropertyDoesNotExist(set, propertyName);
        assertFalse(set.removeProperty(propertyName));

        value = (long)Integer.MAX_VALUE;
        assertFalse(set.setLongProperty(propertyName, value));
        assertLong(set, propertyName, value);

        value = (long)Integer.MAX_VALUE + 1L;
        assertTrue(set.setLongProperty(propertyName, value));
        assertLong(set, propertyName, value);

        assertTrue(set.setLongProperty(propertyName, null));
        assertPropertyDoesNotExist(set, propertyName);
        assertFalse(set.setLongProperty(propertyName, null));

        value = (long)Integer.MIN_VALUE - 1L;
        assertFalse(set.setLongProperty(propertyName, value));
        assertLong(set, propertyName, value);

        value = Long.MAX_VALUE;
        assertTrue(set.setLongProperty(propertyName, value));
        assertLong(set, propertyName, value);

        value = Long.MIN_VALUE;
        assertTrue(set.setLongProperty(propertyName, value));
        assertLong(set, propertyName, value);
    }

    public void testBooleanProperty() throws Exception
    {
        final String propertyName = "test";
        Boolean value = null;

        ConfigPropertySet set = new ConfigPropertySet("name", 7L);
        assertFalse(set.setBooleanProperty(propertyName, value));
        assertPropertyDoesNotExist(set, propertyName);

        value = true;
        assertFalse(set.setBooleanProperty(propertyName, value));
        assertBoolean(set, propertyName, value);

        value = true;
        assertTrue(set.setBooleanProperty(propertyName, value));
        assertBoolean(set, propertyName, value);

        assertTrue(set.removeProperty(propertyName));
        assertPropertyDoesNotExist(set, propertyName);
        assertFalse(set.removeProperty(propertyName));

        value = true;
        assertFalse(set.setBooleanProperty(propertyName, value));
        assertBoolean(set, propertyName, value);

        value = null;
        assertTrue(set.setBooleanProperty(propertyName, value));
        assertPropertyDoesNotExist(set, propertyName);

    }

    private void checkTextProperty(PropertySetter<String> setter)
    {
        final String propertyName = "test";
        String value = null;

        ConfigPropertySet set = new ConfigPropertySet("name", 7L);
        assertFalse(setter.set(set, propertyName, value));
        assertPropertyDoesNotExist(set, propertyName);

        value = "value";
        assertFalse(setter.set(set, propertyName, value));
        assertText(set, propertyName, value);

        value = "true";
        assertTrue(setter.set(set, propertyName, value));
        assertText(set, propertyName, value);

        assertTrue(set.removeProperty(propertyName));
        assertFalse(set.removeProperty(propertyName));

        value = "1";
        assertFalse(setter.set(set, propertyName, value));
        assertText(set, propertyName, value);

        value = "0";
        assertTrue(setter.set(set, propertyName, value));
        assertText(set, propertyName, value);

        value = String.valueOf(Integer.MAX_VALUE);
        assertTrue(setter.set(set, propertyName, value));
        assertText(set, propertyName, value);

        value = String.valueOf(Integer.MIN_VALUE);
        assertTrue(setter.set(set, propertyName, value));
        assertText(set, propertyName, value);

        value = String.valueOf(Integer.MAX_VALUE + 1L);
        assertTrue(setter.set(set, propertyName, value));
        assertText(set, propertyName, value);

        value = String.valueOf(Integer.MIN_VALUE - 1L);
        assertTrue(setter.set(set, propertyName, value));
        assertText(set, propertyName, value);

        BigInteger integer = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE);
        value = integer.toString();
        assertTrue(setter.set(set, propertyName, value));
        assertText(set, propertyName, value);

        integer = BigInteger.valueOf(Long.MIN_VALUE).subtract(BigInteger.ONE);
        value = integer.toString();
        assertTrue(setter.set(set, propertyName, value));
        assertText(set, propertyName, value);

        assertTrue(setter.set(set, propertyName, null));
        assertFalse(setter.set(set, propertyName, null));
        assertPropertyDoesNotExist(set, propertyName);
    }

    private void assertBoolean(final ConfigPropertySet set, final String propertyName, final Boolean boolValue)
    {
        final String strValue;
        final Integer intValue;
        final Long longValue;

        if (boolValue != null)
        {
            strValue = boolValue.toString();
            intValue = boolValue ? 1 : 0;
            longValue = boolValue ? 1L : 0L;
        }
        else
        {
            strValue = null;
            intValue = null;
            longValue = null;
        }

        assertProperty(set, propertyName, strValue, boolValue, longValue, intValue, boolValue);

    }

    private void assertInteger(final ConfigPropertySet set, final String propertyName, final Integer intValue)
    {
        final String strValue;
        final Boolean boolValue;
        final Long longValue;

        if (intValue != null)
        {
            strValue = intValue.toString();
            boolValue = intValue != 0;
            longValue = intValue.longValue();
        }
        else
        {
            strValue = null;
            boolValue = null;
            longValue = null;
        }

        assertProperty(set, propertyName, strValue, boolValue, longValue, intValue, intValue);
    }

    private void assertLong(final ConfigPropertySet set, final String propertyName, final Long longValue)
    {
        final String strValue;
        final Boolean boolValue;
        final Integer intValue;

        if (longValue != null)
        {
            final int tmpInt = longValue.intValue();

            strValue = longValue.toString();
            boolValue = longValue != 0;
            intValue = longValue != tmpInt ? null : tmpInt;
        }
        else
        {
            strValue = null;
            boolValue = null;
            intValue = null;
        }

        assertProperty(set, propertyName, strValue, boolValue, longValue, intValue, longValue);
    }

    private void assertText(final ConfigPropertySet set, final String propertyName, final String stringValue)
    {
        assertProperty(set, propertyName, stringValue, parseBoolean(stringValue), parseLong(stringValue),
                parseInt(stringValue), stringValue);
    }

    private void assertProperty(final ConfigPropertySet set, final String propertyName, final String stringValue,
            final Boolean boolValue, final Long longValue, final Integer intValue, final Object objValue)
    {
        assertTrue(set.contains(propertyName));
        assertEquals(stringValue, set.getStringProperty(propertyName));
        assertEquals(stringValue, set.getTextProperty(propertyName));
        assertEquals(boolValue, set.getBooleanProperty(propertyName));
        assertEquals(intValue, set.getIntegerProperty(propertyName));
        assertEquals(longValue, set.getLongProperty(propertyName));
        assertEquals(objValue, set.getObjectProperty(propertyName));
    }

    private static Boolean parseBoolean(final String stringValue)
    {
        final Integer integer = parseInt(stringValue);
        return integer != null && integer != 0;
    }

    private static Long parseLong(final String stringValue)
    {
        if (stringValue == null)
        {
            return null;
        }

        try
        {
            return Long.valueOf(stringValue);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    private static Integer parseInt(final String stringValue)
    {
        if (stringValue == null)
        {
            return null;
        }

        try
        {
            return Integer.valueOf(stringValue);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    private void assertPropertyDoesNotExist(final ConfigPropertySet set, final String propertyName)
    {
        assertFalse(set.contains(propertyName));
        assertNull(set.getStringProperty(propertyName));
        assertNull(set.getTextProperty(propertyName));
        assertNull(set.getBooleanProperty(propertyName));
        assertNull(set.getIntegerProperty(propertyName));
        assertNull(set.getObjectProperty(propertyName));
        assertNull(set.getLongProperty(propertyName));

        assertEquals("default", set.getStringPropertyDefault(propertyName, "default"));
        assertTrue(set.getBooleanPropertyDefault(propertyName, true));
        assertEquals(-1L, (Object)set.getLongPropertyDefault(propertyName, -1L));
    }

    private interface PropertySetter<T>
    {
        boolean set(ConfigPropertySet s, String name, T value);
    }
}
