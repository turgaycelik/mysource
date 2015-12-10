package com.atlassian.jira.web;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.issue.IssueFieldConstants.COMPONENTS;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestFieldVisibilityManagerImpl
{
    private final Project project = new MockProject(1l, "TST", "Test Project");
    private final String issueTypeA = "1";
    private final String issueTypeB = "2";
    private final String issueTypeSubtask = "3";
    private final ImmutableList<String> allIssueTypeIds = ImmutableList.of(issueTypeA, issueTypeB, issueTypeSubtask);
    private final ImmutableList<String> standardIssueTypes = ImmutableList.of(issueTypeA, issueTypeB);

    @Mock
    FieldManager fieldManager;
    @Mock
    ProjectManager projectManager;
    @Mock
    ConstantsManager constantsManager;
    @Mock
    FieldLayoutManager fieldLayoutManager;
    @Mock
    SubTaskManager subTaskManager;
    @Mock
    FieldLayout fieldLayoutWithVisibleComponentsField;
    @Mock
    FieldLayout fieldLayoutWithHiddenComponentsField;

    @Before
    public void setUp() throws Exception
    {
        when(projectManager.getProjectObj(project.getId())).thenReturn(project);
        when(fieldLayoutWithVisibleComponentsField.isFieldHidden(COMPONENTS)).thenReturn(false);
        when(fieldLayoutWithHiddenComponentsField.isFieldHidden(COMPONENTS)).thenReturn(true);
    }

    @Test
    public void isFieldHiddenForAllIssueTypesShouldReturnFalseWhenFieldIsVisibleForAllIssueTypes()
    {
        when(subTaskManager.isSubTasksEnabled()).thenReturn(true);
        when(constantsManager.expandIssueTypeIds(ImmutableList.of(ConstantsManager.ALL_ISSUE_TYPES)))
                .thenReturn(allIssueTypeIds);

        // all issue types use fieldLayoutWithVisibleComponentsField
        for (final String issueTypeId : allIssueTypeIds)
        {
            when(fieldLayoutManager.getFieldLayout(project, issueTypeId)).thenReturn(fieldLayoutWithVisibleComponentsField);
        }

        final FieldVisibilityManager manager = createManager();

        // Check ALL issue types
        assertFalse(manager.isFieldHidden(project.getId(), COMPONENTS, FieldVisibilityManager.ALL_ISSUE_TYPES));
    }

    @Test
    public void isFieldHiddenForAllIssueTypesShouldReturnTrueWhenFieldIsHiddenForAtLeastOneIssueType()
    {
        when(subTaskManager.isSubTasksEnabled()).thenReturn(true);
        when(constantsManager.expandIssueTypeIds(ImmutableList.of(ConstantsManager.ALL_ISSUE_TYPES)))
                .thenReturn(allIssueTypeIds);

        when(fieldLayoutManager.getFieldLayout(project, issueTypeA)).thenReturn(fieldLayoutWithVisibleComponentsField);
        when(fieldLayoutManager.getFieldLayout(project, issueTypeB)).thenReturn(fieldLayoutWithVisibleComponentsField);
        when(fieldLayoutManager.getFieldLayout(project, issueTypeSubtask)).thenReturn(fieldLayoutWithHiddenComponentsField);

        final FieldVisibilityManager manager = createManager();

        // Check ALL issue types
        assertTrue(manager.isFieldHidden(project.getId(), COMPONENTS, FieldVisibilityManager.ALL_ISSUE_TYPES));
    }

    @Test
    public void isFieldHiddenForAllIssueTypesShouldReturnTrueWhenFieldIsHiddenForAllIssueTypes()
    {
        when(subTaskManager.isSubTasksEnabled()).thenReturn(true);
        when(constantsManager.expandIssueTypeIds(ImmutableList.of(ConstantsManager.ALL_ISSUE_TYPES)))
                .thenReturn(allIssueTypeIds);

        // all issue types use fieldLayoutWithHiddenComponentsField
        for (final String issueTypeId : allIssueTypeIds)
        {
            when(fieldLayoutManager.getFieldLayout(project, issueTypeId)).thenReturn(fieldLayoutWithHiddenComponentsField);
        }

        final FieldVisibilityManager manager = createManager();

        // Check ALL issue types
        assertTrue(manager.isFieldHidden(project.getId(), COMPONENTS, FieldVisibilityManager.ALL_ISSUE_TYPES));
    }

    @Test
    public void isFieldHiddenForAllIssueTypesShouldReturnFalseWhenFieldIsVisibleForAllIssueTypesWhenSubtasksFeatureIsDisabled()
    {
        when(subTaskManager.isSubTasksEnabled()).thenReturn(false);
        when(constantsManager.expandIssueTypeIds(ImmutableList.of(ConstantsManager.ALL_STANDARD_ISSUE_TYPES)))
                .thenReturn(standardIssueTypes);

        when(fieldLayoutManager.getFieldLayout(project, issueTypeA)).thenReturn(fieldLayoutWithVisibleComponentsField);
        when(fieldLayoutManager.getFieldLayout(project, issueTypeB)).thenReturn(fieldLayoutWithVisibleComponentsField);

        final FieldVisibilityManager manager = createManager();

        // Check ALL issue types
        assertFalse(manager.isFieldHidden(project.getId(), COMPONENTS, FieldVisibilityManager.ALL_ISSUE_TYPES));
    }

    @Test
    public void isFieldHiddenForIndividualIssueTypeShouldReturnFalseWhenTheIssueFieldIsVisible()
    {
        when(fieldLayoutManager.getFieldLayout(project, issueTypeA)).thenReturn(fieldLayoutWithVisibleComponentsField);

        final FieldVisibilityManager manager = createManager();

        assertFalse(manager.isFieldHidden(project.getId(), COMPONENTS, issueTypeA));
    }

    @Test
    public void isFieldHiddenForIndividualIssueTypeShouldReturnTrueWhenTheIssueFieldIsHidden()
    {
        when(fieldLayoutManager.getFieldLayout(project, issueTypeA)).thenReturn(fieldLayoutWithHiddenComponentsField);

        final FieldVisibilityManager manager = createManager();

        assertTrue(manager.isFieldHidden(project.getId(), COMPONENTS, issueTypeA));
    }

    @Test
    public void isFieldHiddenForIndividualIssueTypeShouldReturnTrueWhenFieldIsNotInLayout()
    {
        when(fieldLayoutManager.getFieldLayout(project, issueTypeA)).thenReturn(null);

        final FieldVisibilityManager manager = createManager();

        assertTrue(manager.isFieldHidden(project.getId(), COMPONENTS, issueTypeA));
    }

    private FieldVisibilityManagerImpl createManager()
    {
        return new FieldVisibilityManagerImpl(fieldManager, projectManager,
                constantsManager, fieldLayoutManager, subTaskManager);
    }

}
