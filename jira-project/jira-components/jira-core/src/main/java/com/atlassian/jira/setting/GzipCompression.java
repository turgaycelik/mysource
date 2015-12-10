package com.atlassian.jira.setting;

import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;

/**
 * The "Use GZip compression" option.
 *
 * @since v5.1
 */
public class GzipCompression
{
    private final ApplicationProperties applicationProperties;
    private final FeatureManager featureManager;

    public GzipCompression(ApplicationProperties applicationProperties, FeatureManager featureManager)
    {
        this.applicationProperties = applicationProperties;
        this.featureManager = featureManager;
    }

    /**
     * Returns a boolean indicating whether GZIP compression is enabled in JIRA.
     *
     * @return true if GZIP compression is enabled in JIRA
     * @since v5.1
     */
    public boolean isEnabled()
    {
        return !featureManager.isOnDemand() && applicationProperties.getOption(APKeys.JIRA_OPTION_WEB_USEGZIP);
    }

    /**
     * Returns a boolean indicating whether admins can enable or disable GZIP compression in JIRA.
     *
     * @return true if the GZIP compression setting is read-only
     * @since v5.1
     */
    public boolean isSettableBy()
    {
        return !featureManager.isOnDemand();
    }

    /**
     * Enables or disables GZIP compression in JIRA.
     *
     * @param useGzipCompression a boolean indicating whether to enable or disable
     * @see #isSettableBy()
     * @since v5.1
     */
    public void set(boolean useGzipCompression)
    {
        if (isSettableBy())
        {
            applicationProperties.setOption(APKeys.JIRA_OPTION_WEB_USEGZIP, useGzipCompression);
        }
    }
}
