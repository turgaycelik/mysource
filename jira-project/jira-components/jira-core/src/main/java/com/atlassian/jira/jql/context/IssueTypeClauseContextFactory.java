package com.atlassian.jira.jql.context;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.resolver.IssueConstantInfoResolver;
import com.atlassian.jira.jql.resolver.IssueTypeResolver;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operator.Operator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Generates a {@link ClauseContext} based on the issue type values and the project they are visible in
 *
 * @since v4.0
 */
public class IssueTypeClauseContextFactory implements ClauseContextFactory
{
    private final JqlOperandResolver jqlOperandResolver;
    private final IssueConstantInfoResolver<IssueType> resolver;
    private final ConstantsManager constantsManager;

    public IssueTypeClauseContextFactory(IssueTypeResolver issueTypeResolver, JqlOperandResolver jqlOperandResolver,
            final ConstantsManager constantsManager)
    {
        this.constantsManager = notNull("constantsManager", constantsManager);
        this.resolver = new IssueConstantInfoResolver<IssueType>(notNull("issueTypeResolver", issueTypeResolver));
        this.jqlOperandResolver = notNull("jqlOperandResolver", jqlOperandResolver);
    }

    public ClauseContext getClauseContext(final User searcher, final TerminalClause terminalClause)
    {
        final Operator operator = terminalClause.getOperator();
        if (!handlesOperator(operator))
        {
            return ClauseContextImpl.createGlobalClauseContext();
        }

        final List<QueryLiteral> values = jqlOperandResolver.getValues(searcher, terminalClause.getOperand(), terminalClause);
        Set<String> issueTypeIds = new HashSet<String>();
        if (values != null)
        {
            for (QueryLiteral value : values)
            {
                // if we have an empty literal, the Global context will not impact on any existing contexts, so do nothing
                if (!value.isEmpty())
                {
                    issueTypeIds.addAll(getIds(value));
                }
            }
        }

        if (!issueTypeIds.isEmpty() && isNegationOperator(operator))
        {
            final Set<String> allIssueTypes = new HashSet<String>(constantsManager.getAllIssueTypeIds());
            allIssueTypes.removeAll(issueTypeIds);
            issueTypeIds = allIssueTypes;
        }

        if (issueTypeIds.isEmpty())
        {
            return ClauseContextImpl.createGlobalClauseContext();
        }

        final Set<ProjectIssueTypeContext> contexts = new HashSet<ProjectIssueTypeContext>();
        for (String issueTypeId : issueTypeIds)
        {
            contexts.add(new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, new IssueTypeContextImpl(issueTypeId)));
        }

        return new ClauseContextImpl(contexts);
    }

    private boolean isNegationOperator(final Operator operator)
    {
        return OperatorClasses.NEGATIVE_EQUALITY_OPERATORS.contains(operator);
    }

    private boolean handlesOperator(final Operator operator)
    {
        return OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY.contains(operator);
    }

    /**
     * @param value the query literal; must not be null or the empty literal.
     * @return a list of ids of issue types represented by this literal; never null.
     */
    List<String> getIds(final QueryLiteral value)
    {
        if (value.getStringValue() != null)
        {
            return resolver.getIndexedValues(value.getStringValue());
        }
        else if (value.getLongValue() != null)
        {
            return resolver.getIndexedValues(value.getLongValue());
        }
        else
        {
            throw new IllegalStateException("Invalid query literal.");
        }
    }
}
