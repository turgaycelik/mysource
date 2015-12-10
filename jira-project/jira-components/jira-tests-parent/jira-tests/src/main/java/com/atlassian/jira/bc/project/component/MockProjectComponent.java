package com.atlassian.jira.bc.project.component;

import com.atlassian.jira.user.ApplicationUser;

import org.ofbiz.core.entity.GenericValue;

public class MockProjectComponent implements ProjectComponent
{
    private Long id;
    private String name;
    private Long projectId;
    private String lead;

    public MockProjectComponent(Long id, String name)
    {
        this(id, name, null);
    }

    public MockProjectComponent(Long id, String name, Long projectId)
    {
        this.id = id;
        this.name = name;
        this.projectId = projectId;
    }

    public String getName()
    {
        return name;
    }

    public Long getId()
    {
        return id;
    }

    public String getDescription()
    {
        return null;
    }

    public String getLead()
    {
        return lead;
    }

    @Override
    public ApplicationUser getComponentLead()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Long getProjectId()
    {
        return projectId;
    }

    public long getAssigneeType()
    {
        return 0;
    }

    public MockProjectComponent setId(Long id)
    {
        this.id = id;
        return this;
    }

    public MockProjectComponent setName(String name)
    {
        this.name = name;
        return this;
    }

    public MockProjectComponent setProjectId(Long projectId)
    {
        this.projectId = projectId;
        return this;
    }

    public MockProjectComponent setLead(String lead)
    {
        this.lead = lead;
        return this;
    }

    public GenericValue getGenericValue()
    {
        return null;
    }
}
