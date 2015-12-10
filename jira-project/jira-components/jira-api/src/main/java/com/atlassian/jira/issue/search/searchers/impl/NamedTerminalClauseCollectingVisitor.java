package com.atlassian.jira.issue.search.searchers.impl;

import com.atlassian.jira.issue.search.searchers.util.RecursiveClauseVisitor;
import com.atlassian.query.clause.ClauseVisitor;
import com.atlassian.query.clause.TerminalClause;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * <p>A {@link com.atlassian.jira.issue.search.searchers.util.RecursiveClauseVisitor} which collects
 * {@link com.atlassian.query.clause.TerminalClause}s that have the specified clause names.
 *
 * <p>Note: this visitor does not perform any structure checking. It simply collects all the clauses with the specified
 * names.
 *
 * @since v4.0
 */
public class NamedTerminalClauseCollectingVisitor extends RecursiveClauseVisitor implements ClauseVisitor<Void>
{
    private final Set<String> clauseNames;
    private final List<TerminalClause> namedClauses;

    public NamedTerminalClauseCollectingVisitor(String clauseName)
    {
        this(Collections.singleton(clauseName));
    }

    public NamedTerminalClauseCollectingVisitor(Collection<String> clauseNames)
    {
        final TreeSet<String> names = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        if (clauseNames != null)
        {
            names.addAll(clauseNames);
        }
        this.clauseNames = Collections.unmodifiableSet(names);
        this.namedClauses = new ArrayList<TerminalClause>();
    }

    public List<TerminalClause> getNamedClauses()
    {
        return namedClauses;
    }

    public boolean containsNamedClause()
    {
        return !namedClauses.isEmpty();
    }

    public Void visit(final TerminalClause clause)
    {
        if (clauseNames.contains(clause.getName()))
        {
            this.namedClauses.add(clause);
        }
        return null;
    }
}
