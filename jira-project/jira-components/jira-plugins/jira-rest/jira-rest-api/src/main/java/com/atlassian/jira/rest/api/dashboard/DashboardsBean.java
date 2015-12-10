package com.atlassian.jira.rest.api.dashboard;

import com.google.common.collect.ImmutableList;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

/**
 * JSON bean for the dashboard search results.
 *
 * @since v5.0
 */
@SuppressWarnings ({ "UnusedDeclaration" })
public class DashboardsBean
{
    @JsonProperty
    private Integer startAt;

    @JsonProperty
    private Integer maxResults;

    @JsonProperty
    private Integer total;

    @JsonProperty
    private String prev;

    @JsonProperty
    private String next;

    @JsonProperty
    private List<DashboardBean> dashboards;

    public DashboardsBean()
    {
    }

    public DashboardsBean(Integer startAt, Integer maxResults, Integer total, String prev, String next, List<DashboardBean> dashboards)
    {
        this.startAt = startAt;
        this.maxResults = maxResults;
        this.total = total;
        this.prev = prev;
        this.next = next;
        this.dashboards = dashboards != null ? ImmutableList.copyOf(dashboards) : null;
    }

    public Integer startAt()
    {
        return this.startAt;
    }

    public DashboardsBean startAt(Integer startAt)
    {
        return new DashboardsBean(startAt, maxResults, total, prev, next, dashboards);
    }

    public Integer maxResults()
    {
        return this.maxResults;
    }

    public DashboardsBean maxResults(Integer maxResults)
    {
        return new DashboardsBean(startAt, maxResults, total, prev, next, dashboards);
    }

    public Integer total()
    {
        return this.total;
    }

    public DashboardsBean total(Integer total)
    {
        return new DashboardsBean(startAt, maxResults, total, prev, next, dashboards);
    }

    public String prev()
    {
        return this.prev;
    }

    public DashboardsBean prev(String previous)
    {
        return new DashboardsBean(startAt, maxResults, total, previous, next, dashboards);
    }

    public String next()
    {
        return this.next;
    }

    public DashboardsBean next(String next)
    {
        return new DashboardsBean(startAt, maxResults, total, prev, next, dashboards);
    }

    public List<DashboardBean> dashboards()
    {
        return this.dashboards;
    }

    public DashboardsBean dashboards(List<DashboardBean> dashboards)
    {
        return new DashboardsBean(startAt, maxResults, total, prev, next, dashboards);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        DashboardsBean that = (DashboardsBean) o;

        if (dashboards != null ? !dashboards.equals(that.dashboards) : that.dashboards != null) { return false; }
        if (maxResults != null ? !maxResults.equals(that.maxResults) : that.maxResults != null) { return false; }
        if (next != null ? !next.equals(that.next) : that.next != null) { return false; }
        if (prev != null ? !prev.equals(that.prev) : that.prev != null) { return false; }
        if (startAt != null ? !startAt.equals(that.startAt) : that.startAt != null) { return false; }
        if (total != null ? !total.equals(that.total) : that.total != null) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = startAt != null ? startAt.hashCode() : 0;
        result = 31 * result + (maxResults != null ? maxResults.hashCode() : 0);
        result = 31 * result + (total != null ? total.hashCode() : 0);
        result = 31 * result + (prev != null ? prev.hashCode() : 0);
        result = 31 * result + (next != null ? next.hashCode() : 0);
        result = 31 * result + (dashboards != null ? dashboards.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "DashboardsBean{" +
                "startAt=" + startAt +
                ", maxResults=" + maxResults +
                ", total=" + total +
                ", previous='" + prev + '\'' +
                ", next='" + next + '\'' +
                ", dashboards=" + dashboards +
                '}';
    }
}
