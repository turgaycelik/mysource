package com.atlassian.jira.util;

import com.atlassian.jira.util.dbc.Assertions;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An {@link OutputStream} that throws an {@link TooBigIOException} if more than the configured number of bytes are
 * ever written to the output stream.
 *
 * @since v4.4
 */
public class LimitedOutputStream extends OutputStream
{
    private final OutputStream delegate;
    private final long maxLength;
    private long currentLength = 0;

    public LimitedOutputStream(OutputStream delegate, long maxLength)
    {
        this.delegate = Assertions.notNull("delegate", delegate);
        this.maxLength = maxLength;

        if (this.maxLength <= 0)
        {
            throw new IllegalArgumentException("'maxLength' must be > 0.");
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
        checkLength(len);
        delegate.write(b, off, len);
    }

    @Override
    public void write(int b) throws IOException
    {
        checkLength(1);
        delegate.write(b);
    }

    public long getMaxLength()
    {
        return maxLength;
    }

    public long getCurrentLength()
    {
        return currentLength;
    }

    private void checkLength(int len) throws IOException
    {
        long nextLength = currentLength + len;
        if (nextLength > maxLength)
        {
            throw new TooBigIOException(maxLength, currentLength, len);
        }
        else
        {
            currentLength = nextLength;
        }
    }

    @Override
    public void flush() throws IOException
    {
        delegate.flush();
    }

    @Override
    public void close() throws IOException
    {
        delegate.close();
    }

    public static class TooBigIOException extends IOException
    {
        private final long maxSize;
        private final long currentSize;
        private final long writeLength;

        private TooBigIOException(long maxSize, long currentSize, long writeLength)
        {
            this.maxSize = maxSize;
            this.currentSize = currentSize;
            this.writeLength = writeLength;
        }

        public long getMaxSize()
        {
            return maxSize;
        }

        public long getCurrentSize()
        {
            return currentSize;
        }

        public long getNextSize()
        {
            return currentSize + writeLength;
        }

        public long getWriteLength()
        {
            return writeLength;
        }
    }
}
