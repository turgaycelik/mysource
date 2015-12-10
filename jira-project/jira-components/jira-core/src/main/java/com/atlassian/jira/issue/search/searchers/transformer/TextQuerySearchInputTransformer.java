package com.atlassian.jira.issue.search.searchers.transformer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.transport.ActionParams;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.jql.ClauseInformation;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.util.ParameterUtils;
import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

/**
 * A transformer that handles the 2012 Issue Search interface.
 *
 * @since v5.2
 */
public class TextQuerySearchInputTransformer extends AbstractSearchInputTransformer
{
    private ClauseNames clauseNames;

    public TextQuerySearchInputTransformer(final String id, final ClauseInformation information,
            final JqlOperandResolver operandResolver)
    {
        super(operandResolver, id, id);
        this.clauseNames = information.getJqlClauseNames();
    }

    @Override
    public boolean doRelevantClausesFitFilterForm(final User user, final Query query, final SearchContext searchContext)
    {
        if (query != null && query.getWhereClause() != null)
        {
            final Clause whereClause = query.getWhereClause();

            TextQueryValidatingVisitor visitor = new TextQueryValidatingVisitor(clauseNames.getPrimaryName());

            whereClause.accept(visitor);
            if (visitor.isValid()) {
                final String textQuery = visitor.getTextTerminalValue(operandResolver, user);
                return textQuery != null;
            }
        }
        return false;
    }

    @Override
    public void populateFromParams(final User user, final FieldValuesHolder fieldValuesHolder, final ActionParams actionParams)
    {
        fieldValuesHolder.put(fieldsKey, actionParams.getFirstValueForKey(id));
    }

    @Override
    public void populateFromQuery(final User user, final FieldValuesHolder fieldValuesHolder, final Query query, final SearchContext searchContext)
    {
        if (query != null && query.getWhereClause() != null)
        {
            final Clause whereClause = query.getWhereClause();

            TextQueryValidatingVisitor visitor = new TextQueryValidatingVisitor(clauseNames.getPrimaryName());

            whereClause.accept(visitor);
            if (visitor.isValid()) {
                final String textQuery = visitor.getTextTerminalValue(operandResolver, user);
                if (textQuery != null) {
                    fieldValuesHolder.put(fieldsKey, textQuery.trim());
                }
            }
        }
    }

    @Override
    public Clause getSearchClause(final User user, final FieldValuesHolder fieldValuesHolder)
    {
        String query = ParameterUtils.getStringParam(fieldValuesHolder, fieldsKey);
        if (query != null)
        {
            return new TerminalClauseImpl(clauseNames.getPrimaryName(), Operator.LIKE, query);
        }
        else
        {
            return null;
        }
    }

}
