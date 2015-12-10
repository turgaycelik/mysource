package com.atlassian.jira.event;

import com.atlassian.annotations.PublicApi;

/**
 * Published when the dashboard page is viewed.
 */
@PublicApi
public final class DashboardViewEvent extends AbstractEvent
{
    private final Long dashboardId;

    public DashboardViewEvent(Long dashboardId)
    {

        this.dashboardId = dashboardId;
    }

    /**
     * @return the dashboard ID
     */
    public Long getId()
    {
        return dashboardId;
    }
}
