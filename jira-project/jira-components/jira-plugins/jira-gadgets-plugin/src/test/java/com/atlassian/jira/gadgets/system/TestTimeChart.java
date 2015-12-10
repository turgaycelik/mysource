package com.atlassian.jira.gadgets.system;

import com.atlassian.jira.easymock.Mock;
import com.atlassian.jira.timezone.TimeZoneManager;
import org.easymock.IAnswer;
import org.easymock.classextension.EasyMock;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;

import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;

import static org.easymock.classextension.EasyMock.anyInt;
import static org.easymock.classextension.EasyMock.eq;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.same;

/**
 * Tests the {@link com.atlassian.jira.gadgets.system.TimeChart} class.
 *
 * @since v4.0
 */
public class TestTimeChart extends ResourceTest
{
    @Mock
    private XYURLGenerator mockXyurlGenerator;

    @Mock
    private TimeZoneManager timeZoneManager;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        expect(timeZoneManager.getLoggedInUserTimeZone()).andStubReturn(TimeZone.getDefault());
    }

    public final void testGenerateDataSetWithNoUrlGenerator()
    {
        _testGenerateDataSet(false);
    }

    public final void testGenerateDataSetWithUrlGenerator()
    {
        _testGenerateDataSet(true);
    }

    private void _testGenerateDataSet(final boolean withUrlGenerator)
    {
        Calendar cal = Calendar.getInstance();
        Day day0 = new Day(cal.getTime());
        cal.add(Calendar.DAY_OF_YEAR, 1);
        Day day1 = new Day(cal.getTime());
        cal.add(Calendar.DAY_OF_YEAR, 1);
        Day day2 = new Day(cal.getTime());

        TimeSeries issuesSeries = new TimeSeries("issues");
        issuesSeries.add(new TimeSeriesDataItem(day0, 4));
        issuesSeries.add(new TimeSeriesDataItem(day1, 5));
        issuesSeries.add(new TimeSeriesDataItem(day2, 6));

        TimeSeries totalSeries = new TimeSeries("total");
        totalSeries.add(new TimeSeriesDataItem(day0, 14));
        totalSeries.add(new TimeSeriesDataItem(day1, 15));
        totalSeries.add(new TimeSeriesDataItem(day2, 16));

        TimeSeries averageSeries = new TimeSeries("average");
        averageSeries.add(new TimeSeriesDataItem(day0, 7));
        averageSeries.add(new TimeSeriesDataItem(day1, 8));
        averageSeries.add(new TimeSeriesDataItem(day2, 9));

        TimeSeriesCollection input = new TimeSeriesCollection();
        input.addSeries(issuesSeries);
        input.addSeries(totalSeries);
        input.addSeries(averageSeries);

        if (withUrlGenerator)
        {
            expect(mockXyurlGenerator.generateURL(same(input), eq(0), anyInt())).andAnswer(new IAnswer<String>()
            {
                public String answer() throws Throwable
                {
                    return "url+" + EasyMock.getCurrentArguments()[2];
                }
            }).times(3);
        }

        replayAll();

        TimeChart.TimeDataRow[] actual = new TimeChart.Generator().generateDataSet(input, (withUrlGenerator ? mockXyurlGenerator : null), timeZoneManager);

        TimeChart.TimeDataRow[] expected = new TimeChart.TimeDataRow[3];
        expected[0] = new TimeChart.TimeDataRow(day0.toString(), 4, (withUrlGenerator ? "url+0" : null), 14, 7);
        expected[1] = new TimeChart.TimeDataRow(day1.toString(), 5, (withUrlGenerator ? "url+1" : null), 15, 8);
        expected[2] = new TimeChart.TimeDataRow(day2.toString(), 6, (withUrlGenerator ? "url+2" : null), 16, 9);

        assertEquals(Arrays.asList(expected), Arrays.asList(actual));

        verifyAll();
    }
}
