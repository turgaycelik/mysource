package com.atlassian.jira.charts.jfreechart;

/**
 * Thin wrapper over JFreeChart
 *
 * @since v4.0
 */
public interface ChartGenerator
{

    /**
     * Generates the chart and will produce a ChartHelper that can be used to access the chart location
     * and other information.
     *
     * @return a ChartHelper to access all the chart's details.
     */
    ChartHelper generateChart();
}
