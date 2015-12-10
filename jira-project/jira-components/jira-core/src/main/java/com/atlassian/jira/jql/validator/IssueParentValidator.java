package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.util.MovedIssueKeyStore;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.util.JqlIssueKeySupport;
import com.atlassian.jira.jql.util.JqlIssueSupport;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.clause.TerminalClause;

import javax.annotation.Nonnull;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Clause validator for the &quot;Issue Parent&quot; clause.
 *
 * @since v4.0
 */
@InjectableComponent
public class IssueParentValidator implements ClauseValidator
{
    private final SubTaskManager subTaskManager;
    private final IssueIdValidator issueIdValidator;
    private final I18nHelper.BeanFactory i18nFactory;

    IssueParentValidator(final IssueIdValidator issueIdValidator, final SubTaskManager subTaskManager, final I18nHelper.BeanFactory i18nFactory)
    {
        this.issueIdValidator = notNull("issueIdValidator", issueIdValidator);
        this.i18nFactory = notNull("i18nFactory", i18nFactory);
        this.subTaskManager = notNull("subTaskManager", subTaskManager);
    }

    public IssueParentValidator(final JqlOperandResolver operandResolver, final JqlIssueKeySupport issueKeySupport, final JqlIssueSupport issueSupport, final I18nHelper.BeanFactory i18nFactory, final MovedIssueKeyStore moveIssueKeyStore, final SubTaskManager subTaskManager)
    {
        this(new IssueIdValidator(operandResolver, issueKeySupport, issueSupport, i18nFactory, new SupportedOperatorsValidator(SystemSearchConstants.forIssueParent().getSupportedOperators()), new MovedIssueValidator(OperatorClasses.EQUALITY_OPERATORS, moveIssueKeyStore, i18nFactory)), subTaskManager, i18nFactory);
    }

    @Nonnull
    public MessageSet validate(final User searcher, @Nonnull final TerminalClause terminalClause)
    {
        notNull("terminalClause", terminalClause);

        if (!subTaskManager.isSubTasksEnabled())
        {
            MessageSet messageSet = new MessageSetImpl();
            final I18nHelper i18n = i18nFactory.getInstance(searcher);
            messageSet.addErrorMessage(i18n.getText("jira.jql.clause.issue.parent.subtasks.disabled", terminalClause.getName()));
            return messageSet;
        }

        return issueIdValidator.validate(searcher, terminalClause);
    }
}
