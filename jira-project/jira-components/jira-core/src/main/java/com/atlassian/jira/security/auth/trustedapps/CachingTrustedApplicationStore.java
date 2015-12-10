package com.atlassian.jira.security.auth.trustedapps;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CachedReference;
import com.atlassian.cache.Supplier;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.event.ClearCacheEvent;

import com.google.common.collect.ImmutableSet;

/**
 * TrustedApplicationStore that caches the data objects in memory.
 *
 * @since v3.12
 */
@EventComponent
public final class CachingTrustedApplicationStore implements TrustedApplicationStore
{
    private final TrustedApplicationStore delegate;

    // when we clear the cache we clear the lazy loaded reference to the cache.
    private final CachedReference<Cache> cache;

    public CachingTrustedApplicationStore(final TrustedApplicationStore delegate, final CacheManager cacheManager)
    {
        this.delegate = delegate;
        cache = cacheManager.getCachedReference(getClass(),  "cache",
                new Supplier<Cache>()
                {
                    @Override
                    public Cache get()
                    {
                        return new Cache(delegate.getAll());
                    }
                });
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        cache.reset();
    }

    // --------------------------------------------------------------------------------------------------------- getters

    @Override
    public Set<TrustedApplicationData> getAll()
    {
        return cache.get().getAll();
    }

    @Override
    public TrustedApplicationData getByApplicationId(final String applicationId)
    {
        return cache.get().get(applicationId);
    }

    @Override
    public TrustedApplicationData getById(final long id)
    {
        return cache.get().get(id);
    }

    // -------------------------------------------------------------------------------------------------------- mutators

    @Override
    public TrustedApplicationData store(final TrustedApplicationData trustedApplicationData)
    {
        final TrustedApplicationData result = delegate.store(trustedApplicationData);
        cache.reset();
        return result;
    }

    @Override
    public boolean delete(final long id)
    {
        final boolean result = delegate.delete(id);
        cache.reset();
        return result;
    }

    @Override
    public boolean delete(final String applicationId)
    {
        boolean result = delegate.delete(applicationId);
        cache.reset();
        return result;
    }

    /**
     * Cache implementation that permanently caches ALL elements.
     */
    private static final class Cache
    {
        final Map <Long, TrustedApplicationData> byId;
        final Map <String, TrustedApplicationData> byAppId;

        Cache(final Set<TrustedApplicationData> datas)
        {
            final Map<Long, TrustedApplicationData> byId = new HashMap<Long, TrustedApplicationData>(datas.size());
            final Map <String, TrustedApplicationData> byAppId = new HashMap<String, TrustedApplicationData>(datas.size());
            for (TrustedApplicationData data : datas)
            {
                byId.put(data.getId(), data);
                byAppId.put(data.getApplicationId(), data);
            }
            this.byId = Collections.unmodifiableMap(byId);
            this.byAppId = Collections.unmodifiableMap(byAppId);
        }

        TrustedApplicationData get(final long id)
        {
            return byId.get(id);
        }

        TrustedApplicationData get(final String applicationId)
        {
            return byAppId.get(applicationId);
        }

        Set<TrustedApplicationData> getAll()
        {
            return ImmutableSet.copyOf(byId.values());
        }
    }
}
