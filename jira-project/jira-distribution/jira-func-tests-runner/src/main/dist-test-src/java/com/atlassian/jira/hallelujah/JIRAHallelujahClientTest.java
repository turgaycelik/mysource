package com.atlassian.jira.hallelujah;

import com.atlassian.buildeng.hallelujah.HallelujahClient;
import com.atlassian.buildeng.hallelujah.api.model.TestCaseName;
import com.atlassian.buildeng.hallelujah.jms.JMSConnectionFactory.DeliveryMode;
import com.atlassian.buildeng.hallelujah.jms.JMSHallelujahClient;
import com.atlassian.buildeng.hallelujah.listener.TestsRunListener;
import com.atlassian.jira.hallelujah.local.LocalHallelujahClient;
import com.atlassian.jira.webtests.cargo.CargoTestHarness;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import junit.framework.Test;
import junit.framework.TestResult;
import org.apache.commons.io.FileUtils;

import javax.jms.JMSException;
import java.io.File;
import java.io.IOException;

/**
 * We extend the CargoTestHarness in order to bring JIRA online the same way as the JIRA func tests
 *
 * But we don't actually want to run those tests the normal way, so we return a special suite that
 * runs a Halleujah Client instead
 *
 * It suite looks even weirder because it contains another suite - this is what the CargoTestHarness expects
 */
public class JIRAHallelujahClientTest extends CargoTestHarness
{

    public static Test suite() throws IOException
    {
        return suite(JIRAHallelujahClientTest.TestSuiteImpersonator.class);
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
                System.out.println("Creating jms client...");
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
            client = JIRAHallelujahClientTest.createClient();
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

            client.registerListeners(new TestsRunListener(new File("target/hallelujah-executed-tests.txt"))).run();

            System.out.println("JIRA Hallelujah Client finished.");
        }

        public static Test suite()
        {
            return new JIRAHallelujahClientTest.TestSuiteImpersonator();
        }
    }
}
