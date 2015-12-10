package com.atlassian.jira.issue.fields.renderer.text;

import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.issue.fields.renderer.JiraRendererPlugin;
import com.atlassian.jira.plugin.renderer.JiraRendererModuleDescriptor;
import com.atlassian.jira.util.JiraKeyUtils;
import com.opensymphony.util.TextUtils;

/**
 * A simple text renderer for jira..
 */
public class DefaultTextRenderer implements JiraRendererPlugin
{
    public static final String RENDERER_TYPE = "jira-text-renderer";

    private JiraRendererModuleDescriptor jiraRendererModuleDescriptor;

    public String render(String value, IssueRenderContext context)
    {
        return JiraKeyUtils.linkBugKeys(TextUtils.plainTextToHtml(value));
    }

    public String renderAsText(String value, IssueRenderContext context)
    {
        return value;
    }

    public String getRendererType()
    {
        return RENDERER_TYPE;
    }

    public Object transformForEdit(Object rawValue)
    {
        return rawValue;
    }

    public Object transformFromEdit(Object editValue)
    {
        return editValue;
    }

    public void init(JiraRendererModuleDescriptor jiraRendererModuleDescriptor)
    {
        this.jiraRendererModuleDescriptor = jiraRendererModuleDescriptor;
    }

    public JiraRendererModuleDescriptor getDescriptor()
    {
        return jiraRendererModuleDescriptor;
    }
}
