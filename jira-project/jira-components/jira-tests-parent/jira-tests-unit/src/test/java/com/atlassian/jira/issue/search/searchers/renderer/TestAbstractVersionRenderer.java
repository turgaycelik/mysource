package com.atlassian.jira.issue.search.searchers.renderer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.fields.option.Option;
import com.atlassian.jira.issue.fields.option.TextOption;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.transformer.VersionSearchInput;
import com.atlassian.jira.mock.issue.search.MockSearchContext;
import com.atlassian.jira.mock.project.MockVersion;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.MockVersionManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.Permissions;

import org.junit.Test;
import org.mockito.Mock;

import static com.atlassian.jira.issue.search.searchers.transformer.VersionSearchInput.allReleased;
import static com.atlassian.jira.issue.search.searchers.transformer.VersionSearchInput.allUnreleased;
import static com.atlassian.jira.issue.search.searchers.transformer.VersionSearchInput.noVersions;
import static com.atlassian.jira.issue.search.searchers.transformer.VersionSearchInput.version;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.when;

/**
 * @since v5.2
 */
public abstract class TestAbstractVersionRenderer extends TestAbstractProjectConstantsRenderer<VersionSearchInput, VersionsOptions>
{
    @Mock
    MockVersionManager versionManager;

    TestAbstractVersionRenderer(String urlParameter)
    {
        super(urlParameter);
    }

    @Override
    String[] getSpecialOptionIds()
    {
        return new String[] { VersionManager.NO_VERSIONS, VersionManager.ALL_RELEASED_VERSIONS, VersionManager.ALL_UNRELEASED_VERSIONS };
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
        when(versionManager.getAllVersionsForProjects(visibleProjects, false)).thenReturn(Arrays.<Version>asList(new MockVersion(1L, "name1"), new MockVersion(2L, "<name2>")));

        testAll(new MockSearchContext(), asList(""));
    }

    @Test
    public void testOneProjectInContextNoPermission()
    {
        MockProject mockProject = new MockProject(100L, "Project 1");
        when(projectManager.getProjectObj(100L)).thenReturn(mockProject);
        when(permissionManager.hasPermission(Permissions.BROWSE, mockProject, (User) null)).thenReturn(false);

        testEmptyOptions(mockProject);
    }

    @Test
    public void testOneProjectInContext()
    {
        MockProject mockProject = new MockProject(100L, "Project 1");

        when(projectManager.getProjectObj(100L)).thenReturn(mockProject);
        when(versionManager.getVersionsUnreleased(100L, false)).thenReturn(Arrays.<Version>asList(new MockVersion(1L, "name1")));
        when(versionManager.getVersionsReleasedDesc(100L, false)).thenReturn(Arrays.<Version>asList(new MockVersion(2L, "<name2>")));
        when(permissionManager.hasPermission(Permissions.BROWSE, mockProject, (User) null)).thenReturn(true);

        MockSearchContext context = new MockSearchContext(mockProject);
        List<String> validOptionGroupIds = asList(VersionManager.ALL_RELEASED_VERSIONS, VersionManager.ALL_UNRELEASED_VERSIONS);

        testAll(context, validOptionGroupIds);

        // All released.
        test(context,
                asList(allReleased()),
                asList(VersionManager.ALL_RELEASED_VERSIONS),
                Arrays.<Option>asList(new TextOption(createId("&lt;name2&gt;"), "&lt;name2&gt;"),
                        new TextOption(createId("name1"), "name1")),
                validOptionGroupIds,
                null,
                asList("common.filters.releasedversions", "name1", "<name2>"),
                null);

        // All unreleased.
        test(context,
                asList(allUnreleased()),
                asList(VersionManager.ALL_UNRELEASED_VERSIONS),
                Arrays.<Option>asList(new TextOption(createId("&lt;name2&gt;"), "&lt;name2&gt;"),
                        new TextOption(createId("name1"), "name1")),
                validOptionGroupIds,
                null,
                asList("common.filters.unreleasedversions", "name1", "<name2>"),
                null);
    }

    @Test
    public void testTwoProjectsInContextNoPermission()
    {
        MockProject mockProject1 = new MockProject(100L, "Project 1");
        MockProject mockProject2 = new MockProject(200L, "Project 2");

        when(projectManager.getProjectObj(100L)).thenReturn(mockProject1);
        when(projectManager.getProjectObj(200L)).thenReturn(mockProject2);
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
        when(permissionManager.hasPermission(Permissions.BROWSE, mockProject1, (User) null)).thenReturn(true);
        when(permissionManager.hasPermission(Permissions.BROWSE, mockProject2, (User) null)).thenReturn(false);

        when(versionManager.getVersions(100L, false)).thenReturn(Arrays.<Version>asList(new MockVersion(1L, "name1"), new MockVersion(2L, "<name2>")));

        testAll(new MockSearchContext(mockProject1, mockProject2), asList(""));
    }

    @Test
    public void testTwoProjectsInContext()
    {
        MockProject mockProject1 = new MockProject(100L, "Project 1");
        MockProject mockProject2 = new MockProject(200L, "Project 2");

        when(projectManager.getProjectObj(100L)).thenReturn(mockProject1);
        when(projectManager.getProjectObj(200L)).thenReturn(mockProject2);
        when(versionManager.getVersions(100L, false)).thenReturn(Arrays.<Version>asList(new MockVersion(1L, "name1")));
        when(versionManager.getVersions(200L, false)).thenReturn(Arrays.<Version>asList(new MockVersion(2L, "<name2>")));
        when(permissionManager.hasPermission(Permissions.BROWSE, mockProject1, (User) null)).thenReturn(true);
        when(permissionManager.hasPermission(Permissions.BROWSE, mockProject2, (User) null)).thenReturn(true);

        testAll(new MockSearchContext(mockProject1, mockProject2), asList(""));
    }

    private void testAll(SearchContext context, List<String> validOptionGroupIds)
    {
        // No selected values.
        test(context,
                null,
                null,
                Arrays.<Option>asList(new TextOption(createId("&lt;name2&gt;"), "&lt;name2&gt;"),
                        new TextOption(createId("name1"), "name1")),
                validOptionGroupIds,
                null,
                asList("name1", "<name2>"),
                null);

        // Valid selected values.
        test(context,
                asList(version("name1"), version("<name2>")),
                asList(createId("name1"), createId("&lt;name2&gt;")),
                Arrays.<Option>asList(new TextOption(createId("&lt;name2&gt;"), "&lt;name2&gt;"),
                        new TextOption(createId("name1"), "name1")),
                validOptionGroupIds,
                null,
                asList("name1", "<name2>"),
                null);

        // Invalid selected values.
        test(context,
                asList(version("name3"), version("<name4>")),
                asList(createId("name3"), createId("&lt;name4&gt;")),
                Arrays.<Option>asList(new TextOption(createId("&lt;name2&gt;"), "&lt;name2&gt;"),
                        new TextOption(createId("name1"), "name1")),
                validOptionGroupIds,
                Arrays.<Option>asList(new TextOption(createId("name3"), "name3"),
                        new TextOption(createId("&lt;name4&gt;"), "&lt;name4&gt;")),
                null,
                asList("name3", "<name4>"));

        // Valid and invalid selected values.
        test(context,
                asList(version("name1"), version("<name2>"), version("name3"), version("<name4>")),
                asList(createId("name1"), createId("&lt;name2&gt;"), createId("name3"), createId("&lt;name4&gt;")),
                Arrays.<Option>asList(new TextOption(createId("&lt;name2&gt;"), "&lt;name2&gt;"),
                        new TextOption(createId("name1"), "name1")),
                validOptionGroupIds,
                Arrays.<Option>asList(new TextOption(createId("name3"), "name3"),
                        new TextOption(createId("&lt;name4&gt;"), "&lt;name4&gt;")),
                asList("name1", "<name2>"),
                asList("name3", "<name4>"));

        // No versions.
        test(context,
                asList(noVersions()),
                asList(VersionManager.NO_VERSIONS),
                Arrays.<Option>asList(new TextOption(createId("&lt;name2&gt;"), "&lt;name2&gt;"),
                        new TextOption(createId("name1"), "name1")),
                validOptionGroupIds,
                null,
                asList("common.filters.noversion", "name1", "<name2>"),
                null);
    }
}
