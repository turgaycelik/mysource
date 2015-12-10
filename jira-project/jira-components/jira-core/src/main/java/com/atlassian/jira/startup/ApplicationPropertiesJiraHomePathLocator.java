package com.atlassian.jira.startup;

import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.util.concurrent.LazyReference;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * JIRA Home path locator that gets the value from the jira-application.properties file and not the database.
 */
public class ApplicationPropertiesJiraHomePathLocator implements JiraHomePathLocator
{

    private static final String APP_PROPERTIES_DEFAULTS = "jira-application.properties";
    private static final Logger log = Logger.getLogger(ApplicationPropertiesJiraHomePathLocator.class);

    @ClusterSafe("Application properties are read from the class path and only at startup.")
    LazyReference<Properties> applicationProperties = new LazyReference<Properties>()
    {

        @Override
        protected Properties create() throws Exception
        {
            Properties defaultProperties = new Properties();

            final InputStream in = ClassLoaderUtils.getResourceAsStream(APP_PROPERTIES_DEFAULTS, this.getClass());
            try
            {
                defaultProperties.load(in);
                in.close();
            }
            catch (final IOException e)
            {
                log.error("Could not load default properties from '" + APP_PROPERTIES_DEFAULTS + "'.  Not using default properties");
            }
            return defaultProperties;
        }
    };

    public String getJiraHome()
    {
        return applicationProperties.get().getProperty(Property.JIRA_HOME);
    }

    public String getDisplayName()
    {
        return "Application Properties";
    }
}
