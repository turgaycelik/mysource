package com.atlassian.jira.issue.search.searchers.transformer;

import com.atlassian.jira.issue.search.ClauseNames;
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

import static com.atlassian.jira.util.dbc.Assertions.containsNoNulls;
import static com.atlassian.jira.util.dbc.Assertions.notBlank;
import static com.atlassian.jira.util.dbc.Assertions.notNull;


/**
 * A visitor records all the TerminalClauses that match a particular condition. The visitor records whteher or not all
 * the matched clauses are part of a standard navigator query. A standard navigator query is either a single terminal
 * clause or an and clause with terminal clauses as children. This visitor only checks that the matched nodes form part
 * of a standard query.
 *
 * @since 4.0.
 */
//TODO: The NamedTerminalClauseCollectingVisitor is almost this. Do we need to merge or do something similar?

public class SimpleNavigatorCollectorVisitor implements ClauseVisitor<Void>
{
    private final List<TerminalClause> clauses = new ArrayList<TerminalClause>();
    private final Set<String> clauseNames;

    protected boolean valid = true;
    protected boolean validPath = true;

    public SimpleNavigatorCollectorVisitor(final String clauseName)
    {
        this(Collections.singleton(notBlank("clauseName", clauseName)));
    }

    public SimpleNavigatorCollectorVisitor(final Set<String> clauseNames)
    {
        containsNoNulls("clauseNames", clauseNames);
        final TreeSet<String> names = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        if (clauseNames != null)
        {
            names.addAll(clauseNames);
        }
        this.clauseNames = Collections.unmodifiableSet(names);
    }

    public SimpleNavigatorCollectorVisitor(final ClauseNames clauseNames)
    {
        this(notNull("clauseNames", clauseNames).getJqlFieldNames());
    }

    public List<TerminalClause> getClauses()
    {
        return clauses;
    }

    public boolean isValid()
    {
        return valid;
    }

    public Void visit(final AndClause andClause)
    {
        for (Clause clause : andClause.getClauses())
        {
            clause.accept(this);
        }
        return null;
    }

    public Void visit(final NotClause notClause)
    {
        boolean oldValidPath = validPath;
        validPath = false;
        notClause.getSubClause().accept(this);
        validPath = oldValidPath;

        return null;
    }

    public Void visit(final OrClause orClause)
    {
        boolean oldValidPath = validPath;
        validPath = false;

        for (Clause clause : orClause.getClauses())
        {
            clause.accept(this);
        }

        validPath = oldValidPath;
        return null;
    }

    public Void visit(final TerminalClause terminalClause)
    {
        if (matches(terminalClause))
        {
            clauses.add(terminalClause);
            if (!validPath)
            {
                valid = false;
            }
        }

        return null;
    }

    // History searches do not participate in Simple views
    @Override
    public Void visit(WasClause clause)
    {
        return null;
    }

    // Changed searches do not participate in Simple views
    @Override
    public Void visit(ChangedClause clause)
    {
        return null;
    }


    //TODO: This probably need to be made protected or something to make this class extensible, maybe even incoprating
    //the NamedTerminalClauseCollectingVisitor here.
    private boolean matches(final TerminalClause terminalClause)
    {
        return clauseNames.contains(terminalClause.getName());
    }
}
