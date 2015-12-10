package com.atlassian.jira.plugin.profile;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;

import java.util.Set;

/**
 * Represents the current state of the dark features for a particular user.
 */
public class DarkFeatures
{
    private final Set<String> systemEnabledFeatures;
    private final Set<String> siteEnabledFeatures;
    private final Set<String> userEnabledFeatures;

    public DarkFeatures(Set<String> systemEnabledFeatures, Set<String> siteEnabledFeatures, Set<String> userEnabledFeatures)
    {
        this.systemEnabledFeatures = ImmutableSet.copyOf(systemEnabledFeatures);
        this.userEnabledFeatures = ImmutableSet.copyOf(userEnabledFeatures);
        this.siteEnabledFeatures = ImmutableSet.copyOf(siteEnabledFeatures);
    }

    public Set<String> getSystemEnabledFeatures()
    {
        return systemEnabledFeatures;
    }

    public Set<String> getSiteEnabledFeatures()
    {
        return siteEnabledFeatures;
    }

    public Set<String> getUserEnabledFeatures()
    {
        return userEnabledFeatures;
    }

    /**
     * @return Dark Features enabled for all users, whether by system.property or site configuration.
     */
    public Set<String> getGlobalEnabledFeatureKeys()
    {
        return Sets.union(systemEnabledFeatures, siteEnabledFeatures);
    }

    public Set<String> getAllEnabledFeatures()
    {
        return Sets.union(Sets.union(userEnabledFeatures, siteEnabledFeatures), systemEnabledFeatures);
    }

    public boolean isFeatureEnabled(String featureKey)
    {
        return getAllEnabledFeatures().contains(StringUtils.trim(featureKey));
    }
}
