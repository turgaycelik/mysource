package com.atlassian.jira.startup;

import com.atlassian.jira.util.IOUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

/**
 * This can acquire a lock on the JIRA HOME directory and hence prevent other processes from starting up against the
 * same JIRA
 */
class JiraHomeLockAcquirer
{
    private FileOutputStream fileOutputStream;
    private File lockFile;

    enum LockResult
    {
        OK,
        CANT_CREATE_LOCK,
        HELD_BY_OTHERS
    }

    /**
     * This can be called to acquire the lock file in the proposed jira.home directory
     *
     * @param jiraHomeDir the jira.home in force
     * @return a Lock that explain how the operation went
     */
    LockResult acquire(File jiraHomeDir)
    {
        if (fileOutputStream != null)
        {
            throw new IllegalStateException("You are trying to acquire the lock when this object already has it");
        }
        final File lockFile = new File(jiraHomeDir, ".jira-home.lock");
        try
        {
            FileOutputStream stream = new FileOutputStream(lockFile);
            FileChannel fileChannel = stream.getChannel();
            try
            {
                FileLock fileLock = fileChannel.tryLock();
                if (fileLock == null)
                {
                    return LockResult.HELD_BY_OTHERS;
                }
                this.fileOutputStream = stream;
                this.lockFile = lockFile;
                return LockResult.OK;
            }
            catch (OverlappingFileLockException overlappingFileLockException)
            {
                // probably ourselves via a unit test but an error none the less
                return LockResult.HELD_BY_OTHERS;
            }
            catch (IOException ioe)
            {
                return LockResult.CANT_CREATE_LOCK;
            }
        }
        catch (FileNotFoundException fileNotFoundException)
        {
            return LockResult.CANT_CREATE_LOCK;
        }
    }

    /**
     * This must be called to release the lock
     */
    void release()
    {
        IOUtil.shutdownStream(fileOutputStream);
        if (lockFile != null) {
            //noinspection ResultOfMethodCallIgnored
            lockFile.delete();
        }
        fileOutputStream = null;
        lockFile = null;
    }
}
