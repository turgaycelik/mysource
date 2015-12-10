package com.atlassian.jira.functest.config.crowd;

import com.atlassian.jira.functest.config.CheckOptions;
import com.atlassian.jira.functest.config.CheckResultBuilder;
import com.atlassian.jira.functest.config.ConfigurationCheck;
import com.atlassian.jira.functest.config.JiraConfig;
import org.apache.commons.lang.StringUtils;

/**
 * Checks to make sure that the Crow Applications have an application type.
 *
 * @since v4.3
 */
public class CrowdApplicationCheck implements ConfigurationCheck
{
    public static final String CHECK_APPLICATION_TYPE = "crowdapplicationtype";

    @Override
    public Result checkConfiguration(JiraConfig config, CheckOptions options)
    {
        CheckResultBuilder builder = new CheckResultBuilder();
        if (options.checkEnabled(CHECK_APPLICATION_TYPE))
        {
            for (ConfigCrowdApplication application : config.getCrowdApplications())
            {
                if (StringUtils.isBlank(application.getApplicationType()))
                {
                    String name = application.getName() == null ? "<unknown>" : application.getName();
                    builder.error("Crowd application '" + name + "' exists without type.", CHECK_APPLICATION_TYPE);
                }
            }
        }
        return builder.buildResult();
    }

    @Override
    public void fixConfiguration(JiraConfig config, CheckOptions options)
    {
        for (ConfigCrowdApplication crowdApplication : config.getCrowdApplications())
        {
            if (options.checkEnabled(CHECK_APPLICATION_TYPE) && StringUtils.isBlank(crowdApplication.getApplicationType()))
            {
                crowdApplication.setApplicationType("CROWD");
            }
        }
    }
}
