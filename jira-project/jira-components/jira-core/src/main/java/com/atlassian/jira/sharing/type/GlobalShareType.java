package com.atlassian.jira.sharing.type;

/**
 * Implementation of the ShareType that allows a {@link com.atlassian.jira.sharing.SharedEntity} to be shared with all users on a JIRA instance.
 * This includes the sharing with the anonymous user.
 * 
 * @since v3.13
 */
public class GlobalShareType extends AbstractShareType
{
    public static final Name TYPE = ShareType.Name.GLOBAL;
    public static final int PRIORITY = 1;

    public GlobalShareType(final GlobalShareTypeRenderer renderer, final GlobalShareTypeValidator validator)
    {
        super(GlobalShareType.TYPE, true, GlobalShareType.PRIORITY, renderer, validator, new GlobalShareTypePermissionChecker(),
            new GlobalShareQueryFactory(), new DefaultSharePermissionComparator(GlobalShareType.TYPE));
    }
}
