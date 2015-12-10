package com.atlassian.jira.charts.util;

import com.atlassian.jira.charts.ChartFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Utility methods for manipulating data
 *
 * @since v4.0
 */
public class DataUtils
{
    public static final int DAYS_LIMIT_DEFAULT = 1000;

    /**
     * Finds the total of a map's values.
     *
     * @param data The data map to add up
     * @return Integer with the total sum
     */
    public static <T, N extends Number> Integer getTotalNumber(Map<T, N> data)
    {
        int total = 0;
        for (final N num : data.values())
        {
            total += num.intValue();
        }
        return total;
    }

    /**
     * This method takes two maps, and ensures that all the keys in one map are in the other map, and vice versa.
     *
     * @param map1 The first map to combine
     * @param map2 The second map to combine
     */
    public static <T> void normaliseMapKeys(Map<T, Number> map1, Map<T, Number> map2)
    {
        for (T key : map1.keySet())
        {
            if (!map2.containsKey(key))
            {
                map2.put(key, 0);
            }
        }

        for (T key : map2.keySet())
        {
            if (!map1.containsKey(key))
            {
                map1.put(key, 0);
            }
        }
    }

    /**
     * Switch a map from being discrete values to being cumulative.
     * <p/>
     * That is, if a map's values previously are: <br />
     *
     * @param dataMap The datamap that will have its values changed to cumulative.
     */
    public static <T> void makeCumulative(Map<T, Number> dataMap)
    {
        int total = 0;

        for (Map.Entry<T, Number> entry : dataMap.entrySet())
        {
            total += entry.getValue().intValue();
            dataMap.put(entry.getKey(), total);
        }
    }

    public static void normaliseDateRangeCount(Map<RegularTimePeriod, Number> dateMap, int days, Class period, TimeZone timeZone)
    {
        // find earliest date, then move it forwards until we hit now
        Calendar cal = Calendar.getInstance(timeZone);
        cal.add(Calendar.DAY_OF_MONTH, - days);
        Date earliest = cal.getTime();
        RegularTimePeriod cursor = RegularTimePeriod.createInstance(period, earliest, timeZone);
        RegularTimePeriod end = RegularTimePeriod.createInstance(period, new Date(), timeZone);

        //fix for JRA-11686.  Prevents the loop from looping infinitely.
        while (cursor != null && cursor.compareTo(end) <= 0)
        {
            if (!dateMap.containsKey(cursor))
            {
                dateMap.put(cursor, 0);
            }
            cursor = cursor.next();
            cursor.peg(cal);
        }
    }

    public static void normaliseDateRange(Map<RegularTimePeriod, List<Long>> dateMap, int days, Class period, TimeZone timeZone)
    {
        // find earliest date, then move it forwards until we hit now
        Calendar cal = Calendar.getInstance(timeZone);
        cal.add(Calendar.DAY_OF_MONTH, - days);
        Date earliest = cal.getTime();
        RegularTimePeriod cursor = RegularTimePeriod.createInstance(period, earliest, timeZone);
        RegularTimePeriod end = RegularTimePeriod.createInstance(period, new Date(), timeZone);

        //fix for JRA-11686.  Prevents the loop from looping infinitely.
        while (cursor != null && cursor.compareTo(end) <= 0)
        {
            if (!dateMap.containsKey(cursor))
            {
                dateMap.put(cursor, Lists.newArrayList((long) 0));
            }
            cursor = cursor.next();
            cursor.peg(cal);
        }
    }

    /**
     * Reduce a given dataset to only contain a specified number of columns
     *
     * @param dataset The dataset to reduce
     * @param rowKeysToKeep The rows to keep
     * @return A reduced dataset copy.
     */
    public static TimeSeriesCollection reduceDataset(TimeSeriesCollection dataset, List rowKeysToKeep)
    {
        final TimeSeriesCollection newDataSet = new TimeSeriesCollection();
        @SuppressWarnings("unchecked")
        final List<TimeSeries> timeSerieses = new ArrayList<TimeSeries>(dataset.getSeries());
        for (TimeSeries timeSeries : timeSerieses)
        {
            if (rowKeysToKeep.contains(timeSeries.getKey()))
            {
                newDataSet.addSeries(timeSeries);
            }
        }
        return newDataSet;
    }

    /**
     * Trims days to a range of [1...MAX_FOR_PERIOD] where the max is defined in jira-application.properties
     * for each period.  The default value of 1000 days will be used, if no mapping can be found for a particular
     * period or if the property defined can't be parsed.
     *
     * @param days The days input by the user
     * @param period The period selected by the user
     * @return The no of days for which the chart will be generated!
     */
    public static int normalizeDaysValue(int days, ChartFactory.PeriodName period)
    {
        final ApplicationProperties applicationProperties = ComponentAccessor.getComponent(ApplicationProperties.class);
        final String limitString = applicationProperties.getDefaultBackedString(APKeys.JIRA_CHART_DAYS_PREVIOUS_LIMIT_PREFIX + period.toString());
        if(StringUtils.isNotEmpty(limitString))
        {
            int limit = NumberUtils.toInt(limitString, DAYS_LIMIT_DEFAULT);
            return Math.max(Math.min(days, limit), 1);
        }

        return Math.max(Math.min(days, DAYS_LIMIT_DEFAULT), 1);
    }

    public static <T extends Comparable> CategoryDataset getCategoryDataset(List<Map<T, Number>> dataMaps, String[] seriesNames)
    {
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        if (dataMaps.size() != seriesNames.length)
        {
            throw new IllegalArgumentException("Number of datamaps and series names must be equal.");
        }

        for (int i = 0; i < seriesNames.length; i++)
        {
            final String seriesName = seriesNames[i];
            final Map<T, Number> data = dataMaps.get(i);

            for (final Map.Entry<T, Number> entry : data.entrySet())
            {
                dataset.addValue(entry.getValue(), seriesName, entry.getKey());
            }
        }

        return dataset;
    }

    public static TimeSeriesCollection getTimeSeriesCollection(List<Map<RegularTimePeriod, Number>> dataMaps, String[] seriesNames, Class timePeriodClass)
    {
        final TimeSeriesCollection dataset = new TimeSeriesCollection();

        if (dataMaps.size() != seriesNames.length)
        {
            throw new IllegalArgumentException("Number of datamaps and series names must be equal.");
        }

        for (int i = 0; i < seriesNames.length; i++)
        {
            final String seriesName = seriesNames[i];
            final TimeSeries series = new TimeSeries(seriesName, timePeriodClass);
            final Map<RegularTimePeriod, Number> data = dataMaps.get(i);
            for (final Map.Entry<RegularTimePeriod, Number> entry : data.entrySet())
            {
                series.add(entry.getKey(), entry.getValue());
            }
            dataset.addSeries(series);
        }

        return dataset;
    }
}
