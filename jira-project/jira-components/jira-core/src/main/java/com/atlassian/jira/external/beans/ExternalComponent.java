package com.atlassian.jira.external.beans;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class ExternalComponent implements NamedExternalObject
{
    // @TODO refactor Remote RPC objects to use this

    private String id;
    private String name;
    private String projectId;
    private String lead;
    private String assigneeType;
    private String description;

    public ExternalComponent()
    {
    }

    public ExternalComponent(String name)
    {
        this.name = name;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getAssigneeType()
    {
        return assigneeType;
    }

    public void setAssigneeType(final String assigneeType)
    {
        this.assigneeType = assigneeType;
    }

    public String getLead()
    {
        return lead;
    }

    public void setLead(final String lead)
    {
        this.lead = lead;
    }

    public String getProjectId()
    {
        return projectId;
    }

    public void setProjectId(final String projectId)
    {
        this.projectId = projectId;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(final String description)
    {
        this.description = description;
    }

    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public boolean equals(Object o)
    {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

}
