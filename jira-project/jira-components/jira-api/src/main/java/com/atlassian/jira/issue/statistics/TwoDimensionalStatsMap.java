package com.atlassian.jira.issue.statistics;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.issue.comparator.util.DelegatingComparator;
import com.atlassian.jira.issue.statistics.util.CachingStatisticsMapper;
import com.atlassian.jira.issue.statistics.util.ComparatorSelector;
import org.apache.commons.collections.comparators.ReverseComparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class TwoDimensionalStatsMap
{
    public final static String TOTAL_ORDER = "total";
    public final static String NATURAL_ORDER = "natural";
    public final static String DESC = "desc";
    public final static String ASC = "asc";

    private final StatisticsMapper xAxisMapper;
    private final StatisticsMapper yAxisMapper;
    private final StatisticGatherer statisticGatherer;

    private final Map xAxis;
    private final Map xAxisTotals;
    private final Map yAxisTotals;
    private final Map<Object, Number> xAxisIrrelevantTotals;
    private int bothIrrelevant = 0;
    private final Map<Object, Number> yAxisIrrelevantTotals;
    private Number entireTotal;

    /**
     * We always want "irrelevant" to sort last when we use
     * this in our TreeMaps.
     */
    private static class IrrelevantHandlingComparator implements Comparator
    {
        private final Comparator delegate;

        public IrrelevantHandlingComparator(Comparator delegate)
        {
            this.delegate = delegate;
        }

        @Override
        public int compare(Object o1, Object o2)
        {
            // This handles the case where they are identical. i.e. they are both the FilterStatisticsValuesGenerator.IRRELEVANT object
            if (o1 == o2)
            {
                return 0;
            }
            else if (o1 == FilterStatisticsValuesGenerator.IRRELEVANT)
            {
                return -1;
            }
            else if (o2 == FilterStatisticsValuesGenerator.IRRELEVANT)
            {
                return 1;
            }
            else
            {
                return delegate.compare(o1, o2);
            }
        }
    }

    public TwoDimensionalStatsMap(StatisticsMapper xAxisMapper, StatisticsMapper yAxisMapper, StatisticGatherer statisticGatherer)
    {
        final Comparator xAxisComparator = ComparatorSelector.getComparator(xAxisMapper);
        final Comparator yAxisComparator = ComparatorSelector.getComparator(yAxisMapper);

        this.xAxisMapper = new CachingStatisticsMapper(xAxisMapper);
        this.yAxisMapper = new CachingStatisticsMapper(yAxisMapper);
        this.statisticGatherer = statisticGatherer;
        xAxis = new TreeMap(new IrrelevantHandlingComparator(xAxisComparator));
        xAxisTotals = new TreeMap(new IrrelevantHandlingComparator(xAxisComparator));
        yAxisTotals = new TreeMap(new IrrelevantHandlingComparator(yAxisComparator));
        // We swap the comparator on these maps because we will be storing X axis irrelevant counts against yAxis objects and vice versa
        xAxisIrrelevantTotals = new TreeMap<Object, Number>(yAxisComparator);
        yAxisIrrelevantTotals = new TreeMap<Object, Number>(xAxisComparator);

    }

    public TwoDimensionalStatsMap(StatisticsMapper xAxisMapper, StatisticsMapper yAxisMapper)
    {
        this(xAxisMapper, yAxisMapper, new StatisticGatherer.Sum());
    }

    /**
     * This method will increment the unique totals count for the provided
     * xKey.
     *
     * @param xValue identifies the xValue we are keying on, null is valid.
     * @param i      the amount to increment the total by, usually 1.
     */
    private void addToXTotal(Object xValue, int i)
    {
        Number total = (Number) xAxisTotals.get(xValue);
        xAxisTotals.put(xValue, statisticGatherer.getValue(total, i));
    }

    /**
     * This method will increment the unique totals count for the y row identified by yKey.
     *
     * @param yValue identifies the yValue we are keying on, null is valid.
     * @param i      the amount to increment the total by, usually 1.
     */
    private void addToYTotal(Object yValue, int i)
    {
        Number total = (Number) yAxisTotals.get(yValue);
        yAxisTotals.put(yValue, statisticGatherer.getValue(total, i));
    }

    /**
     * Increments the total count of unique issues added to this StatsMap.
     *
     * @param i the amount to increment the total by, usually 1.
     */
    private void addToEntireTotal(int i)
    {
        entireTotal = statisticGatherer.getValue(entireTotal, i);
    }

    // As this is used for testing - it is package private
    void addValue(Object xValue, Object yValue, int i)
    {
        Map yValues = (Map) xAxis.get(xValue);
        if (yValues == null)
        {
            yValues = new TreeMap(ComparatorSelector.getComparator(yAxisMapper));
            xAxis.put(xValue, yValues);
        }

        Number existingValue = (Number) yValues.get(yValue);
        yValues.put(yValue, statisticGatherer.getValue(existingValue, i));
    }

    // Adds to irrelevant totals of Y for all the passed in X keys
    void addToYIrrelevantTotals(final Collection xValues, final int incrementValue)
    {
        for (Object xValue : xValues)
        {
            Number existingValue = yAxisIrrelevantTotals.get(xValue);
            yAxisIrrelevantTotals.put(xValue, statisticGatherer.getValue(existingValue, incrementValue));
        }
    }

    // Adds to irrelevant totals of X for all the passed in Y keys
    void addToXIrrelevantTotals(final Collection yValues, final int incrementValue)
    {
        for (Object yValue : yValues)
        {
            Number existingValue = xAxisIrrelevantTotals.get(yValue);
            xAxisIrrelevantTotals.put(yValue, statisticGatherer.getValue(existingValue, incrementValue));
        }
    }

    public Collection getXAxis()
    {
        final Set xVals = new TreeSet(ComparatorSelector.getComparator(xAxisMapper));
        if (xAxis.keySet() != null)
        {
            xVals.addAll(xAxis.keySet());
        }
        // Need to add any that might have been left out because they are in the irrelevant ones
        // This looks confusing but do not forget that the KEYS to the yAxisIrrelevatTotals are the X values
        if (yAxisIrrelevantTotals.keySet() != null)
        {
            xVals.addAll(yAxisIrrelevantTotals.keySet());
        }
        return xVals;
    }

    public Collection getYAxis()
    {
        return getYAxis(NATURAL_ORDER, ASC);
    }

    public Collection getYAxis(String orderBy, String direction)
    {
        Comparator comp;

        if (orderBy != null && orderBy.equals(TOTAL_ORDER))
        {
            // Compare by total
            comp = new Comparator()
            {

                public int compare(Object o1, Object o2)
                {
                    Long o1Long = new Long(getYAxisUniqueTotal(o1));
                    Long o2Long = new Long(getYAxisUniqueTotal(o2));
                    return o1Long.compareTo(o2Long);
                }
            };

            // Only reverse total Comaparator, not field Comparator
            if (direction != null && direction.equals(DESC))
            {
                comp = new ReverseComparator(comp);
            }

            // If totals are equal, delagate back to field comparator
            comp = new DelegatingComparator(comp, ComparatorSelector.getComparator(yAxisMapper));
        }
        else
        {
            comp = yAxisMapper.getComparator();
            if (direction != null && direction.equals(DESC))
            {
                comp = new ReverseComparator(comp);
            }
        }

        return getYAxis(comp);
    }

    public Collection getYAxis(Comparator comp)
    {
        Set yAxisKeys = new TreeSet(comp);

        for (final Object o : xAxis.values())
        {
            Map yAxisValues = (Map) o;
            yAxisKeys.addAll(yAxisValues.keySet());
        }
        // Need to add any that might have been left out because they are in the irrelevant ones
        // This looks confusing but do not forget that the KEYS to the xAxisIrrelevatTotals are the Y values
        if (xAxisIrrelevantTotals.keySet() != null)
        {
            yAxisKeys.addAll(xAxisIrrelevantTotals.keySet());
        }
        return yAxisKeys;
    }

    public int getCoordinate(Object xAxis, Object yAxis)
    {
        Map yValues = (Map) this.xAxis.get(xAxis);
        if (yValues == null)
        {
            return 0;
        }

        Number value = (Number) yValues.get(yAxis);
        return value == null ? 0 : value.intValue();
    }

    public StatisticsMapper getyAxisMapper()
    {
        return yAxisMapper;
    }

    public StatisticsMapper getxAxisMapper()
    {
        return xAxisMapper;
    }

    /**
     * Returns the value of unique issues contained in the column identified by xAxis.
     *
     * @param xAxis identifies the column who's total is requested, null is valid.
     * @return number of unique issues for the identified column.
     */
    public int getXAxisUniqueTotal(Object xAxis)
    {
        Number xTotal = (Number) xAxisTotals.get(xAxis);
        return xTotal != null ? xTotal.intValue() : 0;
    }

    /**
     * Returns the value of unique issues contained in the column identified by xAxis.
     *
     * @param yAxis identifies the row who's total is requested, null is valid.
     * @return number of unique issues for the identified row.
     */
    public int getYAxisUniqueTotal(Object yAxis)
    {
        Number yTotal = (Number) yAxisTotals.get(yAxis);
        return yTotal != null ? yTotal.intValue() : 0;
    }

    /**
     * Returns the number of irrelevant issues for the X axis contained in the column identified by yAxis.
     *
     * The X axis values is implied by the method, it is the Irrelevant ones, the Y axis identifies which irrelevant
     * X count we are after.
     *
     * @param yAxis identifies the column who's total is requested, null is valid.
     * @return number of number of irrelevant issues for the identified column/row.
     */
    public int getXAxisIrrelevantTotal(Object yAxis)
    {
        Number xTotal = xAxisIrrelevantTotals.get(yAxis);
        return xTotal != null ? xTotal.intValue() : 0;
    }

    /**
     * Returns the number of irrelevant issues for the Y axis contained in the column identified by xAxis.
     *
     * The Y axis values is implied by the method, it is the Irrelevant ones, the X axis identifies which irrelevant
     * Y count we are after.
     *
     * @param xAxis identifies the column who's total is requested, null is valid.
     * @return number of number of irrelevant issues for the identified column/row.
     */
    public int getYAxisIrrelevantTotal(Object xAxis)
    {
        Number yTotal = yAxisIrrelevantTotals.get(xAxis);
        return yTotal != null ? yTotal.intValue() : 0;
    }

    /**
     * If {@link #hasIrrelevantXData()} is true and {@link #hasIrrelevantYData()} is true then we need to know the
     * total where both axis are irrelevant. This method will return that count.
     *
     * @return the number of issues that have both X and Y irrelevant data.
     */
    public int getBothIrrelevant()
    {
        return bothIrrelevant;
    }

    /**
     * @return true if the results contain irrelevant data for the X axis stat type.
     */
    public boolean hasIrrelevantXData()
    {
        if (bothIrrelevant > 0)
        {
            return true;
        }
        for (Number xAxisIrrelevantTotal : xAxisIrrelevantTotals.values())
        {
            if (xAxisIrrelevantTotal != null && xAxisIrrelevantTotal.intValue() > 0)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * @return true if the results contain irrelevant data for the Y axis stat type.
     */
    public boolean hasIrrelevantYData()
    {
        if (bothIrrelevant > 0)
        {
            return true;
        }
        for (Number yAxisIrrelevantTotal : yAxisIrrelevantTotals.values())
        {
            if (yAxisIrrelevantTotal != null && yAxisIrrelevantTotal.intValue() > 0)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the value of all unique issues identified within this StatsMap.
     *
     * @return number of unique issues identified within this StatsMap.
     */
    public long getUniqueTotal()
    {
        return entireTotal != null ? entireTotal.intValue() : 0;
    }

    /**
     * Used to keep track of the irrelevant counts for the issues returned for this 2D stats map.
     *
     * @param xAxisValues null or the relevant X values for an issue
     * @param xIrrelevant true if the X field is not visible for the issue we are recording stats for, false otherwise
     * @param yAxisValues null or the relevant Y values for an issue
     * @param yIrrelevant true if the Y field is not visible for the issue we are recording stats for, false otherwise
     * @param incrementValue the value to increment the counts by, seems to always be 1, perhaps someone once thought we would do otherwise.
     */
    public void adjustMapForIrrelevantValues(final Collection xAxisValues, final boolean xIrrelevant, final Collection yAxisValues, final boolean yIrrelevant, final int incrementValue)
    {
        if (xIrrelevant && yIrrelevant)
        {
            // This is a irrelevant/irrelevant, all we need to do is keep track of the total amount of these
            bothIrrelevant++;
        }
        else if (xIrrelevant && !yIrrelevant)
        {
            // Need to increment the TOTAL irrelevant count for X, we know there is only this one Irrelevant value
            // This is to count UNIQUE issues and this is why we only count it once
            addToXTotal(FilterStatisticsValuesGenerator.IRRELEVANT, incrementValue);

            // Now we need to run through all the valid Y values and add an irrelevant total to the X keyed against Y
            Collection transformedYValues;
            if (yAxisValues == null)
            {
                transformedYValues = EasyList.build((Object) null);
            }
            else
            {
                transformedYValues = transformAndRemoveInvaid(yAxisValues, yAxisMapper);
            }
            addToXIrrelevantTotals(transformedYValues, incrementValue);
        }
        else if (yIrrelevant && !xIrrelevant)
        {
            // Need to increment the TOTAL irrelevant count for Y, we know there is only this one Irrelevant value
            // This is to count UNIQUE issues and this is why we only count it once
            addToYTotal(FilterStatisticsValuesGenerator.IRRELEVANT, incrementValue);

            // Now we need to run through all the valid X values and add an irrelevant total to the Y keyed against X
            Collection transformedXValues;
            if (xAxisValues == null)
            {
                transformedXValues = EasyList.build((Object) null);
            }
            else
            {
                transformedXValues = transformAndRemoveInvaid(xAxisValues, xAxisMapper);
            }
            addToYIrrelevantTotals(transformedXValues, incrementValue);
        }

        // Always log one hit per unique issue.
        addToEntireTotal(1);
    }

    public void adjustMapForValues(Collection xAxisValues, Collection yAxisValues, int value)
    {
        // if one axis is null, we still need something to iterate over to get the other axis' values
        if (xAxisValues == null)
        {
            xAxisValues = EasyList.build((Object) null);
        }
        if (yAxisValues == null)
        {
            yAxisValues = EasyList.build((Object) null);
        }

        xAxisValues = transformAndRemoveInvaid(xAxisValues, xAxisMapper);
        yAxisValues = transformAndRemoveInvaid(yAxisValues, yAxisMapper);

        for (Object xvalue : xAxisValues)
        {
            addToXTotal(xvalue, value);
            for (Object yvalue : yAxisValues)
            {
                addValue(xvalue, yvalue, value);

            }
        }
        // We have to iterate over the y values alone so we don't mess up the totals
        for (Object yvalue : yAxisValues)
        {
            addToYTotal(yvalue, value);
        }

        // Always log one hit per unique issue.
        addToEntireTotal(value);
    }


    /**
     * Transform values in the collection from Strings to Objects,
     * using {@link com.atlassian.jira.issue.search.LuceneFieldSorter#getValueFromLuceneField(String)}
     *
     * @param values A Collection of Strings, obtained from the Lucene Index
     * @param mapper A statsMapper used to convert to objects
     * @return a collection of transformed objects, never null
     */
    private static Collection transformAndRemoveInvaid(Collection values, StatisticsMapper mapper)
    {
        Collection output = new ArrayList();
        for (final Object value1 : values)
        {
            String key = (String) value1;
            final Object value;
            if (key != null)
            {
                value = mapper.getValueFromLuceneField(key);
            }
            else
            {
                value = null;
            }

            //only valid values should be added to the map
            if (mapper.isValidValue(value))
            {
                output.add(value);
            }
        }
        return output;
    }


}