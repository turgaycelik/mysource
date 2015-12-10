package com.atlassian.jira.imports.project.core;

import java.util.List;

/**
 * An object containing system-wide information from the backup file.
 *
 * @since v3.13
 */
public interface BackupSystemInformation
{
    /**
     * Gets all the {@link com.atlassian.jira.plugin.PluginVersion}'s contained in the backup.
     *
     * @return List of {@link com.atlassian.jira.plugin.PluginVersion}'s.
     */
    List /*<PluginVersion>*/getPluginVersions();

    /**
     * Returns the JIRA build number the backup came from.
     *
     * @return string representing the JIRA build number. It is safe to construct a number object with this string.
     */
    String getBuildNumber();

    /**
     * Returns the JIRA edition the backup came from.
     *
     * @return string representing the JIRA edition, 'Enterprise', 'Professional', or 'Standard'
     */
    String getEdition();

    /**
     * Returns <code>true</code> if unassigned issues are allowed in the backup file.
     * @return <code>true</code> if unassigned issues are allowed in the backup file.
     */
    boolean unassignedIssuesAllowed();

    /**
     * Will get the issue key provided the issue id for any issue that existed in the backup file.
     *
     * @param issueId string representation of the issue id long.
     * @return the issue key (i.e. TST-12) if the issue with the provided id existed in the backup file, null
     * otherwise.
     */
    String getIssueKeyForId(String issueId);

    /**
     * Returns the number of entities in the backup file.
     * @return the number of entities in the backup file.
     */
    int getEntityCount();

}
