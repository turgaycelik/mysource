package com.atlassian.jira.issue.fields.screen;

import com.atlassian.jira.issue.fields.screen.issuetype.ProjectIssueTypeScreenSchemeHelper;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.workflow.MockJiraWorkflow;
import com.atlassian.jira.workflow.MockWorkflowManager;
import com.atlassian.jira.workflow.ProjectWorkflowSchemeHelper;
import com.atlassian.jira.workflow.WorkflowActionsBean;
import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.opensymphony.workflow.loader.ActionDescriptor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @since v4.4
 */
@RunWith (MockitoJUnitRunner.class)
public class TestDefaultProjectFieldScreenHelper
{

    private static final ApplicationUser TEST_USER = new MockApplicationUser("user123");
    private static final Project TEST_PROJECT_1 = new MockProject(123L);
    private static final Project TEST_PROJECT_2 = new MockProject(987L);

    @Mock
    private ProjectWorkflowSchemeHelper projectWorkflowSchemeHelper;
    @Mock
    private ProjectIssueTypeScreenSchemeHelper projectIssueTypeScreenSchemeHelper;
    @Mock
    private FieldScreenSchemeManager fieldScreenSchemeManager;
    @Mock
    private PermissionManager permissionManager;

    private MockWorkflowManager workflowManager;

    @Mock
    private ActionDescriptor actionDescriptor;
    @Mock
    private FieldScreen fieldScreen;

    @Before
    public void setUp()
    {
        workflowManager = new MockWorkflowManager();
    }

    @After
    public void tearDown()
    {
        workflowManager = null;
    }

    @Test
    public void testGetProjects()
    {
        final MockProject project1 = new MockProject(9090L, "mtan");
        final MockProject project2 = new MockProject(8080L, "mtan2");

        // Set up workflows
        final MockJiraWorkflow workflow1 = new MockJiraWorkflow().addAction(actionDescriptor);
        workflow1.setName("wf1");

        final MockJiraWorkflow workflow2 = new MockJiraWorkflow();
        workflow2.setName("wf2");

        workflowManager.addActiveWorkflows(workflow1)
            .addActiveWorkflows(workflow2);


        final Map<String, Collection<Project>> backingWfMap = Maps.newHashMap();
        final SetMultimap<String, Project> wfMultimap = Multimaps.newSetMultimap(backingWfMap, new Supplier<Set<Project>>()
        {
            @Override
            public Set<Project> get()
            {
                return Sets.newLinkedHashSet();
            }
        });
        wfMultimap.put("wf1", project1);

        when(projectWorkflowSchemeHelper.getProjectsForWorkflow(Sets.<String>newHashSet("wf1")))
                .thenReturn(wfMultimap);

        // Set up field screen schemes
        final MockFieldScreenScheme mockFieldScreenScheme = new MockFieldScreenScheme();
        final Map<FieldScreenScheme, Collection<Project>> backingFSSMap = Maps.newHashMap();
        final SetMultimap<FieldScreenScheme, Project> fssMultimap = Multimaps.newSetMultimap(backingFSSMap, new Supplier<Set<Project>>()
        {
            @Override
            public Set<Project> get()
            {
                return Sets.newLinkedHashSet();
            }
        });
        fssMultimap.putAll(mockFieldScreenScheme, Arrays.asList(project1, project2));

        final Set<FieldScreenScheme> fieldScreenSchemes = Sets.<FieldScreenScheme>newHashSet(
                mockFieldScreenScheme
        );

        when(fieldScreenSchemeManager.getFieldScreenSchemes(fieldScreen)).thenReturn(fieldScreenSchemes);
        when(projectIssueTypeScreenSchemeHelper.getProjectsForFieldScreenSchemes(fieldScreenSchemes))
                .thenReturn(fssMultimap);


        final ProjectFieldScreenHelper helper = createHelper(
            MapBuilder.<ActionDescriptor, FieldScreen>newBuilder()
                .add(actionDescriptor, fieldScreen)
                .toMap()
        );

        final List<Project> projectsForFieldScreen = helper.getProjectsForFieldScreen(fieldScreen);

        assertEquals(Arrays.<Project>asList(project1, project2), projectsForFieldScreen);
    }

    @Test
    public void testGetProjectsWithNonePresent()
    {
        final Map<String, Collection<Project>> backingWfMap = Maps.newHashMap();
        final SetMultimap<String, Project> wfMultimap = Multimaps.newSetMultimap(backingWfMap, new Supplier<Set<Project>>()
        {
            @Override
            public Set<Project> get()
            {
                return Sets.newLinkedHashSet();
            }
        });
        when(projectWorkflowSchemeHelper.getProjectsForWorkflow(Collections.<String>emptySet()))
                .thenReturn(wfMultimap);

        final MockFieldScreenScheme mockFieldScreenScheme = new MockFieldScreenScheme();
        final Map<FieldScreenScheme, Collection<Project>> backingFSSMap = Maps.newHashMap();
        final SetMultimap<FieldScreenScheme, Project> fssMultimap = Multimaps.newSetMultimap(backingFSSMap, new Supplier<Set<Project>>()
        {
            @Override
            public Set<Project> get()
            {
                return Sets.newLinkedHashSet();
            }
        });

        final Set<FieldScreenScheme> fieldScreenSchemes = Sets.<FieldScreenScheme>newHashSet(
                mockFieldScreenScheme
        );

        when(fieldScreenSchemeManager.getFieldScreenSchemes(fieldScreen)).thenReturn(fieldScreenSchemes);
        when(projectIssueTypeScreenSchemeHelper.getProjectsForFieldScreenSchemes(fieldScreenSchemes))
                .thenReturn(fssMultimap);

        final ProjectFieldScreenHelper helper = createHelper(
            MapBuilder.<ActionDescriptor, FieldScreen>newBuilder()
                .add(actionDescriptor, fieldScreen)
                .toMap()
        );

        final List<Project> projectsForFieldScreen = helper.getProjectsForFieldScreen(fieldScreen);

        assertEquals(Collections.<Project>emptyList(), projectsForFieldScreen);
    }

    @Test
    public void testCanUserViewFieldScreenForProjectReturnsTrueWhenGlobalAdmin()
    {
        final DefaultProjectFieldScreenHelper helper =
                new DefaultProjectFieldScreenHelper(projectWorkflowSchemeHelper, projectIssueTypeScreenSchemeHelper,
                        workflowManager, fieldScreenSchemeManager, permissionManager);

        when(permissionManager.hasPermission(Permissions.ADMINISTER, TEST_USER)).thenReturn(true);
        when(permissionManager.hasPermission(Permissions.PROJECT_ADMIN, TEST_PROJECT_1, TEST_USER)).thenReturn(false);

        assertThat(helper.canUserViewFieldScreenForProject(TEST_USER, fieldScreen, TEST_PROJECT_1), is(true));

        verify(permissionManager, never()).hasPermission(Permissions.PROJECT_ADMIN, TEST_PROJECT_1, TEST_USER);
        verify(fieldScreenSchemeManager, never()).getFieldScreenSchemes(fieldScreen);
        verify(projectIssueTypeScreenSchemeHelper, never()).getProjectsForFieldScreenSchemes(Matchers.<Set<FieldScreenScheme>>any());
    }

    @Test
    public void testCanUserViewFieldScreenForProjectReturnsTrueWhenProjectAdminAndProjectUsesFieldScreen()
    {
        final DefaultProjectFieldScreenHelper helper =
                new DefaultProjectFieldScreenHelper(projectWorkflowSchemeHelper, projectIssueTypeScreenSchemeHelper,
                        workflowManager, fieldScreenSchemeManager, permissionManager);

        when(permissionManager.hasPermission(Permissions.ADMINISTER, TEST_USER)).thenReturn(false);
        when(permissionManager.hasPermission(Permissions.PROJECT_ADMIN, TEST_PROJECT_1, TEST_USER)).thenReturn(true);

        final MockFieldScreenScheme mockFieldScreenScheme = new MockFieldScreenScheme();
        final Map<FieldScreenScheme, Collection<Project>> backingFSSMap = Maps.newHashMap();
        final SetMultimap<FieldScreenScheme, Project> fssMultimap = Multimaps.newSetMultimap(backingFSSMap, new Supplier<Set<Project>>()
        {
            @Override
            public Set<Project> get()
            {
                return Sets.newLinkedHashSet();
            }
        });
        fssMultimap.putAll(mockFieldScreenScheme, Arrays.asList(TEST_PROJECT_1, TEST_PROJECT_2));


        final Set<FieldScreenScheme> fieldScreenSchemes = Sets.<FieldScreenScheme>newHashSet(
                mockFieldScreenScheme
        );

        when(fieldScreenSchemeManager.getFieldScreenSchemes(fieldScreen)).thenReturn(fieldScreenSchemes);
        when(projectIssueTypeScreenSchemeHelper.getProjectsForFieldScreenSchemes(fieldScreenSchemes))
                .thenReturn(fssMultimap);

        assertThat(helper.canUserViewFieldScreenForProject(TEST_USER, fieldScreen, TEST_PROJECT_1), is(true));
    }

    @Test
    public void testCanUserViewFieldScreenForProjectReturnsFalseWhenProjectAdminAndProjectDoesNotUseFieldScreen()
    {
        final DefaultProjectFieldScreenHelper helper =
                new DefaultProjectFieldScreenHelper(projectWorkflowSchemeHelper, projectIssueTypeScreenSchemeHelper,
                        workflowManager, fieldScreenSchemeManager, permissionManager);

        when(permissionManager.hasPermission(Permissions.ADMINISTER, TEST_USER)).thenReturn(false);
        when(permissionManager.hasPermission(Permissions.PROJECT_ADMIN, TEST_PROJECT_1, TEST_USER)).thenReturn(true);

        final MockFieldScreenScheme mockFieldScreenScheme = new MockFieldScreenScheme();
        final Map<FieldScreenScheme, Collection<Project>> backingFSSMap = Maps.newHashMap();
        final SetMultimap<FieldScreenScheme, Project> fssMultimap = Multimaps.newSetMultimap(backingFSSMap, new Supplier<Set<Project>>()
        {
            @Override
            public Set<Project> get()
            {
                return Sets.newLinkedHashSet();
            }
        });

        final Set<FieldScreenScheme> fieldScreenSchemes = Sets.<FieldScreenScheme>newHashSet(
                mockFieldScreenScheme
        );

        when(fieldScreenSchemeManager.getFieldScreenSchemes(fieldScreen)).thenReturn(fieldScreenSchemes);
        when(projectIssueTypeScreenSchemeHelper.getProjectsForFieldScreenSchemes(fieldScreenSchemes))
                .thenReturn(fssMultimap);

        assertThat(helper.canUserViewFieldScreenForProject(TEST_USER, fieldScreen, TEST_PROJECT_1), is(false));
    }

    @Test
    public void testCanUserViewFieldScreenForProjectReturnsFalseWhenNotProjectAdminAndProjectDoesUseFieldScreen()
    {
        final DefaultProjectFieldScreenHelper helper =
                new DefaultProjectFieldScreenHelper(projectWorkflowSchemeHelper, projectIssueTypeScreenSchemeHelper,
                        workflowManager, fieldScreenSchemeManager, permissionManager);

        when(permissionManager.hasPermission(Permissions.ADMINISTER, TEST_USER)).thenReturn(false);
        when(permissionManager.hasPermission(Permissions.PROJECT_ADMIN, TEST_PROJECT_1, TEST_USER)).thenReturn(false);

        final MockFieldScreenScheme mockFieldScreenScheme = new MockFieldScreenScheme();
        final Map<FieldScreenScheme, Collection<Project>> backingFSSMap = Maps.newHashMap();
        final SetMultimap<FieldScreenScheme, Project> fssMultimap = Multimaps.newSetMultimap(backingFSSMap, new Supplier<Set<Project>>()
        {
            @Override
            public Set<Project> get()
            {
                return Sets.newLinkedHashSet();
            }
        });
        fssMultimap.putAll(mockFieldScreenScheme, Arrays.asList(TEST_PROJECT_1, TEST_PROJECT_2));

        final Set<FieldScreenScheme> fieldScreenSchemes = Sets.<FieldScreenScheme>newHashSet(
                mockFieldScreenScheme
        );

        when(fieldScreenSchemeManager.getFieldScreenSchemes(fieldScreen)).thenReturn(fieldScreenSchemes);
        when(projectIssueTypeScreenSchemeHelper.getProjectsForFieldScreenSchemes(fieldScreenSchemes))
                .thenReturn(fssMultimap);

        assertThat(helper.canUserViewFieldScreenForProject(TEST_USER, fieldScreen, TEST_PROJECT_1), is(false));
    }

    @Test
    public void testCanUserViewFieldScreenForProjectReturnsFalseWhenNotProjectAdminAndProjectDoesNotUseFieldScreen()
    {
        final DefaultProjectFieldScreenHelper helper =
                new DefaultProjectFieldScreenHelper(projectWorkflowSchemeHelper, projectIssueTypeScreenSchemeHelper,
                        workflowManager, fieldScreenSchemeManager, permissionManager);

        when(permissionManager.hasPermission(Permissions.ADMINISTER, TEST_USER)).thenReturn(false);
        when(permissionManager.hasPermission(Permissions.PROJECT_ADMIN, TEST_PROJECT_1, TEST_USER)).thenReturn(false);

        final MockFieldScreenScheme mockFieldScreenScheme = new MockFieldScreenScheme();
        final Map<FieldScreenScheme, Collection<Project>> backingFSSMap = Maps.newHashMap();
        final SetMultimap<FieldScreenScheme, Project> fssMultimap = Multimaps.newSetMultimap(backingFSSMap, new Supplier<Set<Project>>()
        {
            @Override
            public Set<Project> get()
            {
                return Sets.newLinkedHashSet();
            }
        });

        final Set<FieldScreenScheme> fieldScreenSchemes = Sets.<FieldScreenScheme>newHashSet(
                mockFieldScreenScheme
        );

        when(fieldScreenSchemeManager.getFieldScreenSchemes(fieldScreen)).thenReturn(fieldScreenSchemes);
        when(projectIssueTypeScreenSchemeHelper.getProjectsForFieldScreenSchemes(fieldScreenSchemes))
                .thenReturn(fssMultimap);

        assertThat(helper.canUserViewFieldScreenForProject(TEST_USER, fieldScreen, TEST_PROJECT_1), is(false));
    }

    private ProjectFieldScreenHelper createHelper(final Map<ActionDescriptor, FieldScreen> workflowDescriptorScreens)
    {
        return new DefaultProjectFieldScreenHelper(projectWorkflowSchemeHelper, projectIssueTypeScreenSchemeHelper, workflowManager,
                fieldScreenSchemeManager, permissionManager)
        {
            @Override
            WorkflowActionsBean getActionsBean()
            {
                return new WorkflowActionsBean()
                {
                    @Override
                    public FieldScreen getFieldScreenForView(ActionDescriptor actionDescriptor)
                    {
                        return workflowDescriptorScreens.get(actionDescriptor);
                    }
                };
            }
        };
    }
}
