package com.atlassian.jira.jql.validator;

import com.atlassian.jira.issue.util.MovedIssueKeyStore;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operator.Operator;
import com.google.common.collect.Sets;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Validates for incorrect usage of historical issue key of moved issue with operator that does not support such keys.
 *
 * @since v6.1
 */
class MovedIssueValidator
{

    private final Set<Operator> supportedOperators;
    private final MovedIssueKeyStore movedIssueKeyStore;
    private final I18nHelper.BeanFactory i18nFactory;

    public MovedIssueValidator(final Set<Operator> operatorsSupportingMovedIssues,
                               final MovedIssueKeyStore movedIssueKeyStore,
                               final I18nHelper.BeanFactory i18nFactory)
    {
        this.movedIssueKeyStore = movedIssueKeyStore;
        this.i18nFactory = i18nFactory;
        this.supportedOperators = Sets.immutableEnumSet(operatorsSupportingMovedIssues);
    }

    public MessageSet validate(final ApplicationUser searcher, final String key, final TerminalClause terminalClause)
    {
        return validate(searcher, new HashSet<String>(Arrays.asList(key)), terminalClause);
    }

    public MessageSet validate(final ApplicationUser searcher, final Set<String> keys,
                               final TerminalClause terminalClause)
    {
        final Operator operator = terminalClause.getOperator();
        if (isSupported(operator))
        {
            return new MessageSetImpl();
        }
        return validateMovedIssue(searcher, keys, operator);
    }

    private MessageSet validateMovedIssue(final ApplicationUser searcher, final Set<String> keys,
                                          final Operator operator)
    {
        final MessageSetImpl messageSet = new MessageSetImpl();
        Set<String> movedIssueKeys = getMovedIssues(keys);
        for (String movedIssueKey : movedIssueKeys)
        {
            final I18nHelper i18n = i18nFactory.getInstance(searcher);
            messageSet.addErrorMessage(
                    i18n.getText("jira.jql.clause.issuekey.movedissue", movedIssueKey, operator.getDisplayString()));
        }
        return messageSet;
    }

    private boolean isSupported(final Operator operator)
    {
        return supportedOperators.contains(operator);
    }

    private Set<String> getMovedIssues(final Set<String> keys)
    {
        Set<String> upperCaseIssueKeys = new HashSet<String>();
        for (String key : keys)
            upperCaseIssueKeys.add(key.toUpperCase(Locale.ENGLISH));
        return movedIssueKeyStore.getMovedIssueKeys(upperCaseIssueKeys);
    }
}
