package com.atlassian.jira.sharing.type;

/**
 * Represents sharing a {@link com.atlassian.jira.sharing.SharedEntity} with a all users that can browse a given project or is in a specified
 * project role
 * 
 * @since v3.13
 */
public class ProjectShareType extends AbstractShareType
{
    public static final Name TYPE = ShareType.Name.PROJECT;
    private static final int PRIORITY = 3;

    public ProjectShareType(final ProjectShareTypeRenderer renderer, final ProjectShareTypeValidator validator, final ProjectShareTypePermissionChecker permissionChecker, final ProjectShareQueryFactory queryFactory, final ProjectSharePermissionComparator comparator)
    {
        super(ProjectShareType.TYPE, false, ProjectShareType.PRIORITY, renderer, validator, permissionChecker, queryFactory, comparator);
    }
}
