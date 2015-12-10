package com.atlassian.jira.web.monitor.dump;

import java.io.PrintStream;
import java.lang.management.ThreadInfo;

/**
 * Used for dumping the threads to a PrintStream.
 */
class StreamWriter implements ThreadInfoWriter
{
    /**
     * The PrintStream where the thread information will be dumped.
     */
    private final PrintStream stream;

    /**
     * Creates a new StreamDumper.
     *
     * @param stream a PrintStream where the thread info will be dumped
     */
    public StreamWriter(PrintStream stream)
    {
        this.stream = stream;
    }

    public void write(ThreadInfo[] threads)
    {
        for (ThreadInfo thread : threads)
        {
            stream.print(ThreadInfos.toString(thread));
        }
    }
}
