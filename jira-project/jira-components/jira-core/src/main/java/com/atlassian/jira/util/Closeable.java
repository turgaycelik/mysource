package com.atlassian.jira.util;

import java.io.IOException;

/**
 * A <tt>Closeable</tt> is a source or destination of data that can be closed. The close method is invoked to release resources that the object is
 * holding (such as open files).
 * <p/>
 * Straight copy of the java.util.Closeable interface except it doesn't throw {@link IOException}
 *
 * @since v3.13
 */
public interface Closeable extends java.io.Closeable
{
    /**
     * Simple {@link Consumer} implementation that closes all elements.
     */
    public static final Consumer<Closeable> CLOSE = new Consumer<Closeable>()
    {
        public void consume(final Closeable element)
        {
            element.close();
        }
    };

    /**
     * Closes this stream and releases any system resources associated with it. If the stream is already closed then invoking this method has no
     * effect.
     *
     * @throws RuntimeIOException if an I/O error occurs
     */
    void close();
}
