package com.atlassian.jira.imports.project.validation;

import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.mapper.IssueTypeMapper;
import com.atlassian.jira.imports.project.mapper.StatusMapper;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;

/**
 * Validates the automatic mappings that have been created to see if the mappings are relevant in the current
 * JIRA instances setup. This validator makes sure that the statuses that are required:
 * <ul>
 * <li>exist in the current instance</li>
 * <li>are valid for the issue type registered based on the workflow associated with the project being restored</li>
 * </ul>
 *
 * @since v3.13
 */
public interface StatusMapperValidator
{
    /**
     * Makes sure that the statuses that are required:
     * <ul>
     * <li>exist in the current instance</li>
     * <li>are valid for the issue type registered based on the workflow associated with the project being restored</li>
     * </ul>
     *
     * @param i18nHelper helper bean that allows us to get i18n translations
     * @param backupProject is the backup project the data is mapped from
     * @param issueTypeMapper is the populated issueTypeMapper
     * @param statusMapper is the populated statusMapper
     * @return a MessageSet that will contain any generated errors (which should stop the import) or warnings
     * (which should be displayed to the user). The error and warning collection's will be empty if all validation
     * passes.
     */
    MessageSet validateMappings(I18nHelper i18nHelper, BackupProject backupProject, IssueTypeMapper issueTypeMapper, StatusMapper statusMapper);

    /**
     * Determines if a Status is valid within the context of the provided project and the provided issue types.
     *
     * @param oldStatusId the status id from the backup file
     * @param existingStatus the status object from the new system that may map to the old status, this can be null
     * @param statusMapper the status mapper that will provide a list of the associated issue type for the status
     * @param issueTypeMapper the fully mapped and populated issueTypeMapper that will allow us to map the backup issue
     * types to existing issue types.
     * @param projectKey the project key that will allow us to find the correct workflow scheme for the project we are
     * importing into
     * @return true if the status is valid for the project and issue types, false otherwise
     */
    boolean isStatusValid(final String oldStatusId, final Status existingStatus, final StatusMapper statusMapper, final IssueTypeMapper issueTypeMapper, final String projectKey);

}
