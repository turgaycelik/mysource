package com.atlassian.jira.imports.project.mapper;

import com.atlassian.annotations.PublicApi;

/**
 * The parent mapper that holds all sub-mappers that are used for a project import.
 *
 * @since v3.13
 */
@PublicApi
public interface ProjectImportMapper
{
    /**
     * Gets a ProjectImportIdMapper which, when fully populated, indicates all the projects from the backup file and
     * the mapped projects in the current JIRA instance. The projects are mapped by project key.
     *
     * @return ProjectImportIdMapper containing project id mappings.
     */
    SimpleProjectImportIdMapper getProjectMapper();

    /**
     * Gets a UserMapper which, when fully populated, indicates all the users "in-use" (required) by the backup
     * projects data's issues.
     *
     * @return UserMapper containing user id's that are required.
     */
    UserMapper getUserMapper();

    /**
     * Gets a ProjectRoleActorMapper which, when fully populated, indicates all the ProjectRoleActors "in-use" (optional) by the backup
     * projects data's issues.
     *
     * @return ProjectRoleActorMapper containing ExternalProjectRoleActors that are in use.
     */
    ProjectRoleActorMapper getProjectRoleActorMapper();

    /**
     * Gets a ProjectImportIdMapper which, when fully populated, indicates all the groups "in-use" (required) by the backup projects
     * data. The mapper will contain the id and key (e.g. "jira-developers", "jira-administrators", etc.) for groups
     * and will indicate which are in-use by the backup data's issues.
     *
     * @return ProjectImportIdMapper containing user id's that are required.
     */
    GroupMapper getGroupMapper();

    /**
     * Gets a StatusMapper which, when fully populated, indicates all the issue statuses "in-use" (required) by the backup projects
     * data. The mapper will contain the id and key (e.g. "In Progress", "Open", etc.) for all statuses and will indicate
     * which are in-use by the backup data's issues.
     *
     * @return StatusMapper containing issue status information.
     */
    StatusMapper getStatusMapper();

    /**
     * Gets a ProjectImportIdMapper which, when fully populated, indicates all the issue priorities "in-use" (required) by the backup projects
     * data. The mapper will contain the id and key (e.g. "Blocker", "Critical", etc.) for all priorities and will indicate
     * which are in-use by the backup data's issues.
     *
     * @return ProjectImportIdMapper containing issue priority information.
     */
    SimpleProjectImportIdMapper getPriorityMapper();

    /**
     * Gets a ProjectImportIdMapper which, when fully populated, indicates all the issue issue types "in-use" (required) by the backup projects
     * data. The mapper will contain the id and key (e.g. "Bug", "Task", etc.) for all issue types and will indicate
     * which are in-use by the backup data's issues.
     *
     * @return ProjectImportIdMapper containing issue issue type information.
     */
    IssueTypeMapper getIssueTypeMapper();

    /**
     * Gets a ProjectImportIdMapper which, when fully populated, indicates all the issue resolutions "in-use" (required) by the backup projects
     * data. The mapper will contain the id and key (e.g. "Fixed", "Won't Fix", etc.) for all resolutions and will indicate
     * which are in-use by the backup data's issues.
     *
     * @return ProjectImportIdMapper containing issue resolution information.
     */
    SimpleProjectImportIdMapper getResolutionMapper();

    /**
     * Gets a ProjectImportIdMapper which, when fully populated, indicates all the issue security levels "in-use" (required) by the backup projects
     * data. The mapper will contain the id and key (e.g. "Level 1", "Level 2", etc.) for all issue security levels
     * that are valid for the backup project as defined by the backup projects issue security scheme. The mapper
     * will indicate which are in-use by the backup data's issues.
     *
     * @return ProjectImportIdMapper containing issue security level information.
     */
    SimpleProjectImportIdMapper getIssueSecurityLevelMapper();

    /**
     * Gets a ProjectImportIdMapper which, when fully populated, indicates all the project versions "in-use" (required) by the backup projects
     * data. The mapper will contain the id and key (e.g. "Version 3.1.2", "Version 3.2.1", etc.) for all affects and
     * fix versions and will indicate which are in-use by the backup data's issues.
     *
     * @return ProjectImportIdMapper containing issue fix and affects version information.
     */
    SimpleProjectImportIdMapper getVersionMapper();

    /**
     * Gets a ProjectImportIdMapper which, when fully populated, indicates all the project components "in-use" (required) by the backup projects
     * data. The mapper will contain the id and key (e.g. "Comp 1", "Comp 2", etc.) for all components
     * and will indicate which are in-use by the backup data's issues.
     *
     * @return ProjectImportIdMapper containing project components information.
     */
    SimpleProjectImportIdMapper getComponentMapper();

    /**
     * Gets a ProjectImportIdMapper which, when fully populated, indicates all the project roles "in-use" (required) by the backup projects
     * data. The mapper will contain the id and key (e.g. "Developer", "Administrator", etc.) for all project roles
     * and will indicate which are in-use by the backup data's comments and worklogs.
     *
     * @return ProjectImportIdMapper containing project role information.
     */
    SimpleProjectImportIdMapper getProjectRoleMapper();

    /**
     * Gets a CustomFieldMapper which, when fully populated, indicates all the custom fields "in-use" (required) by the backup projects
     * data. The mapper will contain the id and key (e.g. "My Text CF", "Extra Reporter CF", etc.) for custom fields
     * that are valid for the backup project as defined by the custom fields configuration. The mapper will indicate
     * which are in-use by the backup data's issues. The mapper will also indicate which issue types are in use for
     * a given custom field.
     *
     * @return CustomFieldMapper containing custom field information.
     */
    CustomFieldMapper getCustomFieldMapper();

    /**
     * Gets a CustomFieldOptionMapper which, when fully populated, indicates how to map the IDs of the Custom Field Options
     * in the Import file, to the equivalent Option in the current JIRA.
     *
     * @return CustomFieldOptionMapper containing custom field option mapping information.
     */
    CustomFieldOptionMapper getCustomFieldOptionMapper();

    /**
     * Gets a ProjectImportIdMapper that maps the issue id's from the old backup project data to the id's
     * generated when adding the issue data to the new JIRA instance.
     *
     * NOTE: This is only populated once the issues have been persisted into the new system.
     *
     * @return a ProjectImportIdMapper that maps JIRA issue id's from old to new.
     */
    SimpleProjectImportIdMapper getIssueMapper();

    /**
     * Gets a ProjectImportIdMapper that maps the IssueLinkType id's from the old backup project data to the id's
     * in the current system.
     *
     * @return a ProjectImportIdMapper that maps IssueLinkType id's from old to new.
     */
    IssueLinkTypeMapper getIssueLinkTypeMapper();

    /**
     * Gets a ProjectImportIdMapper that maps the change group id's from the old backup project data to the id's
     * generated when adding the change group data to the new JIRA instance.
     *
     * NOTE: This is only populated once the change groups have been persisted into the new system.
     *
     * @return a ProjectImportIdMapper that maps JIRA change group id's from old to new.
     */
    SimpleProjectImportIdMapper getChangeGroupMapper();

    /**
     * Gets a ProjectImportIdMapper that maps the Comment id's from the old backup project data to the id's
     * in the current system.
     *
     * @return a ProjectImportIdMapper that maps JIRA comment id's from old to new.
     */
    SimpleProjectImportIdMapper getCommentMapper();

    /**
     * This will clear any mapped data that may have been entered into the mappers. All registered values and
     * values that have been flagged as required will not be changed. This method only affects the mapped data.
     * It is used to re-map and re-validate the data after the user has made changes to the current configuration.
     */
    void clearMappedValues();

}
