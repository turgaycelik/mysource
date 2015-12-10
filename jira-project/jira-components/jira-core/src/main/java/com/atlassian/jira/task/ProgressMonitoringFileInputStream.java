package com.atlassian.jira.task;

import com.atlassian.core.util.FileSize;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;

/**
 * A FileInputStream that can log progress when the inputstream is being read. The passed in task progress sink should
 * have been provided with the overall file size to be read already to provide meaningful updates since the underlying
 * inputstream implementation may not return the overall size of the data to be read reliably.
 *
 * @since v4.4
 */
public class ProgressMonitoringFileInputStream extends FilterInputStream
{

    private long nread = 0;
    private final String subTask;
    private final String message;
    private final TaskProgressSink taskProgressSink;

    public ProgressMonitoringFileInputStream(InputStream in, TaskProgressSink taskProgressSink, String subTask, String message)
    {
        super(in);
        this.subTask = subTask;
        this.message = message;
        this.taskProgressSink = taskProgressSink;
    }

    @Override
    public int read() throws IOException
    {
        final int read = super.read();
        return recordProgress(read);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        final int read = super.read(b, off, len);
        return recordProgress(read);
    }

    @Override
    public int read(byte[] b) throws IOException
    {
        final int read = super.read(b);
        return recordProgress(read);
    }

    private int recordProgress(int read)
    {
        nread += read;
        taskProgressSink.makeProgress(nread, subTask, MessageFormat.format(message, FileSize.format(nread)));
        return read;
    }
}
