package com.atlassian.jira.imports.project.core;

import com.atlassian.jira.external.beans.ExternalComponent;
import com.atlassian.jira.external.beans.ExternalProject;
import com.atlassian.jira.external.beans.ExternalVersion;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldConfiguration;

import java.util.Collection;

/**
 * Represents information about a single project gleaned from an XML backup.
 *
 * @since v3.13
 */
public interface BackupProject
{
    /**
     * @return the {@link com.atlassian.jira.external.beans.ExternalProject} that is represented in the XML backup.
     */
    ExternalProject getProject();

    /**
     * @return the {@link com.atlassian.jira.external.beans.ExternalVersion}'s that are associated with the project
     * returned from {@link #getProject()}.
     */
    Collection<ExternalVersion> getProjectVersions();

    /**
     * @return the {@link com.atlassian.jira.external.beans.ExternalComponent}'s that are associated with the project
     * returned from {@link #getProject()}.
     */
    Collection<ExternalComponent> getProjectComponents();

    /**
     * @return the issue id's that are associated with the project returned from {@link #getProject()}.
     */
    Collection<Long> getIssueIds();

    /**
     * @return the {@link com.atlassian.jira.imports.project.customfield.ExternalCustomFieldConfiguration}'s that
     * describe the custom fields and their configurations for this project.
     */
    Collection<ExternalCustomFieldConfiguration> getCustomFields();

    /**
     * @param customFieldId the id of the custom field configuration to retrieve
     * @return the ExternalCustomFieldConfiguration for the custom field with the specified id, null if the project
     * does not have the custom field with the id.
     */
    ExternalCustomFieldConfiguration getCustomFieldConfiguration(String customFieldId);

    /**
     * @param id an issue id that can be converted to a Long
     * @return true if the backup project contains the issue, false otherwise.
     */
    boolean containsIssue(final String id);
}
