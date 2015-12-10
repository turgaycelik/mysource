package com.atlassian.jira.jql.query;

import com.atlassian.jira.datetime.LocalDate;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.JqlLocalDateSupport;

import java.util.ArrayList;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Provides common date operator specific query factory methods.
 *
 * @since v4.4
 */
public abstract class AbstractLocalDateOperatorQueryFactory
{
    private final JqlLocalDateSupport jqlLocalDateSupport;

    protected AbstractLocalDateOperatorQueryFactory(JqlLocalDateSupport jqlLocalDateSupport)
    {
        this.jqlLocalDateSupport = notNull("jqlLocalDateSupport", jqlLocalDateSupport);
    }

    /**
     * @param rawValues the query literals representing the dates
     * @return a list of dates represented by the literals; never null, but may contain null if an empty literal was specified.
     */
    List<LocalDate> getLocalDateValues(List<QueryLiteral> rawValues)
    {
        List<LocalDate> dates = new ArrayList<LocalDate>();
        for (QueryLiteral rawValue : rawValues)
        {
            // Long values are returned by date-time functions such as "now()"
            if (rawValue.getLongValue() != null)
            {
                final LocalDate localDate = jqlLocalDateSupport.convertToLocalDate(rawValue.getLongValue());
                if (localDate != null)
                {
                    dates.add(localDate);
                }
            }
            else
            if (rawValue.getStringValue() != null)
            {
                final LocalDate localDate = jqlLocalDateSupport.convertToLocalDate(rawValue.getStringValue());
                if (localDate != null)
                {
                    dates.add(localDate);
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
