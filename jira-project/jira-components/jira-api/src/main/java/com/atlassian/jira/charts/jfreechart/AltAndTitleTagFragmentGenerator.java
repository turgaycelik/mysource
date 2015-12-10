package com.atlassian.jira.charts.jfreechart;

import org.jfree.chart.imagemap.ToolTipTagFragmentGenerator;

import static org.jfree.chart.imagemap.ImageMapUtilities.htmlEscape;

/**
 * Generates tooltips using the HTML {@code alt} and {@code title} attributes for image map area tags.
 *
 * @since v6.0
 */
public class AltAndTitleTagFragmentGenerator implements ToolTipTagFragmentGenerator
{
    /**
     * @param toolTipText the tooltip.
     * @return The formatted HTML area tag attributes.
     */
    public String generateToolTipFragment(String toolTipText)
    {
        return String.format(" title=\"%s\" alt=\"%s\"", htmlEscape(toolTipText), htmlEscape(toolTipText));
    }
}