package com.atlassian.jira.issue.customfields.searchers.transformer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.QueryContextConverter;
import com.atlassian.jira.issue.customfields.converters.SelectConverter;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.OptionUtils;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.search.searchers.transformer.SimpleNavigatorCollectorVisitor;
import com.atlassian.jira.jql.context.QueryContext;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;
import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import java.util.Collections;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * The {@link com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer} for select custom fields.
 *
 * @since v4.0
 */
public class SelectCustomFieldSearchInputTransformer extends AbstractSingleValueCustomFieldSearchInputTransformer implements SearchInputTransformer
{
    private final ClauseNames clauseNames;
    private final CustomField customField;
    private final JqlSelectOptionsUtil jqlSelectOptionsUtil;
    private final QueryContextConverter queryContextConverter;
    private final JqlOperandResolver jqlOperandResolver;

    public SelectCustomFieldSearchInputTransformer(final CustomField field, final ClauseNames clauseNames,
            final String urlParameterName, final JqlSelectOptionsUtil jqlSelectOptionsUtil,
            final QueryContextConverter queryContextConverter, final JqlOperandResolver jqlOperandResolver,
            final CustomFieldInputHelper customFieldInputHelper)
    {
        super(field, clauseNames, urlParameterName, customFieldInputHelper);
        this.clauseNames = notNull("clauseNames", clauseNames);
        this.queryContextConverter = notNull("queryContextConverter", queryContextConverter);
        this.jqlSelectOptionsUtil = notNull("jqlSelectOptionsUtil", jqlSelectOptionsUtil);
        this.jqlOperandResolver = notNull("jqlOperandResolver", jqlOperandResolver);
        this.customField = notNull("field", field);
    }

    @Override
    Clause createSearchClause(final User user, final String value)
    {
        if (value != null && !value.equals(SelectConverter.ALL_STRING))
        {
            Long longValue;
            String transformedValue = value;
            longValue = OptionUtils.safeParseLong(value);
            Option option = jqlSelectOptionsUtil.getOptionById(longValue);
            if (option != null)
            {
                transformedValue = option.getValue();
            }
            return new TerminalClauseImpl(getClauseName(user, clauseNames), Operator.EQUALS, transformedValue);
        }
    	return null;
    }

	@Override
    protected CustomFieldParams getParamsFromSearchRequest(final User user, final Query query, final SearchContext searchContext)
    {
        if (query != null && query.getWhereClause() != null)
        {
            SimpleNavigatorCollectorVisitor visitor = createSimpleNavigatorCollectingVisitor();
            query.getWhereClause().accept(visitor);

            // check that the structure is valid
            if (!visitor.isValid())
            {
                return null;
            }
            final List<TerminalClause> clauses = visitor.getClauses();

            // check that we only have one clause
            if (clauses.size() != 1)
            {
                return null;
            }

            final TerminalClause clause = clauses.get(0);

            // check that we have a valid operator
            final Operator operator = clause.getOperator();
            if (operator != Operator.EQUALS && operator != Operator.IS && operator != Operator.IN)
            {
                return null;
            }

            // check that a single value is resolved and that it is non-negative (no means to represent negative search)
            final List<QueryLiteral> literals = jqlOperandResolver.getValues(user, clause.getOperand(), clause);
            if (literals == null || literals.size() != 1 )
            {
                return null;
            }

            // check that we are searching for non-empty value
            final QueryLiteral literal = literals.get(0);
            if (literal.isEmpty())
            {
                return null;
            }

            // check that the options resolved are in context
            final QueryContext queryContext = queryContextConverter.getQueryContext(searchContext);
            List<Option> options = jqlSelectOptionsUtil.getOptions(customField, queryContext, literal, true);

            if (options.size() > 1)
            {
                return null;
            }


            if (options.size() == 0)
            {
                return null;
            }
            else
            {
            	final Option option = options.get(0);
            	return new CustomFieldParamsImpl(customField, Collections.singleton(option.getOptionId().toString()));
            }
        }

        return null;
    }

    public boolean doRelevantClausesFitFilterForm(final User user, final Query query, final SearchContext searchContext)
    {
        final NavigatorConversionResult result = convertForNavigator(query);
        if (result.fitsNavigator() && result.getValue() != null)
        {
            return getOptionsFromValue(result.getValue(), searchContext).size() <= 1;
        }
        else
        {
            return result.fitsNavigator();
        }
    }

    private List<Option> getOptionsFromValue(final SingleValueOperand operand, final SearchContext searchContext)
    {
        String stringValue = operand.getStringValue();
        return jqlSelectOptionsUtil.getOptions(customField, queryContextConverter.getQueryContext(searchContext), new QueryLiteral(operand, stringValue), true);
    }

    SimpleNavigatorCollectorVisitor createSimpleNavigatorCollectingVisitor()
    {
        return new SimpleNavigatorCollectorVisitor(clauseNames.getJqlFieldNames());
    }
}
