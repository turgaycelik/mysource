package com.atlassian.jira.tenancy;

import com.atlassian.tenancy.api.Tenant;

/**
 * @since v6.3
 */
public class TenantContextImpl implements TenantContext
{

    private final ThreadLocal<Tenant> currentTenant = new ThreadLocal<Tenant>();

    @Override
    public Tenant getCurrentTenant()
    {
        return currentTenant.get();
    }

    @Override
    public void setCurrentTenant(Tenant tenant)
    {
        if (currentTenant.get() == null)
        {
            currentTenant.set(tenant);
        }
    }

    @Override
    public void clearTenant()
    {
        currentTenant.remove();
    }
}
