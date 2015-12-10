package com.atlassian.jira.user;

import com.atlassian.annotations.Internal;
import com.atlassian.crowd.embedded.api.User;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Used so we can efficiently convert back and forth between a directory user and an application user.
 *
 * @since v6.0
 */
@Internal
class BridgedDirectoryUser implements User
{
    private final User delegate;
    private final ApplicationUser applicationUser;

    BridgedDirectoryUser(User delegate, ApplicationUser applicationUser)
    {
        this.delegate = notNull("delegate", delegate);
        this.applicationUser = notNull("applicationUser", applicationUser);
    }

    @Override
    public long getDirectoryId() {return delegate.getDirectoryId();}

    @Override
    public boolean isActive() {return delegate.isActive();}

    @Override
    public String getEmailAddress() {return delegate.getEmailAddress();}

    @Override
    public String getDisplayName() {return delegate.getDisplayName();}

    @SuppressWarnings ("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object o) {return delegate.equals(o);}

    @Override
    public int hashCode() {return delegate.hashCode();}

    @Override
    public int compareTo(User user) {return delegate.compareTo(user);}

    @Override
    public String toString() {return delegate.toString();}

    @Override
    public String getName() {return delegate.getName();}

    public ApplicationUser toApplicationUser()
    {
        return applicationUser;
    }
}
