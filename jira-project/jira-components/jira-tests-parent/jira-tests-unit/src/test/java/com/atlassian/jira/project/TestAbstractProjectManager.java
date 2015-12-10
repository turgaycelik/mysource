package com.atlassian.jira.project;

import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestAbstractProjectManager
{
    private static final long PROJECT_ID = 1L;

    @Test
    public void gettingProjectByIssueGenericValueShouldDelegateToFindingProjectByItsId() throws Exception
    {
        // Set up
        final AbstractProjectManager projectManager = mock(AbstractProjectManager.class);
        when(projectManager.getProject(any(GenericValue.class))).thenCallRealMethod();
        final GenericValue mockIssueGV = mock(GenericValue.class);
        when(mockIssueGV.getLong("project")).thenReturn(PROJECT_ID);
        final GenericValue mockProjectGV = mock(GenericValue.class);
        when(projectManager.getProject(PROJECT_ID)).thenReturn(mockProjectGV);

        // Invoke
        final GenericValue project = projectManager.getProject(mockIssueGV);

        // Check
        assertSame(mockProjectGV, project);
    }
}
