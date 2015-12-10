package com.atlassian.jira.webtests.ztests.setup;

import com.atlassian.jira.functest.framework.setup.JiraSetupInstanceHelper;
import com.atlassian.jira.functest.framework.upm.DefaultCredentials;
import com.atlassian.jira.functest.framework.upm.UpmRestClient;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;

import org.junit.Assert;

import net.sourceforge.jwebunit.WebTester;

public class SetupPluginTestInfrastructure
{
    private final JiraSetupInstanceHelper setupInstanceHelper;
    private final UpmRestClient upmRestClient;

    public SetupPluginTestInfrastructure(final WebTester tester, final JIRAEnvironmentData environmentData)
    {
        setupInstanceHelper = new JiraSetupInstanceHelper(tester, environmentData);
        this.upmRestClient = new UpmRestClient(
                environmentData.getBaseUrl().toString(),
                DefaultCredentials.getDefaultAdminCredentials());
    }

    public void setup()
    {
        if (getSetupInstanceHelper().isJiraSetup())
        {
            throw new IllegalStateException("JIRA must be in pristine state.");
        }
    }

    public JiraSetupInstanceHelper getSetupInstanceHelper()
    {
        return setupInstanceHelper;
    }

    public UpmRestClient getUpmRestClient()
    {
        return upmRestClient;
    }
}