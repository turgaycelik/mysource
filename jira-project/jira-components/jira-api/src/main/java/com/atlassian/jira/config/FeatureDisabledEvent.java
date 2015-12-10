package com.atlassian.jira.config;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.ApplicationUser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Event raised when a feature is enabled.
 *
 * @since v5.1
 */
@PublicApi
@Immutable
public class FeatureDisabledEvent extends FeatureEvent
{
    /**
     * Creates a new "feature enabled" event for a site-wide feature.
     *
     * @param feature a String containing a feature name
     */
    public FeatureDisabledEvent(@Nonnull String feature)
    {
        super(feature);
    }

    /**
     * Creates a new "feature enabled" event for a per-user feature.
     *
     * @param feature a String containing a feature name
     * @param user a User (may be null)
     */
    public FeatureDisabledEvent(@Nonnull String feature, @Nullable User user)
    {
        super(feature, user);
    }

    /**
     * Creates a new "feature enabled" event for a per-user feature.
     *
     * @param feature a String containing a feature name
     * @param user a ApplicationUser (may be null)
     */
    public FeatureDisabledEvent(@Nonnull String feature, @Nullable ApplicationUser user)
    {
        super(feature, user);
    }
}
