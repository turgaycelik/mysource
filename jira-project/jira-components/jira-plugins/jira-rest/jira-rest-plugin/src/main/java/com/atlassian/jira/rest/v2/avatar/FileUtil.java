package com.atlassian.jira.rest.v2.avatar;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.atlassian.jira.util.LimitedOutputStream;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

/**
 * Created by dszuksztul on 20/03/14.
 */
@Component
public class FileUtil
{
    public static class StreamSizeMismatchException extends IOException {
        private final long expectedSize;
        private final long actualSize;

        public StreamSizeMismatchException(final long expectedSize, final long actualSize) {
            this.expectedSize = expectedSize;
            this.actualSize = actualSize;
        }

        public long getExpectedSize()
        {
            return expectedSize;
        }

        public long getActualSize()
        {
            return actualSize;
        }
    }

    /**
     * Creates temp file frm given stream with expecteation that this stream will have given siae.
     * @param stream stream to copy data from
     * @param size expected number of bytes from stream
     * @param filenamePrefix
     * @return temp file created from stream contents
     * @throws IOException some io
     * @throws LimitedOutputStream.TooBigIOException when size of stream is bigger than expected
     * @throws StreamSizeMismatchException when size of stream is smaller than expected
     */
    public File createTempFileFromBoundedStream(final InputStream stream, final long size, final String filenamePrefix)
            throws LimitedOutputStream.TooBigIOException, StreamSizeMismatchException, IOException
    {
        final File tempFile = createTemporaryFile(filenamePrefix, null);

        try
        {
            OutputStream fileOutput = new BufferedOutputStream(new FileOutputStream(tempFile));
            try
            {
                LimitedOutputStream limitedOutput = new LimitedOutputStream(new BufferedOutputStream(fileOutput), size);

                IOUtils.copy(stream, limitedOutput);
                limitedOutput.flush();

                if (limitedOutput.getCurrentLength() != size)
                {

                    throw new StreamSizeMismatchException( size, limitedOutput.getCurrentLength() );
                }
                return tempFile;
            }
            finally
            {
                IOUtils.closeQuietly(fileOutput);
            }
        }
        catch( IOException x ) {
            tempFile.delete();
            throw x;
        }
        catch( RuntimeException x ) {
            tempFile.delete();
            throw x;
        }
    }

    /**
     * Creates temp file frm given stream
     * @param stream stream to copy data from
     * @param filenamePrefix
     * @return temp file created from stream contents
     * @throws IOException some io
     */
    public File createTempFile(final InputStream stream, final String filenamePrefix)
            throws IOException
    {
        final File tempFile = createTemporaryFile(filenamePrefix, null);

        try
        {
            OutputStream fileOutput = new FileOutputStream(tempFile);
            try
            {
                IOUtils.copy(stream, fileOutput);
                return tempFile;
            }
            finally
            {
                IOUtils.closeQuietly(fileOutput);
            }
        }
        catch( IOException x ) {
            tempFile.delete();
            throw x;
        }
        catch( RuntimeException x ) {
            tempFile.delete();
            throw x;
        }
    }

    public File createTemporaryFile(String prefix, String suffix) throws IOException
    {
        final File tempFile = File.createTempFile(prefix, suffix);
        tempFile.deleteOnExit();

        return tempFile;
    }
}
