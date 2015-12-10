package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.util.GroupCustomFieldIndexValueConverter;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.query.clause.TerminalClause;

/**
 * Group custom field clause validator
 *
 * @since v4.0
 */
public class GroupCustomFieldValidator implements ClauseValidator
{
    private final IndexValuesValidator indexValuesValidator;
    private final SupportedOperatorsValidator supportedOperatorsValidator;
    private final JqlOperandResolver jqlOperandResolver;

    public GroupCustomFieldValidator(JqlOperandResolver jqlOperandResolver, GroupCustomFieldIndexValueConverter groupCustomFieldIndexValueConverter)
    {
        this.jqlOperandResolver = jqlOperandResolver;
        this.supportedOperatorsValidator = getSupportedOperatorsValidator();
        this.indexValuesValidator = getIndexValuesValidator(groupCustomFieldIndexValueConverter);
    }

    public MessageSet validate(final User searcher, final TerminalClause terminalClause)
    {
        MessageSet errors = supportedOperatorsValidator.validate(searcher, terminalClause);
        if (!errors.hasAnyErrors())
        {
            errors = indexValuesValidator.validate(searcher, terminalClause);
        }
        return errors;
    }

    IndexValuesValidator getIndexValuesValidator(final GroupCustomFieldIndexValueConverter groupCustomFieldIndexValueConverter)
    {
        return new IndexValuesValidator(jqlOperandResolver, groupCustomFieldIndexValueConverter)
        {
            @Override
            void addError(final MessageSet messageSet, final User searcher, TerminalClause terminalClause, final QueryLiteral literal)
            {
                String fieldName = terminalClause.getName();
                if (jqlOperandResolver.isFunctionOperand(literal.getSourceOperand()))
                {
                    messageSet.addErrorMessage(getI18n(searcher).getText("jira.jql.clause.invalid.group.value.function", literal.getSourceOperand().getName(), fieldName));
                }
                else
                {
                    messageSet.addErrorMessage(getI18n(searcher).getText("jira.jql.clause.invalid.group.value", fieldName, literal.asString()));
                }
            }
        };
    }

    SupportedOperatorsValidator getSupportedOperatorsValidator()
    {
        return new SupportedOperatorsValidator(OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY);
    }

    ///CLOVER:OFF
    I18nHelper getI18n(User user)
    {
        return new I18nBean(user);
    }
    ///CLOVER:ON
}