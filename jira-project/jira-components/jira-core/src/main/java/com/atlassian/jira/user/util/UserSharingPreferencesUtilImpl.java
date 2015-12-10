package com.atlassian.jira.user.util;

import com.atlassian.core.user.preferences.Preferences;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.sharing.SharedEntity.SharePermissions;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.util.dbc.Assertions;

/**
 * Standard implementation of the {@link UserSharingPreferencesUtil} interface.
 *
 * @since v3.13
 */
public class UserSharingPreferencesUtilImpl implements UserSharingPreferencesUtil
{
    private final PermissionManager permissionManager;
    private final UserPreferencesManager userPreferencesManager;

    public UserSharingPreferencesUtilImpl(final PermissionManager permissionManager, final UserPreferencesManager userPreferencesManager)
    {
        Assertions.notNull("permissionManager", permissionManager);
        Assertions.notNull("userPreferencesManager", userPreferencesManager);

        this.userPreferencesManager = userPreferencesManager;
        this.permissionManager = permissionManager;
    }

    public SharePermissions getDefaultSharePermissions(final User user)
    {
        SharePermissions returnPermissions = SharePermissions.PRIVATE;
        if ((user != null) && permissionManager.hasPermission(Permissions.CREATE_SHARED_OBJECTS, user))
        {
            final Preferences userPreferences = userPreferencesManager.getPreferences(user);
            if (!userPreferences.getBoolean(PreferenceKeys.USER_DEFAULT_SHARE_PRIVATE))
            {
                returnPermissions = SharePermissions.GLOBAL;
            }
        }
        return returnPermissions;
    }
}
