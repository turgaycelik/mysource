/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.bean;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.search.ReaderCache;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestAppender;
import com.atlassian.jira.issue.statistics.FilterStatisticsValuesGenerator;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.issue.statistics.util.ComparatorSelector;
import com.atlassian.jira.issue.statistics.util.OneDimensionalTermHitCollector;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.plugin.util.TabPanelUtil;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.query.clause.Clause;
import org.apache.commons.collections.comparators.ReverseComparator;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class StatisticAccessorBean
{
    private User remoteUser;
    private SearchRequest filter;
    private final boolean overrideSecurity;
    private final SearchProvider searchProvider = ComponentAccessor.getComponent(SearchProvider.class);
    private final FieldVisibilityManager fieldVisibilityManager = ComponentAccessor.getComponent(FieldVisibilityManager.class);
    private final ReaderCache readerCache = ComponentAccessor.getComponent(ReaderCache.class);
    private final FieldManager fieldManager = ComponentAccessor.getComponent(FieldManager.class);
    private final IssueTypeManager issueTypeManager = ComponentAccessor.getComponent(IssueTypeManager.class);
    private final ProjectManager projectManager = ComponentAccessor.getComponent(ProjectManager.class);


    public StatisticAccessorBean()
    {
        this.overrideSecurity = false;
    }

    public StatisticAccessorBean(User remoteUser, SearchRequest filter)
    {
        this.remoteUser = remoteUser;
        this.filter = filter;
        this.overrideSecurity = false;
    }

    public StatisticAccessorBean(User remoteUser, SearchRequest filter, boolean overrideSecurity)
    {
        this.remoteUser = remoteUser;
        this.filter = filter;
        this.overrideSecurity = overrideSecurity;
    }

    public StatisticAccessorBean(User remoteUser, Long projectId, boolean limitToOpenIssues)
    {
        this.remoteUser = remoteUser;
        this.filter = getProjectFilter(projectId, limitToOpenIssues);
        this.overrideSecurity = false;
    }

    public StatisticAccessorBean(User remoteUser, Long projectId)
    {
        this(remoteUser, projectId, true);
    }

    /**
     * Instantiates this class with given parameters.
     *
     * @param remoteUser remote User
     * @param projectId project id
     * @param additionalClause An additional clause to and to the current filter. Can be null.
     * @param limitToOpenIssues flag that limits issues on status (open vs other)
     * @since v3.10
     */
    public StatisticAccessorBean(User remoteUser, Long projectId, Clause additionalClause, boolean limitToOpenIssues)
    {
        this(remoteUser, projectId, limitToOpenIssues);
        if (additionalClause != null)
        {
            addAdditionalParameters(filter, additionalClause);
        }
    }

    private SearchRequest getProjectFilter(Long projectId, boolean limitToOpenIssues)
    {
        final JqlClauseBuilder builder = JqlQueryBuilder.newBuilder().where().defaultAnd();
        if (limitToOpenIssues)
        {
            builder.unresolved();
        }

        builder.project(projectId);
        return new SearchRequest(builder.buildQuery());
    }

    private void addAdditionalParameters(SearchRequest sr, Clause additionalClause)
    {
        JqlQueryBuilder query = JqlQueryBuilder.newBuilder(sr.getQuery());
        query.where().defaultAnd().addClause(additionalClause);
        sr.setQuery(query.buildQuery());
    }

    public SearchRequest getFilter()
    {
        return filter;
    }

    public StatisticMapWrapper getWrapper(StatisticsMapper mapper) throws SearchException
    {
        return getWrapper(mapper, null, null);
    }

    /**
     * Returns a {@link StatisticMapWrapper} containing ordered search statistic results
     *
     * @param mapper the relevant StatisticsMapper for this instance
     * @param orderBy either OrderBy.NATURAL or OrderBy.TOTAL to specify ordering by result keys or values respectively.
     * null produces the same result as OrderBy.NATURAL
     * @param direction either Direction.DESC or Direction.ASC to specify direction of ordering. null produces the same
     * result as Direction.ASC
     * @return the ordered results
     * @throws SearchException .
     */
    public StatisticMapWrapper getWrapper(StatisticsMapper mapper, OrderBy orderBy, Direction direction)
            throws SearchException
    {
        SearchStatisticsResult statisticsResult = searchCountMap(mapper.getDocumentConstant());

        Map<String, Integer> groupedCounts = statisticsResult.getStatistics();
        long numberOfissues = statisticsResult.getNumberOfIssues();

        Map<Object, Integer> map;
        Set<Map.Entry<String, Integer>> mapEntrySet;

        // JRA-13886: If ordering has been specified as OrderBy.TOTAL, we need to order results by value:
        //   use a custom comparator to order the groupedCounts by value
        // Otherwise, use the default comparator of the mapper to order results by key,
        //   or if none exists, disregard ordering (use HashMap)
        // If direction is specified as Direction.DESC, reverse the ordering of the comparators
        if (orderBy != null && OrderBy.TOTAL.equals(orderBy))
        {
            map = new LinkedHashMap<Object, Integer>();

            // Note: if values of two objects are the same, this method does not provide true "natural ordering"
            //   for the secondary comparison, as you are comparing the Lucene keys of objects (Strings),
            //   as opposed to the objects themselves, which have customised Comparators.
            // TODO: potentially redesign this method to utilise a chain of comparators, to achieve both total ordering and natural ordering.
            mapEntrySet = new TreeSet<Map.Entry<String, Integer>>(getDirectionalComparator(mapEntryComparator, direction));
            mapEntrySet.addAll(groupedCounts.entrySet());
        }
        else
        {
            final Comparator comparator = ComparatorSelector.getComparator(mapper);
            if (comparator != null)
            {
                map = new TreeMap<Object, Integer>(getDirectionalComparator(comparator, direction));
            }
            else
            {
                map = new HashMap<Object, Integer>();
            }

            mapEntrySet = groupedCounts.entrySet();
        }

        // Loop through the statistics results that we got and put it into another map.
        // This allows for:
        // 1. Put the actual objects into the map, rather than the indexed values. For example,
        // for versions, we want Version objects rather than version ids.
        // 2. The results to be sorted (if the map has a comparator)
        // 3. Remove counts for results that are invalid - JRA-5211
        // Note: sorting is either applied on the set being iterated over (total), or on the map being
        // populated (natural)
        for (Map.Entry<String, Integer> entry : mapEntrySet)
        {
            String fieldValue = entry.getKey();
            // Convert the indexed value into the actual object. For example, convert an indexed
            // version id into the Version object.
            Object objectValue = mapper.getValueFromLuceneField(fieldValue);

            if (mapper.isValidValue(objectValue)) //testing for valid values fixes JRA-5211
            {
                // Put the actual object and its count into the map
                // The map will sort itself if it has a comparator

                // There can be multiple indexed values that all have the same object value. Select Options are
                // one case. We index the Option ID (10000, 10001) but they might have the same value (Red, Red).
                // We need to accumulate them all together instead of just picking one at random to store.
                if (map.containsKey(objectValue))
                {
                    final Integer currentValue = map.get(objectValue);
                    map.put(objectValue, entry.getValue() + currentValue);
                }
                else
                {
                    map.put(objectValue, entry.getValue());
                }
            }
        }

        return new StatisticMapWrapper<Object, Integer>(map, numberOfissues, statisticsResult.getNumberOfIrrelevantResults());
    }

    private <T> Comparator<T> getDirectionalComparator(Comparator<T> c, Direction direction)
    {
        if (direction != null && Direction.DESC.equals(direction))
        {
            return new ReverseComparator(c);
        }
        return c;
    }

    protected SearchStatisticsResult searchCountMap(final String groupField) throws SearchException
    {
        OneDimensionalTermHitCollector hitCollector = new OneDimensionalTermHitCollector(groupField,
                fieldVisibilityManager, readerCache, fieldManager, projectManager);
        if (overrideSecurity)
        {
            searchProvider.searchOverrideSecurity(filter.getQuery(), remoteUser, hitCollector);
        }
        else
        {
            searchProvider.search(filter.getQuery(), remoteUser, hitCollector);
        }
        return new SearchStatisticsResult(hitCollector.getHitCount(), hitCollector.getIrrelevantCount(), hitCollector.getResult());
    }

    /**
     * Stats by Component
     */
    public long getOpenByComponent() throws SearchException
    {
        return getOpenByComponent(null);
    }

    public long getOpenByComponent(GenericValue component) throws SearchException
    {
        if (component != null)
        {
            return getCountWithClause(JqlQueryBuilder.newClauseBuilder().component(component.getLong("id")).buildClause());
        }
        else
        {
            return getCountWithClause(JqlQueryBuilder.newClauseBuilder().componentIsEmpty().buildClause());
        }
    }

    /**
     * Stats by FixFor
     */
    public long getOpenByFixFor() throws SearchException
    {
        return getOpenByFixFor(null);
    }

    public long getOpenByFixFor(Version version) throws SearchException
    {
        if (version != null)
        {
            return getCountWithClause(JqlQueryBuilder.newClauseBuilder().fixVersion(version.getId()).buildClause());
        }
        else
        {
            return getCountWithClause(JqlQueryBuilder.newClauseBuilder().fixVersionIsEmpty().buildClause());
        }
    }

    /**
     * Returns nuber of search results for this filter
     *
     * @return count
     * @throws SearchException if search fails
     * @since v3.10
     */
    public long getCount() throws SearchException
    {
        if (overrideSecurity)
        {
            return searchProvider.searchCountOverrideSecurity(filter.getQuery(), remoteUser);
        }
        else
        {
            return searchProvider.searchCount(filter.getQuery(), remoteUser);
        }
    }

    /**
     * Returns a collection of found issues for this filter
     *
     * @return collection of {@link com.atlassian.jira.issue.Issue} objects
     * @throws SearchException if search fails
     * @since v3.10
     */
    public Collection getIssues() throws SearchException
    {
        if (overrideSecurity)
        {
            return searchProvider.searchOverrideSecurity(filter.getQuery(), remoteUser, TabPanelUtil.PAGER_FILTER, null).getIssues();
        }
        else
        {
            return searchProvider.search(filter.getQuery(), remoteUser, TabPanelUtil.PAGER_FILTER).getIssues();
        }
    }

    private long getCountWithClause(Clause clause) throws SearchException
    {
        final JqlQueryBuilder builder = JqlQueryBuilder.newBuilder(filter.getQuery());
        final JqlClauseBuilder whereClauseBuilder = builder.where().defaultAnd();
        whereClauseBuilder.addClause(clause);
        if (overrideSecurity)
        {
            return searchProvider.searchCountOverrideSecurity(whereClauseBuilder.buildQuery(), remoteUser);
        }
        else
        {
            return searchProvider.searchCount(whereClauseBuilder.buildQuery(), remoteUser);
        }
    }

    public StatisticMapWrapper getAllFilterBy(String type) throws SearchException
    {
        return getWrapper(getMapper(type));
    }

    public StatisticMapWrapper getAllFilterBy(String type, OrderBy orderBy, Direction direction) throws SearchException
    {
        return getWrapper(getMapper(type), orderBy, direction);
    }

    public StatisticsMapper getMapper(String type)
    {
        return new FilterStatisticsValuesGenerator().getStatsMapper(type);
    }

    /**
     * This will obtain a SearchRequestAppender relevant for the given statisticType, either by casting the
     * StatisticsMapper to SearchRequestAppender.Factory or by constructing an artificial SearchRequestAppender wrapper
     * around the StatisticsMapper.
     */
    public SearchRequestAppender getSearchRequestAppender(final String statisticType)
    {
        final StatisticsMapper statisticsMapper = getMapper(statisticType);

        if (statisticsMapper instanceof SearchRequestAppender.Factory)
        {
            return ((SearchRequestAppender.Factory) statisticsMapper).getSearchRequestAppender();
        }
        else
        {
            return new StatisticMapperWrappingSearchRequestAppender(statisticsMapper);
        }
    }

    protected static class SearchStatisticsResult
    {
        private final long numberOfIssues;
        private final long numberOfIrrelevantResults;
        private final Map<String, Integer> statistics;

        public SearchStatisticsResult(long numberOfIssues, long numberOfIrrelevantResults, Map<String, Integer> statistics)
        {
            this.numberOfIssues = numberOfIssues;
            this.numberOfIrrelevantResults = numberOfIrrelevantResults;
            this.statistics = statistics;
        }

        public long getNumberOfIssues()
        {
            return numberOfIssues;
        }

        public long getNumberOfIrrelevantResults()
        {
            return numberOfIrrelevantResults;
        }

        public Map<String, Integer> getStatistics()
        {
            return statistics;
        }
    }

    /**
     * Enumerated type for ordering of statistics. "natural" ordering denotes ordering by search result keys natural
     * ordering (e.g. assignee ordering) "total" ordering denotes ordering by search result values (e.g. total hits per
     * assignee)
     */
    public static class OrderBy
    {
        public static final OrderBy TOTAL = new OrderBy("total");
        public static final OrderBy NATURAL = new OrderBy("natural");

        /**
         * Get an instance of an OrderBy object
         *
         * @param orderBy "total" will return a TOTAL instance, anything else (including null) will return a NATURAL
         * instance
         * @return an OrderBy instance
         */
        public static OrderBy get(String orderBy)
        {
            return (TOTAL.description.equals(orderBy)) ? TOTAL : NATURAL;
        }

        private final String description;

        private OrderBy(String description)
        {
            this.description = description;
        }

        public String toString()
        {
            return description;
        }
    }

    /**
     * Enumerated type for sort direction. "asc" is ascending order "desc" is descending order
     */
    public static class Direction
    {
        public static final Direction ASC = new Direction("asc");
        public static final Direction DESC = new Direction("desc");

        /**
         * Get an instance of a Direction object
         *
         * @param direction "desc" will return a DESC instance, anything else (including null) will return an ASC
         * instance
         * @return a Direction instance
         */
        public static Direction get(String direction)
        {
            return (DESC.description.equals(direction)) ? DESC : ASC;
        }

        private final String description;

        private Direction(String description)
        {
            this.description = description;
        }

        public String toString()
        {
            return description;
        }
    }

    /**
     * Compares two Map.Entry<String, Integer> objects by their Integer values first, and then their String keys if
     * necessary
     */
    private static final Comparator<Map.Entry<String, Integer>> mapEntryComparator = new Comparator<Map.Entry<String, Integer>>()
    {
        public int compare(Map.Entry<String, Integer> e1, Map.Entry<String, Integer> e2)
        {
            Integer v1 = e1.getValue();
            Integer v2 = e2.getValue();

            int result = v1.compareTo(v2);
            if (result == 0)
            {
                String k1 = e1.getKey();
                String k2 = e2.getKey();
                if (k1 == null && k2 == null)
                {
                    return 0;
                }

                if (k1 == null)
                {
                    return 1;
                }

                if (k2 == null)
                {
                    return -1;
                }

                result = k1.compareTo(k2);
            }

            return result;
        }
    };

    private static class StatisticMapperWrappingSearchRequestAppender<T> implements SearchRequestAppender<T>
    {
        private final StatisticsMapper<T> statisticsMapper;

        public StatisticMapperWrappingSearchRequestAppender(StatisticsMapper<T> statisticsMapper)
        {
            this.statisticsMapper = Assertions.notNull(statisticsMapper);
        }

        @Override
        public SearchRequest appendInclusiveSingleValueClause(T value, SearchRequest searchRequest)
        {
            return statisticsMapper.getSearchUrlSuffix(value, searchRequest);
        }

        @Override
        public SearchRequest appendExclusiveMultiValueClause(Iterable<? extends T> values, SearchRequest searchRequest)
        {
            return null;
        }
    }
}
