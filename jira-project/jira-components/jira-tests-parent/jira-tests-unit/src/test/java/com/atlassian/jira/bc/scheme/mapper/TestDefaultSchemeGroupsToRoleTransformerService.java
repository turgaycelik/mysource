package com.atlassian.jira.bc.scheme.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.atlassian.core.util.collection.EasyList;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class TestDefaultSchemeGroupsToRoleTransformerService
{
    @Test
    public void testGetGroupsWithGlobalUsePermission()
    {
        List groupsWithUsePerm = EasyList.build("HAS_USE_PERM_1", "HAS_USE_PERM_2");
        SchemeGroupsToRoleTransformerService service = new MySchemeGroupsToRoleTransformerService(groupsWithUsePerm);

        List miscGroups = EasyList.build("TEST GROUP", "ANOTHER TEST GROUP", "GROUP 451");
        miscGroups.addAll(groupsWithUsePerm);

        Collection resultGroups = service.getGroupsWithGlobalUsePermission(miscGroups);
        assertTrue(resultGroups.containsAll(groupsWithUsePerm));
        assertEquals(groupsWithUsePerm.size(), resultGroups.size());
    }

    @Test
    public void testGetGroupsWithGlobalUsePermissionNonePresent()
    {
        List groupsWithUsePerm = Collections.EMPTY_LIST;
        SchemeGroupsToRoleTransformerService service = new MySchemeGroupsToRoleTransformerService(groupsWithUsePerm);

        List miscGroups = EasyList.build("TEST GROUP", "ANOTHER TEST GROUP", "GROUP 451");
        miscGroups.addAll(groupsWithUsePerm);

        Collection resultGroups = service.getGroupsWithGlobalUsePermission(miscGroups);
        assertTrue(resultGroups.isEmpty());        
    }

    @Test
    public void testGetGroupsWithoutGlobalUsePermission()
    {
        List groupsWithUsePerm = EasyList.build("HAS_USE_PERM_1", "HAS_USE_PERM_2", "HAS_USE_PERM_3");
        SchemeGroupsToRoleTransformerService service = new MySchemeGroupsToRoleTransformerService(groupsWithUsePerm);

        List groupsWithoutUsePerm = EasyList.build("TEST GROUP", "ANOTHER TEST GROUP");

        List miscGroups = new ArrayList();
        miscGroups.addAll(groupsWithUsePerm);
        miscGroups.addAll(groupsWithoutUsePerm);

        Collection resultGroups = service.getGroupsWithoutGlobalUsePermission(miscGroups);
        assertTrue(resultGroups.containsAll(groupsWithoutUsePerm));
        assertEquals(groupsWithoutUsePerm.size(), resultGroups.size());
    }

    @Test
    public void testGetGroupsWithoutGlobalUsePermissionNonePresent()
    {
        List groupsWithUsePerm = EasyList.build("HAS_USE_PERM_1", "HAS_USE_PERM_2", "HAS_USE_PERM_3");
        SchemeGroupsToRoleTransformerService service = new MySchemeGroupsToRoleTransformerService(groupsWithUsePerm);

        List groupsWithoutUsePerm = Collections.EMPTY_LIST;

        List miscGroups = new ArrayList();
        miscGroups.addAll(groupsWithUsePerm);
        miscGroups.addAll(groupsWithoutUsePerm);

        Collection resultGroups = service.getGroupsWithoutGlobalUsePermission(miscGroups);
        assertTrue(resultGroups.isEmpty());        
    }

    @Test
    public void testIsAnyGroupGrantedGlobalUsePermissionTrue()
    {
        List groupsWithUsePerm = EasyList.build("HAS_USE_PERM_1", "HAS_USE_PERM_2", "HAS_USE_PERM_3");
        SchemeGroupsToRoleTransformerService service = new MySchemeGroupsToRoleTransformerService(groupsWithUsePerm);

        List groupsWithoutUsePerm = EasyList.build("TEST GROUP", "ANOTHER TEST GROUP");

        List miscGroups = new ArrayList();
        miscGroups.addAll(groupsWithUsePerm);
        miscGroups.addAll(groupsWithoutUsePerm);

        assertTrue(service.isAnyGroupGrantedGlobalUsePermission(miscGroups));
    }

    @Test
    public void testIsAnyGroupGrantedGlobalUsePermissionFalse()
    {
        List groupsWithUsePerm = EasyList.build();
        SchemeGroupsToRoleTransformerService service = new MySchemeGroupsToRoleTransformerService(groupsWithUsePerm);

        List groupsWithoutUsePerm = EasyList.build("TEST GROUP", "ANOTHER TEST GROUP", "TEST TEST LOVELY TESTS");

        List miscGroups = new ArrayList();
        miscGroups.addAll(groupsWithUsePerm);
        miscGroups.addAll(groupsWithoutUsePerm);

        assertFalse(service.isAnyGroupGrantedGlobalUsePermission(miscGroups));
    }

    private class MySchemeGroupsToRoleTransformerService extends DefaultSchemeGroupsToRoleTransformerService
    {
        Set grantedGlobalUsePermission;

        public MySchemeGroupsToRoleTransformerService(Collection groupsWithGlobalUsePermission)
        {
            super(null, null, null, null);
            grantedGlobalUsePermission = new HashSet(groupsWithGlobalUsePermission);
        }

        public boolean isGroupGrantedGlobalUsePermission(String groupName)
        {
            return grantedGlobalUsePermission.contains(groupName);
        }
    }
}
