package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operator.Operator;

/**
 * @since v4.0
 */
public class DefaultOperatorUsageValidator implements OperatorUsageValidator
{
    private final JqlOperandResolver operandResolver;
    private final I18nHelper.BeanFactory factory;

    public DefaultOperatorUsageValidator(JqlOperandResolver operandResolver, I18nHelper.BeanFactory factory)
    {
        this.factory = Assertions.notNull("factory", factory);
        this.operandResolver = Assertions.notNull("operandResolver", operandResolver);
    }

    public boolean check(User user, TerminalClause clause)
    {
        return validate(user, clause, null);
    }

    public MessageSet validate(final User searcher, final TerminalClause clause)
    {
        final MessageSet messages = new MessageSetImpl();
        validate(searcher, clause, messages);
        return messages;
    }

    private boolean validate(User user, TerminalClause clause, MessageSet set)
    {
        boolean valid = true;
        final Operand operand = clause.getOperand();
        if (operandResolver.isValidOperand(operand))
        {
            final Operator operator = clause.getOperator();
            // Check some global rules
            final boolean isList = operandResolver.isListOperand(operand);
            if (isList)
            {
                if (!OperatorClasses.LIST_ONLY_OPERATORS.contains(operator))
                {
                    valid = false;
                    addError(user, set, "jira.jql.operator.usage.not.support.list", operator.getDisplayString(), operand.getDisplayString(), clause.getName());
                }
            }
            else
            {
                if (OperatorClasses.LIST_ONLY_OPERATORS.contains(operator))
                {
                    valid = false;
                    addError(user, set, "jira.jql.operator.usage.not.support.non.list", operator.getDisplayString(), operand.getDisplayString(), clause.getName());
                }
            }

            if (operandResolver.isEmptyOperand(operand))
            {
                if (!OperatorClasses.EMPTY_OPERATORS.contains(operator))
                {
                    valid = false;
                    addError(user, set, "jira.jql.operator.usage.not.support.empty", operator.getDisplayString(), operand.getDisplayString(), clause.getName());
                }
            }
            else
            {
                if (OperatorClasses.EMPTY_ONLY_OPERATORS.contains(operator))
                {
                    valid = false;
                    addError(user, set, "jira.jql.operator.usage.is.only.supports.empty", operator.getDisplayString(), clause.getName());
                }
            }
        }
        
        return valid;
    }

    private void addError(User searcher, MessageSet messageset, String key, Object...values)
    {
        if (messageset != null)
        {
            final I18nHelper i18n = factory.getInstance(searcher);
            messageset.addErrorMessage(i18n.getText(key, values));
        }
    }
}
