package com.atlassian.jira.issue.statistics.util;

import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.statistics.StatisticsMapper;

import java.util.Comparator;

/**
 * Provides a convenient implementation of the StatisticsMapper interface 
 * that can be subclassed by developers wishing to adapt the request to a mapper.
 * This class implements the Wrapper or Decorator pattern. Methods default to
 * calling through to the wrapped statisticsMapper object.
 */
public class StatisticsMapperWrapper implements StatisticsMapper
{
    private final StatisticsMapper statisticsMapper;

    public StatisticsMapperWrapper(StatisticsMapper statisticsMapper)
    {
        this.statisticsMapper = statisticsMapper;
    }

    public boolean isValidValue(Object value)
    {
        return statisticsMapper.isValidValue(value);
    }

    public boolean isFieldAlwaysPartOfAnIssue()
    {
        return statisticsMapper.isFieldAlwaysPartOfAnIssue();
    }

    public SearchRequest getSearchUrlSuffix(Object value, SearchRequest searchRequest)
    {
        return statisticsMapper.getSearchUrlSuffix(value, searchRequest);
    }

    public String getDocumentConstant()
    {
        return statisticsMapper.getDocumentConstant();
    }

    public Object getValueFromLuceneField(String documentValue)
    {
        return statisticsMapper.getValueFromLuceneField(documentValue);
    }

    public Comparator getComparator()
    {
        return ComparatorSelector.getComparator(statisticsMapper);
    }
}
