package com.atlassian.jira.cluster.lock;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import com.atlassian.beehive.core.ManagedClusterLock;
import com.atlassian.beehive.core.stats.StatisticsKey;
import com.atlassian.cache.CacheStatisticsKey;
import com.atlassian.instrumentation.ExternalCounter;
import com.atlassian.instrumentation.ExternalGauge;
import com.atlassian.instrumentation.ExternalValue;
import com.atlassian.instrumentation.Instrument;

import com.google.common.base.Function;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Iterables.transform;

/**
 *
 * @since v6.3.6
 */
public class ClusterLockInstruments
{
    private final ManagedClusterLock clusterLock;
    final String name;

    public ClusterLockInstruments(final ManagedClusterLock clusterLock)
    {
        this.clusterLock = notNull("clusterLock", clusterLock);
        this.name = clusterLock.getName();
    }

    @Nonnull
    public List<Instrument> getInstruments()
    {
        return copyOf(transform(getStatistics(), new ToInstrument()));
    }

    private Set<Map.Entry<StatisticsKey,Long>> getStatistics()
    {
        return clusterLock.getStatistics().entrySet();
    }

    public String getName()
    {
        return name;
    }

    class ToInstrument implements Function<Map.Entry<StatisticsKey,Long>, Instrument>
    {
        @Override
        public Instrument apply(Map.Entry<StatisticsKey, Long> entry)
        {
            return apply(entry.getKey(), entry.getValue());
        }

        private Instrument apply(StatisticsKey key, Long value)
        {
            switch (key.getType())
            {
                case COUNTER:
                    return counter(key, value);
                default:
                    return gauge(key, value);
            }
        }

        private Instrument counter(StatisticsKey key, Long value)
        {
            return new ExternalCounter(name + '.' + key.getLabel(), new StatisticValue(value));
        }

        private Instrument gauge(StatisticsKey key, Long value)
        {
            return new ExternalGauge(name + '.' + key.getLabel(), new StatisticValue(value));
        }
    }

    static class StatisticValue implements ExternalValue
    {
        private final long value;

        StatisticValue(final Long value)
        {
            this.value = (value != null) ? value : 0L;
        }

        @Override
        public long getValue()
        {
            return value;
        }
    }
}
