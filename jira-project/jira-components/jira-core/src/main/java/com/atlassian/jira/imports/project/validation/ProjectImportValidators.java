package com.atlassian.jira.imports.project.validation;

/**
 * This injected interface holds all the individual validators for the Project Imports.
 *
 * @since v3.13
 */
public interface ProjectImportValidators
{
    /**
     * @return the validator that is responsible for issue type validation.
     */
    IssueTypeMapperValidator getIssueTypeMapperValidator();

    /**
     * @return the validator that is responsible for custom field validation.
     */
    CustomFieldMapperValidator getCustomFieldMapperValidator();

    /**
     * @return the validator that is responsible for priority validation.
     */
    PriorityMapperValidator getPriorityMapperValidator();

    /**
     * @return the validator that is responsible for resolution validation.
     */
    ResolutionMapperValidator getResolutionMapperValidator();

    /**
     * @return the validator that is responsible for status validation.
     */
    StatusMapperValidator getStatusMapperValidator();

    /**
     * @return the validator that is responsible for CustomField Option validation.
     */
    CustomFieldOptionMapperValidator getCustomFieldOptionMapperValidator();

    /**
     * @return the validator that is responsible for ProjectRole validation.
     */
    ProjectRoleMapperValidator getProjectRoleMapperValidator();

    /**
     * @return the validator that is responsible for Group validation.
     */
    GroupMapperValidator getGroupMapperValidator();

    /**
     * @return the validator that is responsible for IssueLinkType validation.
     */
    IssueLinkTypeMapperValidator getIssueLinkTypeMapperValidator();

    /**
     * @return the validator that is responsible for User validation
     */
    UserMapperValidator getUserMapperValidator();

    /**
     * @return the validator that is responsible for the Project Role Actor validation
     */
    ProjectRoleActorMapperValidator getProjectRoleActorMapperValidator();

    /**
     * @return the validator that is responsible for the issue security level validation
     */
    IssueSecurityLevelValidator getIssueSecurityLevelValidator();
}
