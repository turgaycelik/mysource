package com.atlassian.jira.gadgets.system;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.util.QueryOptimizer;
import com.atlassian.jira.issue.search.util.RedundantClausesQueryOptimizer;
import com.atlassian.jira.issue.statistics.IssueTypeStatisticsMapper;
import com.atlassian.jira.issue.statistics.ProjectStatisticsMapper;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.issue.statistics.TwoDimensionalStatsMap;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.query.Query;

public class DefaultStatsSearchUrlBuilder implements StatsSearchUrlBuilder
{
    private SearchService searchService;
    private JiraAuthenticationContext authenticationContext;
    private QueryOptimizer optimizer = new RedundantClausesQueryOptimizer();

    public DefaultStatsSearchUrlBuilder(SearchService searchService, JiraAuthenticationContext authenticationContext)
    {
        this.searchService = searchService;
        this.authenticationContext = authenticationContext;
    }

    public String getSearchUrlForHeaderCell(Object axisObject, StatisticsMapper axisMapper, SearchRequest searchRequest)
    {
        SearchRequest searchUrlSuffix = axisMapper.getSearchUrlSuffix(axisObject, searchRequest);
        if(searchUrlSuffix == null)
        {
            return "";
        }
        final Query query = optimizer.optimizeQuery(searchUrlSuffix.getQuery());
        return searchService.getQueryString(authenticationContext.getLoggedInUser(), query);
    }

    /**
     * This should be rewritten to extract a clause from each of the statistics mappers and finding the intersection of
     * these two filters, but this requires an interface change to the statistics mapper.
     */
    public String getSearchUrlForCell(Object xAxisObject, Object yAxisObject, TwoDimensionalStatsMap statsMap, SearchRequest searchRequest)
    {
        StatisticsMapper xAxisMapper = statsMap.getxAxisMapper();
        StatisticsMapper yAxisMapper = statsMap.getyAxisMapper();

        SearchRequest srAfterSecond;
        if (isFirst(yAxisMapper, xAxisMapper))
        {
            SearchRequest srAfterFirst = yAxisMapper.getSearchUrlSuffix(yAxisObject, searchRequest);
            srAfterSecond = xAxisMapper.getSearchUrlSuffix(xAxisObject, srAfterFirst);
        }
        else
        {
            SearchRequest srAfterFirst = xAxisMapper.getSearchUrlSuffix(xAxisObject, searchRequest);
            srAfterSecond = yAxisMapper.getSearchUrlSuffix(yAxisObject, srAfterFirst);
        }
        final Query query = (srAfterSecond != null) ? optimizer.optimizeQuery(srAfterSecond.getQuery()) : null;
        return query != null ? searchService.getQueryString(authenticationContext.getLoggedInUser(), query) : "";
    }

    private boolean isFirst(StatisticsMapper a, StatisticsMapper b)
    {
        if (a instanceof ProjectStatisticsMapper)
        {
            return true;
        }
        else if (b instanceof ProjectStatisticsMapper)
        {
            return false;
        }
        else if (a instanceof IssueTypeStatisticsMapper)
        {
            return true;
        }
        else if (b instanceof IssueTypeStatisticsMapper)
        {
            return false;
        }
        else
        {
            return true;
        }
    }
}
