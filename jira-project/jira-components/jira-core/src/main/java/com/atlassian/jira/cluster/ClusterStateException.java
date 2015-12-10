package com.atlassian.jira.cluster;

/**
 * A generic exception for cluster related problems
 *
 * @since v6.1
 */
public class ClusterStateException extends Exception
{
    public ClusterStateException(final String message)
    {
        super(message);
    }
}
