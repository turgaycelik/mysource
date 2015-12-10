package com.atlassian.jira.issue.customfields.searchers.transformer;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.customfields.converters.GroupConverter;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.search.searchers.transformer.SimpleNavigatorCollectorVisitor;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * The {@link com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer} for group cusotmfields
 *
 * @since v4.0
 */
public class MultiGroupCustomFieldSearchInputTransformer extends AbstractCustomFieldSearchInputTransformer
        implements SearchInputTransformer
{
    private final ClauseNames clauseNames;
    private final JqlOperandResolver jqlOperandResolver;
    private final GroupConverter groupConverter;

    public MultiGroupCustomFieldSearchInputTransformer(String urlParameterName, final ClauseNames clauseNames, CustomField field,
            final JqlOperandResolver jqlOperandResolver, final CustomFieldInputHelper customFieldInputHelper, GroupConverter groupConverter)
    {
        super(field, urlParameterName, customFieldInputHelper);
        this.groupConverter = notNull("groupConverter", groupConverter);
        this.jqlOperandResolver = notNull("jqlOperandResolver", jqlOperandResolver);
        this.clauseNames = notNull("clauseNames", clauseNames);
    }

    protected Clause getClauseFromParams(final User user, CustomFieldParams params)
    {
        Collection<String> searchValues = params.getValuesForNullKey();

        // Remove the "all" flag from the input
        searchValues.removeAll(CollectionBuilder.newBuilder("-1", "").asCollection());
        if (!searchValues.isEmpty())
        {
            return new TerminalClauseImpl(getClauseName(user, clauseNames), searchValues.toArray(new String[searchValues.size()]));
        }
        return null;
    }

    @Override
    protected CustomFieldParams getParamsFromSearchRequest(final User user, final Query query, final SearchContext searchContext)
    {
        final Collection<String> groupStrings = getGroupsFromQuery(user, query);
        if (groupStrings == null || groupStrings.isEmpty())
        {
            return null;
        }

        final Collection<String> validGroups = getValidGroups(groupStrings);
        if (validGroups == null || validGroups.isEmpty())
        {
            return null;
        }

        return new CustomFieldParamsImpl(getCustomField(), validGroups);
    }

    private Collection<String> getValidGroups(Collection<String> rawGroups)
    {
        final CollectionBuilder<String> builder = CollectionBuilder.newBuilder();
        for (String rawGroup : rawGroups)
        {
            try
            {
                Group group = groupConverter.getGroup(rawGroup);
                if (group != null)
                {
                    builder.add(rawGroup);
                }
            }
            catch (FieldValidationException e)
            {
                // Ignore group doesn't exist.
            }
        }
        return builder.asList();
    }

    private Collection<String> getGroupsFromQuery(final User user, final Query query)
    {
        if (query != null)
        {
            if (query.getWhereClause() != null)
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
                            final Set<String> valuesAsStrings = new LinkedHashSet<String>();

                            for (QueryLiteral literal : literals)
                            {
                                valuesAsStrings.add(literal.asString());
                            }

                            return valuesAsStrings;
                        }
                    }
                }
            }
        }
        return null;
    }

    protected boolean isValidOperatorForFitness(Operator operator)
    {
        return operator == Operator.EQUALS || operator == Operator.IN;
    }

    ///CLOVER:OFF

    public boolean doRelevantClausesFitFilterForm(final User user, final Query query, final SearchContext searchContext)
    {
        final Collection<String> rawGroups = getGroupsFromQuery(user, query);

        // if there are no groups specified, it doesn't fit.
        if (rawGroups == null)
        {
            return false;
        }

        final Collection<String> validGroups = getValidGroups(rawGroups);

        // if there are any invalid groups, it doesn't fit.
        return validGroups != null && rawGroups.size() == validGroups.size();
    }

    @Override
    public void validateParams(final User user, final SearchContext searchContext, final FieldValuesHolder fieldValuesHolder, final I18nHelper i18nHelper, final ErrorCollection errors)
    {
        // don't bother validating (from old searcher)
    }

    ///CLOVER:ON
}
