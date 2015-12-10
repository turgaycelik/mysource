package com.atlassian.jira.rest.v2.dashboard;

import com.atlassian.jira.rest.api.dashboard.DashboardBean;
import com.atlassian.jira.rest.api.dashboard.DashboardsBean;
import com.atlassian.jira.rest.v2.issue.Examples;

import java.util.Arrays;

/**
 * Examples for the <code>/dashboard</code> resource.
 *
 * @since v5.0
 */
public final class DashboardResourceExamples
{
    public static final DashboardBean SINGLE_EXAMPLE = new DashboardBean()
            .id("10000")
            .name("System Dashboard")
            .self(Examples.restURI("dashboard/10000").toString())
            .view(Examples.jiraURI("secure/Dashboard.jspa?selectPageId=10000").toString());

    public static final DashboardsBean LIST_EXAMPLE = new DashboardsBean()
            .startAt(10)
            .maxResults(10)
            .total(143)
            .prev(Examples.restURI("dashboard?startAt=0").toString())
            .next(Examples.restURI("dashboard?startAt=10").toString())
            .dashboards(Arrays.asList(
                    SINGLE_EXAMPLE,
                    new DashboardBean().id("20000").name("Build Engineering").self(Examples.restURI("dashboard/20000").toString()).view(Examples.jiraURI("secure/Dashboard.jspa?selectPageId=20000").toString())
            ));

    private DashboardResourceExamples()
    {
    }
}
