package com.atlassian.jira.config;

import com.atlassian.annotations.Internal;
import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.profile.DarkFeatures;
import com.atlassian.jira.user.ApplicationUser;

import java.util.Set;
import javax.annotation.Nullable;

/**
 * Component responsible for providing information whether certain features in JIRA are enabled or disabled.
 *
 * @since v4.4
 */
@PublicApi
public interface FeatureManager
{
    /**
     * The prefix used for enabling dark features from the command line. For example, one might use
     * <code>-Datlassian.darkfeature.com.atlassian.jira.config.FAST_TABS=true</code> as a JVM argument in order to
     * enable fast tabs.
     */
    String SYSTEM_PROPERTY_PREFIX = "atlassian.darkfeature.";

    /**
     * Checks whether feature {@code featureKey} is enabled either in the running JIRA instance
     * or for the current user.
     *
     * @param featureKey feature key
     * @return <code>true</code>, if feature identified by <tt>featureKey</tt> is enabled, <code>false</code> otherwise
     */
    boolean isEnabled(String featureKey);


    /**
     * Checks whether {@code feature} is enabled either in the running JIRA instance
     * or for the current user.  This method should be prefered over
     * {@link #isEnabled(String)} for internal feature checks, particularly for
     * core features that are not user-settable, as it will skip loading the
     * current user's preferences when possible.
     *
     * @param feature the core feature to check
     * @return {@code true} if {@code feature} is enabled; {@code false} otherwise
     */
    @Internal
    boolean isEnabled(CoreFeatures feature);

    /**
     * Checks whether or not the specified feature is enabled.  This method
     * should be prefered over {@link #isEnabled(String)} for internal feature checks,
     * particularly for core features that are not user-settable, as it will skip
     * loading the current user's preferences when possible.
     *
     * @param feature the feature to check
     * @return {@code true} if {@code feature} is enabled; {@code false} otherwise
     * @since v6.0
     */
    @Internal
    boolean isEnabled(Feature feature);

    /**
     * Returns a set containing the feature keys of all features that are currently enabled.
     *
     * @return a set containing the feature keys of all features that are currently enabled
     * @since v5.0
     */
    Set<String> getEnabledFeatureKeys();

    /**
     * Creates {@link com.atlassian.jira.plugin.profile.DarkFeatures} instances from the PropertySet associated
     * with the current user.
     *
     * @return Returns the Dark Features state for the current user.
     */
    DarkFeatures getDarkFeatures();

    /**
     * Convenience method equivalent to calling
     * {@link #isEnabled(CoreFeatures) isEnabled(CoreFeatures.ON_DEMAND)}.
     *
     * @return {@code true} if this is JIRA OnDemand; {@code false} otherwise
     * @since v6.0
     */
    boolean isOnDemand();

    /**
     * Enables a feature for a particular User. Raises a {@link FeatureEnabledEvent}.
     * <p/>
     * Since JIRA 5.1, this method raises a {@link FeatureEnabledEvent} if it is successful.
     *
     * @param user the user to enable the feature for
     * @param feature the feature to enable
     * @deprecated Use {@link #enableUserDarkFeature(com.atlassian.jira.user.ApplicationUser, String feature)} instead. Since v6.0.
     */
    void enableUserDarkFeature(User user, String feature);

    /**
     * Disables a feature for a particular user.
     * <p/>
     * Since JIRA 5.1, this method raises a {@link FeatureDisabledEvent} if it is successful.
     *
     * @param user the user to disable the feature for
     * @param feature the feature to disable
     * @deprecated Use {@link #disableUserDarkFeature(com.atlassian.jira.user.ApplicationUser, String feature)} instead. Since v6.0.
     */
    void disableUserDarkFeature(User user, String feature);

    /**
     * Enables a feature for a particular User. Raises a {@link FeatureEnabledEvent}.
     * <p/>
     * Since JIRA 5.1, this method raises a {@link FeatureEnabledEvent} if it is successful.
     *
     * @param user the user to enable the feature for
     * @param feature the feature to enable
     */
    void enableUserDarkFeature(ApplicationUser user, String feature);

    /**
     * Disables a feature for a particular user.
     * <p/>
     * Since JIRA 5.1, this method raises a {@link FeatureDisabledEvent} if it is successful.
     *
     * @param user the user to disable the feature for
     * @param feature the feature to disable
     */
    void disableUserDarkFeature(ApplicationUser user, String feature);

    /**
     * Enables a site-wide feature.
     * <p/>
     * Since JIRA 5.1, this method raises a {@link FeatureEnabledEvent} if it is successful.
     *
     * @param feature the feature to enable
     */
    void enableSiteDarkFeature(String feature);

    /**
     * Disables a site-wide feature.
     *
     * Since JIRA 5.1, this method raises a {@link FeatureDisabledEvent} if it is successful.
     *
     * @param feature the feature to disable
     */
    void disableSiteDarkFeature(String feature);

    /**
     * Returns true if the currently logged in user has permission to edit site dark features.
     *
     * @return true if the currently logged in user has permission to edit site dark features.
     * @since 5.2
     */
    boolean hasSiteEditPermission();

    /**
     * @param user the user being queried; <code>null</code> represents the anonymous user
     * @return Returns the dark features state for the current user.
     */
    DarkFeatures getDarkFeaturesForUser(@Nullable ApplicationUser user);

    /**
     * Checks whether a feature with given <tt>featureKey</tt> is enabled in the running JIRA instance for the given user.
     *
     *
     * @param user the user being queried
     * @param featureKey feature key
     * @return <code>true</code>, if feature identified by <tt>featureKey</tt> is enabled, <code>false</code> otherwise
     */
    boolean isEnabledForUser(ApplicationUser user, String featureKey);
}
