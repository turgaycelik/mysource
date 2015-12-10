package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operator.Operator;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Does validation to determine if the operator in use is something other than the specified accepted
 * operators and adds an error message if so.
 *
 * @since v4.0
 */
public class SupportedOperatorsValidator
{
    private final Collection<Operator> supportedOperators;

    public SupportedOperatorsValidator(Collection<Operator>... supportedOperatorSets)
    {
        Set<Operator> tmpOperators = EnumSet.noneOf(Operator.class);
        for (Collection<Operator> supportedOperatorSet : supportedOperatorSets)
        {
            tmpOperators.addAll(supportedOperatorSet);
        }

        this.supportedOperators = Collections.unmodifiableSet(tmpOperators);
    }

    public MessageSet validate(final User searcher, final TerminalClause terminalClause)
    {
        MessageSet messageSet = new MessageSetImpl();
        // First lets validate that we are not being used with the greater-than/less-than operators, we don't support it
        final Operator operator = terminalClause.getOperator();
        if (!supportedOperators.contains(operator))
        {
            I18nHelper i18n = getI18n(searcher);
            messageSet.addErrorMessage(i18n.getText("jira.jql.clause.does.not.support.operator", operator.getDisplayString(), terminalClause.getName()));
        }

        return messageSet;
    }

    I18nHelper getI18n(User user)
    {
        return ComponentAccessor.getI18nHelperFactory().getInstance(user);
    }
}
