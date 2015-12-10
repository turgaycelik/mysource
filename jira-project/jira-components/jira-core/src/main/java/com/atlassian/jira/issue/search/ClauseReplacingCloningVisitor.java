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
import java.util.List;

/**
 * A visitor that will clone the tree it is visiting, replacing any terminal nodes with the same name as
 * a node in the provided substitutions list with the substitution node.
 *
 * @since v4.0
 */
public class ClauseReplacingCloningVisitor implements ClauseVisitor<Clause>
{
    private final List<TerminalClause> substitutions;

    public ClauseReplacingCloningVisitor(List<TerminalClause> substitutions)
    {
        this.substitutions = substitutions;
    }

    public Clause visit(final AndClause andClause)
    {
        List<Clause> subClauses = new ArrayList<Clause>();
        for (Clause clause : andClause.getClauses())
        {
            subClauses.add(clause.accept(this));
        }
        return new AndClause(subClauses);
    }

    public Clause visit(final NotClause notClause)
    {
        final Clause newSubClause = notClause.getSubClause().accept(this);
        return new NotClause(newSubClause);
    }

    public Clause visit(final OrClause orClause)
    {
        List<Clause> subClauses = new ArrayList<Clause>();
        for (Clause clause : orClause.getClauses())
        {
            subClauses.add(clause.accept(this));
        }
        return new OrClause(subClauses);
    }

    public Clause visit(final TerminalClause clause)
    {
        if (substitutions != null)
        {
            for (TerminalClause substitution : substitutions)
            {
                if (clause.getName().equalsIgnoreCase(substitution.getName()))
                {
                    return substitution;
                }
            }
        }
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
