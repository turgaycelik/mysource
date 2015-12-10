package com.atlassian.jira.issue.search.optimizers;


import java.util.Set;

import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.ChangedClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.ClauseVisitor;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.WasClause;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.OperandVisitor;
import com.atlassian.query.operand.SingleValueOperand;

import com.google.common.collect.ImmutableList;

/**
 * Abstract for optimizing unreleasedVersions() and releasedVersions() functions.
 */
public class VersionsFunctionOptimizer
{
    private final VersionsFunctionOptimizerVisitor versionsFunctionOptimizerVisitor;

    public VersionsFunctionOptimizer(final VersionsFunctionOptimizerVisitor versionsFunctionOptimizerVisitor)
    {
        this.versionsFunctionOptimizerVisitor = versionsFunctionOptimizerVisitor;
    }

    /**
     * Optimizes given query depending on previously provided versionsFunctionOptimizerVisitor.
     *
     * @param query to be optimized
     * @return new optimized query or passed query if optimization couldn't be performed.
     */
    public Query createOptimizedQuery(final Query query)
    {
        final DeterminedProjectsInQueryVisitor determinedProjectsInQueryVisitor = new DeterminedProjectsInQueryVisitor();
        final Clause whereClause = query.getWhereClause();

        if(whereClause != null && whereClause.accept(determinedProjectsInQueryVisitor))
        {
            final Set<String> determinedProjects = determinedProjectsInQueryVisitor.getDeterminedProjects();
            final Clause optimizedWhereClause = whereClause.accept(new ClauseRebuildingOptimizer(determinedProjects));

            return new QueryImpl(optimizedWhereClause, query.getOrderByClause(), null);
        }

        return query;
    }

    /**
     * Abstract for classes that specify which terminal clasuses should be rewritten and how.
     */
    abstract static class VersionsFunctionOptimizerVisitor implements OperandVisitor<Boolean>
    {
        @Override
        public Boolean visit(final EmptyOperand emptyOperand)
        {
            return false;
        }

        @Override
        public Boolean visit(final MultiValueOperand multiValueOperand)
        {
            return false;
        }

        @Override
        public Boolean visit(final SingleValueOperand singleValueOperand)
        {
            return false;
        }

        public abstract TerminalClause optimizeClause(TerminalClause clause, String[] arguments);
    }

    /**
     * Class for rebuilding query and optimizing its terminal clasues (specified by versionsFunctionOptimizerVisitor).
     */
    private class ClauseRebuildingOptimizer implements ClauseVisitor<Clause>
    {
        private final Set<String> projects;

        private ClauseRebuildingOptimizer(final Set<String> projects)
        {
            this.projects = projects;
        }

        public Clause visit(final AndClause andClause)
        {
            final ImmutableList.Builder<Clause> rebuiltClauses = ImmutableList.builder();
            for (final Clause clause : andClause.getClauses())
            {
                rebuiltClauses.add(clause.accept(this));
            }
            return new AndClause(rebuiltClauses.build());
        }

        public Clause visit(final NotClause notClause)
        {
            return new NotClause(notClause.getSubClause().accept(this));
        }

        public Clause visit(final OrClause orClause)
        {
            final ImmutableList.Builder<Clause> rebuiltClauses = ImmutableList.builder();
            for (final Clause clause : orClause.getClauses())
            {
                rebuiltClauses.add(clause.accept(this));
            }
            return new OrClause(rebuiltClauses.build());
        }

        public Clause visit(final TerminalClause clause)
        {
            if (clause.getOperand().accept(versionsFunctionOptimizerVisitor))
            {
                return versionsFunctionOptimizerVisitor.optimizeClause(clause, projects.toArray(new String[projects.size()]));
            }

            return clause;
        }

        @Override
        public Clause visit(final WasClause clause)
        {
            return clause;
        }

        @Override
        public Clause visit(final ChangedClause clause)
        {
            return clause;
        }
    }
}
