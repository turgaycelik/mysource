package com.atlassian.jira.issue.search.searchers.renderer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.component.MockProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.issue.fields.option.Option;
import com.atlassian.jira.issue.fields.option.TextOption;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.transformer.ComponentSearchInput;
import com.atlassian.jira.mock.issue.search.MockSearchContext;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.Permissions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.issue.search.searchers.transformer.ComponentSearchInput.component;
import static com.atlassian.jira.issue.search.searchers.transformer.ComponentSearchInput.noComponents;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.when;

/**
 * @since v5.2
 */
@RunWith(MockitoJUnitRunner.class)
public class TestKickassComponentSearchRenderer extends TestAbstractProjectConstantsRenderer<ComponentSearchInput, ComponentOptions>
{
    @Mock
    private ProjectComponentManager projectComponentManager;

    public TestKickassComponentSearchRenderer()
    {
        super(SystemSearchConstants.forComponent().getUrlParameter());
    }

    @Before
    public void setUp()
    {
        super.setUp();

        renderer = new ComponentSearchRenderer(null, fieldVisibilityManager, projectComponentManager,
                projectManager, SystemSearchConstants.forComponent(), null, null, velocityRequestContextFactory, permissionManager);
    }

    @Override
    String[] getSpecialOptionIds()
    {
        return new String[] { ProjectComponentManager.NO_COMPONENTS };
    }

    @Test
    public void testNoProjectInContextNoPermissions()
    {
        List<Project> visibleProjects = Collections.emptyList();
        when(permissionManager.getProjectObjects(Permissions.BROWSE, null)).thenReturn(visibleProjects);

        testEmptyOptions();
    }

    @Test
    public void testNoProjectInContext()
    {
        Project mockProject1 = new MockProject(100L, "Project 1");
        Project mockProject2 = new MockProject(200L, "Project 2");

        List<Project> visibleProjects = asList(mockProject1, mockProject2);
        when(permissionManager.getProjectObjects(Permissions.BROWSE, null)).thenReturn(visibleProjects);
        when(projectComponentManager.findAllUniqueNamesForProjectObjects(visibleProjects)).thenReturn(asList("<name2>", "name1"));

        testAll(new MockSearchContext());
    }

    @Test
    public void testOneProjectInContextNoPermission()
    {
        MockProject mockProject = new MockProject(100L, "Project 1");

        when(projectManager.getProjectObj(100L)).thenReturn(mockProject);
        when(projectComponentManager.findAllForProject(100L)).thenReturn(Arrays.<ProjectComponent>asList(new MockProjectComponent(1L, "name1"),
                new MockProjectComponent(2L, "<name2>")));
        when(permissionManager.hasPermission(Permissions.BROWSE, mockProject, (User)null)).thenReturn(false);

        testEmptyOptions(mockProject);
    }

    @Test
    public void testOneProjectInContext()
    {
        MockProject mockProject = new MockProject(100L, "Project 1");

        when(projectManager.getProjectObj(100L)).thenReturn(mockProject);
        when(projectComponentManager.findAllForProject(100L)).thenReturn(Arrays.<ProjectComponent>asList(new MockProjectComponent(1L, "name1"),
                new MockProjectComponent(2L, "<name2>")));
        when(permissionManager.hasPermission(Permissions.BROWSE, mockProject, (User) null)).thenReturn(true);

        testAll(new MockSearchContext(mockProject));
    }

    @Test
    public void testTwoProjectsInContextNoPermissions()
    {
        MockProject mockProject1 = new MockProject(100L, "Project 1");
        MockProject mockProject2 = new MockProject(200L, "Project 2");

        when(projectManager.getProjectObj(100L)).thenReturn(mockProject1);
        when(projectManager.getProjectObj(200L)).thenReturn(mockProject2);
        when(projectComponentManager.findAllUniqueNamesForProjects(asList(100L, 200L))).thenReturn(asList("<name2>", "name1"));
        when(permissionManager.hasPermission(Permissions.BROWSE, mockProject1, (User) null)).thenReturn(false);
        when(permissionManager.hasPermission(Permissions.BROWSE, mockProject2, (User) null)).thenReturn(false);

        testEmptyOptions(mockProject1, mockProject2);
    }

    @Test
    public void testTwoProjectsInContextPermissionToOne()
    {
        MockProject mockProject1 = new MockProject(100L, "Project 1");
        MockProject mockProject2 = new MockProject(200L, "Project 2");

        when(projectManager.getProjectObj(100L)).thenReturn(mockProject1);
        when(projectManager.getProjectObj(200L)).thenReturn(mockProject2);
        when(projectComponentManager.findAllUniqueNamesForProjects(asList(100L))).thenReturn(asList("<name2>", "name1"));
        when(permissionManager.hasPermission(Permissions.BROWSE, mockProject1, (User) null)).thenReturn(true);
        when(permissionManager.hasPermission(Permissions.BROWSE, mockProject2, (User) null)).thenReturn(false);

        testAll(new MockSearchContext(mockProject1, mockProject2));
    }

    @Test
    public void testTwoProjectsInContext()
    {
        MockProject mockProject1 = new MockProject(100L, "Project 1");
        MockProject mockProject2 = new MockProject(200L, "Project 2");

        when(projectManager.getProjectObj(100L)).thenReturn(mockProject1);
        when(projectManager.getProjectObj(200L)).thenReturn(mockProject2);
        when(projectComponentManager.findAllUniqueNamesForProjects(asList(100L, 200L))).thenReturn(asList("<name2>", "name1"));
        when(permissionManager.hasPermission(Permissions.BROWSE, mockProject1, (User) null)).thenReturn(true);
        when(permissionManager.hasPermission(Permissions.BROWSE, mockProject2, (User) null)).thenReturn(true);

        testAll(new MockSearchContext(mockProject1, mockProject2));
    }

    private void testAll(SearchContext context)
    {
        // No selected values.
        test(context,
                null,
                null,
                Arrays.<Option>asList(new TextOption(createId("&lt;name2&gt;"), "&lt;name2&gt;"),
                        new TextOption(createId("name1"), "name1")),
                null,
                asList("name1", "<name2>"),
                null);

        // Valid selected values.
        test(context,
                asList(component("name1"), component("<name2>")),
                asList(createId("name1"), createId("&lt;name2&gt;")),
                Arrays.<Option>asList(new TextOption(createId("&lt;name2&gt;"), "&lt;name2&gt;"),
                        new TextOption(createId("name1"), "name1")),
                null,
                asList("name1", "<name2>"),
                null);

        // Invalid selected values.
        test(context,
                asList(component("name3"), component("<name4>")),
                asList(createId("name3"), createId("&lt;name4&gt;")),
                Arrays.<Option>asList(new TextOption(createId("&lt;name2&gt;"), "&lt;name2&gt;"),
                        new TextOption(createId("name1"), "name1")),
                Arrays.<Option>asList(new TextOption(createId("name3"), "name3"),
                        new TextOption(createId("&lt;name4&gt;"), "&lt;name4&gt;")),
                null,
                asList("name3", "<name4>"));

        // Valid and invalid selected values.
        test(context,
                asList(component("name1"), component("<name2>"), component("name3"), component("<name4>")),
                asList(createId("name1"), createId("&lt;name2&gt;"), createId("name3"), createId("&lt;name4&gt;")),
                Arrays.<Option>asList(new TextOption(createId("&lt;name2&gt;"), "&lt;name2&gt;"),
                        new TextOption(createId("name1"), "name1")),
                Arrays.<Option>asList(new TextOption(createId("name3"), "name3"),
                        new TextOption(createId("&lt;name4&gt;"), "&lt;name4&gt;")),
                asList("name1", "<name2>"),
                asList("name3", "<name4>"));

        // No components.
        test(context,
                asList(noComponents()),
                asList(ProjectComponentManager.NO_COMPONENTS),
                Arrays.<Option>asList(new TextOption(createId("&lt;name2&gt;"), "&lt;name2&gt;"),
                        new TextOption(createId("name1"), "name1")),
                null,
                asList("common.concepts.nocomponent", "name1", "<name2>"),
                null);
    }
}
