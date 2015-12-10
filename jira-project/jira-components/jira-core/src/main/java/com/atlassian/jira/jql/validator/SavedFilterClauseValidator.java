package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.resolver.SavedFilterResolver;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.Operand;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * A validator that checks to see if a saved filter exists and is visible to the user creating the search.
 *
 * @since v4.0
 */
public class SavedFilterClauseValidator implements ClauseValidator
{
    private final SupportedOperatorsValidator supportedOperatorsValidator;
    private final SavedFilterResolver savedFilterResolver;
    private final JqlOperandResolver jqlOperandResolver;
    private final SavedFilterCycleDetector savedFilterCycleDetector;

    public SavedFilterClauseValidator(final SavedFilterResolver savedFilterResolver, final JqlOperandResolver jqlOperandResolver,
            final SavedFilterCycleDetector savedFilterCycleDetector)
    {
        this.savedFilterResolver = savedFilterResolver;
        this.jqlOperandResolver = jqlOperandResolver;
        this.savedFilterCycleDetector = savedFilterCycleDetector;
        this.supportedOperatorsValidator = getSupportedOperatorsValidator();
    }

    public MessageSet validate(final User searcher, final TerminalClause terminalClause, Long filterId)
    {
        final MessageSet messageSet = supportedOperatorsValidator.validate(searcher, terminalClause);
        if (!messageSet.hasAnyErrors())
        {
            final Operand operand = terminalClause.getOperand();
            final List<QueryLiteral> rawValues = jqlOperandResolver.getValues(searcher, operand, terminalClause);
            if (rawValues != null)
            {
                // Now lets look up the filter and see if it exists
                final I18nHelper i18n = getI18n(searcher);

                for (QueryLiteral rawValue : rawValues)
                {
                    if (rawValue.isEmpty())
                    {
                        // we got an empty operand inside a multi value operand or function
                        if (jqlOperandResolver.isFunctionOperand(rawValue.getSourceOperand()))
                        {
                            messageSet.addErrorMessage(i18n.getText("jira.jql.clause.field.does.not.support.empty.from.func", terminalClause.getName(), rawValue.getSourceOperand().getName()));
                        }
                        else
                        {
                            messageSet.addErrorMessage(i18n.getText("jira.jql.clause.field.does.not.support.empty", terminalClause.getName()));
                        }
                        continue;
                    }

                    // Get a filter for each individual raw value and see if it resolves
                    final List<SearchRequest> matchingFilters = savedFilterResolver.getSearchRequest(searcher, Collections.singletonList(rawValue));
                    if (matchingFilters.isEmpty())
                    {
                        if (rawValue.getStringValue() != null)
                        {
                            if (jqlOperandResolver.isFunctionOperand(rawValue.getSourceOperand()))
                            {
                                messageSet.addErrorMessage(i18n.getText("jira.jql.clause.no.value.for.name.from.function", rawValue.getSourceOperand().getName(), terminalClause.getName()));
                            }
                            else
                            {
                                messageSet.addErrorMessage(i18n.getText("jira.jql.clause.no.value.for.name", terminalClause.getName(), rawValue.toString()));
                            }
                        }
                        else if (rawValue.getLongValue() != null)
                        {
                            if (jqlOperandResolver.isFunctionOperand(rawValue.getSourceOperand()))
                            {
                                messageSet.addErrorMessage(i18n.getText("jira.jql.clause.no.value.for.name.from.function", rawValue.getSourceOperand().getName(), terminalClause.getName()));
                            }
                            else
                            {
                                messageSet.addErrorMessage(i18n.getText("jira.jql.clause.no.value.for.id", terminalClause.getName(), rawValue.toString()));
                            }
                        }
                    }
                    else
                    {
                        // Run through and make sure the filters don't contain any cycles
                        for (SearchRequest matchingFilter : matchingFilters)
                        {
                            if (savedFilterCycleDetector.containsSavedFilterReference(searcher, false, matchingFilter, filterId))
                            {
                                messageSet.addErrorMessage(i18n.getText("jira.jql.saved.filter.detected.cycle", terminalClause.getName(), rawValue.toString(), matchingFilter.getName()));
                            }
                        }
                    }
                }
            }
        }
        return messageSet;
    }

    @Nonnull
    public MessageSet validate(final User searcher, @Nonnull final TerminalClause terminalClause)
    {
        return validate(searcher, terminalClause, null);
    }

    SupportedOperatorsValidator getSupportedOperatorsValidator()
    {
        return new SupportedOperatorsValidator(OperatorClasses.EQUALITY_OPERATORS);
    }

    ///CLOVER:OFF
    I18nHelper getI18n(User user)
    {
        return new I18nBean(user);
    }
    ///CLOVER:ON
}
