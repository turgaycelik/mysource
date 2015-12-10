package com.atlassian.jira.issue.search.util;

import com.atlassian.jira.util.Function;
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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.atlassian.jira.util.collect.CollectionUtil.transform;

public class RedundantClausesQueryOptimizer implements QueryOptimizer
{
    public Query optimizeQuery(final Query query)
    {
        final Clause whereClause = query.getWhereClause();
        if (whereClause == null)
        {
            return query;
        }

        return new QueryImpl(whereClause.accept(new ClauseOptimizerVisitor()), query.getOrderByClause(), null);
    }

    private static class ClauseOptimizerVisitor implements ClauseVisitor<Clause>
    {
        private static final Function<Clause, Clause> optimizer = new Function<Clause, Clause>()
        {
            public Clause get(final Clause clause)
            {
                return clause.accept(new ClauseOptimizerVisitor());
            }
        };

        Set<TerminalClause> uniqueClauses = new LinkedHashSet<TerminalClause>();

        public Clause visit(final AndClause andClause)
        {
            final List<Clause> subClauses = new ArrayList<Clause>();
            for (final Clause clause : andClause.getClauses())
            {
                if (clause instanceof TerminalClause)
                {
                    if (uniqueClauses.add((TerminalClause) clause))
                    {
                        subClauses.add(clause);
                    }
                }
                else if (clause instanceof AndClause)
                {
                    final Clause cl = clause.accept(this);
                    if (cl != null)
                    {
                        subClauses.add(cl);
                    }
                }
                else
                {
                    subClauses.add(optimizer.get(clause));
                }
            }
            return (subClauses.isEmpty()) ? null : (subClauses.size() == 1) ? subClauses.get(0) : new AndClause(subClauses);
        }

        public Clause visit(final NotClause notClause)
        {
            return new NotClause(optimizer.get(notClause.getSubClause()));
        }

        public Clause visit(final OrClause orClause)
        {
            return new OrClause(transform(orClause.getClauses(), optimizer));
        }

        public Clause visit(final TerminalClause clause)
        {
            return clause;
        }

        @Override
        public Clause visit(WasClause clause)
        {
            return clause;
        }

        @Override
        public Clause visit(ChangedClause clause)
        {
            return clause;
        }
    }
}