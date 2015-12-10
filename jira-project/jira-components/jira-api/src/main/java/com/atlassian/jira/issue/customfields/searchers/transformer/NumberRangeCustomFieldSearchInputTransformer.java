package com.atlassian.jira.issue.customfields.searchers.transformer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.customfields.converters.DoubleConverter;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.customfields.searchers.NumberRangeSearcher;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.query.Query;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The {@link com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer} for number range searcher.
 *
 * @since v4.0
 */
public class NumberRangeCustomFieldSearchInputTransformer extends AbstractCustomFieldSearchInputTransformer implements SearchInputTransformer
{
    private final ClauseNames clauseNames;
    private final DoubleConverter doubleConverter;
    private final JqlOperandResolver jqlOperandResolver;
    private final NumberRangeCustomFieldInputHelper inputHelper;

    public NumberRangeCustomFieldSearchInputTransformer(final ClauseNames clauseNames, final CustomField field, final String urlParameterName,
            final DoubleConverter doubleConverter, final JqlOperandResolver jqlOperandResolver, final CustomFieldInputHelper customFieldInputHelper)
    {
        super(field, urlParameterName, customFieldInputHelper);
        this.clauseNames = clauseNames;
        this.doubleConverter = doubleConverter;
        this.jqlOperandResolver = jqlOperandResolver;
        this.inputHelper = createInputHelper(clauseNames, jqlOperandResolver);
    }

    @Override
    public void validateParams(final User user, final SearchContext searchContext, final FieldValuesHolder fieldValuesHolder, final I18nHelper i18nHelper, final ErrorCollection errors)
    {
        CustomFieldParams params = (CustomFieldParams) fieldValuesHolder.get(getCustomField().getId());
        if (params != null)
        {
            String[] rangeParams = { NumberRangeSearcher.LESS_THAN_PARAM, NumberRangeSearcher.GREATER_THAN_PARAM};
            for (String rangeParam : rangeParams)
            {
                final String numberString = (String) params.getFirstValueForKey(rangeParam);
                if (StringUtils.isNotEmpty(numberString))
                {
                    try
                    {
                        doubleConverter.getDouble(numberString);
                    }
                    catch (FieldValidationException e)
                    {
                        errors.addError(getCustomField().getId(), e.getMessage());
                    }
                }
            }
        }
    }

    protected Clause getClauseFromParams(final User user, final CustomFieldParams customFieldParams)
    {
        List<Clause> clauses = new ArrayList<Clause>();

        final String clauseName = getClauseName(user, clauseNames);
        if (customFieldParams.containsKey(NumberRangeSearcher.LESS_THAN_PARAM))
        {
            String s = (String) customFieldParams.getFirstValueForKey(NumberRangeSearcher.LESS_THAN_PARAM);
            if (s != null)
            {
                clauses.add(new TerminalClauseImpl(clauseName, Operator.LESS_THAN_EQUALS, s));
            }
        }

        if (customFieldParams.containsKey(NumberRangeSearcher.GREATER_THAN_PARAM))
        {
            String s = (String) customFieldParams.getFirstValueForKey(NumberRangeSearcher.GREATER_THAN_PARAM);
            if (s != null)
            {
                clauses.add(new TerminalClauseImpl(clauseName, Operator.GREATER_THAN_EQUALS, s));
            }
        }

        if (clauses.isEmpty())
        {
            return null;
        }
        else if (clauses.size() == 1)
        {
            return clauses.get(0);
        }
        else
        {
            return new AndClause(clauses);
        }
    }

    protected CustomFieldParams getParamsFromSearchRequest(final User user, final Query query, final SearchContext searchContext)
    {
        if (query != null && query.getWhereClause() != null)
        {
            final List<TerminalClause> clauses = inputHelper.getValuesFromQuery(query);
            if (clauses != null && !clauses.isEmpty())
            {
                Map<String, Collection<String>> params = new HashMap<String, Collection<String>>();
                for (TerminalClause clause : clauses)
                {
                    List<QueryLiteral> values = jqlOperandResolver.getValues(user, clause.getOperand(), clause);

                    // we could have somehow got multiple or empty literals which are not allowed - return null
                    if (values == null || values.size() != 1 || values.get(0).isEmpty())
                    {
                        return null;
                    }

                    String value = values.get(0).asString();
                    if (clause.getOperator() == Operator.GREATER_THAN_EQUALS)
                    {
                        params.put(NumberRangeSearcher.GREATER_THAN_PARAM, Collections.singleton(value));
                    }
                    else if (clause.getOperator() == Operator.LESS_THAN_EQUALS)
                    {
                        params.put(NumberRangeSearcher.LESS_THAN_PARAM, Collections.singleton(value));
                    }
                    else
                    {
                        throw new IllegalStateException("Invalid operator in retreiving params from search request");
                    }
                }
                return new CustomFieldParamsImpl(getCustomField(), params);
            }
        }
        return null;
    }

    public boolean doRelevantClausesFitFilterForm(final User user, final Query query, final SearchContext searchContext)
    {
        return query.getWhereClause() == null || inputHelper.getValuesFromQuery(query) != null;
    }

    ///CLOVER:OFF
    NumberRangeCustomFieldInputHelper createInputHelper(final ClauseNames clauseNames, final JqlOperandResolver jqlOperandResolver)
    {
        return new NumberRangeCustomFieldInputHelper(clauseNames, jqlOperandResolver);
    }
    ///CLOVER:ON
}
