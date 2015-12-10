package com.atlassian.jira.functest.framework.log;

import com.atlassian.jira.testkit.client.Backdoor;
import com.atlassian.jira.testkit.client.log.FuncTestOut;

/**
 * Common code for logging messages on both side of the equator.  It writes to the func test log and to the
 * JIRA log if it can!
 *
 * @since v4.0
 */
public class LogOnBothSides
{
    /**
     * This will log messages on the client side and the JIRA log.  It catches all runtime exceptions so logging
     * doesn't stop things from working otherwise
     *
     * @param backdoor backdoor used
     * @param msg the message to log
     */
    public static void log(Backdoor backdoor, String msg)
    {
        try
        {
            FuncTestOut.log(msg);
            backdoor.logControl().info(msg);
        }
        catch (RuntimeException ignored)
        {
            // don't let the logging stop the eventual running of the test
        }
    }
}

