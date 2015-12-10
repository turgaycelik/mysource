package com.atlassian.jira.datetime;

import java.util.concurrent.ConcurrentMap;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import com.atlassian.jira.cluster.ClusterSafe;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;

import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cache for JODA's DateTimeFormatter instances. Eventually the formatters should be moved into a per-request cache,
 * instead of a global cache like this one.
 *
 * @since 4.4
 */
@ThreadSafe
class JodaFormatterCache implements JodaFormatterSupplier
{
    /**
     * Logger for DateTimeFormatterCache.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(JodaFormatterCache.class);

    /**
     * Thread-safe computing map, which we use as a poor man's cache.
     */
    @ClusterSafe
    private final ConcurrentMap<Key, org.joda.time.format.DateTimeFormatter> cache = new MapMaker().makeComputingMap(new CreateFormatter());

    /**
     * Creates a new DateTimeFormatterCache.
     */
    JodaFormatterCache()
    {
    }

    /**
     * Returns the DateTimeFormatter for the given cache key.
     *
     * @param key a Key
     * @return a DateTimeFormatter
     */
    public org.joda.time.format.DateTimeFormatter get(Key key)
    {
        return cache.get(key);
    }

    /**
     * Clears this cache.
     */
    public void clear()
    {
        LOGGER.trace("Clearing cache: {}", this);
        cache.clear();
    }

    private class CreateFormatter implements Function<Key, org.joda.time.format.DateTimeFormatter>
    {
        @Override
        public org.joda.time.format.DateTimeFormatter apply(@Nullable Key key)
        {
            LOGGER.trace("Creating formatter for {}", key);

            // create a formatter on demand
            return DateTimeFormat.forPattern(key.pattern).withLocale(key.locale);
        }
    }
}
