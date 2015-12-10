package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.Operand;

import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Validates integer values.
 *
 * @since v4.0
 */
class IntegerValueValidator
{
    private final JqlOperandResolver operandResolver;

    IntegerValueValidator(final JqlOperandResolver operandResolver)
    {
        this.operandResolver = notNull("operandResolver", operandResolver);
    }

    MessageSet validate(final User searcher, final TerminalClause terminalClause)
    {
        notNull("terminalClause", terminalClause);

        final Operand operand = terminalClause.getOperand();
        final MessageSet messages = new MessageSetImpl();

        if (operandResolver.isValidOperand(operand))
        {
            final I18nHelper i18n = getI18n(searcher);
            final List<QueryLiteral> values = operandResolver.getValues(searcher, operand, terminalClause);
            final String fieldName = terminalClause.getName();

            for (QueryLiteral value : values)
            {
                // we are ok with longValues
                boolean isValid = true;

                final String str = value.getStringValue();
                if (str != null)
                {
                    try
                    {
                        Integer.parseInt(str);
                    }
                    catch (NumberFormatException e)
                    {
                        isValid = false;
                    }
                }

                if (!isValid)
                {
                    String msg;
                    if (operandResolver.isFunctionOperand(value.getSourceOperand()))
                    {
                        msg = i18n.getText("jira.jql.clause.integer.format.invalid.from.func", value.getSourceOperand().getName(), fieldName);
                    }
                    else
                    {
                        msg = i18n.getText("jira.jql.clause.integer.format.invalid", str, fieldName);
                    }
                    messages.addErrorMessage(msg);
                }
            }
        }
        return messages;
    }

    ///CLOVER:OFF
    I18nHelper getI18n(User user)
    {
        return new I18nBean(user);
    }
    ///CLOVER:ON
}
