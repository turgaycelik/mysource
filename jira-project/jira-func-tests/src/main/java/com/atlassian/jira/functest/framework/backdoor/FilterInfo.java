package com.atlassian.jira.functest.framework.backdoor;

import java.util.ArrayList;
import java.util.List;

public class FilterInfo
{
    public Long id;
    public String name;
    public String description;
    public String owner;
    public Long favouriteCount;
    public Boolean favourite;
    public List<SharePermissionInfo> permissions = new ArrayList<SharePermissionInfo>();

    public FilterInfo() {}

    public FilterInfo(Long id, String name, String description, String owner, Boolean favourite, Long favouriteCount, List<SharePermissionInfo> permissions)
    {
        this.id = id;
        this.name = name;
        this.description = description;
        this.owner = owner;
        this.favourite = favourite;
        this.favouriteCount = favouriteCount;
        this.permissions = permissions;
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

        final FilterInfo that = (FilterInfo) o;

        if (favourite != that.favourite)
        {
            return false;
        }
        if (description != null ? !description.equals(that.description) : that.description != null)
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
        if (owner != null ? !owner.equals(that.owner) : that.owner != null)
        {
            return false;
        }
        if (permissions != null ? !permissions.equals(that.permissions) : that.permissions != null)
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
        result = 31 * result + (permissions != null ? permissions.hashCode() : 0);
        result = 31 * result + (owner != null ? owner.hashCode() : 0);
        result = 31 * result + (favouriteCount != null ? favouriteCount.hashCode() : 0);
        return result;
    }


}
