package com.atlassian.jira.charts;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.charts.jfreechart.ChartHelper;
import com.atlassian.jira.issue.search.SearchRequest;

/**
 * Main entry point for charting in JIRA.  This factory can be used to generate the different types of charts that are
 * used in JIRA.  It returns a generic Chart object that contains all the information required to render the generated
 * chart.  This ensures that we can switch to a different charting framework at some stage in the future.
 *
 * @since v4.0
 */
public interface ChartFactory
{
    static final int FRAGMENT_IMAGE_HEIGHT = 250;
    static final int FRAGMENT_IMAGE_WIDTH = 380;
    static final int PORTLET_IMAGE_HEIGHT = 300;
    static final int PORTLET_IMAGE_WIDTH = 450;
    static final int REPORT_IMAGE_HEIGHT = 500;
    static final int REPORT_IMAGE_WIDTH = 800;

    /**
     * Defines what version labels to show in a chart that supports this feature.
     */
    enum VersionLabel
    {
        none, major, all
    }

    /**
     * Defines the period for which data in the charts will be grouped.
     */
    enum PeriodName
    {
        hourly, daily, weekly, monthly, quarterly, yearly
    }

    /**
     * Generates a created vs resolved line graph, showing the difference between created and resolved issues.
     *
     * @param context Basic information needed to generate all charts
     * @param days Number of days to go back in time for the chart
     * @param periodName Defines if the data will be grouped in days, weeks, months...
     * @param versionLabels Defines what version labels to show in the chart
     * @param cumulative Display cumulative totals for the resolved and unresolved issues
     * @param showUnresolvedTrend Displays a subgraph showing the trend at which issues are being resolved
     * @return A chart object containing all the information required to display the chart.
     */
    Chart generateCreatedVsResolvedChart(ChartContext context, int days, PeriodName periodName, VersionLabel versionLabels,
            boolean cumulative, boolean showUnresolvedTrend);

    /**
     * Generates a date range chart time, meaning a chart that shows time values for particular date ranges. For example
     * the total resolution time for issues in any given time period.
     *
     * @param context Basic information needed to generate all charts
     * @param days Number of days to go back in time for the chart
     * @param periodName Defines if the data will be grouped in days, weeks, months...
     * @param yAxisTimePeriod Time period in millis to display on the yAxis.  Only supports Hours and Days
     * @param labelSuffixKey The i18n suffix key to display for mouse tool tips in the chart
     * @param dateFieldId The date field to use as the basis for the times displayed per date range
     * @return A chart object containing all the information required to display the chart.
     */
    Chart generateDateRangeTimeChart(ChartContext context, int days, PeriodName periodName, long yAxisTimePeriod, String labelSuffixKey, String dateFieldId);

    /**
     * Generates a pie chart for a particular field.  For example, this may generate a pie chart showing how many issues
     * are in each issue type available. See {@link com.atlassian.jira.issue.statistics.FilterStatisticsValuesGenerator}
     * for possible values for the statisticType.  Custom fields are also supported by simply entering the
     * 'customfield_<id>' for the statisticType.
     *
     * @param context Basic information needed to generate all charts
     * @param statisticType The field for which to generate the pie slices.
     * @return A chart object containing all the information required to display the chart.
     */
    Chart generatePieChart(ChartContext context, String statisticType);

    /**
     * Generates what is essentially a bar chart showing the average time that issues have been open for over time.
     *
     * @param context Basic information needed to generate all charts
     * @param days Number of days to go back in time for the chart
     * @param periodName Defines if the data will be grouped in days, weeks, months...
     * @return A chart object containing all the information required to display the chart.
     */
    Chart generateAverageAgeChart(ChartContext context, int days, PeriodName periodName);

    /**
     * Generates a stacked bar chart showing the number of created/resolved issues for a certain time period.
     *
     * @param context Basic information needed to generate all charts
     * @param days Number of days to go back in time for the chart
     * @param periodName Defines if the data will be grouped in days, weeks, months...
     * @return A chart object containing all the information required to display the chart.
     */
    Chart generateRecentlyCreated(ChartContext context, int days, PeriodName periodName);

    /**
     * Generates a chart showing the number of issues since a certain date field.  For example if the 'Created' field
     * is selected, this chart will show the number of issues created for each date in the past (depending on
     * period and days previously selected).  Please note that the dateField must be a Field marked by
     * {@link com.atlassian.jira.issue.fields.DateField}.
     *
     * @param context Basic information needed to generate all charts
     * @param days Number of days to go back in time for the chart
     * @param periodName Defines if the data will be grouped in days, weeks, months...
     * @param cumulative Display cumulative totals for the number of issues
     * @param dateFieldId The date field to use as the basis for the times displayed per date range
     * @return A chart object containing all the information required to display the chart.
     */
    Chart generateTimeSinceChart(ChartContext context, int days, PeriodName periodName, boolean cumulative, String dateFieldId);

    /**
     * Provides context for rendering a chart that is needed by all chart types.  This includes the user who's
     * requesting the chart, a searchrequest needed to lookup data for the chart, and the chart's width and height.
     */
    static class ChartContext
    {
        private final User remoteUser;
        private final SearchRequest searchRequest;
        private final int width;
        private final int height;
        private final boolean inline;

        public ChartContext(final User remoteUser, final SearchRequest searchRequest, final int width, final int height, final boolean inline)
        {
            super();
            this.remoteUser = remoteUser;
            this.searchRequest = searchRequest;
            this.width = width;
            this.height = height;
            this.inline = inline;
        }

        /**
         * @deprecated use {@link ChartContext#ChartContext(User, SearchRequest, int, int, boolean)}
         * @see ChartHelper
         */
        @Deprecated
        public ChartContext(final User remoteUser, final SearchRequest searchRequest, final int width, final int height)
        {
            this(remoteUser, searchRequest, width, height, false);
        }

        public int getHeight()
        {
            return height;
        }

        public User getRemoteUser()
        {
            return remoteUser;
        }

        public SearchRequest getSearchRequest()
        {
            return searchRequest;
        }

        public int getWidth()
        {
            return width;
        }

        public boolean isInline()
        {
            return inline;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            final ChartContext that = (ChartContext) o;

            if (height != that.height)
            {
                return false;
            }
            if (width != that.width)
            {
                return false;
            }
            if (remoteUser != null ? !remoteUser.equals(that.remoteUser) : that.remoteUser != null)
            {
                return false;
            }
            if (searchRequest != null ? !searchRequest.equals(that.searchRequest) : that.searchRequest != null)
            {
                return false;
            }
            if (inline != that.inline)
            {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode()
        {
            int result = remoteUser != null ? remoteUser.hashCode() : 0;
            result = 31 * result + (searchRequest != null ? searchRequest.hashCode() : 0);
            result = 31 * result + width;
            result = 31 * result + height;
            result += inline ? 1 : 0;
            return result;
        }
    }
}
