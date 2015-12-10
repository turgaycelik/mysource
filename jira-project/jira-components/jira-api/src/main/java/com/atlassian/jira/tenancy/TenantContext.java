package com.atlassian.jira.tenancy;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.tenancy.api.Tenant;

import javax.annotation.Nullable;

/**
 * The tenant context is used to track tenants
 *
 * @since v6.3
 */
@ExperimentalApi
public interface TenantContext extends com.atlassian.tenancy.api.TenantContext{

    @Nullable
    Tenant getCurrentTenant();

    void setCurrentTenant(Tenant tenant);

    void clearTenant();
}
