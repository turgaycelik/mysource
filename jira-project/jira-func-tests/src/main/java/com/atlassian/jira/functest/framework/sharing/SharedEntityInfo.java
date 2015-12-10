package com.atlassian.jira.functest.framework.sharing;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.Set;

/**
 * Represents the fundamental settings of a Shared Entity such as Search Filter or a Dashboard Page.
 *
 * @since v3.13
 */
public class SharedEntityInfo
{
    private Long id;
    private String name;
    private String description;
    private boolean favourite;
    private Set<? extends TestSharingPermission> sharingPermissions;
    private String owner;
    private Integer favCount;

    public SharedEntityInfo(final String name, final String description, final boolean favourite, final Set<? extends TestSharingPermission> sharingPermissions,
            String owner, Integer favCount)
    {
        this (null, name, description, favourite, sharingPermissions, owner, favCount);
    }

    public SharedEntityInfo(Long id, final String name, final String description, final boolean favourite, final Set<? extends TestSharingPermission> sharingPermissions,
            String owner, Integer favCount)
    {
        setId(id);
        setName(name);
        setDescription(description);
        setFavourite(favourite);
        setSharingPermissions(sharingPermissions);
        setOwner(owner);
        setFavCount(favCount);
    }

    public SharedEntityInfo(final String name, final String description, final boolean favourite, final Set<? extends TestSharingPermission> sharingPermissions)
    {
        this(null, name, description, favourite, sharingPermissions, null, null);
    }

    public SharedEntityInfo(final Long id, final String name, final String description, final boolean favourite, final Set<? extends TestSharingPermission> sharingPermissions)
    {
        this(id, name, description, favourite, sharingPermissions, null, null);
    }

    public SharedEntityInfo(final SharedEntityInfo sharedEntityInfo)
    {
        setId(sharedEntityInfo.getId());
        setName(sharedEntityInfo.getName());
        setDescription(sharedEntityInfo.getDescription());
        setFavourite(sharedEntityInfo.isFavourite());
        setSharingPermissions(sharedEntityInfo.getSharingPermissions());
        setOwner(sharedEntityInfo.getOwner());
        setFavCount(sharedEntityInfo.getFavCount());
    }

    public Long getId()
    {
        return id;
    }

    public SharedEntityInfo setId(final Long id)
    {
        this.id = id;
        return this;
    }

    public String getName()
    {
        return name;
    }

    public SharedEntityInfo setName(String name)
    {
        this.name = name == null ? "" : name;
        return this;
    }

    public String getDescription()
    {
        return description;
    }

    public SharedEntityInfo setDescription(String description)
    {
        this.description = description == null ? "" : description;
        return this;
    }

    public boolean isFavourite()
    {
        return favourite;
    }

    public SharedEntityInfo setFavourite(boolean favourite)
    {
        this.favourite = favourite;
        return this;
    }

    public Set<? extends TestSharingPermission> getSharingPermissions()
    {
        return sharingPermissions;
    }

    public SharedEntityInfo setSharingPermissions(final Set<? extends TestSharingPermission> sharingPermissions)
    {
        this.sharingPermissions = sharingPermissions;
        return this;
    }

    public String getOwner()
    {
        return owner;
    }

    public SharedEntityInfo setOwner(final String owner)
    {
        this.owner = owner;
        return this;
    }

    public Integer getFavCount()
    {
        return favCount;
    }

    public SharedEntityInfo setFavCount(Integer favCount)
    {
        this.favCount = favCount;
        return this;
    }

    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

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

        final SharedEntityInfo that = (SharedEntityInfo) o;

        if (favourite != that.favourite)
        {
            return false;
        }
        if (description != null ? !description.equals(that.description) : that.description != null)
        {
            return false;
        }
        if (favCount != null ? !favCount.equals(that.favCount) : that.favCount != null)
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
        if (owner != null ? !owner.equals(that.owner) : that.owner != null)
        {
            return false;
        }
        if (sharingPermissions != null ? !sharingPermissions.equals(that.sharingPermissions) : that.sharingPermissions != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (id != null ? id.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (favourite ? 1 : 0);
        result = 31 * result + (sharingPermissions != null ? sharingPermissions.hashCode() : 0);
        result = 31 * result + (owner != null ? owner.hashCode() : 0);
        result = 31 * result + (favCount != null ? favCount.hashCode() : 0);
        return result;
    }
}
