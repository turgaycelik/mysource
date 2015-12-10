package com.atlassian.jira.sharing;

import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.PrimitiveMap;
import com.atlassian.jira.sharing.SharedEntity.SharePermissions;
import com.atlassian.jira.sharing.type.ShareType;
import com.atlassian.jira.util.dbc.Assertions;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * OfBiz implementation of SharePermissionStore
 * 
 * @since v3.13
 */
public class OfBizSharePermissionStore implements SharePermissionStore
{
    private static final class Table
    {
        private static final String NAME = "SharePermissions";
    }

    private static final class Column
    {
        private static final String ENTITY_ID = "entityId";
        private static final String ENTITY_TYPE = "entityType";
        private static final String PARAM2 = "param2";
        private static final String PARAM1 = "param1";
        private static final String TYPE = "type";
        private static final String ID = "id";
    }

    private final OfBizDelegator delegator;

    public OfBizSharePermissionStore(final OfBizDelegator delegator)
    {
        this.delegator = delegator;
    }

    public SharePermissions getSharePermissions(final SharedEntity entity)
    {
        validate(entity);

        final Collection<GenericValue> perms = delegator.findByAnd(Table.NAME, new PrimitiveMap.Builder().add(Column.ENTITY_ID, entity.getId()).add(
            Column.ENTITY_TYPE, entity.getEntityType().getName()).toMap());

        final Set<SharePermission> returnPerms = new HashSet<SharePermission>(perms.size());
        for (final GenericValue genericValue : perms)
        {
            returnPerms.add(convertGVToSharePermission(genericValue));
        }
        return new SharePermissions(returnPerms);
    }

    public int deleteSharePermissions(final SharedEntity entity)
    {
        validate(entity);

        return delegator.removeByAnd(Table.NAME, new PrimitiveMap.Builder().add(Column.ENTITY_ID, entity.getId()).add(Column.ENTITY_TYPE,
            entity.getEntityType().getName()).toMap());
    }

    public int deleteSharePermissionsLike(final SharePermission permission)
    {
        Assertions.notNull("permission", permission);
        Assertions.notNull("permission.type", permission.getType());
        if ((permission.getParam1() == null) && (permission.getParam2() == null))
        {
            throw new IllegalArgumentException("You must provide at least a non null param1 or param2");
        }

        final PrimitiveMap.Builder params = new PrimitiveMap.Builder().add(Column.TYPE, permission.getType().get());
        if (permission.getParam1() != null)
        {
            params.add(Column.PARAM1, permission.getParam1());
        }
        if (permission.getParam2() != null)
        {
            params.add(Column.PARAM2, permission.getParam2());
        }

        return delegator.removeByAnd(Table.NAME, params.toMap());
    }

    public SharePermissions storeSharePermissions(final SharedEntity entity)
    {
        validate(entity);

        final SharePermissions permissions = entity.getPermissions();
        Assertions.notNull("permissions", permissions);

        deleteSharePermissions(entity);
        final Set<SharePermission> returnPermissions = new HashSet<SharePermission>();
        for (final SharePermission permission : permissions)
        {
            final GenericValue gv = delegator.createValue(Table.NAME, createMap(permission, entity));
            returnPermissions.add(convertGVToSharePermission(gv));
        }
        return new SharePermissions(returnPermissions);
    }

    private void validate(final SharedEntity entity)
    {
        notNull("entity", entity);
        notNull("entity.id", entity.getId());
        notNull("entity.entityType", entity.getEntityType());
    }

    private Map<String, Object> createMap(final SharePermission permission, final SharedEntity entity)
    {
        final PrimitiveMap.Builder builder = new PrimitiveMap.Builder();
        builder.add(Column.ENTITY_ID, entity.getId());
        builder.add(Column.ENTITY_TYPE, entity.getEntityType().getName());
        builder.add(Column.TYPE, permission.getType().get());
        builder.add(Column.PARAM1, permission.getParam1());
        builder.add(Column.PARAM2, permission.getParam2());
        return builder.toMap();
    }

    private SharePermissionImpl convertGVToSharePermission(final GenericValue genericValue)
    {
        return new SharePermissionImpl(genericValue.getLong(Column.ID), new ShareType.Name(genericValue.getString(Column.TYPE)),
            genericValue.getString(Column.PARAM1), genericValue.getString(Column.PARAM2));
    }
}
