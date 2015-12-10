package com.atlassian.jira.charts.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import com.atlassian.core.util.collection.EasyList;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.jfree.data.category.CategoryDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.Week;
import org.jfree.data.time.Year;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestDataUtils
{

    public static final int PRETTY_MUCH_TWO_YEARS = 731; // Equal to two years if one is a leap year. Otherwise, just a bit over.

    @Test
    public void testGetTotalNumber()
    {
        final Map<String, Integer> data = new HashMap<String, Integer>();

        data.put("foo1", 1);
        data.put("foo2", 2);
        data.put("foo3", 3);
        data.put("foo4", 4);

        assertEquals(new Integer(10), DataUtils.getTotalNumber(data));
    }

    @Test
    public void testNormaliseMapKeys()
    {
        final Map<RegularTimePeriod, Number> fooMap = new HashMap<RegularTimePeriod, Number>();
        final Map<RegularTimePeriod, Number> barMap = new HashMap<RegularTimePeriod, Number>();
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, 1);
        final RegularTimePeriod key1 = RegularTimePeriod.createInstance(Day.class, calendar.getTime(), RegularTimePeriod.DEFAULT_TIME_ZONE);
        calendar.set(Calendar.DAY_OF_YEAR, 2);
        final RegularTimePeriod key2 = RegularTimePeriod.createInstance(Day.class, calendar.getTime(), RegularTimePeriod.DEFAULT_TIME_ZONE);
        calendar.set(Calendar.DAY_OF_YEAR, 3);
        final RegularTimePeriod key3 = RegularTimePeriod.createInstance(Day.class, calendar.getTime(), RegularTimePeriod.DEFAULT_TIME_ZONE);
        calendar.set(Calendar.DAY_OF_YEAR, 4);
        final RegularTimePeriod key4 = RegularTimePeriod.createInstance(Day.class, calendar.getTime(), RegularTimePeriod.DEFAULT_TIME_ZONE);
        calendar.set(Calendar.DAY_OF_YEAR, 5);
        final RegularTimePeriod key5 = RegularTimePeriod.createInstance(Day.class, calendar.getTime(), RegularTimePeriod.DEFAULT_TIME_ZONE);
        calendar.set(Calendar.DAY_OF_YEAR, 6);
        final RegularTimePeriod key6 = RegularTimePeriod.createInstance(Day.class, calendar.getTime(), RegularTimePeriod.DEFAULT_TIME_ZONE);

        fooMap.put(key1, 1);
        fooMap.put(key2, 2);
        fooMap.put(key3, 3);

        barMap.put(key4, 4);
        barMap.put(key5, 5);
        barMap.put(key6, 6);

        DataUtils.normaliseMapKeys(fooMap, barMap);

        assertTrue(fooMap.containsKey(key4));
        assertEquals(0, fooMap.get(key4));
        assertTrue(fooMap.containsKey(key5));
        assertEquals(0, fooMap.get(key5));
        assertTrue(fooMap.containsKey(key6));
        assertEquals(fooMap.get(key6), 0);

        assertTrue(barMap.containsKey(key1));
        assertEquals(0, barMap.get(key1));
        assertTrue(barMap.containsKey(key2));
        assertEquals(0, barMap.get(key2));
        assertTrue(barMap.containsKey(key3));
        assertEquals(0, barMap.get(key3));
    }

    @Test
    public void testMakeCumulative()
    {
        final Map data = new LinkedHashMap();

        data.put("foo1", 1);
        data.put("foo2", 2);
        data.put("foo3", 3);
        data.put("foo4", 4);

        DataUtils.makeCumulative(data);

        assertEquals(new Integer(1), data.get("foo1"));
        assertEquals(new Integer(3), data.get("foo2"));
        assertEquals(new Integer(6), data.get("foo3"));
        assertEquals(new Integer(10), data.get("foo4"));
    }

    @Test
    public void testNormaliseDateRangeCount()
    {
        final Map dateMap = new LinkedHashMap();
        final List ranges = new ArrayList();

        DataUtils.normaliseDateRangeCount(dateMap, 2, Day.class, TimeZone.getDefault());
        assertEquals(3, dateMap.size());
        ranges.addAll(dateMap.keySet());
        assertEquals(1, ((Comparable) ranges.get(1)).compareTo(ranges.get(0)));

        dateMap.clear();
        ranges.clear();
        DataUtils.normaliseDateRangeCount(dateMap, 14, Week.class, TimeZone.getDefault());
        assertEquals(3, dateMap.size());
        ranges.addAll(dateMap.keySet());
        assertEquals(1, ((Comparable) ranges.get(1)).compareTo(ranges.get(0)));

        dateMap.clear();
        ranges.clear();
        DataUtils.normaliseDateRangeCount(dateMap, PRETTY_MUCH_TWO_YEARS, Year.class, TimeZone.getDefault());
        assertEquals(3, dateMap.size());
        ranges.addAll(dateMap.keySet());
        assertEquals(1, ((Comparable) ranges.get(1)).compareTo(ranges.get(0)));

        assertEquals(0, dateMap.get(ranges.get(0)));
    }

    @Test
    public void testNormaliseDateRange()
    {
        final Map dateMap = new LinkedHashMap();
        final List ranges = new ArrayList();

        DataUtils.normaliseDateRange(dateMap, 2, Day.class, TimeZone.getDefault());
        assertEquals(3, dateMap.size());
        ranges.addAll(dateMap.keySet());
        assertEquals(1, ((Comparable) ranges.get(1)).compareTo(ranges.get(0)));

        dateMap.clear();
        ranges.clear();
        DataUtils.normaliseDateRange(dateMap, 14, Week.class, TimeZone.getDefault());
        assertEquals(3, dateMap.size());
        ranges.addAll(dateMap.keySet());
        assertEquals(1, ((Comparable) ranges.get(1)).compareTo(ranges.get(0)));

        dateMap.clear();
        ranges.clear();
        DataUtils.normaliseDateRange(dateMap, PRETTY_MUCH_TWO_YEARS, Year.class, TimeZone.getDefault());
        assertEquals(3, dateMap.size());
        ranges.addAll(dateMap.keySet());
        assertEquals(1, ((Comparable) ranges.get(1)).compareTo(ranges.get(0)));

        assertEquals(EasyList.build((long) 0), dateMap.get(ranges.get(0)));
    }

    @Test
    public void testGetCategoryDataSet()
    {
        final String[] seriesNames = new String[] { "created", "resolved" };
        final List<Map<RegularTimePeriod, Number>> data = new ArrayList<Map<RegularTimePeriod, Number>>();

        final Map<RegularTimePeriod, Number> createdData = new HashMap<RegularTimePeriod, Number>();
        final Map<RegularTimePeriod, Number> resolvedData = new HashMap<RegularTimePeriod, Number>();

        final Date currentDate = new Date();
        final RegularTimePeriod timePeriod = RegularTimePeriod.createInstance(Day.class, currentDate, RegularTimePeriod.DEFAULT_TIME_ZONE);
        createdData.put(timePeriod, 1);

        data.add(createdData);

        try
        {
            DataUtils.getCategoryDataset(data, seriesNames);
            fail("Should have thrown IAE");
        }
        catch (final IllegalArgumentException e)
        {
            //yay
        }

        resolvedData.put(timePeriod, 2);
        data.add(resolvedData);

        final CategoryDataset dataset = DataUtils.getCategoryDataset(data, seriesNames);
        assertEquals(1, dataset.getValue(0, 0));
        assertEquals(2, dataset.getValue(1, 0));
        assertEquals(timePeriod, dataset.getColumnKey(0));
    }

    @Test
    public void normaliseDateRangeShouldBeTimeZoneAware() throws Exception
    {
        final TimeZone timeZone = TimeZone.getTimeZone("Pacific/Fakaofo");
        final RegularTimePeriod today = RegularTimePeriod.createInstance(Day.class, new Date(), timeZone);
        final RegularTimePeriod yesterday = today.previous();
        final RegularTimePeriod beforeYesterday = yesterday.previous();

        final Map<RegularTimePeriod, List<Long>> dateMap = Maps.newHashMap(ImmutableMap.<RegularTimePeriod, List<Long>>builder()
                .put(beforeYesterday, Lists.<Long>newArrayList(ImmutableList.<Long>builder().add(1L).build()))
                .build());

        final int days = 2;

        DataUtils.normaliseDateRange(dateMap, days, Day.class, timeZone);
        assertThat(dateMap.keySet(), equalTo((Set) Sets.newHashSet(beforeYesterday, yesterday, today)));
        assertThat(dateMap.get(beforeYesterday), hasItem(1L));
    }

    @Test
    public void normaliseDateRangeCountShouldBeTimeZoneAware() throws Exception
    {
        final TimeZone timeZone = TimeZone.getTimeZone("Pacific/Fakaofo");
        final RegularTimePeriod today = RegularTimePeriod.createInstance(Day.class, new Date(), timeZone);
        final RegularTimePeriod yesterday = today.previous();
        final RegularTimePeriod beforeYesterday = yesterday.previous();

        final Map<RegularTimePeriod, Number> dateMap = Maps.newHashMap(ImmutableMap.<RegularTimePeriod, Number>builder()
                .put(beforeYesterday, 123)
                .build());

        final int days = 2;

        DataUtils.normaliseDateRangeCount(dateMap, days, Day.class, timeZone);
        assertThat(dateMap.keySet(), equalTo((Set) Sets.newHashSet(beforeYesterday, yesterday, today)));
        assertThat(dateMap, hasEntry(beforeYesterday, (Number) 123));
    }
}
