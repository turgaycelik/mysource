package com.atlassian.jira.crowd.embedded;

import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.manager.permission.*;
import com.atlassian.crowd.model.page.Page;
import com.atlassian.crowd.model.page.PageImpl;
import com.atlassian.crowd.model.permission.UserPermission;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * @since v6.2
 */
public class NoopUserPermissionAdminService implements UserPermissionAdminService
{

    @Override
    public void setPermissionForGroups(final List<? extends DirectoryGroup> directoryGroupPairs, final UserPermission permission)
            throws DirectoryNotFoundException, OperationFailedException, ApplicationNotFoundException,
                UserPermissionDowngradeException, AnonymousUserPermissionException
    {
    }

    @Override
    public void revokePermissionsForGroup(final DirectoryGroup group)
            throws DirectoryNotFoundException, OperationFailedException, ApplicationNotFoundException,
                UserPermissionDowngradeException, AnonymousUserPermissionException
    {
    }

    @Override
    public Page<PermittedGroup> findGroupsWithPermissionByPrefix(@Nonnull final String prefix, final int start, final int limit)
            throws UserPermissionException, AnonymousUserPermissionException
    {
        final List<PermittedGroup> emptyList = Collections.emptyList();
        return new PageImpl<PermittedGroup>(emptyList, 0, 0, 0, true);
    }

    @Override
    public Page<PermittedGroup> findGroupsWithPermission(final int start, final int limit)
            throws UserPermissionException, AnonymousUserPermissionException
    {
        final List<PermittedGroup> emptyList = Collections.emptyList();
        return new PageImpl<PermittedGroup>(emptyList, 0, 0, 0, true);
    }

    @Override
    public Page<DirectoryGroup> findGroupsByPrefix(@Nonnull final String prefix, final int start, final int limit)
            throws AnonymousUserPermissionException
    {
        final List<DirectoryGroup> emptyList = Collections.emptyList();
        return new PageImpl<DirectoryGroup>(emptyList, 0, 0, 0, true);
    }

    @Override
    public Page<DirectoryGroup> findGroups(final int start, final int limit)
            throws AnonymousUserPermissionException
    {
        final List<DirectoryGroup> emptyList = Collections.emptyList();
        return new PageImpl<DirectoryGroup>(emptyList, 0, 0, 0, true);
    }
}
