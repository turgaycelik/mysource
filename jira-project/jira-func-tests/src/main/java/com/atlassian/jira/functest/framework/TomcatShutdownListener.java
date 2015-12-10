package com.atlassian.jira.functest.framework;

import com.atlassian.cargotestrunner.SingleThreadedCargoRunner;
import com.atlassian.cargotestrunner.SingleThreadedCargoWebTest;
import com.atlassian.jira.webtests.util.NativeCommands;
import org.apache.log4j.Logger;

import java.util.concurrent.TimeUnit;

public class TomcatShutdownListener implements WebTestListener
{
    private static final Logger log = Logger.getLogger(TomcatShutdownListener.class);

    private volatile boolean startedTest = false;

    @Override
    public void suiteStarted(final WebTestDescription suiteDescription)
    {
    }

    @Override
    public void suiteFinished(final WebTestDescription suiteDescription)
    {
        if (!startedTest)
        {
            log.warn("Unable to monitor Tomcat shutdown. I was not able to work out how it started.");
        }
    }

    @Override
    public void testError(WebTestDescription description, Throwable t)
    {
        fail(description, t);
    }

    @Override
    public void testFailure(WebTestDescription description, Throwable t)
    {
        fail(description, t);
    }

    @Override
    public void testFinished(WebTestDescription test)
    {
    }

    @Override
    public void testStarted(WebTestDescription test)
    {
        if (isTomcat(test))
        {
            startedTest = true;
        }
    }

    private void fail(WebTestDescription testDescription, Throwable throwable)
    {
        if (isTomcat(testDescription))
        {
            log.error(throwable.getMessage(), throwable);
            NativeCommands.outputJavaProcesses(true);
            try
            {
                for (int i = 0; i < 3; i++)
                {
                    NativeCommands.dumpTomcatThreads();
                    Thread.sleep(TimeUnit.SECONDS.toMillis(20));
                }
            }
            catch (InterruptedException e)
            {
                //exit
            }
        }
    }

    private boolean isTomcat(WebTestDescription test)
    {
        return test instanceof SingleThreadedCargoWebTest || test instanceof SingleThreadedCargoRunner;
    }
}
