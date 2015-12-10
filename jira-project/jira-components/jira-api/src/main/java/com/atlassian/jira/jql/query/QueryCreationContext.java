package com.atlassian.jira.jql.query;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.ApplicationUser;

/**
 * A parameter object that encapsulates the context required when creating queries in the JQL way.
 * <p>
 * The user specified by {@link #getUser()} represents the user that is performing the search, and thus the user to use
 * when performing security checks in query factories, JQL functions and resolving fields/handlers from clause names.
 * <p>
 * These security checks will be ignored when the {@link #isSecurityOverriden()} is set to <code>true</code>. This is
 * necessary when creating queries in the context of administration searches, where you need to ensure that all the
 * results in the system are returned, regardless of who is performing the search.
 *
 * @since v4.0
 */
public interface QueryCreationContext
{
    /**
     * Get the User.
     *
     * @return the user in this context; null signifies the anonymous user.
     * @deprecated since 6.1 use {@link #getApplicationUser()} instead
     */
    @Deprecated
    User getUser();

    /**
     * Get the User.
     *
     * @return the user in this context; null signifies the anonymous user.
     */
    ApplicationUser getApplicationUser();

    /**
     * Get the User.
     *
     * @return the user in this context; null signifies the anonymous user.
     *
     * @deprecated Use {@link #getUser()} instead. Since v5.0.
     */
    User getQueryUser();

    /**
     * @return true if security should be overriden when creating the lucene query or evaluating JQL functions. Security
     * restrictions will also be lifted on retrieving the field ids or clause handler for a clause name. If true, the
     * user specified in {@link #getQueryUser()} will be ignored. If false, the user will be used to do permission checks.
     */
    boolean isSecurityOverriden();
}
