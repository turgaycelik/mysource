package com.atlassian.jira.security;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

import com.atlassian.fugue.Option;
import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.permission.GlobalPermissionType;
import com.atlassian.jira.security.plugin.GlobalPermissionTypesManager;

import com.google.common.collect.Lists;

/**
 * Pretends we only have system global permissions
 */
public class MockGlobalPermissionTypeManager implements GlobalPermissionTypesManager
{
    public static List<GlobalPermissionType> SYSTEM_PERMISSIONS = Lists.newArrayList(
            new GlobalPermissionType("SYSTEM_ADMIN", "admin.global.permissions.system.administer", "admin.permissions.descriptions.SYS_ADMIN", false),
            new GlobalPermissionType("ADMINISTER", "admin.global.permissions.administer", "admin.permissions.descriptions.ADMINISTER", false),
            new GlobalPermissionType("USE", "admin.global.permissions.use", "admin.permissions.descriptions.USE", false),
            new GlobalPermissionType("USER_PICKER", "admin.global.permissions.user.picker", "admin.permissions.descriptions.USER_PICKER", true),
            new GlobalPermissionType("CREATE_SHARED_OBJECTS", "admin.global.permissions.create.shared.filter", "admin.permissions.descriptions.CREATE_SHARED_OBJECTS", true),
            new GlobalPermissionType("MANAGE_GROUP_FILTER_SUBSCRIPTIONS", "admin.global.permissions.manage.group.filter.subscriptions", "admin.permissions.descriptions.MANAGE_GROUP_FILTER_SUBSCRIPTIONS", true),
            new GlobalPermissionType("BULK_CHANGE", "admin.global.permissions.bulk.change", "admin.permissions.descriptions.BULK_CHANGE", true)
    );

    @Override
    public Collection<GlobalPermissionType> getAll()
    {
        return SYSTEM_PERMISSIONS;
    }

    @Override
    public Option<GlobalPermissionType> getGlobalPermission(@Nonnull final String permissionKey)
    {
        for(GlobalPermissionType gpt: SYSTEM_PERMISSIONS)
        {
            if(gpt.getKey().equals(permissionKey))
            {
                return Option.some(gpt);
            }
        }
        return Option.none();
    }

    @Override
    public Option<GlobalPermissionType> getGlobalPermission(@Nonnull final GlobalPermissionKey permissionKey)
    {
        return getGlobalPermission(permissionKey.getKey());
    }
}
