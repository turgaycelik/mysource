package com.atlassian.jira.functest.framework.parser.filter;

import com.atlassian.jira.functest.framework.parser.SharedEntityItem;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A filter Item
 *
 * @since v3.13
 */
public class FilterItem implements Cloneable, SharedEntityItem
{
    private long id;
    private String name;
    private String description;
    private String author;
    private List<WebTestSharePermission> sharing;
    private boolean fav;
    private long subscriptions;
    private List<String> operations = new ArrayList<String>();
    private long favCount;

    // TODO: Replace this with a proper builder object and make this value object immutable


    /**
     * @deprecated JRADEV-14514 - The issues column has been removed from the filter list
     * @see #FilterItem(long, String, String, String, List, Boolean, long, List, long)
     */
    @Deprecated
    public FilterItem(final long id, final String name, final String description, final String author, final long issues,
            final List<WebTestSharePermission> sharing, final Boolean fav, final long subscriptions,
            final List<String> operations, final long favCount)
    {
        this(id, name, description, author, sharing, fav, subscriptions, operations, favCount);
    }

    public FilterItem(final long id, final String name, final String description, final String author,
            final List<WebTestSharePermission> sharing, final Boolean fav, final long subscriptions,
            final List<String> operations, final long favCount)
    {
        this.id = id;
        this.name = name;
        this.description = description;
        this.author = author;
        this.sharing = sharing;
        this.fav = fav;
        this.subscriptions = subscriptions;
        if (operations != null)
        {
            this.operations = operations;
        }
        this.favCount = favCount;
    }

    public FilterItem(FilterItem copyFrom)
    {
        this(copyFrom.id,
            copyFrom.name,
            copyFrom.description,
            copyFrom.author,
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

    /**
     * @return {@code 0L}
     * @deprecated JRADEV-14514 - The issues column has been removed from the filter list, so this
     *      always returns {@code 0L}, now.
     */
    public Long getIssues()
    {
        return 0L;
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

    public FilterItem setName(final String name)
    {
        this.name = name;
        return this;
    }

    public FilterItem setDescription(final String description)
    {
        this.description = description;
        return this;
    }

    public FilterItem setAuthor(final String author)
    {
        this.author = author;
        return this;
    }

    /**
     * @param issues ignored
     * @return {@code this}
     * @deprecated JRADEV-14514 - The issues column has been removed from the filter list
     */
    @Deprecated
    public FilterItem setIssues(final long issues)
    {
        return this;
    }

    public FilterItem setSharing(final List sharing)
    {
        this.sharing = sharing;
        return this;
    }

    public FilterItem setFav(final boolean fav)
    {
        this.fav = fav;
        return this;
    }

    public FilterItem setSubscriptions(final long subscriptions)
    {
        this.subscriptions = subscriptions;
        return this;
    }

    public FilterItem setOperations(final List operations)
    {
        this.operations = operations;
        return this;
    }

    public FilterItem setNoOperations()
    {
        return setOperations(Collections.EMPTY_LIST);
    }

    public FilterItem setFavCount(final long favCount)
    {
        this.favCount = favCount;
        return this;
    }

    public FilterItem setNoFavCount()
    {
        return setFavCount(0);
    }

    public FilterItem setNoAuthor()
    {
        return setAuthor(null);
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj) { return true; }

        if (!(obj instanceof FilterItem)) { return false; }

        FilterItem rhs = (FilterItem) obj;

        return new EqualsBuilder().
                append(author, rhs.author).
                append(description, rhs.description).
                append(fav, rhs.fav).
                append(favCount, rhs.favCount).
                append(id, rhs.id).
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
                append("sharing", sharing).
                append("subscriptions", subscriptions).
                append("operations", operations).
                toString();
    }

    public FilterItem cloneFilter()
    {
        try
        {
            return (FilterItem) clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static class Builder
    {
        private long id;
        private String name;
        private String owner;
        private boolean isFavourite = false;
        private int favouriteCount;
        private String description = "";
        private int subscriptionCount;
        private List<String> availableOperations;
        private List<WebTestSharePermission> sharePermissions;

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

        /**
         * @param issueCount ignored
         * @return {@code this}
         * @deprecated JRADEV-14514 - The issues column has been removed from the filter list
         */
        @Deprecated
        public Builder issueCount(final int issueCount)
        {
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

        public FilterItem build()
        {
            return new FilterItem(id, name, description, owner, sharePermissions, isFavourite,
                    subscriptionCount, availableOperations, favouriteCount);
        }
    }
}
