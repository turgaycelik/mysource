package com.atlassian.jira.mock.project;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.Date;

public class MockVersion implements Version
{
    private GenericValue genericValue;
    private Date startDate;
    private Date releaseDate;
    private Project project;
    private Long sequence;

    private boolean released=false;
    private boolean archived=false;

    public MockVersion()
    {
        this(1, "test");
    }

    public MockVersion(GenericValue genericValue)
    {
        this.genericValue = genericValue;
    }

    public MockVersion(final long id, final String name)
    {
        this(id, name, null, null);
    }

    public MockVersion(final long id, final String name, final Project project)
    {
        this(id, name, project, null);
    }

    public MockVersion(final long id, final String name, final Project project, Long sequence)
    {
        this.genericValue = new MockGenericValue("Version", EasyMap.build("id", new Long(id), "name", name, "sequence", sequence));
        this.sequence = sequence;
        this.project = project;
    }

    public GenericValue getProject()
    {
        return null;
    }

    @Override
    public Long getProjectId()
    {
        if (project == null)
            return null;
        return project.getId();
    }

    public Project getProjectObject()
    {
        return project;
    }

    public MockVersion setProjectObject(Project project)
    {
        this.project = project;
        return this;
    }

    public Long getId()
    {
        return genericValue.getLong("id");
    }

    public void setId(Long id)
    {
        genericValue.set("id", id);
    }

    public String getName()
    {
        return genericValue.getString("name");
    }

    public void setName(final String name)
    {
        genericValue.setString("name", name);
    }

    public String getDescription()
    {
        return null;
    }

    public void setDescription(String description)
    {

    }

    public Long getSequence()
    {
        return sequence;
    }

    public void setSequence(Long sequence)
    {
        this.sequence = sequence;
        genericValue.set("sequence", sequence);
    }

    public boolean isArchived()
    {
        return archived;
    }

    public void setArchived(boolean archived)
    {
        this.archived = archived;
    }

    public boolean isReleased()
    {
        return released;
    }

    public void setReleased(boolean released)
    {
        this.released = released;
    }

    public Date getStartDate()
    {
        return startDate;
    }

    public void setStartDate(Date startDate)
    {
        this.startDate = startDate;
    }

    @Override
    public Version clone()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Date getReleaseDate()
    {
        return releaseDate;
    }

    public void setReleaseDate(Date releasedate)
    {
        releaseDate = releasedate;
    }

    public String getString(String name)
    {
        return null;
    }

    public Timestamp getTimestamp(String name)
    {
        return null;
    }

    public Long getLong(String name)
    {
        return null;
    }

    public GenericValue getGenericValue()
    {
        return genericValue;
    }

    public void store()
    {
        
    }

    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}
