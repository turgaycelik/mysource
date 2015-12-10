package com.atlassian.jira.imports.project.mapper;

import com.atlassian.jira.imports.project.core.BackupProject;

/**
 * Contains methods to automatically map data from our import file to the corresponding object in the current JIRA.
 *
 * @since v3.13
 */
public interface AutomaticDataMapper
{
    /**
     * Populates the issueTypeMapper with the issue type mappings that are relevant to the backup project.
     *
     * This mapper does not add validation errors but will only map issue types that are valid in the system.
     *
     * @param backupProject the backup project that will identify the issue type scheme we will use for mapping
     * @param issueTypeMapper the issue type mapper to populate with issue types from the system
     */
    void mapIssueTypes(final BackupProject backupProject, final IssueTypeMapper issueTypeMapper);

    /**
     * Populates the issueLinkTypeMapper with the issue link type mappings that are relevant to the backup project.
     *
     * This mapper does not add validation errors but will only map issue link types that are valid to be used.
     *
     * @param issueLinkTypeMapper the issue link type mapper to populate.
     */
    void mapIssueLinkTypes(final IssueLinkTypeMapper issueLinkTypeMapper);

    /**
     * Automatically map Priorities in the given mapper.
     * Looks at all priorities that are registered in the mapper from the import file, and maps them to a priority
     * in the current system if possible.
     *
     * @param priorityMapper ProjectImportIdMapper with old values registered from the import file.
     */
    void mapPriorities(final SimpleProjectImportIdMapper priorityMapper);

    /**
     * Automatically map Resolutions in the given mapper.
     * Looks at all Resolutions that are registered in the mapper from the import file, and maps them to a Resolution
     * in the current system if possible.
     *
     * @param resolutionMapper ProjectImportIdMapper with old values registered from the import file.
     */
    void mapResolutions(final SimpleProjectImportIdMapper resolutionMapper);

    /**
     * Automatically map Statuses in the given mapper.
     * Looks at all Statuses that are registered in the mapper from the import file, and maps them to a Status in the
     * current system if possible.
     *
     * @param backupProject the backup project that will identify the workflow scheme we will use for mapping
     * @param statusMapper StatusMapper with old values registered from the import file.
     * @param issueTypeMapper that has ALREADY been populated and mapped.
     */
    void mapStatuses(final BackupProject backupProject, final StatusMapper statusMapper, final IssueTypeMapper issueTypeMapper);

    /**
     * Automatically map project roles in the given mapper.
     * Looks at all project roles that are registered in the mapper from the import file, and maps them to a project role in the
     * current system if possible.
     *
     * @param projectRoleMapper the mapper that contains the registered values and will be mapped to.
     */
    void mapProjectRoles(final SimpleProjectImportIdMapper projectRoleMapper);

    /**
     * Automatically map Projects in the given mapper.
     * Looks at all Projects that are registered in the mapper from the import file, and maps them to a Project
     * in the current system if possible, this is done by matching the projects key.
     *
     * @param projectMapper ProjectImportIdMapper with old values registered from the import file.
     */
    void mapProjects(final SimpleProjectImportIdMapper projectMapper);

    /**
     * Automatically map issue security levels in the given mapper.
     * @param projectKey the backup project key that will identify the issue security level scheme we will use for mapping
     * @param securityLevelMapper the mapper that contains the registered values wand will be mapped to.
     */
    void mapIssueSecurityLevels(String projectKey, SimpleProjectImportIdMapper securityLevelMapper);

    /**
     * Automatically map Custom Fields in the given mapper.
     * Looks at all Custom Fields that are required in the mapper from the import file, and maps them to a Custom Field
     * in the current system if possible.
     *
     * @param backupProject the backup project that will identify the workflow scheme we will use for mapping
     * @param customFieldMapper with old values registered from the import file.
     * @param issueTypeMapper that has ALREADY been populated and mapped.
     */
    void mapCustomFields(BackupProject backupProject, CustomFieldMapper customFieldMapper, IssueTypeMapper issueTypeMapper);

    /**
     * Automatically map custom field options in the given mapper.
     * Looks at all the Custom Field Options that are required in the mapper from the import file, and maps them to a
     * Custom Field option in the current system if possible.
     *
     * @param backupProject the backup project that will identify the workflow scheme we will use for mapping
     * @param customFieldOptionMapper with old values registered from the import file.
     * @param customFieldMapper a fully mapped and validated custom field mapper.
     * @param issueTypeMapper a fully mapped and validated issue type mapper.
     */
    void mapCustomFieldOptions(BackupProject backupProject, CustomFieldOptionMapper customFieldOptionMapper, CustomFieldMapper customFieldMapper, IssueTypeMapper issueTypeMapper);
}
