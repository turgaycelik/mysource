package com.atlassian.jira.util.log;

import org.apache.log4j.Logger;

/**
 * An Log4J logger wrapper than will only log 1 times  After maxTimes number of calls
 * the logger silently discards thee logging output.
 * <p/>
 * The logging is done via a delegate Logger
 * <p/>
 * You might use it a bit like this
 * <pre>
 * private static final Logger log = Logger.getLogger(MyObject.class);
 * private static final Logger oneShotParseErrorLog = new OneShotLogger(log);
 * ...
 * ...
 * if (parseErrorOnSomethingThatWeOnlywanToReportOnce == true) {
 *      oneShotParseErrorLog.log("Things seem quite screwy in your config");
 * }
 * </pre>
 *
 * @since v3.13
 */
public class OneShotLogger extends NShotLogger
{
    public OneShotLogger(final Logger delegateLogger)
    {
        super(delegateLogger, 1);
    }
}
