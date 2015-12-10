package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.util.MovedIssueKeyStore;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.util.JqlIssueKeySupport;
import com.atlassian.jira.jql.util.JqlIssueSupport;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.Operand;
import com.atlassian.util.profiling.UtilTimerStack;
import org.apache.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Clause validator for the &quot;IssueKey&quot; clause.
 *
 * @since v4.0
 */
@InjectableComponent
public class IssueIdValidator implements ClauseValidator
{
    private static final Logger log = Logger.getLogger(IssueIdValidator.class);
    private static final int BATCH_MAX_SIZE = 1000;

    private final JqlOperandResolver operandResolver;
    private final SupportedOperatorsValidator supportedOperatorsValidator;
    private final JqlIssueKeySupport issueKeySupport;
    private final JqlIssueSupport issueSupport;
    private final I18nHelper.BeanFactory i18nFactory;
    private final MovedIssueValidator movedIssueValidator;

    IssueIdValidator(final JqlOperandResolver operandResolver, final JqlIssueKeySupport issueKeySupport,
                     final JqlIssueSupport issueSupport, final I18nHelper.BeanFactory i18nFactory,
                     final SupportedOperatorsValidator supportedOperatorsValidator,
                     final MovedIssueValidator movedIssueValidator)
    {
        this.issueSupport = issueSupport;
        this.issueKeySupport = notNull("issueKeySupport", issueKeySupport);
        this.i18nFactory = notNull("i18nFactory", i18nFactory);
        this.supportedOperatorsValidator = notNull("supportedOperatorsValidator", supportedOperatorsValidator);
        this.operandResolver = notNull("operandResolver", operandResolver);
        this.movedIssueValidator = notNull("movedIssueValidator", movedIssueValidator);
    }

    public IssueIdValidator(final JqlOperandResolver operandResolver, final JqlIssueKeySupport issueKeySupport,
                            final JqlIssueSupport issueSupport, final I18nHelper.BeanFactory i18nFactory,
                            final MovedIssueKeyStore moveIssueKeyStore)
    {
        this(operandResolver, issueKeySupport, issueSupport, i18nFactory,
                new SupportedOperatorsValidator(SystemSearchConstants.forIssueKey().getSupportedOperators()),
                new MovedIssueValidator(OperatorClasses.EQUALITY_OPERATORS, moveIssueKeyStore, i18nFactory));
    }

    @Nonnull
    public MessageSet validate(final User searcher, @Nonnull final TerminalClause terminalClause)
    {
        notNull("terminalClause", terminalClause);
        UtilTimerStack.push("IssueIdValidator.validate()");

        try
        {
            MessageSet messages = supportedOperatorsValidator.validate(searcher, terminalClause);
            if (!messages.hasAnyErrors())
            {
                final Operand operand = terminalClause.getOperand();

                //Thus should not return null since the outside validation makes sure the operand is valid before
                //calling this method.
                final List<QueryLiteral> values = operandResolver.getValues(searcher, operand, terminalClause);

                UtilTimerStack.push("IssueIdValidator.validate() - looping");
                int batches = values == null ? 0 : values.size() / BATCH_MAX_SIZE + 1;
                for (int batchIndex = 0; batchIndex < batches; batchIndex++)
                {
                    List<QueryLiteral> valuesBatch = values.subList(batchIndex * BATCH_MAX_SIZE,
                            Math.min((batchIndex + 1) * BATCH_MAX_SIZE, values.size()));
                    validateBatch(searcher, terminalClause, valuesBatch, messages);
                }
                UtilTimerStack.pop("IssueIdValidator.validate() - looping");
            }

            return messages;
        }
        finally
        {
            UtilTimerStack.pop("IssueIdValidator.validate()");
        }
    }

    private void validateBatch(final User searcher, @Nonnull final TerminalClause terminalClause,
                               List<QueryLiteral> values, final MessageSet messages)
    {
        final Operand operand = terminalClause.getOperand();
        Set<Long> numericLiterals = new HashSet<Long>();
        Set<String> stringLiterals = new HashSet<String>();
        for (QueryLiteral value : values)
        {
            if (!value.isEmpty())
            {
                if (value.getLongValue() != null)
                {
                    numericLiterals.add(value.getLongValue());
                }
                else if (value.getStringValue() != null)
                {
                    stringLiterals.add(value.getStringValue());
                }
                else
                {
                    log.debug("Unknown QueryLiteral: " + value.toString());
                }
            }
            else
            {
                validateEmptyOperand(messages, searcher, terminalClause, value.getSourceOperand());
            }
        }
        if (!numericLiterals.isEmpty() || !stringLiterals.isEmpty())
        {
            if (numericLiterals.size() > 0)
                validateIssueIdsBatch(messages, numericLiterals, searcher, terminalClause, operand);
            if (stringLiterals.size() > 0)
                validateIssueKeysBatch(messages, stringLiterals, searcher, terminalClause, operand);
        }
    }

    private MessageSet validateEmptyOperand(final MessageSet messageSet, final User searcher,
                                            final TerminalClause clause, final Operand operand)
    {
        final I18nHelper i18n = i18nFactory.getInstance(searcher);
        if (!operandResolver.isFunctionOperand(operand))
        {
            messageSet.addErrorMessage(i18n.getText("jira.jql.clause.field.does.not.support.empty", clause.getName()));
        }
        else
        {
            messageSet.addErrorMessage(
                    i18n.getText("jira.jql.clause.field.does.not.support.empty.from.func", clause.getName(),
                            operand.getName()));
        }
        return messageSet;
    }

    private void validateIssueIdsBatch(final MessageSet messages, final Set<Long> issueIds, final User searcher,
                                       final TerminalClause clause, final Operand operand)
    {
        Set<Long> missingIssues = issueSupport.getIdsOfMissingIssues(issueIds);
        for (Long missingIssue : missingIssues)
        {
            addErrorIssueIdNotFound(messages, missingIssue, searcher, clause, operand);
        }
    }

    private void addErrorIssueIdNotFound(final MessageSet messages, final Long issueId, final User searcher,
                                         final TerminalClause clause, final Operand operand)
    {
        final I18nHelper i18n = i18nFactory.getInstance(searcher);
        if (!operandResolver.isFunctionOperand(operand))
        {
            messages.addErrorMessage(
                    i18n.getText("jira.jql.clause.no.value.for.id", clause.getName(), issueId.toString()));
        }
        else
        {
            messages.addErrorMessage(i18n.getText("jira.jql.clause.no.value.for.name.from.function", operand.getName(),
                    clause.getName()));
        }
    }

    private void validateIssueKeysBatch(final MessageSet messages, final Set<String> issueKeys, final User searcher,
                                        final TerminalClause clause, final Operand operand)
    {
        Set<String> missingIssueKeys = issueSupport.getKeysOfMissingIssues(issueKeys);
        for (String missingIssueKey : missingIssueKeys)
        {
            addErrorIssueKeyNotFound(messages, missingIssueKey, searcher, clause, operand);
        }
        Set<String> validIssueKeys = new HashSet<String>(issueKeys);
        validIssueKeys.removeAll(missingIssueKeys);
        if (!validIssueKeys.isEmpty())
        {
            messages.addMessageSet(
                    movedIssueValidator.validate(ApplicationUsers.from(searcher), validIssueKeys, clause));
        }
    }

    private void addErrorIssueKeyNotFound(final MessageSet messages, final String key, final User searcher,
                                          final TerminalClause clause, final Operand operand)
    {
        final I18nHelper i18n = i18nFactory.getInstance(searcher);
        final boolean validIssueKey = issueKeySupport.isValidIssueKey(key);
        if (!operandResolver.isFunctionOperand(operand))
        {
            if (validIssueKey)
            {
                messages.addErrorMessage(i18n.getText("jira.jql.clause.issuekey.noissue", key, clause.getName()));
            }
            else
            {
                messages.addErrorMessage(
                        i18n.getText("jira.jql.clause.issuekey.invalidissuekey", key, clause.getName()));
            }
        }
        else
        {
            if (validIssueKey)
            {
                messages.addErrorMessage(i18n.getText("jira.jql.clause.issuekey.noissue.from.func", operand.getName(),
                        clause.getName()));
            }
            else
            {
                messages.addErrorMessage(
                        i18n.getText("jira.jql.clause.issuekey.invalidissuekey.from.func", operand.getName(),
                                clause.getName()));
            }
        }
    }

    /**
     * Determine whether issue permission check should be skipped during validation.
     * <p/>
     * We want to skip permission check during validation for jql functions, to allow sql functions to be lazy and
     * to return more than what the user could view.
     * Permission check would still be performed when the actual search happens in SearchProvider.
     * see https://jira.atlassian.com/browse/JRA-34436
     * <p/>
     * At the same time, we don't want to skip checking for literals, as users might use {@code issue = ABC-1} to discover the existence of project ABC,
     * which we've always tried to prevent.
     *
     * @param operand the operand
     * @return whether issue permission check during validation should be skipped
     */
    private boolean skipPermissionCheck(final Operand operand)
    {
        return operandResolver.isFunctionOperand(operand);
    }
}
