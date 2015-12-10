package com.atlassian.jira.issue.search;

import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.ChangedClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.ClauseVisitor;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.WasClause;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * A visitor that will remove the specified named clauses from the query tree.
 *
 * NOTE: it is possible to remove all clauses from a tree which will result in a null clause.
 *
 * @since v4.0
 */
public class ClauseRemovingCloningVisitor implements ClauseVisitor<Clause>
{
    private final Set<String> clauseNamesToRemove;

    public ClauseRemovingCloningVisitor(List<String> clauseNamesToRemove)
    {
        final TreeSet<String> names = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        if (clauseNamesToRemove != null)
        {
            names.addAll(clauseNamesToRemove);
        }
        this.clauseNamesToRemove = Collections.unmodifiableSet(names);
    }

    public Clause visit(final AndClause andClause)
    {
        List<Clause> subClauses = new ArrayList<Clause>();
        for (Clause clause : andClause.getClauses())
        {
            final Clause subClause = clause.accept(this);
            if (subClause != null)
            {
                subClauses.add(subClause);
            }
        }
        if (subClauses.isEmpty())
        {
            return null;
        }
        return new AndClause(subClauses);
    }

    public Clause visit(final NotClause notClause)
    {
        final Clause newSubClause = notClause.getSubClause().accept(this);
        if (newSubClause == null)
        {
            return null;
        }
        return new NotClause(newSubClause);
    }

    public Clause visit(final OrClause orClause)
    {
        List<Clause> subClauses = new ArrayList<Clause>();
        for (Clause clause : orClause.getClauses())
        {
            final Clause subClause = clause.accept(this);
            if (subClause != null)
            {
                subClauses.add(subClause);
            }
        }
        if (subClauses.isEmpty())
        {
            return null;
        }
        return new OrClause(subClauses);
    }

    public Clause visit(final TerminalClause clause)
    {
        return removeClause(clause);
    }

    @Override
    public Clause visit(WasClause clause)
    {
        return removeClause(clause);
    }

    @Override
    public Clause visit(ChangedClause clause)
    {
        return removeClause(clause);
    }

    private Clause removeClause(Clause clause)
    {
        if (clauseNamesToRemove != null)
        {
            if (clauseNamesToRemove.contains(clause.getName()))
            {
                return null;
            }
        }
        return clause;
    }
}
