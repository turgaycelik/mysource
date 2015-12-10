package com.atlassian.jira.project.renderer;

import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.issue.fields.renderer.JiraRendererPlugin;
import com.atlassian.jira.issue.fields.renderer.wiki.AtlassianWikiRenderer;
import com.atlassian.jira.plugin.renderer.JiraRendererModuleDescriptor;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WikiMarkupProjectDescriptionRendererTest
{
    private static final String PROJECT_DESCRIPTION = "_My_ Project *Description*";
    private static final String RENDERER_RESULT = "result";

    private final RendererManager mockRendererManager = mock(RendererManager.class);
    private final JiraRendererPlugin mockJiraPlugin = mock(JiraRendererPlugin.class);

    private final JiraRendererModuleDescriptor mockJiraRendererPlugin = mock(JiraRendererModuleDescriptor.class);
    private final ProjectDescriptionRenderer renderer = new WikiMarkupProjectDescriptionRenderer(mockRendererManager);

    @Before
    public void setUpMocks()
    {
        when(mockRendererManager.getRendererForType(AtlassianWikiRenderer.RENDERER_TYPE)).thenReturn(mockJiraPlugin);
        when(mockJiraPlugin.getDescriptor()).thenReturn(mockJiraRendererPlugin);
    }

    @Test
    public void testGetViewHtml() throws Exception
    {
        when(mockJiraPlugin.render(eq(PROJECT_DESCRIPTION), any(IssueRenderContext.class))).thenReturn(RENDERER_RESULT);
        
        final String html = renderer.getViewHtml(PROJECT_DESCRIPTION);
        assertThat(html, is(RENDERER_RESULT));
    }

    @Test
    public void testGetEditHtml() throws Exception
    {
        when(mockJiraRendererPlugin.getEditVM(eq(PROJECT_DESCRIPTION), any(String.class), eq(AtlassianWikiRenderer.RENDERER_TYPE), eq("description"), eq("description"), anyMap(), anyBoolean())).thenReturn(RENDERER_RESULT);

        final String html = renderer.getEditHtml(PROJECT_DESCRIPTION);
        assertThat(html, is(RENDERER_RESULT));
    }

}
