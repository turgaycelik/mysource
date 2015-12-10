package com.atlassian.sal.jira.pluginsettings;

import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import static org.mockito.Mockito.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import com.atlassian.jira.propertyset.JiraPropertySetFactory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.Project;
import com.opensymphony.module.propertyset.PropertySet;

@RunWith(MockitoJUnitRunner.class)
public class TestLazyProjectMigratingPropertySet
{
    private static final String KEY = "KEY";

    private PropertySet lazyProjectMigratingPropertySet;
    @Mock
    private JiraPropertySetFactory jiraPropertySetFactory;
    @Mock
    private ProjectManager projectManager;
    @Mock
    private PropertySet targetPropertySet;

    @Before
    public void setUp()
    {
        lazyProjectMigratingPropertySet =
                LazyProjectMigratingPropertySet.create(projectManager, jiraPropertySetFactory, targetPropertySet, KEY);
    }

    @Test
    public void testNormalMethod()
    {
        // Invoke
        lazyProjectMigratingPropertySet.getString(KEY);

        // Check
        verify(targetPropertySet).getString(KEY);
        verifyZeroInteractions(jiraPropertySetFactory);
    }

    @Test
    public void testNoMigrationNeeded()
    {
        // Set up
        when(targetPropertySet.exists(KEY)).thenReturn(true);

        // Invoke and check
        assertTrue(lazyProjectMigratingPropertySet.exists(KEY));
        verify(targetPropertySet).exists(KEY);
        verifyNoMoreInteractions(targetPropertySet);
        verifyZeroInteractions(jiraPropertySetFactory);
    }

    @Test
    public void testMissNotProjectKey()
    {
        // Invoke and check
        assertFalse(lazyProjectMigratingPropertySet.exists(KEY));
        verify(targetPropertySet).exists(KEY);
        verify(projectManager).getProjectObjByKey(KEY);
        // Try again
        assertFalse(lazyProjectMigratingPropertySet.exists(KEY));
        // Should be cached
        verifyNoMoreInteractions(projectManager);
    }

    @Test
    public void testMissOnProjectNoFallback()
    {
        // Set up
        final PropertySet propertySet = setupExistingProject();

        // Invoke and check
        assertFalse(lazyProjectMigratingPropertySet.exists(KEY));
        verify(propertySet).exists(KEY);
        verifyNoMoreInteractions(propertySet);
    }

    @Test
    public void testMissWithMigrateString()
    {
        // Set up
        final PropertySet propertySet = setupExistingProject();
        when(propertySet.exists(KEY)).thenReturn(true);
        when(propertySet.getType(KEY)).thenReturn(PropertySet.STRING);
        when(propertySet.getString(KEY)).thenReturn("value");

        // Invoke and check
        assertTrue(lazyProjectMigratingPropertySet.exists(KEY));
        verify(targetPropertySet).setString(KEY, "value");
        verify(propertySet).remove(KEY);
    }

    @Test
    public void testMissWithMigrateText()
    {
        final PropertySet propertySet = setupExistingProject();
        when(propertySet.exists(KEY)).thenReturn(true);
        when(propertySet.getType(KEY)).thenReturn(PropertySet.TEXT);
        when(propertySet.getText(KEY)).thenReturn("value");
        assertTrue(lazyProjectMigratingPropertySet.exists(KEY));
        verify(targetPropertySet).setText(KEY, "value");
        verify(propertySet).remove(KEY);
    }

    @Test
    public void testMissWithUnmigratableType()
    {
        PropertySet propertySet = setupExistingProject();
        when(propertySet.exists(KEY)).thenReturn(true);
        when(propertySet.getType(KEY)).thenReturn(PropertySet.INT);
        assertFalse(lazyProjectMigratingPropertySet.exists(KEY));
        verify(propertySet).exists(KEY);
        verify(propertySet).getType(KEY);
        verify(targetPropertySet).exists(KEY);
        verifyNoMoreInteractions(targetPropertySet, propertySet);
    }

    private PropertySet setupExistingProject()
    {
        final Project project = mock(Project.class);
        when(project.getId()).thenReturn(1L);
        when(projectManager.getProjectObjByKey(KEY)).thenReturn(project);

        final PropertySet propertySet = mock(PropertySet.class);
        when(jiraPropertySetFactory.buildCachingPropertySet("Project", 1L)).thenReturn(propertySet);
        return propertySet;
    }
}
