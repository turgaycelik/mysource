package com.atlassian.jira.charts.jfreechart;

import com.atlassian.jira.charts.jfreechart.util.ChartDefaults;
import com.atlassian.jira.charts.jfreechart.util.ChartUtil;
import com.atlassian.jira.web.bean.I18nBean;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;

import java.text.DateFormat;
import java.text.NumberFormat;

/**
 * Creates Histogram charts.  These are essentially bar charts, however histograms in JFreechart
 * deal better with dates along the X-axis.
 *
 * @since v4.0
 */
public class HistogramChartGenerator implements ChartGenerator
{
    private final TimeSeriesCollection dataset;
    private final String yLabel;
    private I18nBean i18nBean;

    public HistogramChartGenerator(TimeSeriesCollection dataset, String yLabel, final I18nBean i18nBean)
    {
        this.dataset = dataset;
        this.yLabel = yLabel;
        this.i18nBean = i18nBean;
    }

    public ChartHelper generateChart()
    {
        boolean legend = false;
        boolean tooltips = false;
        boolean urls = false;

        JFreeChart chart = ChartFactory.createHistogram(null, null, yLabel, dataset, PlotOrientation.VERTICAL, legend, tooltips, urls);
        setHistogramChartDefaults(chart, i18nBean);
        chart.setBorderVisible(false);

        return new ChartHelper(chart);
    }

    /**
     * Utility method to set the default style of the Bar Charts
     *
     * @param chart {@link JFreeChart} to style
     * @param i18nBean an i18nBean with the remote user
     */
    private static void setHistogramChartDefaults(JFreeChart chart, final I18nBean i18nBean)
    {
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setDomainAxis(new DateAxis());

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits()); 

        ChartUtil.setDefaults(chart, i18nBean);

        plot.setAxisOffset(new RectangleInsets(1.0, 1.0, 1.0, 1.0));

        // renderer
        XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardXYBarPainter());
        renderer.setShadowVisible(false);
        renderer.setBaseItemLabelFont(ChartDefaults.defaultFont);
        renderer.setBaseItemLabelsVisible(false);
        renderer.setMargin(0.2);

        renderer.setBasePositiveItemLabelPosition(
                new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER));
        renderer.setBaseItemLabelGenerator(new StandardXYItemLabelGenerator());
        renderer.setBaseItemLabelPaint(ChartDefaults.axisLabelColor);

        StandardXYToolTipGenerator generator =
                new StandardXYToolTipGenerator("{1}, {2}", DateFormat.getInstance(), NumberFormat.getInstance());
        renderer.setBaseToolTipGenerator(generator);
        renderer.setDrawBarOutline(false);
        for (int j = 0; j < ChartDefaults.darkColors.length; j++)
        {
            renderer.setSeriesPaint(j, ChartDefaults.darkColors[j]);
        }
    }
}
