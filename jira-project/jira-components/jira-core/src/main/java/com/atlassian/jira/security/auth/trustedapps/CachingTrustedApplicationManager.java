package com.atlassian.jira.security.auth.trustedapps;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CachedReference;
import com.atlassian.cache.Supplier;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.event.ClearCacheEvent;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

/**
 * TrustedApplicationManager that caches the info objects in memory.
 *
 * @since v3.12
 */
@EventComponent
public final class CachingTrustedApplicationManager implements TrustedApplicationManager
{
    private final TrustedApplicationManager delegate;

    /** when we clear the cache we clear the lazy loaded reference to the cache. */
    private final CachedReference<Cache> cache;

    public CachingTrustedApplicationManager(final TrustedApplicationManager delegate, final CacheManager cacheManager)
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
                }
        );
    }

    @EventListener
    public void onClearCache(@SuppressWarnings("unused") final ClearCacheEvent event)
    {
        cache.reset();
    }

    // --------------------------------------------------------------------------------------------------------- getters

    @Override
    public Set<TrustedApplicationInfo> getAll()
    {
        return cache.get().getAll();
    }

    @Override
    public TrustedApplicationInfo get(final String applicationId)
    {
        return cache.get().get(applicationId);
    }

    @Override
    public TrustedApplicationInfo get(final long id)
    {
        return cache.get().get(id);
    }

    // -------------------------------------------------------------------------------------------------------- mutators

    @Override
    public boolean delete(final User user, final long id)
    {
        final boolean result = delegate.delete(user, id);
        cache.reset();
        return result;
    }

    @Override
    public boolean delete(final User user, final String applicationId)
    {
        boolean result = delegate.delete(user, applicationId);
        cache.reset();
        return result;
    }

    @Override
    public TrustedApplicationInfo store(final User user, final TrustedApplicationInfo info)
    {
        final TrustedApplicationInfo result = delegate.store(user, info);
        cache.reset();
        return result;
    }

    @Override
    public TrustedApplicationInfo store(final String user, final TrustedApplicationInfo info)
    {
        final TrustedApplicationInfo result = delegate.store(user, info);
        cache.reset();
        return result;
    }

    // --------------------------------------------------------------------------------------------------------- helpers

    /** Cache implementation that permananently caches ALL elements. */
    private static final class Cache
    {
        final Map<Long, TrustedApplicationInfo> byId;
        final Map<String, TrustedApplicationInfo> byAppId;

        Cache(final Set<TrustedApplicationInfo> infos)
        {
            final Map<Long, TrustedApplicationInfo> byId = Maps.newHashMap();
            final Map<String, TrustedApplicationInfo> byAppId = Maps.newHashMap();
            for (final TrustedApplicationInfo info : infos)
            {
                byId.put(info.getNumericId(), info);
                byAppId.put(info.getID(), info);
            }
            this.byId = Collections.unmodifiableMap(byId);
            this.byAppId = Collections.unmodifiableMap(byAppId);
        }

        TrustedApplicationInfo get(final long id)
        {
            return byId.get(id);
        }

        TrustedApplicationInfo get(final String applicationId)
        {
            return byAppId.get(applicationId);
        }

        Set<TrustedApplicationInfo> getAll()
        {
            return ImmutableSet.copyOf(byId.values());
        }
    }
}
