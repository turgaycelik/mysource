package com.atlassian.jira.bc.license;

/**
 * This can provide a serverID to calling code
 *
 * @since v4.4
 */
public interface JiraServerIdProvider
{
    /**
     * Gets the server ID of the JIRA instance, creates it if it doesn't already exists.
     *
     * @return the server ID for this JIRA instance.
     */
    String getServerId();

}
