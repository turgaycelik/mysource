package com.atlassian.jira.hallelujah;

import com.atlassian.buildeng.hallelujah.HallelujahClient;
import com.atlassian.buildeng.hallelujah.jms.JMSConnectionFactory.DeliveryMode;
import com.atlassian.buildeng.hallelujah.jms.JMSHallelujahClient;
import com.atlassian.buildeng.hallelujah.listener.RateLimitingFailureClientListener;
import com.atlassian.buildeng.hallelujah.listener.TestsRunListener;
import com.atlassian.jira.functest.framework.CompositeSuiteListener;
import com.atlassian.jira.functest.framework.WebTestDescription;
import com.atlassian.jira.hallelujah.local.LocalHallelujahClient;
import com.atlassian.jira.pageobjects.config.junit4.LogTestInformationListener;
import com.atlassian.jira.webtest.capture.FFMpegSuiteListener;
import com.atlassian.jira.webtests.cargo.CargoTestHarness;
import com.google.common.base.Predicates;
import junit.framework.Test;
import junit.framework.TestResult;

import javax.jms.JMSException;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * We extend the CargoTestHarness in order to bring JIRA online the same way as the JIRA func tests
 *
 * But we don't actually want to run those tests the normal way, so we return a special suite that
 * runs a Halleujah Client instead
 *
 */
public class JIRAHallelujahClientTest extends CargoTestHarness
{
    public static Test suite() throws IOException
    {
        return suite(TestSuiteImpersonator.class);
    }

    private static HallelujahClient createClient()
    {
        final String localTestList = System.getProperty(LocalHallelujahClient.TEST_LIST_PROPERTY);
        if (localTestList != null)
        {
            try
            {
                System.out.println("Creating local client...");
                return new LocalHallelujahClient(new File(localTestList));
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        } else {
            try
            {
                System.out.println("Creating JMS client...");
                return new JMSHallelujahClient.Builder()
                        .setJmsConfig(JIRAHallelujahConfig.getConfiguration())
                        .setDeliveryMode(DeliveryMode.PERSISTENT)
                        .build();
            }
            catch (JMSException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    public static class TestSuiteImpersonator implements Test
    {
        private final HallelujahClient client;

        public TestSuiteImpersonator()
        {
            this.client = createClient();
        }

        @Override
        public int countTestCases()
        {
            return 1;
        }

        @Override
        public void run(TestResult result)
        {
            System.out.println("JIRA Hallelujah Client starting...");

            client.registerListeners(
                new TestsRunListener(new File("target/hallelujah-executed-tests.txt")),
                new RateLimitingFailureClientListener(20, 1, TimeUnit.MINUTES),
                new WebTestListenerToClientListenerAdapter(
                        CompositeSuiteListener.of(
                                new FFMpegSuiteListener(Predicates.<WebTestDescription>alwaysTrue()),
                                new LogTestInformationListener("=====WEB DRIVER")
                        )
                )
            ).run();

            System.out.println("JIRA Hallelujah Client finished.");
        }

        public static Test suite()
        {
            return new TestSuiteImpersonator();
        }
    }
}
