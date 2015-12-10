package com.atlassian.jira.functest.framework.util;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import junit.framework.Assert;
import net.sourceforge.jwebunit.WebTester;

/**
 * Common methods for navigation of pages that display the progress of an asynchronous task. i.e. Indexing, Workflow
 * Migration...
 *
 * @since v4.3
 */
public class AsynchronousTasks extends AbstractFuncTestUtil
{
    public AsynchronousTasks(WebTester tester, JIRAEnvironmentData environmentData, int logIndentLevel)
    {
        super(tester, environmentData, logIndentLevel);
    }

    /**
     * Waits for an asynchronous action to complete and acknowledges the result on completion
     * @param sleepTime The time to sleep before refreshing the page again and checking for the operation to
     * be finished.
     * @param retryCount The number of times we will try to check for the operation to be finished.
     * @param operationName The name of the operation that we are checking. Only used for printing messages.
     */
    public void waitForSuccessfulCompletion(long sleepTime, int retryCount, String operationName)
    {
        for (int i = 0; i < retryCount; i++)
        {
            if (tester.getDialog().hasSubmitButton("Refresh"))
            {
                tester.submit("Refresh");
            }
            else if (tester.getDialog().hasSubmitButton("Acknowledge"))
            {
                tester.submit("Acknowledge");
                return;
            }
            else
            {
                Assert.fail("Unexpected button on progress screen.");
            }

            try
            {
                Thread.sleep(sleepTime);
            }
            catch (InterruptedException e)
            {
                //ignore me.
            }
        }

        Assert.fail(operationName + " operation did not complete after " + (sleepTime * retryCount / 1000d) + " sec.");
    }
}
