package com.atlassian.jira.imports.project.validation;

/**
 * @since v3.13
 */
public class ProjectImportValidatorsImpl implements ProjectImportValidators
{
    private final IssueTypeMapperValidator issueTypeMapperValidator;
    private final CustomFieldMapperValidator customFieldMapperValidator;
    private final PriorityMapperValidator priorityMapperValidator;
    private final ResolutionMapperValidator resolutionMapperValidator;
    private final StatusMapperValidator statusMapperValidator;
    private final CustomFieldOptionMapperValidator customFieldOptionMapperValidator;
    private final ProjectRoleMapperValidator projectRoleMapperValidator;
    private final GroupMapperValidator groupMapperValidator;
    private final IssueLinkTypeMapperValidator issueLinkTypeMapperValidator;
    private final UserMapperValidator userMapperValidator;
    private final ProjectRoleActorMapperValidator projectRoleActorMapperValidator;
    private final IssueSecurityLevelValidator issueSecurityLevelValidator;

    public ProjectImportValidatorsImpl(final IssueTypeMapperValidator issueTypeMapperValidator, final CustomFieldMapperValidator customFieldMapperValidator, final PriorityMapperValidator priorityMapperValidator, final ResolutionMapperValidator resolutionMapperValidator, final StatusMapperValidator statusMapperValidator, final CustomFieldOptionMapperValidator customFieldOptionMapperValidator, final ProjectRoleMapperValidator projectRoleMapperValidator, final GroupMapperValidator groupMapperValidator, final IssueLinkTypeMapperValidator issueLinkTypeMapperValidator, final UserMapperValidator userMapperValidator, final ProjectRoleActorMapperValidator projectRoleActorMapperValidator, final IssueSecurityLevelValidator issueSecurityLevelValidator)
    {
        this.issueTypeMapperValidator = issueTypeMapperValidator;
        this.customFieldMapperValidator = customFieldMapperValidator;
        this.priorityMapperValidator = priorityMapperValidator;
        this.resolutionMapperValidator = resolutionMapperValidator;
        this.statusMapperValidator = statusMapperValidator;
        this.customFieldOptionMapperValidator = customFieldOptionMapperValidator;
        this.projectRoleMapperValidator = projectRoleMapperValidator;
        this.groupMapperValidator = groupMapperValidator;
        this.issueLinkTypeMapperValidator = issueLinkTypeMapperValidator;
        this.userMapperValidator = userMapperValidator;
        this.projectRoleActorMapperValidator = projectRoleActorMapperValidator;
        this.issueSecurityLevelValidator = issueSecurityLevelValidator;
    }

    public IssueTypeMapperValidator getIssueTypeMapperValidator()
    {
        return issueTypeMapperValidator;
    }

    public CustomFieldMapperValidator getCustomFieldMapperValidator()
    {
        return customFieldMapperValidator;
    }

    public PriorityMapperValidator getPriorityMapperValidator()
    {
        return priorityMapperValidator;
    }

    public ResolutionMapperValidator getResolutionMapperValidator()
    {
        return resolutionMapperValidator;
    }

    public StatusMapperValidator getStatusMapperValidator()
    {
        return statusMapperValidator;
    }

    public CustomFieldOptionMapperValidator getCustomFieldOptionMapperValidator()
    {
        return customFieldOptionMapperValidator;
    }

    public ProjectRoleMapperValidator getProjectRoleMapperValidator()
    {
        return projectRoleMapperValidator;
    }

    public GroupMapperValidator getGroupMapperValidator()
    {
        return groupMapperValidator;
    }

    public IssueLinkTypeMapperValidator getIssueLinkTypeMapperValidator()
    {
        return issueLinkTypeMapperValidator;
    }

    public UserMapperValidator getUserMapperValidator()
    {
        return userMapperValidator;
    }

    public ProjectRoleActorMapperValidator getProjectRoleActorMapperValidator()
    {
        return projectRoleActorMapperValidator;
    }

    public IssueSecurityLevelValidator getIssueSecurityLevelValidator()
    {
        return issueSecurityLevelValidator;
    }
}
