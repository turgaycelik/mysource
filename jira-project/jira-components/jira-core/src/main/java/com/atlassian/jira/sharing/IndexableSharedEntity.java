package com.atlassian.jira.sharing;

import com.atlassian.jira.user.ApplicationUser;

/**
 * Represents a {@link com.atlassian.jira.sharing.SharedEntity} in a lightweight way that can be used for indexing.
 *
 * Like {@link com.atlassian.jira.issue.search.SearchRequest}s, you can set the share permissions after construction
 * because they might not always be available at that time.
 *
 * @since v4.0
 */
public class IndexableSharedEntity<S extends SharedEntity> implements SharedEntity
{
    private final Long id;
    private final String name;
    private final String description;
    private final TypeDescriptor<S> entityType;
    private final ApplicationUser owner;
    private final Long favouriteCount;
    private SharePermissions permissions;

    public IndexableSharedEntity(final Long id, final String name, final String description, final TypeDescriptor<S> entityType, final ApplicationUser owner, final Long favouriteCount)
    {
        this(id, name, description, entityType, owner, favouriteCount, null);
    }

    public IndexableSharedEntity(final Long id, final String name, final String description, final TypeDescriptor<S> entityType, final ApplicationUser owner, final Long favouriteCount, final SharePermissions permissions)
    {
        this.id = id;
        this.name = name;
        this.description = description;
        this.entityType = entityType;
        this.owner = owner;
        this.favouriteCount = favouriteCount;
        this.permissions = permissions;
    }

    public Long getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    @Override
    public ApplicationUser getOwner()
    {
        return owner;
    }

    @Override
    public String getOwnerUserName()
    {
        return owner == null ? null : owner.getUsername();
    }

    @SuppressWarnings("unchecked")
    public TypeDescriptor<S> getEntityType()
    {
        return entityType;
    }

    public SharePermissions getPermissions()
    {
        return permissions;
    }

    public void setPermissions(final SharePermissions permissions)
    {
        this.permissions = permissions;
    }

    public Long getFavouriteCount()
    {
        return favouriteCount;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final IndexableSharedEntity that = (IndexableSharedEntity) o;

        if (description != null ? !description.equals(that.description) : that.description != null)
        {
            return false;
        }
        if (entityType != null ? !entityType.equals(that.entityType) : that.entityType != null)
        {
            return false;
        }
        if (favouriteCount != null ? !favouriteCount.equals(that.favouriteCount) : that.favouriteCount != null)
        {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null)
        {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null)
        {
            return false;
        }
        if (owner != null ? !owner.equals(that.getOwner()) : that.getOwner() != null)
        {
            return false;
        }
        if (permissions != null ? !permissions.equals(that.permissions) : that.permissions != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (entityType != null ? entityType.hashCode() : 0);
        result = 31 * result + (owner != null ? owner.hashCode() : 0);
        result = 31 * result + (permissions != null ? permissions.hashCode() : 0);
        result = 31 * result + (favouriteCount != null ? favouriteCount.hashCode() : 0);
        return result;
    }
}
