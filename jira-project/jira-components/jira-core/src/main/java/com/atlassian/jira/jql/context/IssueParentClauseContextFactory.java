package com.atlassian.jira.jql.context;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.util.JqlIssueSupport;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operator.Operator;
import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A context factory for issue parent clauses. The project and issue type is taken from the issues' sub tasks. If the
 * operator is a negating operator then this returns a context with {@link AllProjectsContext}
 * and {@link AllIssueTypesContext}.
 *
 * Note: this is sort of expensive to calculate, but this should never really be used because there is no searcher
 * for the issue parent clause.
 *
 * @since v4.0
 */
@InjectableComponent
public class IssueParentClauseContextFactory implements ClauseContextFactory
{
    private final JqlIssueSupport jqlIssueSupport;
    private final JqlOperandResolver jqlOperandResolver;
    private final SubTaskManager subTaskManager;

    public IssueParentClauseContextFactory(final JqlIssueSupport jqlIssueSupport, final JqlOperandResolver jqlOperandResolver, final SubTaskManager subTaskManager)
    {
        this.jqlIssueSupport = jqlIssueSupport;
        this.jqlOperandResolver = jqlOperandResolver;
        this.subTaskManager = subTaskManager;
    }

    public ClauseContext getClauseContext(final User searcher, final TerminalClause terminalClause)
    {
        final Operator operator = terminalClause.getOperator();

        if (!subTaskManager.isSubTasksEnabled())
        {
            return ClauseContextImpl.createGlobalClauseContext();
        }

        if (!handlesOperator(operator) || isNegationOperator(operator))
        {
            return ClauseContextImpl.createGlobalClauseContext();
        }

        final Set<ProjectIssueTypeContext> contexts = new HashSet<ProjectIssueTypeContext>();
        final List<QueryLiteral> literals = jqlOperandResolver.getValues(searcher, terminalClause.getOperand(), terminalClause);
        if (literals != null)
        {
            for (QueryLiteral literal : literals)
            {
                // if we have an empty literal, the Global context will not impact on any existing contexts, so do nothing
                if (!literal.isEmpty())
                {
                    for (Issue issue : getSubTasks(searcher, literal))
                    {
                        contexts.add(new ProjectIssueTypeContextImpl(new ProjectContextImpl(issue.getProjectObject().getId()),
                                new IssueTypeContextImpl(issue.getIssueTypeObject().getId())));
                    }
                }
            }
        }
        return contexts.isEmpty() ? ClauseContextImpl.createGlobalClauseContext() : new ClauseContextImpl(contexts);
    }

    boolean isNegationOperator(final Operator operator)
    {
        return operator == Operator.NOT_EQUALS || operator == Operator.NOT_IN;
    }

    boolean handlesOperator(final Operator operator)
    {
        return OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY.contains(operator);
    }

    /**
     * Returns the sub tasks of the issues specified by the query literal, because this is the "issue parent" clause.
     *
     * @param searcher the user performing the search
     * @param literal the query literal; must not be null or the empty literal.
     * @return a collection of issues representing sub tasks
     */
    Collection<Issue> getSubTasks(User searcher, QueryLiteral literal)
    {
        notNull("literal", literal);
        final Issue parent;
        if (literal.getStringValue() != null)
        {
            parent = jqlIssueSupport.getIssue(literal.getStringValue(), ApplicationUsers.from(searcher));
        }
        else if (literal.getLongValue() != null)
        {
            parent = jqlIssueSupport.getIssue(literal.getLongValue(), ApplicationUsers.from(searcher));
        }
        else
        {
            throw new IllegalStateException("Invalid query literal");
        }
        return parent != null ? ImmutableList.copyOf(parent.getSubTaskObjects()) : Collections.<Issue>emptyList();
    }
}