package com.atlassian.jira.bc.project;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * @since v6.1
 */
@RunWith(MockitoJUnitRunner.class)
public class ProjectNameValidatorTest
{
    public static final String PROJECT_NAME = "Project Name";
    private static final String PROJECT_KEY = "KEY";
    public static final int DEFAULT_MAX_NAME_LENGTH = 20;
    private static final int SMALL_MAX_NAME_LENGTH = 5;
    @Mock
    private ProjectService projectService;

    @Mock
    private ProjectManager projectManager;

    @Mock
    private Project project;

    @Mock
    private ErrorCollection errorCollection;

    @Mock
    private I18nHelper i18n;

    @Test
    public void testAcceptsValidNameForCreate() throws Exception
    {
        ProjectNameValidator sut = new ProjectNameValidator(projectService, projectManager);
        when(projectService.getMaximumNameLength()).thenReturn(DEFAULT_MAX_NAME_LENGTH);

        sut.validateForCreate(PROJECT_NAME, errorCollection, i18n);

        verifyZeroInteractions(errorCollection);
    }

    @Test
    public void testRejectsToLongNameForCreate() throws Exception
    {
        ProjectNameValidator sut = new ProjectNameValidator(projectService, projectManager);
        when(projectService.getMaximumNameLength()).thenReturn(SMALL_MAX_NAME_LENGTH);

        sut.validateForCreate(PROJECT_NAME, errorCollection, i18n);

        verify(errorCollection, times(1)).addError(anyString(), anyString());
    }

    @Test
    public void testRejectsToShortNameForCreate() throws Exception
    {
        ProjectNameValidator sut = new ProjectNameValidator(projectService, projectManager);
        when(projectService.getMaximumNameLength()).thenReturn(DEFAULT_MAX_NAME_LENGTH);

        sut.validateForCreate("P", errorCollection, i18n);

        verify(errorCollection, times(1)).addError(anyString(), anyString());
    }

    @Test
    public void testRejectsNameThatAlreadyExistsForCreate() throws Exception
    {
        ProjectNameValidator sut = new ProjectNameValidator(projectService, projectManager);
        when(projectService.getMaximumNameLength()).thenReturn(DEFAULT_MAX_NAME_LENGTH);
        when(projectManager.getProjectObjByName(PROJECT_NAME)).thenReturn(project);

        sut.validateForCreate(PROJECT_NAME, errorCollection, i18n);

        verify(errorCollection, times(1)).addError(anyString(), anyString());
    }

    @Test
    public void testAcceptsValidNameForUpdate() throws Exception
    {
        ProjectNameValidator sut = new ProjectNameValidator(projectService, projectManager);
        when(projectService.getMaximumNameLength()).thenReturn(DEFAULT_MAX_NAME_LENGTH);

        sut.validateForUpdate(PROJECT_NAME, PROJECT_KEY, errorCollection, i18n);

        verifyZeroInteractions(errorCollection);
    }

    @Test
    public void testRejectsToLongNameForUpdate() throws Exception
    {
        ProjectNameValidator sut = new ProjectNameValidator(projectService, projectManager);
        when(projectService.getMaximumNameLength()).thenReturn(SMALL_MAX_NAME_LENGTH);

        sut.validateForUpdate(PROJECT_NAME, PROJECT_KEY, errorCollection, i18n);

        verify(errorCollection, times(1)).addError(anyString(), anyString());
    }

    @Test
    public void testRejectsToShortNameForUpdate() throws Exception
    {
        ProjectNameValidator sut = new ProjectNameValidator(projectService, projectManager);
        when(projectService.getMaximumNameLength()).thenReturn(DEFAULT_MAX_NAME_LENGTH);

        sut.validateForUpdate("P", PROJECT_KEY, errorCollection, i18n);

        verify(errorCollection, times(1)).addError(anyString(), anyString());
    }

    @Test
    public void testRejectsNameThatAlreadyExistsForUpdate() throws Exception
    {
        ProjectNameValidator sut = new ProjectNameValidator(projectService, projectManager);
        when(projectService.getMaximumNameLength()).thenReturn(DEFAULT_MAX_NAME_LENGTH);
        when(projectManager.getProjectObjByName(PROJECT_NAME)).thenReturn(project);

        sut.validateForUpdate(PROJECT_NAME, PROJECT_KEY, errorCollection, i18n);

        verify(errorCollection, times(1)).addError(anyString(), anyString());
    }

    @Test
    public void testAcceptsOwnNameForUpdate() throws Exception
    {
        ProjectNameValidator sut = new ProjectNameValidator(projectService, projectManager);
        when(projectService.getMaximumNameLength()).thenReturn(DEFAULT_MAX_NAME_LENGTH);
        when(projectManager.getProjectObjByName(PROJECT_NAME)).thenReturn(project);
        when(project.getKey()).thenReturn(PROJECT_KEY);

        sut.validateForUpdate(PROJECT_NAME, PROJECT_KEY, errorCollection, i18n);

        verifyZeroInteractions(errorCollection);
    }
}
