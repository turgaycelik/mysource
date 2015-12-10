/**
 * Copyright 2008 Atlassian Pty Ltd 
 */
package com.atlassian.jira.sharing.index;

import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.SharedEntity.SharePermissions;
import com.atlassian.jira.sharing.type.PrivateShareQueryFactory;
import com.atlassian.jira.sharing.type.ShareQueryFactory;
import com.atlassian.jira.sharing.type.ShareType;
import com.atlassian.jira.sharing.type.ShareTypeFactory;
import com.atlassian.jira.util.dbc.Assertions;
import com.google.common.collect.ImmutableList;
import org.apache.lucene.document.Field;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @since v3.13
 */
public class ShareTypePermissionsFieldFactory implements SharedEntityFieldFactory
{
    /**
     * Get the Field for Private SharePermissions
     */
    static final PrivateShareQueryFactory PRIVATE = new PrivateShareQueryFactory()
    {};

    static interface PermissionFieldFactory
    {
        Field getField();
    }

    private final ShareTypeFactory shareTypeFactory;

    public ShareTypePermissionsFieldFactory(final ShareTypeFactory shareTypeFactory)
    {
        this.shareTypeFactory = shareTypeFactory;
    }

    public String getFieldName()
    {
        return null;
    }

    public Collection<Field> getField(final SharedEntity entity)
    {
        final SharePermissions permissions = entity.getPermissions();
        if (permissions.isPrivate())
        {
            return ImmutableList.of(ShareTypePermissionsFieldFactory.PRIVATE.getField(entity, null));
        }
        final List<Field> result = new ArrayList<Field>(permissions.size());
        for (final SharePermission permission : permissions)
        {
            final ShareType shareType = shareTypeFactory.getShareType(permission.getType());
            Assertions.notNull("shareType", shareType);
            final ShareQueryFactory<?> queryFactory = shareType.getQueryFactory();
            result.add(queryFactory.getField(entity, permission));
        }
        return result;
    }
}
