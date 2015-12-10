package com.atlassian.jira.jql.query;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;

/**
 * Default implementation of {@link QueryCreationContext}.
 *
 * @since v4.0
 */
public class QueryCreationContextImpl implements QueryCreationContext
{
    private final ApplicationUser user;
    private final boolean securityOverriden;

    /**
     * Use this constructor unless you know you need to override security.
     *
     * @param user the user performing the search
     */
    public QueryCreationContextImpl(final ApplicationUser user)
    {
        this(user, false);
    }

    /**
     * Use this constructor if you need to override security.
     *
     * @param user the user performing the search
     * @param securityOverriden true if you want to override security; false otherwise
     */
    public QueryCreationContextImpl(final ApplicationUser user, final boolean securityOverriden)
    {
        this.user = user;
        this.securityOverriden = securityOverriden;
    }

    /**
     * Use this constructor unless you know you need to override security.
     *
     * @param user the user performing the search
      @deprecated since 6.1 use {@link #QueryCreationContextImpl(com.atlassian.jira.user.ApplicationUser)} instead
     */
    @Deprecated
    public QueryCreationContextImpl(final User user)
    {
        this(ApplicationUsers.from(user), false);
    }

    /**
     * Use this constructor if you need to override security.
     *
     * @param user the user performing the search
     * @param securityOverriden true if you want to override security; false otherwise
     * @deprecated since 6.1 use {@link #QueryCreationContextImpl(com.atlassian.jira.user.ApplicationUser, boolean)} instead
     */
    @Deprecated
    public QueryCreationContextImpl(final User user, final boolean securityOverriden)
    {
        this(ApplicationUsers.from(user), securityOverriden);
    }

    @Override
    public ApplicationUser getApplicationUser()
    {
        return user;
    }

    @Override
    public User getUser()
    {
        return ApplicationUsers.toDirectoryUser(user);
    }

    @Override
    public User getQueryUser()
    {
        return ApplicationUsers.toDirectoryUser(user);
    }

    @Override
    public boolean isSecurityOverriden()
    {
        return securityOverriden;
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

        final QueryCreationContextImpl that = (QueryCreationContextImpl) o;

        if (securityOverriden != that.securityOverriden)
        {
            return false;
        }
        if (user != null ? !user.equals(that.user) : that.user != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = user != null ? user.hashCode() : 0;
        result = 31 * result + (securityOverriden ? 1 : 0);
        return result;
    }
}
