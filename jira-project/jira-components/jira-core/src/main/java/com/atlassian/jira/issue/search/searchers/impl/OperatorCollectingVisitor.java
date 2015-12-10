package com.atlassian.jira.issue.search.searchers.impl;

import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.ChangedClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.ClauseVisitor;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.WasClause;
import com.atlassian.query.operator.Operator;

import java.util.HashSet;
import java.util.Set;

public class OperatorCollectingVisitor implements ClauseVisitor<Void>
{
    private final Set<Operator> operators;

    public OperatorCollectingVisitor()
    {
        this.operators = new HashSet<Operator>();
    }

    public Set<Operator> getOperators()
    {
        return operators;
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
        return notClause.getSubClause().accept(this);
    }

    public Void visit(final OrClause orClause)
    {
        for (Clause clause : orClause.getClauses())
        {
            clause.accept(this);
        }
        return null;
    }

    public Void visit(final TerminalClause clause)
    {
        operators.add(clause.getOperator());
        return null;
    }

    @Override
    public Void visit(WasClause clause)
    {
        //need to add predicates when we implement them
        return null;
    }

    @Override
    public Void visit(ChangedClause clause)
    {
        return null;
    }
}