package com.atlassian.jira.dev.reference.plugin.caching;

import java.util.concurrent.TimeUnit;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheLoader;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CacheSettingsBuilder;

/**
 * Basic example of using an Atlassian Cache in a plugin.
 *
 * @since v6.1
 */
public class CachingExample
{
    private final CacheManager cacheManager;
    public CachingExample(final CacheManager cacheManager)
    {
        this.cacheManager = cacheManager;
        createCache();
    }

    private Cache<String, String> myLowerCaseCache;

    private void createCache()
    {
        myLowerCaseCache = cacheManager.getCache(CachingExample.class.getName() + ".myCache",
                new CacheLoader<String, String>()
                {
                    @Override
                    public String load(final String key)
                    {
                        return key.toLowerCase();
                    }
                },
                new CacheSettingsBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build());
    }

    String getLowerCase(String word)
    {
        return myLowerCaseCache.get(word);
    }
}
