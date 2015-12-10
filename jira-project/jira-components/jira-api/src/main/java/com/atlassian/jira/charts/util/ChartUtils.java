package com.atlassian.jira.charts.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;

import com.atlassian.jira.issue.search.SearchRequest;

/**
 * Utility class for charting
 *
 * @since v4.0
 */
public interface ChartUtils
{
    /**
     * Make a search request out of the searchQuery parameter and populate the report/portlet param map
     *
     * @param searchQuery a jql string, a project id or a filter id
     * @param params            the map of parameters to modify
     * @return The SearchRequest for the project/filter id
     */
    SearchRequest retrieveOrMakeSearchRequest(String searchQuery, Map<String, Object> params);

    /**
     * Get the directory that charts are temporarily stored in.
     * @return Directory
     * @since v6.3
     */
    File getChartDirectory();

    /**
     * Encode given <code>image</code> into the base 64 string.
     *
     * @param image image to be encoded
     * @param chartName name that occurs in log in case of error
     *
     * @return <code>image</code> as a {@link String} in base 64 encoded format
     * ("data:image/png;base64,iVBORw0KGgoAAAANS...")
     */
    String renderBase64Chart(BufferedImage image, String chartName);
}
