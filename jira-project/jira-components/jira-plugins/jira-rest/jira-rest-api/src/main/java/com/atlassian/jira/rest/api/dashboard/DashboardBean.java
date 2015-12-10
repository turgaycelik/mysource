package com.atlassian.jira.rest.api.dashboard;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * JSON bean for representing a single dashboard.
 *
 * @since v5.0
 */
@SuppressWarnings ({ "UnusedDeclaration" })
public class DashboardBean
{
    @JsonProperty
    private String id;

    @JsonProperty
    private String name;

    @JsonProperty
    private String self;

    @JsonProperty
    private String view;

    public DashboardBean()
    {
    }

    public DashboardBean(String id, String name, String self, String view)
    {
        this.id = id;
        this.name = name;
        this.self = self;
        this.view = view;
    }

    public String id()
    {
        return this.id;
    }

    public DashboardBean id(String id)
    {
        return new DashboardBean(id, name, self, view);
    }

    public String name()
    {
        return this.name;
    }

    public DashboardBean name(String name)
    {
        return new DashboardBean(id, name, self, view);
    }

    public String self()
    {
        return this.self;
    }

    public DashboardBean self(String self)
    {
        return new DashboardBean(id, name, self, view);
    }

    public String view()
    {
        return this.view;
    }

    public DashboardBean view(String view)
    {
        return new DashboardBean(id, name, self, view);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        DashboardBean that = (DashboardBean) o;

        if (id != null ? !id.equals(that.id) : that.id != null) { return false; }
        if (name != null ? !name.equals(that.name) : that.name != null) { return false; }
        if (self != null ? !self.equals(that.self) : that.self != null) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (self != null ? self.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "DashboardBean{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", self='" + self + '\'' +
                '}';
    }
}
