package com.atlassian.jira.imports.project.core;

import java.io.Serializable;
import java.util.List;

/**
 * Contains project and system information gleaned from an XML backup.
 *
 * @since v3.13
 */
public interface BackupOverview extends Serializable
{
    /**
     * Gets a {@link com.atlassian.jira.imports.project.core.BackupProject} by the projectKey.
     * 
     * @param projectKey the unique key that identifies the project.
     * @return a {@link com.atlassian.jira.imports.project.core.BackupProject} identified by the key or null if
     * the object does not know about the project key.
     */
    BackupProject getProject(String projectKey);

    /**
     * Gets all the {@link com.atlassian.jira.imports.project.core.BackupProject}'s contained in the backup ordered
     * alphabetically by the projects name.
     *
     * @return List of {@link com.atlassian.jira.imports.project.core.BackupProject}'s.
     */
    List /*<BackupProject>*/getProjects();

    /**
     * Returns a BackupSystemInformation object containing system-wide information from the backup file.
     * @return a BackupSystemInformation object containing system-wide information from the backup file.
     */
    BackupSystemInformation getBackupSystemInformation();
}
