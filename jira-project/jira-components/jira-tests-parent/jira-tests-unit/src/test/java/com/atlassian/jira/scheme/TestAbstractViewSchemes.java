package com.atlassian.jira.scheme;

import java.util.List;

import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class TestAbstractViewSchemes
{
    @Test
    public void getSchemesShouldRetrieveSchemesFromSchemeManager() throws GenericEntityException
    {
        final GenericValue schemeA = new MockGenericValue("NotificationScheme", ImmutableMap.of("name", "scheme A"));
        final GenericValue schemeB = new MockGenericValue("NotificationScheme", ImmutableMap.of("name", "scheme B"));
        final SchemeManager schemeManager = mock(SchemeManager.class);
        Mockito.when(schemeManager.getSchemes()).thenReturn(ImmutableList.of(schemeA, schemeB));

        final AbstractViewSchemes viewSchemes = new ViewSchemesMock(schemeManager);

        final List<GenericValue> schemes = viewSchemes.getSchemes();

        assertThat(schemes, Matchers.contains(schemeA, schemeB));
    }

    @Test
    public void getSchemeObjectsShouldRetrieveSchemesFromSchemeManager() throws GenericEntityException
    {
        final Scheme schemeA = new Scheme("scheme type", "scheme A");
        final Scheme schemeB = new Scheme("scheme type", "scheme B");
        final SchemeManager schemeManager = mock(SchemeManager.class);
        Mockito.when(schemeManager.getSchemeObjects()).thenReturn(ImmutableList.of(schemeA, schemeB));

        final AbstractViewSchemes viewSchemes = new ViewSchemesMock(schemeManager);

        final List<Scheme> schemes = viewSchemes.getSchemeObjects();

        assertThat(schemes, Matchers.contains(schemeA, schemeB));
    }

    @Test
    public void getProjectsGenericValueShouldRetrieveSchemesFromSchemeManager() throws GenericEntityException
    {
        final GenericValue scheme = new MockGenericValue("Scheme");

        final GenericValue projectA = new MockGenericValue("Project", ImmutableMap.of("name", "project A"));
        final GenericValue projectB = new MockGenericValue("Project", ImmutableMap.of("name", "project B"));
        final SchemeManager schemeManager = mock(SchemeManager.class);
        Mockito.when(schemeManager.getProjects(scheme)).thenReturn(ImmutableList.of(projectA, projectB));

        final AbstractViewSchemes viewSchemes = new ViewSchemesMock(schemeManager);

        final List<GenericValue> schemes = viewSchemes.getProjects(scheme);

        assertThat(schemes, Matchers.contains(projectA, projectB));
    }

    @Test
    public void getProjectsShouldRetrieveSchemesFromSchemeManager() throws GenericEntityException
    {
        final Scheme scheme = new Scheme("scheme type", "test scheme");

        final Project projectA = new MockProject(1l, "PA", "Project A");
        final Project projectB = new MockProject(2l, "PB", "Project B");

        final SchemeManager schemeManager = mock(SchemeManager.class);
        Mockito.when(schemeManager.getProjects(scheme)).thenReturn(ImmutableList.of(projectA, projectB));

        final AbstractViewSchemes viewSchemes = new ViewSchemesMock(schemeManager);

        final List<Project> schemes = viewSchemes.getProjects(scheme);

        assertThat(schemes, Matchers.contains(projectA, projectB));
    }

    private final class ViewSchemesMock extends AbstractViewSchemes {

        private final SchemeManager schemeManager;

        private ViewSchemesMock(final SchemeManager schemeManager) {
            this.schemeManager = schemeManager;
        }

        @Override
        public SchemeManager getSchemeManager()
        {
            return schemeManager;
        }

        @Override
        public String getRedirectURL() throws GenericEntityException
        {
            throw new AssertionError("Unexpected method call");
        }
    }
}
