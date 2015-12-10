package com.atlassian.jira.util;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.annotation.Nonnull;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Tests the IOUtil class.
 *
 * @since v3.13
 */
public class TestIOUtil
{
    @Test
    public void testGetLeadingBytes() throws IOException
    {
        final byte[] inBytes = { 0x1, 0x2, 0x3 };
        ByteArrayInputStream is = new ByteArrayInputStream(inBytes);
        byte[] leading = IOUtil.getLeadingBytes(is, 5);
        assertTrue(leading.length == 3);
        assertTrue(Arrays.equals(inBytes, leading));


        TrickleInputStream tis = new TrickleInputStream(new ByteArrayInputStream(inBytes));
        leading = IOUtil.getLeadingBytes(tis, 5);
        assertTrue(leading.length == 3);
        assertTrue(Arrays.equals(inBytes, leading));

    }

    @Test
    public void testGetLeadingBytesLargerArray() throws IOException
    {
        final byte[] inBytes = { 0x1, 0x2, 0x3, 0x1, 0x2, 0x3, 0x1, 0x2, 0x3, 0x1, 0x2, 0x3, 0x1, 0x2, 0x3 };
        ByteArrayInputStream is = new ByteArrayInputStream(inBytes);
        byte[] leading = IOUtil.getLeadingBytes(is, 6);
        assertTrue(leading.length == 6);
        assertTrue(Arrays.equals(new byte[] { 0x1, 0x2, 0x3, 0x1, 0x2, 0x3 }, leading));
    }

    @Test
    public void testEmpty() throws IOException
    {
        final byte[] inBytes = {};
        ByteArrayInputStream is = new ByteArrayInputStream(inBytes);
        byte[] leading = IOUtil.getLeadingBytes(is, 6);
        assertTrue(leading.length == 0);
        assertTrue(Arrays.equals(inBytes, leading));
    }

    private class TrickleInputStream extends FilterInputStream
    {

        /**
         * Creates a <code>FilterInputStream</code>
         * by assigning the  argument <code>in</code>
         * to the field <code>this.in</code> so as
         * to remember it for later use.
         *
         * @param in the underlying input stream, or <code>null</code> if
         *           this instance is to be created without an underlying stream.
         */
        protected TrickleInputStream(InputStream in)
        {
            super(in);
        }

        @Override
        public int read(@Nonnull byte b[]) throws IOException
        {
            return super.read(b, 0, 1); // always reads 1
        }

        @Override
        public int read(@Nonnull byte b[], int off, int len) throws IOException
        {
            return super.read(b, off, 1); // always reads 1
        }

        @Override
        public long skip(long n) throws IOException
        {
            return super.skip(1);
        }

        @Override
        public int available() throws IOException
        {
            return 1;
        }

        @Override
        public void mark(int readlimit)
        {
            throw new UnsupportedOperationException("no mark");
        }

        public boolean markSupported()
        {
            return false;
        }
    }
}
