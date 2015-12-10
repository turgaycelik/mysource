package com.atlassian.jira.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * provides access to the  jira-downgrade.properties file
 *
 * @since v6.0
 */
public class DowngradeUtilsImpl
{

    /**
     * The name of the properties file that contains the build properties.
     */
    private static final String PROPERTIES_FILENAME = "jira-downgrade.properties";

    /**
     * Logger for this class.
     */
    private static final Logger logger = LoggerFactory.getLogger(BuildUtilsInfoImpl.class);

    /**
     * The downgrade properties.
     */
    private final Properties downgradeProperties;
    private final int downgradeBuildNumber;

    /**
     * Creates a new DowngradeUtilsImpl, loading the properties from the '{@value #PROPERTIES_FILENAME}' file.
     * file.
     */
    public DowngradeUtilsImpl()
    {
        downgradeProperties = loadProperties();
        downgradeBuildNumber = Integer.parseInt(BuildUtils.getCurrentBuildNumber());
    }


    public String getDowngradeAllowedVersion()
    {
        return downgradeProperties.getProperty("downgrade.minimum.build.version");
    }
     /**
     * Loads the properties from the '{@value #PROPERTIES_FILENAME}' file.
     *
     * @return a new Properties instance
     * @throws RuntimeException if there's a problem reading the file
     */
    private Properties loadProperties() throws RuntimeException
    {
        InputStream propsFile = BuildUtilsInfoImpl.class.getResourceAsStream("/" + PROPERTIES_FILENAME);
        if (propsFile == null)
        {
            throw new IllegalStateException("File not found: " + PROPERTIES_FILENAME);
        }

        Properties result = new Properties();
        try
        {
            result.load(propsFile);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            try
            {
                propsFile.close();
            }
            catch (IOException e)
            {
                logger.warn("Error closing {}", propsFile);
            }
        }

        return result;
    }

}
