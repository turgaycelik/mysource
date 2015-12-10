package com.atlassian.jira.issue.search.searchers.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstants;
import com.atlassian.jira.issue.search.searchers.transformer.SimpleNavigatorCollectorVisitor;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.util.NonInjectableComponent;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operator.Operator;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Helper class to parse JQL clauses and determine if they are suitable for usage in the Navigator or Search URL.
 *
 * @since v4.0
 */
@NonInjectableComponent
public class DefaultWorkRatioSearcherInputHelper implements WorkRatioSearcherInputHelper
{
    private final SimpleFieldSearchConstants constants;
    private final WorkRatioSearcherConfig config;
    private final JqlOperandResolver operandResolver;

    public DefaultWorkRatioSearcherInputHelper(final SimpleFieldSearchConstants constants, final JqlOperandResolver operandResolver)
    {
        this.constants = notNull("constants", constants);
        this.config = new WorkRatioSearcherConfig(constants.getSearcherId());
        this.operandResolver = notNull("operandResolver", operandResolver);
    }

    public Map<String, String> convertClause(final Clause clause, final User user)
    {
        if (clause == null)
        {
            return null;
        }

        // Lets get all the clauses for this field.
        final List<TerminalClause> clauseList = validateClauseStructure(clause);
        if (clauseList == null)
        {
            return null;
        }

        //we have some time related clauses, so lets do something with them.
        String minValue = null, maxValue = null;
        for (final TerminalClause terminalClause : clauseList)
        {
            // EMPTY is not supported for work ratio searcher
            final Operand operand = terminalClause.getOperand();
            if (operandResolver.isEmptyOperand(operand))
            {
                return null;
            }

            final List<QueryLiteral> list = operandResolver.getValues(user, operand, terminalClause);
            if ((list == null) || (list.size() != 1))
            {
                //Either something very bad happened, or we are getting more results than we can handle. In either
                //case, just ignore.
                return null;
            }
            final QueryLiteral ratioLiteral = list.get(0);

            // we could've got a single empty literal without using the empty operand; this is also not supported
            if (ratioLiteral.isEmpty())
            {
                return null;
            }

            final Operator operator = terminalClause.getOperator();

            if (operator == Operator.LESS_THAN_EQUALS)
            {
                String ratio = getRatioFromLiteral(ratioLiteral);
                if (ratio != null)
                {
                    if (maxValue == null)
                    {
                        maxValue = ratio;
                    }
                    else
                    {
                        //we already have a max ratio before, so don't do anything. We could actually be nice
                        //and use the smaller maximum, but this would change the clause.
                        return null;
                    }
                }
                else
                {
                    return null;
                }
            }
            else if (operator == Operator.GREATER_THAN_EQUALS)
            {
                String ratio = getRatioFromLiteral(ratioLiteral);
                if (ratio != null)
                {
                    if (minValue == null)
                    {
                        minValue = ratio;
                    }
                    else
                    {
                        //we already have a min ratio before, so don't do anything. We could actually be nice
                        //and use the bigger minimum, but this would change the clause.
                        return null;
                    }
                }
                else
                {
                    return null;
                }
            }
            else
            {
                return null;
            }
        }

        // we don't want to store the parameters which were not present in the clause
        return MapBuilder.<String, String> newBuilder()
                .addIfValueNotNull(config.getMaxField(), maxValue)
                .addIfValueNotNull(config.getMinField(), minValue)
                .toMap();
    }

    /**
     * Checks the clause structure for validity, and returns the needed clauses from the tree if valid.
     *
     * @param clause the clause to check
     * @return a list of clauses for the field specified in the config, or null if the clause was invalid or there
     * were no clauses in the tree
     */
    List<TerminalClause> validateClauseStructure(final Clause clause)
    {
        final SimpleNavigatorCollectorVisitor visitor = new SimpleNavigatorCollectorVisitor(constants.getJqlClauseNames());
        clause.accept(visitor);
        final List<TerminalClause> clauses = visitor.getClauses();

        // If the clause was invalid (in any part), or there were no clauses, return null
        if (!visitor.isValid() || clauses.isEmpty())
        {
            return null;
        }

        return clauses;
    }

    /**
     * @param ratioLiteral the query literal
     * @return the string representation of the ratio; null if literal had no value.
     */
    private static String getRatioFromLiteral(final QueryLiteral ratioLiteral)
    {
        if (ratioLiteral.getLongValue() != null)
        {
            return ratioLiteral.getLongValue().toString();
        }
        else if (StringUtils.isNotBlank(ratioLiteral.getStringValue()))
        {
            return ratioLiteral.getStringValue();
        }
        else
        {
            return null;
        }
    }
}
