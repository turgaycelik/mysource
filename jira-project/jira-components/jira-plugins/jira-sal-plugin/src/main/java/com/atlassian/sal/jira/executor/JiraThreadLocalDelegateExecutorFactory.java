package com.atlassian.sal.jira.executor;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.tenancy.TenantContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.sal.core.executor.DefaultThreadLocalDelegateExecutorFactory;
import com.atlassian.sal.core.executor.ThreadLocalDelegateScheduledExecutorService;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.DisposableBean;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import com.atlassian.sal.core.executor.ThreadLocalDelegateExecutorService;

/**
 * Instance of the delegate executor factory tailored to JIRA
 */
public class JiraThreadLocalDelegateExecutorFactory
        extends DefaultThreadLocalDelegateExecutorFactory<JiraThreadLocalContextManager.JiraThreadLocalContext> implements DisposableBean
{
    private final List<ThreadLocalDelegateExecutorService> executorServices = Lists.newArrayList();

    public JiraThreadLocalDelegateExecutorFactory(JiraAuthenticationContext authenticationContext,
            final VelocityRequestContextFactory velocityRequestContextFactory, final TenantContext tenantContext)
    {
        super(new JiraThreadLocalContextManager(authenticationContext, velocityRequestContextFactory, tenantContext));
    }



    @Override
    public ExecutorService createExecutorService(final ExecutorService delegate)
    {
        final ThreadLocalDelegateExecutorService executorService = (ThreadLocalDelegateExecutorService)super.createExecutorService(delegate);
        executorServices.add(executorService);
        return executorService;
    }

    @Override
    public ScheduledExecutorService createScheduledExecutorService(final ScheduledExecutorService delegate)
    {
        final ThreadLocalDelegateScheduledExecutorService scheduledExecutorService = (ThreadLocalDelegateScheduledExecutorService)super.createScheduledExecutorService(delegate);
        executorServices.add(scheduledExecutorService);
        return scheduledExecutorService;
    }


    @Override
    public void destroy()
    {
        try
        {
            for (ThreadLocalDelegateExecutorService executorService : executorServices)
            {
                executorService.shutdownNow();
            }
        }
        finally
        {
            executorServices.clear();
        }
    }
}
