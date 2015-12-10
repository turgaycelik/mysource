package com.atlassian.jira.issue.fields.screen.issuetype;

import java.util.Collection;
import java.util.Map;

import javax.annotation.Nonnull;

import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.issuetype.IssueType;

import com.google.common.collect.Maps;

import org.ofbiz.core.entity.GenericValue;

/**
 * Simple implementation of a {@link IssueTypeScreenScheme} for tests.
 *
 * @since v6.2
 */
public class MockIssueTypeScreenScheme implements IssueTypeScreenScheme
{
    private long id;
    private String name;
    private String description;
    private Map<String, IssueTypeScreenSchemeEntity> issueTypeIdToEntity = Maps.newHashMap();

    @Override
    public Long getId()
    {
        return id;
    }

    public MockIssueTypeScreenScheme id(long id)
    {
        this.id = id;
        return this;
    }

    @Override
    public void setId(final Long id)
    {
        this.id = id;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public void setName(final String name)
    {
        this.name = name;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public void setDescription(final String description)
    {
        this.description = description;
    }

    @Override
    public GenericValue getGenericValue()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void setGenericValue(final GenericValue genericValue)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void store()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Collection<IssueTypeScreenSchemeEntity> getEntities()
    {
        return issueTypeIdToEntity.values();
    }

    @Override
    public IssueTypeScreenSchemeEntity getEntity(final String issueTypeId)
    {
        return issueTypeIdToEntity.get(issueTypeId);
    }

    @Nonnull
    @Override
    public FieldScreenScheme getEffectiveFieldScreenScheme(@Nonnull final IssueType type)
    {
        IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity = getEntity(type.getId());
        if (issueTypeScreenSchemeEntity == null)
        {
            issueTypeScreenSchemeEntity = getDefaultEntity();
        }
        return issueTypeScreenSchemeEntity.getFieldScreenScheme();
    }

    public IssueTypeScreenSchemeEntity getDefaultEntity() {return getEntity(null);}

    @Override
    public void addEntity(final IssueTypeScreenSchemeEntity entity)
    {
        issueTypeIdToEntity.put(entity.getIssueTypeId(), entity);
    }

    @Override
    public void removeEntity(final String issueTypeId)
    {
        issueTypeIdToEntity.remove(issueTypeId);
    }

    @Override
    public boolean containsEntity(final String issueTypeId)
    {
        return issueTypeIdToEntity.containsKey(issueTypeId);
    }

    @Override
    public Collection<GenericValue> getProjects()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isDefault()
    {
        return IssueTypeScreenScheme.DEFAULT_SCHEME_ID.equals(getId());
    }

    private long getNextEntityId()
    {
        long maxId = -1;
        for (IssueTypeScreenSchemeEntity entity : issueTypeIdToEntity.values())
        {
            if (entity.getId() != null)
            {
                maxId = Math.max(entity.getId() & 0xFFFFFFFFL, maxId);
            }
        }
        return id << 32 | (maxId + 1);
    }

    public MockIssueTypeScreenSchemeEntity createEntity(IssueType type)
    {
        final MockIssueTypeScreenSchemeEntity entity = new MockIssueTypeScreenSchemeEntity()
                .id(getNextEntityId())
                .issueType(type)
                .issueTypeScreenScheme(this);
        addEntity(entity);
        return entity;
    }

    public MockIssueTypeScreenSchemeEntity createDefaultEntity()
    {
        return createEntity(null);
    }
}
