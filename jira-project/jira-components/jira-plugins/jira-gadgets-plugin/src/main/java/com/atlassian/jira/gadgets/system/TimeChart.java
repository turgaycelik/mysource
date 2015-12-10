package com.atlassian.jira.gadgets.system;

import com.atlassian.jira.charts.jfreechart.TimePeriodUtils;
import com.atlassian.jira.timezone.TimeZoneManager;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;

///CLOVER:OFF
/**
 * Represents a time period based issue counting chart.
 *
 * @since v4.0
 */
@XmlRootElement
public class TimeChart extends DateRangeChart
{
    @XmlElement
    private TimeDataRow[] data;

    @SuppressWarnings ({ "UnusedDeclaration", "unused" })
    private TimeChart()
    {}

    public TimeChart(final String location, final String title, final String filterUrl, final String imageMap,
            final String imageMapName, TimeDataRow[] data, final int width, final int height, final String base64Image)
    {
        super(location, title, filterUrl, imageMap, imageMapName, width, height, base64Image);
        this.data = data;
    }

    TimeDataRow[] getData()
    {
        return data;
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(final Object o)
    {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public static class Generator
    {
        TimeChart.TimeDataRow[] generateDataSet(final TimeSeriesCollection dataSet, XYURLGenerator urlGenerator, TimeZoneManager timeZoneManager)
        {
            final TimePeriodUtils timePeriodUtils = new TimePeriodUtils(timeZoneManager);

            ArrayList<TimeDataRow> data = new ArrayList<TimeChart.TimeDataRow>();
            final TimeSeries issuesSeries = dataSet.getSeries(0);
            TimeSeries totalSeries = dataSet.getSeries(1);
            TimeSeries averageSeries = dataSet.getSeries(2);
            int count = issuesSeries.getItems().size();
            for (int i = 0; i < count; i++)
            {
                final int issues = issuesSeries.getValue(i).intValue();
                final int total = totalSeries.getValue(i).intValue();
                final int avg = averageSeries.getValue(i).intValue();
                final String issuesLink = urlGenerator != null ? urlGenerator.generateURL(dataSet, 0, i) : null;
                final RegularTimePeriod timePeriod = issuesSeries.getTimePeriod(i);
                data.add(new TimeChart.TimeDataRow(timePeriodUtils.prettyPrint(timePeriod), issues, issuesLink, total, avg));
            }
            return data.toArray(new TimeChart.TimeDataRow[data.size()]);
        }
    }

    @XmlRootElement
    public static class TimeDataRow
    {
        @XmlElement
        private String period;
        @XmlElement
        private int issues;
        @XmlElement
        private String issuesLink;
        @XmlElement
        private int totalTime;
        @XmlElement
        private int avgTime;

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        private TimeDataRow()
        {}

        public TimeDataRow(final String period, final int issues, final String issuesLink, final int totalTime, final int avgTime)
        {
            this.period = period;
            this.issues = issues;
            this.issuesLink = issuesLink;
            this.totalTime = totalTime;
            this.avgTime = avgTime;
        }

        @Override
        public int hashCode()
        {
            return HashCodeBuilder.reflectionHashCode(this);
        }

        @Override
        public boolean equals(final Object o)
        {
            return EqualsBuilder.reflectionEquals(this, o);
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }
}
