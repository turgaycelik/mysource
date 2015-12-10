package com.atlassian.jira.hallelujah;

import com.atlassian.buildeng.hallelujah.HallelujahServer;
import com.atlassian.buildeng.hallelujah.jms.JMSConnectionFactory.DeliveryMode;
import com.atlassian.buildeng.hallelujah.jms.JMSHallelujahServer;
import com.atlassian.jira.functest.framework.SuiteListenerWrapper;
import com.atlassian.jira.hallelujah.FailSlowTestsListener;
import com.atlassian.jira.webtests.AcceptanceTestHarness;
import junit.framework.TestSuite;

import javax.jms.JMSException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class JIRAHallelujahServer
{
    public static void main (String[] args) throws IOException
    {
        System.out.println("JIRA Hallelujah Server starting...");

        /* run all the tests */
        System.setProperty("jira.edition", "all");

        final TestSuite testSuite = (TestSuite) ((SuiteListenerWrapper) AcceptanceTestHarness.suite()).delegate();
        final String junitFilename = "TEST-Hallelujah.xml";
        final String suiteName = "AcceptanceTestHarness";

        HallelujahServer hallelujahServer = null;
        try
        {
            hallelujahServer = new JMSHallelujahServer.Builder()
                    .setJmsConfig(JIRAHallelujahConfig.getConfiguration())
                    .setSuite(testSuite)
                    .setTestResultFileName(junitFilename)
                    .setSuiteName(suiteName)
                    .setDeliveryMode(DeliveryMode.PERSISTENT)
                    .build()
                    .registerListeners(new FailSlowTestsListener(5, TimeUnit.MINUTES));
        }
        catch (JMSException e)
        {
            throw new RuntimeException(e);
        }

        boolean success = hallelujahServer.call();

        System.out.println("JIRA Hallelujah Server finished. " + success + " was the execution result");

        if (!success)
        {
            System.exit(1);
        }
    }

}
