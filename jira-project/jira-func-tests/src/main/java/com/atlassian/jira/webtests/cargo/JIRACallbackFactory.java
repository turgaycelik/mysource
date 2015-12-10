package com.atlassian.jira.webtests.cargo;

import com.atlassian.cargotestrunner.serverinformation.ServerInformation;
import com.atlassian.cargotestrunner.serverinformation.ServerInformationFactory;
import com.atlassian.cargotestrunner.webtest.TestSetupCallback;
import com.atlassian.cargotestrunner.webtest.TestSetupCallbackFactory;
import com.atlassian.jira.webtests.util.EnvironmentAware;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import junit.framework.Test;

import java.util.Properties;

public class JIRACallbackFactory implements TestSetupCallbackFactory
{
    private final String urlPrefix;

    public JIRACallbackFactory(String urlPrefix)
    {
        this.urlPrefix = urlPrefix;
    }

    public TestSetupCallback getTestSetupCallback(final String containerId, final Properties properties)
    {
        return new TestSetupCallback()
        {
            public void setupTest(Test test)
            {
                if (test instanceof EnvironmentAware)
                {
                    final ServerInformation serverInformation = ServerInformationFactory.getServerInformation(containerId, properties);
                    final JIRAEnvironmentData environmentData = new CargoEnvironmentData(serverInformation, urlPrefix, containerId, properties);
                    ((EnvironmentAware) test).setEnvironmentData(environmentData);
                }
            }
        };
    }
}
