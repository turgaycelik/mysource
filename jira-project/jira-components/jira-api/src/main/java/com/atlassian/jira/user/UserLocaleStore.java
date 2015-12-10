package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.api.User;

import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Store that deals with an user's configured locale.
 *
 * @since v6.2.3
 */
public interface UserLocaleStore
{
    /**
     * Return the {@code Locale} associated with the passed user.
     *
     * @param user the user for the query.
     *
     * @return the {@code Locale} associated with the passed user.
     */
    @Nonnull
    Locale getLocale(@Nullable ApplicationUser user);

    /**
     * Return the {@code Locale} associated with the passed user.
     *
     * @param user the user for the query.
     *
     * @return the {@code Locale} associated with the passed user.
     */
    @Nonnull
    Locale getLocale(@Nullable User user);

    /**
     * Return the {@code Locale} to use for a user without a locale.
     *
     * @return the default locale of JIRA.
     */
    @Nonnull
    Locale getDefaultLocale();
}
