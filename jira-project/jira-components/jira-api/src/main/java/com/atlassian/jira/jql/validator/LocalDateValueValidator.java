package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.JqlLocalDateSupport;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.Operand;

import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A clause validator for LocalDate fields
 *
 * @since v4.4
 */
class LocalDateValueValidator
{
    private final JqlOperandResolver operandResolver;
    private final JqlLocalDateSupport localDateSupport;

    LocalDateValueValidator(final JqlOperandResolver operandResolver, final JqlLocalDateSupport support)
    {
        this.operandResolver = notNull("operandResolver", operandResolver);
        this.localDateSupport = notNull("support", support);
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

                final String str = value.getStringValue();
                if (str != null)
                {
                    final boolean result = localDateSupport.validate(str);
                    if (!result)
                    {
                        String msg;
                        if (operandResolver.isFunctionOperand(value.getSourceOperand()))
                        {
                            msg = i18n.getText("jira.jql.clause.local.date.format.invalid.from.func", fieldName, value.getSourceOperand().getName());
                        }
                        else
                        {
                            msg = i18n.getText("jira.jql.clause.local.date.format.invalid", str, fieldName);
                        }
                        messages.addErrorMessage(msg);
                    }
                }
            }
        }
        return messages;
    }

    ///CLOVER:OFF
    I18nHelper getI18n(User user)
    {
       return ComponentAccessor.getComponent(I18nHelper.BeanFactory.class).getInstance(user);
    }
    ///CLOVER:ON
}
