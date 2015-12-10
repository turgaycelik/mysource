package com.atlassian.jira.user;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.crowd.embedded.api.User;

import com.opensymphony.module.propertyset.PropertySet;

/**
 * The manager allows the caller to get the {@link PropertySet} associated with a user.
 * Property sets are live objects and changes to the property set are persisted when they occur.
 * The properties that are stored in the user's property set are not part of the JIRA API, and
 * modifying the property set directly may have unexpected side-effects.  In most cases, it
 * will make more sense to use the {@link com.atlassian.jira.user.preferences.UserPreferencesManager}
 * to obtain a user's preferences, instead.
 *
 * @since v4.3
 */

public interface UserPropertyManager
{
    /**
     * Get the property set associated with a user.
     *
     * @param user the user that the property set is associated with.
     * @return the property set for this user
     * @throws IllegalArgumentException if {@code user} is {@code null}
     * @throws IllegalStateException if the user does not have an ID mapping, which suggests that
     *      the user does not actually exist
     */
    @Nonnull
    PropertySet getPropertySet(@Nonnull ApplicationUser user);

    /**
     * Get the property set associated with a user.
     *
     * @param user the user that the property set is associated with.
     * @return property set
     * @throws IllegalArgumentException if {@code user} is {@code null}
     * @throws IllegalStateException if the user does not have an ID mapping, which suggests that
     *      the user does not actually exist
     * @deprecated Use {@link #getPropertySet(ApplicationUser)} or {@link #getPropertySetForUserKey(String)} instead. Since v6.2.
     */
    @Deprecated
    @Nonnull
    PropertySet getPropertySet(@Nonnull User user);

    /**
     * Get the property set associated with a user.
     *
     * @param userKey the key for the user that the property set is associated with.
     * @return property set
     * @throws IllegalArgumentException if {@code userKey} is {@code null}
     * @throws IllegalStateException if the user does not have an ID mapping, which suggests that
     *      the user does not actually exist
     */
    @Nonnull
    PropertySet getPropertySetForUserKey(String userKey);
}
