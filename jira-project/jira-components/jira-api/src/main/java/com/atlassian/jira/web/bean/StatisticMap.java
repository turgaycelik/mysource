package com.atlassian.jira.web.bean;

import java.util.Map;
import java.util.Set;

/**
 * @since v5.2
 */
public interface StatisticMap<K, N extends Number>
{
    Map<K, N> getStatistics();

    int getIrrelevantPercentage();

    void setStatistics(Map<K, N> statistics);

    Set<Map.Entry<K, N>> entrySet();

    long getTotalCount();

    int getIrrelevantCount();

    long getLargestPercentage();

    int getPercentage(K key);

    // Retrieve the value for 'null key' entry in the map
    // For example, issues without a priority have a null key in the map
    long getNullKeyValue();
}