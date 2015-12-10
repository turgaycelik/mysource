package com.atlassian.jira.datetime;

/**
 * Represents a "Local Date" or "Calendar Date" - that is a date (no time) without any associated timezone.
 *
 * LocalDate is only defined to handle years in the Common Era - it cannot handle dates that are BC.
 *
 * @since v4.4
 */
public class LocalDate implements Comparable<LocalDate>
{
    private final int year, month, day;

    public LocalDate(int year, int month, int day)
    {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public int getYear()
    {
        return year;
    }

    public int getMonth()
    {
        return month;
    }

    public int getDay()
    {
        return day;
    }

    @Override
    public String toString()
    {
        return year + "-" + month + "-" + day;
    }

    @SuppressWarnings ( { "RedundantIfStatement" })
    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        LocalDate localDate = (LocalDate) o;

        if (day != localDate.day) { return false; }
        if (month != localDate.month) { return false; }
        if (year != localDate.year) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = year;
        result = 31 * result + month;
        result = 31 * result + day;
        return result;
    }

    @Override
    public int compareTo(LocalDate localDate)
    {
        if (year < localDate.year)
        {
            return -1;
        }
        else if (year > localDate.year)
        {
            return 1;
        }

        if (month < localDate.month)
        {
            return -1;
        }
        else if (month > localDate.month)
        {
            return 1;
        }

        if (day < localDate.day)
        {
             return -1;
        }
        else if (day > localDate.day)
        {
           return 1;
        }
        return 0;
    }
}
