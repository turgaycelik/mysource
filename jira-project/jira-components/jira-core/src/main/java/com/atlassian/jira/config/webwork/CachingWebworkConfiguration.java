package com.atlassian.jira.config.webwork;

import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.util.dbc.Assertions;
import webwork.config.ConfigurationInterface;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The webwork {@link webwork.config.Configuration} code will look up keys OVER and OVER and OVER again, throwing
 * IllegalArgumentExceptions as it goes.
 * <p/>
 * So by introducing this cache, it helps reduce this load.
 * <p/>
 * The side effect of this is that this will break "action.xml" relaoding but thats OK becauses its already broken.
 * <p/>
 * Webworks {@link webwork.dispatcher.GenericDispatcher} uses {@link webwork.dispatcher.DefaultViewMapping} which caches
 * the views and hence breaks action reloading itself!
 *
 * @since v4.0
 */
class CachingWebworkConfiguration implements ConfigurationInterface
{
    private final ConfigurationInterface delegate;
    @ClusterSafe
    private final ConcurrentMap<String, Object> cacheMap;

    CachingWebworkConfiguration(final ConfigurationInterface delegate)
    {
        Assertions.notNull("delegate", delegate);

        this.delegate = delegate;
        cacheMap = new ConcurrentHashMap<String, Object>();
    }


    public Object getImpl(final String key) throws IllegalArgumentException
    {
        Object value = cacheMap.get(key);
        if (value == null)
        {
            value = delegate.getImpl(key);
            // we only cache certain values
            if (isCacheable(key, value))
            {
                cacheMap.putIfAbsent(key, value);
            }
        }
        return value;
    }

    /**
     * These guys are set via the class ApplicationProperties which in turn are put into
     * the mix as webwork values via the class ApplicationPropertiesConfiguration
     */
    private static final Set<String> UN_CACHEABLE_KEYS;
    static
    {
        UN_CACHEABLE_KEYS = new HashSet<String>();
        UN_CACHEABLE_KEYS.add("webwork.multipart.maxSize");
        UN_CACHEABLE_KEYS.add("webwork.i18n.encoding");
    }

    /**
     * We only cache keys that have non null values and certain keys ranges
     * 
     * @param key the key name
     * @param value the delegated value
     * @return true if the values can be cached
     */
    private boolean isCacheable(final String key, final Object value)
    {
        if (value == null)
        {
            return false;
        }
        if (UN_CACHEABLE_KEYS.contains(key))
        {
            return false;
        }
        if (key.startsWith("webwork."))
        {
            return true;
        }
        return false;
    }

    public void setImpl(final String aName, final Object aValue) throws IllegalArgumentException, UnsupportedOperationException
    {
        throw new UnsupportedOperationException("JIRA never calls setImpl() and hence we wont support it here");
    }

    public Iterator listImpl()
    {
        return delegate.listImpl();
    }
}
