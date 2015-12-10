package com.atlassian.jira.issue.search;

import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.ChangedClause;
import com.atlassian.query.clause.ChangedClauseImpl;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.ClauseVisitor;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.clause.WasClause;
import com.atlassian.query.clause.WasClauseImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A visitor that will clone the tree it is visiting, renaming any terminal nodes whose name is a key in the
 * substitutions map with its value in the substitutions map. Note that terminal node names are case insensitive.
 *
 * @since v4.2
 */
public class ClauseRenamingCloningVisitor implements ClauseVisitor<Clause>
{
    private final Map<String, String> lowerCaseSubstitutions;

    public ClauseRenamingCloningVisitor(Map<String, String> substitutions)
    {
        notNull("substitutions", substitutions);

        this.lowerCaseSubstitutions = new HashMap<String, String>();
        for (Map.Entry<String, String> entry : substitutions.entrySet())
        {
            this.lowerCaseSubstitutions.put(entry.getKey().toLowerCase(), entry.getValue());
        }
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
        String lowerCaseName = clause.getName().toLowerCase();
        if (lowerCaseSubstitutions.containsKey(lowerCaseName))
        {
            return new TerminalClauseImpl(lowerCaseSubstitutions.get(lowerCaseName), clause.getOperator(), clause.getOperand());
        }
        else
        {
            return clause;
        }
    }

    @Override
    public Clause visit(WasClause clause)
    {
        return new WasClauseImpl(clause);
    }

    @Override
    public Clause visit(ChangedClause clause)
    {
        return new ChangedClauseImpl(clause);
    }


}
