package com.atlassian.jira.config.util;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;

/**
 * Thumbnail configuration.
 * 
 * @since v3.13
 */
public interface ThumbnailConfiguration
{
    /**
     * Maximum height for a thumbnail
     * @return height in pixels.
     */
    int getMaxHeight();

    /**
     * Maximum width for a thumbnail
     * @return height in pixels.
     */
    int getMaxWidth();

    /**
     * Implementation of {@link ThumbnailConfiguration} that uses the {@link ApplicationProperties} to get the values.
     */
    public class PropertiesAdaptor implements ThumbnailConfiguration
    {
        private final ApplicationProperties applicationProperties;

        public PropertiesAdaptor(final ApplicationProperties applicationProperties)
        {
            this.applicationProperties = applicationProperties;
        }

        public int getMaxHeight()
        {
            return Integer.valueOf(applicationProperties.getDefaultBackedString(APKeys.JIRA_THUMBNAIL_MAX_HEIGHT)).intValue();
        }

        public int getMaxWidth()
        {
            return Integer.valueOf(applicationProperties.getDefaultBackedString(APKeys.JIRA_THUMBNAIL_MAX_WIDTH)).intValue();
        }
    }
}
