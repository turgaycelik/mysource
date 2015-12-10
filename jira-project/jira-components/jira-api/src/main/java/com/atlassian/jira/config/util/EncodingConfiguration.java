/**
 * Copyright 2008 Atlassian Pty Ltd 
 */
package com.atlassian.jira.config.util;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.util.dbc.Assertions;

/**
 * Get the current encoding.
 * 
 * @since v3.13
 */
public interface EncodingConfiguration
{
    String getEncoding();

    public static class PropertiesAdaptor implements EncodingConfiguration
    {
        private final ApplicationProperties applicationProperties;

        public PropertiesAdaptor(final ApplicationProperties applicationProperties)
        {
            Assertions.notNull("applicationProperties", applicationProperties);
            this.applicationProperties = applicationProperties;
        }

        public String getEncoding()
        {
            return applicationProperties.getEncoding();
        }
    }

    public static class Static implements EncodingConfiguration
    {
        private final String encoding;

        public Static(final String encoding)
        {
            Assertions.notBlank("encoding", encoding);
            this.encoding = encoding;
        }

        public String getEncoding()
        {
            return encoding;
        }
    }
}
