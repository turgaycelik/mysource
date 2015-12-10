package com.atlassian.jira.cluster;

/**
 * Marker annotation to indicate that an item, such as a cache, is cluster safe, even though it uses a pattern,
 * e.g. ConcurrentHashMap, that would not normally be so.
 *
 * This would typically be used for something that is safely cached independently on each node of the cluster, such
 * as a Velocity Template Cache.
 *
 * @since v6.2
 */
public @interface ClusterSafe
{
    /**
     * The optional reason why the target is cluster-safe.
     */
    String value() default "";
}