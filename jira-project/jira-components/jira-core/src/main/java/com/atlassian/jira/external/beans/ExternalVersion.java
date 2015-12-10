package com.atlassian.jira.external.beans;

import com.atlassian.jira.issue.IssueFieldConstants;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Date;

public class ExternalVersion implements NamedExternalObject, Comparable
{
    // @TODO refactor Remote RPC objects to use this

    public static final String AFFECTED_VERSION_PREFIX = IssueFieldConstants.AFFECTED_VERSIONS;
    public static final String FIXED_VERSION_PREFIX = IssueFieldConstants.FIX_FOR_VERSIONS;

    String id;
    String projectId;
    String name;
    boolean released;
    boolean archived;
    private Long sequence;
    private Date releaseDate;
    private String description;

    public ExternalVersion()
    {
    }

    public ExternalVersion(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getProjectId()
    {
        return projectId;
    }

    public void setProjectId(final String projectId)
    {
        this.projectId = projectId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public boolean isReleased()
    {
        return released;
    }

    public void setReleased(boolean released)
    {
        this.released = released;
    }

    public boolean isArchived()
    {
        return archived;
    }

    public void setArchived(boolean archived)
    {
        this.archived = archived;
    }

    public Long getSequence()
    {
        return sequence;
    }

    public void setSequence(Long sequence)
    {
        this.sequence = sequence;
    }

    public Date getReleaseDate()
    {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate)
    {
        this.releaseDate = releaseDate;
    }

    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public boolean equals(Object o)
    {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    public int compareTo(Object o)
    {
        ExternalVersion rhs = (ExternalVersion) o;
        return new CompareToBuilder()
                .append(this.getName(), rhs.getName())
                .toComparison();
    }

    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}
