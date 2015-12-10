/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.search;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.issue.search.util.SearchSortUtil;
import com.atlassian.jira.util.I18nHelper;
import org.apache.commons.collections.ListUtils;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.List;

public class SearchRequestUtils
{
    private static final Logger log = Logger.getLogger(SearchRequestUtils.class);

    /**
     * Creates the most specific {@link SearchContext} possible from the two parameters. The baseSearchContext is used as
     * the parent context and the possibleContext is used to narrow it down. Thus if the possibleContext contains Project A
     * and the baseSearchContext is global, a context with Project A will be returned. If baseSearchContext is Project A &amp; Project B
     * the Project A will be returned. If baseSearchContext is only Project B, then Project B will be returned. The same
     * logic applies for issue types.
     *
     * @param baseSearchContext the base <em>parent</em> context
     * @param possibleContext the context to try to narrow the baseSearchContext on
     * @return a combineed {@link SearchContext} object based on the baseSearchContext. Null if baseSearchContext is null
     */
    public static SearchContext getCombinedSearchContext(SearchContext baseSearchContext, SearchContext possibleContext)
    {
        if (baseSearchContext != null)
        {
            if (possibleContext != null)
            {
                // Deal with the projects
                List combinedProjects;
                if (baseSearchContext.isForAnyProjects())
                {
                    combinedProjects = possibleContext.getProjectIds();
                }
                else
                {
                    combinedProjects = ListUtils.intersection(baseSearchContext.getProjectIds(), possibleContext.getProjectIds() != null ? possibleContext.getProjectIds() : Collections.EMPTY_LIST);
                    if (combinedProjects.isEmpty())
                    {
                        combinedProjects = baseSearchContext.getProjectIds();
                    }
                }

                // Deal with the issue types
                List combinedIssuetypes;
                if (baseSearchContext.isForAnyIssueTypes())
                {
                    combinedIssuetypes = possibleContext.getIssueTypeIds();
                }
                else
                {
                    combinedIssuetypes = ListUtils.intersection(baseSearchContext.getIssueTypeIds(), possibleContext.getIssueTypeIds() != null ? possibleContext.getIssueTypeIds() : Collections.EMPTY_LIST);
                    if (combinedIssuetypes.isEmpty())
                    {
                        combinedIssuetypes = baseSearchContext.getIssueTypeIds();
                    }
                }
                SearchContextFactory searchContextFactory = ComponentAccessor.getComponent(SearchContextFactory.class);
                return searchContextFactory.create(null, combinedProjects, combinedIssuetypes);

            }
            else
            {
                SearchContextFactory searchContextFactory = ComponentAccessor.getComponent(SearchContextFactory.class);
                return searchContextFactory.create(baseSearchContext);
            }
        }
        else
        {
            return null;
        }
    }

    /**
     * Returns a list of the descriptions of each sorter defined in the search request.
     *
     * If one of the sorters references a field which does not exist, it will be skipped.
     *
     * @param searchRequest the search request containing the sorts; must not be null.
     * @param fieldManager field manager
     * @param searchHandlerManager search handler manager
     * @param searchSortUtil search sort utility
     * @param i18nHelper i18n helper
     * @param searcher the user making the request
     * @return a list of strings describing the sorters; never null.
     * @since v3.13.3
     *
     * @deprecated Use {@link SearchSortUtil#getSearchSortDescriptions(SearchRequest, I18nHelper, User)} instead. Since v5.0.
     */
    public static List<String> getSearchSortDescriptions(SearchRequest searchRequest, final FieldManager fieldManager, final SearchHandlerManager searchHandlerManager, final SearchSortUtil searchSortUtil, final I18nHelper i18nHelper, final User searcher)
    {
        return ComponentAccessor.getComponent(SearchSortUtil.class).getSearchSortDescriptions(searchRequest, i18nHelper, searcher);
    }
}
