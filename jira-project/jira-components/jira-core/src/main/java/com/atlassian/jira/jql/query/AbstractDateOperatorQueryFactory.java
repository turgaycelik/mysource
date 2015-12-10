package com.atlassian.jira.jql.query;

import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.JqlDateSupport;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Provideds common date operator specific query factory methods.
 *
 * @since v4.0
 */
public abstract class AbstractDateOperatorQueryFactory
{
    private final JqlDateSupport jqlDateSupport;

    protected AbstractDateOperatorQueryFactory(JqlDateSupport jqlDateSupport)
    {
        this.jqlDateSupport = notNull("jqlDateSupport", jqlDateSupport);
    }

    /**
     * @param rawValues the query literals representing the dates
     * @return a list of dates represented by the literals; never null, but may contain null if an empty literal was specified.
     */
    List<Date> getDateValues(List<QueryLiteral> rawValues)
    {
        //For the time being, assume jqlDateSupport returns 1 to 1
        List<Date> dates = new ArrayList<Date>();
        for (QueryLiteral rawValue : rawValues)
        {
            if (rawValue.getLongValue() != null)
            {
                final Date date = jqlDateSupport.convertToDate(rawValue.getLongValue());
                if (date != null)
                {
                    dates.add(date);
                }
            }
            else if (rawValue.getStringValue() != null)
            {
                final Date date = jqlDateSupport.convertToDate(rawValue.getStringValue());
                if (date != null)
                {
                    dates.add(date);
                }
            }
            else
            {
                dates.add(null);
            }
        }
        return dates;
    }
}
