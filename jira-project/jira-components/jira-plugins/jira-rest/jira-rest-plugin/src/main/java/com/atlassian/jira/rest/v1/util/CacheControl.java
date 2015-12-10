package com.atlassian.jira.rest.v1.util;

/**
 * Contains some static definitions for caching that may be useful to REST endpoints
 *
 * @since v4.0
 */
public class CacheControl
{
    // HTTP spec limits the max-age directive to one year.
    private static final int ONE_YEAR = 60 * 60 * 24 * 365;

    /**
     * Provides a cacheControl with noStore and noCache set to true
     */
    public static final javax.ws.rs.core.CacheControl NO_CACHE = new javax.ws.rs.core.CacheControl();

    static
    {
        NO_CACHE.setNoStore(true);
        NO_CACHE.setNoCache(true);
    }

    /**
     * Provides a cacheControl with a 1 year limit.  Effectively forever.
     */
    public static final javax.ws.rs.core.CacheControl CACHE_FOREVER = new javax.ws.rs.core.CacheControl();

    static
    {
        CACHE_FOREVER.setPrivate(false);
        CACHE_FOREVER.setMaxAge(ONE_YEAR);
    }
}
