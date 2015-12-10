package com.atlassian.jira.tenancy;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.concurrent.Callable;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.InitializingComponent;
import com.atlassian.tenancy.api.Tenant;
import com.atlassian.tenancy.api.TenantAccessor;
import com.atlassian.tenancy.api.TenantUnavailableException;
import com.atlassian.tenancy.api.UnexpectedTenantChangeException;
import com.atlassian.tenancy.api.event.TenantArrivedEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of TenantAccessor for InstantOn - only has a system tenant
 *
 * @since v6.3
 */
public class JiraTenantAccessor implements TenantAccessor, InitializingComponent
{
    private final static Logger log = LoggerFactory.getLogger(JiraTenantAccessor.class);

    private final TenantContext tenantContext;
    private final EventPublisher eventPublisher;

    private volatile Tenant systemTenant;

    public JiraTenantAccessor(final TenantContext tenantContext, final EventPublisher eventPublisher)
    {
        this.tenantContext = tenantContext;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Iterable<Tenant> getAvailableTenants()
    {
        if (systemTenant == null)
        {
            return Collections.emptySet();
        }
        else
        {
            return Collections.singleton(systemTenant);
        }
    }

    @Override
    public <T> T asTenant(Tenant tenant, Callable<T> call) throws TenantUnavailableException, InvocationTargetException
    {
        Tenant currentTenant = tenantContext.getCurrentTenant();
        if (currentTenant == null)
        {
            log.warn("You are not associated with a tenant, so cannot call tenant specific code");
            throw new TenantUnavailableException();
        }
        if (tenant != currentTenant)
        {
            log.warn("You cannot invoke a runnable in another tenant's context");
            throw new UnexpectedTenantChangeException();
        }
        try
        {
            return call.call();
        }
        catch (Exception e)
        {
            throw new InvocationTargetException(e);
        }
    }

    @EventListener
    public void onTenantArrived(TenantArrivedEvent e)
    {
        systemTenant = e.getTenant();
    }

    @Override
    public void afterInstantiation() throws Exception
    {
        eventPublisher.register(this);
    }
}
