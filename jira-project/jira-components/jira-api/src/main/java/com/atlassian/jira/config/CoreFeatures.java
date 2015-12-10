package com.atlassian.jira.config;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.config.properties.JiraSystemProperties;

import static com.atlassian.jira.config.FeatureManager.SYSTEM_PROPERTY_PREFIX;

/**
 * Core manipulable JIRA features.
 */
@Internal
public enum CoreFeatures implements Feature
{
    /**
     * Enabled when running in the 'On Demand' environment.
     */
    ON_DEMAND,

    /**
     * Turn off comment limiting on view issue for performance testing only.
     */
    PREVENT_COMMENTS_LIMITING,

    /**
     * Enabled to allow for role based licensing
     */
    LICENSE_ROLES_ENABLED;

    private static final String FEATURE_KEY_PREFIX = CoreFeatures.class.getName() + '.';

    private final String featureKey;
    private final boolean isUserSettable;
    private final JiraProperties jiraSystemProperties = JiraSystemProperties.getInstance();


    /**
     * Default constructor which uses {@code false} for {@code isDevFeature}.
     */
    CoreFeatures()
    {
        this(false);
    }

    /**
     * Constructor allowing whether or not this is a development feature to be specified.
     * Set it to {@code true} if this feature can be safely enabled or disabled by individual
     * users without breaking things, which will allow user to opt in or out of using the
     * feature during its development.  Use {@code false} (the default) if the feature is
     * site-wide and can not safely be set differently for individual users.
     * <p/>
     * Other names "Dark Feature", "User-enabled Feature", "Runtime Feature".
     *
     * @param isUserSettable indicates that this is a development feature that individual
     *      users can safely enable and disable
     */
    CoreFeatures(boolean isUserSettable)
    {
        featureKey = CoreFeatures.class.getName() + '.' + name();
        this.isUserSettable = isUserSettable;
    }

    @Override
    public String featureKey()
    {
        return featureKey;
    }

    /**
     * @return the name of the system property you can use to enable or disable a core feature
     */
    public String systemPropertyKey()
    {
        return SYSTEM_PROPERTY_PREFIX + featureKey;
    }

    /**
     * Returns whether or not the feature is under active development and can be enabled or
     * disabled by individual users.
     *
     * @return {@code true} if the feature is a user-configurable development feature; {@code false}
     *      if the feature can only be enabled or disabled system-wide.
     */
    public boolean isDevFeature()
    {
        return isUserSettable;
    }

    /**
     * Returns true if the system property corresponding to this feature is set to <b>true</b>. The property name will
     * have the form <code>{@value FeatureManager#SYSTEM_PROPERTY_PREFIX}com.atlassian.jira.config.CoreFeatures.FEATURE</code>.
     *
     * @return a boolean indicating whether this feature is enabled by a system property
     */
    public boolean isSystemPropertyEnabled()
    {
        return jiraSystemProperties.getBoolean(systemPropertyKey());
    }

    /**
     * Returns the core feature with the given feature key.
     * @param featureKey the feature key to check (must not be {@code null})
     * @return the CoreFeatures object with the specified feature key, or {@code null} if
     *      {@code featureKey} does not correspond to any known core feature
     */
    public static CoreFeatures forFeatureKey(String featureKey)
    {
        if (!featureKey.startsWith(FEATURE_KEY_PREFIX))
        {
            return null;
        }
        try
        {
            return valueOf(featureKey.substring(FEATURE_KEY_PREFIX.length()));
        }
        catch (IllegalArgumentException iae)
        {
            return null;
        }

    }
}
