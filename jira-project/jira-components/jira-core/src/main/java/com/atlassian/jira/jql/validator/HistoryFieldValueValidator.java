package com.atlassian.jira.jql.validator;


import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.changehistory.JqlChangeItemMapping;
import com.atlassian.jira.issue.index.ChangeHistoryFieldConfigurationManager;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.SearchProviderFactory;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.issue.search.parameters.lucene.sort.JiraLuceneFieldFinder;
import com.atlassian.jira.jql.ClauseHandler;
import com.atlassian.jira.jql.ValueGeneratingClauseHandler;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.resolver.NameResolver;
import com.atlassian.jira.jql.values.ClauseValuesGenerator;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.web.bean.I18nBean;

import com.google.common.collect.Sets;
import org.apache.lucene.index.IndexReader;


import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Validates that the values in a history clause are valid for a field.  Has to take into account historical name changes.
 *
 * @since v4.4
 */
public class HistoryFieldValueValidator
{
    private final SearchHandlerManager searchHandlerManager;
    private final JqlChangeItemMapping jqlChangeItemMapping;
    private final ChangeHistoryManager changeHistoryManager;
    private final JqlOperandResolver operandResolver;
    private final ChangeHistoryFieldConfigurationManager configurationManager;
    private final SearchProviderFactory searchProviderFactory;

    public HistoryFieldValueValidator(SearchHandlerManager searchHandlerManager, JqlChangeItemMapping jqlChangeItemMapping, ChangeHistoryManager changeHistoryManager, JqlOperandResolver operandResolver, ChangeHistoryFieldConfigurationManager configurationManager, SearchProviderFactory searchProviderFactory) {
        this.searchHandlerManager = searchHandlerManager;
        this.jqlChangeItemMapping = jqlChangeItemMapping;
        this.changeHistoryManager = changeHistoryManager;
        this.operandResolver = operandResolver;
        this.configurationManager = configurationManager;
        this.searchProviderFactory = searchProviderFactory;
    }

    private boolean stringValueExists(PossibleValuesHolder possibleValuesHolder, User searcher, String fieldName, String rawValue)
    {
        final String valuePrefix = "";
        final Collection<ClauseHandler> clauseHandlers = searchHandlerManager.getClauseHandler(searcher, fieldName);
        if (clauseHandlers != null && clauseHandlers.size() == 1)
        {
            ClauseHandler clauseHandler = clauseHandlers.iterator().next();
            if (clauseHandler instanceof ValueGeneratingClauseHandler)
            {
                if (possibleValuesHolder.getCurrentValues() == null)
                {
                    final ClauseValuesGenerator.Results generatorResults = ((ValueGeneratingClauseHandler) (clauseHandler))
                            .getClauseValuesGenerator().getPossibleValues(searcher, jqlChangeItemMapping.mapJqlClauseToFieldName(fieldName), valuePrefix, Integer.MAX_VALUE);
                    final List<ClauseValuesGenerator.Result> list = generatorResults.getResults();
                    final Set<String> possibleValues = new HashSet<String>(list.size());
                    for (ClauseValuesGenerator.Result result : list)
                    {
                        possibleValues.add(result.getValue().toLowerCase());
                    }
                    possibleValuesHolder.setCurrentValues(possibleValues);
                }
                if (!possibleValuesHolder.getCurrentValues().contains(rawValue.toLowerCase()))
                {
                    if (possibleValuesHolder.getHistoricalValues() == null)
                    {
                        final Set<String> possibleValuesSet = findAllPossibleFieldTerms(searcher, fieldName.toLowerCase());
                        possibleValuesHolder.setHistoricalValues(possibleValuesSet);
                    }
                    return possibleValuesHolder.getHistoricalValues().contains(rawValue.toLowerCase());
                }
                return possibleValuesHolder.getCurrentValues().contains(rawValue.toLowerCase());
            }
        }
        return false;
    }

    private Set<String> findAllPossibleFieldTerms(User searcher, String fieldName)
    {
        final Set<String> values = Sets.newHashSet();
        final JiraLuceneFieldFinder luceneFieldFinder = JiraLuceneFieldFinder.getInstance();
        try
        {
            final IndexReader reader = searchProviderFactory.getSearcher(SearchProviderFactory.CHANGE_HISTORY_INDEX).getIndexReader();
            final List<String> oldValues = luceneFieldFinder.getTermValuesForField(reader, fieldName+"."+DocumentConstants.CHANGE_FROM);
            final List<String> newValues =  luceneFieldFinder.getTermValuesForField(reader, fieldName+"."+DocumentConstants.CHANGE_TO);
            values.addAll(oldValues);
            values.addAll(newValues);
        }
        catch (IOException ioe) 
        {
             throw new RuntimeException(ioe);
        }
        return stripProtocol(values);
    }

    private Set<String> stripProtocol(Set<String> values)
    {
        final Set<String> newValues = Sets.newHashSetWithExpectedSize(values.size());
        final int index = DocumentConstants.CHANGE_HISTORY_PROTOCOL.length();
        for (String value : values) 
        {
            newValues.add(value.substring(index));
        }
        return newValues;
    }


    public MessageSet validateValues(User searcher, String fieldName, List<QueryLiteral> rawValues)
    {
        final PossibleValuesHolder possibleValuesHolder = new PossibleValuesHolder();
        final MessageSet messages = new MessageSetImpl();
        for (QueryLiteral rawValue : rawValues)
        {
            if (rawValue.getStringValue() != null)
            {
                if (!stringValueExists(possibleValuesHolder, searcher, fieldName, rawValue.getStringValue()))
                {
                    if (operandResolver.isFunctionOperand(rawValue.getSourceOperand()))
                    {
                        messages.addErrorMessage(getI18n(searcher).getText("jira.jql.clause.no.value.for.name.from.function", rawValue.getSourceOperand().getName(), fieldName));
                    }
                    else
                    {
                        messages.addErrorMessage(getI18n(searcher).getText("jira.jql.clause.no.value.for.name", fieldName, rawValue.getStringValue()));
                    }
                }
            }
            else if (rawValue.getLongValue() != null)
            {
                if (stringValueExists(possibleValuesHolder, searcher, fieldName, rawValue.getLongValue().toString()))
                {
                    return messages;
                }
                if (!configurationManager.supportsIdSearching(fieldName.toLowerCase()))
                {
                    if (operandResolver.isFunctionOperand(rawValue.getSourceOperand()))
                    {
                        messages.addErrorMessage(getI18n(searcher).getText("jira.jql.clause.no.value.for.name.from.function", rawValue.getSourceOperand().getName(), fieldName));
                    }
                    else
                    {
                        messages.addErrorMessage(getI18n(searcher).getText("jira.jql.history.clause.not.string", rawValue.getSourceOperand().getName(), fieldName));
                    }
                }
                else
                {
                    if (!longValueExists(searcher, fieldName, rawValue.getLongValue()))
                    {
                        if (operandResolver.isFunctionOperand(rawValue.getSourceOperand()))
                        {
                            messages.addErrorMessage(getI18n(searcher).getText("jira.jql.clause.no.value.for.name.from.function", rawValue.getSourceOperand().getName(), fieldName));
                        }
                        else
                        {
                            messages.addErrorMessage(getI18n(searcher).getText("jira.jql.clause.no.value.for.id", fieldName, rawValue.getLongValue().toString()));
                        }
                    }
                }
            }
        }
        return messages;
    }

    private boolean longValueExists(User searcher, String fieldName, Long longValue)
    {
        NameResolver<?> resolver  = configurationManager.getNameResolver(fieldName.toLowerCase());
        return resolver.idExists(longValue);
    }

    I18nHelper getI18n(User user)
    {
        return new I18nBean(user);
    }

    private class PossibleValuesHolder
    {
        Set<String> currentValues = null;
        Set<String> historicalValues = null;

        private Set<String> getCurrentValues()
        {
            return currentValues;
        }

        private void setCurrentValues(final Set<String> currentValues)
        {
            this.currentValues = currentValues;
        }

        private Set<String> getHistoricalValues()
        {
            return historicalValues;
        }

        private void setHistoricalValues(final Set<String> historicalValues)
        {
            this.historicalValues = historicalValues;
        }
    }

}
