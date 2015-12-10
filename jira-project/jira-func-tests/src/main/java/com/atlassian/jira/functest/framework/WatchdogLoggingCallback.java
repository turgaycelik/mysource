package com.atlassian.jira.functest.framework;

import com.google.common.base.Function;
import org.apache.log4j.Logger;

import static com.atlassian.jira.webtests.util.NativeCommands.dumpTomcatThreads;

/**
 * A callback to the JIRA watchdog listener that logs the frozen test
 *
 * @see JiraTestWatchDog
 * @since v4.4
 */
public class WatchdogLoggingCallback implements Function<WebTestDescription, Void>
{
    public static WatchdogLoggingCallback INSTANCE = new WatchdogLoggingCallback();

    private static final Logger logger = Logger.getLogger("watchdog");

    @Override
    public Void apply(WebTestDescription test)
    {
        logger.error("Test '" + test.name() + "' seems to have frozen. Taking thread dumps.");
        dumpTomcatThreads();
        return null;
    }
}
