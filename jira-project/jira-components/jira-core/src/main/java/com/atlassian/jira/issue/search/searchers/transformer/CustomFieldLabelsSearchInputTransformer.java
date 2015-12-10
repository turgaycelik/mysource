package com.atlassian.jira.issue.search.searchers.transformer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.customfields.searchers.transformer.AbstractCustomFieldSearchInputTransformer;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.LabelsSystemField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The SearchInputTransformer for the Labels custom field.
 *
 * @since v4.2
 */
public class CustomFieldLabelsSearchInputTransformer extends AbstractCustomFieldSearchInputTransformer
{
    private final ClauseNames clauseNames;

    public CustomFieldLabelsSearchInputTransformer(CustomField field, String urlParameterName,
            final CustomFieldInputHelper customFieldInputHelper, final ClauseNames clauseNames)
    {
        super(field, urlParameterName, customFieldInputHelper);
        this.clauseNames = clauseNames;
    }

    @Override
    protected Clause getClauseFromParams(final User user, final CustomFieldParams customFieldParams)
    {
        @SuppressWarnings("unchecked")
        Collection<String> searchValuesUnTokenized = customFieldParams.getValuesForNullKey();
        if (null != searchValuesUnTokenized && !searchValuesUnTokenized.isEmpty())
        {
            final Set<String> searchStrings = new HashSet<String>();
            for (String string : searchValuesUnTokenized)
            {
                final String[] strings = StringUtils.split(string, LabelsSystemField.SEPARATOR_CHAR);
                searchStrings.addAll(Arrays.asList(strings));
            }

            if (searchStrings.size() == 1)
            {
                final String sanitizedLabel = searchStrings.iterator().next().trim();
                return new TerminalClauseImpl(getClauseName(user, clauseNames), Operator.EQUALS, new SingleValueOperand(sanitizedLabel));
            }
            else
            {
                final List<Operand> operands = new ArrayList<Operand>();
                for (String label : searchStrings)
                {
                    final String sanitizedLabel = label.trim();
                    operands.add(new SingleValueOperand(sanitizedLabel.trim()));
                }
                return new TerminalClauseImpl(getClauseName(user, clauseNames), Operator.IN, new MultiValueOperand(operands));
            }
        }
        return null;
    }

    @Override
    protected CustomFieldParams getParamsFromSearchRequest(final User user, final Query query, final SearchContext searchContext)
    {
        final QueryInputPlaceHolder queryInputPlaceHolder = getQueryInputPlaceHolder(query);
        return queryInputPlaceHolder.isConveratableToSimple() ? new CustomFieldParamsImpl(getCustomField(), queryInputPlaceHolder.getValues()) : null;
    }

    public boolean doRelevantClausesFitFilterForm(final User user, final Query query, final SearchContext searchContext)
    {
        return getQueryInputPlaceHolder(query).isConveratableToSimple();
    }

    private QueryInputPlaceHolder getQueryInputPlaceHolder(Query query)
    {
        final SimpleNavigatorCollectorVisitor labelsClauseVisitor = new SimpleNavigatorCollectorVisitor(clauseNames.getJqlFieldNames());

        if (query != null && query.getWhereClause() != null)
        {
            query.getWhereClause().accept(labelsClauseVisitor);

            if (labelsClauseVisitor.isValid())
            {
                Collection<TerminalClause> terminalClauses = labelsClauseVisitor.getClauses();
                Set<String> searchValues = new LinkedHashSet<String>(terminalClauses.size());

                boolean queryFitsSimple = true;
                for (TerminalClause terminalClause : terminalClauses)
                {
                    if (terminalClause.getOperator().equals(Operator.EQUALS) || terminalClause.getOperator().equals(Operator.IN))
                    {
                        final Operand operand = terminalClause.getOperand();
                        if (operand instanceof SingleValueOperand)
                        {
                            searchValues.add(getOperandValue(operand));
                        }
                        else if (operand instanceof MultiValueOperand)
                        {
                            final List<Operand> list = ((MultiValueOperand) operand).getValues();
                            for (Operand singleOperand : list)
                            {
                                searchValues.add(getOperandValue(singleOperand));
                            }
                        }
                    }
                    else
                    {
                        queryFitsSimple = false;
                    }
                }

                return new QueryInputPlaceHolder(queryFitsSimple, searchValues);
            }
        }

        return new QueryInputPlaceHolder(false, Collections.<String>emptySet());
    }

    private String getOperandValue(Operand operand)
    {
        SingleValueOperand singleValueOperand = (SingleValueOperand) operand;

        if (singleValueOperand.getStringValue() != null)
        {
            return singleValueOperand.getStringValue();
        }

        return singleValueOperand.getLongValue().toString();
    }

    private static class QueryInputPlaceHolder
    {
        private final boolean converatableToSimple;
        private final Set<String> values;

        private QueryInputPlaceHolder(boolean converatableToSimple, Set<String> values)
        {
            this.converatableToSimple = converatableToSimple;
            this.values = values;
        }

        public boolean isConveratableToSimple()
        {
            return converatableToSimple;
        }

        public Set<String> getValues()
        {
            return values;
        }
    }
}
