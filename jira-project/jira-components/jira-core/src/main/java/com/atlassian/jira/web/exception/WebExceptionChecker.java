package com.atlassian.jira.web.exception;

import com.atlassian.jira.util.Predicate;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.LinkedHashSet;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * There are a series of Exceptions that we deem to be acceptable to be ignored when sending web data back to clients
 * <p/>
 * For example if the browser has reset the connection, then we dont really care about sending back a 500 or logging the
 * exception since the former will never be heard and the latter will just fill up the logs.
 *
 * @since v4.2
 */
public class WebExceptionChecker
{
    private final static Set<Predicate<Throwable>> IGNORED_EXCEPTIONS = new LinkedHashSet<Predicate<Throwable>>();
    private static final Logger log = Logger.getLogger(WebExceptionChecker.class);

    static
    {
        IGNORED_EXCEPTIONS.add(new Predicate<Throwable>()
        {
            public boolean evaluate(final Throwable throwable)
            {
                return "org.apache.catalina.connector.ClientAbortException".equals(throwable.getClass().getName());
            }
        });
        IGNORED_EXCEPTIONS.add(new Predicate<Throwable>()
        {
            public boolean evaluate(final Throwable throwable)
            {
                if ("java.net.SocketException".equals(throwable.getClass().getName()))
                {
                    // ok check the message for specific conditions
                    String msg = throwable.getMessage();
                    if (StringUtils.isNotBlank(msg))
                    {
                        return msg.contains("Connection reset") || msg.contains("Broken pipe");
                    }
                }
                return false;
            }
        });
    }

    /**
     * Returns true if the Throwable can be safely ignored.
     *
     * @param throwable the throwable in play
     *
     * @return true if it can safely be ignored
     */
    public static boolean canBeSafelyIgnored(Throwable throwable)
    {
        //noinspection ThrowableResultOfMethodCallIgnored
        notNull("throwable", throwable);

        for (Predicate<Throwable> predicate : IGNORED_EXCEPTIONS)
        {
            if (predicate.evaluate(throwable))
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Ignoring this exception", throwable);
                }
                return true;
            }
        }
        return false;
    }
}
