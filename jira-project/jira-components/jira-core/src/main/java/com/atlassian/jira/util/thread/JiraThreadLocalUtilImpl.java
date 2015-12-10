package com.atlassian.jira.util.thread;

import com.atlassian.jira.util.thread.JiraThreadLocalUtils.ProblemDeterminationCallback;
import org.apache.log4j.Logger;


/**
 * A concrete implementation of {@link JiraThreadLocalUtil} so that plugin
 * developers can have an API route into the {@link JiraThreadLocalUtils}
 * cleanup code.
 *
 * @since v6.0
 */
public class JiraThreadLocalUtilImpl implements JiraThreadLocalUtil
{
    @Override
    public void preCall()
    {
        JiraThreadLocalUtils.preCall();
    }

    @Override
    public void postCall(final Logger log)
    {
        JiraThreadLocalUtils.postCall(log, null);
    }

    @Override
    public void postCall(final Logger log, final WarningCallback warningCallback)
    {
        JiraThreadLocalUtils.postCall(log, wrap(warningCallback));
    }



    // TODO in v7.0: Delete this and change JiraThreadLocalUtils to accept WarningCallback directly.
    private static ProblemDeterminationCallback wrap(final WarningCallback callback)
    {
        if (callback == null)
        {
            return null;
        }

        return new ProblemDeterminationCallback()
        {
            @Override
            public void onOpenTransaction()
            {
                callback.onOpenTransaction();
            }
        };
    }
}
