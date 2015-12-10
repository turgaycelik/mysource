package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.changehistory.JqlChangeItemMapping;
import com.atlassian.jira.issue.index.ChangeHistoryFieldConfigurationManager;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.jql.ClauseHandler;
import com.atlassian.jira.jql.ValueGeneratingClauseHandler;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.values.ClauseValuesGenerator;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.query.clause.WasClause;
import com.atlassian.query.operand.Operand;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class WasValuesExistValidator
{
    private final JqlOperandResolver operandResolver;
    private final SearchHandlerManager searchHandlerManager;
    private final ChangeHistoryManager changeHistoryManager;
    private final JqlChangeItemMapping jqlChangeItemMapping;
    private final ChangeHistoryFieldConfigurationManager configurationManager;
    private final HistoryFieldValueValidator historyFieldValueValidator;

    public WasValuesExistValidator(JqlOperandResolver operandResolver, SearchHandlerManager searchHandlerManager, ChangeHistoryManager changeHistoryManager, JqlChangeItemMapping jqlChangeItemMapping, ChangeHistoryFieldConfigurationManager configurationManager, HistoryFieldValueValidator historyFieldValueValidator)
    {
        this.operandResolver = operandResolver;
        this.searchHandlerManager = searchHandlerManager;
        this.changeHistoryManager = changeHistoryManager;
        this.jqlChangeItemMapping = jqlChangeItemMapping;
        this.configurationManager = configurationManager;
        this.historyFieldValueValidator = historyFieldValueValidator;
    }

    public MessageSet validate(User searcher, WasClause clause)
    {

        final Operand operand = clause.getOperand();
        final String fieldName = clause.getName();
        final MessageSet messages = new MessageSetImpl();
        if (operandResolver.isValidOperand(operand))
        {
            final List<QueryLiteral> rawValues = operandResolver.getValues(searcher, operand, clause);
            messages.addMessageSet(historyFieldValueValidator.validateValues(searcher, fieldName, rawValues));
        }
        return messages;
    }


}