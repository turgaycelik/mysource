package com.atlassian.jira.charts.jfreechart;

import com.atlassian.jira.charts.jfreechart.util.ChartDefaults;
import com.atlassian.jira.charts.jfreechart.util.ChartUtil;
import com.atlassian.jira.util.I18nHelper;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieToolTipGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.PieDataset;
import org.jfree.util.Rotation;

import java.awt.*;

/**
 * Generates a JFreechart pie chart.
 *
 * @since v4.0
 */
public class PieChartGenerator implements ChartGenerator
{
    private final PieDataset pieDataset;
    private I18nHelper i18nHelper;

    public PieChartGenerator(PieDataset pieDataset, final I18nHelper i18nHelper)
    {
        this.pieDataset = pieDataset;
        this.i18nHelper = i18nHelper;
    }

    public ChartHelper generateChart()
    {
        boolean legend = false;
        boolean tooltips = false;
        boolean urls = false;

        final JFreeChart chart = ChartFactory.createPieChart(null, pieDataset, legend, tooltips, urls);
        setPieChartDefaults(chart, pieDataset, i18nHelper);
        return new ChartHelper(chart);
    }

    /**
     * Utility method to set the defaule style of the Pie Chart
     *
     * @param chart {@link JFreeChart} to style
     * @param dataset {@link PieDataset}
     * @param i18nHelper an i18nBean with the remote user
     */
    private static void setPieChartDefaults(JFreeChart chart, PieDataset dataset,final I18nHelper i18nHelper)
    {
        ChartUtil.setDefaults(chart, i18nHelper);

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(Color.WHITE);
        plot.setCircular(true);
        plot.setDirection(Rotation.CLOCKWISE);
        plot.setIgnoreNullValues(true);
        plot.setIgnoreZeroValues(true);
        plot.setStartAngle(290);
        plot.setShadowXOffset(0.0);
        plot.setShadowYOffset(0.0);

        plot.setBaseSectionOutlinePaint(ChartDefaults.outlinePaintColor);
        plot.setBaseSectionOutlineStroke(new BasicStroke(2.0f));

        // tooltip generator
        plot.setToolTipGenerator(new StandardPieToolTipGenerator("{0} {1} ({2})"));

        // set the colors
        for (int j = 0; j < dataset.getItemCount(); j++)
        {
            if (j < ChartDefaults.darkColors.length && dataset.getValue(j).intValue() > 0)
            {
                plot.setSectionPaint(dataset.getKey(j), ChartDefaults.darkColors[j]);
            }
            else
            {
                break;
            }
        }

        // labels
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} = {1}"));
        plot.setLabelGap(0.04);
        plot.setLabelBackgroundPaint(Color.WHITE);
        plot.setLabelOutlinePaint(Color.gray.brighter());
        plot.setLabelShadowPaint(Color.WHITE);
        plot.setLabelFont(ChartDefaults.defaultFont);

        // legend
        plot.setLegendLabelGenerator(new StandardPieSectionLabelGenerator("{0} ({1} - {2})"));
        plot.setLegendItemShape(new Rectangle(0, 0, 10, 10));
    }
}
