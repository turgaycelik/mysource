package com.atlassian.jira.issue.search.searchers.transformer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.issue.label.LabelParser;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.util.DefaultIndexedInputHelper;
import com.atlassian.jira.issue.search.searchers.util.IndexedInputHelper;
import com.atlassian.jira.issue.transport.ActionParams;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.IndexInfoResolver;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;

import java.util.Collection;
import java.util.Set;

/**
 * The SearchInputTransformer for the Labels system field.
 *
 * @since v4.2
 */
public class LabelsSearchInputTransformer implements SearchInputTransformer
{
    private final IndexInfoResolver<Label> indexInfoResolver;
    private final FieldFlagOperandRegistry fieldFlagOperandRegistry;
    private final JqlOperandResolver operandResolver;

    public LabelsSearchInputTransformer(final IndexInfoResolver<Label> indexInfoResolver, final JqlOperandResolver operandResolver,
            final FieldFlagOperandRegistry fieldFlagOperandRegistry)
    {
        this.indexInfoResolver = indexInfoResolver;
        this.fieldFlagOperandRegistry = fieldFlagOperandRegistry;
        this.operandResolver = operandResolver;
    }

    public void populateFromParams(final User user, final FieldValuesHolder fieldValuesHolder, final ActionParams actionParams)
    {
        final String url = SystemSearchConstants.forLabels().getUrlParameter();
        final String[] labels = actionParams.getValuesForKey(url);
        if (labels != null)
        {
            fieldValuesHolder.put(url, labels);
        }
    }

    public void validateParams(final User user, final SearchContext searchContext, final FieldValuesHolder fieldValuesHolder, final I18nHelper i18nHelper, final ErrorCollection errors)
    {
        final String url = SystemSearchConstants.forLabels().getUrlParameter();
        final Collection<String> labels = (Collection<String>) fieldValuesHolder.get(url);
        if (labels != null)
        {
            for (String label : labels)
            {
                if (!LabelParser.isValidLabelName(label))
                {
                    errors.addErrorMessage(i18nHelper.getText("label.service.error.label.invalid", label));
                }
                if (label.length() > LabelParser.MAX_LABEL_LENGTH)
                {
                    errors.addErrorMessage(i18nHelper.getText("label.service.error.label.toolong", label));
                }
            }
        }
    }

    public void populateFromQuery(final User user, final FieldValuesHolder fieldValuesHolder, final Query query, final SearchContext searchContext)
    {
        final String url = SystemSearchConstants.forLabels().getUrlParameter();
        final Collection<String> cleanValues = Collections2.transform(getNavigatorValuesAsStrings(user, query, searchContext), new Function<String, String>()
        {
            @Override
            public String apply(String input)
            {
                return input.trim();
            }
        });
        fieldValuesHolder.put(url, cleanValues);
    }

    Set<String> getNavigatorValuesAsStrings(User searcher, Query query, SearchContext searchContext)
    {
        IndexedInputHelper helper = new DefaultIndexedInputHelper<Label>(indexInfoResolver, operandResolver, fieldFlagOperandRegistry);
        return helper.getAllNavigatorValuesForMatchingClauses(searcher, SystemSearchConstants.forLabels().getJqlClauseNames(), query);
    }

    public boolean doRelevantClausesFitFilterForm(final User user, final Query query, final SearchContext searchContext)
    {
        return createNavigatorStructureChecker().checkSearchRequest(query);
    }

    private NavigatorStructureChecker<Label> createNavigatorStructureChecker()
    {
        return new NavigatorStructureChecker<Label>(SystemSearchConstants.forLabels().getJqlClauseNames(), false, fieldFlagOperandRegistry, operandResolver);
    }

    public Clause getSearchClause(final User user, final FieldValuesHolder fieldValuesHolder)
    {
        final String url = SystemSearchConstants.forLabels().getUrlParameter();
        final Collection<String> labels = (Collection<String>) fieldValuesHolder.get(url);
        if (labels != null && !labels.isEmpty())
        {
            final String primaryName = SystemSearchConstants.forLabels().getJqlClauseNames().getPrimaryName();
            if (labels.size() == 1)
            {
                final String label = Iterables.getOnlyElement(labels).trim();
                return new TerminalClauseImpl(primaryName, Operator.EQUALS, new SingleValueOperand(label));
            }
            else
            {
                final Collection<Operand> operands = Collections2.transform(labels, new Function<String, Operand>()
                {
                    @Override
                    public Operand apply(String label)
                    {
                        return new SingleValueOperand(label.trim());
                    }
                });
                return new TerminalClauseImpl(primaryName, Operator.IN, new MultiValueOperand(operands));
            }
        }
        return null;
    }
}
