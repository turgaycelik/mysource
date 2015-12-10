package com.atlassian.jira.jql.permission;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.jql.ClauseHandler;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.util.NonInjectableComponent;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.ChangedClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.ClauseVisitor;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.clause.WasClause;
import com.atlassian.query.clause.WasClauseImpl;
import com.atlassian.query.operand.Operand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A visitor for converting a clause into its sanitised form.
 *
 * @since v4.0
 */
@NonInjectableComponent
public class ClauseSanitisingVisitor implements ClauseVisitor<Clause>
{
    private final SearchHandlerManager searchHandlerManager;
    private final JqlOperandResolver jqlOperandResolver;
    private final User user;

    public ClauseSanitisingVisitor(final SearchHandlerManager searchHandlerManager, final JqlOperandResolver jqlOperandResolver, final User user)
    {
        this.searchHandlerManager = notNull("searchHandlerManager", searchHandlerManager);
        this.jqlOperandResolver = notNull("jqlOperandResolver", jqlOperandResolver);
        this.user = user;
    }

    public Clause visit(final AndClause andClause)
    {
        return new AndClause(sanitiseChildren(andClause));
    }

    public Clause visit(final NotClause notClause)
    {
        return new NotClause(notClause.getSubClause().accept(this));
    }

    public Clause visit(final OrClause orClause)
    {
        return new OrClause(sanitiseChildren(orClause));
    }

    public Clause visit(TerminalClause clause)
    {
        // if we don't get any handlers back, this means the user does not have permission to use the clause, so just
        // return the input
        final Collection<ClauseHandler> handlers = searchHandlerManager.getClauseHandler(user, clause.getName());
        if (handlers.isEmpty())
        {
            return clause;
        }

        // first, we want to sanitise all operands with the DefaultOperandSanitisingVisitor, as it uses a strategy that
        // should be applied across all fields
        clause = sanitiseOperands(clause);

        // we only care about unique sanitised clauses, so use a set
        final Set<Clause> newClauses = new LinkedHashSet<Clause>();
        for (ClauseHandler clauseHandler : handlers)
        {
            newClauses.add(clauseHandler.getPermissionHandler().sanitise(user, clause));
        }

        return newClauses.size() == 1 ? newClauses.iterator().next() : new OrClause(newClauses);
    }

    @Override
    public Clause visit(WasClause clause)
    {
        final Operand originalOperand = clause.getOperand();
        final Operand sanitisedOperand = sanitisedOperand(originalOperand);
        if (originalOperand.equals(sanitisedOperand))
        {
            return clause;
        }
        else
        {
            return new WasClauseImpl(clause.getField(), clause.getOperator(), sanitisedOperand,clause.getPredicate());
        }
    }

    @Override
    public Clause visit(ChangedClause clause)
    {
        // There are no Operands to sanitize
        return clause;
    }

    /**
     * Important note: we are making a big assumption here that the {@link com.atlassian.jira.jql.permission.DefaultOperandSanitisingVisitor}
     * will always return the same kind of operand back after sanitising. This is because it only mutates function arguments
     * and multi value operands that contain function operands. In either case, the multiplicity of the operand does not
     * change after sanitising. Because of this, we blindly reuse the original operator from the input clause.
     *
     * If this assumption ever changes, we will need to revisit this code.
     *
     * @param clause the clause to sanitise
     * @return the sanitised clause; never null.
     */
    TerminalClause sanitiseOperands(final TerminalClause clause)
    {
        final Operand originalOperand = clause.getOperand();
        final Operand sanitisedOperand = sanitisedOperand(originalOperand);
        if (originalOperand.equals(sanitisedOperand))
        {
            return clause;
        }
        else
        {
            return new TerminalClauseImpl(clause.getName(), clause.getOperator(), sanitisedOperand);
        }
    }

    private Operand sanitisedOperand(Operand originalOperand)
    {
        final DefaultOperandSanitisingVisitor visitor = createOperandVisitor(user);

        return originalOperand.accept(visitor);
    }

    /// CLOVER:OFF
    DefaultOperandSanitisingVisitor createOperandVisitor(final User user)
    {
        return new DefaultOperandSanitisingVisitor(jqlOperandResolver, user);
    }
    /// CLOVER:ON

    List<Clause> sanitiseChildren(final Clause parentClause)
    {
        List<Clause> newClauses = new ArrayList<Clause>(parentClause.getClauses().size());
        for (Clause clause : parentClause.getClauses())
        {
            newClauses.add(clause.accept(this));
        }
        return newClauses;
    }
}
