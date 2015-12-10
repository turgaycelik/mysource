package com.atlassian.jira.sharing.search;

import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.SharedEntityColumn;
import com.atlassian.jira.sharing.type.GroupSharePermission;
import com.atlassian.jira.sharing.type.ProjectSharePermission;
import com.atlassian.jira.sharing.type.ShareType.Name;
import com.atlassian.jira.util.dbc.Assertions;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * A builder object used to create {@link SharedEntitySearchParameters searching parameters} when looking for SharedEntityColumn instances.
 * <p>
 * This has a {@link #toSearchParameters()} method that gives back a new immutable instance of SharedEntitySearchParameters when called.
 *
 * @see SharedEntitySearchParameters
 * @since v3.13
 */
public class SharedEntitySearchParametersBuilder
{
    private String name = null;
    private String description = null;
    private String user = null;
    private Boolean favourite = null;
    private SharedEntityColumn sortColumn = SharedEntityColumn.NAME;
    private boolean sortOrderAscending = true;
    private ShareTypeSearchParameter shareTypeParameter = null;
    //JRA-19918 : Text Search used to ignore OR, now has OR and AND, or is a more sensible default
    private SharedEntitySearchParameters.TextSearchMode textSearchMode = SharedEntitySearchParameters.TextSearchMode.OR;

    private SharedEntitySearchContext entitySearchContext = SharedEntitySearchContext.USE;

    /**
     * Creates a new template with empty parameters. This will find the most data possible in this state
     *
     * @see SharedEntitySearchParameters
     */
    public SharedEntitySearchParametersBuilder()
    {}

    /**
     * Creates a new template based on the parameters contained within an existing SharedEntitySearchParameters instance
     *
     * @param sharedEntitySearchParameters the existing SharedEntitySearchParameters object. Must be non null.
     * @throws IllegalArgumentException if the sharedEntitySearchParameters is null.
     * @see SharedEntitySearchParameters
     */
    public SharedEntitySearchParametersBuilder(final SharedEntitySearchParameters sharedEntitySearchParameters)
    {
        Assertions.notNull("sharedEntitySearchParameters", sharedEntitySearchParameters);

        setName(sharedEntitySearchParameters.getName());
        setDescription(sharedEntitySearchParameters.getDescription());
        setFavourite(sharedEntitySearchParameters.getFavourite());
        setShareTypeParameter(sharedEntitySearchParameters.getShareTypeParameter());
        setSortColumn(sharedEntitySearchParameters.getSortColumn(), sharedEntitySearchParameters.isAscendingSort());
        setUserName(sharedEntitySearchParameters.getUserName());
        setTextSearchMode(sharedEntitySearchParameters.getTextSearchMode());
    }

    public SharedEntitySearchParametersBuilder setName(final String name)
    {
        this.name = name;
        return this;
    }

    public SharedEntitySearchParametersBuilder setDescription(final String description)
    {
        this.description = description;
        return this;
    }

    public SharedEntitySearchParametersBuilder setUserName(final String user)
    {
        this.user = user;
        return this;
    }

    public SharedEntitySearchParametersBuilder setFavourite(final Boolean favourite)
    {
        this.favourite = favourite;
        return this;
    }

    public SharedEntitySearchParametersBuilder setSortColumn(final SharedEntityColumn column, final boolean sortOrderAscending)
    {
        sortColumn = column;
        this.sortOrderAscending = sortOrderAscending;
        return this;
    }

    /**
     * Sets the Share Type parameter, used to search for shared entities that are shared based
     * <p/>
     * The following classes and singleton instances can be used <ul> <li>{@link com.atlassian.jira.sharing.search.GlobalShareTypeSearchParameter#GLOBAL_PARAMETER}
     * <li>{@link com.atlassian.jira.sharing.search.GroupShareTypeSearchParameter} <li>{@link
     * com.atlassian.jira.sharing.search.ProjectShareTypeSearchParameter} <li>{@link
     * PrivateShareTypeSearchParameter#PRIVATE_PARAMETER} </ul>
     *
     * @param shareTypeParameter the ShareTypeSearchParameter
     * @return this Builder
     */
    public SharedEntitySearchParametersBuilder setShareTypeParameter(final ShareTypeSearchParameter shareTypeParameter)
    {
        this.shareTypeParameter = shareTypeParameter;
        return this;
    }

    public SharedEntitySearchParametersBuilder setTextSearchMode(final SharedEntitySearchParameters.TextSearchMode textSearchMode)
    {
        this.textSearchMode = textSearchMode;
        return this;
    }

    public SharedEntitySearchParametersBuilder setEntitySearchContext(SharedEntitySearchContext entitySearchContext)
    {
        this.entitySearchContext = entitySearchContext;
        return this;
    }

    public SharedEntitySearchParametersBuilder setSharePermission(final SharePermission permission)
    {
        setShareTypeParameter(getShareTypeParameter(permission));
        return this;
    }

    private ShareTypeSearchParameter getShareTypeParameter(final SharePermission permission)
    {
        final Name type = permission.getType();
        if (Name.GLOBAL.equals(type))
        {
            return GlobalShareTypeSearchParameter.GLOBAL_PARAMETER;
        }
        if (Name.PROJECT.equals(type))
        {
            return new ProjectSharePermission(permission).getSearchParameter();
        }
        if (Name.GROUP.equals(type))
        {
            return new GroupSharePermission(permission).getSearchParameter();
        }
        throw new UnsupportedOperationException("Cannot create a Search Parameter for: " + type);
    }

    /**
     * Call this method to clone the current SharedEntitySearchParametersTemplate into an immutable SharedEntitySearchParameters object. This
     * immutable object can then safely be shared between threads and placed into the Session for example.
     *
     * @return an im0mutable SharedEntitySearchParameters object.
     */
    public SharedEntitySearchParameters toSearchParameters()
    {
        return new SearchParametersImpl(this);
    }

    // /CLOVER:OFF
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

    ///CLOVER:ON

    private static class SearchParametersImpl implements SharedEntitySearchParameters
    {
        private final String name;
        private final String description;
        private final String userName;
        private final SharedEntityColumn sortColumn;
        private final Boolean favourite;
        private final boolean sortAscending;
        private final ShareTypeSearchParameter shareTypeParameter;
        private final TextSearchMode textSearchMode;
        private final SharedEntitySearchContext entitySearchContext;

        private SearchParametersImpl(final SharedEntitySearchParametersBuilder builder)
        {
            name = builder.name;
            description = builder.description;
            userName = builder.user;
            sortColumn = builder.sortColumn;
            favourite = builder.favourite;
            sortAscending = builder.sortOrderAscending;
            shareTypeParameter = builder.shareTypeParameter;
            textSearchMode = builder.textSearchMode;
            entitySearchContext = builder.entitySearchContext;
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
        public String getUserName()
        {
            return userName;
        }

        public Boolean getFavourite()
        {
            return favourite;
        }

        public boolean isAscendingSort()
        {
            return sortAscending;
        }

        public SharedEntityColumn getSortColumn()
        {
            return sortColumn;
        }

        public ShareTypeSearchParameter getShareTypeParameter()
        {
            return shareTypeParameter;
        }

        @Override
        public SharedEntitySearchContext getEntitySearchContext()
        {
            return entitySearchContext;
        }

        public TextSearchMode getTextSearchMode()
        {
            return textSearchMode;
        }

        // /CLOVER:OFF
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
        ///CLOVER:ON

        // /CLOVER:OFF
        @SuppressWarnings ( { "RedundantIfStatement" })
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

            final SearchParametersImpl that = (SearchParametersImpl) o;

            if (sortAscending != that.sortAscending)
            {
                return false;
            }
            if (description != null ? !description.equals(that.description) : that.description != null)
            {
                return false;
            }
            if (favourite != null ? !favourite.equals(that.favourite) : that.favourite != null)
            {
                return false;
            }
            if (name != null ? !name.equals(that.name) : that.name != null)
            {
                return false;
            }
            if (shareTypeParameter != null ? !shareTypeParameter.equals(that.shareTypeParameter) : that.shareTypeParameter != null)
            {
                return false;
            }
            if (sortColumn != null ? !sortColumn.equals(that.sortColumn) : that.sortColumn != null)
            {
                return false;
            }
            if (textSearchMode != null ? !textSearchMode.equals(that.textSearchMode) : that.textSearchMode != null)
            {
                return false;
            }
            if (userName != null ? !userName.equals(that.userName) : that.userName != null)
            {
                return false;
            }

            return true;
        }

        // /CLOVER:OFF
        @Override
        public int hashCode()
        {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (description != null ? description.hashCode() : 0);
            result = 31 * result + (userName != null ? userName.hashCode() : 0);
            result = 31 * result + (sortColumn != null ? sortColumn.hashCode() : 0);
            result = 31 * result + (favourite != null ? favourite.hashCode() : 0);
            result = 31 * result + (sortAscending ? 1 : 0);
            result = 31 * result + (shareTypeParameter != null ? shareTypeParameter.hashCode() : 0);
            result = 31 * result + (textSearchMode != null ? textSearchMode.hashCode() : 0);
            return result;
        }
        ///CLOVER:ON
    }
}
