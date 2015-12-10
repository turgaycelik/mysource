package com.atlassian.jira.issue.search.optimizers;


import com.atlassian.query.Query;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.Operand;

import static com.atlassian.jira.plugin.jql.function.AllReleasedVersionsFunction.FUNCTION_RELEASED_VERSIONS;

/**
 * Class that optimizes releasedVersions() function in given query by trying to determine projects from query
 * and adding them as arguments to that function provided that no arguments are already specified.
 *
 */
public class ReleasedVersionsFunctionOptimizer
{
    public Query createOptimizedQuery(final Query query)
    {
        final VersionsFunctionOptimizer optimizer = new VersionsFunctionOptimizer(new ReleasedVersionsFunctionVisitorVisitor());
        return optimizer.createOptimizedQuery(query);
    }

    private static class ReleasedVersionsFunctionVisitorVisitor extends VersionsFunctionOptimizer.VersionsFunctionOptimizerVisitor
    {
        @Override
        public TerminalClause optimizeClause(final TerminalClause clause, final String[] arguments)
        {
            if (clause.getOperand().accept(new ReleasedVersionsFunctionVisitorVisitor()))
            {
                final Operand optimizedOperand = new FunctionOperand(FUNCTION_RELEASED_VERSIONS, arguments);
                return new TerminalClauseImpl(clause.getName(), clause.getOperator(), optimizedOperand);
            }

            return clause;
        }

        @Override
        public Boolean visit(final FunctionOperand functionOperand)
        {
            return FUNCTION_RELEASED_VERSIONS.equalsIgnoreCase(functionOperand.getName())
                    && functionOperand.getArgs().size() == 0;
        }
    }
}
