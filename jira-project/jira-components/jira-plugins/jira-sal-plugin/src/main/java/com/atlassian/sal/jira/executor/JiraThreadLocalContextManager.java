package com.atlassian.sal.jira.executor;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.tenancy.TenantContext;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.sal.api.executor.ThreadLocalContextManager;
import com.atlassian.tenancy.api.Tenant;

/**
 * Manages the thread local state for JIRA
 */
public class JiraThreadLocalContextManager implements ThreadLocalContextManager<JiraThreadLocalContextManager.JiraThreadLocalContext>
{
    private final JiraAuthenticationContext authenticationContext;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final TenantContext tenantContext;

    public JiraThreadLocalContextManager(JiraAuthenticationContext authenticationContext, final VelocityRequestContextFactory velocityRequestContextFactory, TenantContext tenantContext)
    {
        this.authenticationContext = authenticationContext;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.tenantContext = tenantContext;
    }

    /**
     * Get the thread local context of the current thread
     *
     * @return The thread local context
     */
    public JiraThreadLocalContext getThreadLocalContext()
    {
        return new JiraThreadLocalContext(authenticationContext.getLoggedInUser(), velocityRequestContextFactory.getJiraVelocityRequestContext(), tenantContext.getCurrentTenant());
    }

    /**
     * Set the thread local context on the current thread
     *
     * @param context The context to set
     */
    @Override
    public void setThreadLocalContext(JiraThreadLocalContext context)
    {
        authenticationContext.setLoggedInUser(context.getUser());
        velocityRequestContextFactory.setVelocityRequestContext(context.getVelocityRequestContext());
        tenantContext.setCurrentTenant(context.getTenant());
    }

    /**
     * Clear the thread local context on the current thread
     */
    public void clearThreadLocalContext()
    {
        velocityRequestContextFactory.clearVelocityRequestContext();
        authenticationContext.clearLoggedInUser();
        tenantContext.clearTenant();
    }

    static class JiraThreadLocalContext
    {
        private final User user;
        private final VelocityRequestContext velocityRequestContext;
        private final Tenant tenant;

        private JiraThreadLocalContext(User user, VelocityRequestContext velocityRequestContext, Tenant tenant)
        {
            this.user = user;
            this.tenant = tenant;
            this.velocityRequestContext = velocityRequestContext;
        }

        public User getUser()
        {
            return user;
        }

        public VelocityRequestContext getVelocityRequestContext()
        {
            return velocityRequestContext;
        }

        public Tenant getTenant()
        {
            return tenant;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            JiraThreadLocalContext that = (JiraThreadLocalContext) o;

            if (user != null ? !user.equals(that.user) : that.user != null)
            {
                return false;
            }
            if (velocityRequestContext != null ? !velocityRequestContext.equals(that.velocityRequestContext) : that.velocityRequestContext != null)
            {
                return false;
            }
            if (tenant != null ? !tenant.equals(that.tenant) : that.tenant != null)
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = user != null ? user.hashCode() : 0;
            result = 31 * result + (velocityRequestContext != null ? velocityRequestContext.hashCode() : 0);
            result = 31 * result + (tenant != null ? tenant.hashCode() : 0);
            return result;
        }
    }

}
