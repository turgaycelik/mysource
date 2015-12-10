package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.jira.jql.util.JqlDateSupportImpl;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.Operand;

import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A clause validator for arbitrary date fields
 *
 * @since v4.0
 */
class DateValueValidator
{
    private final JqlOperandResolver operandResolver;
    private final JqlDateSupport support;

    DateValueValidator(final JqlOperandResolver operandResolver, final JqlDateSupport support)
    {
        this.operandResolver = notNull("operandResolver", operandResolver);
        this.support = notNull("support", support);
    }

    DateValueValidator(final JqlOperandResolver operandResolver, TimeZoneManager timeZoneManager)
    {
        this(operandResolver, new JqlDateSupportImpl(timeZoneManager));
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
                    final boolean result = support.validate(str);
                    if (!result)
                    {
                        String msg;
                        if (operandResolver.isFunctionOperand(value.getSourceOperand()))
                        {
                            msg = i18n.getText("jira.jql.clause.date.format.invalid.from.func", fieldName, value.getSourceOperand().getName());
                        }
                        else
                        {
                            msg = i18n.getText("jira.jql.clause.date.format.invalid", str, fieldName);
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
        return new I18nBean(user);
    }
    ///CLOVER:ON
}
