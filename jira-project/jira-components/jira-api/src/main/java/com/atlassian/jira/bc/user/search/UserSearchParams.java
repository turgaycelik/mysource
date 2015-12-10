package com.atlassian.jira.bc.user.search;

import java.util.Collection;
import java.util.Set;

import com.atlassian.jira.user.UserFilter;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Optional parameters to restrict a user search.
 *
 * This allows you to include or exclude active and inactive users and allow or disallow empty search queries.
 *
 * @since v5.1.5
 */
public class UserSearchParams
{
    public static final UserSearchParams ACTIVE_USERS_IGNORE_EMPTY_QUERY = new UserSearchParams(false, true, false, false, null, null);
    public static final UserSearchParams ACTIVE_USERS_ALLOW_EMPTY_QUERY = new UserSearchParams(true, true, false, false, null, null);

    private final boolean allowEmptyQuery;
    private final boolean includeActive;
    private final boolean includeInactive;
    /**
     * Indicate whether the search would apply the query to email address as well.
     * @since v6.2
     */
    private final boolean canMatchEmail;
    /**
     * The additional filters to be applied to the search.
     * @since v6.2
     */
    private final UserFilter userFilter;
    /**
     * The list of project ids to be used in conjunction with the roles in {@link #userFilter}.
     * @since v6.2
     */
    private final Set<Long> projectIds;

    public UserSearchParams(boolean allowEmptyQuery, boolean includeActive, boolean includeInactive)
    {
        this(allowEmptyQuery, includeActive, includeInactive, false, null, null);
    }

    public UserSearchParams(boolean allowEmptyQuery, boolean includeActive, boolean includeInactive,
            boolean canMatchEmail,
            final UserFilter userFilter, final Set<Long> projectIds)
    {
        this.allowEmptyQuery = allowEmptyQuery;
        this.includeActive = includeActive;
        this.includeInactive = includeInactive;
        this.canMatchEmail = canMatchEmail;
        this.userFilter = userFilter;
        this.projectIds = projectIds;
    }

    public boolean allowEmptyQuery()
    {
        return allowEmptyQuery;
    }

    public boolean includeActive()
    {
        return includeActive;
    }

    public boolean includeInactive()
    {
        return includeInactive;
    }

    public boolean canMatchEmail()
    {
        return canMatchEmail;
    }

    public UserFilter getUserFilter()
    {
        return userFilter;
    }

    public Set<Long> getProjectIds()
    {
        return projectIds;
    }

    @Override
    public boolean equals(final Object o)
    {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static Builder builder(UserSearchParams prototype)
    {
        return new Builder(prototype);
    }

    public static class Builder
    {
        private boolean allowEmptyQuery = false;
        private boolean includeActive = true;
        private boolean includeInactive = false;
        private boolean canMatchEmail = false;
        private UserFilter userFilter = null;
        private Set<Long> projectIds = null;

        public Builder()
        {
        }

        private Builder(UserSearchParams prototype)
        {
            this.allowEmptyQuery = prototype.allowEmptyQuery;
            this.includeActive = prototype.includeActive;
            this.includeInactive = prototype.includeInactive;
            this.canMatchEmail = prototype.canMatchEmail;
            this.userFilter = prototype.userFilter;
            this.projectIds = prototype.projectIds;
        }

        public UserSearchParams build()
        {
            return new UserSearchParams(allowEmptyQuery, includeActive, includeInactive, canMatchEmail, userFilter, projectIds);
        }

        public Builder allowEmptyQuery(boolean allowEmptyQuery)
        {
            this.allowEmptyQuery = allowEmptyQuery;
            return this;
        }

        public Builder includeActive(boolean includeActive)
        {
            this.includeActive = includeActive;
            return this;
        }

        public Builder includeInactive(boolean includeInactive)
        {
            this.includeInactive = includeInactive;
            return this;
        }

        public Builder canMatchEmail(boolean canMatchEmail)
        {
            this.canMatchEmail = canMatchEmail;
            return this;
        }

        public Builder filter(UserFilter userFilter)
        {
            this.userFilter = userFilter;
            return this;
        }

        public Builder filterByProjectIds(Collection<Long> projectIds)
        {
            this.projectIds = projectIds == null ? null : ImmutableSet.copyOf(projectIds);
            return this;
        }
    }
}
