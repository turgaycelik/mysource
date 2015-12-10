package com.atlassian.jira.jql.validator;


import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.changehistory.JqlChangeItemMapping;
import com.atlassian.jira.issue.index.ChangeHistoryFieldConfigurationManager;
import com.atlassian.jira.issue.index.IndexedChangeHistoryFieldManager;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.PredicateOperandResolver;
import com.atlassian.jira.jql.operand.registry.JqlFunctionHandlerRegistry;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.resolver.UserResolver;
import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.WasClause;


/**
 * Validat the was clause against any field.
 *
 * @since v4.3
 */
public class WasClauseValidator implements ClauseValidator
{
    private final SupportedOperatorsValidator supportedOperatorsValidator;
    private final JqlOperandResolver operandResolver;
    private final IndexedChangeHistoryFieldManager indexedChangeHistoryFieldManager;
    private final SearchHandlerManager searchHandlerManager;
    private final ChangeHistoryManager changeHistoryManager;
    private final HistoryPredicateValidator  historyPredicateValidator;
    private final JqlChangeItemMapping jqlChangeItemMapping;
    private final ChangeHistoryFieldConfigurationManager configurationManager;
    private final HistoryFieldValueValidator historyFieldValueValidator;

    public WasClauseValidator(
            final JqlOperandResolver operandResolver,
            final SearchHandlerManager searchHandlerManager,
            final IndexedChangeHistoryFieldManager indexedChangeHistoryFieldManager,
            final ChangeHistoryManager changeHistoryManager,
            final PredicateOperandResolver predicateOperandResolver,
            final JqlDateSupport jqlDateSupport,
            final JiraAuthenticationContext authContext,
            final JqlChangeItemMapping jqlChangeItemMapping,
            final ChangeHistoryFieldConfigurationManager configurationManager,
            final HistoryFieldValueValidator historyFieldValueValidator,
            final UserManager userManager,
            final JqlFunctionHandlerRegistry registry)
    {
        this.searchHandlerManager = searchHandlerManager;
        this.operandResolver = operandResolver;
        this.indexedChangeHistoryFieldManager = indexedChangeHistoryFieldManager;
        this.jqlChangeItemMapping = jqlChangeItemMapping;
        this.configurationManager = configurationManager;
        this.historyFieldValueValidator = historyFieldValueValidator;
        this.supportedOperatorsValidator = getSupportedOperatorsValidator();
        this.changeHistoryManager = changeHistoryManager;
        this.historyPredicateValidator = new HistoryPredicateValidator(authContext, predicateOperandResolver, jqlDateSupport, historyFieldValueValidator, registry, userManager);
    }

    public MessageSet validate(final User searcher, final TerminalClause terminalClause)
    {
        WasClause wasClause = (WasClause) terminalClause;
        final WasValuesExistValidator rawValuesExistValidator = getRawValuesValidator(terminalClause.getName(), operandResolver, searcher);
        final MessageSet messageSet = supportedOperatorsValidator.validate(searcher, terminalClause);
        messageSet.addMessageSet(rawValuesExistValidator.validate(searcher, wasClause));
        validateField(searcher, wasClause.getField(), messageSet);
        if (wasClause.getPredicate() != null)
        {
           messageSet.addMessageSet(historyPredicateValidator.validate(searcher, wasClause, wasClause.getPredicate()));
        }
        return messageSet;
    }

    private void validateField(User searcher, String fieldName, MessageSet messages)
    {
        if (!indexedChangeHistoryFieldManager.getIndexedChangeHistoryFieldNames().contains(fieldName.toLowerCase()))
        {
            messages.addErrorMessage(getI18n(searcher).getText("jira.jql.history.field.not.supported", fieldName));
        }
    }

    SupportedOperatorsValidator getSupportedOperatorsValidator()
    {
        return new SupportedOperatorsValidator(OperatorClasses.CHANGE_HISTORY_OPERATORS);
    }

    WasValuesExistValidator getRawValuesValidator(final String fieldName, final JqlOperandResolver operandResolver, final User searcher)
    {
        return new WasValuesExistValidator(operandResolver, searchHandlerManager, changeHistoryManager, jqlChangeItemMapping, configurationManager, historyFieldValueValidator);
    }

    I18nHelper getI18n(User user)
    {
        return new I18nBean(user);
    }


}

