package com.atlassian.jira.issue.fields;

import com.atlassian.jira.bc.project.component.MockProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class TestComponentsSystemFieldUnit
{
    private ComponentsSystemField componentsSystemField;

    private I18nHelper i18n = new MockI18nHelper();
    @Mock
    private FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem;
    @Mock
    private LongIdsValueHolder componentIds;
    @Mock
    private Project project;
    @Mock
    private ProjectComponentManager projectComponentManager;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        componentsSystemField = new ComponentsSystemField(null, projectComponentManager, null, null, null, null, null, null, null, null);
    }

    @Test
    public void fieldValidationPassesWhenComponentsFieldIsNotRequired()
    {
        when(fieldScreenRenderLayoutItem.isRequired()).thenReturn(false);

        final ErrorCollection errors = new SimpleErrorCollection();
        boolean result = componentsSystemField.validateForRequiredField(errors, i18n, fieldScreenRenderLayoutItem, componentIds, project);

        assertTrue("Validation should have passed", result);
        assertFalse(errors.hasAnyErrors());
    }

    @Test
    public void fieldValidationPassesWhenExistingComponentsProvided()
    {
        when(fieldScreenRenderLayoutItem.isRequired()).thenReturn(true);
        when(componentIds.isEmpty()).thenReturn(false);

        final ErrorCollection errors = new SimpleErrorCollection();
        boolean result = componentsSystemField.validateForRequiredField(errors, i18n, fieldScreenRenderLayoutItem, componentIds, project);

        assertTrue("Validation should have passed", result);
        assertFalse(errors.hasAnyErrors());
    }

    @Test
    public void fieldValidationPassesWhenUnknownComponentsProvidedDuringBulkOperation()
    {
        when(fieldScreenRenderLayoutItem.isRequired()).thenReturn(true);
        when(componentIds.contains(-1L)).thenReturn(false);

        final ErrorCollection errors = new SimpleErrorCollection();
        boolean result = componentsSystemField.validateForRequiredField(errors, i18n, fieldScreenRenderLayoutItem, componentIds, project);

        assertTrue("Validation should have passed", result);
        assertFalse(errors.hasAnyErrors());
    }

    @Test
    public void fieldValidationPassesWhenNewComponentsProvided()
    {
        when(fieldScreenRenderLayoutItem.isRequired()).thenReturn(true);
        when(componentIds.getValuesToAdd()).thenReturn(Sets.newHashSet("abc"));

        final ErrorCollection errors = new SimpleErrorCollection();
        boolean result = componentsSystemField.validateForRequiredField(errors, i18n, fieldScreenRenderLayoutItem, componentIds, project);

        assertTrue("Validation should have passed", result);
        assertFalse(errors.hasAnyErrors());
    }

    @Test
    public void fieldValidationFailsWhenNoComponentsProvided()
    {
        when(fieldScreenRenderLayoutItem.isRequired()).thenReturn(true);
        when(componentIds.isEmpty()).thenReturn(true);
        when(project.getId()).thenReturn(123L);
        when(projectComponentManager.findAllForProject(123L)).thenReturn(Lists.<ProjectComponent>newArrayList(new MockProjectComponent(20L, "abc")));

        final ErrorCollection errors = new SimpleErrorCollection();
        boolean result = componentsSystemField.validateForRequiredField(errors, i18n, fieldScreenRenderLayoutItem, componentIds, project);

        assertFalse("Validation should have failed", result);
        assertEquals("issue.field.required [issue.field.components]", errors.getErrors().get("components"));
    }

    @Test
    public void fieldValidationFailsWhenNoComponentsConfiguredInProject()
    {
        when(fieldScreenRenderLayoutItem.isRequired()).thenReturn(true);
        when(componentIds.isEmpty()).thenReturn(true);
        when(project.getId()).thenReturn(123L);
        when(project.getName()).thenReturn("JRA");
        when(projectComponentManager.findAllForProject(123L)).thenReturn(Collections.<ProjectComponent>emptyList());

        final ErrorCollection errors = new SimpleErrorCollection();
        boolean result = componentsSystemField.validateForRequiredField(errors, i18n, fieldScreenRenderLayoutItem, componentIds, project);

        assertFalse("Validation should have failed", result);
        assertEquals("createissue.error.components.required [issue.field.components] [JRA]", Iterables.getFirst(errors.getErrorMessages(), null));
    }
}
