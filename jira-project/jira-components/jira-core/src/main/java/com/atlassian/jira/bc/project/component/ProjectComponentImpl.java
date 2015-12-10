package com.atlassian.jira.bc.project.component;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.ApplicationUser;

import org.ofbiz.core.entity.GenericValue;

/**
 * Standard implementation of the {@link ProjectComponent}.
 */
public class ProjectComponentImpl implements ProjectComponent
{
    private Long id;
    private String name;
    private long assigneeType;
    private String lead;
    private String description;
    private Long projectId;
    private GenericValue genericValue;

    public ProjectComponentImpl(String name, String description, String lead, long assigneeType)
    {
        this.description = description;
        this.lead = lead;
        this.name = name;
        this.assigneeType = assigneeType;
        this.genericValue = null;
        this.id = null;
    }


    protected ProjectComponentImpl(Long id, String name, String description, String lead, long assigneeType, Long projectId, GenericValue gv)
    {
        this(name, description, lead, assigneeType);
        this.id = id;
        this.projectId = projectId;
        this.genericValue = gv;
    }

    public Long getId()
    {
        return id;
    }

    public String getDescription()
    {
        return description;
    }

    public String getLead()
    {
        return lead;
    }

    @Override
    public ApplicationUser getComponentLead()
    {
        if (getLead() == null)
        {
            return null;
        }

        return ComponentAccessor.getUserManager().getUserByKey(getLead());
    }

    public String getName()
    {
        return name;
    }

    public Long getProjectId()
    {
        return projectId;
    }

    public long getAssigneeType()
    {
        return assigneeType;
    }

    /**
     * Returns true if same object, instance of ProjectComponent and equal ID.
     *
     * @param o object to compare with
     * @return true if same object, instance of ProjectComponent and equal ID; false otherwise
     */
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || !(o instanceof ProjectComponent))
        {
            return false;
        }
        Long thatId = ((ProjectComponent) o).getId();
        return thatId != null && thatId.equals(id);
    }

    /**
     * Returns the hash code of the ID
     *
     * @return hash code of the ID
     */
    public int hashCode()
    {
        return id.hashCode();
    }

    /**
     * Nice debug output.
     * @return a detailed String representation useful in debug.
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("ProjectComponentImpl { name='");
        sb.append(name);
        sb.append("', description='");
        sb.append(description == null ? "" : description);
        sb.append("'");
        sb.append(", lead='");
        sb.append(lead == null ? "" : lead);
        sb.append("', assigneeType='");
        sb.append(assigneeType);
        sb.append("', projectId='");
        sb.append(projectId == null ? "" : projectId.toString());
        sb.append("', id='");
        sb.append(id == null ? "" : id.toString());
        sb.append("' }");
        return sb.toString();
    }

    /**
     * @deprecated use this object instead
     * @return a GenericValue representation
     */
    public GenericValue getGenericValue()
    {
        return genericValue;
    }

    /**
     * @deprecated temporary
     */
    void setGenericValue(GenericValue genericValue)
    {
        this.genericValue = genericValue;
    }
}
