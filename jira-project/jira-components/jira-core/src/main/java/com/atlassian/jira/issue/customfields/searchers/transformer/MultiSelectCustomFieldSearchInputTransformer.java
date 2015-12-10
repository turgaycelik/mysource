package com.atlassian.jira.issue.customfields.searchers.transformer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.QueryContextConverter;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.OptionUtils;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.search.searchers.transformer.SimpleNavigatorCollectorVisitor;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.jql.context.QueryContext;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * The {@link com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer} for custom fields with multi-select searchers (multi-select and check boxes)
 *
 * @since v4.0
 */
public class MultiSelectCustomFieldSearchInputTransformer extends AbstractCustomFieldSearchInputTransformer implements SearchInputTransformer
{
    private final ClauseNames clauseNames;
    private final CustomField field;
    private final JqlOperandResolver jqlOperandResolver;
    private final JqlSelectOptionsUtil jqlSelectOptionsUtil;
    private final QueryContextConverter queryContextConverter;

    public MultiSelectCustomFieldSearchInputTransformer(String urlParameterName, final ClauseNames clauseNames, CustomField field,
            final JqlOperandResolver jqlOperandResolver, final JqlSelectOptionsUtil jqlSelectOptionsUtil,
            final QueryContextConverter queryContextConverter, final CustomFieldInputHelper customFieldInputHelper)
    {
        super(field, urlParameterName, customFieldInputHelper);
        this.queryContextConverter = notNull("queryContextConverter", queryContextConverter);
        this.jqlSelectOptionsUtil = notNull("jqlSelectOptionsUtil", jqlSelectOptionsUtil);
        this.jqlOperandResolver = notNull("jqlOperandResolver", jqlOperandResolver);
        this.field = notNull("field", field);
        this.clauseNames = notNull("clauseNames", clauseNames);
    }

    protected Clause getClauseFromParams(final User user, CustomFieldParams params)
    {
        Collection<String> searchValues = params.getValuesForNullKey();

        // Remove the "all" flag from the input
        searchValues.removeAll(CollectionBuilder.newBuilder("-1", "").asCollection());
        if (!searchValues.isEmpty())
        {
            List<String> termValues = new ArrayList();
            // turn the ids into values
            for (String value : searchValues)
            {
                Long longValue = null;
                String transformedValue = value;
                longValue = OptionUtils.safeParseLong(value);
                Option option = jqlSelectOptionsUtil.getOptionById(longValue);
                if (option != null)
                {
                    transformedValue = option.getValue();
                }
                termValues.add(transformedValue);
            }
            if (termValues.size() > 0)
            {
                return new TerminalClauseImpl(getClauseName(user, clauseNames), termValues.toArray(new String[termValues.size()]));
            }
        }
        return null;
    }

    @Override
    protected CustomFieldParams getParamsFromSearchRequest(final User user, final Query query, final SearchContext searchContext)
    {
        if (query != null)
        {
            if(query.getWhereClause() != null)
            {
                SimpleNavigatorCollectorVisitor visitor = new SimpleNavigatorCollectorVisitor(clauseNames.getJqlFieldNames());
                query.getWhereClause().accept(visitor);

                // check that structure is valid and we only have one clause
                if (visitor.isValid() && visitor.getClauses().size() == 1)
                {
                    final TerminalClause clause = visitor.getClauses().get(0);

                    // check that we have a valid operator
                    if (isValidOperatorForFitness(clause.getOperator()))
                    {
                        // check that we have values but no EmptyLiterals
                        final List<QueryLiteral> literals = jqlOperandResolver.getValues(user, clause.getOperand(), clause);
                        if (literals != null && !literals.contains(new QueryLiteral()))
                        {
                            final Set<String> valuesAsStrings = new HashSet<String>();

                            // check that all the options are available in the context of the query
                            for (QueryLiteral literal : literals)
                            {
                                List<Option> options = jqlSelectOptionsUtil.getOptions(field, literal, false);
                                if (options.isEmpty())
                                {
                                    return null;
                                }
                                for (Option option : options)
                                {
                                    valuesAsStrings.add(option.getOptionId().toString());
                                }
                            }
                            if (valuesAsStrings.isEmpty())
                            {
                                return null;
                            }
                            else
                            {
                                return new CustomFieldParamsImpl(getCustomField(), valuesAsStrings);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    protected boolean isValidOperatorForFitness(Operator operator)
    {
        return operator == Operator.EQUALS || operator == Operator.IS || operator == Operator.IN;
    }

    ///CLOVER:OFF

    public boolean doRelevantClausesFitFilterForm(final User user, final Query query, final SearchContext searchContext)
    {
        return getParamsFromSearchRequest(user, query, searchContext) != null;
    }

    @Override
    public void validateParams(final User user, final SearchContext searchContext, final FieldValuesHolder fieldValuesHolder, final I18nHelper i18nHelper, final ErrorCollection errors)
    {
        // don't bother validating (from old searcher)
    }
}
