package com.atlassian.jira.charts;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.jira.charts.jfreechart.ChartHelper;

/**
 * Result of generating a chart using a class such as {@link com.atlassian.jira.charts.CreatedVsResolvedChart} for
 * example.  It provides the image location, as well as image map and image map name for the chart that was
 * generated.  It also contains a parameter map that may contain obtional parameters needed to render the chart.
 * (For example total issue counts for the data displayed in the chart)
 *
 * @since v4.0
 */
public class Chart
{
    private final Map<String, Object> parameters = new HashMap<String, Object>();
    private String location = null;
    private String imageMap;
    private String imageMapName;

    public Chart(final String location, final String imageMap, final String imageMapName,
            final Map<String, Object> parameters)
    {
        this.location = location;
        this.imageMap = imageMap;
        this.imageMapName = imageMapName;
        this.parameters.putAll(parameters);
    }

    /**
     * @deprecated you should use {@link #getBase64Image()}
     *
     * @see ChartHelper
     */
    public String getLocation()
    {
        return location;
    }

    public String getImageMap()
    {
        return imageMap;
    }

    public String getImageMapName()
    {
        return imageMapName;
    }

    public Map<String, Object> getParameters()
    {
        return parameters;
    }

    public String getBase64Image()
    {
        return (String) parameters.get("base64Image");
    }

}
