package com.atlassian.jira.cache;

/**
 * Component for removing all expired entries from caches on a periodical basis.
 * You should provide a suitable implementation of this interface if the cache implementation does not
 * remove expired entries on a regular basis.
 *
 * @since v6.3
 */
public interface CacheCompactor
{
    /**
     * Removes expired entries from all caches.
     */
    CacheCompactionResult purgeExpiredCacheEntries();

    class CacheCompactionResult
    {
        private int cacheCount;
        private int totalEntriesCount;
        private int purgedEntriesCount;

        public CacheCompactionResult(final int cacheCount, final int totalEntriesCount, final int purgedEntriesCount)
        {
            this.cacheCount = cacheCount;
            this.totalEntriesCount = totalEntriesCount;
            this.purgedEntriesCount = purgedEntriesCount;
        }

        public int getCacheCount()
        {
            return cacheCount;
        }

        public int getTotalEntriesCount()
        {
            return totalEntriesCount;
        }

        public int getPurgedEntriesCount()
        {
            return purgedEntriesCount;
        }
    }
}
