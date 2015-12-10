package com.atlassian.jira.issue.search.searchers.renderer;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.atlassian.crowd.model.user.User;
import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectImpl;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.UserProjectHistoryManager;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v5.2
 */
public class TestProjectSearchRenderer
{
    private User user;
    private ProjectManager projectManager;
    private PermissionManager permissionManager;
    private UserProjectHistoryManager projectHistoryManager;
    private ProjectSearchRenderer searchRenderer;

    @Before
    public void setUp()
    {
        user = mock(User.class);
        projectManager = mock(ProjectManager.class);
        permissionManager = mock(PermissionManager.class);
        projectHistoryManager = mock(UserProjectHistoryManager.class);
        searchRenderer = new ProjectSearchRenderer(projectManager, permissionManager, null, null, null, "project", projectHistoryManager);
    }

    @Test
    public void testViewRecentDoesNotShowForThreshold() throws Exception
    {
        when(permissionManager.getProjects(anyInt(), eq(user))).thenReturn(createGvs(1));

        Map<String, Object> results = Maps.newHashMap();
        searchRenderer.addParameters(user, new FieldValuesHolderImpl(), false, results);

        assertFalse(results.containsKey("recentProjects"));
    }

    @Test
    public void testViewRecentDoesShowForAboveThreshold() throws Exception
    {
        when(permissionManager.getProjects(anyInt(), eq(user))).thenReturn(createGvs(20));
        when(projectHistoryManager.getProjectHistoryWithPermissionChecks(ProjectAction.VIEW_ISSUES, user)).thenReturn(createProjects(5));

        Map<String, Object> results = Maps.newHashMap();
        searchRenderer.addParameters(user, new FieldValuesHolderImpl(), false, results);

        assertTrue(results.containsKey("recentProjects"));
    }

    @Test
    public void testViewRecentShowsOnlyMaxProjects() throws Exception
    {
        when(permissionManager.getProjects(anyInt(), eq(user))).thenReturn(createGvs(20));
        when(projectHistoryManager.getProjectHistoryWithPermissionChecks(ProjectAction.VIEW_ISSUES, user)).thenReturn(createProjects(11));

        Map<String, Object> results = Maps.newHashMap();
        searchRenderer.addParameters(user, new FieldValuesHolderImpl(), false, results);

        assertTrue(results.containsKey("recentProjects"));
        Collection<Project> projects = (Collection<Project>) results.get("recentProjects");
        assertEquals(ProjectSearchRenderer.MAX_RECENT_PROJECTS_TO_SHOW, projects.size());
    }

    private List<Project> createProjects(int n)
    {
        List<Project> projects = Lists.newArrayList();
        for (int i = 0; i < n; ++i)
        {
            projects.add(new ProjectImpl(createGv(new Long(i), String.valueOf(i))));
        }
        return projects;
    }

    private List<GenericValue> createGvs(int n)
    {
        List<GenericValue> gvs = Lists.newArrayList();
        for (int i = 0; i < n; ++i)
        {
            gvs.add(createGv(new Long(i), String.valueOf(i)));
        }
        return gvs;
    }

    private GenericValue createGv(Long id, String name)
    {
        MockGenericValue gv = new MockGenericValue(name);
        gv.set("id", id);
        gv.set("key", name);
        gv.set("name", name);
        return gv;
    }
}
