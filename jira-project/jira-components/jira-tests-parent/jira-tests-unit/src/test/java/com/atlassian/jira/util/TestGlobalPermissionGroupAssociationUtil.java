package com.atlassian.jira.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.MockGroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.MockGroup;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests GlobalPermissionGroupAssociationUtil
 *
 * @since v3.12
 */
public class TestGlobalPermissionGroupAssociationUtil
{
    @Test
    public void testGetMemberGroupNames()
    {
        final ApplicationUser testUser = new MockApplicationUser("testGetMemberGroupNames");

        final List<String> permGroups = Arrays.asList("group1", "group2");
        GlobalPermissionManager globalPermissionManager = mock(GlobalPermissionManager.class);
        when(globalPermissionManager.getGroupNames(Permissions.SYSTEM_ADMIN)).thenReturn(permGroups);

        MockGroupManager mockGroupManager = new MockGroupManager();
        mockGroupManager.addMember("group1", "testGetMemberGroupNames");

        final GlobalPermissionGroupAssociationUtil groupAssociationUtil = new GlobalPermissionGroupAssociationUtil(
                globalPermissionManager, mockGroupManager);
        final Collection memberGroups = groupAssociationUtil.getMemberGroupNames(testUser, Permissions.SYSTEM_ADMIN);
        assertNotNull(memberGroups);
        assertEquals(1, memberGroups.size());
        assertEquals("group1", memberGroups.iterator().next());
    }

    @Test
    public void testGetMemberGroupNamesNoGroups()
    {
        final ApplicationUser testUser = new MockApplicationUser("testGetMemberGroupNamesNoGroups");

        final List<String> permGroups = Arrays.asList("group1", "group2");
        GlobalPermissionManager globalPermissionManager = mock(GlobalPermissionManager.class);
        when(globalPermissionManager.getGroupNames(Permissions.SYSTEM_ADMIN)).thenReturn(permGroups);

        final GlobalPermissionGroupAssociationUtil groupAssociationUtil = new GlobalPermissionGroupAssociationUtil(
                globalPermissionManager, new MockGroupManager());
        final Collection memberGroups = groupAssociationUtil.getMemberGroupNames(testUser, Permissions.SYSTEM_ADMIN);
        assertNotNull(memberGroups);
        assertEquals(0, memberGroups.size());
    }

    @Test
    public void testGetAdminMemberGroups()
            throws OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        final ApplicationUser testUser = new MockApplicationUser("testGetAdminMemberGroups");

        final GlobalPermissionGroupAssociationUtil groupAssociationUtil = new GlobalPermissionGroupAssociationUtil(null, null)
        {
            @Override
            Collection<String> getMemberGroupNames(final ApplicationUser user, final int permissionType)
            {
                assertEquals(Permissions.ADMINISTER, permissionType);
                return null;
            }
        };

        groupAssociationUtil.getAdminMemberGroups(testUser);
    }

    @Test
    public void testGetSysAdminMemberGroups()
            throws OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        final ApplicationUser testUser = new MockApplicationUser("testGetSysAdminMemberGroups");

        final GlobalPermissionGroupAssociationUtil groupAssociationUtil = new GlobalPermissionGroupAssociationUtil(null, null)
        {
            @Override
            Collection<String> getMemberGroupNames(final ApplicationUser user, final int permissionType)
            {
                assertEquals(Permissions.SYSTEM_ADMIN, permissionType);
                return null;
            }
        };

        groupAssociationUtil.getSysAdminMemberGroups(testUser);
    }

    @Test
    public void testIsRemovingAlMyAdminGroupsAreRemoving()
    {
        final List groupsToLeave = Arrays.asList("group1", "group2");
        final GlobalPermissionGroupAssociationUtil groupAssociationUtil = new GlobalPermissionGroupAssociationUtil(null, null)
        {

            @Override
            public Collection<String> getAdminMemberGroups(final ApplicationUser user)
            {
                return new ArrayList<String>(Arrays.asList("group1", "group2"));
            }
        };

        assertTrue(groupAssociationUtil.isRemovingAllMyAdminGroups(groupsToLeave, null));
    }

    @Test
    public void testIsRemovingAlMyAdminGroupsAreNotRemoving()
    {
        final List groupsToLeave = Arrays.asList("group1", "group2");
        final GlobalPermissionGroupAssociationUtil groupAssociationUtil = new GlobalPermissionGroupAssociationUtil(null, null)
        {

            @Override
            public Collection<String> getAdminMemberGroups(final ApplicationUser user)
            {
                return new ArrayList<String>(Arrays.asList("group1", "group2", "group3"));
            }
        };

        assertFalse(groupAssociationUtil.isRemovingAllMyAdminGroups(groupsToLeave, null));
    }

    @Test
    public void testIsRemovingAlMySysAdminGroupsAreRemoving()
    {
        final List groupsToLeave = Arrays.asList("group1", "group2");
        final GlobalPermissionGroupAssociationUtil groupAssociationUtil = new GlobalPermissionGroupAssociationUtil(null, null)
        {

            @Override
            public Collection<String> getSysAdminMemberGroups(final ApplicationUser user)
            {
                return new ArrayList<String>(Arrays.asList("group1", "group2"));
            }
        };

        assertTrue(groupAssociationUtil.isRemovingAllMySysAdminGroups(groupsToLeave, null));
    }

    @Test
    public void testIsRemovingAlMySysAdminGroupsAreNotRemoving()
    {
        final List groupsToLeave = Arrays.asList("group1", "group2");
        final GlobalPermissionGroupAssociationUtil groupAssociationUtil = new GlobalPermissionGroupAssociationUtil(null, null)
        {

            @Override
            public Collection<String> getSysAdminMemberGroups(final ApplicationUser user)
            {
                return new ArrayList<String>(Arrays.asList("group1", "group2", "group3"));
            }
        };

        assertFalse(groupAssociationUtil.isRemovingAllMySysAdminGroups(groupsToLeave, null));
    }

    @Test
    public void testIsUserAbleToDeleteGroupHasGlobalAdminPerm()
    {
        GlobalPermissionManager globalPermissionManager = mock(GlobalPermissionManager.class);
        when(globalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN)).thenReturn(true);

        final GlobalPermissionGroupAssociationUtil groupAssociationUtil = new GlobalPermissionGroupAssociationUtil(
            globalPermissionManager, null);
        assertTrue(groupAssociationUtil.isUserAbleToDeleteGroup(null, "testgroup"));
    }

    @Test
    public void testIsUserAbleToDeleteGroupGroupNotInSysAdmins()
    {
        GlobalPermissionManager globalPermissionManager = mock(GlobalPermissionManager.class);
        when(globalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN)).thenReturn(false);
        when(globalPermissionManager.getGroupNames(Permissions.SYSTEM_ADMIN)).thenReturn(Arrays.asList("othergroup"));

        final GlobalPermissionGroupAssociationUtil groupAssociationUtil = new GlobalPermissionGroupAssociationUtil(
                globalPermissionManager, null);
        assertTrue(groupAssociationUtil.isUserAbleToDeleteGroup(null, "testgroup"));
    }

    @Test
    public void testIsUserAbleToDeleteGroupNotSysAdminWithSysAdminGroup()
    {
        GlobalPermissionManager globalPermissionManager = mock(GlobalPermissionManager.class);
        when(globalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN)).thenReturn(false);
        when(globalPermissionManager.getGroupNames(Permissions.SYSTEM_ADMIN)).thenReturn(Arrays.asList("testgroup"));

        final GlobalPermissionGroupAssociationUtil groupAssociationUtil = new GlobalPermissionGroupAssociationUtil(
            globalPermissionManager, null);
        assertFalse(groupAssociationUtil.isUserAbleToDeleteGroup(null, "testgroup"));
    }

    @Test
    public void testGetGroupNamesModifiableByCurrentUserHasPerm()
    {
        GlobalPermissionManager globalPermissionManager = mock(GlobalPermissionManager.class);
        when(globalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN)).thenReturn(true);

        final GlobalPermissionGroupAssociationUtil groupAssociationUtil = new GlobalPermissionGroupAssociationUtil(
            globalPermissionManager, null);
        final Collection visibleGroups = groupAssociationUtil.getGroupNamesModifiableByCurrentUser(null, Arrays.asList("testgroup1", "testgroup2"));
        assertEquals(2, visibleGroups.size());
        assertTrue(visibleGroups.contains("testgroup1"));
        assertTrue(visibleGroups.contains("testgroup2"));
    }

    @Test
    public void testGetGroupNamesModifiableByCurrentUserHasNoPerm()
    {
        GlobalPermissionManager globalPermissionManager = mock(GlobalPermissionManager.class);
        when(globalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN)).thenReturn(false);
        when(globalPermissionManager.getGroupNames(Permissions.SYSTEM_ADMIN)).thenReturn(Arrays.asList("testgroup2"));

        final GlobalPermissionGroupAssociationUtil groupAssociationUtil = new GlobalPermissionGroupAssociationUtil(
            globalPermissionManager, null);
        final Collection visibleGroups = groupAssociationUtil.getGroupNamesModifiableByCurrentUser(null, Arrays.asList("testgroup1", "testgroup2"));
        assertEquals(1, visibleGroups.size());
        assertTrue(visibleGroups.contains("testgroup1"));
        assertFalse(visibleGroups.contains("testgroup2"));
    }

    @Test
    public void testGetGroupsModifiableByCurrentUserHasPerm()
            throws OperationNotPermittedException, InvalidGroupException
    {
        GlobalPermissionManager globalPermissionManager = mock(GlobalPermissionManager.class);
        when(globalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN)).thenReturn(true);

        final Group group1 = new MockGroup("testgroup1");
        final Group group2 = new MockGroup("testgroup2");

        final GlobalPermissionGroupAssociationUtil groupAssociationUtil = new GlobalPermissionGroupAssociationUtil(
            globalPermissionManager, null);
        final Collection visibleGroups = groupAssociationUtil.getGroupsModifiableByCurrentUser(null, Arrays.asList(group1, group2));
        assertEquals(2, visibleGroups.size());
        assertTrue(visibleGroups.contains(group1));
        assertTrue(visibleGroups.contains(group2));
    }

    @Test
    public void testGetGroupsModifiableByCurrentUserHasNoPerm()
            throws OperationNotPermittedException, InvalidGroupException
    {
        final Group group1 = new MockGroup("testgroup1");
        final Group group2 = new MockGroup("testgroup2");

        GlobalPermissionManager globalPermissionManager = mock(GlobalPermissionManager.class);
        when(globalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN)).thenReturn(false);
        when(globalPermissionManager.getGroupsWithPermission(Permissions.SYSTEM_ADMIN)).thenReturn(Arrays.asList(group2));

        final GlobalPermissionGroupAssociationUtil groupAssociationUtil = new GlobalPermissionGroupAssociationUtil(
            globalPermissionManager, null);
        final Collection visibleGroups = groupAssociationUtil.getGroupsModifiableByCurrentUser(null, Arrays.asList(group1, group2));
        assertEquals(1, visibleGroups.size());
        assertTrue(visibleGroups.contains(group1));
        assertFalse(visibleGroups.contains(group2));
    }

}
