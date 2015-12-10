package com.atlassian.jira.jql.permission;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;

import java.util.List;

/**
 * An abstract implementation of a {@link com.atlassian.jira.jql.permission.DefaultOperandSanitisingVisitor} that utilises
 * a {@link com.atlassian.jira.jql.permission.LiteralSanitiser} to convert a {@link com.atlassian.query.operand.SingleValueOperand}
 * into its sanitised form.
 *
 * @since v4.0
 */
abstract class AbstractLiteralSanitisingVisitor extends DefaultOperandSanitisingVisitor
{
    private final JqlOperandResolver jqlOperandResolver;
    private final User user;
    private final TerminalClause terminalClause;

    AbstractLiteralSanitisingVisitor(final JqlOperandResolver jqlOperandResolver, final User user, final TerminalClause terminalClause)
    {
        super(jqlOperandResolver, user);
        this.jqlOperandResolver = jqlOperandResolver;
        this.user = user;
        this.terminalClause = terminalClause;
    }

    /**
     * Sanitise the values stored in {@link com.atlassian.query.operand.SingleValueOperand}s.
     * Utilise the {@link com.atlassian.jira.jql.permission.LiteralSanitiser} to do the hard work.
     *
     * @param singleValueOperand the operand being visited.
     * @return the sanitised operand; never null.
     */
    public Operand visit(final SingleValueOperand singleValueOperand)
    {
        // short circuit if we can't get any values back
        final List<QueryLiteral> literals = jqlOperandResolver.getValues(user, singleValueOperand, terminalClause);
        if (literals == null)
        {
            return singleValueOperand;
        }

        final ProjectLiteralSanitiser.Result result = createLiteralSanitiser().sanitiseLiterals(literals);

        if (!result.isModified())
        {
            return singleValueOperand;
        }
        else
        {
            final List<QueryLiteral> resultantLiterals = result.getLiterals();
            if (resultantLiterals.size() == 1)
            {
                return new SingleValueOperand(resultantLiterals.get(0));
            }
            else
            {
                return MultiValueOperand.ofQueryLiterals(resultantLiterals);
            }
        }
    }

    /**
     * Creates an instance of a {@link com.atlassian.jira.jql.permission.LiteralSanitiser} that knows how to sanitise the
     * literals expected of this {@link com.atlassian.jira.jql.permission.DefaultOperandSanitisingVisitor}.
     *
     * @return a new instance of the sanitiser; never null.
     */
    protected abstract LiteralSanitiser createLiteralSanitiser();
}
