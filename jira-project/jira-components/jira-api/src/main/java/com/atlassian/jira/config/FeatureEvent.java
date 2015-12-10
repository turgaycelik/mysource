package com.atlassian.jira.config;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.ApplicationUser;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Dark feature info.
 *
 * @since v5.1
 */
@PublicApi
@Immutable
public class FeatureEvent
{
    /**
     * A string containing the name of the dark feature.
     */
    @Nonnull
    private final String feature;

    /**
     * The name of the user for whom the dark feature was enabled. Null if it's a site-wide dark feature.
     */
    @Nullable
    private final String username;

    /**
     * Creates a new feature info.
     *
     * @param feature a String containing a feature name
     */
    protected FeatureEvent(@Nonnull String feature)
    {
        this(feature, (ApplicationUser) null);
    }

    /**
     * Creates a new feature info for a per-user feature.
     *
     * @param feature a String containing a feature name
     * @param user a User (may be null)
     * @deprecated use {@link #FeatureEvent(String, com.atlassian.jira.user.ApplicationUser)} since 6.0
     */
    protected FeatureEvent(@Nonnull String feature, @Nullable User user)
    {
        this.feature = notNull("feature", feature);
        this.username = user != null ? user.getName() : null;
    }
    /**
     * Creates a new feature info for a per-user feature.
     *
     * @param feature a String containing a feature name
     * @param user a ApplicationUser (may be null)
     */
    protected FeatureEvent(@Nonnull String feature, @Nullable ApplicationUser user)
    {
        this.feature = notNull("feature", feature);
        this.username = user != null ? user.getUsername() : null;
    }

    /**
     * @return a string containing the name of the dark feature.
     */
    public String feature()
    {
        return feature;
    }

    /**
     * @return the name of the user for whom the dark feature was enabled. Null if it's a site-wide dark feature.
     */
    @Nullable
    public String username()
    {
        return username;
    }
}
