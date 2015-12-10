package com.atlassian.jira.util.thread;

import com.atlassian.annotations.PublicApi;
import com.atlassian.annotations.PublicSpi;
import org.apache.log4j.Logger;

/**
 * The main purpose of this component is to setup and clear {@code ThreadLocal} variables that
 * can otherwise interfere with the smooth running of JIRA by leaking resources or allowing
 * stale cached information to survive between requests.
 * <p/>
 * Services that are registered as a {@link com.atlassian.jira.service.JiraService} or as a
 * {@link com.atlassian.sal.api.scheduling.PluginJob} do <strong>not</strong> need to use
 * this component, because the scheduler will perform the cleanup automatically as part of
 * the service's execution lifecycle.  However, any plugin that creates its own threads
 * for background processing must use this component to guard its work.  Prior to JIRA v6.0,
 * the only way to do this was to access the {@code jira-core} class {@code JiraThreadLocalUtils}
 * directly.
 * <p/>
 * You <strong>must</strong> place the cleanup call to {@link #postCall(Logger)} or
 * {@link #postCall(Logger, WarningCallback)} in a {@code finally} block
 * to guarantee correct behaviour.  For example:
 * <p/>
 * <code><pre>
 * public void run()
 * {
 *     jiraThreadLocalUtil.preCall();
 *     try
 *     {
 *         // do runnable code here
 *     }
 *     finally
 *     {
 *         jiraThreadLocalUtil.postCall(log, myWarningCallback);
 *     }
 * }
 * </pre></code>
 *
 * @since v6.0
 */
@PublicApi
public interface JiraThreadLocalUtil
{
    /**
     * This should be called <strong>before</strong> any "runnable code" is called.
     * This will setup a clean {@code ThreadLocal} environment for the runnable
     * code to execute in.
     */
    void preCall();

    /**
     * This convenience method is equivalent to
     * {@link #postCall(Logger,WarningCallback) postCall(log, null)}.
     * @param log as for {@link #postCall(Logger, WarningCallback)}
     */
    void postCall(final Logger log);

    /**
     * This should be called in a {@code finally} block to clear up {@code ThreadLocal}s
     * once the runnable stuff has been done.
     *
     * @param log the log to write error messages to in casse of any problems
     * @param warningCallback the callback to invoke in case where problems are
     *      detected after the runnable code is done running and its not cleaned up properly.
     *      This may be {@code null}, in which case those problems are logged as errors.
     */
    void postCall(final Logger log, final WarningCallback warningCallback);

    /**
     * This interface is used as a callback mechanism in the case where "runnable code" has completed
     * and the {@link #postCall(Logger, WarningCallback) postCall} determines that it did not clean
     * up properly.   Typically, all that can be done is to log the problem, but this interface
     * allows the detection of the problem and its logging to be separated, as the caller may be
     * able to provide more helpful information.
     */
    @PublicSpi
    public interface WarningCallback
    {
        /**
         * Called when the {@link #postCall(Logger, WarningCallback) postCall} determines that the runnable
         * code began a {@link org.ofbiz.core.entity.TransactionUtil database transaction} and failed to
         * {@link org.ofbiz.core.entity.TransactionUtil#commit() commit} or
         * {@link org.ofbiz.core.entity.TransactionUtil#rollback() rollback} the transaction
         * before it finished.  This could result in data inconsistencies, so the runnable code
         * should be fixed to handle transactions properly.
         */
        public void onOpenTransaction();
    }
}
