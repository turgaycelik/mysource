package com.atlassian.jira.crowd.embedded;

import com.atlassian.crowd.dao.permission.InternalUserPermissionDAO;
import com.atlassian.crowd.manager.permission.PermittedGroup;
import com.atlassian.crowd.model.application.GroupMapping;
import com.atlassian.crowd.model.permission.InternalGrantedPermission;
import com.atlassian.crowd.model.permission.UserPermission;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @since v6.2
 */
public class NoopInternalUserPermissionDAO implements InternalUserPermissionDAO
{
    @Override
    public boolean exists(final InternalGrantedPermission permission)
    {
        return false;
    }

    @Override
    public boolean revoke(final InternalGrantedPermission permission)
    {
        return false;
    }

    @Override
    public void grant(final InternalGrantedPermission permission)
    {
    }

    @Override
    public Collection<GroupMapping> getGroupMappingsWithGrantedPermission(final UserPermission permission)
    {
        return Collections.emptyList();
    }

    @Override
    public List<PermittedGroup> findHighestPermissionPerGroupByPrefix(final String prefix, final int start, final int limit)
    {
        return Collections.emptyList();
    }

    @Override
    public List<PermittedGroup> findHighestPermissionPerGroup(final int start, final int limit)
    {
        return Collections.emptyList();
    }

    @Override
    public List<InternalGrantedPermission> findAllPermissionsForGroup(final String groupName, final long directoryId)
    {
        return Collections.emptyList();
    }
}
