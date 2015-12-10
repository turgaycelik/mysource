package com.atlassian.jira.web.monitor.dump;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.lang.management.ThreadInfo;

/**
 * Used for dumping the threads to a File.
 */
class FileWriter implements ThreadInfoWriter
{
    /**
     * The File where the thread information will be dumped.
     */
    private final File file;

    /**
     * Creates a new FileDumper that dumps thread info to the given file.
     *
     * @param file a File, must already exist
     */
    public FileWriter(File file)
    {
        this.file = file;
    }

    public void write(ThreadInfo[] threads)
    {
        PrintWriter pw = null;
        try
        {
            pw = new PrintWriter(new FileOutputStream(file));
            for (ThreadInfo thread : threads)
            {
                pw.print(ThreadInfos.toString(thread));
                pw.print("\n");
            }
        }
        catch (FileNotFoundException e)
        {
            throw new RuntimeException(e); // won't happen, file already exists
        }
        finally
        {
            if (pw != null)
            {
                pw.close();
            }
        }
    }
}
