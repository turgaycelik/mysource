package com.atlassian.jira.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @since v4.4
 */
public class TestLimitedOutputStream
{
    @Test
    public void testWriting() throws IOException
    {
        final int maxLength = 1024;

        ByteArrayOutputStream arrayStream = new ByteArrayOutputStream();
        LimitedOutputStream stream = new LimitedOutputStream(arrayStream, maxLength);

        assertEquals(maxLength, stream.getMaxLength());
        assertEquals(0, stream.getCurrentLength());

        stream.write(0);

        byte[] expectedArray = { 0 };
        assertArrayEquals(expectedArray, arrayStream.toByteArray());
        assertEquals(maxLength, stream.getMaxLength());
        assertEquals(1, stream.getCurrentLength());

        byte[] bytes = randomBytes(5, 29238929);
        stream.write(bytes);

        expectedArray = concat(expectedArray, bytes);
        assertEquals(maxLength, stream.getMaxLength());
        assertEquals(6, stream.getCurrentLength());
        assertArrayEquals(expectedArray, arrayStream.toByteArray());

        bytes = randomBytes(5, 23718267);
        stream.write(bytes, 0, 5);
        expectedArray = concat(expectedArray, bytes);

        assertArrayEquals(expectedArray, arrayStream.toByteArray());
        assertEquals(maxLength, stream.getMaxLength());
        assertEquals(11, stream.getCurrentLength());

        bytes = randomBytes(1024, 387429738);

        stream.write(bytes, 5, 1013);
        expectedArray = concat(expectedArray, sub(bytes, 5, 1013));

        assertArrayEquals(expectedArray, arrayStream.toByteArray());
        assertEquals(maxLength, stream.getMaxLength());
        assertEquals(maxLength, stream.getCurrentLength());
    }

    @Test
    public void tooBigByte() throws IOException
    {
        final int maxLength = 1;

        ByteArrayOutputStream arrayStream = new ByteArrayOutputStream();
        LimitedOutputStream stream = new LimitedOutputStream(arrayStream, maxLength);

        assertEquals(maxLength, stream.getMaxLength());
        assertEquals(0, stream.getCurrentLength());
        assertArrayEquals(new byte[]{}, arrayStream.toByteArray());

        stream.write(0);

        assertEquals(maxLength, stream.getMaxLength());
        assertEquals(1, stream.getCurrentLength());
        assertArrayEquals(new byte[]{0}, arrayStream.toByteArray());

        try
        {
            stream.write(1);
            fail("Expecting an exception to occur.");
        }
        catch (LimitedOutputStream.TooBigIOException e)
        {
            assertEquals(maxLength, e.getMaxSize());
            assertEquals(1, e.getCurrentSize());
            assertEquals(1, e.getWriteLength());
            assertEquals(2, e.getNextSize());
            assertArrayEquals(new byte[]{0}, arrayStream.toByteArray());            
        }
    }

    @Test
    public void tooBigByteArray() throws IOException
    {
        final int maxLength = 5;

        ByteArrayOutputStream arrayStream = new ByteArrayOutputStream();
        LimitedOutputStream stream = new LimitedOutputStream(arrayStream, maxLength);

        byte[] data = randomBytes(3, 28747848348484L);
        byte[] expected = concat(data);

        stream.write(data);

        assertEquals(maxLength, stream.getMaxLength());
        assertEquals(3, stream.getCurrentLength());
        assertArrayEquals(expected, arrayStream.toByteArray());

        data = randomBytes(6, 48754775994L);

        try
        {
            stream.write(data);
            fail("Expecting an exception to occur.");
        }
        catch (LimitedOutputStream.TooBigIOException e)
        {
            assertEquals(maxLength, e.getMaxSize());
            assertEquals(3, e.getCurrentSize());
            assertEquals(6, e.getWriteLength());
            assertEquals(9, e.getNextSize());
            assertArrayEquals(expected, arrayStream.toByteArray());
        }

        try
        {
            stream.write(data, 1, 3);
            fail("Expecting an exception to occur.");
        }
        catch (LimitedOutputStream.TooBigIOException e)
        {
            assertEquals(maxLength, e.getMaxSize());
            assertEquals(3, e.getCurrentSize());
            assertEquals(3, e.getWriteLength());
            assertEquals(6, e.getNextSize());
            assertArrayEquals(expected, arrayStream.toByteArray());
        }
    }

    private byte[] randomBytes(int len, long seed)
    {
        final Random random = new Random(seed);
        final byte[] data = new byte[len];
        random.nextBytes(data);

        return data;
    }

    private byte[] sub(byte[] array, int start, int len)
    {
        byte[] data = new byte[len];
        System.arraycopy(array, start, data, 0, len);
        return data;
    }

    private byte[] concat(byte[] ... arrays)
    {
        int len = 0;
        for (byte[] array : arrays)
        {
            len += array.length;
        }

        byte[] data = new byte[len];
        int pos = 0;
        for (byte[] array : arrays)
        {
            System.arraycopy(array, 0, data, pos, array.length);
            pos += array.length;
        }
        return data;
    }
}
