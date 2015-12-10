package com.atlassian.jira.issue.search.optimizers;


import com.atlassian.query.Query;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.Operand;

import static com.atlassian.jira.plugin.jql.function.AllUnreleasedVersionsFunction.FUNCTION_UNRELEASED_VERSIONS;

/**
 * Class that optimizes unreleasedVersions() function in given query by trying to determine projects from query
 * and adding them as arguments to that function provided that no arguments are already specified.
 *
 */
public class UnreleasedVersionsFunctionOptimizer
{
    public Query createOptimizedQuery(final Query query)
    {
        final VersionsFunctionOptimizer optimizer = new VersionsFunctionOptimizer(new UnreleasedVersionsFunctionVisitorVisitor());
        return optimizer.createOptimizedQuery(query);
    }

    private static class UnreleasedVersionsFunctionVisitorVisitor extends VersionsFunctionOptimizer.VersionsFunctionOptimizerVisitor
    {
        @Override
        public TerminalClause optimizeClause(final TerminalClause clause, final String[] arguments)
        {
            if (clause.getOperand().accept(new UnreleasedVersionsFunctionVisitorVisitor()))
            {
                final Operand optimizedOperand = new FunctionOperand(FUNCTION_UNRELEASED_VERSIONS, arguments);
                return new TerminalClauseImpl(clause.getName(), clause.getOperator(), optimizedOperand);
            }

            return clause;
        }

        @Override
        public Boolean visit(final FunctionOperand functionOperand)
        {
            return FUNCTION_UNRELEASED_VERSIONS.equalsIgnoreCase(functionOperand.getName())
                    && functionOperand.getArgs().size() == 0;
        }
    }
}
