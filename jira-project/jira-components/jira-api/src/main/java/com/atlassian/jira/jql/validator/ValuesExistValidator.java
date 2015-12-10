package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.ListOrderedMessageSetImpl;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.Operand;

import java.util.List;

/**
 * A clause validator that can be used for multiple constant (priority, status, resolution) clause types.
 *
 */
abstract class ValuesExistValidator
{
    private final JqlOperandResolver operandResolver;
    private final I18nHelper.BeanFactory beanFactory;
    private final MessageSet.Level level;


    ValuesExistValidator(final JqlOperandResolver operandResolver, I18nHelper.BeanFactory beanFactory)
    {
        this(operandResolver, beanFactory, MessageSet.Level.ERROR);
    }
    
    ValuesExistValidator(final JqlOperandResolver operandResolver, I18nHelper.BeanFactory beanFactory, MessageSet.Level level)
    {
        this.beanFactory = Assertions.notNull("beanFactory", beanFactory);
        this.operandResolver = Assertions.notNull("operandResolver", operandResolver);
        this.level = level;
    }

    MessageSet validate(final User searcher, TerminalClause terminalClause)
    {
        final Operand operand = terminalClause.getOperand();
        final String fieldName = terminalClause.getName();

        MessageSet messages = new ListOrderedMessageSetImpl();

        if (operandResolver.isValidOperand(operand))
        {
            // visit every query literal and determine lookup failures
            final List<QueryLiteral> rawValues = operandResolver.getValues(searcher, operand, terminalClause);
            for (QueryLiteral rawValue : rawValues)
            {
                if (rawValue.getStringValue() != null)
                {
                    if (!stringValueExists(searcher, rawValue.getStringValue()))
                    {
                        if (operandResolver.isFunctionOperand(rawValue.getSourceOperand()))
                        {
                            messages.addMessage(this.level, getI18n(searcher).getText("jira.jql.clause.no.value.for.name.from.function", rawValue.getSourceOperand().getName(), fieldName));
                        }
                        else
                        {
                            messages.addMessage(this.level, getI18n(searcher).getText("jira.jql.clause.no.value.for.name", fieldName, rawValue.getStringValue()));
                        }
                    }
                }
                else if (rawValue.getLongValue() != null)
                {
                    if (!longValueExist(searcher, rawValue.getLongValue()))
                    {
                        if (operandResolver.isFunctionOperand(rawValue.getSourceOperand()))
                        {
                            messages.addMessage(this.level, getI18n(searcher).getText("jira.jql.clause.no.value.for.name.from.function", rawValue.getSourceOperand().getName(), fieldName));
                        }
                        else
                        {
                            messages.addMessage(this.level, getI18n(searcher).getText("jira.jql.clause.no.value.for.id", fieldName, rawValue.getLongValue().toString()));
                        }
                    }
                }
            }
        }

        return messages;
    }

    abstract boolean stringValueExists(final User searcher, String value);

    abstract boolean longValueExist(final User searcher, Long value);

    ///CLOVER:OFF
    I18nHelper getI18n(User user)
    {
        return beanFactory.getInstance(user);
    }
    ///CLOVER:ON
}
