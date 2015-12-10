package com.atlassian.jira.gadgets.system;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A simple bean contain all information required to render any Date Range Chart.
 *
 * @since v4.0
 */
@XmlRootElement
public class DateRangeChart
{
    // The URL where the chart image is available from.  The image is once of image that can only be accessed once.
    @XmlElement
    protected String location;
    // The title of the chart
    @XmlElement
    protected String filterTitle;
    // The link of where to send the user to - For a project, send em to the browse project, for a filter, send em tothe Issue Nav
    @XmlElement
    protected String filterUrl;
    @XmlElement
    protected String imageMap;
    @XmlElement
    protected String imageMapName;
    @XmlElement
    protected int width;
    @XmlElement
    protected int height;
    @XmlElement
    protected String base64Image;

    @SuppressWarnings ({ "UnusedDeclaration", "unused" })
    DateRangeChart()
    {}

    DateRangeChart(String location, String filterTitle, String filterUrl, String imageMap, String imageMapName,
            int width, int height, final String base64Image)
    {
        this.location = location;
        this.filterTitle = filterTitle;
        this.filterUrl = filterUrl;
        this.imageMap = imageMap;
        this.imageMapName = imageMapName;
        this.width = width;
        this.height = height;
        this.base64Image = base64Image;
    }
}
