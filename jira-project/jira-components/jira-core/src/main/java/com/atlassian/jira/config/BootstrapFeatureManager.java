package com.atlassian.jira.config;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.profile.DarkFeatures;
import com.atlassian.jira.user.ApplicationUser;

import java.util.Collections;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * Simple feature manager used during bootstrap. Setup does not use features at all currently.
 *
 * @since v5.0
 */
public class BootstrapFeatureManager implements FeatureManager
{
    @Override
    public boolean isEnabled(String featureKey)
    {
        return false;
    }

    @Override
    public boolean isEnabled(Feature coreFeature)
    {
        return false;
    }

    @Override
    public boolean isEnabled(CoreFeatures coreFeature)
    {
        return false;
    }

    @Override
    public boolean isOnDemand()
    {
        return false;
    }

    @Override
    public Set<String> getEnabledFeatureKeys()
    {
        return Collections.emptySet();
    }

    @Override
    public DarkFeatures getDarkFeatures()
    {
        return new DarkFeatures(Collections.<String>emptySet(), Collections.<String>emptySet(), Collections.<String>emptySet());
    }

    @Override
    public void enableUserDarkFeature(User user, String feature)
    {
    }

    @Override
    public void disableUserDarkFeature(User user, String feature)
    {
    }

    @Override
    public void enableUserDarkFeature(ApplicationUser user, String feature)
    {

    }

    @Override
    public void disableUserDarkFeature(ApplicationUser user, String feature)
    {

    }

    @Override
    public void enableSiteDarkFeature(String feature)
    {
    }

    @Override
    public void disableSiteDarkFeature(String feature)
    {
    }

    @Override
    public boolean hasSiteEditPermission()
    {
        return true;
    }

    @Override
    public DarkFeatures getDarkFeaturesForUser(@Nullable ApplicationUser user)
    {
        return getDarkFeatures();
    }

    @Override
    public boolean isEnabledForUser(ApplicationUser user, String featureKey)
    {
        return isEnabled(featureKey);
    }
}
