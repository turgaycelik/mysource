package com.atlassian.jira.bc.issue.search;

import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.SearchContextImpl;
import com.atlassian.jira.jql.context.AllIssueTypesContext;
import com.atlassian.jira.jql.context.AllProjectsContext;
import com.atlassian.jira.jql.context.ClauseContextImpl;
import com.atlassian.jira.jql.context.IssueTypeContext;
import com.atlassian.jira.jql.context.IssueTypeContextImpl;
import com.atlassian.jira.jql.context.ProjectContextImpl;
import com.atlassian.jira.jql.context.ProjectIssueTypeContext;
import com.atlassian.jira.jql.context.ProjectIssueTypeContextImpl;
import com.atlassian.jira.jql.context.QueryContext;
import com.atlassian.jira.jql.context.QueryContextImpl;
import com.atlassian.jira.util.InjectableComponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A utlility class for converting a {@link com.atlassian.jira.jql.context.QueryContext} into a {@link com.atlassian.jira.issue.search.SearchContext}.
 * This conversion only makes sense if the {@link com.atlassian.query.Query} the QueryContext was generated from fits the simple navigator.
 *
 * @since v4.0
 */
@InjectableComponent
public class QueryContextConverter
{
    /**
     * Converts a {@link com.atlassian.jira.issue.search.SearchContext} representation into
     * the {@link com.atlassian.jira.jql.context.QueryContext} of a search context.
     *
     * As search contexts represented by {@link com.atlassian.jira.jql.context.QueryContext}s is a super set of those
     * represented by {@link com.atlassian.jira.issue.search.SearchContext}, this coversion will always be valid and
     * never return null.
     *
     * @param searchContext the context to convert into a {@link com.atlassian.jira.jql.context.QueryContext}
     * @return the context represented by a {@link com.atlassian.jira.jql.context.QueryContext}. Never Null.
     */
    public QueryContext getQueryContext(SearchContext searchContext)
    {
        Set<ProjectIssueTypeContext> contexts = new HashSet<ProjectIssueTypeContext>();
        if (searchContext.isForAnyProjects() && searchContext.isForAnyIssueTypes())
        {
            return new QueryContextImpl(ClauseContextImpl.createGlobalClauseContext());
        }
        else if (searchContext.isForAnyProjects())
        {
            for (String typeId : searchContext.getIssueTypeIds())
            {
                contexts.add(new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, new IssueTypeContextImpl( typeId )));
            }
        }
        else if (searchContext.isForAnyIssueTypes())
        {
            for (Long projId : searchContext.getProjectIds())
            {
                contexts.add(new ProjectIssueTypeContextImpl(new ProjectContextImpl(projId), AllIssueTypesContext.INSTANCE));
            }
        }
        else
        {
            for (Long projId : searchContext.getProjectIds())
            {
                for (String typeId : searchContext.getIssueTypeIds())
                {
                    contexts.add(new ProjectIssueTypeContextImpl(new ProjectContextImpl(projId), new IssueTypeContextImpl( typeId )));
                }
            }
        }
        return new QueryContextImpl(new ClauseContextImpl(contexts));
    }

    /**
     * Converts a {@link com.atlassian.jira.jql.context.QueryContext} into a {@link com.atlassian.jira.issue.search.SearchContext}.
     * If the conversion does not make sense, null is returned.
     *
     * If you would like to know if this method will correctly generate a SearchContext you should call
     * {@link SearchService#doesQueryFitFilterForm(com.atlassian.crowd.embedded.api.User,com.atlassian.query.Query)}.
     *
     * @param queryContext the QueryContext to convert into a SearchContext, if null then a null SearchContext is returned.
     * @return the SearchContext generated from the QueryContext, this will be null if we are unable to correctly
     * generate a SearchContext.
     */
    public SearchContext getSearchContext(QueryContext queryContext)
    {
        List<Long> projects = null;
        List<String> issueTypes = null;

        if (queryContext != null)
        {
            // If we are unable to generate a list of project ids from a queryContext then we should not create a search context
            projects = getSearchContextProjects(queryContext);
            if (projects != null)
            {
                // If we are unable to generate a list of issue types from a queryContext then we should not create a search context
                issueTypes = getSearchContextIssueTypes(queryContext);
            }
        }

        if (projects != null && issueTypes != null)
        {
            return createSearchContext(projects, issueTypes);
        }
        else
        {
            // If we couldnt generate a valid SearchContext return  null
            return null;
        }
    }

    private List<String> getSearchContextIssueTypes(QueryContext queryContext)
    {
        final Set<Set<String>> typesPerProject = new HashSet<Set<String>>();

        final Collection<QueryContext.ProjectIssueTypeContexts> contexts = queryContext.getProjectIssueTypeContexts();

        // assume "All" type contexts return null for ids.
        for (QueryContext.ProjectIssueTypeContexts context : contexts)
        {
            final Collection<IssueTypeContext> issueTypeContexts = context.getIssueTypeContexts();
            Set<String> typesForProject = new LinkedHashSet<String>();
            for (IssueTypeContext issueTypeContext : issueTypeContexts)
            {
                typesForProject.add(issueTypeContext.getIssueTypeId());
            }

            typesPerProject.add(typesForProject);
        }

        // all projects must have the same set of issue types
        if (typesPerProject.size() != 1)
        {
            return null;
        }


        Set<String> types = typesPerProject.iterator().next();

        // If there is an "All" issue type context, then there can be no specific issue type context
        if (types.contains(null) && types.size() != 1)
        {
            return null;
        }
        else if (types.contains(null))
        {
            return Collections.emptyList();
        }
        else
        {
            return new ArrayList<String>(types);
        }
    }

    private List<Long> getSearchContextProjects(QueryContext queryContext)
    {
        final Set<Long> projects = new LinkedHashSet<Long>();

        final Collection<QueryContext.ProjectIssueTypeContexts> contexts = queryContext.getProjectIssueTypeContexts();

        // assume "All" type contexts return null for ids.
        for (QueryContext.ProjectIssueTypeContexts context : contexts)
        {
            final Long project = context.getProjectContext().getProjectId();
            projects.add(project);
        }

        // If there is an "All" project context, then there can be no specific project context
        if (projects.contains(null) && projects.size() != 1)
        {
            return null;
        }
        else if (projects.contains(null))
        {
            return Collections.emptyList();
        }

        return new ArrayList<Long>(projects);
    }

    // We use this for testing since building a SearchContext brings up the entire world :)
    ///CLOVER:OFF
    SearchContext createSearchContext(final List<Long> projects, final List<String> issueTypes)
    {
        return new SearchContextImpl(Collections.emptyList(), projects, issueTypes);
    }
    ///CLOVER:ON
}
