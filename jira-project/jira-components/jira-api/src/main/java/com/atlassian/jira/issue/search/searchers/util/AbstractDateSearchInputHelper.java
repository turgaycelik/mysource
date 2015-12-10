package com.atlassian.jira.issue.search.searchers.util;

import com.atlassian.annotations.Internal;
import com.atlassian.core.util.DateUtils;
import com.atlassian.core.util.InvalidDurationException;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.searchers.transformer.SimpleNavigatorCollectorVisitor;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
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
 *
 * @since v4.4
 */
@Internal
public abstract class AbstractDateSearchInputHelper implements DateSearcherInputHelper
{
    protected static final ConvertClauseResult CONVERT_CLAUSE_BAD_RESULT = new ConvertClauseResult(null, false);

    protected final DateSearcherConfig config;
    protected final JqlOperandResolver operandResolver;

    public AbstractDateSearchInputHelper(DateSearcherConfig config, JqlOperandResolver operandResolver)
    {
        this.config = notNull("config", config);
        this.operandResolver = notNull("notNull", operandResolver);
    }

    protected static class ParseDateResult
    {
        String parsedDate;
        boolean fits;

        ParseDateResult(final boolean fits, final String parsedDate)
        {
            this.fits = fits;
            this.parsedDate = parsedDate;
        }
    }

    @Override
    public ConvertClauseResult convertClause(final Clause clause, final User user, final boolean allowTimeComponent)
    {
        if (clause == null)
        {
            return CONVERT_CLAUSE_BAD_RESULT;
        }

        // Lets get all the clauses for this field.
        final List<TerminalClause> clauseList = validateClauseStructure(clause);
        if (clauseList == null)
        {
            return CONVERT_CLAUSE_BAD_RESULT;
        }

        boolean fits = true;
        //we have some time related clauses, so lets do something with them.
        String afterValue = null, beforeValue = null, previousValue = null, nextValue = null;
        for (final TerminalClause terminalClause : clauseList)
        {
            // EMPTY is not supported for dates
            final Operand operand = terminalClause.getOperand();
            if (operandResolver.isEmptyOperand(operand))
            {
                return CONVERT_CLAUSE_BAD_RESULT;
            }

            // We need to check if a function is being used (e.g. the "Now()" function)
            if (operandResolver.isFunctionOperand(operand))
            {
                return CONVERT_CLAUSE_BAD_RESULT;
            }

            final List<QueryLiteral> list = operandResolver.getValues(user, operand, terminalClause);
            if ((list == null) || (list.size() != 1))
            {
                //Either something very bad happened, or we are getting more results than we can handle. In either
                //case, just ignore.
                return CONVERT_CLAUSE_BAD_RESULT;
            }
            final QueryLiteral dateLiteral = list.get(0);

            // we could've got a single empty literal without using the empty operand; this is also not supported
            if (dateLiteral.isEmpty())
            {
                return CONVERT_CLAUSE_BAD_RESULT;
            }

            final Operator operator = terminalClause.getOperator();

            if (operator == Operator.LESS_THAN_EQUALS)
            {
                String date = getValidNavigatorPeriod(dateLiteral);
                if (date != null)
                {
                    if (nextValue == null)
                    {
                        nextValue = date;
                    }
                    else
                    {
                        //we already have a relative next, so don't do anything. We could actually be nice
                        //and use the younger date, but this would change the clause.
                        return CONVERT_CLAUSE_BAD_RESULT;
                    }
                }
                else
                {
                    final ParseDateResult result = getValidNavigatorDate(dateLiteral, allowTimeComponent);
                    date = result.parsedDate;
                    if (date != null)
                    {
                        if (beforeValue == null)
                        {
                            if (!result.fits) { fits = false; }
                            beforeValue = date;
                        }
                        else
                        {
                            return CONVERT_CLAUSE_BAD_RESULT;
                        }
                    }
                    else
                    {
                        return CONVERT_CLAUSE_BAD_RESULT;
                    }

                }
            }
            else if (operator == Operator.GREATER_THAN_EQUALS)
            {
                String date = getValidNavigatorPeriod(dateLiteral);
                if (date != null)
                {
                    if (previousValue == null)
                    {
                        previousValue = date;
                    }
                    else
                    {
                        return CONVERT_CLAUSE_BAD_RESULT;
                    }
                }
                else
                {
                    final ParseDateResult result = getValidNavigatorDate(dateLiteral, allowTimeComponent);
                    date = result.parsedDate;
                    if (date != null)
                    {
                        if (afterValue == null)
                        {
                            if (!result.fits) { fits = false; }
                            afterValue = date;
                        }
                        else
                        {
                            return CONVERT_CLAUSE_BAD_RESULT;
                        }

                    }
                    else
                    {
                        return CONVERT_CLAUSE_BAD_RESULT;
                    }
                }
            }
            else
            {
                return CONVERT_CLAUSE_BAD_RESULT;
            }
        }

        // we don't want to store the parameters which were not present in the clause
        final Map<String, String> fields = MapBuilder.<String, String>newBuilder() //
                .addIfValueNotNull(config.getBeforeField(), beforeValue) //
                .addIfValueNotNull(config.getAfterField(), afterValue) //
                .addIfValueNotNull(config.getPreviousField(), previousValue) //
                .addIfValueNotNull(config.getNextField(), nextValue)//
                .toMap();

        return new ConvertClauseResult(fields, fits);
    }

    abstract ParseDateResult getValidNavigatorDate(final QueryLiteral dateLiteral, final boolean allowTimeComponent);

    private String getValidNavigatorPeriod(final QueryLiteral dateLiteral)
    {
        if (StringUtils.isNotBlank(dateLiteral.getStringValue()))
        {
            try
            {
                DateUtils.getDurationWithNegative(dateLiteral.getStringValue());
                return dateLiteral.getStringValue();
            }
            catch (final InvalidDurationException e)
            {
                return null;
            }
            catch (NumberFormatException ne)
            {
                return null;
            }
        }
        else
        {
            return null;
        }
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
        final SimpleNavigatorCollectorVisitor visitor = new SimpleNavigatorCollectorVisitor(config.getClauseNames());
        clause.accept(visitor);
        final List<TerminalClause> clauses = visitor.getClauses();

        // If the clause was invalid (in any part), or there were such dates in clause, return null
        if (!visitor.isValid() || clauses.isEmpty())
        {
            return null;
        }

        return clauses;
    }
}
