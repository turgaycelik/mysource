package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.resolver.IssueConstantInfoResolver;
import com.atlassian.jira.jql.resolver.PriorityResolver;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;

/**
 * A simple wrapper on the ConstantsClauseValidator.
 *
 * @since v4.0
 */
public class PriorityValidator implements ClauseValidator
{
    ///CLOVER:OFF

    private final RawValuesExistValidator rawValuesExistValidator;
    private final SupportedOperatorsValidator supportedOperatorsValidator;

    public PriorityValidator(final PriorityResolver priorityResolver, final JqlOperandResolver operandResolver, I18nHelper.BeanFactory beanFactory)
    {
        this.supportedOperatorsValidator = getSupportedOperatorsValidator();
        this.rawValuesExistValidator = new RawValuesExistValidator(operandResolver, new IssueConstantInfoResolver<Priority>(priorityResolver), beanFactory);
    }

    public MessageSet validate(final User searcher, final TerminalClause terminalClause)
    {
        MessageSet errors = supportedOperatorsValidator.validate(searcher, terminalClause);
        if (!errors.hasAnyErrors())
        {
            errors = rawValuesExistValidator.validate(searcher, terminalClause);
        }
        return errors;
    }

    SupportedOperatorsValidator getSupportedOperatorsValidator()
    {
        return new SupportedOperatorsValidator(OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY, OperatorClasses.RELATIONAL_ONLY_OPERATORS);
    }

    ///CLOVER:ON
}
