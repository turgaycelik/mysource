package com.atlassian.jira.web.action.admin.issuefields.enterprise;

import java.util.Collection;
import java.util.List;

import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestViewSchemes
{
    @Test
    public void getIssueFieldSchemesShouldRetrievesValuesFromFieldLayoutManager()
    {
        final FieldLayoutManager mockFieldLayoutManager = mock(FieldLayoutManager.class);

        final FieldLayoutScheme fieldLayoutScheme1 = mock(FieldLayoutScheme.class);
        final FieldLayoutScheme fieldLayoutScheme2 = mock(FieldLayoutScheme.class);
        final List<FieldLayoutScheme> fieldLayoutSchemes = ImmutableList.of(fieldLayoutScheme1, fieldLayoutScheme2);
        when(mockFieldLayoutManager.getFieldLayoutSchemes()).thenReturn(fieldLayoutSchemes);

        final ViewSchemes viewSchemes = new ViewSchemes(mockFieldLayoutManager);

        final List<FieldLayoutScheme> issueFieldSchemes = viewSchemes.getFieldLayoutScheme();
        assertThat(issueFieldSchemes, Matchers.contains(fieldLayoutScheme1, fieldLayoutScheme2));
    }

    @Test
    public void getSchemeProjectsShouldRetrievesProjectFromFieldLayoutManagerAndSortThemByName()
    {
        final FieldLayoutManager mockFieldLayoutManager = mock(FieldLayoutManager.class);
        final FieldLayoutScheme mockFieldLayoutScheme = mock(FieldLayoutScheme.class);
        when(mockFieldLayoutScheme.getId()).thenReturn(1L);

        final GenericValue projectA = new MockGenericValue("Project", ImmutableMap.of("name", "project A"));
        final GenericValue projectB = new MockGenericValue("Project", ImmutableMap.of("name", "project B"));
        when(mockFieldLayoutManager.getProjects(mockFieldLayoutScheme)).thenReturn(ImmutableList.of(projectB, projectA));

        final ViewSchemes viewSchemes = new ViewSchemes(mockFieldLayoutManager);

        final Collection<GenericValue> schemeProjects = viewSchemes.getSchemeProjects(mockFieldLayoutScheme);
        assertThat(schemeProjects, Matchers.<GenericValue>contains(projectA, projectB));
    }
}
