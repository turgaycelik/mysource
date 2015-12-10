package com.atlassian.jira.cache;

/**
 * Null implementation of the Cache Compactor for use when the Cache implementation is well behaved and
 * compacts its own trash.
 *
 * @since v6.3
 */
public class NullCacheCompactor implements CacheCompactor
{
    @Override
    public CacheCompactionResult purgeExpiredCacheEntries()
    {
        return new CacheCompactionResult(0, 0, 0);
    }
}
