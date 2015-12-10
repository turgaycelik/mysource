package com.atlassian.jira.charts.jfreechart;

import com.atlassian.jira.charts.jfreechart.util.ChartDefaults;
import com.atlassian.jira.charts.jfreechart.util.ChartUtil;
import com.atlassian.jira.web.bean.I18nBean;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;

import java.text.NumberFormat;

/**
 * Creates Histogram charts.  These are essentially bar charts, however histograms in JFreechart deal better with dates
 * along the X-axis.
 *
 * @since v4.0
 */
public class StackedBarChartGenerator implements ChartGenerator
{
    private final CategoryDataset dataset;
    private final String yLabel;
    private I18nBean i18nBean;

    public StackedBarChartGenerator(CategoryDataset dataset, String yLabel, final I18nBean I18nBean)
    {
        this.dataset = dataset;
        this.yLabel = yLabel;
        i18nBean = I18nBean;
    }

    public ChartHelper generateChart()
    {
        boolean legend = false;
        boolean tooltips = false;
        boolean urls = false;

        JFreeChart chart = ChartFactory.createStackedBarChart(null, null, yLabel, dataset, PlotOrientation.VERTICAL, legend, tooltips, urls);
        setStackedBarChartDefaults(chart, i18nBean);

        CategoryPlot plot = chart.getCategoryPlot();

        NumberAxis axis = (NumberAxis) plot.getRangeAxis();
        TickUnitSource units = NumberAxis.createIntegerTickUnits();
        axis.setStandardTickUnits(units);

        CategoryAxis catAxis = plot.getDomainAxis();
        catAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);

        plot.getRenderer().setSeriesOutlinePaint(1, ChartDefaults.GREEN_DIFF);
        plot.getRenderer().setSeriesPaint(1, ChartDefaults.GREEN_DIFF);
        plot.getRenderer().setSeriesOutlinePaint(0, ChartDefaults.RED_DIFF);
        plot.getRenderer().setSeriesPaint(0, ChartDefaults.RED_DIFF);

        return new ChartHelper(chart);
    }

    /**
     * Utility method to set the default style of the Bar Charts
     *
     * @param chart {@link JFreeChart} to style
     * @param i18nBean an i18nBean with the remote user
     */
    private static void setStackedBarChartDefaults(JFreeChart chart, final I18nBean i18nBean)
    {
        ChartUtil.setDefaults(chart, i18nBean);

        CategoryPlot plot = (CategoryPlot) chart.getPlot();

        plot.setAxisOffset(new RectangleInsets(1.0, 1.0, 1.0, 1.0));

        // renderer
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setBaseItemLabelFont(ChartDefaults.defaultFont);
        renderer.setBaseItemLabelsVisible(false);
        renderer.setItemMargin(0.2);

        renderer.setBasePositiveItemLabelPosition(
                new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER));
        renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setBaseItemLabelPaint(ChartDefaults.axisLabelColor);

        StandardCategoryToolTipGenerator generator =
                new StandardCategoryToolTipGenerator("{1}, {2}", NumberFormat.getInstance());
        renderer.setBaseToolTipGenerator(generator);
        renderer.setDrawBarOutline(false);
        for (int j = 0; j < ChartDefaults.darkColors.length; j++)
        {
            renderer.setSeriesPaint(j, ChartDefaults.darkColors[j]);
        }
    }
}