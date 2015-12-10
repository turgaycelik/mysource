package com.atlassian.jira.issue.search.searchers;

import com.atlassian.jira.issue.search.searchers.impl.AffectedVersionsSearcher;
import com.atlassian.jira.issue.search.searchers.impl.AssigneeSearcher;
import com.atlassian.jira.issue.search.searchers.impl.CommentQuerySearcher;
import com.atlassian.jira.issue.search.searchers.impl.ComponentsSearcher;
import com.atlassian.jira.issue.search.searchers.impl.CreatedDateSearcher;
import com.atlassian.jira.issue.search.searchers.impl.DescriptionQuerySearcher;
import com.atlassian.jira.issue.search.searchers.impl.DueDateSearcher;
import com.atlassian.jira.issue.search.searchers.impl.EnvironmentQuerySearcher;
import com.atlassian.jira.issue.search.searchers.impl.FixForVersionsSearcher;
import com.atlassian.jira.issue.search.searchers.impl.IssueTypeSearcher;
import com.atlassian.jira.issue.search.searchers.impl.LabelsSearcher;
import com.atlassian.jira.issue.search.searchers.impl.PrioritySearcher;
import com.atlassian.jira.issue.search.searchers.impl.ProjectSearcher;
import com.atlassian.jira.issue.search.searchers.impl.ReporterSearcher;
import com.atlassian.jira.issue.search.searchers.impl.ResolutionDateSearcher;
import com.atlassian.jira.issue.search.searchers.impl.ResolutionSearcher;
import com.atlassian.jira.issue.search.searchers.impl.StatusSearcher;
import com.atlassian.jira.issue.search.searchers.impl.SummaryQuerySearcher;
import com.atlassian.jira.issue.search.searchers.impl.TextQuerySearcher;
import com.atlassian.jira.issue.search.searchers.impl.UpdatedDateSearcher;
import com.atlassian.jira.issue.search.searchers.impl.WorkRatioSearcher;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Static Factory that can provide a Searcher Comparator for a given SearcherGroupType.
 *
 * @since v4.3
 */
public final class SearcherComparatorFactory
{
    private static final Map<SearcherGroupType, SearcherComparator> comparatorMap;

    static
    {
        // Initialise
        MapBuilder mapBuilder = new MapBuilder();

        mapBuilder.add(SearcherGroupType.TEXT, TextQuerySearcher.class, SummaryQuerySearcher.class, DescriptionQuerySearcher.class, EnvironmentQuerySearcher.class, CommentQuerySearcher.class);
        mapBuilder.add(SearcherGroupType.CONTEXT, ProjectSearcher.class, IssueTypeSearcher.class);
        mapBuilder.add(SearcherGroupType.PROJECT, FixForVersionsSearcher.class, ComponentsSearcher.class, AffectedVersionsSearcher.class);
        mapBuilder.add(SearcherGroupType.ISSUE, ReporterSearcher.class, AssigneeSearcher.class, StatusSearcher.class, ResolutionSearcher.class, PrioritySearcher.class, LabelsSearcher.class);
        mapBuilder.add(SearcherGroupType.DATE, CreatedDateSearcher.class, UpdatedDateSearcher.class, DueDateSearcher.class, ResolutionDateSearcher.class);
        mapBuilder.add(SearcherGroupType.WORK, WorkRatioSearcher.class);
        mapBuilder.add(SearcherGroupType.CUSTOM);

        comparatorMap = mapBuilder.toImmutableMap();
    }

    public static Comparator<IssueSearcher<?>> getSearcherComparator(SearcherGroupType searcherGroupType)
    {
        return comparatorMap.get(searcherGroupType);
    }

    private static class MapBuilder
    {
        private HashMap<SearcherGroupType, SearcherComparator> map = new HashMap<SearcherGroupType, SearcherComparator>();

        public void add(SearcherGroupType searcherGroupType, Class<? extends IssueSearcher<?>>... classes)
        {
            map.put(searcherGroupType, new SearcherComparator(Arrays.asList(classes)));
        }

        public Map<SearcherGroupType, SearcherComparator> toImmutableMap()
        {
            return Collections.unmodifiableMap(map);
        }
    }

    static final class SearcherComparator implements Comparator<IssueSearcher<?>>
    {
        private final List<Class<? extends IssueSearcher<?>>> orderList;

        SearcherComparator(final List<Class<? extends IssueSearcher<?>>> orderList)
        {
            this.orderList = orderList;
        }

        public int compare(final IssueSearcher<?> o1, final IssueSearcher<?> o2)
        {
            int o1position = indexOf(o1);
            int o2position = indexOf(o2);

            if (o1position == -1)
            {
                if (o2position == -1)
                {
                    return 0;
                }
                else
                {
                    return 1;
                }
            }
            else if (o2position == -1)
            {
                return -1;
            }
            else
            {
                return o1position - o2position;
            }
        }

        @SuppressWarnings ({ "SuspiciousMethodCalls" })
        private int indexOf(IssueSearcher<?> searcher)
        {
            return orderList.indexOf(searcher.getClass());
        }
    }
}
