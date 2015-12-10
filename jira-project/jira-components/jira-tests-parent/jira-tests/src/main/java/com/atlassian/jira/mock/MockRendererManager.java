package com.atlassian.jira.mock;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.issue.fields.renderer.JiraRendererPlugin;
import com.atlassian.jira.issue.renderers.FieldRenderContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockRendererManager implements RendererManager
{
    private Map<String, JiraRendererPlugin> typeToRenderer = new HashMap<String, JiraRendererPlugin>();

    public List<JiraRendererPlugin> getAllActiveRenderers()
    {
        return new java.util.ArrayList<JiraRendererPlugin>();
    }

    public JiraRendererPlugin getRendererForType(String rendererType)
    {
        return typeToRenderer.get(rendererType);
    }

    public JiraRendererPlugin getRendererForField(FieldLayoutItem fieldConfig)
    {
        throw new UnsupportedOperationException();
    }

    public String getRenderedContent(FieldLayoutItem fieldConfig, Issue issue)
    {
        throw new UnsupportedOperationException();
    }

    public String getRenderedContent(String rendererType, String value, IssueRenderContext renderContext)
    {
        return value;
    }

    public String getRenderedContent(FieldRenderContext fieldRenderContext)
    {
        throw new UnsupportedOperationException();
    }

    public MockRendererManager setRendererForType(final String rendererType, final JiraRendererPlugin jiraRendererPlugin)
    {
        typeToRenderer.put(rendererType, jiraRendererPlugin);
        return this;
    }
}
