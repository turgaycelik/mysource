package com.atlassian.jira.cluster;

/**
 * Thrown when a cluster-specific operation is attempted but the target instance is not part of a cluster.
 *
 * @since 6.1
 */
public class NotClusteredException extends ClusterStateException
{
    public NotClusteredException()
    {
        super("This JIRA instance is not part of a cluster. Does JIRA_HOME contain a cluster.properties file?");
    }
}
