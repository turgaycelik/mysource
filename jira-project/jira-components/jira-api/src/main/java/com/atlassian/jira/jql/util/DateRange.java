package com.atlassian.jira.jql.util;

import com.atlassian.jira.util.dbc.Assertions;

import java.util.Date;

/**
 * A simple class to represent a date range
 *
 * @since v4.4
 */
public final class DateRange
{
    private final Date lowerDate;
    private final Date upperDate;

    /**
     * The passed in dates can never be null and if the lower is higher than the upper then they are swapped into lower
     * then upper order.
     *
     * @param lowerDate the lower date of the range
     * @param upperDate the upper date of the range
     */
    public DateRange(final Date lowerDate, final Date upperDate)
    {
        Assertions.notNull("lowerDate", lowerDate);
        Assertions.notNull("upperDate", upperDate);

        this.lowerDate = minOf(lowerDate, upperDate);
        this.upperDate = maxOf(lowerDate, upperDate);
    }

    private Date maxOf(Date lowerDate, Date upperDate)
    {
        return (lowerDate.getTime() > upperDate.getTime() ? lowerDate : upperDate);

    }

    private Date minOf(Date lowerDate, Date upperDate)
    {
        return (lowerDate.getTime() <= upperDate.getTime() ? lowerDate : upperDate);
    }

    /**
     * @return the lower of the dates in the date range
     */
    public Date getLowerDate()
    {
        return lowerDate;
    }

    /**
     * @return the lower of the dates in the date range
     */
    public Date getUpperDate()
    {
        return upperDate;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        DateRange dateRange = (DateRange) o;

        if (!lowerDate.equals(dateRange.lowerDate)) { return false; }
        if (!upperDate.equals(dateRange.upperDate)) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = lowerDate.hashCode();
        result = 31 * result + upperDate.hashCode();
        return result;
    }
}
