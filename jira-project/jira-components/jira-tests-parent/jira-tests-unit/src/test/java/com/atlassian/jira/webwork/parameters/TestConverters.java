package com.atlassian.jira.webwork.parameters;

import java.util.Arrays;

import org.junit.Test;

import webwork.util.editor.PropertyEditorException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for the webwork1 parameters converters
 * <p/>
 * We dont go into super exhaustive tests for all types of input because we are not testing Long.valueOf() but rather
 * that the Java primitive code gets called sensibly.  That said we do do some tests.
 * <p/>
 * Also since the array classes use their underlying counterpart converters, we dont re-test all the different input
 * scenarios.
 *
 * @since v3.13.2
 */
public class TestConverters
{
    @Test
    public void testBoolean()
    {
        Class classUnderTest = Boolean.class;
        ParameterConverter converter = KnownParameterConverters.getConverter(classUnderTest);

        // non primitive allows for empty input
        assertEquals(null, converter.convertParameter(new String[] { null }, classUnderTest));
        assertEquals(null, converter.convertParameter(new String[] { "" }, classUnderTest));

        assertBooleanInput(classUnderTest, converter);

        classUnderTest = Boolean.TYPE;
        converter = KnownParameterConverters.getConverter(classUnderTest);

        // primitive checks dont allow for empty input
        assertConversionException(classUnderTest, converter, (String) null);
        assertConversionException(classUnderTest, converter, "");

        assertBooleanInput(classUnderTest, converter);

    }

    private void assertBooleanInput(final Class classUnderTest, final ParameterConverter converter)
    {
        assertEquals(Boolean.FALSE, converter.convertParameter(new String[] { "123" }, classUnderTest));
        assertEquals(Boolean.FALSE, converter.convertParameter(new String[] { "123x" }, classUnderTest));
        assertEquals(Boolean.FALSE, converter.convertParameter(new String[] { "xyz" }, classUnderTest));
        assertEquals(Boolean.FALSE, converter.convertParameter(new String[] { " \t\n" }, classUnderTest));
        assertEquals(Boolean.FALSE, converter.convertParameter(new String[] { "FALSE" }, classUnderTest));
        assertEquals(Boolean.FALSE, converter.convertParameter(new String[] { "false" }, classUnderTest));
        assertEquals(Boolean.FALSE, converter.convertParameter(new String[] { "fAlSe" }, classUnderTest));
        assertEquals(Boolean.TRUE, converter.convertParameter(new String[] { "TRUE" }, classUnderTest));
        assertEquals(Boolean.TRUE, converter.convertParameter(new String[] { "true" }, classUnderTest));
    }

    @Test
    public void testCharacter()
    {
        Class classUnderTest = Character.class;
        ParameterConverter converter = KnownParameterConverters.getConverter(classUnderTest);

        // non primitive allows for empty input
        assertEquals(null, converter.convertParameter(new String[] { null }, classUnderTest));
        assertEquals(null, converter.convertParameter(new String[] { "" }, classUnderTest));

        assertCharacterInput(classUnderTest, converter);

        classUnderTest = Character.TYPE;
        converter = KnownParameterConverters.getConverter(classUnderTest);

        // primitive checks dont allow for empty input
        assertConversionException(classUnderTest, converter, (String) null);
        assertConversionException(classUnderTest, converter, "");

        assertCharacterInput(classUnderTest, converter);
    }

    private void assertCharacterInput(final Class classUnderTest, final ParameterConverter converter)
    {
        assertEquals(new Character('a'), converter.convertParameter(new String[] { "abc" }, classUnderTest));
        assertEquals(new Character('a'), converter.convertParameter(new String[] { "a" }, classUnderTest));
        assertEquals(new Character('1'), converter.convertParameter(new String[] { "1" }, classUnderTest));
    }

    @Test
    public void testByte()
    {
        Class classUnderTest = Byte.class;
        ParameterConverter converter = KnownParameterConverters.getConverter(classUnderTest);

        // non primitive allows for empty input
        assertEquals(null, converter.convertParameter(new String[] { null }, classUnderTest));
        assertEquals(null, converter.convertParameter(new String[] { "" }, classUnderTest));

        assertByteInput(classUnderTest, converter);

        classUnderTest = Byte.TYPE;
        converter = KnownParameterConverters.getConverter(classUnderTest);

        // primitive checks dont allow for empty input
        assertConversionException(classUnderTest, converter, (String) null);
        assertConversionException(classUnderTest, converter, "");

        assertByteInput(classUnderTest, converter);
    }


    private void assertByteInput(final Class classUnderTest, final ParameterConverter converter)
    {
        assertBasicNumericInput(classUnderTest, converter);
        // too big for this type
        assertConversionException(classUnderTest, converter, "9999999999999999");

        assertEquals(new Byte("123"), converter.convertParameter(new String[] { "123" }, classUnderTest));
    }

    @Test
    public void testShort()
    {
        Class classUnderTest = Short.class;
        ParameterConverter converter = KnownParameterConverters.getConverter(classUnderTest);

        // non primitive allows for empty input
        assertEquals(null, converter.convertParameter(new String[] { null }, classUnderTest));
        assertEquals(null, converter.convertParameter(new String[] { "" }, classUnderTest));

        assertShortInput(classUnderTest, converter);

        classUnderTest = Short.TYPE;
        converter = KnownParameterConverters.getConverter(classUnderTest);

        // primitive checks dont allow for empty input
        assertConversionException(classUnderTest, converter, (String) null);
        assertConversionException(classUnderTest, converter, "");

        assertShortInput(classUnderTest, converter);
    }

    private void assertShortInput(final Class classUnderTest, final ParameterConverter converter)
    {
        assertBasicNumericInput(classUnderTest, converter);
        // too big for this type
        assertConversionException(classUnderTest, converter, "9999999999999999");

        assertEquals(new Short("123"), converter.convertParameter(new String[] { "123" }, classUnderTest));
    }

    @Test
    public void testInteger()
    {
        Class classUnderTest = Integer.class;
        ParameterConverter converter = KnownParameterConverters.getConverter(classUnderTest);

        // non primitive allows for empty input
        assertEquals(null, converter.convertParameter(new String[] { null }, classUnderTest));
        assertEquals(null, converter.convertParameter(new String[] { "" }, classUnderTest));

        assertIntegerInput(classUnderTest, converter);

        classUnderTest = Integer.TYPE;
        converter = KnownParameterConverters.getConverter(classUnderTest);

        // primitive checks dont allow for empty input
        assertConversionException(classUnderTest, converter, (String) null);
        assertConversionException(classUnderTest, converter, "");

        assertIntegerInput(classUnderTest, converter);
    }

    private void assertIntegerInput(final Class classUnderTest, final ParameterConverter converter)
    {
        assertBasicNumericInput(classUnderTest, converter);
        assertConversionException(classUnderTest, converter, "0xF2FF");
        // too big for this type
        assertConversionException(classUnderTest, converter, "2147483648");
        assertConversionException(classUnderTest, converter, "9999999999999999");

        assertEquals(new Integer(123), converter.convertParameter(new String[] { "123" }, classUnderTest));
        assertEquals(new Integer(2147483647), converter.convertParameter(new String[] { "2147483647" }, classUnderTest));
    }

    @Test
    public void testLong()
    {
        Class classUnderTest = Long.class;
        ParameterConverter converter = KnownParameterConverters.getConverter(classUnderTest);

        // non primitive allows for empty input
        assertEquals(null, converter.convertParameter(new String[] { null }, classUnderTest));
        assertEquals(null, converter.convertParameter(new String[] { "" }, classUnderTest));

        assertLongInput(classUnderTest, converter);

        classUnderTest = Long.TYPE;
        converter = KnownParameterConverters.getConverter(classUnderTest);

        // primitive checks dont allow for empty input
        assertConversionException(classUnderTest, converter, (String) null);
        assertConversionException(classUnderTest, converter, "");

        assertLongInput(classUnderTest, converter);
    }

    private void assertLongInput(final Class classUnderTest, final ParameterConverter converter)
    {
        assertBasicNumericInput(classUnderTest, converter);
        assertConversionException(classUnderTest, converter, "0xF2FF");
        // too big for this type
        assertConversionException(classUnderTest, converter, "9223372036854775808");

        assertEquals(new Long("123"), converter.convertParameter(new String[] { "123" }, classUnderTest));
        assertEquals(new Long(9223372036854775807L), converter.convertParameter(new String[] { "9223372036854775807" }, classUnderTest));
    }

    @Test
    public void testFloat()
    {
        Class classUnderTest = Float.class;
        ParameterConverter converter = KnownParameterConverters.getConverter(classUnderTest);

        // non primitive allows for empty input
        assertEquals(null, converter.convertParameter(new String[] { null }, classUnderTest));
        assertEquals(null, converter.convertParameter(new String[] { "" }, classUnderTest));

        assertFloatInput(classUnderTest, converter);

        classUnderTest = Float.TYPE;
        converter = KnownParameterConverters.getConverter(classUnderTest);

        // primitive checks dont allow for empty input
        assertConversionException(classUnderTest, converter, (String) null);
        assertConversionException(classUnderTest, converter, "");

        assertFloatInput(classUnderTest, converter);
    }

    private void assertFloatInput(final Class classUnderTest, final ParameterConverter converter)
    {
        assertBasicNumericInput(classUnderTest, converter);
        assertConversionException(classUnderTest, converter, "0xF2FF");

        assertEquals(new Float("123"), converter.convertParameter(new String[] { "123" }, classUnderTest));
        assertEquals(new Float("123.456"), converter.convertParameter(new String[] { "123.456" }, classUnderTest));
        assertEquals(new Float("9223372036854775808"), converter.convertParameter(new String[] { "9223372036854775808" }, classUnderTest));
        assertEquals(new Float(3.4028235e+38f), converter.convertParameter(new String[] { "3.4028235e+38" }, classUnderTest));
        assertEquals(new Float(Float.POSITIVE_INFINITY), converter.convertParameter(new String[] { "3.4028235e+39" }, classUnderTest));
    }

    @Test
    public void testDouble()
    {
        Class classUnderTest = Double.class;
        ParameterConverter converter = KnownParameterConverters.getConverter(classUnderTest);

        // non primitive allows for empty input
        assertEquals(null, converter.convertParameter(new String[] { null }, classUnderTest));
        assertEquals(null, converter.convertParameter(new String[] { "" }, classUnderTest));

        assertDoubleInput(classUnderTest, converter);

        classUnderTest = Double.TYPE;
        converter = KnownParameterConverters.getConverter(classUnderTest);

        // primitive checks dont allow for empty input
        assertConversionException(classUnderTest, converter, (String) null);
        assertConversionException(classUnderTest, converter, "");

        assertDoubleInput(classUnderTest, converter);
    }

    private void assertDoubleInput(final Class classUnderTest, final ParameterConverter converter)
    {
        assertBasicNumericInput(classUnderTest, converter);
        assertConversionException(classUnderTest, converter, "0xF2FF");


        assertEquals(new Double("123"), converter.convertParameter(new String[] { "123" }, classUnderTest));
        assertEquals(new Double("123.456"), converter.convertParameter(new String[] { "123.456" }, classUnderTest));
        assertEquals(new Double("9223372036854775808"), converter.convertParameter(new String[] { "9223372036854775808" }, classUnderTest));
        assertEquals(new Double(1.7976931348623157e+308), converter.convertParameter(new String[] { "1.7976931348623157e+308" }, classUnderTest));
        assertEquals(new Double(Double.POSITIVE_INFINITY), converter.convertParameter(new String[] { "1.7976931348623157e+309" }, classUnderTest));
    }


    @Test
    public void testBooleanArray()
    {
        Class classUnderTest = Boolean[].class;
        ParameterConverter converter = KnownParameterConverters.getConverter(classUnderTest);

        // non primitive allows for empty input
        assertArraysEquals(new Boolean[] { null }, converter.convertParameter(new String[] { null }, classUnderTest));
        assertArraysEquals(new Boolean[] { null }, converter.convertParameter(new String[] { "" }, classUnderTest));

        assertArraysEquals(new Boolean[0], converter.convertParameter(new String[] { }, classUnderTest));
        assertArraysEquals(new Boolean[] { Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, Boolean.TRUE }, converter.convertParameter(new String[] { "123", "abc", "f", "t", "true" }, classUnderTest));

        // primitive array
        classUnderTest = boolean[].class;
        converter = KnownParameterConverters.getConverter(classUnderTest);
        assertConversionException(classUnderTest, converter, (String) null);
        assertConversionException(classUnderTest, converter, "");
        assertConversionException(classUnderTest, converter, new String[] { "true", "false", "" });

        assertTrue(Arrays.equals(new boolean[0], (boolean[]) converter.convertParameter(new String[] { }, classUnderTest)));
        assertTrue(Arrays.equals(new boolean[] { false, false, false, false, true }, (boolean[]) converter.convertParameter(new String[] { "123", "abc", "f", "t", "true" }, classUnderTest)));
    }

    @Test
    public void testByteArray()
    {
        Class classUnderTest = Byte[].class;
        ParameterConverter converter = KnownParameterConverters.getConverter(classUnderTest);

        // non primitive allows for empty input
        assertArraysEquals(new Byte[] { null }, converter.convertParameter(new String[] { null }, classUnderTest));
        assertArraysEquals(new Byte[] { null }, converter.convertParameter(new String[] { "" }, classUnderTest));

        assertArraysEquals(new Byte[0], converter.convertParameter(new String[] { }, classUnderTest));
        assertArraysEquals(new Byte[] { new Byte((byte) 123), new Byte((byte) 45) }, converter.convertParameter(new String[] { "123", "45" }, classUnderTest));

        // primitive array
        classUnderTest = byte[].class;
        converter = KnownParameterConverters.getConverter(classUnderTest);
        assertConversionException(classUnderTest, converter, (String) null);
        assertConversionException(classUnderTest, converter, "");
        assertConversionException(classUnderTest, converter, new String[] { "123", "456", "" });

        assertTrue(Arrays.equals(new byte[0], (byte[]) converter.convertParameter(new String[] { }, classUnderTest)));
        assertTrue(Arrays.equals(new byte[] { 123, 45 }, (byte[]) converter.convertParameter(new String[] { "123", "45" }, classUnderTest)));
    }

    @Test
    public void testCharacterArray()
    {
        Class classUnderTest = Character[].class;
        ParameterConverter converter = KnownParameterConverters.getConverter(classUnderTest);

        // non primitive allows for empty input
        assertArraysEquals(new Character[] { null }, converter.convertParameter(new String[] { null }, classUnderTest));
        assertArraysEquals(new Character[] { null }, converter.convertParameter(new String[] { "" }, classUnderTest));

        assertArraysEquals(new Character[0], converter.convertParameter(new String[] { }, classUnderTest));
        assertArraysEquals(new Character[] { new Character('a'), new Character('1') }, converter.convertParameter(new String[] { "a", "1" }, classUnderTest));

        // primitive array
        classUnderTest = char[].class;
        converter = KnownParameterConverters.getConverter(classUnderTest);
        assertConversionException(classUnderTest, converter, (String) null);
        assertConversionException(classUnderTest, converter, "");
        assertConversionException(classUnderTest, converter, new String[] { "a", "1", "" });

        assertTrue(Arrays.equals(new char[0], (char[]) converter.convertParameter(new String[] { }, classUnderTest)));
        assertTrue(Arrays.equals(new char[] { 'a', '1' }, (char[]) converter.convertParameter(new String[] { "a", "1" }, classUnderTest)));
    }

    @Test
    public void testShortArray()
    {
        Class classUnderTest = Short[].class;
        ParameterConverter converter = KnownParameterConverters.getConverter(classUnderTest);

        // non primitive allows for empty input
        assertArraysEquals(new Short[] { null }, converter.convertParameter(new String[] { null }, classUnderTest));
        assertArraysEquals(new Short[] { null }, converter.convertParameter(new String[] { "" }, classUnderTest));

        assertArraysEquals(new Short[0], converter.convertParameter(new String[] { }, classUnderTest));
        assertArraysEquals(new Short[] { new Short((short) 123), new Short((short) 45) }, converter.convertParameter(new String[] { "123", "45" }, classUnderTest));

        // primitive array
        classUnderTest = short[].class;
        converter = KnownParameterConverters.getConverter(classUnderTest);
        assertConversionException(classUnderTest, converter, (String) null);
        assertConversionException(classUnderTest, converter, "");
        assertConversionException(classUnderTest, converter, new String[] { "123", "456", "" });

        assertTrue(Arrays.equals(new short[0], (short[]) converter.convertParameter(new String[] { }, classUnderTest)));
        assertTrue(Arrays.equals(new short[] { 123, 45 }, (short[]) converter.convertParameter(new String[] { "123", "45" }, classUnderTest)));
    }

    @Test
    public void testIntegerArray()
    {
        Class classUnderTest = Integer[].class;
        ParameterConverter converter = KnownParameterConverters.getConverter(classUnderTest);

        // non primitive allows for empty input
        assertArraysEquals(new Integer[] { null }, converter.convertParameter(new String[] { null }, classUnderTest));
        assertArraysEquals(new Integer[] { null }, converter.convertParameter(new String[] { "" }, classUnderTest));

        assertArraysEquals(new Integer[0], converter.convertParameter(new String[] { }, classUnderTest));
        assertArraysEquals(new Integer[] { new Integer(123), new Integer(45) }, converter.convertParameter(new String[] { "123", "45" }, classUnderTest));

        // primitive array
        classUnderTest = int[].class;
        converter = KnownParameterConverters.getConverter(classUnderTest);
        assertConversionException(classUnderTest, converter, (String) null);
        assertConversionException(classUnderTest, converter, "");
        assertConversionException(classUnderTest, converter, new String[] { "123", "456", "" });

        assertTrue(Arrays.equals(new int[0], (int[]) converter.convertParameter(new String[] { }, classUnderTest)));
        assertTrue(Arrays.equals(new int[] { 123, 45 }, (int[]) converter.convertParameter(new String[] { "123", "45" }, classUnderTest)));
    }

    @Test
    public void testLongArray()
    {
        Class classUnderTest = Long[].class;
        ParameterConverter converter = KnownParameterConverters.getConverter(classUnderTest);

        // non primitive allows for empty input
        assertArraysEquals(new Long[] { null }, converter.convertParameter(new String[] { null }, classUnderTest));
        assertArraysEquals(new Long[] { null }, converter.convertParameter(new String[] { "" }, classUnderTest));

        assertArraysEquals(new Long[0], converter.convertParameter(new String[] { }, classUnderTest));
        assertArraysEquals(new Long[] { new Long(123), new Long(45) }, converter.convertParameter(new String[] { "123", "45" }, classUnderTest));

        // primitive array
        classUnderTest = long[].class;
        converter = KnownParameterConverters.getConverter(classUnderTest);
        assertConversionException(classUnderTest, converter, (String) null);
        assertConversionException(classUnderTest, converter, "");
        assertConversionException(classUnderTest, converter, new String[] { "123", "456", "" });

        assertTrue(Arrays.equals(new long[0], (long[]) converter.convertParameter(new String[] { }, classUnderTest)));
        assertTrue(Arrays.equals(new long[] { 123, 45 }, (long[]) converter.convertParameter(new String[] { "123", "45" }, classUnderTest)));
    }

    @Test
    public void testFloatArray()
    {
        Class classUnderTest = Float[].class;
        ParameterConverter converter = KnownParameterConverters.getConverter(classUnderTest);

        // non primitive allows for empty input
        assertArraysEquals(new Float[] { null }, converter.convertParameter(new String[] { null }, classUnderTest));
        assertArraysEquals(new Float[] { null }, converter.convertParameter(new String[] { "" }, classUnderTest));

        assertArraysEquals(new Float[0], converter.convertParameter(new String[] { }, classUnderTest));
        assertArraysEquals(new Float[] { new Float(123), new Float(45e12f) }, converter.convertParameter(new String[] { "123", "45e12" }, classUnderTest));

        // primitive array
        classUnderTest = float[].class;
        converter = KnownParameterConverters.getConverter(classUnderTest);
        assertConversionException(classUnderTest, converter, (String) null);
        assertConversionException(classUnderTest, converter, "");
        assertConversionException(classUnderTest, converter, new String[] { "123", "456e12", "" });

        assertTrue(Arrays.equals(new float[0], (float[]) converter.convertParameter(new String[] { }, classUnderTest)));
        assertTrue(Arrays.equals(new float[] { 123, 45e12f }, (float[]) converter.convertParameter(new String[] { "123", "45e12" }, classUnderTest)));
    }

    @Test
    public void testDoubleArray()
    {
        Class classUnderTest = Double[].class;
        ParameterConverter converter = KnownParameterConverters.getConverter(classUnderTest);

        // non primitive allows for empty input
        assertArraysEquals(new Double[] { null }, converter.convertParameter(new String[] { null }, classUnderTest));
        assertArraysEquals(new Double[] { null }, converter.convertParameter(new String[] { "" }, classUnderTest));

        assertArraysEquals(new Double[0], converter.convertParameter(new String[] { }, classUnderTest));
        assertArraysEquals(new Double[] { new Double(123), new Double(45e12) }, converter.convertParameter(new String[] { "123", "45e12" }, classUnderTest));

        // primitive array
        classUnderTest = double[].class;
        converter = KnownParameterConverters.getConverter(classUnderTest);
        assertConversionException(classUnderTest, converter, (String) null);
        assertConversionException(classUnderTest, converter, "");
        assertConversionException(classUnderTest, converter, new String[] { "123", "456e12", "" });

        assertTrue(Arrays.equals(new double[0], (double[]) converter.convertParameter(new String[] { }, classUnderTest)));
        assertTrue(Arrays.equals(new double[] { 123, 45e12 }, (double[]) converter.convertParameter(new String[] { "123", "45e12" }, classUnderTest)));
    }

    @Test
    public void testKnownParameterConverters()
    {
        assertNull(KnownParameterConverters.getConverter(KnownParameterConverters.class));
    }

    private void assertConversionException(final Class classUnderTest, final ParameterConverter converter, final String inputValue)
    {
        assertConversionException(classUnderTest, converter, new String[] { inputValue });
    }

    private void assertConversionException(final Class classUnderTest, final ParameterConverter converter, final String[] inputValues)
    {
        try
        {
            converter.convertParameter(inputValues, classUnderTest);
            fail("Should have thrown an exception for input :" + inputValues);
        }
        catch (PropertyEditorException ignored)
        {
        }
    }


    private void assertBasicNumericInput(final Class classUnderTest, final ParameterConverter converter)
    {
        assertConversionException(classUnderTest, converter, "abc");
        assertConversionException(classUnderTest, converter, "abc123");
        assertConversionException(classUnderTest, converter, "123abc");
        assertConversionException(classUnderTest, converter, "1abc2");
        assertConversionException(classUnderTest, converter, "0xFF");
    }

    private void assertArraysEquals(final Object expected[], final Object actual)
    {
        if (expected == actual)
        {
            return;
        }
        if (!actual.getClass().isArray())
        {
            fail("is not an array");
        }
        Object[] actualArr = (Object[]) actual;
        if (actualArr.length != expected.length)
        {
            fail("not the same length expected : " + expected.length + " actual : " + actualArr.length);
        }
        for (int i = 0; i < expected.length; i++)
        {
            if (expected[i] != actualArr[i])
            {
                if (!expected[i].equals(actualArr[i]))
                {
                    fail("Unequal values in index : " + i + " expected : " + expected[i] + " actual : " + actualArr[i]);
                }
            }

        }
    }

}
