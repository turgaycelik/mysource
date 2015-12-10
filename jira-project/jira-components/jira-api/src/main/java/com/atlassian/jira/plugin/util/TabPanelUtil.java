package com.atlassian.jira.plugin.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import com.atlassian.query.order.SortOrder;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * @since v3.10
 */
public class TabPanelUtil
{
    public static final int MAX_ISSUES_TO_DISPLAY = 50;
    public static final PagerFilter PAGER_FILTER = new PagerFilter(MAX_ISSUES_TO_DISPLAY); //only show the top X voted issues
    private static final Long NO_VOTES = (long) 0;

    /**
     * Returns a sub-set of the collection that has up to 'subset' number of entries.
     * Returns an empty collection in case of the collection being null or empty.
     *
     * @param collection collection of versions
     * @param subset   max number of entries in the returning collection
     * @return collection of entries, may be empty but never null
     */
    public static Collection subSetCollection(Collection collection, int subset)
    {
        if (collection == null || collection.isEmpty())
        {
            return Collections.EMPTY_LIST;
        }
        else if (subset >= 0 && subset <= collection.size())
        {
            //if subset is set, only return a subset of versions.
            return new ArrayList(collection).subList(0, subset);
        }
        else
        {
            return collection;
        }
    }


    /**
     * Removes the issue with 0 votes from the given list.
     * Note: It also removes it if {@link com.atlassian.jira.issue.Issue#getVotes()} returns null.
     *
     * @param issues list of {@link com.atlassian.jira.issue.Issue} objects
     */
    public static void filterIssuesWithNoVotes(List issues)
    {
        if (issues != null)
        {
            for (Iterator iterator = issues.iterator(); iterator.hasNext();)
            {
                final Long votes = ((Issue) iterator.next()).getVotes();
                if (votes == null || NO_VOTES.equals(votes))
                {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * Helper class to find all the popular issues for a given project.
     */
    public static class PopularIssues
    {
        private static final Logger log = Logger.getLogger(TabPanelUtil.PopularIssues.class);
        private final SearchProvider searchProvider;
        private final ConstantsManager constantsManager;

        public PopularIssues(SearchProvider searchProvider, ConstantsManager constantsManager)
        {
            this.searchProvider = searchProvider;
            this.constantsManager = constantsManager;
        }

        public List getIssues(final BrowseContext context, final boolean resolved)
        {
            notNull("context", context);

            final User searcher = context.getUser();
            final List<Issue> issues = new ArrayList<Issue>();
            try
            {
                final Query initialQuery = context.createQuery();

                final JqlQueryBuilder builder = JqlQueryBuilder.newBuilder(initialQuery);
                final JqlClauseBuilder whereBuilder = builder.where().defaultAnd();

                if(!resolved)
                {
                    whereBuilder.unresolved();
                }
                else
                {
                    final List<String> resolutionIds = new ArrayList<String>();
                    for (Resolution resolution : constantsManager.getResolutionObjects())
                    {
                        resolutionIds.add(resolution.getId());
                    }
                    if (!resolutionIds.isEmpty())
                    {
                        whereBuilder.resolution().inStrings(resolutionIds);
                    }
                }

                // Set the sorts on our query.
                builder.orderBy().clear().votes(SortOrder.DESC).priority(SortOrder.DESC).issueKey(SortOrder.ASC);

                issues.addAll(searchProvider.search(builder.buildQuery(), searcher, PAGER_FILTER).getIssues());
                TabPanelUtil.filterIssuesWithNoVotes(issues);
            }
            catch (SearchException e)
            {
                log.error("Error finding popular issues: " + e, e);
            }
            return issues;
        }
    }
}
