package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.resolver.ResolutionIndexInfoResolver;
import com.atlassian.jira.jql.resolver.ResolutionResolver;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;

/**
 * A simple wrapper around ConstantsClauseValidator.
 *
 * @since v4.0
 */
public class ResolutionValidator implements ClauseValidator
{
    ///CLOVER:OFF

    private final RawValuesExistValidator rawValuesExistValidator;
    private final SupportedOperatorsValidator supportedOperatorsValidator;

    public ResolutionValidator(final ResolutionResolver resolutionResolver, final JqlOperandResolver operandResolver, I18nHelper.BeanFactory beanFactory)
    {
        this.rawValuesExistValidator = new RawValuesExistValidator(operandResolver, new ResolutionIndexInfoResolver(resolutionResolver), beanFactory);
        this.supportedOperatorsValidator = getSupportedOperatorsValidator();
    }

    public MessageSet validate(final User searcher, final TerminalClause terminalClause)
    {
        final MessageSet messageSet = supportedOperatorsValidator.validate(searcher, terminalClause);
        if (!messageSet.hasAnyErrors())
        {
            messageSet.addMessageSet(rawValuesExistValidator.validate(searcher, terminalClause));
        }
        return  messageSet;
    }

    SupportedOperatorsValidator getSupportedOperatorsValidator()
    {
        return new SupportedOperatorsValidator(OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY, OperatorClasses.RELATIONAL_ONLY_OPERATORS);
    }

    ///CLOVER:ON
}
