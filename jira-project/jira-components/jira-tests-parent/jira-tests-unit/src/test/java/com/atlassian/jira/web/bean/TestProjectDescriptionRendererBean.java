package com.atlassian.jira.web.bean;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.renderer.ProjectDescriptionRenderer;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ofbiz.core.entity.GenericValue;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestProjectDescriptionRendererBean
{
    private static final String PROJECT_DESCRIPTION = "project description";

    private final Project project = mock(Project.class);
    private final GenericValue genericValue = mock(GenericValue.class);
    private final ProjectDescriptionRenderer projectDescriptionRenderer = mock(ProjectDescriptionRenderer.class);

    private final ProjectDescriptionRendererBean bean = new ProjectDescriptionRendererBean(projectDescriptionRenderer);

    @Test
    public void getViewHtmlFromProject()
    {
        when(project.getDescription()).thenReturn(PROJECT_DESCRIPTION);
        when(projectDescriptionRenderer.getViewHtml(any(String.class))).thenAnswer(new Answer<String>()
        {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable
            {
                return (String) invocation.getArguments()[0];
            }
        });
        final String result = bean.getViewHtml(project);
        assertThat(result, is(PROJECT_DESCRIPTION));
    }

    @Test
    public void getViewHtmlFromGenericValue()
    {
        when(projectDescriptionRenderer.getViewHtml(any(String.class))).thenReturn(PROJECT_DESCRIPTION);
        final String result = bean.getViewHtml(genericValue);
        assertThat(result, is(PROJECT_DESCRIPTION));
    }
}
