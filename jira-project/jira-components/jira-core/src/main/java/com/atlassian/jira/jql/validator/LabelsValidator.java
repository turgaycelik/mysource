package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.label.LabelParser;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operator.Operator;
import org.apache.commons.lang.StringUtils;

import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A validator for the Labels field that is a simple wrapper around the text field validator.
 *
 * @since v4.2
 */
public class LabelsValidator implements ClauseValidator
{
    private final JqlOperandResolver operandResolver;

    public LabelsValidator(JqlOperandResolver operandResolver)
    {
        this.operandResolver = notNull("operandResolver", operandResolver);
    }

    public MessageSet validate(final User searcher, final TerminalClause terminalClause)
    {
        final MessageSet messageSet = new MessageSetImpl();
        final I18nHelper i18n = getI18n(searcher);
        final Operator operator = terminalClause.getOperator();
        final String fieldName = terminalClause.getName();
        if (!handlesOperator(operator))
        {
            messageSet.addErrorMessage(i18n.getText("jira.jql.clause.does.not.support.operator", operator.getDisplayString(), fieldName));
            return messageSet;
        }

        final Operand operand = terminalClause.getOperand();

        final List<QueryLiteral> values = operandResolver.getValues(searcher, operand, terminalClause);
        if (values != null)
        {
            for (QueryLiteral literal : values)
            {
                // empty literals are always okay
                if (!literal.isEmpty())
                {
                    final String query = literal.asString();
                    if (StringUtils.isNotBlank(query))
                    {
                        String label = query.trim();
                        if (!LabelParser.isValidLabelName(label))
                        {
                            messageSet.addErrorMessage(i18n.getText("label.service.error.label.invalid", label));
                        }
                        if (label.length() > LabelParser.MAX_LABEL_LENGTH)
                        {
                            messageSet.addErrorMessage(i18n.getText("label.service.error.label.toolong", label));
                        }
                    }
                }
            }
        }

        return messageSet;
    }

    private boolean handlesOperator(final Operator operator)
    {
        return OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY.contains(operator);
    }

    ///CLOVER:OFF
    I18nHelper getI18n(User user)
    {
        return new I18nBean(user);
    }
    ///CLOVER:ON
}
