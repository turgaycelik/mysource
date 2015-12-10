package com.atlassian.jira.sharing.type;

import com.atlassian.jira.security.groups.GroupManager;

/**
 * Represents sharing a {@link com.atlassian.jira.sharing.SharedEntity} with a group of JIRA users.
 * 
 * @since v3.13
 */
public class GroupShareType extends AbstractShareType
{
    public static final Name TYPE = ShareType.Name.GROUP;
    private static final int PRIORITY = 2;

    public GroupShareType(final GroupShareTypeRenderer renderer, final GroupShareTypeValidator validator,
            final GroupShareTypePermissionChecker permissionChecker, GroupManager groupManager)
    {
        super(GroupShareType.TYPE, false, GroupShareType.PRIORITY, renderer, validator, permissionChecker, new GroupShareQueryFactory(groupManager),
            new GroupSharePermissionComparator());
    }
}
