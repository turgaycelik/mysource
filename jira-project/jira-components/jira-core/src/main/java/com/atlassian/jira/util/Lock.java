package com.atlassian.jira.util;

import java.io.File;
import java.io.IOException;

/**
 * Used to lock resources using a file system file as a lock
 */
public class Lock
{
    File lockFile;
    public static final long LOOP_WAIT_TIME = 1000;


    public Lock(String fileName)
    {
        lockFile = new File(fileName);
    }

    public Lock(String directoryName, String fileName)
    {
        this.lockFile = new File(directoryName, fileName);
    }

    public boolean obtain() throws IOException
    {
        return lockFile.createNewFile();
    }

    /**
     * Try to obtain a lock, wait for timeout ms if necessary
     *
     * @param timeout number of milliseconds to wait for (if necessary)
     * @return try if the lock was successfully obtained, false otherwise
     * @throws IOException if an error occurs creating the lock
     */
    public boolean obtain(long timeout) throws IOException
    {
        boolean locked = obtain();
        if (locked)
            return locked;

        long loopTimes = timeout / LOOP_WAIT_TIME;
        long remainder = timeout % LOOP_WAIT_TIME;
        for (long i = 0; i < loopTimes; i++)
        {
            sleep(LOOP_WAIT_TIME);
            locked = obtain();
            if (locked)
                return locked;
        }

        // Went through all loop interations, see if we have any remainder
        if (remainder > 0)
        {
            sleep(remainder);
            locked = obtain();
        }

        return locked;
    }

    private void sleep(long loopWaitTime) throws IOException
    {
        try
        {
            Thread.sleep(loopWaitTime);
        }
        catch (InterruptedException e)
        {
            throw new IOException(e.toString());
        }
    }

    public void release()
    {
        lockFile.delete();
    }

    public boolean isLocked()
    {
        return lockFile.exists();
    }

    public String getLockFilePath()
    {
        return lockFile.getAbsolutePath();
    }
}
