package com.atlassian.jira.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.Nonnull;

/**
     * Copies an {@link java.io.InputStream} to the configured {@link java.io.OutputStream}. If there is an
 * IOException during the copy, this will be thrown from the consume method inside a RuntimeException, but either
 * way the
 */
public class StreamCopyingConsumer implements Consumer<InputStream>
{
    private final OutputStream outputStream;
    private int bufferSize;

    /**
     * Copies the consumed {@link java.io.InputStream} into the given OutputStream (without closing anything).
     * If there is a problem copying, the IOException will be thrown wrapped in a RuntimeException.
     * @param outputStream the OutputStream to copy to.
     * @param bufferSize the desired number of bytes in the copy buffer.
     */
    public StreamCopyingConsumer(final OutputStream outputStream, int bufferSize)
    {
        this.outputStream = outputStream;
        this.bufferSize = bufferSize;
    }

    public void consume(@Nonnull final InputStream inputStream)
    {
        try
        {
            IOUtil.copy(inputStream, outputStream, bufferSize);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
