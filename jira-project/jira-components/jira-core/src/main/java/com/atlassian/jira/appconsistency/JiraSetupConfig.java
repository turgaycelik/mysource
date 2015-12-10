package com.atlassian.jira.appconsistency;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.johnson.setup.SetupConfig;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Determines whether or not JIRA is setup
 */
public class JiraSetupConfig implements SetupConfig
{
    private static final Logger log = Logger.getLogger(JiraSetupConfig.class);

    public void init(Map params)
    {
    }

    /**
     * Determines if JIRA is setup
     * @param uri The uri of the current page
     * @return returns false if JIRA is not setup up and the uri is not a setup page, otherwise false
     */
    public boolean isSetupPage(String uri)
    {
        try
        {
            //noinspection SimplifiableIfStatement
            if (uri == null)
            {
                return true;
            }
            else
            {
                return uri.startsWith("/secure/Setup");
            }
        }
        catch (Exception e)
        {
            //if there is an error assume that it is set up and there is a problem
            return true;
        }
    }

    public boolean isSetup()
    {
        try
        {
            final ApplicationProperties applicationProperties = ComponentAccessor.getApplicationProperties();
            return applicationProperties.getString(APKeys.JIRA_SETUP) != null;
        }
        catch (Exception e)
        {
            log.error(e, e);
        }
        return false;
    }
}
