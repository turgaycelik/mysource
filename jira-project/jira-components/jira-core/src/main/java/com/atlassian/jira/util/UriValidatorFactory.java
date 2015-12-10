package com.atlassian.jira.util;

import com.atlassian.jira.config.properties.ApplicationProperties;

/**
 * Instantiates an {@link com.atlassian.jira.util.UriValidator} using the encoding defined in the
 * {@link com.atlassian.jira.config.properties.ApplicationProperties}
 *
 * @since v4.3
 */
public class UriValidatorFactory
{
    /**
     * Creates a new instance of {@link com.atlassian.jira.util.UriValidator} using the encoding defined in the
     * {@link com.atlassian.jira.config.properties.ApplicationProperties}
     * @param applicationProperties the JIRA application properties.
     * @return An {@link com.atlassian.jira.util.UriValidator} configured with the supplied
     * {@link com.atlassian.jira.config.properties.ApplicationProperties}.
     */
    public static UriValidator create(ApplicationProperties applicationProperties)
    {
        return new UriValidator(applicationProperties.getEncoding());
    }
}