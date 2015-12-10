package com.atlassian.jira.charts.jfreechart.util;

import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.DrawingSupplier;

import java.awt.*;

/**
 * Collection of colors and defaults used in the charts
 *
 * @since v4.0
 */
public class ChartDefaults
{
    public static final AxisLocation rangeAxisLocation = AxisLocation.BOTTOM_OR_LEFT;
    public static final Font defaultFont = new Font("Helvetica", Font.PLAIN, 10);
    public static final Font titleFont = new Font("Helvetica", Font.BOLD, 20);
    public static final Stroke defaultStroke = new BasicStroke(3.0f);

    /* Colours */
    public static final Color axisLabelColor = Color.GRAY;
    public static final Color axisLineColor = Color.BLACK;
    public static final Color legendTextColor = Color.BLACK;
    public static final Color titleTextColor = Color.GRAY;
    public static final Color gridLineColor = Color.LIGHT_GRAY;
    public static final Color outlinePaintColor = Color.WHITE;
    public static final Color transparent = new Color(0, 0, 0, 0);

    private static final Color TOPLINE_LIGHT = new Color(230, 242, 250);
    private static final Color LIGHT_BLUE = new Color(71, 142, 199);
    private static final Color KHAKI = new Color(118, 152, 16);
    private static final Color YELLOW_XY = new Color(222, 228, 57);
    private static final Color DARK_BLUE = new Color(12, 67, 131);
    private static final Color LIGHT_YELLOW = new Color(237, 239, 0);
    private static final Color BRIGHT_BLUE = new Color(12, 135, 201);

    public static final Color GREEN = new Color(95, 190, 65);
    public static final Color ORANGE_XY = new Color(215, 86, 31);
    public static final Color LIGHT_ORANGE = new Color(245, 131, 43);
    public static final Color DIRTY_RED = new Color(173, 42, 21);
    public static final Color RED = new Color(222, 23, 33);
    public static final Color BRIGHT_GREEN = new Color(69, 182, 63);
    public static final Color GREEN2 = new Color(174, 191, 71);
    public static final Color GREEN_DIFF = new Color(135, 206, 112);
    public static final Color RED_DIFF = new Color(193, 95, 79);

    public static final Color[] darkColors =
            {
                    LIGHT_BLUE,
                    ORANGE_XY,
                    KHAKI,
                    YELLOW_XY,
                    GREEN,
                    DARK_BLUE,
                    LIGHT_ORANGE,
                    LIGHT_YELLOW,
                    BRIGHT_BLUE,
                    DIRTY_RED,
                    TOPLINE_LIGHT,
                    GREEN2,
            };

    public static final DrawingSupplier darkColorDrawingSupplier = new DefaultDrawingSupplier(
            darkColors,
            DefaultDrawingSupplier.DEFAULT_OUTLINE_PAINT_SEQUENCE,
            DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE,
            DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE,
            DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE
    );
}
