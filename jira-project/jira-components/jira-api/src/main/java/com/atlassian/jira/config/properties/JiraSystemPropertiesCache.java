package com.atlassian.jira.config.properties;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import org.apache.log4j.Logger;

/**
 * This class provides caching of system properties. This is internal implementation used by {@link
 * JiraSystemProperties}.
 * <p/>
 * Cache misses are not cached (those returning null). Because of the implementation, cache misses raise
 * {@link NullPointerException}s which should be avoided.
 *
 * @since v6.1
 */
public class JiraSystemPropertiesCache implements PropertiesAccessor
{
    private static final Object NULL_VALUE = new Object() {};

    private static Logger getLogger()
    {
        return Logger.getLogger(JiraSystemPropertiesCache.class);
    }

    private static Function<Callable<Object>, Object> HANDLING_EXCEPTIONS = new Function<Callable<Object>, Object>()
    {
        @Override
        public Object apply(final Callable<Object> input)
        {
            try
            {
                return input.call();
            }
            catch (SecurityException se)
            {
                getLogger().warn("Security exception occurred while accessing system properties.", se);
                return null;
            }
            catch (NullPointerException e)
            {
                return null;
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    };

    /**
     * Proxy object to access the properties store.
     */
    private final PropertiesAccessor propertiesAccessor;

    /**
     * Actual cache
     */
    private final Cache<CacheKey, Object> cache;

    /**
     * Expiry time for the cache.
     */
    public static final long CACHE_WRITE_EXPIRY_SECONDS = 60L;

    /**
     * Capacity of cache
     */
    public static final int CACHE_CAPACITY = 100;

    /**
     * Constructor exposed currently for testing.
     *
     * @param accessor the proxy object to access properties store.
     */
    public JiraSystemPropertiesCache(final PropertiesAccessor accessor)
    {
        this.propertiesAccessor = accessor;

        this.cache = newBuilder().build(CacheLoader.from(new Function<CacheKey, Object>()
        {

            // Did not really gain much by moving the "class switch" to a map...
            private final Map<Class<?>, Function<String, Object>> classBasedHandlers = ImmutableMap.<Class<?>, Function<String, Object>>builder()
                    .put(Properties.class, new Function<String, Object>()
                    {
                        @Override
                        public Object apply(final String input)
                        {
                            return JiraSystemPropertiesCache.this.propertiesAccessor.getProperties();
                        }
                    }).put(String.class, new Function<String, Object>()
                    {
                        @Override
                        public Object apply(final String input)
                        {
                            return JiraSystemPropertiesCache.this.propertiesAccessor.getProperty(input);
                        }
                    }).put(Integer.class, new Function<String, Object>()
                    {
                        @Override
                        public Object apply(final String input)
                        {
                            return JiraSystemPropertiesCache.this.propertiesAccessor.getInteger(input);
                        }
                    }).put(Long.class, new Function<String, Object>()
                    {
                        @Override
                        public Object apply(final String input)
                        {
                            return JiraSystemPropertiesCache.this.propertiesAccessor.getLong(input);
                        }
                    }).put(Boolean.class, new Function<String, Object>()
                    {
                        @Override
                        public Object apply(final String input)
                        {
                            return JiraSystemPropertiesCache.this.propertiesAccessor.getBoolean(input);
                        }
                    }).build();

            /** Actual cache loading function. */
            @Override
            public Object apply(final CacheKey input)
            {
                final Function<String, Object> propertyAccessFunction = classBasedHandlers.get(input.ofClass);
                Preconditions.checkNotNull(propertyAccessFunction);
                final Object result = propertyAccessFunction.apply(input.key);
                return result == null ? NULL_VALUE : result;
            }
        }));
    }

    /**
     * Cache builder. Exposed to mock and test timed eviction.
     */
    @VisibleForTesting
    protected CacheBuilder<Object, Object> newBuilder()
    {
        return CacheBuilder.newBuilder()
                .expireAfterWrite(CACHE_WRITE_EXPIRY_SECONDS, TimeUnit.SECONDS)
                .maximumSize(CACHE_CAPACITY)
                .concurrencyLevel(6);
    }

    public String getProperty(@Nonnull final String key)
    {
        return (String) getCached(CacheKey.build(key, String.class));
    }

    public void setProperty(@Nonnull final String key, @Nonnull final String value)
    {
        HANDLING_EXCEPTIONS.apply(new Callable<Object>()
        {
            @Override
            public Object call() throws Exception
            {
                propertiesAccessor.setProperty(key, value);
                return null;
            }
        });
        invalidate(key);
    }

    public Boolean getBoolean(@Nonnull final String key)
    {
        return (Boolean) getCached(CacheKey.build(key, Boolean.class));
    }

    public Integer getInteger(@Nonnull final String key)
    {
        return (Integer) getCached(CacheKey.build(key, Integer.class));
    }

    public Long getLong(@Nonnull final String key)
    {
        return (Long) getCached(CacheKey.build(key, Long.class));
    }

    @Override
    public void refresh()
    {
        clear();
    }

    /**
     * Gets all system properties. Returns a snapshot of system properties obtain at the time of the call, which may be
     * considered immediately invalid. This is not a live view of the system properties.
     */
    public Properties getProperties()
    {
        final Properties props = new Properties();
        final Properties systemProps = (Properties) HANDLING_EXCEPTIONS.apply(new Callable<Object>()
        {
            @Override
            public Object call() throws Exception
            {
                return cache.getUnchecked(CacheKey.PROPERTIES_CACHE_KEY);
            }
        });
        props.putAll(systemProps);
        return props;
    }

    public void setProperties(final Properties props)
    {
        HANDLING_EXCEPTIONS.apply(new Callable<Object>()
        {
            @Override
            public Object call() throws Exception
            {
                propertiesAccessor.setProperties(props);
                return null;
            }
        });
        invalidate();
    }

    public void clear()
    {
        invalidate();
    }

    public void refresh(final String key)
    {
        Preconditions.checkNotNull(key);
        invalidate(key);
    }

    public void unsetProperty(final String key)
    {
        HANDLING_EXCEPTIONS.apply(new Callable<Object>()
        {
            @Override
            public Object call() throws Exception
            {
                propertiesAccessor.unsetProperty(key);
                return null;
            }
        });
        invalidate(key);
    }

    private Object getCached(final CacheKey cacheKey) {
        try
        {
            final Object result = HANDLING_EXCEPTIONS.apply(new Callable<Object>()
            {
                @Override
                public Object call() throws Exception
                {
                    return cache.getUnchecked(cacheKey);
                }
            });
            if(result == NULL_VALUE)
            {
                cache.invalidate(cacheKey);
                return null;
            }
            else
            {
                return result;
            }
        }
        catch (NullPointerException npe)
        {
            // cannot load:
            return null;
        }
    }

    /**
     * Invalidates all entries with the given key from the cache.
     *
     * @param key the key to be invalidated.
     */
    private void invalidate(@Nonnull final String key)
    {
        for (final Class<?> ofClass : CacheKey.handledClasses)
        {
            cache.invalidate(CacheKey.build(key, ofClass));
        }
    }

    private void invalidate()
    {
        cache.invalidateAll();
    }

    /**
     * Property cache key. A tuple of key and desired class which determines the way the property is accessed.
     * Two requests of different types and the same key will yield two different cache entries.
     */
    static class CacheKey
    {
        /** Constant key for the Properties class cache entry. */
        static final CacheKey PROPERTIES_CACHE_KEY = new CacheKey("", Properties.class);
        /** Handled types by the cache. */
        static final Set<Class<?>> handledClasses = ImmutableSet.<Class<?>>of(String.class, Integer.class, Long.class, Boolean.class, Properties.class);

        final Class<?> ofClass;
        final String key;

        @VisibleForTesting
        static CacheKey build(final String key, final Class<?> ofClass)
        {
            assert handledClasses.contains(ofClass): "Keys must be constructed for allowed classes only. Got " + (ofClass == null ? ofClass : ofClass.getName());

            if (Properties.class.equals(ofClass))
            {
                return PROPERTIES_CACHE_KEY;
            }
            else
            {
                Preconditions.checkNotNull(key);
                return new CacheKey(key, ofClass);
            }
        }

        private CacheKey(final String key, final Class<?> ofClass)
        {
            this.key = key;
            this.ofClass = ofClass;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            final CacheKey other = (CacheKey) o;

            return key.equals(other.key) && ofClass.equals(other.ofClass);
        }

        @Override
        public int hashCode()
        {
            return 31 * ofClass.hashCode() + (key != null ? key.hashCode() : 0);
        }
    }

}