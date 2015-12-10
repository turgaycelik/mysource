package com.atlassian.sal.jira.features;

import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.plugin.profile.DarkFeatures;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.sal.api.features.DarkFeatureManager;
import com.atlassian.sal.api.features.EnabledDarkFeatures;
import com.atlassian.sal.api.features.EnabledDarkFeaturesBuilder;
import com.atlassian.sal.api.features.ValidFeatureKeyPredicate;
import com.atlassian.sal.api.user.UserKey;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.atlassian.jira.util.dbc.Assertions.stateTrue;

public class JiraDarkFeaturesManager implements DarkFeatureManager
{
    private final FeatureManager featureManager;
    private final UserManager userManager;
    private final JiraAuthenticationContext authenticationContext;

    public JiraDarkFeaturesManager(FeatureManager featureManager, UserManager userManager, JiraAuthenticationContext authenticationContext)
    {
        this.featureManager = featureManager;
        this.userManager = userManager;
        this.authenticationContext = authenticationContext;
    }

    @Override
    public boolean isFeatureEnabledForAllUsers(final String featureKey)
    {
        return ValidFeatureKeyPredicate.isValidFeatureKey(featureKey) && featureManager.getDarkFeatures().getGlobalEnabledFeatureKeys().contains(featureKey);
    }

    @Override
    public boolean isFeatureEnabledForCurrentUser(final String featureKey)
    {
        return ValidFeatureKeyPredicate.isValidFeatureKey(featureKey) && featureManager.getDarkFeatures().isFeatureEnabled(featureKey);
    }

    @Override
    public boolean isFeatureEnabledForUser(UserKey user, String featureKey)
    {
        final ApplicationUser applicationUser = resolveApplicationUser(user);
        return featureManager.getDarkFeaturesForUser(applicationUser).isFeatureEnabled(featureKey);
    }

    @Override
    public boolean canManageFeaturesForAllUsers()
    {
        return featureManager.hasSiteEditPermission();
    }

    @Override
    public void enableFeatureForAllUsers(final String featureKey)
    {
        featureManager.enableSiteDarkFeature(ValidFeatureKeyPredicate.checkFeatureKey(featureKey));
    }

    @Override
    public void disableFeatureForAllUsers(final String featureKey)
    {
        featureManager.disableSiteDarkFeature(ValidFeatureKeyPredicate.checkFeatureKey(featureKey));
    }

    @Override
    public void enableFeatureForCurrentUser(final String featureKey)
    {
        final ApplicationUser applicationUser = authenticationContext.getUser();
        stateTrue("Anonymous user is not supported", applicationUser != null);
        enableFeatureForUser(applicationUser, featureKey);
    }

    @Override
    public void enableFeatureForUser(final UserKey userKey, final String featureKey)
    {
        Assertions.is("Anonymous user is not supported", userKey != null);
        final ApplicationUser applicationUser = resolveApplicationUser(userKey);
        enableFeatureForUser(applicationUser, featureKey);
    }

    private void enableFeatureForUser(@Nullable final ApplicationUser user, final String featureKey)
    {
        if (user != null)
        {
            featureManager.enableUserDarkFeature(user, ValidFeatureKeyPredicate.checkFeatureKey(featureKey));
        }
    }

    @Override
    public void disableFeatureForCurrentUser(final String featureKey)
    {
        final ApplicationUser applicationUser = authenticationContext.getUser();
        stateTrue("Anonymous user is not supported", applicationUser != null);
        disableFeatureForUser(applicationUser, featureKey);
    }

    @Override
    public void disableFeatureForUser(final UserKey userKey, final String featureKey)
    {
        Assertions.is("Anonymous user is not supported", userKey != null);
        final ApplicationUser applicationUser = resolveApplicationUser(userKey);
        disableFeatureForUser(applicationUser, featureKey);
    }

    private void disableFeatureForUser(@Nullable final ApplicationUser user, final String featureKey)
    {
        if (user != null)
        {
            featureManager.disableUserDarkFeature(user, ValidFeatureKeyPredicate.checkFeatureKey(featureKey));
        }
    }

    @Override
    public EnabledDarkFeatures getFeaturesEnabledForAllUsers()
    {
        return createEnabledDarkFeatures(featureManager.getDarkFeatures());
    }

    @Override
    public EnabledDarkFeatures getFeaturesEnabledForCurrentUser()
    {
        return createEnabledDarkFeatures(featureManager.getDarkFeatures());
    }

    @Override
    public EnabledDarkFeatures getFeaturesEnabledForUser(@Nullable final UserKey userKey)
    {
        final ApplicationUser applicationUser = resolveApplicationUser(userKey);
        return createEnabledDarkFeatures(featureManager.getDarkFeaturesForUser(applicationUser));
    }

    @Nullable
    private ApplicationUser resolveApplicationUser(@Nullable final UserKey userKey)
    {
        if (userKey == null) {
            return null;
        }

        final ApplicationUser applicationUser = userManager.getUserByKey(userKey.getStringValue());
        if (applicationUser == null)
        {
            throw new IllegalArgumentException(String.format("The given user key '%s' could not be resolved to an existing JIRA user.", userKey.getStringValue()));
        }
        return applicationUser;
    }

    @Nonnull
    private EnabledDarkFeatures createEnabledDarkFeatures(@Nonnull final DarkFeatures darkFeatures)
    {
        return new EnabledDarkFeaturesBuilder()
                .unmodifiableFeaturesEnabledForAllUsers(darkFeatures.getSystemEnabledFeatures())
                .featuresEnabledForAllUsers(darkFeatures.getSiteEnabledFeatures())
                .featuresEnabledForCurrentUser(darkFeatures.getUserEnabledFeatures())
                .build();
    }
}
