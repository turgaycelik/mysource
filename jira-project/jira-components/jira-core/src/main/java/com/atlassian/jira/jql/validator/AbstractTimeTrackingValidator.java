package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.util.JqlTimetrackingDurationSupport;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.query.clause.TerminalClause;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Validates time tracking clauses -- these can only take values which are positive durations.
 *
 * @since v4.0
 */
abstract class AbstractTimeTrackingValidator implements ClauseValidator
{
    private final SupportedOperatorsValidator supportedOperatorsValidator;
    private final PositiveDurationValueValidator positiveDurationValueValidator;
    private final ApplicationProperties applicationProperties;

    public AbstractTimeTrackingValidator(final JqlOperandResolver operandResolver,
            final ApplicationProperties applicationProperties, final JqlTimetrackingDurationSupport timetrackingDurationSupport)
    {
        notNull("operandResolver", operandResolver);
        this.applicationProperties = notNull("applicationProperties", applicationProperties);
        this.supportedOperatorsValidator = getSupportedOperatorsValidator();
        this.positiveDurationValueValidator = getPositiveDurationValueValidator(operandResolver, timetrackingDurationSupport);
    }

    public MessageSet validate(final User searcher, final TerminalClause terminalClause)
    {
        if (applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING))
        {
            MessageSet errors = supportedOperatorsValidator.validate(searcher, terminalClause);
            if (!errors.hasAnyErrors())
            {
                errors = positiveDurationValueValidator.validate(searcher, terminalClause);
            }
            return errors;
        }
        else
        {
            MessageSet messageSet = new MessageSetImpl();
            messageSet.addErrorMessage(getI18n(searcher).getText("jira.jql.clause.timetracking.disabled", terminalClause.getName()));
            return messageSet;
        }
    }

    SupportedOperatorsValidator getSupportedOperatorsValidator()
    {
        return new SupportedOperatorsValidator(OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY, OperatorClasses.RELATIONAL_ONLY_OPERATORS);
    }

    PositiveDurationValueValidator getPositiveDurationValueValidator(final JqlOperandResolver operandResolver,
            JqlTimetrackingDurationSupport timetrackingDurationSupport)
    {
        return new PositiveDurationValueValidator(operandResolver, timetrackingDurationSupport);
    }

    ///CLOVER:OFF
    I18nHelper getI18n(User user)
    {
        return new I18nBean(user);
    }
    ///CLOVER:ON
}