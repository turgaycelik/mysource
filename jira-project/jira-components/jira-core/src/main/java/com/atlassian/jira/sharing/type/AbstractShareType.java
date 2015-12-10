package com.atlassian.jira.sharing.type;

import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.search.ShareTypeSearchParameter;

import java.util.Comparator;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Abstract class for ShareType classes.
 *
 * @since v3.13
 */
public class AbstractShareType implements ShareType
{
    private final Name type;
    private final ShareTypeValidator validator;
    private final ShareTypeRenderer renderer;
    private final ShareTypePermissionChecker permissionChecker;
    private final ShareQueryFactory<? extends ShareTypeSearchParameter> queryFactory;
    private final Comparator<SharePermission> comparator;
    private final boolean singleton;
    private final int priority;

    public AbstractShareType(final Name type, final boolean singleton, final int priority, final ShareTypeRenderer renderer, final ShareTypeValidator validator, final ShareTypePermissionChecker permissionChecker, final ShareQueryFactory<? extends ShareTypeSearchParameter> queryFactory, final Comparator<SharePermission> comparator)
    {
        this.type = notNull("type", type);
        this.renderer = notNull("renderer", renderer);
        this.validator = notNull("validator", validator);
        this.permissionChecker = notNull("permissionChecker", permissionChecker);
        this.comparator = notNull("comparator", comparator);
        this.queryFactory = notNull("queryFactory", queryFactory);
        this.singleton = singleton;
        this.priority = priority;
    }

    public Name getType()
    {
        return type;
    }

    public boolean isSingleton()
    {
        return singleton;
    }

    public int getPriority()
    {
        return priority;
    }

    public ShareTypeRenderer getRenderer()
    {
        return renderer;
    }

    public ShareTypeValidator getValidator()
    {
        return validator;
    }

    public ShareTypePermissionChecker getPermissionsChecker()
    {
        return permissionChecker;
    }

    public Comparator<SharePermission> getComparator()
    {
        return comparator;
    }

    public ShareQueryFactory<? extends ShareTypeSearchParameter> getQueryFactory()
    {
        return queryFactory;
    }
}
