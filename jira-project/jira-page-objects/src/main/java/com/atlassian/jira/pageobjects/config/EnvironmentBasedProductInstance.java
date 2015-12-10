package com.atlassian.jira.pageobjects.config;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.atlassian.jira.webtests.util.LocalTestEnvironmentData;
import com.atlassian.pageobjects.ProductInstance;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * {@link com.atlassian.pageobjects.ProductInstance} implementation based on
 * {@link com.atlassian.jira.webtests.util.JIRAEnvironmentData}.
 *
 * @since v4.3
 */
public class EnvironmentBasedProductInstance implements ProductInstance
{
    private final JIRAEnvironmentData environmentData;

    public EnvironmentBasedProductInstance(JIRAEnvironmentData environmentData)
    {
        this.environmentData = notNull(environmentData);
    }

    public EnvironmentBasedProductInstance()
    {
        this(new LocalTestEnvironmentData());
    }

    @Override
    public String getBaseUrl()
    {
        return environmentData.getBaseUrl().toString();
    }

    @Override
    public int getHttpPort()
    {
        return environmentData.getBaseUrl().getPort();
    }

    @Override
    public String getContextPath()
    {
        return environmentData.getContext();
    }

    @Override
    public String getInstanceId()
    {
        return "JIRA";
    }

    public JIRAEnvironmentData environmentData()
    {
        return environmentData;
    }
}
