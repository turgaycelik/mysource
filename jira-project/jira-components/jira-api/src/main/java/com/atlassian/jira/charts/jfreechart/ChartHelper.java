package com.atlassian.jira.charts.jfreechart;

import static com.atlassian.core.util.RandomGenerator.randomString;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.charts.util.ChartUtils;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.io.SessionNotFoundException;
import com.atlassian.jira.io.TempFileFactory;
import com.atlassian.util.profiling.UtilTimerStack;
import com.google.common.annotations.VisibleForTesting;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.imagemap.ImageMapUtilities;
import org.jfree.chart.imagemap.StandardURLTagFragmentGenerator;
import org.jfree.chart.servlet.ServletUtilities;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * A nice utility class to manage the generation of a charts.
 *
 * The original implementation use jfreechart's one time image mechanism
 * which can lead to JRA-21854.
 *
 * New implementation is using data uri mechanism (inlining base64 encoded images directly)
 * so the {@link #generate(int, int)} and {@link #getLocation()} methods are here just for
 * backward compatibility.
 *
 * When you are going to create new kind of chart you should use {@link #generateInline(int, int)}
 * instead of {@link #generate(int, int)}
 *
 * @see ChartUtils
 * @since v4.0
 */
public class ChartHelper
{
    private final static Logger log = Logger.getLogger(ChartHelper.class);

    private final JFreeChart chart;
    private final TempFileFactory tempFileFactory;
    private final ChartUtils chartUtils;

    private ChartRenderingInfo renderingInfo;
    private String location;
    private String imageMapHtml;
    private String imageMapName;

    private BufferedImage image;

    public ChartHelper(JFreeChart chart)
    {
        this(chart, ComponentAccessor.getComponent(TempFileFactory.class), ComponentAccessor
                .getComponent(ChartUtils.class));
    }

    @VisibleForTesting @Internal
    public ChartHelper(JFreeChart chart, TempFileFactory tempFileFactory, ChartUtils chartUtils)
    {
        this.chart = chart;
        this.tempFileFactory = tempFileFactory;
        this.chartUtils = chartUtils;
    }

    public JFreeChart getChart()
    {
        return chart;
    }

    /**
     * Generates chart "into the memory" which can be later retrieved via {@link #getImage()} method.
     */
    public void generateInline(int width, int height) throws IOException
    {
        if (log.isDebugEnabled())
        {
            log.debug("ChartHelper.generate() Create a ChartRenderingInfo.");
        }
        renderingInfo = new ChartRenderingInfo();
        // Profile the call to JFreeChart
        final String logLine = "ChartHelper calling JFreeChart: JFreeChart.createBufferedImage()";
        UtilTimerStack.push(logLine);
        try
        {
            log.debug("ChartHelper.generateInline(): Use JFreeChart to create PNG file.");
            image = createChartImageInline(chart, width, height, renderingInfo);
            log.debug("ChartHelper.generateInline(): PNG file created .");
        }
        finally
        {
            UtilTimerStack.pop(logLine);
        }
    }

    /**
     * @deprecated JRA-21854 images should be rendered using data uri.
     * Please use {@link #generateInline(int, int)}
     *
     * @see #getImage()
     */
    @Deprecated
    public void generate(int width, int height) throws IOException
    {
        if (log.isDebugEnabled())
        {
            log.debug("ChartHelper.generate() Create a ChartRenderingInfo.");
        }
        renderingInfo = new ChartRenderingInfo();
        // Profile the call to JFreeChart
        final String logLine = "ChartHelper calling JFreeChart: ServletUtilities.saveChartAsPNG()";
        UtilTimerStack.push(logLine);
        try
        {
            log.debug("ChartHelper.generate(): Use JFreeChart to create PNG file.");
            location = createChartImage(chart, width, height, renderingInfo);
            // JRA-34911: this change is causing undesired side effects. disabling for now.
            // markAsTemporaryFile(location);
            log.debug("ChartHelper.generate(): PNG file created in '" + location + "'.");
        }
        finally
        {
            UtilTimerStack.pop(logLine);
        }
    }

    private String createChartImage(final JFreeChart chart, final int width, final int height, final ChartRenderingInfo renderingInfo)
            throws IOException
    {
        File tempFile = getChartTempFile();
        ChartUtilities.saveChartAsPNG(tempFile, chart, width, height, renderingInfo);
        return tempFile.getName();
    }

    private BufferedImage createChartImageInline(final JFreeChart chart, final int width, final int height, final ChartRenderingInfo renderingInfo)
            throws IOException
    {
        return chart.createBufferedImage(width, height, renderingInfo);
    }

    public ChartRenderingInfo getRenderingInfo()
    {
        return renderingInfo;
    }

    /**
     * @deprecated use inline charts
     *
     * @see #getImage()
     * @see ChartUtils
     */
    @Deprecated
    public String getLocation()
    {
        return location;
    }

    /**
     * @deprecated Use #getImageMapHtml
     */
    public String getImageMap()
    {
        return getImageMapHtml();
    }

    /**
     * @since v6.0
     */
    public String getImageMapHtml()
    {
        if (imageMapHtml == null)
        {
            imageMapName = generateRandomImageMapName();
            imageMapHtml = ImageMapUtilities.getImageMap(
                    imageMapName, renderingInfo,
                    new AltAndTitleTagFragmentGenerator(),
                    new StandardURLTagFragmentGenerator()
            );
        }

        return imageMapHtml;
    }

    private static String generateRandomImageMapName()
    {
        return "chart-" + randomString(10);
    }

    public String getImageMapName()
    {
        return imageMapName;
    }

    /**
     * Marks the file at {@code location} for deletion (JRA-21854). Under normal circumstances the chart will be deleted
     * directly after it is streamed once. However, if there is an exception or if some reason the image is not
     * requested then it does not get cleaned up by JFreeChart. To fix JRA-21854 we add a SessionTempFile for it to at
     * least guarantee that the file will not outlive the user's session.
     * <p/>
     * This is a best-effort method, so any failures are silently ignored.
     */
    private void markAsTemporaryFile(final String location)
    {
        // JRA-21854:
        if (tempFileFactory != null)
        {
            try
            {
                tempFileFactory.makeSessionTempFile(location);
            }
            catch (SessionNotFoundException e)
            {
                // ignore
            }
        }
    }

    /**
     * Retrieve image that has been generated with
     * {@link #generateInline(int, int)}
     *
     * @return generated image or <code>null</code> if the
     *         {@link #generateInline(int, int)} method has not been used.
     */
    public BufferedImage getImage()
    {
        return image;
    }

    private File getChartTempFile() throws IOException
    {
        return File.createTempFile(ServletUtilities.getTempOneTimeFilePrefix(), ".png", chartUtils.getChartDirectory());
    }
}
