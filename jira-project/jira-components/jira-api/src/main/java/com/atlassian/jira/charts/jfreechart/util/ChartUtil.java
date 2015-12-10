package com.atlassian.jira.charts.jfreechart.util;

import com.atlassian.jira.charts.ChartFactory;
import com.atlassian.jira.util.I18nHelper;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Month;
import org.jfree.data.time.Quarter;
import org.jfree.data.time.Week;
import org.jfree.data.time.Year;
import org.jfree.ui.HorizontalAlignment;

import java.awt.*;

/**
 * @since v4.0
 */
public class ChartUtil
{

    /**
     * Note: This could be made part of the PeriodName Enum, however I don't want to leak any JFreeChart classes
     * out of this package, which is why I'd like to keep it in this Util.
     *
     * @param periodName The Period enum to convert to JFreechart
     * @return A Jfreechart period class equivalent to the PeriodName enum
     */
    public static Class getTimePeriodClass(ChartFactory.PeriodName periodName)
    {
        switch(periodName)
        {
            case daily:
                return Day.class;
            case hourly:
                return Hour.class;
            case weekly:
                return Week.class;
            case monthly:
                return Month.class;
            case quarterly:
                return Quarter.class;
            case yearly:
                return Year.class;
            default:
                return Day.class;
        }
    }

    /* Helper methods */
    public static void setDefaults(JFreeChart chart, final I18nHelper i18nHelper)
    {
        chart.setBackgroundPaint(Color.WHITE);
        chart.setBorderVisible(false);
        chart.getPlot().setNoDataMessage(i18nHelper.getText("gadget.charts.no.data"));

        setupPlot(chart.getPlot());

        ChartUtil.setupTextTitle(chart.getTitle());
        ChartUtil.setupLegendTitle(chart.getLegend());
    }

    public static void setupPlot(Plot plot)
    {
        if (plot instanceof CategoryPlot)
        {
            setupPlot((CategoryPlot) plot);
        }
        else if (plot instanceof XYPlot)
        {
            setupPlot((XYPlot) plot);
        }
    }

    public static void setupPlot(CategoryPlot plot)
    {
        plot.setBackgroundPaint(ChartDefaults.transparent);
        plot.setOutlinePaint(ChartDefaults.transparent);
        plot.setRangeGridlinePaint(ChartDefaults.gridLineColor);
        plot.setRangeGridlineStroke(new BasicStroke(0.5f));
        plot.setRangeGridlinesVisible(true);
        plot.setRangeAxisLocation(ChartDefaults.rangeAxisLocation);
        plot.setDomainGridlinesVisible(false);

        ChartUtil.setupRangeAxis(plot.getRangeAxis());
        ChartUtil.setupDomainAxis(plot.getDomainAxis());
    }

    public static void setupPlot(XYPlot plot)
    {
        plot.setBackgroundPaint(ChartDefaults.transparent);
        plot.setOutlinePaint(ChartDefaults.transparent);
        plot.setRangeGridlinePaint(ChartDefaults.gridLineColor);
        plot.setRangeGridlineStroke(new BasicStroke(0.5f));
        plot.setRangeGridlinesVisible(true);
        plot.setRangeAxisLocation(ChartDefaults.rangeAxisLocation);
        plot.setDomainGridlinesVisible(true);

        ChartUtil.setupRangeAxis(plot.getRangeAxis());
        ChartUtil.setupDomainAxis(plot.getDomainAxis());
    }

    public static void setupRangeAxis(ValueAxis rangeAxis)
    {
        if (rangeAxis != null)
        {
            rangeAxis.setAxisLinePaint(ChartDefaults.gridLineColor);
            rangeAxis.setTickLabelPaint(ChartDefaults.axisLabelColor);
            rangeAxis.setTickMarksVisible(false);
            rangeAxis.setAxisLineVisible(false);
        }
    }

    public static void setupDomainAxis(CategoryAxis domainAxis)
    {
        if (domainAxis != null)
        {
            domainAxis.setAxisLineStroke(new BasicStroke(0.5f));
            domainAxis.setAxisLinePaint(Color.BLACK);
            domainAxis.setTickLabelPaint(ChartDefaults.axisLabelColor);
        }
    }

    public static void setupDomainAxis(ValueAxis domainAxis)
    {
        if (domainAxis != null)
        {
            domainAxis.setAxisLineStroke(new BasicStroke(0.5f));
            domainAxis.setAxisLinePaint(ChartDefaults.axisLineColor);
            domainAxis.setTickLabelPaint(ChartDefaults.axisLabelColor);
        }
    }

    public static void setupTextTitle(TextTitle title)
    {
        if (title != null)
        {
            title.setFont(ChartDefaults.titleFont);
            title.setTextAlignment(HorizontalAlignment.LEFT);
            title.setPaint(ChartDefaults.titleTextColor);
            title.setBackgroundPaint(ChartDefaults.transparent);
        }
    }

    public static void setupLegendTitle(LegendTitle legend)
    {
        if (legend != null)
        {
            legend.setBorder(0, 0, 0, 0);
            legend.setItemPaint(ChartDefaults.legendTextColor);
            legend.setMargin(2, 2, 2, 2);
        }
    }
}
