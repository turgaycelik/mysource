package com.atlassian.jira.functest.framework.parser.dashboard;

import com.atlassian.jira.functest.framework.parser.SharedEntityItem;
import com.atlassian.jira.functest.framework.parser.filter.WebTestSharePermission;
import com.google.common.collect.Lists;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @since v4.4
 */
public class DashboardItem implements Cloneable, SharedEntityItem
{
    private long id;
    private String name;
    private String description;
    private String author;
    private long issues;
    private List sharing;
    private boolean fav;
    private long subscriptions;
    private List<String> operations = new ArrayList<String>();
    private long favCount;

    protected DashboardItem(final long id, final String name, final String description, final String author, final long issues,
            final List<WebTestSharePermission> sharing, final Boolean fav, final long subscriptions,
            final List<String> operations, final long favCount)
    {
        this.id = id;
        this.name = name;
        this.description = description;
        this.author = author;
        this.issues = issues;
        this.sharing = sharing;
        this.fav = fav;
        this.subscriptions = subscriptions;
        if (operations != null)
        {
            this.operations = operations;
        }
        this.favCount = favCount;
    }

    public DashboardItem(DashboardItem copyFrom)
    {
        this(copyFrom.id,
            copyFrom.name,
            copyFrom.description,
            copyFrom.author,
            copyFrom.issues,
            copyFrom.sharing,
            copyFrom.fav,
            copyFrom.subscriptions,
            copyFrom.operations,
            copyFrom.favCount);
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public String getAuthor()
    {
        return author;
    }

    public Long getIssues()
    {
        return issues;
    }

    public List getSharing()
    {
        return sharing;
    }

    public Boolean isFav()
    {
        return fav;
    }

    public Long getSubscriptions()
    {
        return subscriptions;
    }

    public List getOperations()
    {
        return operations;
    }

    public Long getFavCount()
    {
        return favCount;
    }

    public Long getId()
    {
        return id;
    }


    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj) { return true; }

        if (!(obj instanceof DashboardItem)) { return false; }

        DashboardItem rhs = (DashboardItem) obj;

        return new EqualsBuilder().
                append(author, rhs.author).
                append(description, rhs.description).
                append(fav, rhs.fav).
                append(favCount, rhs.favCount).
                append(id, rhs.id).
                append(issues, rhs.issues).
                append(name, rhs.name).
                append(operations, rhs.operations).
                append(sharing, rhs.sharing).
                append(subscriptions,rhs.subscriptions).
                isEquals();

    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 31).
                append(id).
                append(name).
                append(description).
                append(author).
                append(issues).
                append(sharing).
                append(fav).
                append(subscriptions).
                append(operations).
                append(favCount).
                toHashCode();
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).
                append("id", id).
                append("name", name).
                append("author", author).
                append("fav", fav).
                append("favCount", favCount).
                append("description", description).
                append("issues", issues).
                append("sharing", sharing).
                append("subscriptions", subscriptions).
                append("operations", operations).
                toString();
    }

    public DashboardItem cloneFilter()
    {
        try
        {
            return (DashboardItem) clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public static class Builder
    {
    
        private long id;
        private String name="";
        private String owner="";
        private boolean isFavourite = false;
        private int favouriteCount;
        private int issueCount;
        private String description = "";
        private int subscriptionCount;
        private List<String> availableOperations = Lists.newArrayList();
        private List<WebTestSharePermission> sharePermissions = Lists.newArrayList();
    
        public Builder id(final long id)
        {
            this.id = id;
            return this;
        }
    
        public Builder name(final String filterName)
        {
            name = filterName;
            return this;
        }
    
        public Builder description(final String description)
        {
            this.description = description;
            return this;
        }
    
        public Builder owner(final String owner)
        {
            this.owner = owner;
            return this;
        }
    
        public Builder asFavourite()
        {
            isFavourite = true;
            return this;
        }
    
        public Builder favouriteCount(final int favouriteCount)
        {
            this.favouriteCount = favouriteCount;
            return this;
        }
    
        public Builder sharedWith(final List<WebTestSharePermission> sharePermissions)
        {
            this.sharePermissions = sharePermissions;
            return this;
        }
    
        public Builder issueCount(final int issueCount)
        {
            this.issueCount = issueCount;
            return this;
        }
    
        public Builder subscriptionCount(final int subscriptionCount)
        {
            this.subscriptionCount = subscriptionCount;
            return this;
        }
    
        public Builder availableOperations(final List<String> availableOperations)
        {
            this.availableOperations = availableOperations;
            return this;
        }
    
        public DashboardItem build()
        {
            return new DashboardItem(id, name, description, owner, issueCount, sharePermissions, isFavourite,
                    subscriptionCount, availableOperations, favouriteCount);
        }
    }
}

