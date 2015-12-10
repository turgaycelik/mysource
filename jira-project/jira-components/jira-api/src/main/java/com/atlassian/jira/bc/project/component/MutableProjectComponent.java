package com.atlassian.jira.bc.project.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.ApplicationUser;

import com.opensymphony.util.TextUtils;

import org.ofbiz.core.entity.GenericValue;

@PublicApi
public class MutableProjectComponent implements ProjectComponent
{

    private Long id;
    private String name;
    private String lead;
    private String description;
    private Long projectId;
    private long assigneeType;

    /**
     * Temporary, during migration to use ProjectComponent object.
     */
    private GenericValue genericValue;


    public MutableProjectComponent(Long id, String name, String description, String lead, long assigneeType, Long projectId)
    {
        this.id = id;
        setDescription(description);
        this.lead = lead;
        this.name = name;
        this.projectId = projectId;
        this.assigneeType = assigneeType;
    }

    /**
     * Copy given component
     *
     * @param c component to copy
     * @return new instance of MutableProjectComponent set with values of the given component
     */
    public static MutableProjectComponent copy(final ProjectComponent c)
    {
        MutableProjectComponent mutableProjectComponent = new MutableProjectComponent(c.getId(), c.getName(), c.getDescription(), c.getLead(), c.getAssigneeType(), c.getProjectId());
        mutableProjectComponent.setGenericValue(c.getGenericValue());
        return mutableProjectComponent;
    }

    /**
     * Copy given collection of components
     *
     * @param c collection of components to copy
     * @return new instance collection of MutableProjectComponent objects set with values of the given components
     */
    public static Collection<MutableProjectComponent> copy(final Collection<ProjectComponent> c)
    {
        if (c != null && !c.isEmpty())
        {
            List copyList = new ArrayList(c.size());
            for (final Object aC : c)
            {
                copyList.add(MutableProjectComponent.copy((ProjectComponent) aC));
            }
            return copyList;
        }
        else
        {
            return Collections.EMPTY_LIST;
        }
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getDescription()
    {
        return description;
    }

    /**
     * Set description to give value. If the value is an empty string, it will be set to null
     * @param description description to set it to
     */
    public void setDescription(String description)
    {
        this.description = TextUtils.stringSet(description) ? description : null;
    }

    public String getLead()
    {
        return lead;
    }

    @Override
    public ApplicationUser getComponentLead()
    {
        return ComponentAccessor.getUserManager().getUserByKey(getLead());
    }

    public void setLead(String lead)
    {
        this.lead = lead;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Long getProjectId()
    {
        return projectId;
    }

    /**
     * Returns true if same object, instance of MutableProjectComponent and equal ID.
     *
     * @param o object to compare with
     * @return true if same object, instance of MutableProjectComponent and equal ID; false otherwise
     */
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || !(o instanceof MutableProjectComponent))
        {
            return false;
        }
        Long thatId = ((MutableProjectComponent) o).getId();
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

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("MutableProjectComponent { name='");
        sb.append(name);
        sb.append("', description='");
        sb.append(description == null ? "" : description);
        sb.append("'");
        sb.append(", lead='");
        sb.append(lead == null ? "" : lead);
        sb.append(", assigneeType='").append(assigneeType);
        sb.append("' }");
        return sb.toString();
    }

    public boolean equalsName(MutableProjectComponent component)
    {
        return getName().equalsIgnoreCase(component.getName());
    }

    public long getAssigneeType()
    {
        return this.assigneeType;
    }

    public void setAssigneeType(long assigneeType)
    {
        this.assigneeType = assigneeType;
    }

    void setGenericValue(GenericValue genericValue)
    {
        this.genericValue = genericValue;
    }

    public GenericValue getGenericValue()
    {
        return genericValue;
    }

}
