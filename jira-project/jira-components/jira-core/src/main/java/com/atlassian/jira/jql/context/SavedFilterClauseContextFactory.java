package com.atlassian.jira.jql.context;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.resolver.SavedFilterResolver;
import com.atlassian.jira.jql.validator.SavedFilterCycleDetector;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operator.Operator;

import java.util.List;

/**
 * A Clause context factory for saved filters. If the operator is equality, then the query context
 * of the saved filter is generated and returned, otherwise the inversion is returned.
 *
 * @since v4.0
 */
@InjectableComponent
public class SavedFilterClauseContextFactory implements ClauseContextFactory
{
    private final SavedFilterResolver savedFilterResolver;
    private final JqlOperandResolver jqlOperandResolver;
    private final QueryContextVisitor.QueryContextVisitorFactory queryContextVisitorFactory;
    private final ContextSetUtil contextSetUtil;
    private final SavedFilterCycleDetector savedFilterCycleDetector;

    public SavedFilterClauseContextFactory(final SavedFilterResolver savedFilterResolver, final JqlOperandResolver jqlOperandResolver,
            final QueryContextVisitor.QueryContextVisitorFactory queryContextVisitorFactory, final ContextSetUtil contextSetUtil,
            final SavedFilterCycleDetector savedFilterCycleDetector)
    {
        this.savedFilterResolver = savedFilterResolver;
        this.jqlOperandResolver = jqlOperandResolver;
        this.queryContextVisitorFactory = queryContextVisitorFactory;
        this.contextSetUtil = contextSetUtil;
        this.savedFilterCycleDetector = savedFilterCycleDetector;
    }

    public ClauseContext getClauseContext(final User searcher, final TerminalClause terminalClause)
    {
        final Operator operator = terminalClause.getOperator();
        if (!handlesOperator(operator))
        {
            return ClauseContextImpl.createGlobalClauseContext();
        }

        ClauseContext context = null;

        final List<QueryLiteral> values = jqlOperandResolver.getValues(searcher, terminalClause.getOperand(), terminalClause);
        final List<SearchRequest> requests = savedFilterResolver.getSearchRequest(searcher, values);
        final boolean equalsOperator = isEqualsOperator(operator);
        for (SearchRequest request : requests)
        {
            if (savedFilterCycleDetector.containsSavedFilterReference(searcher, false, request, null))
            {
                // This is very bad we can not continue
                return ClauseContextImpl.createGlobalClauseContext();
            }
            final Query query = request.getQuery();
            if (equalsOperator)
            {
                Clause subClause = query.getWhereClause();
                final ClauseContext subContext = getSavedFilterContext(searcher, subClause);
                if (context == null)
                {
                    context = subContext;
                }
                else
                {
                    context = contextSetUtil.union(CollectionBuilder.newBuilder(context, subContext).asSet());
                }
            }
            else
            {
                Clause subClause = null;
                if (query.getWhereClause() != null)
                {
                    subClause = new NotClause(query.getWhereClause());
                }

                final ClauseContext subContext = getSavedFilterContext(searcher, subClause);
                if (context == null)
                {
                    context = subContext;
                }
                else
                {
                    context = contextSetUtil.intersect(CollectionBuilder.newBuilder(context, subContext).asSet());
                }
            }
        }

        return context == null ? ClauseContextImpl.createGlobalClauseContext() : context;
    }

    private boolean isEqualsOperator(final Operator operator)
    {
        return operator == Operator.EQUALS || operator == Operator.IN;
    }

    private boolean handlesOperator(final Operator operator)
    {
        return OperatorClasses.EQUALITY_OPERATORS.contains(operator);
    }

    ClauseContext getSavedFilterContext(User searcher, Clause subClause)
    {
        if (subClause == null)
        {
            return ClauseContextImpl.createGlobalClauseContext();
        }
        else
        {
            final QueryContextVisitor visitor = queryContextVisitorFactory.createVisitor(searcher);
            final ClauseContext fullContext = subClause.accept(visitor).getFullContext();
            if (fullContext == null)
            {
                return ClauseContextImpl.createGlobalClauseContext();
            }
            else
            {
                return fullContext;
            }
        }
    }
}
