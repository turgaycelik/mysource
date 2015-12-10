package com.atlassian.jira.sharing;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.sharing.SharedEntity.SharePermissions;
import com.atlassian.jira.sharing.type.ShareType;
import com.atlassian.jira.sharing.type.ShareTypeFactory;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.dbc.Assertions;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Default implementation of the {@link ShareManager}.
 *
 * @since v3.13
 */
public class DefaultShareManager implements ShareManager
{
    private final SharePermissionStore store;
    private final ShareTypeFactory shareTypeFactory;
    private final SharePermissionReindexer reindexer;

    public DefaultShareManager(final SharePermissionStore store, final ShareTypeFactory shareTypeFactory, final SharePermissionReindexer reindexer)
    {
        notNull("Can not instantiate a ShareManager with a 'null' SharePermissionStore", store);
        notNull("Can not instantiate a ShareManager with a 'null' ShareTypeFactory", shareTypeFactory);
        notNull("Can not instantiate a ShareManager with a 'null' SharePermissionReindexer", reindexer);

        this.store = store;
        this.shareTypeFactory = shareTypeFactory;
        this.reindexer = reindexer;
    }

    public SharePermissions getSharePermissions(final SharedEntity entity)
    {
        return store.getSharePermissions(entity);
    }

    public void deletePermissions(final SharedEntity entity)
    {
        store.deleteSharePermissions(entity);
    }

    public void deleteSharePermissionsLike(final SharePermission permission)
    {
        store.deleteSharePermissionsLike(permission);
        reindexer.reindex(permission);
    }

    public SharePermissions updateSharePermissions(final SharedEntity entity)
    {
        Assertions.notNull("entity", entity);
        Assertions.notNull("entity.sharePermissions", entity.getPermissions());

        if (entity.getPermissions().isPrivate())
        {
            store.deleteSharePermissions(entity);
            return SharePermissions.PRIVATE;
        }
        return store.storeSharePermissions(entity);
    }

    @Deprecated
    public boolean hasPermission(final User user, final SharedEntity entity)
    {
        return isSharedWith(ApplicationUsers.from(user), entity);
    }

    @Override
    public boolean isSharedWith(ApplicationUser user, SharedEntity sharedEntity)
    {
        Assertions.notNull("entity", sharedEntity);

        if (sharedEntity.getOwner() != null)
        {
            if (sharedEntity.getOwner().equals(user))
            {
                return true;
            }
        }
        else if (user == null)
        {
            return true;
        }
        final SharePermissions permissions = store.getSharePermissions(sharedEntity);
        if (permissions != null)
        {
            for (final SharePermission sharePermission : permissions)
            {
                final ShareType type = shareTypeFactory.getShareType(sharePermission.getType());
                if ((type != null) && type.getPermissionsChecker().hasPermission(ApplicationUsers.toDirectoryUser(user), sharePermission))
                {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean isSharedWith(User user, SharedEntity sharedEntity)
    {
        return isSharedWith(ApplicationUsers.from(user), sharedEntity);
    }
}
