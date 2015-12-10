package com.atlassian.jira.util.thread;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import org.apache.log4j.Logger;

/**
 * The implementation of OffRequestThreadExecutor
 *
 * @since v6.0
 */
public class OffRequestThreadExecutorImpl implements OffRequestThreadExecutor
{

    private static final Logger log = Logger.getLogger(OffRequestThreadExecutorImpl.class);

    private final JiraAuthenticationContext jiraAuthenticationContext;

    public OffRequestThreadExecutorImpl(final JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    @Override
    public void execute(final Runnable command)
    {
        execute(null, command);
    }

    @Override
    public void execute(final ApplicationUser runAsUser, final Runnable command)
    {
        try
        {
            preCall(runAsUser);
            command.run();
        }
        finally
        {
            postCall(command);

        }
    }

    private void preCall(final ApplicationUser runAsUser)
    {
        JiraThreadLocalUtils.preCall();
        jiraAuthenticationContext.setLoggedInUser(runAsUser);
    }

    private void postCall(final Runnable command)
    {
        jiraAuthenticationContext.setLoggedInUser((ApplicationUser) null);
        JiraThreadLocalUtils.postCall(log, new JiraThreadLocalUtils.ProblemDeterminationCallback()
        {
            @Override
            public void onOpenTransaction()
            {
                log.error("A database connection was left open by the code : " + command.getClass().getName());
            }
        });
    }


}
