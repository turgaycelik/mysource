package com.atlassian.jira.cache;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import com.atlassian.cache.CacheStatisticsKey;
import com.atlassian.cache.ManagedCache;
import com.atlassian.instrumentation.ExternalCounter;
import com.atlassian.instrumentation.ExternalGauge;
import com.atlassian.instrumentation.ExternalValue;
import com.atlassian.instrumentation.Instrument;
import com.atlassian.util.concurrent.Supplier;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

import org.apache.log4j.Logger;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;

/**
 * @since v6.3
 */
@ThreadSafe
public class ManagedCacheInstruments
{
    private static final Logger LOG = Logger.getLogger(ManagedCacheInstruments.class);
    private static final IgnoreHeapSize IGNORE_HEAP_SIZE = new IgnoreHeapSize();

    private final ManagedCache managedCache;
    private final String name;

    public ManagedCacheInstruments(final ManagedCache managedCache)
    {
        this.managedCache = notNull("managedCache", managedCache);
        this.name = managedCache.getName();
    }

    @Nonnull
    public List<Instrument> getInstruments()
    {
        return ImmutableList.copyOf(transform(getStatistics(), new Function<Map.Entry<CacheStatisticsKey,Supplier<Long>>,Instrument>()
        {
            @Override
            public Instrument apply(final Map.Entry<CacheStatisticsKey,Supplier<Long>> entry)
            {
                return toInstrument(entry.getKey(), entry.getValue());
            }
        }));

    }

    private Instrument toInstrument(CacheStatisticsKey key, Supplier<Long> supplier)
    {
        switch (key.getType())
        {
            case COUNTER:
                return counter(key, supplier);
            default:
                return gauge(key, supplier);
        }
    }

    private Iterable<Map.Entry<CacheStatisticsKey,Supplier<Long>>> getStatistics()
    {
        // Include the heap size iff debug logging is enabled
        if (LOG.isDebugEnabled())
        {
            return managedCache.getStatistics().entrySet();
        }
        return filter(managedCache.getStatistics().entrySet(), IGNORE_HEAP_SIZE);
    }

    public String getName()
    {
        return name;
    }

    private ExternalCounter counter(CacheStatisticsKey key, Supplier<Long> supplier)
    {
        return new ExternalCounter(name + '.' + key.getLabel(), new StatisticValue(supplier));
    }

    private ExternalGauge gauge(CacheStatisticsKey key, Supplier<Long> supplier)
    {
        return new ExternalGauge(name + '.' + key.getLabel(), new StatisticValue(supplier));
    }

    static class IgnoreHeapSize implements Predicate<Map.Entry<CacheStatisticsKey, Supplier<Long>>>
    {
        @Override
        public boolean apply(final Map.Entry<CacheStatisticsKey,Supplier<Long>> entry)
        {
            return entry.getKey() != CacheStatisticsKey.HEAP_SIZE;
        }
    }

    static class StatisticValue implements ExternalValue
    {
        private final Supplier<Long> supplier;

        StatisticValue(final Supplier<Long> supplier)
        {
            this.supplier = supplier;
        }

        @Override
        public long getValue()
        {
            final Long value = supplier.get();
            return (value != null) ? value : 0L;
        }
    }
}
