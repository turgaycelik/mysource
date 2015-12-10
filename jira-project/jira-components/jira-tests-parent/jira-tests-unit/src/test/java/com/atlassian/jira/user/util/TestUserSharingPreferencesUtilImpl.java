package com.atlassian.jira.user.util;

import com.atlassian.core.user.preferences.Preferences;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.sharing.SharedEntity.SharePermissions;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.user.preferences.UserPreferencesManager;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test for {@link UserSharingPreferencesUtilImpl}.
 *
 * @since v3.13
 */
public class TestUserSharingPreferencesUtilImpl extends MockControllerTestCase
{
    private User user;
    private PermissionManager permissionManager;
    private UserPreferencesManager userPreferencesManager;
    private Preferences preferences;

    /**
     * Ensure that passing the anonymous user works.
     */
    @Test
    public void testGetDefaultSharePermissionsNullUser()
    {
        mockController.replay();
        final UserSharingPreferencesUtil sharingPreferences = new UserSharingPreferencesUtilImpl(permissionManager, userPreferencesManager);
        assertEquals(SharePermissions.PRIVATE, sharingPreferences.getDefaultSharePermissions(null));

        mockController.verify();
    }

    /**
     * Ensure that a user without sharing permission is forced to save as private.
     */
    @Test
    public void testGetDefaultSharePermissionsNoSharingAllowed()
    {
        permissionManager.hasPermission(Permissions.CREATE_SHARED_OBJECTS, user);
        mockController.setReturnValue(false);

        mockController.replay();
        final UserSharingPreferencesUtil sharingPreferences = new UserSharingPreferencesUtilImpl(permissionManager, userPreferencesManager);
        assertEquals(SharePermissions.PRIVATE, sharingPreferences.getDefaultSharePermissions(user));

        mockController.verify();
    }

    /**
     * Ensure that a user with private setting returns private shares.
     */
    @Test
    public void testGetDefaultSharePermissionsDefaultPrivate()
    {
        permissionManager.hasPermission(Permissions.CREATE_SHARED_OBJECTS, user);
        mockController.setReturnValue(true);

        preferences.getBoolean(PreferenceKeys.USER_DEFAULT_SHARE_PRIVATE);
        mockController.setReturnValue(true);

        userPreferencesManager.getPreferences(user);
        mockController.setReturnValue(preferences);

        mockController.replay();
        final UserSharingPreferencesUtil sharingPreferences = new UserSharingPreferencesUtilImpl(permissionManager, userPreferencesManager);
        assertEquals(SharePermissions.PRIVATE, sharingPreferences.getDefaultSharePermissions(user));

        mockController.verify();
    }

    /**
     * Ensure that a user with global setting returns global shares.
     */
    @Test
    public void testGetDefaultSharePermissionsDefaultPublic()
    {
        permissionManager.hasPermission(Permissions.CREATE_SHARED_OBJECTS, user);
        mockController.setReturnValue(true);

        preferences.getBoolean(PreferenceKeys.USER_DEFAULT_SHARE_PRIVATE);
        mockController.setReturnValue(false);

        userPreferencesManager.getPreferences(user);
        mockController.setReturnValue(preferences);

        mockController.replay();
        final UserSharingPreferencesUtil sharingPreferences = new UserSharingPreferencesUtilImpl(permissionManager, userPreferencesManager);
        assertEquals(SharePermissions.GLOBAL, sharingPreferences.getDefaultSharePermissions(user));

        mockController.verify();
    }

    @Before
    public void setUp() throws Exception
    {
        user = new MockUser("testUser");
        permissionManager =  mockController.getMock(PermissionManager.class);
        userPreferencesManager = mockController.getMock(UserPreferencesManager.class);
        preferences = mockController.getMock(Preferences.class);
    }

}
