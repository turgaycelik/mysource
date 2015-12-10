package com.atlassian.jira.gadgets.system;

import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.issue.statistics.TwoDimensionalStatsMap;

/**
 * Extracted to an interface to allow this behavior to be easily stubbed out.
 */
interface StatsSearchUrlBuilder
{
    String getSearchUrlForCell(Object xAxisObject, Object yAxisObject, TwoDimensionalStatsMap statsMap, SearchRequest searchRequest);

    String getSearchUrlForHeaderCell(Object axisObject, StatisticsMapper axisMapper, SearchRequest searchRequest);
}

