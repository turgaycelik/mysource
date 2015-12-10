package com.atlassian.jira.issue.customfields.searchers.transformer;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.searchers.transformer.SimpleNavigatorCollectorVisitor;
import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import java.util.Collection;
import java.util.Collections;

/**
 * An abstract Search Input Transformer for custom fields that only allow a single value and no functions
 * in the Navigator.
 *
 * @since v4.0
 */
@PublicSpi
public abstract class AbstractSingleValueCustomFieldSearchInputTransformer extends AbstractCustomFieldSearchInputTransformer
{
    private final ClauseNames clauseNames;

    public AbstractSingleValueCustomFieldSearchInputTransformer(CustomField field, ClauseNames clauseNames, String urlParameterName, final CustomFieldInputHelper customFieldInputHelper)
    {
        super(field, urlParameterName, customFieldInputHelper);
        this.clauseNames = clauseNames;
    }

    protected Clause getClauseFromParams(final User user, final CustomFieldParams customFieldParams)
    {
        Collection<String> searchValues = customFieldParams.getValuesForNullKey();
        if (searchValues.size() == 1)
        {
            return createSearchClause(user, searchValues.iterator().next());
        }
        return null;
    }

    protected CustomFieldParams getParamsFromSearchRequest(final User user, final Query query, final SearchContext searchContext)
    {
        final NavigatorConversionResult result = convertForNavigator(query);
        if (result.fitsNavigator() && result.getValue() != null)
        {
            String stringValue = result.getValue().getStringValue() == null ? result.getValue().getLongValue().toString() : result.getValue().getStringValue();
            return new CustomFieldParamsImpl(getCustomField(), Collections.singleton(stringValue));
        }
        return null;
    }

    Clause createSearchClause(final User user, String value)
    {
        return new TerminalClauseImpl(getClauseName(user, clauseNames), Operator.EQUALS, value);
    }

    /**
     * Checks if the {@link SearchRequest} fits the navigator for a single value custom field and
     * retrieves the single value for the clause from the {@link SearchRequest}.
     *
     * @param query defines the search criteria to convert.
     * @return returns a {@link NavigatorConversionResult}.
     */
    NavigatorConversionResult convertForNavigator(final Query query)
    {
        SimpleNavigatorCollectorVisitor collectorVisitor = createSimpleNavigatorCollectorVisitor();
        final NavigatorConversionResult result;
        if (query != null && query.getWhereClause() != null)
        {
            query.getWhereClause().accept(collectorVisitor);
            if (!collectorVisitor.isValid())
            {
                result = new NavigatorConversionResult(false, null);
            }
            else if (collectorVisitor.getClauses().isEmpty())
            {
                result = new NavigatorConversionResult(true, null);
            }
            else if (collectorVisitor.getClauses().size() == 1 &&
                    checkOperand(collectorVisitor.getClauses().get(0).getOperator()) &&
                    collectorVisitor.getClauses().get(0).getOperand() instanceof SingleValueOperand)
            {
                result = new NavigatorConversionResult(true, (SingleValueOperand)collectorVisitor.getClauses().get(0).getOperand());
            }
            else
            {
               result = new NavigatorConversionResult(false, null);
            }
        }
        else
        {
            result = new NavigatorConversionResult(true, null);
        }
        return result;
    }

    SimpleNavigatorCollectorVisitor createSimpleNavigatorCollectorVisitor()
    {
        return new SimpleNavigatorCollectorVisitor(clauseNames.getJqlFieldNames());
    }

    private boolean checkOperand(final Operator operator)
    {
        return operator == Operator.EQUALS || operator == Operator.IS || operator == Operator.LIKE || operator == Operator.IN;
    }
}
