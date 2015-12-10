package com.atlassian.jira.dashboard;

import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Implements static utility methods for dashboards
 *
 * @since v4.0
 */
public final class DashboardUtil
{
    private static final Logger log = Logger.getLogger(DashboardUtil.class);

    private static final int MAX_GADGETS_DEFAULT = 20;

    private DashboardUtil() {}

    /**
     * Convert a {@link com.atlassian.gadgets.dashboard.DashboardId} to long
     *
     * @param dashboardId dashboardId to convert
     * @return The converted Long value
     */
    public static Long toLong(DashboardId dashboardId)
    {
        return dashboardId == null ? null : Long.valueOf(dashboardId.value());
    }

    /**
     * Convert a {@link com.atlassian.gadgets.GadgetId} to long
     *
     * @param gadgetId gadgetId to convert
     * @return The converted Long value
     */
    public static Long toLong(GadgetId gadgetId)
    {
        return gadgetId == null ? null : Long.valueOf(gadgetId.value());
    }

    public static int getMaxGadgets(ApplicationProperties applicationProperties)
    {
        final String maxGadgetsString = applicationProperties.getDefaultBackedString(APKeys.JIRA_DASHBOARD_MAX_GADGETS);
        if (StringUtils.isNotBlank(maxGadgetsString))
        {
            try
            {
                return Integer.valueOf(maxGadgetsString);
            }
            catch (NumberFormatException e)
            {
                log.warn(APKeys.JIRA_DASHBOARD_MAX_GADGETS + " is not set to valid number value '" + maxGadgetsString + "'. Falling back to default");
                return MAX_GADGETS_DEFAULT;
            }
        }

        //looks like no property has been set. Falling back to default.
        return MAX_GADGETS_DEFAULT;
    }

}
