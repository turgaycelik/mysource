package com.atlassian.jira.jql.context;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.resolver.IndexInfoResolver;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operator.Operator;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * An abstract class for generating the context for version and components of projects.
 *
 * @since v4.0
 */
public abstract class AbstractProjectAttributeClauseContextFactory<T> implements ClauseContextFactory
{
    private final IndexInfoResolver<T> indexInfoResolver;
    private final JqlOperandResolver jqlOperandResolver;
    private final PermissionManager permissionManager;

    protected AbstractProjectAttributeClauseContextFactory(final IndexInfoResolver<T> indexInfoResolver,
            final JqlOperandResolver jqlOperandResolver, final PermissionManager permissionManager)
    {
        this.permissionManager = notNull("permissionManager", permissionManager);
        this.jqlOperandResolver = notNull("jqlOperandResolver", jqlOperandResolver);
        this.indexInfoResolver = notNull("indexInfoResolver", indexInfoResolver);
    }

    public ClauseContext getClauseContext(final User searcher, final TerminalClause terminalClause)
    {
        if (jqlOperandResolver.isEmptyOperand(terminalClause.getOperand()))
        {
            return ClauseContextImpl.createGlobalClauseContext();
        }
        else
        {
            final ClauseContext context = getContextFromClause(searcher, terminalClause);
            if (context.getContexts().isEmpty())
            {
                return ClauseContextImpl.createGlobalClauseContext();
            }
            else
            {
                return context;
            }
        }
    }

    abstract ClauseContext getContextFromClause(final User searcher, final TerminalClause terminalClause);

    Set<ProjectIssueTypeContext> getContextsForProject(final User searcher, final Project project)
    {
        if (project == null || !permissionManager.hasPermission(Permissions.BROWSE, project, searcher))
        {
            return Collections.emptySet();
        }
        else
        {
            return Collections.<ProjectIssueTypeContext>singleton(new ProjectIssueTypeContextImpl(new ProjectContextImpl(project.getId()), AllIssueTypesContext.INSTANCE));
        }
    }

    boolean isNegationOperator(final Operator operator)
    {
        return operator == Operator.NOT_EQUALS || operator == Operator.NOT_IN || operator == Operator.IS_NOT;
    }

    boolean isRelationalOperator(final Operator operator)
    {
        return OperatorClasses.RELATIONAL_ONLY_OPERATORS.contains(operator);
    }

    boolean isEqualityOperator(final Operator operator)
    {
        return OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY.contains(operator);
    }

    /**
     * @param literal the query literal; must not be null or the empty literal
     * @return the ids representing the index values of this literal; never null.
     */
    List<Long> getIds(QueryLiteral literal)
    {
        notNull("literal", literal);

        List<String> ids;
        if (literal.getStringValue() != null)
        {
            ids = indexInfoResolver.getIndexedValues(literal.getStringValue());
        }
        else if (literal.getLongValue() != null)
        {
            ids = indexInfoResolver.getIndexedValues(literal.getLongValue());
        }
        else
        {
            throw new IllegalStateException("Invalid query literal");
        }

        return CollectionUtil.transform(ids, new Function<String, Long>()
        {
            public Long get(final String input)
            {
                return Long.parseLong(input);
            }
        });
    }
}
