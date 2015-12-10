package com.atlassian.jira.jql.context;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.util.JqlIssueSupport;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.jira.util.lang.Pair;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operator.Operator;
import com.atlassian.util.profiling.UtilTimerStack;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A context factory for issue keys and id clauses. The project and issue type is taken from the issues. If the operator
 * is a negating operator then this returns a context with {@link com.atlassian.jira.jql.context.AllProjectsContext} and
 * {@link com.atlassian.jira.jql.context.AllIssueTypesContext}.
 *
 * @since v4.0
 */
public class IssueIdClauseContextFactory implements ClauseContextFactory
{
    private static final int BATCH_MAX_SIZE = 1000;

    private final JqlIssueSupport jqlIssueSupport;
    private final JqlOperandResolver jqlOperandResolver;
    private final Set<Operator> supportedOperators;

    IssueIdClauseContextFactory(final JqlIssueSupport jqlIssueSupport, final JqlOperandResolver jqlOperandResolver,
                                final Set<Operator> supportedOperators)
    {
        this.jqlIssueSupport = jqlIssueSupport;
        this.jqlOperandResolver = jqlOperandResolver;
        this.supportedOperators = supportedOperators;
    }

    private Set<Pair<Long, String>> getProjectIssueTypes(final User searcher, final List<QueryLiteral> literals)
    {
        Set<Pair<Long, String>> projectIssueTypes = new HashSet<Pair<Long, String>>();
        for (QueryLiteral literal : literals)
        {
            // if we have an empty literal, the Global context will not impact on any existing contexts, so do nothing
            if (!literal.isEmpty())
            {
                for (Issue issue : getIssues(searcher, literal))
                {
                    projectIssueTypes
                            .add(Pair.of(issue.getProjectObject().getId(), issue.getIssueTypeObject().getId()));
                }
            }
        }
        return projectIssueTypes;
    }

    private Set<Pair<Long, String>> getProjectIssueTypesBatch(final User searcher, final List<QueryLiteral> literals)
    {
        UtilTimerStack.push("IssueIdClauseContextFactory.getProjectIssueTypesBatch()");
        Set<Long> numericLiterals = new HashSet<Long>();
        Set<String> stringLiterals = new HashSet<String>();
        for (QueryLiteral literal : literals)
        {
            if (!literal.isEmpty())
            {
                if (literal.getLongValue() != null)
                {
                    numericLiterals.add(literal.getLongValue());
                }
                else if (literal.getStringValue() != null)
                {
                    stringLiterals.add(literal.getStringValue());
                }
            }
        }
        Set<Pair<Long, String>> projectIssueTypes = new HashSet<Pair<Long, String>>();
        if (numericLiterals.size() > 0)
            projectIssueTypes.addAll(jqlIssueSupport.getProjectIssueTypePairsByIds(numericLiterals));
        if (stringLiterals.size() > 0)
            projectIssueTypes.addAll(jqlIssueSupport.getProjectIssueTypePairsByKeys(stringLiterals));
        UtilTimerStack.pop("IssueIdClauseContextFactory.getProjectIssueTypesBatch()");
        return projectIssueTypes;
    }

    public ClauseContext getClauseContext(final User searcher, final TerminalClause terminalClause)
    {
        final Operator operator = terminalClause.getOperator();
        if (!handlesOperator(operator) || isNegationOperator(operator) || isEmptyOperator(operator))
        {
            return ClauseContextImpl.createGlobalClauseContext();
        }
        else
        {
            UtilTimerStack.push("IssueIdClauseContextFactory.getClauseContext() - get literals");
            final List<QueryLiteral> literals =
                    jqlOperandResolver.getValues(searcher, terminalClause.getOperand(), terminalClause);
            UtilTimerStack.pop("IssueIdClauseContextFactory.getClauseContext() - get literals");
            if (literals == null || literals.isEmpty())
            {
                return ClauseContextImpl.createGlobalClauseContext();
            }

            UtilTimerStack.push("IssueIdClauseContextFactory.getClauseContext() - creating context");
            final Set<ProjectIssueTypeContext> contexts = new HashSet<ProjectIssueTypeContext>();
            int batches = literals.size() / BATCH_MAX_SIZE + 1;
            for (int batchIndex = 0; batchIndex < batches; batchIndex++)
            {
                List<QueryLiteral> literalsBatch = literals.subList(batchIndex * BATCH_MAX_SIZE,
                        Math.min((batchIndex + 1) * BATCH_MAX_SIZE, literals.size()));
                Set<Pair<Long, String>> projectIssueTypes = getProjectIssueTypesBatch(searcher, literalsBatch);
                for (Pair<Long, String> projectIssueType : projectIssueTypes)
                {
                    if (isEqualsOperator(operator))
                    {
                        contexts.add(new ProjectIssueTypeContextImpl(new ProjectContextImpl(projectIssueType.first()),
                                new IssueTypeContextImpl(projectIssueType.second())));
                    }
                    else if (isRelationalOperator(operator))
                    {
                        contexts.add(new ProjectIssueTypeContextImpl(new ProjectContextImpl(projectIssueType.first()),
                                AllIssueTypesContext.INSTANCE));
                    }
                }
            }
            UtilTimerStack.pop("IssueIdClauseContextFactory.getClauseContext() - creating context");
            return contexts.isEmpty() ? ClauseContextImpl.createGlobalClauseContext() : new ClauseContextImpl(contexts);
        }
    }

    private boolean isEmptyOperator(final Operator operator)
    {
        return OperatorClasses.EMPTY_ONLY_OPERATORS.contains(operator);
    }

    private boolean isNegationOperator(final Operator operator)
    {
        return OperatorClasses.NEGATIVE_EQUALITY_OPERATORS.contains(operator);
    }

    private boolean isRelationalOperator(final Operator operator)
    {
        return OperatorClasses.RELATIONAL_ONLY_OPERATORS.contains(operator);
    }

    private boolean isEqualsOperator(final Operator operator)
    {
        return operator == Operator.EQUALS || operator == Operator.IN;
    }

    private boolean handlesOperator(final Operator operator)
    {
        return supportedOperators.contains(operator);
    }

    /**
     * @param searcher the user performing the search
     * @param literal  the query literal; must not be null or the empty literal
     * @return a collection of issues represented by the literal which the user can see; never null.
     */
    private Collection<Issue> getIssues(User searcher, QueryLiteral literal)
    {
        notNull("literal", literal);
        final Issue issue;
        if (literal.getStringValue() != null)
        {
            issue = jqlIssueSupport.getIssue(literal.getStringValue(), ApplicationUsers.from(searcher));
        }
        else if (literal.getLongValue() != null)
        {
            issue = jqlIssueSupport.getIssue(literal.getLongValue(), searcher);
        }
        else
        {
            throw new IllegalStateException("Invalid query literal");
        }

        if (issue != null)
        {
            return Collections.singleton(issue);
        }
        else
        {
            return Collections.emptySet();
        }
    }

    @InjectableComponent
    public static class Factory
    {
        private final JqlIssueSupport issueSupport;
        private final JqlOperandResolver operandResolver;

        public Factory(final JqlIssueSupport issueSupport, final JqlOperandResolver operandResolver)
        {
            this.issueSupport = issueSupport;
            this.operandResolver = operandResolver;
        }

        public IssueIdClauseContextFactory create(final Set<Operator> supportedOperators)
        {
            return new IssueIdClauseContextFactory(issueSupport, operandResolver, supportedOperators);
        }
    }
}
