package com.atlassian.jira.functest.unittests.config.ps;

import com.atlassian.jira.functest.config.ps.ConfigPropertySetEntry;
import junit.framework.TestCase;

import java.math.BigInteger;

/**
 * Test for {@link com.atlassian.jira.functest.config.ps.ConfigPropertySetEntry}.
 *
 * @since v4.1
 */
public class TestConfigPropertySetEntry extends TestCase
{
    public void testStringEntry() throws Exception
    {
        checkString(new EntryFactory<String>()
        {
            public ConfigPropertySetEntry create(final String name, final String value)
            {
                return ConfigPropertySetEntry.createStringEntry(name, value);
            }
        }, ConfigPropertySetEntry.Type.STRING);
    }

    public void testTextEntry() throws Exception
    {
        checkString(new EntryFactory<String>()
        {
            public ConfigPropertySetEntry create(final String name, final String value)
            {
                return ConfigPropertySetEntry.createTextEntry(name, value);
            }
        }, ConfigPropertySetEntry.Type.TEXT);
    }

    public void testIntegerEntry() throws Exception
    {
        final String name = "name";
        Integer value = null;

        ConfigPropertySetEntry entry = ConfigPropertySetEntry.createIntegerEntry(name, value);
        assertIntegerEntry(entry, name, value);

        value = 10;
        entry = ConfigPropertySetEntry.createIntegerEntry(name, value);
        assertIntegerEntry(entry, name, value);

        value = 0;
        entry = ConfigPropertySetEntry.createIntegerEntry(name, value);
        assertIntegerEntry(entry, name, value);

        value = Integer.MAX_VALUE;
        entry = ConfigPropertySetEntry.createIntegerEntry(name, value);
        assertIntegerEntry(entry, name, value);

        value = Integer.MIN_VALUE;
        entry = ConfigPropertySetEntry.createIntegerEntry(name, value);
        assertIntegerEntry(entry, name, value);
    }

    public void testBooleanEntry() throws Exception
    {
        final String name = "name";
        Boolean value = null;

        ConfigPropertySetEntry entry = ConfigPropertySetEntry.createBooleanEntry(name, value);
        assertBooleanEntry(entry, name, value);

        value = Boolean.TRUE;
        entry = ConfigPropertySetEntry.createBooleanEntry(name, value);
        assertBooleanEntry(entry, name, value);

        value = Boolean.FALSE;
        entry = ConfigPropertySetEntry.createBooleanEntry(name, value);
        assertBooleanEntry(entry, name, value);
    }

    public void testLongEntry() throws Exception
    {
        final String name = "name";
        Long value = null;

        ConfigPropertySetEntry entry = ConfigPropertySetEntry.createLongEntry(name, value);
        assertLongEntry(entry, name, value);

        value = 10L;
        entry = ConfigPropertySetEntry.createLongEntry(name, value);
        assertLongEntry(entry, name, value);

        value = 0L;
        entry = ConfigPropertySetEntry.createLongEntry(name, value);
        assertLongEntry(entry, name, value);

        value = (long)Integer.MAX_VALUE;
        entry = ConfigPropertySetEntry.createLongEntry(name, value);
        assertLongEntry(entry, name, value);

        value = Integer.MAX_VALUE + 1L;
        entry = ConfigPropertySetEntry.createLongEntry(name, value);
        assertLongEntry(entry, name, value);

        value = Integer.MIN_VALUE - 1L;
        entry = ConfigPropertySetEntry.createLongEntry(name, value);
        assertLongEntry(entry, name, value);

        value = Long.MAX_VALUE;
        entry = ConfigPropertySetEntry.createLongEntry(name, value);
        assertLongEntry(entry, name, value);

        value = Long.MIN_VALUE;
        entry = ConfigPropertySetEntry.createLongEntry(name, value);
        assertLongEntry(entry, name, value);
    }

    public void testGetTypeFromInt() throws Exception
    {
        assertEquals(ConfigPropertySetEntry.Type.STRING, ConfigPropertySetEntry.Type.forPropertySetType(5));
        assertEquals(ConfigPropertySetEntry.Type.TEXT, ConfigPropertySetEntry.Type.forPropertySetType(6));
        assertEquals(ConfigPropertySetEntry.Type.INTEGER, ConfigPropertySetEntry.Type.forPropertySetType(2));
        assertEquals(ConfigPropertySetEntry.Type.LONG, ConfigPropertySetEntry.Type.forPropertySetType(3));
        assertEquals(ConfigPropertySetEntry.Type.BOOLEAN, ConfigPropertySetEntry.Type.forPropertySetType(1));

        assertNull(ConfigPropertySetEntry.Type.forPropertySetType(0));
        assertNull(ConfigPropertySetEntry.Type.forPropertySetType(-1));
        assertNull(ConfigPropertySetEntry.Type.forPropertySetType(-7));
        assertNull(ConfigPropertySetEntry.Type.forPropertySetType(7));
    }

    private void checkString(EntryFactory<String> factory, ConfigPropertySetEntry.Type type)
    {
        final String name = "name";
        String value = "value";

        ConfigPropertySetEntry entry = factory.create(name, value);
        assertStringEntry(entry, name, value, type);

        value = null;
        entry = factory.create(name, value);
        assertStringEntry(entry, name, value, type);

        value = "0";
        entry = factory.create(name, value);
        assertStringEntry(entry, name, value, type);

        value = "1";
        entry = factory.create(name, value);
        assertStringEntry(entry, name, value, type);

        value = String.valueOf(Integer.MAX_VALUE);
        entry = factory.create(name, value);
        assertStringEntry(entry, name, value, type);

        value = String.valueOf(Integer.MIN_VALUE);
        entry = factory.create(name, value);
        assertStringEntry(entry, name, value, type);

        value = String.valueOf(Integer.MAX_VALUE + 1L);
        entry = factory.create(name, value);
        assertStringEntry(entry, name, value, type);

        value = String.valueOf(Integer.MIN_VALUE - 1L);
        entry = factory.create(name, value);
        assertStringEntry(entry, name, value, type);

        value = String.valueOf(Long.MAX_VALUE );
        entry = factory.create(name, value);
        assertStringEntry(entry, name, value, type);

        value = String.valueOf(Long.MIN_VALUE);
        entry = factory.create(name, value);
        assertStringEntry(entry, name, value, type);

        BigInteger bigInt = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE);
        value = bigInt.toString();
        entry = factory.create(name, value);
        assertStringEntry(entry, name, value, type);

        bigInt = BigInteger.valueOf(Long.MIN_VALUE).subtract(BigInteger.ONE);
        value = bigInt.toString();
        entry = factory.create(name, value);
        assertStringEntry(entry, name, value, type);

    }

    private void assertStringEntry(final ConfigPropertySetEntry entry, final String name, final String textVal,
            final ConfigPropertySetEntry.Type type)
    {
        assertEntry(entry, name, textVal, parseBoolean(textVal), parseLong(textVal), parseInt(textVal), textVal, type);
    }

    private void assertIntegerEntry(final ConfigPropertySetEntry entry, final String name, final Integer intVal)
    {
        final Boolean boolVal;
        final String strVal;
        final Long longVal;

        if (intVal != null)
        {
            boolVal = intVal != 0;
            strVal = intVal.toString();
            longVal = intVal.longValue();
        }
        else
        {
            boolVal = false;
            strVal = null;
            longVal = null;
        }

        assertEntry(entry, name, strVal, boolVal, longVal, intVal, intVal, ConfigPropertySetEntry.Type.INTEGER);
    }

    private void assertLongEntry(final ConfigPropertySetEntry entry, final String name, final Long longVal)
    {
        final Boolean boolVal;
        final String strVal;
        final Integer intVal;

        if (longVal != null)
        {
            boolVal = longVal != 0;
            strVal = longVal.toString();

            final int tmpVal = longVal.intValue();
            intVal = tmpVal == longVal ? tmpVal : null;
        }
        else
        {
            boolVal = false;
            strVal = null;
            intVal = null;
        }

        assertEntry(entry, name, strVal, boolVal, longVal, intVal, longVal, ConfigPropertySetEntry.Type.LONG);
    }

    private void assertBooleanEntry(final ConfigPropertySetEntry entry, final String name, final Boolean boolVal)
    {
        final Long longVal;
        final String strVal;
        final Integer intVal;

        if (boolVal != null)
        {
            strVal = boolVal.toString();
            longVal = boolVal ? 1L : 0L;
            intVal = boolVal ? 1 : 0;
        }
        else
        {
            longVal = null;
            strVal = null;
            intVal = null;
        }

        assertEntry(entry, name, strVal, boolVal, longVal, intVal, boolVal, ConfigPropertySetEntry.Type.BOOLEAN);
    }

    private void assertEntry(final ConfigPropertySetEntry entry, final String name, final String textVal,
            final Boolean boolVal, final Long longValue, final Integer intVal, final Object objVal,
            final ConfigPropertySetEntry.Type type)
    {
        assertEquals(type, entry.getPropertyType());
        assertEquals(name, entry.getPropertyName());
        assertEquals(textVal, entry.asString());
        assertEquals(intVal, entry.asInteger());
        assertEquals(longValue, entry.asLong());
        assertEquals(objVal, entry.asObject());
        assertEquals(boolVal, entry.asBoolean());
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

    private interface EntryFactory<T>
    {
        ConfigPropertySetEntry create(String name, T value);
    }
}
