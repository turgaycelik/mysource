package com.atlassian.jira.plugin.userformat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import com.atlassian.fugue.Option;
import com.atlassian.jira.plugin.profile.UserFormat;
import com.atlassian.jira.security.JiraAuthenticationContextImpl;
import com.atlassian.jira.security.RequestCacheKeys;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Wrapping implementation of UserFormat that caches the results in RequestCache.
 *
 * Note: results of both format methods are cached in the same cache.
 * But for example format("1", "2") and format("1", "2", null) are still considered as different invocations because
 * there is no guarantee they will return the same results.
 *
 * @since v6.4
 */
public class CachingUserFormat implements UserFormat
{
    private static final long MAX_CACHE_SIZE = 500L;
    private final UserFormat delegate;
    private final Cache<Key, Option<String>> cache;

    public CachingUserFormat(final UserFormat delegate)
    {
        this.delegate = delegate;

        final Map<String, Object> requestCache = JiraAuthenticationContextImpl.getRequestCache();

        @SuppressWarnings("unchecked")
        Cache<Key, Option<String>> cache = (Cache<Key, Option<String>>) requestCache.get(RequestCacheKeys.USER_FORMAT_CACHE);

        if (cache == null)
        {
            cache = CacheBuilder.newBuilder().maximumSize(MAX_CACHE_SIZE).build();
            requestCache.put(RequestCacheKeys.USER_FORMAT_CACHE, cache);
        }
        this.cache = cache;
    }

    @Override
    public String format(final String userkey, final String id)
    {
        final Key key = new Key(userkey, id, null, false, delegate);
        try
        {
            return cache.get(key, new Callable<Option<String>>()
            {
                @Override
                public Option<String> call()
                {
                    return Option.option(delegate.format(userkey, id));
                }
            }).getOrNull();
        }
        catch (ExecutionException e)
        {
            throw new RuntimeException(e.getCause());
        }
    }

    @Override
    public String format(final String userkey, final String id, final Map<String, Object> params)
    {
        final Key key = new Key(userkey, id, params, true, delegate);
        try
        {
            return cache.get(key, new Callable<Option<String>>()
            {
                @Override
                public Option<String> call()
                {
                    return Option.option(delegate.format(userkey, id, params));
                }
            }).getOrNull();
        }
        catch (ExecutionException e)
        {
            throw new RuntimeException(e.getCause());
        }
    }

    @VisibleForTesting
    UserFormat getDelegate()
    {
        return delegate;
    }

    static class Key
    {
        private final String userkey;
        private final String id;
        private final Map<String, Object> contextParams;
        private final boolean paramsProvided;
        private final UserFormat userFormat;
        private final int hashCode;

        public Key(final String userkey, final String id, final Map<String, Object> contextParams, final boolean paramsProvided, final UserFormat userFormat)
        {
            this.userkey = userkey;
            this.id = id;
            this.contextParams = contextParams == null ? null : new HashMap<String, Object>(contextParams);
            this.paramsProvided = paramsProvided;
            this.userFormat = userFormat;
            hashCode = calculateHashCode();
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            final Key key = (Key) o;

            if (paramsProvided != key.paramsProvided) { return false; }
            if (id != null ? !id.equals(key.id) : key.id != null) { return false; }
            if (userkey != null ? !userkey.equals(key.userkey) : key.userkey != null) { return false; }
            if (userFormat != null ? !userFormat.equals(key.userFormat) : key.userFormat != null) { return false; }
            if (contextParams != null ? !contextParams.equals(key.contextParams) : key.contextParams != null)
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            return hashCode;
        }

        private int calculateHashCode()
        {
            int result = userkey != null ? userkey.hashCode() : 0;
            result = 31 * result + (id != null ? id.hashCode() : 0);
            result = 31 * result + (contextParams != null ? contextParams.hashCode() : 0);
            result = 31 * result + (paramsProvided ? 1 : 0);
            result = 31 * result + (userFormat != null ? userFormat.hashCode() : 0);
            return result;
        }
    }
}
