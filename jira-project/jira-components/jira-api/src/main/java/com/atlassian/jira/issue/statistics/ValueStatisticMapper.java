package com.atlassian.jira.issue.statistics;

import java.util.Comparator;

/**
 * Provides extra method to get non-standard comparator for StatisticMappers
 * to be used when we need different comparison method (eg.comparing custom fields options in gadgets)
 *
 * @see com.atlassian.jira.issue.search.LuceneFieldSorter
 * @see StatisticsMapper
 *
 * @since v5.2
 */
public interface ValueStatisticMapper<T> extends StatisticsMapper<T> {

    Comparator<T> getValueComparator();

}
