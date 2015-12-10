package com.atlassian.jira.functest.config;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Reads build-time functional test properties.
 *
 * @since v5.1.3
 */
public class FuncProperties
{
    private static final String JIRA_FUNC_PROPERTIES = "/jira-func.properties";
    private static final Logger log = Logger.getLogger(FuncProperties.class);

    /**
     * Returns the value of a property as defined in the {@code jira-func.properties} file.
     *
     * @param propertyName a String containing the property name
     * @return the value of a property as defined in {@code jira-func.properties}, or null
     */
    public static String get(String propertyName)
    {
        InputStream is = FuncProperties.class.getResourceAsStream(JIRA_FUNC_PROPERTIES);
        if (is == null)
        {
            throw new IllegalStateException("File not found in classpath: " + JIRA_FUNC_PROPERTIES);
        }

        try
        {
            Properties props = new Properties();
            props.load(is);

            return props.getProperty(propertyName);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            try
            {
                is.close();
            }
            catch (IOException e)
            {
                log.error("Error closing file", e);
            }
        }
    }
}
