package com.atlassian.jira.jql.query;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.statistics.util.LongComparator;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.jira.user.UserHistoryItem;
import com.atlassian.jira.user.UserIssueHistoryManager;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operator.Operator;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Creates clauses for queries on the last viewed date field.
 *
 * @since v4.0
 */
public class LastViewedDateClauseQueryFactory extends AbstractDateOperatorQueryFactory implements ClauseQueryFactory
{
    private static final Logger log = Logger.getLogger(LastViewedDateClauseQueryFactory.class);

    private final JqlOperandResolver operandResolver;
    private final UserIssueHistoryManager historyManager;

    public LastViewedDateClauseQueryFactory(JqlDateSupport jqlDateSupport, JqlOperandResolver operandResolver, UserIssueHistoryManager historyManager)
    {
        super(jqlDateSupport);
        this.operandResolver = operandResolver;
        this.historyManager = historyManager;
    }

    public QueryFactoryResult getQuery(final QueryCreationContext queryCreationContext, final TerminalClause terminalClause)
    {
        final Operand operand = terminalClause.getOperand();
        Operator operator = terminalClause.getOperator();

        if (operandResolver.isValidOperand(operand))
        {
            if (handlesOperator(operator))
            {
                final List<UserHistoryItem> fullHistory = historyManager.getFullIssueHistoryWithoutPermissionChecks(queryCreationContext.getUser());

                if (!fullHistory.isEmpty()){
                    if (operandResolver.isEmptyOperand(operand))
                    {
                        return createQueryForEmptyOperand(operator, fullHistory);
                    }
                    else
                    {
                        // Turn the raw input values from the Operand into values that we can query in the index.
                        final List<QueryLiteral> rawValues = getRawValues(queryCreationContext, terminalClause);

                        final List<Date> dateValues = getDateValues(rawValues);

                        // We want to indicate to the OperatorQueryFactory whether these index values come from a
                        // single inputted value or a list of inputted values.
                        if (operandResolver.isListOperand(operand))
                        {
                            return createQueryForMultipleValues(operator, dateValues, fullHistory);
                        }
                        else
                        {
                            return createQueryForSingleValue(operator, dateValues, fullHistory);
                        }
                    }
                }
                else
                {
                    // No history so return nothing
                    return QueryFactoryResult.createFalseResult();
                }
            }
            log.debug(String.format("The '%s' clause does not support the %s operator.", terminalClause.getName(), operator.getDisplayString()));
            return QueryFactoryResult.createFalseResult();

        }

        // If there is no registered OperandHandler then lets log it and return false
        log.debug(String.format("There is no OperandHandler registered to handle the operand '%s' for operand '%s'.", operator.getDisplayString(), terminalClause.getOperand().getDisplayString()));
        return QueryFactoryResult.createFalseResult();
    }

    private QueryFactoryResult createQueryForSingleValue(final Operator operator, final List<Date> dates, final List<UserHistoryItem> history)
    {
        if (operator == Operator.EQUALS)
        {
            return handleIn(dates, history, false);
        }
        else if (operator == Operator.NOT_EQUALS)
        {
            return handleIn(dates, history, true);
        }

        // if there were no parsable dates in the literals, the resultant list will be empty
        if (dates.isEmpty())
        {
            return QueryFactoryResult.createFalseResult();
        }

        // most operators only expect one value
        final Date date = dates.get(0);

        // if we somehow got null as the value, don't error out but just return a false query
        if (date == null)
        {
            return QueryFactoryResult.createFalseResult();
        }

        // Lets do the relational ones
        if (operator == Operator.LESS_THAN)
        {
            return new QueryFactoryResult(generateQueryForHistory(getHistoryItemsLessThan(date, history)));
        }
        else if(operator == Operator.LESS_THAN_EQUALS)
        {
            return new QueryFactoryResult(generateQueryForHistory(getHistoryItemsLessThanOrEqual(date, history)));
        }
        else if(operator == Operator.GREATER_THAN_EQUALS)
        {
            return new QueryFactoryResult(generateQueryForHistory(getHistoryItemsGreaterThanOrEqual(date, history)));
        }
        else if(operator == Operator.GREATER_THAN)
        {
            return new QueryFactoryResult(generateQueryForHistory(getHistoryItemsGreaterThan(date, history)));
        }


        // No operator found, return nothing.  Shouldn't get here as it wont be a valid operator
        return QueryFactoryResult.createFalseResult();
    }

    private QueryFactoryResult createQueryForMultipleValues(Operator operator, List<Date> dates, List<UserHistoryItem> history)
    {
        if (operator == Operator.IN)
        {
            return handleIn(dates, history, false);
        }
        else if (operator == Operator.NOT_IN)
        {
            return handleIn(dates, history, true);
        }
        else
        {
            log.debug(String.format("Creating an equality query for multiple values for date field '%s' using unsupported operator: '%s', returning "
                    + "a false result (no issues). Supported operators are: '%s' and '%s'", IssueFieldConstants.LAST_VIEWED, operator, Operator.IN, Operator.NOT_IN));

            return QueryFactoryResult.createFalseResult();
        }
    }

    private QueryFactoryResult createQueryForEmptyOperand(Operator operator, List<UserHistoryItem> historyItems)
    {
        if ((operator == Operator.IS) || (operator == Operator.EQUALS))
        {
            return new QueryFactoryResult(generateQueryForHistory(historyItems), true);
        }
        else if ((operator == Operator.IS_NOT) || (operator == Operator.NOT_EQUALS))
        {
            return new QueryFactoryResult(generateQueryForHistory(historyItems));
        }
        else
        {

            log.debug(String.format("Creating an equality query for an empty value for date field '%s' using unsupported operator: '%s', returning "
                    + "a false result (no issues). Supported operators are: '%s','%s', '%s' and '%s'", IssueFieldConstants.LAST_VIEWED, operator,
                    Operator.IS, Operator.EQUALS, Operator.IS_NOT, Operator.NOT_EQUALS));

            return QueryFactoryResult.createFalseResult();
        }
    }

    private QueryFactoryResult handleIn(final List<Date> dates, final List<UserHistoryItem> history, final boolean negate)
    {
        if (dates.size() == 1)
        {
            // these are simple, lets just get them out of the way
            final Date date = dates.get(0);
            return (date == null) ? new QueryFactoryResult(generateQueryForHistory(history), !negate) : new QueryFactoryResult(generateQueryForHistory(getEqualHistoryItems(date, history)), negate);
        }
        else
        {

            // If we fins an "empty" things get a bit curly
            boolean foundEmpty = false;
            // This set needs to be ordered to produce consistant queries
            final Set<UserHistoryItem> matchingHistoryItems = new TreeSet<UserHistoryItem>(new Comparator<UserHistoryItem>(){
                @Override
                public int compare(UserHistoryItem o1, UserHistoryItem o2)
                {
                    if (o1 == null && o2 == null)
                    {
                        return 0;
                    }
                    if (o1 == null)
                    {
                        return -1;
                    }
                    if (o2 == null)
                    {
                        return 1;
                    }
                    return LongComparator.COMPARATOR.compare(o1.getLastViewed(), o2.getLastViewed());
                }
            });

            // lets iterate over the dats get all matching history items
            for (Date date : dates)
            {
                if (date == null)
                {
                    // ah bugger empty!
                    foundEmpty = true;
                }
                else
                {
                    matchingHistoryItems.addAll(getEqualHistoryItems(date, history));
                }
            }
            // if empty isn't in list, jsut generate a query with all matching items
            if (!foundEmpty)
            {
                return new QueryFactoryResult(generateQueryForHistory(matchingHistoryItems), negate);
            }
            else
            {
                // Return issues that don't match the history items that aren't in the list
                final List<UserHistoryItem> userHistoryItems = Lists.newArrayList(history);
                userHistoryItems.removeAll(matchingHistoryItems);
                final BooleanQuery combined = new BooleanQuery();

                for (UserHistoryItem item : userHistoryItems)
                {
                    combined.add(new TermQuery(new Term(SystemSearchConstants.forIssueId().getIndexField(), item.getEntityId())), BooleanClause.Occur.MUST_NOT);
                }

                return new QueryFactoryResult(combined, negate);
            }

        }
    }

    private Query generateQueryForHistory(Collection<UserHistoryItem> history)
    {
        final BooleanQuery combined = new BooleanQuery();
        for (final UserHistoryItem item : history)
        {
            combined.add(new TermQuery(new Term(SystemSearchConstants.forIssueId().getIndexField(), item.getEntityId())), BooleanClause.Occur.SHOULD);
        }

        return combined;
    }

    private boolean handlesOperator(Operator operator)
    {
        return OperatorClasses.RELATIONAL_ONLY_OPERATORS.contains(operator) || OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY.contains(operator);
    }

    List<QueryLiteral> getRawValues(QueryCreationContext queryCreationContext, final TerminalClause clause)
    {
        return operandResolver.getValues(queryCreationContext, clause.getOperand(), clause);
    }

    private List<UserHistoryItem> getEqualHistoryItems(final Date date, final List<UserHistoryItem> history)
    {
        final long time = date.getTime();
        final Predicate<UserHistoryItem> predicate = new Predicate<UserHistoryItem>()
        {
            @Override
            public boolean apply(@Nullable UserHistoryItem input)
            {
                return input.getLastViewed() == time;
            }
        };
        return generateNewList(history, predicate);
    }

    private List<UserHistoryItem> getHistoryItemsLessThan(final Date date, final List<UserHistoryItem> history)
    {
        final long time = date.getTime();
        final Predicate<UserHistoryItem> predicate = new Predicate<UserHistoryItem>()
        {
            @Override
            public boolean apply(@Nullable UserHistoryItem input)
            {
                return input.getLastViewed() < time;
            }
        };
        return generateNewList(history, predicate);
    }

    private List<UserHistoryItem> getHistoryItemsLessThanOrEqual(final Date date, final List<UserHistoryItem> history)
    {
        final long time = date.getTime();
        final Predicate<UserHistoryItem> predicate = new Predicate<UserHistoryItem>()
        {
            @Override
            public boolean apply(@Nullable UserHistoryItem input)
            {
                return input.getLastViewed() <= time;
            }
        };
        return generateNewList(history, predicate);
    }

    private List<UserHistoryItem> getHistoryItemsGreaterThanOrEqual(final Date date, final List<UserHistoryItem> history)
    {
        final long time = date.getTime();
        final Predicate<UserHistoryItem> predicate = new Predicate<UserHistoryItem>()
        {
            @Override
            public boolean apply(@Nullable UserHistoryItem input)
            {
                return input.getLastViewed() >= time;
            }
        };
        return generateNewList(history, predicate);
    }

    private List<UserHistoryItem> getHistoryItemsGreaterThan(final Date date, final List<UserHistoryItem> history)
    {
        final long time = date.getTime();
        final Predicate<UserHistoryItem> predicate = new Predicate<UserHistoryItem>()
        {
            @Override
            public boolean apply(@Nullable UserHistoryItem input)
            {
                return input.getLastViewed() > time;
            }
        };
        return generateNewList(history, predicate);
    }

    private List<UserHistoryItem> generateNewList(final List<UserHistoryItem> history, Predicate<UserHistoryItem> predicate)
    {
        return Lists.newArrayList(Iterables.filter(history, predicate));
    }
}
