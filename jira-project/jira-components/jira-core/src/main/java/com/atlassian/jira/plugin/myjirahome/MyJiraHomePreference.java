package com.atlassian.jira.plugin.myjirahome;

import com.atlassian.crowd.embedded.api.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Retrieves a user's My JIRA Home. This is not the actual link to the location but the plugin module key instead.
 *
 * @since 5.1
 */
public interface MyJiraHomePreference
{
    /**
     * Finds a home location for the given user. If the value found is not usable or there was an error, a default value
     * <em>can</em> be provided.
     *
     * @param user the user for which the home location is requested
     * @return the user's home - if it's usable; an empty value if there is no value set and no default value available
     */
    @Nonnull
    String findHome(@Nullable User user);

}
