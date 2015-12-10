package com.atlassian.jira.bc.issue.watcher;

import com.atlassian.annotations.PublicApi;

/**
 * This exception indicates that there was an attempt to perform a watching-related operation while watching is disabled
 * in JIRA.
 *
 * @since v4.2
 */
@PublicApi
public class WatchingDisabledException extends RuntimeException
{
    public WatchingDisabledException()
    {
        super();
    }
}
