package com.atlassian.jira.config;

/**
 * A switchable feature identity, can be enabled or disabled with {@link FeatureManager}.
 * Although {@link CoreFeatures} can control whether or not they are user-settable, other
 * {@code Feature}s can not; they are always implicitly user-settable.  Plugin developers
 * are responsible for ensuring that any {@code Feature} they check can safely be enabled
 * and disabled on a per-user basis.
 *
 * @since v6.0
 */
public interface Feature
{
    /**
     * The feature key that can be used to enable, disable or query the status of a feature using the
     * {@link FeatureManager}.
     *
     * @return the unique key used to identify the feature.
     */
    String featureKey();
}
