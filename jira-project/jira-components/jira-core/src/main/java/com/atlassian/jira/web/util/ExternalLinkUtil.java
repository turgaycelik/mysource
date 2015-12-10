package com.atlassian.jira.web.util;

/**
 * A simple utility class that lets you resolve external links that may need to change, because of partner sites and
 * such.
 */
public interface ExternalLinkUtil
{
    /**
     * @return The name of the propertiesfile that provides the external-links.
     */
    String getPropertiesFilename();

    String getProperty(String key);

    String getProperty(String key, String value1);

    String getProperty(String key, String value1, String value2);

    String getProperty(String key, String value1, String value2, String value3);

    String getProperty(String key, String value1, String value2, String value3, String value4);    

    String getProperty(String key, Object parameters);
}
