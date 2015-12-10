package com.atlassian.jira.issue.statistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A simple way of calculating statistics
 *
 * @since v3.11
 */
public interface StatisticGatherer
{
    /**
     * Returns current value. If null, the return value will be the 0.
     *
     * @param current  The current value.  If null, the return value will be the equivalent of 0
     * @param newValue The value to add to the current
     * @return The new value - guaranteed not to be null
     */
    public Number getValue(Number current, int newValue);

    public static class Sum implements StatisticGatherer
    {
        public Number getValue(Number current, int newValue)
        {
            if (current == null)
            {
                current = new Integer(newValue);
            }
            else
            {
                current = new Integer(newValue + current.intValue());
            }
            return current;
        }
    }

    public static class Mean implements StatisticGatherer
    {
        public Number getValue(Number current, int newValue)
        {
            MeanValue meanValue = (MeanValue) current;
            if (meanValue == null)
            {
                meanValue = new MeanValue();
            }
            meanValue.addValue(newValue);
            return meanValue;
        }
    }

    public static class Median implements StatisticGatherer
    {
        public Number getValue(Number current, int newValue)
        {
            MedianValue medianValue = (MedianValue) current;
            if (medianValue == null)
            {
                medianValue = new MedianValue();
            }
            medianValue.addValue(newValue);
            return medianValue;
        }
    }

    public static class CountUnique implements StatisticGatherer
    {
        public Number getValue(Number current, int newValue)
        {
            CountUniqueValue countUniqueValue = (CountUniqueValue) current;
            if (countUniqueValue == null)
            {
                countUniqueValue = new CountUniqueValue();
            }
            countUniqueValue.addValue(newValue);
            return countUniqueValue;
        }
    }

    /**
     * Using some trickiness - we extend Number so that we can call {@link #intValue()} without having
     * to change all the client code
     */
    static class MeanValue extends Number
    {
        private int total;
        private int count;

        public void addValue(int value)
        {
            total += value;
            count++;
        }

        public int intValue()
        {
            return total / count;
        }

        public long longValue()
        {
            return intValue();
        }

        public float floatValue()
        {
            return intValue();
        }

        public double doubleValue()
        {
            return intValue();
        }

        @Override
        public String toString()
        {
            return String.valueOf(intValue());
        }
    }

    public static class MedianValue extends Number
    {
        private final List values = new ArrayList();

        public void addValue(int value)
        {
            values.add(new Integer(value));
        }

        public int intValue()
        {
            Collections.sort(values, new Comparator()
            {
                public int compare(Object o, Object o1)
                {
                    if (o == null)
                    {
                        return -1; //null is smaller than anything
                    }
                    if (o1 == null)
                    {
                        return 1;
                    }
                    return ((Integer) o).compareTo((Integer) o1);

                }
            });
            if (values.isEmpty())
            {
                return 0;
            }
            else if (values.size() % 2 == 0)
            {
                int i = ((Integer) values.get((values.size()) / 2)).intValue();
                int j = ((Integer) values.get(values.size() / 2 - 1)).intValue();
                return (i + j) / 2;
            }
            else
            {
                return ((Integer) values.get(values.size() / 2)).intValue();
            }
        }

        public long longValue()
        {
            return intValue();
        }

        public float floatValue()
        {
            return intValue();
        }

        public double doubleValue()
        {
            return intValue();
        }

        @Override
        public String toString()
        {
            return String.valueOf(intValue());
        }
    }

    static class CountUniqueValue extends Number
    {
        private final Set values = new HashSet();

        public void addValue(int value)
        {
            values.add(new Integer(value));
        }

        public int intValue()
        {
            return values.size();
        }

        public long longValue()
        {
            return intValue();
        }

        public float floatValue()
        {
            return intValue();
        }

        public double doubleValue()
        {
            return intValue();
        }

        @Override
        public String toString()
        {
            return String.valueOf(intValue());
        }
    }
    

}
