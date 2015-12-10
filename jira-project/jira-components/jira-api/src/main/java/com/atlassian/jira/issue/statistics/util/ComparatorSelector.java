package com.atlassian.jira.issue.statistics.util;

import com.atlassian.jira.issue.statistics.ValueStatisticMapper;
import com.atlassian.jira.issue.statistics.StatisticsMapper;

import java.util.Comparator;

/**
 * Utility for selecting comparator basing on mapper type
 *
 * @since v5.2
 */
public class ComparatorSelector
{
    public static <T> Comparator<T> getComparator(StatisticsMapper<T> statisticsMapper) {
        if (statisticsMapper instanceof ValueStatisticMapper) {
            return ((ValueStatisticMapper<T>) statisticsMapper).getValueComparator();
        } else {
            return statisticsMapper.getComparator();
        }
    }

}
