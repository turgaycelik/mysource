package com.atlassian.jira.issue.search.searchers.transformer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.query.clause.ClauseVisitor;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operator.Operator;

/**
 * Checks that the query fits the expectations of the KA Search UI introduced in JIRA 5.2.
 *
 * @since v5.2
 */
class TextQueryValidatingVisitor extends SimpleNavigatorCollectorVisitor implements ClauseVisitor<Void>
{
    private boolean seenQueryClauses = false;
    private String clauseName;
    private TerminalClause terminal = null;

    public TextQueryValidatingVisitor(String clauseName)
    {
        super(clauseName);
        this.clauseName = Assertions.notNull("clauseName", clauseName);
    }

    public Void visit(final OrClause orClause)
    {
        validPath = false;
        return null;
    }

    public Void visit(final TerminalClause terminalClause)
    {
        super.visit(terminalClause);
        if (terminalClause.getName().equalsIgnoreCase(clauseName))
        {
            if ((seenQueryClauses) || !terminalClause.getOperator().equals(Operator.LIKE))
            {
                valid = false;
            }
            else
            {
                seenQueryClauses = true;
                if (!valid)
                {
                    terminal = null;
                }
                else
                {
                    terminal = terminalClause;
                }
            }
        }
        return null;
    }

    public String getTextTerminalValue(JqlOperandResolver operandResolver, User user)
    {
        if (terminal != null)
        {
            final QueryLiteral rawValue = operandResolver.getSingleValue(user, terminal.getOperand(), terminal);
            if (rawValue != null && !rawValue.isEmpty())
            {
                return rawValue.asString();
            }
        }
        return null;
    }
}
