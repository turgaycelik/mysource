package com.atlassian.jira.config.properties;

import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.util.UrlValidator;
import org.apache.log4j.Logger;

/**
 * Checks that application properties (e.g. base URL) are valid on startup.
 */
public class ApplicationPropertiesChecker implements Startable
{
    private final ApplicationProperties applicationProperties;
    private static final Logger logger = Logger.getLogger(ApplicationPropertiesChecker.class);

    public ApplicationPropertiesChecker(ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void start() throws Exception
    {
        // Check the base URL.
        String baseUrl = applicationProperties.getString(APKeys.JIRA_BASEURL);
        if (baseUrl != null && !UrlValidator.isValid(baseUrl))
        {
            logger.error("The installation's base URL appears to be invalid ('" + baseUrl + "').");
        }
    }
}
