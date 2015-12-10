/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.bean;

import com.atlassian.annotations.PublicApi;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

@PublicApi
public final class PercentageGraphRow
{
    private final String color;
    private final long number;
    private final String description;
    private final String statuses;

    public PercentageGraphRow(String color, long number, String description, String statuses)
    {
        this.color = color;
        this.number = number;
        this.description = description;
        this.statuses = statuses;
    }

    public String getColor()
    {
        return color;
    }

    public long getNumber()
    {
        return number;
    }

    public String getDescription()
    {
        return description;
    }

    public String getStatuses()
    {
        return statuses;
    }

    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (!(o instanceof PercentageGraphRow))
            return false;

        final PercentageGraphRow percentageGraphRow = (PercentageGraphRow) o;

        if (number != percentageGraphRow.number)
            return false;
        if (color != null ? !color.equals(percentageGraphRow.color) : percentageGraphRow.color != null)
            return false;
        if (description != null ? !description.equals(percentageGraphRow.description) : percentageGraphRow.description != null)
            return false;
        if (statuses != null ? !statuses.equals(percentageGraphRow.statuses) : percentageGraphRow.statuses != null)
            return false;

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (color != null ? color.hashCode() : 0);
        result = 29 * result + (int) (number ^ (number >>> 32));
        result = 29 * result + (description != null ? description.hashCode() : 0);
        result = 29 * result + (statuses != null ? statuses.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
