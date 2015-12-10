package com.atlassian.jira.issue.statistics;

import java.util.Comparator;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.issue.statistics.util.NullStatsMapper;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestTwoDimensionalStatsMap
{
    TwoDimensionalStatsMap statsMap = new TwoDimensionalStatsMap(NULL_STATS_MAPPER, NULL_STATS_MAPPER);
    private String xAxis = "xkey";
    private String xAxis2 = "xkey2";
    private String yAxis = "ykey";
    private String yAxis2 = "ykey2";
    private static final NullStatsMapper NULL_STATS_MAPPER = new NullStatsMapper();

    /**
     * When we accumulate stats we want to use the comparator defined by the mapper. That means there may be two
     * distinct objects (i.e. different hash values) but they "compareTo" the same thing. If we DON'T do this then
     * we would end up with two entries in our stats mapper. What we want is to have a single entry whose value
     * is the sum of the two.
     */
    @Test
    public void testComparatorAccumulation()
    {
        // A simple wrapper so that things with the same value hash to different places.
        class StatThing
        {
            public String value;

            public StatThing(final String value)
            {
                this.value = value;
            }
        }

        // Our stats mapper for the y-axis has a custom comparator. This comparator reaches inside the simple wrapper
        // above to compare based on value.
        TwoDimensionalStatsMap statsMap = new TwoDimensionalStatsMap(NULL_STATS_MAPPER, new NullStatsMapper()
        {
            @Override
            public Comparator<StatThing> getComparator()
            {
                return new Comparator<StatThing>()
                {
                    @Override
                    public int compare(StatThing s1, StatThing s2)
                    {
                        return s1.value.compareTo(s2.value);
                    }
                };
            }
        });

        // We've added two things. But the statsMap should see it as one thing, since the comparator from the y-axis mapper
        // looks at their value rather than just object id. We should accumulate them into a single y-axis rather than
        // have two y-axis values.
        statsMap.addValue(xAxis, new StatThing("green"), 10);
        statsMap.addValue(xAxis, new StatThing("green"), 21);

        assertEquals(1, statsMap.getXAxis().size());
        assertEquals(xAxis, statsMap.getXAxis().iterator().next());

        // There should only be 1 y-axis value and it should be a "green" thing.
        assertEquals(1, statsMap.getYAxis().size());
        final Object yaxis = statsMap.getYAxis().iterator().next();
        assertEquals("green", ((StatThing) yaxis).value);

        assertEquals(31, statsMap.getCoordinate(xAxis, yaxis));
    }

    @Test
    public void testAddSingle()
    {
        statsMap.addValue(xAxis, yAxis, 10);
        assertEquals(1, statsMap.getXAxis().size());
        assertEquals(xAxis, statsMap.getXAxis().iterator().next());

        assertEquals(1, statsMap.getYAxis().size());
        assertEquals(yAxis, statsMap.getYAxis().iterator().next());

        assertEquals(10, statsMap.getCoordinate(xAxis, yAxis));
    }

    @Test
    public void testAddDoubleYAxis()
    {
        statsMap.addValue(xAxis, yAxis, 10);
        statsMap.addValue(xAxis, yAxis2, 21);

        assertEquals(1, statsMap.getXAxis().size());
        assertEquals(xAxis, statsMap.getXAxis().iterator().next());

        assertEquals(2, statsMap.getYAxis().size());
        assertTrue(statsMap.getYAxis().contains(yAxis));
        assertTrue(statsMap.getYAxis().contains(yAxis2));

        assertEquals(10, statsMap.getCoordinate(xAxis, yAxis));
        assertEquals(21, statsMap.getCoordinate(xAxis, yAxis2));
    }

    @Test
    public void testAddDoubleXAxis()
    {
        statsMap.addValue(xAxis, yAxis, 10);
        statsMap.addValue(xAxis2, yAxis, 21);

        assertEquals(2, statsMap.getXAxis().size());
        assertTrue(statsMap.getXAxis().contains(xAxis));
        assertTrue(statsMap.getXAxis().contains(xAxis2));

        assertEquals(1, statsMap.getYAxis().size());
        assertTrue(statsMap.getYAxis().contains(yAxis));

        assertEquals(10, statsMap.getCoordinate(xAxis, yAxis));
        assertEquals(21, statsMap.getCoordinate(xAxis2, yAxis));
    }

    @Test
    public void testAddDoubleBothAxisWithNotAllValues()
    {
        statsMap.addValue(xAxis, yAxis, 10);
        statsMap.addValue(xAxis2, yAxis2, 21);

        assertEquals(2, statsMap.getXAxis().size());
        assertTrue(statsMap.getXAxis().contains(xAxis));
        assertTrue(statsMap.getXAxis().contains(xAxis2));

        assertEquals(2, statsMap.getYAxis().size());
        assertTrue(statsMap.getYAxis().contains(yAxis));
        assertTrue(statsMap.getYAxis().contains(yAxis2));

        assertEquals(10, statsMap.getCoordinate(xAxis, yAxis));
        assertEquals(21, statsMap.getCoordinate(xAxis2, yAxis2));
        assertEquals(0, statsMap.getCoordinate(xAxis2, yAxis));
        assertEquals(0, statsMap.getCoordinate(xAxis, yAxis2));
    }

    @Test
    public void testAdjustMapSimple()
    {
        statsMap.adjustMapForValues(EasyList.build(xAxis), EasyList.build(yAxis), 10);

        assertEquals(10, statsMap.getXAxisUniqueTotal(xAxis));
        assertEquals(0, statsMap.getXAxisUniqueTotal(xAxis2));

        assertEquals(10, statsMap.getYAxisUniqueTotal(yAxis));
        assertEquals(0, statsMap.getYAxisUniqueTotal(yAxis2));

        assertEquals(10, statsMap.getUniqueTotal());
    }

    @Test
    public void testAdjustMapComplex()
    {
        statsMap.adjustMapForValues(EasyList.build(xAxis), EasyList.build(yAxis), 3);
        statsMap.adjustMapForValues(EasyList.build(xAxis, xAxis2), EasyList.build(yAxis, yAxis2), 7);
        statsMap.adjustMapForValues(EasyList.build(xAxis), EasyList.build(yAxis, yAxis2), 5);
        statsMap.adjustMapForValues(null, null, 11);

        assertEquals(15, statsMap.getXAxisUniqueTotal(xAxis));
        assertEquals(7, statsMap.getXAxisUniqueTotal(xAxis2));
        assertEquals(11, statsMap.getXAxisUniqueTotal(null));

        assertEquals(15, statsMap.getYAxisUniqueTotal(yAxis));
        assertEquals(12, statsMap.getYAxisUniqueTotal(yAxis2));
        assertEquals(11, statsMap.getYAxisUniqueTotal(null));

        assertEquals(26, statsMap.getUniqueTotal());
    }

    @Test
    public void testAdjustMapSimpleNullXValues()
    {
        statsMap.adjustMapForValues(null, EasyList.build(yAxis), 10);

        assertEquals(10, statsMap.getXAxisUniqueTotal(null));
        assertEquals(0, statsMap.getXAxisUniqueTotal(xAxis));
        assertEquals(0, statsMap.getXAxisUniqueTotal(xAxis2));
        assertEquals(10, statsMap.getYAxisUniqueTotal(yAxis));
        assertEquals(0, statsMap.getYAxisUniqueTotal(yAxis2));
        assertEquals(10, statsMap.getUniqueTotal());
    }

    @Test
    public void testAdjustMapComplexNullXValues()
    {
        statsMap.adjustMapForValues(null, EasyList.build(yAxis), 10);
        statsMap.adjustMapForValues(EasyList.build(xAxis), EasyList.build(yAxis2), 5);

        assertEquals(10, statsMap.getXAxisUniqueTotal(null));
        assertEquals(5, statsMap.getXAxisUniqueTotal(xAxis));
        assertEquals(0, statsMap.getXAxisUniqueTotal(xAxis2));
        assertEquals(10, statsMap.getYAxisUniqueTotal(yAxis));
        assertEquals(5, statsMap.getYAxisUniqueTotal(yAxis2));
        assertEquals(15, statsMap.getUniqueTotal());

    }

    @Test
    public void testAdjustMapSimpleNullYValues()
    {
        statsMap.adjustMapForValues(EasyList.build(xAxis), null, 10);

        assertEquals(0, statsMap.getXAxisUniqueTotal(null));
        assertEquals(10, statsMap.getXAxisUniqueTotal(xAxis));
        assertEquals(0, statsMap.getXAxisUniqueTotal(xAxis2));

        assertEquals(0, statsMap.getYAxisUniqueTotal(yAxis));
        assertEquals(0, statsMap.getYAxisUniqueTotal(yAxis2));
        assertEquals(10, statsMap.getYAxisUniqueTotal(null));
        assertEquals(10, statsMap.getUniqueTotal());
    }

    @Test
    public void testAdjustMapComplexNullYValues()
    {
        statsMap.adjustMapForValues(EasyList.build(xAxis), null, 10);
        statsMap.adjustMapForValues(EasyList.build(xAxis, xAxis2), null, 6);

        assertEquals(0, statsMap.getXAxisUniqueTotal(null));
        assertEquals(16, statsMap.getXAxisUniqueTotal(xAxis));
        assertEquals(6, statsMap.getXAxisUniqueTotal(xAxis2));

        assertEquals(0, statsMap.getYAxisUniqueTotal(yAxis));
        assertEquals(0, statsMap.getYAxisUniqueTotal(yAxis2));
        assertEquals(16, statsMap.getYAxisUniqueTotal(null));
        assertEquals(16, statsMap.getUniqueTotal());
    }

    @Test
    public void testAdjustMapIrrelevantXValues()
    {
        statsMap.adjustMapForIrrelevantValues(null, true, EasyList.build(yAxis, yAxis2), false, 1);

        assertEquals(1, statsMap.getXAxisIrrelevantTotal(yAxis));
        assertEquals(1, statsMap.getXAxisIrrelevantTotal(yAxis2));
        assertEquals(1, statsMap.getXAxisUniqueTotal(FilterStatisticsValuesGenerator.IRRELEVANT));
        assertTrue(statsMap.hasIrrelevantXData());
        assertEquals(0, statsMap.getBothIrrelevant());

        assertEquals(0, statsMap.getXAxisUniqueTotal(null));
        assertEquals(0, statsMap.getXAxisUniqueTotal(xAxis));
        assertEquals(0, statsMap.getXAxisUniqueTotal(xAxis2));

        // Lets check that we get the Y values from the YAxis even though they were added against the X irrelevant value
        assertTrue(statsMap.getYAxis().contains(yAxis));
        assertTrue(statsMap.getYAxis().contains(yAxis2));

        assertEquals(0, statsMap.getYAxisUniqueTotal(yAxis));
        assertEquals(0, statsMap.getYAxisUniqueTotal(yAxis2));
        assertEquals(0, statsMap.getYAxisUniqueTotal(null));
        assertEquals(1, statsMap.getUniqueTotal());
    }

    @Test
    public void testAdjustMapIrrelevantYValues()
    {
        statsMap.adjustMapForIrrelevantValues(EasyList.build(xAxis, xAxis2), false, null, true, 1);

        assertEquals(1, statsMap.getYAxisIrrelevantTotal(xAxis));
        assertEquals(1, statsMap.getYAxisIrrelevantTotal(xAxis2));
        assertEquals(1, statsMap.getYAxisUniqueTotal(FilterStatisticsValuesGenerator.IRRELEVANT));
        assertTrue(statsMap.hasIrrelevantYData());
        assertEquals(0, statsMap.getBothIrrelevant());

        assertEquals(0, statsMap.getYAxisUniqueTotal(null));
        assertEquals(0, statsMap.getYAxisUniqueTotal(yAxis));
        assertEquals(0, statsMap.getYAxisUniqueTotal(yAxis2));

        // Lets check that we get the X values from the XAxis even though they were added against the Y irrelevant value
        assertTrue(statsMap.getXAxis().contains(xAxis));
        assertTrue(statsMap.getXAxis().contains(xAxis2));
        
        assertEquals(0, statsMap.getXAxisUniqueTotal(xAxis));
        assertEquals(0, statsMap.getXAxisUniqueTotal(xAxis2));
        assertEquals(0, statsMap.getXAxisUniqueTotal(null));
        assertEquals(1, statsMap.getUniqueTotal());
    }

    @Test
    public void testAdjustMapIrrelevantBoth()
    {
        statsMap.adjustMapForIrrelevantValues(null, true, null, true, 1);

        assertEquals(0, statsMap.getYAxisIrrelevantTotal(xAxis));
        assertEquals(0, statsMap.getYAxisIrrelevantTotal(xAxis2));
        assertEquals(0, statsMap.getYAxisUniqueTotal(FilterStatisticsValuesGenerator.IRRELEVANT));
        assertTrue(statsMap.hasIrrelevantYData());
        assertTrue(statsMap.hasIrrelevantXData());
        assertEquals(1, statsMap.getBothIrrelevant());

        assertEquals(0, statsMap.getYAxisUniqueTotal(null));
        assertEquals(0, statsMap.getYAxisUniqueTotal(yAxis));
        assertEquals(0, statsMap.getYAxisUniqueTotal(yAxis2));

        // Lets make sure there are no x or y axis values as a result of adding one with both irrelevant
        assertTrue(statsMap.getYAxis().isEmpty());
        assertTrue(statsMap.getXAxis().isEmpty());

        assertEquals(0, statsMap.getXAxisUniqueTotal(xAxis));
        assertEquals(0, statsMap.getXAxisUniqueTotal(xAxis2));
        assertEquals(0, statsMap.getXAxisUniqueTotal(null));
        assertEquals(1, statsMap.getUniqueTotal());
    }

    @Test
    public void testAdjustMapSomeInvalidValues()
    {
        NullStatsMapper selectionStatsMapper = new NullStatsMapper()
        {
            public boolean isValidValue(Object value)
            {
                //only xAxis and yAxis are valid values
                return xAxis.equals(value) || yAxis.equals(value);
            }
        };
        TwoDimensionalStatsMap statsMap = new TwoDimensionalStatsMap(selectionStatsMapper, selectionStatsMapper);

        statsMap.adjustMapForValues(EasyList.build(xAxis), EasyList.build(yAxis), 10);
        statsMap.adjustMapForValues(EasyList.build(xAxis, xAxis2), null, 6);

        assertEquals(0, statsMap.getXAxisUniqueTotal(null));
        assertEquals(16, statsMap.getXAxisUniqueTotal(xAxis));
        assertEquals(0, statsMap.getXAxisUniqueTotal(xAxis2));

        assertEquals(10, statsMap.getYAxisUniqueTotal(yAxis));
        assertEquals(0, statsMap.getYAxisUniqueTotal(yAxis2));
        assertEquals(0, statsMap.getYAxisUniqueTotal(null));

        assertEquals(16, statsMap.getUniqueTotal());
    }

    @Test
    public void testAdjustMapUsesValueConversionFromDocumentToString()
    {
        NullStatsMapper convertingStatsMapper = new NullStatsMapper()
        {
            public Object getValueFromLuceneField(String documentValue)
            {
                return documentValue + "-value";
            }
        };
        TwoDimensionalStatsMap statsMap = new TwoDimensionalStatsMap(convertingStatsMapper, convertingStatsMapper);

        statsMap.adjustMapForValues(EasyList.build(xAxis), EasyList.build(yAxis), 10);

        assertEquals(10, statsMap.getXAxisUniqueTotal(xAxis + "-value"));
        assertEquals(0, statsMap.getXAxisUniqueTotal(xAxis2 + "-value"));
        assertEquals(10, statsMap.getXAxisUniqueTotal(xAxis + "-value"));
        assertEquals(0, statsMap.getXAxisUniqueTotal(xAxis2 + "-value"));

        assertEquals(10, statsMap.getYAxisUniqueTotal(yAxis + "-value"));
        assertEquals(0, statsMap.getYAxisUniqueTotal(yAxis2 + "-value"));
        assertEquals(10, statsMap.getYAxisUniqueTotal(yAxis + "-value"));
        assertEquals(0, statsMap.getYAxisUniqueTotal(yAxis2 + "-value"));

        assertEquals(10, statsMap.getUniqueTotal());
    }

    @Test
    public void testAdjustMapUsesValueConversionFromDocumentToObject()
    {
        NullStatsMapper convertingStatsMapper = new NullStatsMapper()
        {
            public Object getValueFromLuceneField(String documentValue)
            {
                return new Long(documentValue);
            }
        };
        TwoDimensionalStatsMap statsMap = new TwoDimensionalStatsMap(convertingStatsMapper, convertingStatsMapper);

        statsMap.adjustMapForValues(EasyList.build("1"), EasyList.build("2"), 10);

        assertEquals(10, statsMap.getXAxisUniqueTotal((long) 1));
        assertEquals(0, statsMap.getXAxisUniqueTotal((long) 0));
        assertEquals(10, statsMap.getXAxisUniqueTotal((long) 1));
        assertEquals(0, statsMap.getXAxisUniqueTotal((long) 0));

        assertEquals(10, statsMap.getYAxisUniqueTotal((long) 2));
        assertEquals(0, statsMap.getYAxisUniqueTotal((long) 0));
        assertEquals(10, statsMap.getYAxisUniqueTotal((long) 2));
        assertEquals(0, statsMap.getYAxisUniqueTotal((long) 0));

        assertEquals(10, statsMap.getUniqueTotal());
    }

    @Test
    public void testAdjustMapUsingMean()
    {
        TwoDimensionalStatsMap statsMap = new TwoDimensionalStatsMap(NULL_STATS_MAPPER, NULL_STATS_MAPPER, new StatisticGatherer.Mean());

        statsMap.adjustMapForValues(EasyList.build(xAxis), EasyList.build(yAxis), 10);
        statsMap.adjustMapForValues(EasyList.build(xAxis), EasyList.build(yAxis), 20);
        statsMap.adjustMapForValues(EasyList.build(xAxis), EasyList.build(yAxis), 50);

        assertEquals(26, statsMap.getXAxisUniqueTotal(xAxis));
        assertEquals(0, statsMap.getXAxisUniqueTotal(xAxis2));
        assertEquals(26, statsMap.getXAxisUniqueTotal(xAxis));
        assertEquals(0, statsMap.getXAxisUniqueTotal(xAxis2));

        assertEquals(26, statsMap.getYAxisUniqueTotal(yAxis));
        assertEquals(0, statsMap.getYAxisUniqueTotal(yAxis2));
        assertEquals(26, statsMap.getYAxisUniqueTotal(yAxis));
        assertEquals(0, statsMap.getYAxisUniqueTotal(yAxis2));

        assertEquals(26, statsMap.getUniqueTotal());
    }

    @Test
    public void testMedianStatsGatherer()
    {
        StatisticGatherer.MedianValue median = new StatisticGatherer.MedianValue();
        assertEquals(0, median.intValue());

        median.addValue(7);
        median.addValue(9);
        assertEquals(8, median.intValue());

        median.addValue(-1);
        assertEquals(7, median.intValue());
    }

    @Test
    public void testAdjustMapUsingMedian()
    {
        TwoDimensionalStatsMap statsMap = new TwoDimensionalStatsMap(NULL_STATS_MAPPER, NULL_STATS_MAPPER, new StatisticGatherer.Median());

        statsMap.adjustMapForValues(EasyList.build(xAxis), EasyList.build(yAxis), 1);
        statsMap.adjustMapForValues(EasyList.build(xAxis), EasyList.build(yAxis), 4);
        statsMap.adjustMapForValues(EasyList.build(xAxis), EasyList.build(yAxis), 7);
        statsMap.adjustMapForValues(EasyList.build(xAxis), EasyList.build(yAxis), 9);
        statsMap.adjustMapForValues(EasyList.build(xAxis), EasyList.build(yAxis), 11);

        assertEquals(7, statsMap.getXAxisUniqueTotal(xAxis));
        assertEquals(0, statsMap.getXAxisUniqueTotal(xAxis2));
        assertEquals(7, statsMap.getXAxisUniqueTotal(xAxis));
        assertEquals(0, statsMap.getXAxisUniqueTotal(xAxis2));

        assertEquals(7, statsMap.getYAxisUniqueTotal(yAxis));
        assertEquals(0, statsMap.getYAxisUniqueTotal(yAxis2));
        assertEquals(7, statsMap.getYAxisUniqueTotal(yAxis));
        assertEquals(0, statsMap.getYAxisUniqueTotal(yAxis2));

        assertEquals(7, statsMap.getUniqueTotal());
    }

    public void testAdjustMapUsingCountUnique()
    {
        TwoDimensionalStatsMap statsMap = new TwoDimensionalStatsMap(NULL_STATS_MAPPER, NULL_STATS_MAPPER, new StatisticGatherer.CountUnique());

        // add 5 unique values
        statsMap.adjustMapForValues(EasyList.build(xAxis), EasyList.build(yAxis), 1);
        statsMap.adjustMapForValues(EasyList.build(xAxis), EasyList.build(yAxis), 4);
        statsMap.adjustMapForValues(EasyList.build(xAxis), EasyList.build(yAxis), 4);
        statsMap.adjustMapForValues(EasyList.build(xAxis), EasyList.build(yAxis), 7);
        statsMap.adjustMapForValues(EasyList.build(xAxis), EasyList.build(yAxis), 9);
        statsMap.adjustMapForValues(EasyList.build(xAxis), EasyList.build(yAxis), 9);
        statsMap.adjustMapForValues(EasyList.build(xAxis), EasyList.build(yAxis), 9);
        statsMap.adjustMapForValues(EasyList.build(xAxis), EasyList.build(yAxis), 11);

        assertEquals(5, statsMap.getXAxisUniqueTotal(xAxis));
        assertEquals(0, statsMap.getXAxisUniqueTotal(xAxis2));
        assertEquals(5, statsMap.getXAxisUniqueTotal(xAxis));
        assertEquals(0, statsMap.getXAxisUniqueTotal(xAxis2));

        assertEquals(5, statsMap.getYAxisUniqueTotal(yAxis));
        assertEquals(0, statsMap.getYAxisUniqueTotal(yAxis2));
        assertEquals(5, statsMap.getYAxisUniqueTotal(yAxis));
        assertEquals(0, statsMap.getYAxisUniqueTotal(yAxis2));

        assertEquals(5, statsMap.getUniqueTotal());
    }



}
