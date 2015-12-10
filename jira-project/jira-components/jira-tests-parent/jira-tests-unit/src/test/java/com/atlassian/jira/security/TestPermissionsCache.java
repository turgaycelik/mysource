package com.atlassian.jira.security;

import java.util.Collection;
import java.util.List;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.collect.CollectionBuilder;

import com.google.common.collect.Lists;

import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class TestPermissionsCache
{
    @Test
    public void testPermissionsCacheNullUser()
    {
        final ProjectFactory mockProjectFactory = createMock(ProjectFactory.class);
        final MockGenericValue mockProject1 = new MockGenericValue("project", EasyMap.build("id", new Long(1000)));
        final MockGenericValue mockProject2 = new MockGenericValue("project", EasyMap.build("id", new Long(1002)));
        final List projectList = EasyList.build(mockProject1, mockProject2);

        expect(mockProjectFactory.getProject(mockProject1)).andReturn(new MockProject(1000));
        expect(mockProjectFactory.getProject(mockProject2)).andReturn(new MockProject(1002));

        replay(mockProjectFactory);
        PermissionsCache permissionsCache = new PermissionsCache(mockProjectFactory);
        permissionsCache.setProjectsWithBrowsePermission(null, projectList);
        Collection projects = permissionsCache.getProjectsWithBrowsePermission(null);

        assertNotNull(projects);
        assertEquals(2, projects.size());
        assertTrue(projects.contains(mockProject1));
        assertTrue(projects.contains(mockProject2));

        User user = new MockUser("test");
        projects = permissionsCache.getProjectsWithBrowsePermission(user);
        assertNull(projects);
        verify(mockProjectFactory);
    }

    @Test
    public void testSetProjectObjects()
    {
        final MockGenericValue mockProject1 = new MockGenericValue("project", EasyMap.build("id", new Long(1000)));
        final Project mockProjectObject = new MockProject(mockProject1);

        PermissionsCache permissionsCache = new PermissionsCache(null);
        permissionsCache.setProjectObjectsWithBrowsePermission(null, CollectionBuilder.newBuilder(mockProjectObject).asList());

        final Collection<Project> projects = permissionsCache.getProjectObjectsWithBrowsePermission(null);
        assertEquals(1, projects.size());
        assertEquals(Long.valueOf(1000), projects.iterator().next().getId());

        final Collection<GenericValue> projectGvs = permissionsCache.getProjectsWithBrowsePermission(null);
        assertEquals(mockProject1, projectGvs.iterator().next());
    }

    @Test
    public void testPermissionsCache()
    {
        final ProjectFactory mockProjectFactory = createMock(ProjectFactory.class);

        final MockGenericValue mockProject1 = new MockGenericValue("project", EasyMap.build("id", new Long(1000)));
        final MockGenericValue mockProject2 = new MockGenericValue("project", EasyMap.build("id", new Long(1002)));
        final MockGenericValue mockProject3 = new MockGenericValue("project", EasyMap.build("id", new Long(1003)));
        final MockGenericValue mockProject4 = new MockGenericValue("project", EasyMap.build("id", new Long(1004)));
        User user = new MockUser("test");
        User user2 = new MockUser("test2");
        final List projectList = EasyList.build(mockProject1, mockProject2);

        expect(mockProjectFactory.getProject(mockProject1)).andReturn(new MockProject(1000));
        expect(mockProjectFactory.getProject(mockProject2)).andReturn(new MockProject(1002));
        expect(mockProjectFactory.getProject(mockProject3)).andReturn(new MockProject(1003));
        expect(mockProjectFactory.getProject(mockProject4)).andReturn(new MockProject(1004));

        replay(mockProjectFactory);
        PermissionsCache permissionsCache = new PermissionsCache(mockProjectFactory);
        permissionsCache.setProjectsWithBrowsePermission(null, projectList);
        permissionsCache.setProjectsWithBrowsePermission(user, Lists.<GenericValue>newArrayList(mockProject3));
        permissionsCache.setProjectsWithBrowsePermission(user2, Lists.<GenericValue>newArrayList(mockProject4));

        Collection projects = permissionsCache.getProjectsWithBrowsePermission(null);

        assertNotNull(projects);
        assertEquals(2, projects.size());
        assertTrue(projects.contains(mockProject1));
        assertTrue(projects.contains(mockProject2));

        projects = permissionsCache.getProjectsWithBrowsePermission(user);
        assertNotNull(projects);
        assertEquals(1, projects.size());
        assertTrue(projects.contains(mockProject3));

        projects = permissionsCache.getProjectsWithBrowsePermission(user2);
        assertNotNull(projects);
        assertEquals(1, projects.size());
        assertTrue(projects.contains(mockProject4));

        final Collection<Project> projectObjects = permissionsCache.getProjectObjectsWithBrowsePermission(user);
        assertNotNull(projects);
        assertEquals(1, projects.size());
        assertEquals(Long.valueOf(1003), projectObjects.iterator().next().getId());

        verify(mockProjectFactory);
    }

    @Test
    public void testGetNullFromPermissionCache()
    {
        PermissionsCache permissionsCache = new PermissionsCache(null);
        Collection projects = permissionsCache.getProjectsWithBrowsePermission(null);
        assertNull(projects);

        User user = new MockUser("test");
        projects = permissionsCache.getProjectsWithBrowsePermission(user);
        assertNull(projects);
    }
}
