package com.atlassian.jira.charts.jfreechart;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Calendar;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.atlassian.jira.charts.util.ChartUtils;
import com.atlassian.jira.io.SessionTempFile;
import com.atlassian.jira.io.TempFileFactory;

@RunWith(MockitoJUnitRunner.class)
public class TestChartHelper
{
    @Mock TempFileFactory tempFileFactory;
    @Mock SessionTempFile sessionTempFile;
    @Mock ChartUtils chartUtils;

    @Before
    public void setUp() throws Exception
    {
        when(tempFileFactory.makeSessionTempFile(anyString())).thenReturn(sessionTempFile);
    }

    @Test
    public void testGetRenderingInfo() throws Exception
    {
        final ChartHelper chartHelper = getChartHelper();

        assertNull(chartHelper.getRenderingInfo());
        chartHelper.generate(512, 384);
        assertNotNull(chartHelper.getRenderingInfo());
    }

    @Test
    public void testGetLocation() throws Exception
    {
        final ChartHelper chartHelper = getChartHelper();

        assertNull(chartHelper.getLocation());
        chartHelper.generate(512, 384);
        assertNotNull(chartHelper.getLocation());
    }

    @Test
    public void testGetImageMap() throws Exception
    {
        final ChartHelper chartHelper = getChartHelper();

        try
        {
            assertNull(chartHelper.getImageMap());
            fail("should have thrown NPE.");
        }
        catch (NullPointerException e)
        {
            //yay
        }
        chartHelper.generate(512, 384);
        assertNotNull(chartHelper.getImageMap());
        assertNotNull(chartHelper.getImageMapName());
    }

    private ChartHelper getChartHelper()
    {
        return new ChartHelper(getChart(), tempFileFactory, chartUtils);
    }

    private JFreeChart getChart()
    {
        final Calendar now = Calendar.getInstance();
        final DefaultCategoryDataset sourceSet = new DefaultCategoryDataset();
        final boolean legend = false;
        final boolean tooltips = false;
        final boolean urls = false;

        final StringBuilder stringBuffer = new StringBuilder();
        for (int i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 3; ++j)
            {

                now.add(Calendar.DAY_OF_YEAR, 1);

                stringBuffer.setLength(0);

                sourceSet.addValue(
                        new Integer(stringBuffer.append(i).append(j).toString()),
                        String.valueOf(i),
                        now.getTime());
            }
        }

        return ChartFactory.createBarChart("fooTitle", "fooYLabel", "fooXLabel", sourceSet,
                PlotOrientation.VERTICAL, legend, tooltips, urls);
    }
}
