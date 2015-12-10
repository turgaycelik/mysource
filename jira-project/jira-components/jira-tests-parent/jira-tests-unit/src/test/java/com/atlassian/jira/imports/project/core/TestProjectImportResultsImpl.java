package com.atlassian.jira.imports.project.core;

import java.io.Serializable;

import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockComponentContainer;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.I18nHelper;

import org.hamcrest.core.IsInstanceOf;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.apache.commons.lang3.SerializationUtils.deserialize;
import static org.apache.commons.lang3.SerializationUtils.serialize;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * @since v3.13
 */
@RunWith (MockitoJUnitRunner.class)
public class TestProjectImportResultsImpl
{
    @Rule
    public MockComponentContainer container = new MockComponentContainer(this);

    @Mock @AvailableInContainer
    private I18nHelper i18nHelper;
    @Mock @AvailableInContainer
    private ProjectManager mockProjectManager;

    @Test
    public void testAbortExceptionThrownWhenErrorLimitHit() throws Exception
    {
        ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, null);

        for (int i = 0; i < projectImportResults.getErrorCountLimit() - 1; i++)
        {
            projectImportResults.addError("error " + i);
            assertFalse(projectImportResults.abortImport());
        }

        // Now add the error that pushes it over the edge
        projectImportResults.addError("last errror");
        assertTrue(projectImportResults.abortImport());
    }

    @Test
    public void testGetProjectRoles() throws Exception
    {
        ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, null);

        projectImportResults.incrementRoleGroupCreatedCount("Role 1");
        projectImportResults.incrementRoleUserCreatedCount("Role 2");

        assertEquals(2, projectImportResults.getRoles().size());
        assertTrue(projectImportResults.getRoles().contains("Role 1"));
        assertTrue(projectImportResults.getRoles().contains("Role 2"));
        assertEquals(1, projectImportResults.getGroupsCreatedCountForRole("Role 1"));
        assertEquals(1, projectImportResults.getUsersCreatedCountForRole("Role 2"));
    }

    @Test
    public void testSerialization()
    {
        final Project importedProject = new MockProject(123L, "PR1", "Project_1");
        final MockI18nHelper i18n = new MockI18nHelper();
        // Verify that the 2 above instances are not serializable
        assertThat(importedProject, not(instanceOf(Serializable.class)));
        assertThat(i18n, not(instanceOf(Serializable.class)));

        when(mockProjectManager.getProjectObj(any(Long.class))).thenReturn(importedProject);

        ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(1, 2, 3, 4, i18n);
        projectImportResults.setImportedProject(importedProject);
        projectImportResults.setEndTime(100);
        // Invoke
        final ProjectImportResults roundTrippedObject = (ProjectImportResults) deserialize(serialize(projectImportResults));

        // Check
        assertThat(roundTrippedObject.getImportDuration(), is(99L));
        assertThat(roundTrippedObject.getExpectedIssuesCreatedCount(), is(2));
        assertThat(roundTrippedObject.getExpectedUsersCreatedCount(), is(3));
        assertThat(roundTrippedObject.getExpectedAttachmentsCreatedCount(), is(4));

        assertThat(roundTrippedObject.getImportedProject(), is(importedProject));
        assertThat(roundTrippedObject.getI18n(), is(i18nHelper));

    }
}
