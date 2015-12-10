package com.atlassian.jira.charts.util;

import org.apache.commons.lang.StringUtils;

/**
 * Common utility methods for charting reports
 *
 * @since v4.0
 */
public class ChartReportUtils
{
    public static final String PROJECT_PARAM_PREFIX = "project-";
    public static final String FILTER_PARAM_PREFIX = "filter-";

    /**
     * This class should only be used for its static utility methods
     */
    private ChartReportUtils()
    {
    }

    /**
     * Checks that the projectOrFilterId has a valid format according to {@link #isValidFilterParamFormat(String)} with
     * prefix {@link #PROJECT_PARAM_PREFIX}
     *
     * @param projectOrFilterId the value from form to validate
     * @return true if the projectOrFilterId has {@link #PROJECT_PARAM_PREFIX project}-ID format
     * @see #isValidFilterParamFormat(String)
     */
    public static boolean isValidProjectParamFormat(String projectOrFilterId)
    {
        return isValidParamFormat(projectOrFilterId, PROJECT_PARAM_PREFIX);
    }

    /**
     * Checks that the projectOrFilterId has a valid format according to {@link #isValidFilterParamFormat(String)} with
     * prefix {@link #FILTER_PARAM_PREFIX}
     *
     * @param projectOrFilterId the value from form to validate
     * @return true if the projectOrFilterId has {@link #FILTER_PARAM_PREFIX filter-}ID format
     * @see #isValidFilterParamFormat(String)
     */
    public static boolean isValidFilterParamFormat(String projectOrFilterId)
    {
        return isValidParamFormat(projectOrFilterId, FILTER_PARAM_PREFIX);
    }

    /**
     * Checks that the report param 'projectOrFilterId' has the expected internal format. It must start with the given
     * prefix and have an id component. This method does not check if the id component is valid, just checks it has
     * one.
     *
     * @param projectOrFilterId the value from form
     * @param prefix the expected prefix of the param. either {@link #PROJECT_PARAM_PREFIX} or {@link
     * #FILTER_PARAM_PREFIX}
     * @return true if projectOrFilterId starts with the expected prefix and has an id component
     */
    private static boolean isValidParamFormat(String projectOrFilterId, String prefix)
    {
        String id = ChartReportUtils.extractProjectOrFilterId(projectOrFilterId);
        return StringUtils.isNotEmpty(id) && projectOrFilterId.startsWith(prefix);
    }

    /**
     * Extracts the id component of the 'projectOrFilterId'.
     *
     * @param projectOrFilterId the raw value of 'projectOrFilterId' from the request
     * @return the id component of the 'projectOrFilterId', null if projectOrFilterId is null or empty string or has no
     *         id
     */
    public static String extractProjectOrFilterId(String projectOrFilterId)
    {
        if (StringUtils.isNotEmpty(projectOrFilterId))
        {
            int startOfId = projectOrFilterId.indexOf('-');
            if (startOfId != -1)
            {
                return projectOrFilterId.substring(startOfId + 1);//+1 to ignore the dash
            }
        }
        return null;
    }
}