package com.atlassian.jira.util;

import com.atlassian.util.concurrent.CopyOnWriteMap;
import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockFactory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.SimpleFSDirectory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @since v5.0
 */
public class LuceneDirectoryUtilsImpl implements LuceneDirectoryUtils
{
    private static final Logger log = Logger.getLogger(LuceneDirectoryUtilsImpl.class);
    private static final String LOCK_FILENAME_PREFIX = "Lock@";

    @Override
    public Directory getDirectory(final File path)
    {
        try
        {
            return FSDirectory.open(path, new UtilConcurrentLockFactory()); // returns an NIOFSDirectory on *nix SimpleFSDirectory on Windows
        }
        catch (final IOException e)
        {
            throw new RuntimeIOException(e);
        }
    }

    @Override
    public void createDirRobust(final String path) throws IOException
    {
        final File potentialPath = new File(path);
        if (!potentialPath.exists())
        {
            log.warn("Directory " + path + " does not exist - perhaps it was deleted?  Creating..");

            final boolean created = potentialPath.mkdirs();
            if (!created)
            {
                log.warn("Directory " + path + " could not be created.  Aborting index creation");
                throw new IOException("Could not create directory: " + path);
            }
        }
        if (!potentialPath.isDirectory())
        {
            log.warn("File " + path + " is not a directory.  Cannot create index");
            throw new IOException("File " + path + " is not a directory.  Cannot create index");
        }
        if (!potentialPath.canWrite())
        {
            log.warn("Dir " + path + " is not writable.  Cannot create index");
            throw new IOException("Dir " + path + " is not writable.  Cannot create index");
        }
    }

    @Override
    public Collection<String> getStaleLockPaths(final Collection<String> indexDirectoryPaths)
    {
        // A collection to which we will add all found lock file paths (if any)
        final Collection<String> existingLockFilepaths = new ArrayList<String>();

        try
        {
            // Get a path for each index directory
            if (indexDirectoryPaths != null)
            {
                for (final String indexDirectoryPath : indexDirectoryPaths)
                {
                    existingLockFilepaths.addAll(getLocks(indexDirectoryPath));
                }
            }
        }
        catch (final IOException e)
        {
            log.error("While trying to check for stale lock files: " + e.getMessage());
        }

        return existingLockFilepaths;
    }


    public Collection<String> getLocks(final String path) throws IOException
    {
        final Collection<String> locks = new ArrayList<String>();

        Directory dir = null;
        try
        {
            dir = getDirectory(new File(path));
            // Check write lock
            final org.apache.lucene.store.Lock lock = dir.makeLock(IndexWriter.WRITE_LOCK_NAME);
            if (lock.isLocked())
            {
                locks.add(getLockFilepath(lock));
            }
        }
        finally
        {
            if (dir != null)
            {
                dir.close();
            }
        }

        return locks;
    }

    private static String getLockFilepath(final org.apache.lucene.store.Lock lock)
    {
        if (lock == null)
        {
            return "";
        }

        String filePath = lock.toString();
        if ((filePath != null) && filePath.startsWith(LOCK_FILENAME_PREFIX))
        {
            filePath = filePath.substring(LOCK_FILENAME_PREFIX.length());
        }

        return filePath;
    }

    static final class UtilConcurrentLockFactory extends LockFactory
    {
        private final ConcurrentMap<String, UtilConcurrentLock> map = CopyOnWriteMap.newHashMap();

        @Override
        public void clearLock(final String lockName) throws IOException
        {
            map.remove(lockName);
        }

        @Override
        public org.apache.lucene.store.Lock makeLock(final String lockName)
        {
            final UtilConcurrentLock result = new UtilConcurrentLock();
            try
            {
                return result;
            }
            finally
            {
                map.put(lockName, result);
            }
        }
    }

    static final class UtilConcurrentLock extends org.apache.lucene.store.Lock
    {
        private final ReentrantLock lock = new ReentrantLock();

        @Override
        public boolean isLocked()
        {
            return lock.isLocked();
        }

        @Override
        public boolean obtain()
        {
            return lock.tryLock();
        }

        @Override
        public void release()
        {
            lock.unlock();
        }

        @Override
        public boolean obtain(final long timeout) throws LockObtainFailedException
        {
            try
            {
                return lock.tryLock(timeout, TimeUnit.MILLISECONDS);
            }
            catch (final InterruptedException e)
            {
                throw new LockObtainFailedException(e.toString());
            }
        }
    }
}
