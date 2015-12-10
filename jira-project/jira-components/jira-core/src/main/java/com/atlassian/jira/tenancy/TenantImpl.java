package com.atlassian.jira.tenancy;

import com.atlassian.tenancy.api.Tenant;

/**
 * Simple implementation of {@code Tenant} interface, with a simple ID property.
 *
 * @since v6.3.
 */
public class TenantImpl implements Tenant
{
    private final String id;

    public TenantImpl(final String id)
    {
        this.id = id;
    }

    public String getId()
    {
        return id;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        final TenantImpl tenant = (TenantImpl) o;
        return id.equals(tenant.id);
    }

    @Override
    public int hashCode()
    {
        return id.hashCode();
    }
}
