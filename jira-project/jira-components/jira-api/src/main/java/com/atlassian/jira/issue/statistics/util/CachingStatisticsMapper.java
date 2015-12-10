package com.atlassian.jira.issue.statistics.util;

import com.atlassian.jira.issue.statistics.StatisticsMapper;

import java.util.HashMap;
import java.util.Map;

/**
 * A statisticsMapper that caches the return value from {@link #getValueFromLuceneField(String)} in an internal
 * cache.
 * <p/>
 * As the cache is not bounded, this object should not be stored for longer than a request
 */
public class CachingStatisticsMapper extends StatisticsMapperWrapper
{
    private final Map<String, Object> valuesCache = new HashMap<String, Object>();

    public CachingStatisticsMapper(StatisticsMapper statisticsMapper)
    {
        super(statisticsMapper);
    }

    /**
     * As lookups may be expensive, we cache the String->Object values in a cache
     */
    public Object getValueFromLuceneField(String documentValue)
    {
        Object value = valuesCache.get(documentValue);
        if (value == null)
        {
            value = super.getValueFromLuceneField(documentValue);
            if (value != null)
            {
                valuesCache.put(documentValue, value);
            }
        }
        return value;
    }
}
