package com.atlassian.jira.webtests.cargo;

import com.atlassian.cargotestrunner.serverinformation.ServerInformation;
import com.atlassian.cargotestrunner.serverinformation.ServerInformationFactory;
import com.atlassian.cargotestrunner.webtest.TestSetupRunnerCallback;
import com.atlassian.cargotestrunner.webtest.TestSetupRunnerCallbackFactory;
import com.atlassian.jira.webtests.util.EnvironmentAware;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import org.junit.runner.Runner;

import java.util.Properties;

public class JiraRunnerCallbackFactory implements TestSetupRunnerCallbackFactory
{
    private final String urlPrefix;

    public JiraRunnerCallbackFactory(String urlPrefix)
    {
        this.urlPrefix = urlPrefix;
    }

    @Override
    public TestSetupRunnerCallback createCallback(final String containerId, final Properties properties)
    {
        return new TestSetupRunnerCallback()
        {
            @Override
            public void setupRunner(Runner runner)
            {
                if (runner instanceof EnvironmentAware)
                {
                    final ServerInformation serverInformation = ServerInformationFactory.getServerInformation(containerId, properties);
                    final JIRAEnvironmentData environmentData = new CargoEnvironmentData(serverInformation, urlPrefix, containerId, properties);
                    ((EnvironmentAware) runner).setEnvironmentData(environmentData);
                }
            }
        };
    }
}
