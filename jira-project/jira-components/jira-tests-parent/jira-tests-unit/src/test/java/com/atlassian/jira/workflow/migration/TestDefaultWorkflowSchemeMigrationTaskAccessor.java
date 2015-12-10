package com.atlassian.jira.workflow.migration;

import java.util.Arrays;
import java.util.Collections;

import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.task.TaskContext;
import com.atlassian.jira.task.TaskDescriptor;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.task.TaskMatcher;
import com.atlassian.jira.web.action.admin.workflow.WorkflowMigrationResult;
import com.atlassian.jira.workflow.AssignableWorkflowScheme;
import com.atlassian.jira.workflow.DraftWorkflowScheme;
import com.atlassian.jira.workflow.MockAssignableWorkflowScheme;
import com.atlassian.jira.workflow.MockDraftWorkflowScheme;
import com.atlassian.jira.workflow.WorkflowSchemeManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.workflow.migration.DefaultWorkflowSchemeMigrationTaskAccessor.ProjectMigrationTaskMatcher;
import static com.atlassian.jira.workflow.migration.DefaultWorkflowSchemeMigrationTaskAccessor.SchemeMigrationTaskMatcher;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestDefaultWorkflowSchemeMigrationTaskAccessor
{
    @Mock
    private TaskManager taskManager;

    @Mock
    private WorkflowSchemeManager manager;

    private DefaultWorkflowSchemeMigrationTaskAccessor workflowSchemeMigrationTaskAccessor;

    @Before
    public void setUp()
    {
        workflowSchemeMigrationTaskAccessor = new DefaultWorkflowSchemeMigrationTaskAccessor(taskManager, manager);
    }

    @Test
    public void testGetActiveAssignable()
    {
        SchemeMigrationTaskMatcher taskMatcher = new SchemeMigrationTaskMatcher(new MockAssignableWorkflowScheme(1L, "scheme name"));

        TaskDescriptor taskDescriptor = mock(TaskDescriptor.class);
        when(taskDescriptor.isFinished()).thenReturn(true);
        assertFalse(taskMatcher.match(taskDescriptor));

        when(taskDescriptor.isFinished()).thenReturn(false);
        when(taskDescriptor.getTaskContext()).thenReturn(new NoOpTaskContext());
        assertFalse(taskMatcher.match(taskDescriptor));

        EnterpriseWorkflowTaskContext enterpriseWorkflowTaskContext = new EnterpriseWorkflowTaskContext(new MockProject(1L), 2L, false);
        when(taskDescriptor.getTaskContext()).thenReturn(enterpriseWorkflowTaskContext);
        assertFalse(taskMatcher.match(taskDescriptor));

        enterpriseWorkflowTaskContext = new EnterpriseWorkflowTaskContext(new MockProject(1L), 1L, false);
        when(taskDescriptor.getTaskContext()).thenReturn(enterpriseWorkflowTaskContext);
        assertTrue(taskMatcher.match(taskDescriptor));
    }

    @Test
    public void getActiveDraftReturnsNullIfNoProjectsAreUsingTheDraft()
    {
        AssignableWorkflowScheme parent = new MockAssignableWorkflowScheme(1L, "scheme name");

        when(manager.getProjectsUsing(parent)).thenReturn(Collections.<Project>emptyList());
        TaskDescriptor<WorkflowMigrationResult> taskDescriptor = workflowSchemeMigrationTaskAccessor.getActiveByProjects(new MockDraftWorkflowScheme(2L, parent), true);

        assertNull(taskDescriptor);
        verify(taskManager, never()).findFirstTask(any(TaskMatcher.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getActiveByProjectsSearchesForParentMigrationIfNoDraftMigration()
    {
        AssignableWorkflowScheme parent = new MockAssignableWorkflowScheme(1L, "scheme name");

        when(manager.getProjectsUsing(parent)).thenReturn(Arrays.<Project>asList(new MockProject(1L)));

        TaskDescriptor taskDescriptor = mock(TaskDescriptor.class);

        when(taskManager.findFirstTask(any(ProjectMigrationTaskMatcher.class))).thenReturn(taskDescriptor);

        TaskDescriptor<WorkflowMigrationResult> result = workflowSchemeMigrationTaskAccessor.getActiveByProjects(new MockDraftWorkflowScheme(2L, parent), true);
        assertEquals(taskDescriptor, result);

        verify(taskManager, times(1)).findFirstTask(any(ProjectMigrationTaskMatcher.class));
        reset(taskManager);

        // No draft migration, search for parent.
        when(taskManager.findFirstTask(any(ProjectMigrationTaskMatcher.class))).thenReturn(null);
        when(taskManager.findFirstTask(any(SchemeMigrationTaskMatcher.class))).thenReturn(taskDescriptor);

        result = workflowSchemeMigrationTaskAccessor.getActiveByProjects(new MockDraftWorkflowScheme(2L, parent), true);
        assertEquals(taskDescriptor, result);

        verify(taskManager, times(1)).findFirstTask(any(ProjectMigrationTaskMatcher.class));
        verify(taskManager, times(1)).findFirstTask(any(SchemeMigrationTaskMatcher.class));
    }

    @Test
    public void testGetActiveByProjects()
    {
        AssignableWorkflowScheme parent = new MockAssignableWorkflowScheme(1L, "");
        DraftWorkflowScheme draft = new MockDraftWorkflowScheme(1L, parent);

        MockProject projectOne = new MockProject(1L);
        MockProject projectTwo = new MockProject(2L);

        ProjectMigrationTaskMatcher taskMatcher = new ProjectMigrationTaskMatcher(draft, Arrays.<Project>asList(projectOne), projectOne, false, false);

        TaskDescriptor taskDescriptor = mock(TaskDescriptor.class);

        when(taskDescriptor.isFinished()).thenReturn(true);
        assertFalse(taskMatcher.match(taskDescriptor));

        when(taskDescriptor.isFinished()).thenReturn(false);
        when(taskDescriptor.getTaskContext()).thenReturn(new NoOpTaskContext());
        assertFalse(taskMatcher.match(taskDescriptor));

        when(taskDescriptor.getTaskContext()).thenReturn(new EnterpriseWorkflowTaskContext(projectTwo, 1L, false));
        assertFalse(taskMatcher.match(taskDescriptor));

        when(taskDescriptor.getTaskContext()).thenReturn(new EnterpriseWorkflowTaskContext(projectOne, Arrays.<Project>asList(projectOne, projectTwo), 1L, false));
        assertTrue(taskMatcher.match(taskDescriptor));


        taskMatcher = new ProjectMigrationTaskMatcher(draft, Arrays.<Project>asList(projectOne), projectOne, true, false);

        when(taskDescriptor.getTaskContext()).thenReturn(new EnterpriseWorkflowTaskContext(projectOne, Arrays.<Project>asList(projectOne, projectTwo), 1L, false));
        assertFalse(taskMatcher.match(taskDescriptor));

        when(taskDescriptor.getTaskContext()).thenReturn(new EnterpriseWorkflowTaskContext(projectOne, Arrays.<Project>asList(projectOne, projectTwo), 1L, true));
        assertTrue(taskMatcher.match(taskDescriptor));


        taskMatcher = new ProjectMigrationTaskMatcher(draft, Arrays.<Project>asList(projectOne), projectOne, true, true);

        EnterpriseWorkflowTaskContext context = new EnterpriseWorkflowTaskContext(projectOne, Arrays.<Project>asList(projectOne, projectTwo), 1L, true);
        when(taskDescriptor.getTaskContext()).thenReturn(context);
        assertTrue(taskMatcher.match(taskDescriptor));

        context.markSafeToDelete();
        assertFalse(taskMatcher.match(taskDescriptor));
    }

    private static class NoOpTaskContext implements TaskContext
    {
        @Override
        public String buildProgressURL(Long taskId)
        {
            return "";
        }
    }
}
