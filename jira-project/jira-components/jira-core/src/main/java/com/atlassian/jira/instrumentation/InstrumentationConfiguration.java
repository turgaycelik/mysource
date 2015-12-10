package com.atlassian.jira.instrumentation;

import com.atlassian.instrumentation.RegistryConfiguration;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.util.BuildUtilsInfo;

import java.io.File;

/**
 * Our configuration of instrumentation code
 *
 * @since v5.0
 */
public class InstrumentationConfiguration implements RegistryConfiguration
{
    public InstrumentationConfiguration()
    {
    }

    @Override
    public String getRegistryName()
    {
        BuildUtilsInfo buildInfo = ComponentAccessor.getComponent(BuildUtilsInfo.class);
        return "JIRA-" + buildInfo.getBuildInformation();
    }

    @Override
    public boolean isCPUCostCollected()
    {
        return JiraSystemProperties.isDevMode();
    }

    @Override
    public File getRegistryHomeDirectory()
    {
        return new File(ComponentAccessor.getComponent(JiraHome.class).getLocalHome(),"instrumentation");
    }
}
