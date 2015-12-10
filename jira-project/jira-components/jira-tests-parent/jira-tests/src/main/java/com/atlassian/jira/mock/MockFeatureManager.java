package com.atlassian.jira.mock;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.CoreFeatures;
import com.atlassian.jira.config.Feature;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.plugin.profile.DarkFeatures;
import com.atlassian.jira.user.ApplicationUser;

public class MockFeatureManager implements FeatureManager
{
    private DarkFeatures darkFeatures;
    private Set<String> enabledFeatures;

    public MockFeatureManager()
    {
        darkFeatures = new DarkFeatures
                (
                        Collections.<String>emptySet(), Collections.<String>emptySet(), Collections.<String>emptySet()
                );
        enabledFeatures = new HashSet<String>();
    }

    public static class AllDarkFeaturesDisabled
    {
        public static MockFeatureManager get()
        {
            return new MockFeatureManager();
        }
    }

    @Override
    public boolean isEnabled(String featureKey)
    {
        return enabledFeatures.contains(featureKey);
    }

    @Override
    public boolean isEnabled(Feature feature)
    {
        return isEnabled(feature.featureKey());
    }

    @Override
    public boolean isOnDemand()
    {
        return isEnabled(CoreFeatures.ON_DEMAND);
    }

    public MockFeatureManager setOnDemand(boolean ondemand)
    {
        if (ondemand)
        {
            enable(CoreFeatures.ON_DEMAND);
        }
        else
        {
            disable(CoreFeatures.ON_DEMAND);
        }
        return this;
    }

    public boolean isEnabled(CoreFeatures feature)
    {
        return isEnabled(feature.featureKey());
    }

    public void enable(CoreFeatures feature)
    {
        enabledFeatures.add(feature.featureKey());
    }

    public void enable(Feature feature)
    {
        enabledFeatures.add(feature.featureKey());
    }

    public MockFeatureManager disable(Feature feature)
    {
        enabledFeatures.remove(feature.featureKey());
        return this;
    }

    @Override
    public Set<String> getEnabledFeatureKeys()
    {
        return enabledFeatures;
    }

    @Override
    public DarkFeatures getDarkFeatures()
    {
        return darkFeatures;
    }

    @Override
    public void enableUserDarkFeature(User user, String feature)
    {
        darkFeatures.getUserEnabledFeatures().add(feature);
    }

    @Override
    public void disableUserDarkFeature(User user, String feature)
    {
        darkFeatures.getUserEnabledFeatures().remove(feature);
    }

    @Override
    public void enableUserDarkFeature(ApplicationUser user, String feature)
    {
        darkFeatures.getUserEnabledFeatures().add(feature);
    }

    @Override
    public void disableUserDarkFeature(ApplicationUser user, String feature)
    {
        darkFeatures.getUserEnabledFeatures().remove(feature);
    }

    @Override
    public void enableSiteDarkFeature(String feature)
    {
        darkFeatures.getSiteEnabledFeatures().add(feature);
    }

    @Override
    public void disableSiteDarkFeature(String feature)
    {
        darkFeatures.getSiteEnabledFeatures().remove(feature);
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
