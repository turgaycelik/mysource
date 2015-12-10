/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.bean;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.CollectionUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@PublicApi
public final class PercentageGraphModel
{
    private final List<PercentageGraphRow> rows;
    private PercentageGraphRow lastRow;
    private long total;

    public PercentageGraphModel()
    {
        rows = new ArrayList<PercentageGraphRow>();
        total = 0;
    }

    public void addRow(String color, long number, String description, String statuses)
    {
        rows.add(lastRow = new PercentageGraphRow(color, number, description, statuses));
        total += number;
    }

    public void addRow(String color, long number, String description)
    {
        addRow(color, number, description, null);
    }

    public List<PercentageGraphRow> getRows()
    {
        return Collections.unmodifiableList(rows);
    }

    /**
     * Use to get the percentage of a particular row.
     * <br/>
     * If the percentage calculated is not a whole number it is rounded down. An exception is the last row
     * for which the percentage is calculated as a remainder to 100 (percent).
     *
     * @param row row to get the width percentage for
     * @return percentage for the given row
     */
    public int getPercentage(PercentageGraphRow row)
    {
        final RowPercentageCalculator calculator = new RowPercentageCalculator(getTotal());

        // if last element
        if (lastRow.equals(row))
        {
            return calculator.getLastPercentage(CollectionUtil.transformIterator(rows.iterator(), new Function<PercentageGraphRow, Long>()
            {
                public Long get(final PercentageGraphRow input)
                {
                    return input.getNumber();
                }
            }));
        }
        else
        {
            return calculator.getPercentage(row.getNumber());
        }
    }

    public long getTotal()
    {
        return total;
    }

    public boolean isTotalZero()
    {
        return total == 0;
    }

    static class RowPercentageCalculator
    {
        final float total;

        public RowPercentageCalculator(long total)
        {
            this.total = total;
        }

        /**
         * Calculates the percentage as a fraction that the given value represents in the total.
         * The percentage is rounded down to the nearest whole number.
         *
         * @param value value
         * @return percentage the given value takes in the total
         */
        public int getPercentage(long value)
        {
            return (int) ((float) value / total * 100F);
        }

        public int getLastPercentage(Iterator<Long> numberIterator)
        {
            int cumulativePercentage = 0;
            while(numberIterator.hasNext())
            {
                final Long val = numberIterator.next();
                if (numberIterator.hasNext()) // skip last element
                {
                    cumulativePercentage += getPercentage(val);
                }
            }
            return 100 - cumulativePercentage;
        }
    }
}
