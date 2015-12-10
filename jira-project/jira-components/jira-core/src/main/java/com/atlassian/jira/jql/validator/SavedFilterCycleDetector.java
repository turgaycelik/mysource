package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.impl.NamedTerminalClauseCollectingVisitor;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryCreationContextImpl;
import com.atlassian.jira.jql.resolver.SavedFilterResolver;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClause;
import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Runs through a saved filter to determine if there is any self-reference anywhere in the nested filter.
 *
 * @since v4.0
 */
@InjectableComponent
public class SavedFilterCycleDetector
{
    private final SavedFilterResolver savedFilterResolver;
    private final JqlOperandResolver jqlOperandResolver;

    public SavedFilterCycleDetector(final SavedFilterResolver savedFilterResolver, final JqlOperandResolver jqlOperandResolver)
    {
        this.savedFilterResolver = notNull("savedFilterResolver", savedFilterResolver);
        this.jqlOperandResolver = notNull("jqlOperandResolver", jqlOperandResolver);
    }

    /**
     * Checks if the specified saved {@link com.atlassian.jira.issue.search.SearchRequest} contains a reference to another saved filter
     * by using the <code>savedFilter</code> clause.
     *
     * @param searcher the user performing the search
     * @param overrideSecurity false if we should check permissions
     * @param savedFilter the saved filter to check if it holds a reference to the filter with the filterId
     * @param filterId the id of the filter to check if it can be referenced from the savedFilter, can be null.
     *        if null it takes the filter id from the savedFilter, thus checking if it can reference itself
     * @return true if the filter does contains a reference to the filter with filterId; false otherwise.
     */
    public boolean containsSavedFilterReference(User searcher, final boolean overrideSecurity, SearchRequest savedFilter, Long filterId)
    {
        notNull("savedFilter", savedFilter);
        return new DepthFirstWalker(searcher, overrideSecurity).walk(savedFilter, filterId);
    }

    private class DepthFirstWalker
    {
        private final QueryCreationContext creationContext;

        private DepthFirstWalker(final User searcher, final boolean overrideSecurity)
        {
            this.creationContext = new QueryCreationContextImpl(searcher, overrideSecurity);
        }

        private boolean walk(SearchRequest request, Long filterId)
        {
            Set<Long> path = Sets.newHashSet();
            if (filterId != null)
            {
                path.add(filterId);
            }
            return visitSearchRequest(request, path, Sets.<Long>newHashSet());
        }

        private boolean visitClause(Clause clause, Set<Long> path, Set<Long> visited)
        {
            if (clause == null)
            {
                return false;
            }

            for (final TerminalClause nestedFilterClause : getFilterClauses(clause))
            {
                for (SearchRequest matchingSearchRequest : getSearchRequests(creationContext, nestedFilterClause))
                {
                    // Recurse through that filter to make sure it has no saved filters
                    if (visitSearchRequest(matchingSearchRequest, path, visited))
                    {
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean visitSearchRequest(SearchRequest filter, Set<Long> path, Set<Long> visited)
        {
            if (filter == null)
            {
                return false;
            }

            //No point on walking to nodes that we already know don't have cycles.
            if (visited.contains(filter.getId()))
            {
                return false;
            }

            if (!path.add(filter.getId()))
            {
                return true;
            }

            if (visitClause(filter.getQuery().getWhereClause(), path, visited))
            {
                return true;
            }
            else
            {
                //No cycle so we can remove the current filterId from the path for the next time through
                //the loop.
                path.remove(filter.getId());

                //If we visit this filter again we know we wont find a cycle, so lets mark it as visited.
                visited.add(filter.getId());
                return false;
            }
        }

        private List<SearchRequest> getSearchRequests(final QueryCreationContext creationContext, final TerminalClause clause)
        {
            final List<QueryLiteral> filterValues = jqlOperandResolver.getValues(creationContext, clause.getOperand(), clause);
            return creationContext.isSecurityOverriden()
                    ? savedFilterResolver.getSearchRequestOverrideSecurity(filterValues)
                    : savedFilterResolver.getSearchRequest(creationContext.getUser(), filterValues);
        }

        private List<TerminalClause> getFilterClauses(final Clause clause)
        {
            if (clause == null)
            {
                return Collections.emptyList();
            }
            else
            {
                NamedTerminalClauseCollectingVisitor collectingVisitor = new NamedTerminalClauseCollectingVisitor(SystemSearchConstants.forSavedFilter().getJqlClauseNames().getJqlFieldNames());
                clause.accept(collectingVisitor);
                return collectingVisitor.getNamedClauses();
            }
        }
    }
}
