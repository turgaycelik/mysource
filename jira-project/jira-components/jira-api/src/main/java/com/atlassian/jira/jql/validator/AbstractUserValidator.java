package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.resolver.UserResolver;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;

/**
 * An abstract Validator for the User field clauses
 *
 * @since v4.0
 */
public abstract class AbstractUserValidator implements ClauseValidator
{
    private final SupportedOperatorsValidator supportedOperatorsValidator;
    private final DataValuesExistValidator dataValuesExistValidator;

    public AbstractUserValidator(UserResolver userResolver, JqlOperandResolver operandResolver, I18nHelper.BeanFactory beanFactory)
    {
        this.supportedOperatorsValidator = getSupportedOperatorsValidator();
        this.dataValuesExistValidator = getDataValuesValidator(userResolver, operandResolver, beanFactory);
    }

    public MessageSet validate(final User searcher, final TerminalClause terminalClause)
    {
        MessageSet errors = supportedOperatorsValidator.validate(searcher, terminalClause);
        if (!errors.hasAnyErrors())
        {
            errors = dataValuesExistValidator.validate(searcher, terminalClause);
        }
        return errors;
    }

    SupportedOperatorsValidator getSupportedOperatorsValidator()
    {
        return new SupportedOperatorsValidator(OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY);
    }

    DataValuesExistValidator getDataValuesValidator(final UserResolver resolver, final JqlOperandResolver operandResolver, final I18nHelper.BeanFactory beanFactory)
    {
        return new DataValuesExistValidator(operandResolver, resolver, beanFactory, MessageSet.Level.WARNING);
    }
}