package com.atlassian.jira.util.thread;

import com.atlassian.jira.security.JiraAuthenticationContextImpl;
import com.atlassian.jira.util.ImportUtils;
import com.atlassian.jira.util.searchers.ThreadLocalSearcherCache;
import com.atlassian.jira.web.filters.ThreadLocalQueryProfiler;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.TransactionUtil;

import java.io.IOException;

// WARNING: The JiraThreadLocalUtil component should be preferred over using this class
// directly because it is injectable (and therefore can be mocked).  However, there are
// some parts of JIRA (like ImportTaskManagerImpl) that can not use an injectable
// component.  Also, prior to JIRA v6.0, plugin developers had no way to accomplish
// this cleanup except to access this jira-core class directly.  Therefore,  this
// class must remain around.  Do not deprecate it, remove it, or move the logic into
// JiraThreadLocalUtilImpl without considering those obstacles, first.

/**
 * This class has static methods that perform a number of standard operations at the start and
 * end of "runnable code" such as a {@link com.atlassian.jira.service.JiraServiceContainerImpl}
 * or a {@link com.atlassian.jira.task.TaskManagerImpl}.  Plugin developers that have previously
 * used this class directly should change to using {@link JiraThreadLocalUtil} from the API,
 * so that they can avoid having to depend on {@code jira-core}.
 * <p/>
 * The main purpose of this class is to setup and clear {@code ThreadLocal}
 * variables that can otherwise interfere with the smooth running of JIRA
 * by leaking resources or polluting the information used by the next request.
 * <p/>
 * You MUST remember to call {@link #postCall(Logger, ProblemDeterminationCallback)} in a
 * {@code finally} block to guarantee correct behaviour.  For example:
 * <p/>
 * <code><pre>
 * public void run()
 * {
 *     JiraThreadLocalUtils.preCall();
 *     try
 *     {
 *         // do runnable code here
 *     }
 *     finally
 *     {
 *         JiraThreadLocalUtils.postCall(log, myProblemDeterminationCallback);
 *     }
 * }
 * <pre></code>
 *
 * @since v3.13
 * @see JiraThreadLocalUtil
 */
//@PublicApi
public class JiraThreadLocalUtils
{
    /**
     * This should be called <strong>before</strong> any "runnable code" is called.
     * This will setup a clean {@code ThreadLocal} environment for the runnable
     * code to execute in.
     */
    public static void preCall()
    {
        JiraAuthenticationContextImpl.clearRequestCache();
        ThreadLocalQueryProfiler.start();
    }

    /**
     * This should be called in a {@code finally} block to clear up {@code ThreadLocal}s
     * once the runnable stuff has been done.
     *
     * @param log the log to write error messages to in casse of any problems
     * @param problemDeterminationCallback the callback to invoke in case where problems are
     *      detected after the runnable code is done running and its not cleaned up properly.
     *      This may be {@code null}, in which case those problems are logged as errors.
     */
    public static void postCall(final Logger log, final ProblemDeterminationCallback problemDeterminationCallback)
    {
        try
        {
            ThreadLocalQueryProfiler.end();
        }
        catch (final IOException e)
        {
            log.error("Unable to call ThreadLocalQueryProfiler.end()", e);
        }

        ThreadLocalSearcherCache.resetSearchers();

        if (!ImportUtils.isIndexIssues())
        {
            log.error("Indexing thread local not cleared. Clearing...");
            ImportUtils.setIndexIssues(true);
        }

        try
        {
            if (TransactionUtil.getLocalTransactionConnection() != null)
            {
                try
                {
                    if (problemDeterminationCallback != null)
                    {
                        problemDeterminationCallback.onOpenTransaction();
                    }
                    else
                    {
                        log.error("Uncommitted database transaction detected.  Closing...");
                    }
                }
                finally
                {
                    // Close the connection and clear the thead local
                    TransactionUtil.closeAndClearThreadLocalConnection();
                }
            }
        }
        catch (Exception t)
        {
            log.error("Error while inspecting transaction thread local.", t);
        }
    }

    // TODO in v7.0: Remove this interface, replacing all references with JiraThreadLocalUtil.WarningCallback.
    /**
     * This interface is used as a callback mechanism in the case where "runnable code" has completed
     * and the {@link #postCall(Logger, ProblemDeterminationCallback) postCall} determines that it did not clean
     * up properly.   Typically, all that can be done is to log the problem, but this interface
     * allows the detection of the problem and its logging to be separated, as the caller may be
     * able to provide more helpful information.
     */
    public interface ProblemDeterminationCallback
    {
        /**
         * Called when the {@link #postCall(Logger, ProblemDeterminationCallback) postCall} determines that
         * the runnable code began a {@link TransactionUtil database transaction} and failed to
         * {@link TransactionUtil#commit()} or {@link TransactionUtil#rollback()} the transaction
         * before it finished.  This could result in data inconsistencies, so the runnable code
         * should be fixed to handle transactions properly.
         */
        public void onOpenTransaction();
    }
}
