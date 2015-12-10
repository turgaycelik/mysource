/*
 * Copyright (c) 2002-2006
 * All rights reserved.
 */

package com.atlassian.jira.project.version;

import com.atlassian.jira.ofbiz.AbstractOfBizValueWrapper;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import org.ofbiz.core.entity.GenericValue;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.util.Date;

public class VersionImpl extends AbstractOfBizValueWrapper implements Version, Comparable
{
    private final ProjectManager projectManager;
    private final Long projectId;
    private Project project = null;

    public VersionImpl(ProjectManager projectManager, GenericValue genericValue)
    {
        super(genericValue);
        this.projectManager = projectManager;
        projectId = genericValue.getLong("project");
    }

    @Override
    public GenericValue getProject()
    {
        return getProjectObject().getGenericValue();
    }

    @Override
    public Long getProjectId()
    {
        return projectId;
    }

    @Override
    public Project getProjectObject()
    {
        if (project == null)
        {
            project = projectManager.getProjectObj(projectId);
        }
        return project;
    }

    @Override
    public Long getId()
    {
        return genericValue.getLong("id");
    }

    @Override
    public String getName()
    {
        return genericValue.getString("name");
    }

    @Override
    public void setName(String name)
    {
        genericValue.setString("name", name);
    }

    @Nullable
    @Override
    public String getDescription()
    {
        return genericValue.getString("description");
    }

    @Override
    public void setDescription(@Nullable String description)
    {
        genericValue.setString("description", description);
    }

    @Override
    public Long getSequence()
    {
        return genericValue.getLong("sequence");
    }

    @Override
    public void setSequence(Long sequence)
    {
        genericValue.set("sequence", sequence);
    }

    @Override
    public boolean isArchived()
    {
        return "true".equals(genericValue.getString("archived"));
    }

    @Override
    public void setArchived(boolean archived)
    {
        genericValue.set("archived", archived ? "true" : null);
    }

    @Override
    public boolean isReleased()
    {
        return "true".equals(genericValue.getString("released"));
    }

    @Override
    public void setReleased(boolean released)
    {
        genericValue.set("released", released ? "true" : null);
    }

    @Nullable
    @Override
    public Date getReleaseDate()
    {
        return genericValue.getTimestamp("releasedate");
    }

    @Override
    public void setReleaseDate(@Nullable Date releasedate)
    {
        if (releasedate == null)
        {
            genericValue.set("releasedate", null);
        }
        else
        {
            genericValue.set("releasedate", new Timestamp(releasedate.getTime()));
        }
    }

    @Nullable
    @Override
    public Date getStartDate()
    {
        return genericValue.getTimestamp("startdate");
    }

    @Override
    public void setStartDate(@Nullable Date startDate)
    {
        if (startDate == null)
        {
            genericValue.set("startdate", null);
        }
        else
        {
            genericValue.set("startdate", new Timestamp(startDate.getTime()));
        }
    }

    @Override
    public Version clone()
    {
        return new VersionImpl(projectManager, (GenericValue) genericValue.clone());
    }

    @Override
    public int compareTo(Object o)
    {
        return getGenericValue().compareTo(((VersionImpl) o).getGenericValue());
    }

    @Override
    public String toString()
    {
        return getName();
    }
}
