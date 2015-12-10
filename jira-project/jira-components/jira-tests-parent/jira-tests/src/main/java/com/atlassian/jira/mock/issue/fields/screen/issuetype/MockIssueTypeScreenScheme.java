package com.atlassian.jira.mock.issue.fields.screen.issuetype;

import java.util.Collection;
import java.util.Map;

import javax.annotation.Nonnull;

import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntity;
import com.atlassian.jira.issue.issuetype.IssueType;

import com.google.common.collect.Maps;

import org.ofbiz.core.entity.GenericValue;

/**
 * Mock for {@link IssueTypeScreenScheme} that only compares equality based on
 * id and name. Throws {@link UnsupportedOperationException} for {@link #store()}
 * and {@link #remove()}.
 *
 * @since v4.4
 */
public class MockIssueTypeScreenScheme implements IssueTypeScreenScheme
{

    private Long id;
    private String name;
    private String description;
    private GenericValue genericValue;
    private Map<String, IssueTypeScreenSchemeEntity> entities = Maps.newHashMap();
    private boolean isDefaultEntity;
    private Collection<GenericValue> projects;

    public MockIssueTypeScreenScheme(final Long id, final String name, final String description)
    {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
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

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public GenericValue getGenericValue()
    {
        return genericValue;
    }

    public void setGenericValue(GenericValue genericValue)
    {
        this.genericValue = genericValue;
    }

    public void store()
    {
        throw new UnsupportedOperationException();
    }

    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    public Collection getEntities()
    {
        return entities.values();
    }

    public IssueTypeScreenSchemeEntity getEntity(String issueTypeId)
    {
        return entities.get(issueTypeId);
    }

    @Nonnull
    @Override
    public FieldScreenScheme getEffectiveFieldScreenScheme(@Nonnull final IssueType type)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void addEntity(IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity)
    {
        entities.put(issueTypeScreenSchemeEntity.getIssueTypeId(), issueTypeScreenSchemeEntity);
    }

    public MockIssueTypeScreenScheme setEntities(Map<String, IssueTypeScreenSchemeEntity> entities)
    {
        this.entities = entities;
        return this;
    }

    public void removeEntity(String issueTypeId)
    {
        entities.remove(issueTypeId);
    }

    public boolean containsEntity(String issueTypeId)
    {
        return entities.containsKey(issueTypeId);
    }

    public Collection getProjects()
    {
        return projects;
    }

    public MockIssueTypeScreenScheme setProjects(Collection<GenericValue> projects)
    {
        this.projects = projects;
        return this;
    }

    public boolean isDefault()
    {
        return isDefaultEntity;
    }

    public MockIssueTypeScreenScheme setDefault(boolean isDefaultEntity)
    {
        this.isDefaultEntity = isDefaultEntity;
        return this;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        MockIssueTypeScreenScheme that = (MockIssueTypeScreenScheme) o;

        if (id != null ? !id.equals(that.id) : that.id != null) { return false; }
        if (name != null ? !name.equals(that.name) : that.name != null) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
