package com.atlassian.jira.jql.context;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.jql.ClauseHandler;
import com.atlassian.jira.jql.clause.DeMorgansVisitor;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.jira.util.NonInjectableComponent;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.ChangedClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.ClauseVisitor;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.WasClause;
import net.jcip.annotations.NotThreadSafe;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A visitor that is used to generate a {@link ContextResult}, which contains the full and simple
 * {@link com.atlassian.jira.jql.context.QueryContext}s of the visited {@link com.atlassian.query.Query}.
 * <p/>
 * To construct an instance of this class, please use the {@link com.atlassian.jira.jql.context.QueryContextVisitor.QueryContextVisitorFactory}.
 *
 * @see com.atlassian.jira.jql.context.QueryContextVisitor.QueryContextVisitorFactory
 * @see com.atlassian.jira.jql.context.QueryContextVisitor.ContextResult
 * @since v4.0
 */
@NotThreadSafe
@NonInjectableComponent
public class QueryContextVisitor implements ClauseVisitor<QueryContextVisitor.ContextResult>
{
    private final User searcher;
    private final ContextSetUtil contextSetUtil;
    private final SearchHandlerManager searchHandlerManager;
    private boolean rootClause = true;

    public QueryContextVisitor(final User searcher, final ContextSetUtil contextSetUtil, final SearchHandlerManager searchHandlerManager)
    {
        this.searcher = searcher;
        this.contextSetUtil = contextSetUtil;
        this.searchHandlerManager = searchHandlerManager;
    }

    public QueryContextVisitor.ContextResult createContext(final Clause clause)
    {
        // This method handles the root clause case
        this.rootClause = false;
        Clause normalisedClause = clause.accept(new DeMorgansVisitor());
        final ContextResult result = normalisedClause.accept(this);
        // reset the rootClause to stay immutable (but not thread safe)
        rootClause = true;

        return replaceEmptyContextsWithGlobal(result);
    }

    private ContextResult replaceEmptyContextsWithGlobal(final ContextResult result)
    {
        ClauseContext fullContext = result.fullContext.getContexts().isEmpty() ? ClauseContextImpl.createGlobalClauseContext() : result.fullContext;
        ClauseContext simpleContext = result.simpleContext.getContexts().isEmpty() ? ClauseContextImpl.createGlobalClauseContext() : result.simpleContext;
        return new ContextResult(fullContext, simpleContext);
    }

    public QueryContextVisitor.ContextResult visit(final AndClause andClause)
    {
        if (rootClause)
        {
            return createContext(andClause);
        }
        final Set<ClauseContext> fullChildClauseContexts = new HashSet<ClauseContext>();
        final Set<ClauseContext> simpleChildClauseContexts = new HashSet<ClauseContext>();
        final List<Clause> childClauses = andClause.getClauses();
        for (Clause childClause : childClauses)
        {
            final ContextResult result = childClause.accept(this);
            fullChildClauseContexts.add(result.getFullContext());
            simpleChildClauseContexts.add(result.getSimpleContext());
        }

        // Now lets perform an intersection of all the child clause contexts.
        return createIntersectionResult(fullChildClauseContexts, simpleChildClauseContexts);
    }

    public QueryContextVisitor.ContextResult visit(final NotClause notClause)
    {
        if (rootClause)
        {
            return createContext(notClause);
        }
        throw new IllegalStateException("We have removed all the NOT clauses from the query, this should never occur.");
    }

    public QueryContextVisitor.ContextResult visit(final OrClause orClause)
    {
        if (rootClause)
        {
            return createContext(orClause);
        }
        final Set<ClauseContext> fullChildClauseContexts = new HashSet<ClauseContext>();
        final Set<ClauseContext> simpleChildClauseContexts = new HashSet<ClauseContext>();
        final List<Clause> childClauses = orClause.getClauses();
        for (Clause childClause : childClauses)
        {
            final ContextResult result = childClause.accept(this);
            fullChildClauseContexts.add(result.getFullContext());
            simpleChildClauseContexts.add(result.getSimpleContext());
        }

        // Now lets perform a union of all the child clause contexts.
        return createUnionResult(fullChildClauseContexts, simpleChildClauseContexts);
    }

    public QueryContextVisitor.ContextResult visit(final TerminalClause clause)
    {
        if (rootClause)
        {
            return createContext(clause);
        }

        final String clauseName = clause.getName();
        final Collection<ClauseHandler> handlers = searchHandlerManager.getClauseHandler(searcher, clauseName);
        final Set<ClauseContext> fullClauseContexts = new HashSet<ClauseContext>();
        final Set<ClauseContext> simpleClauseContexts = new HashSet<ClauseContext>();
        final boolean explicit = isExplict(clause);

        for (ClauseHandler clauseHandler : handlers)
        {
            // keep track of this context in the full contexts
            final ClauseContext context = clauseHandler.getClauseContextFactory().getClauseContext(searcher, clause);
            if (!context.getContexts().isEmpty())
            {           
                fullClauseContexts.add(context);

                // the simple context is only made up of project and issue type clauses - if this clause is not one of those
                // then ignore (by adding the Global)
                if (explicit)
                {
                    simpleClauseContexts.add(context);
                }
                else
                {
                    simpleClauseContexts.add(ClauseContextImpl.createGlobalClauseContext());
                }
            }
        }

        return createUnionResult(fullClauseContexts, simpleClauseContexts);
    }

    @Override
    public ContextResult visit(WasClause clause)
    {
        //for now simply return the ALL-ALL context
        return new ContextResult(ClauseContextImpl.createGlobalClauseContext(),ClauseContextImpl.createGlobalClauseContext());
    }

    @Override
    public ContextResult visit(ChangedClause clause)
    {
        //for now simply return the ALL-ALL context
        return new ContextResult(ClauseContextImpl.createGlobalClauseContext(),ClauseContextImpl.createGlobalClauseContext());
    }

    private boolean isExplict(final TerminalClause clause)
    {
        return SystemSearchConstants.forProject().getJqlClauseNames().contains(clause.getName()) ||
                SystemSearchConstants.forIssueType().getJqlClauseNames().contains(clause.getName());
    }

    private ContextResult createUnionResult(Set<? extends ClauseContext> fullContexts, Set<? extends ClauseContext> simpleContexts)
    {
        final ClauseContext fullContext = safeUnion(fullContexts);
        final ClauseContext simpleContext;
        if (fullContexts.equals(simpleContexts))
        {
            simpleContext = fullContext;
        }
        else
        {
            simpleContext = safeUnion(simpleContexts);
        }
        return new ContextResult(fullContext, simpleContext);
    }

    private ContextResult createIntersectionResult(Set<? extends ClauseContext> fullContexts, Set<? extends ClauseContext> simpleContexts)
    {
        final ClauseContext fullContext = safeIntersection(fullContexts);
        final ClauseContext simpleContext;
        if (fullContexts.equals(simpleContexts))
        {
            simpleContext = fullContext;
        }
        else
        {
            simpleContext = safeIntersection(simpleContexts);
        }
        return new ContextResult(fullContext, simpleContext);
    }

    private ClauseContext safeUnion(Set<? extends ClauseContext> contexts)
    {
        if (contexts == null || contexts.isEmpty())
        {
            return ClauseContextImpl.createGlobalClauseContext();
        }

        final ClauseContext returnContext;
        if (contexts.size() == 1)
        {
            returnContext = contexts.iterator().next();
        }
        else
        {
            returnContext = contextSetUtil.union(contexts);
        }

        return (returnContext.getContexts().isEmpty()) ? ClauseContextImpl.createGlobalClauseContext() : returnContext;
    }

    private ClauseContext safeIntersection(Set<? extends ClauseContext> contexts)
    {
        if (contexts == null || contexts.isEmpty())
        {
            return ClauseContextImpl.createGlobalClauseContext();
        }

        final ClauseContext returnContext;
        if (contexts.size() == 1)
        {
            returnContext = contexts.iterator().next();
        }
        else
        {
            returnContext = contextSetUtil.intersect(contexts);
        }

        return (returnContext.getContexts().isEmpty()) ? ClauseContextImpl.createGlobalClauseContext() : returnContext;
    }

    /**
     * Constructs an instance of {@link com.atlassian.jira.jql.context.QueryContextVisitor} for use.
     *
     * @since v4.0
     */
    @InjectableComponent
    public static class QueryContextVisitorFactory
    {
        private final ContextSetUtil contextSetUtil;
        private final SearchHandlerManager searchHandlerManager;

        public QueryContextVisitorFactory(final ContextSetUtil contextSetUtil, final SearchHandlerManager searchHandlerManager)
        {
            this.contextSetUtil = Assertions.notNull("contextSetUtil", contextSetUtil);
            this.searchHandlerManager = Assertions.notNull("searchHandlerManager", searchHandlerManager);
        }

        /**
         * Use this to calculate the context for an entire Query.
         *
         * @param searcher the user to calculate the contexts for
         * @return a visitor that will calculate the context for all clauses specified in the {@link com.atlassian.query.Query}.
         */
        public QueryContextVisitor createVisitor(User searcher)
        {
            return new QueryContextVisitor(searcher, contextSetUtil, searchHandlerManager);
        }
    }

    /**
     * The result of visiting a {@link com.atlassian.query.Query} with the {@link com.atlassian.jira.jql.context.QueryContextVisitor}.
     * Contains a reference to the full and simple {@link ClauseContext}s.
     * <p/>
     * The <strong>full</strong> ClauseContext takes into account all clauses in the Query, and hence may contain a combination
     * of explicit and implicit projects and issue types.
     * <p/>
     * The <strong>simple</strong> ClauseContext only takes into account the project and issue type clauses in the Query,
     * and hence will only contain explicit projects and issue types.
     * <p/>
     * To understand why we need this distinction, consider the following scenario. A custom field <code>cf[100]</code>
     * has only one field configuration for the project <code>HSP</code>. There is also another project called
     * <code>MKY</code>, for which this custom field is not visible. Consider the query
     * <code>cf[100] = "a" AND project IN (HSP, MKY)</code>.
     * <p/>
     * The full ClauseContext is the intersection of the ClauseContexts of the custom field and project clauses. In this case,
     * the custom field context is implicitly the HSP project with all issue types, since the HSP project is the only
     * project it is configured for. The project clause's context is explicitly the HSP and MKY projects, since it names
     * them both. Intersecting these yields the <strong>explicit</strong> context of project <strong>HSP</strong> with
     * all issue types. If you think about what kind of results this query could return, this makes sense: the query
     * could only return issues from project HSP, since only those issues will have values for that custom field.
     * <p/>
     * The simple ClauseContext, on the other hand, is the intersection of the Global Context and the ClauseContexts of the
     * project and issue type clauses, of which there is only one. (The Global Context gets substituted in place of any
     * non-project or non-issuetype clauses.) Again, the project clause's context is explicitly the HSP and MKY projects,
     * since it names them both. Intersecting these yields the <strong>explicit</strong> context of projects
     * <strong>HSP and MKY</strong> and all issue types.
     * <p/>
     * So, by knowing both of these contexts, we get more information about the query's clauses. The full context tells us more
     * about what results to expect, but at the same time can hide information about what was originally specified in the
     * query. The simple context gives us an easy way to figure out what project and issue types were explicitly specified
     * in the query. This is useful for testing fitness in the filter form.
     *
     * @since v4.0
     */
    public static class ContextResult
    {
        private final ClauseContext fullContext;
        private final ClauseContext simpleContext;

        public ContextResult(final ClauseContext fullContext, final ClauseContext simpleContext)
        {
            this.fullContext = fullContext;
            this.simpleContext = simpleContext;
        }

        public ClauseContext getFullContext()
        {
            return fullContext;
        }

        public ClauseContext getSimpleContext()
        {
            return simpleContext;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            final ContextResult that = (ContextResult) o;

            if (simpleContext != null ? !simpleContext.equals(that.simpleContext) : that.simpleContext != null)
            {
                return false;
            }
            if (fullContext != null ? !fullContext.equals(that.fullContext) : that.fullContext != null)
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = fullContext != null ? fullContext.hashCode() : 0;
            result = 31 * result + (simpleContext != null ? simpleContext.hashCode() : 0);
            return result;
        }

        @Override
        public String toString()
        {
            return String.format("[Complex: %s, Simple: %s]", fullContext, simpleContext);
        }
    }
}
