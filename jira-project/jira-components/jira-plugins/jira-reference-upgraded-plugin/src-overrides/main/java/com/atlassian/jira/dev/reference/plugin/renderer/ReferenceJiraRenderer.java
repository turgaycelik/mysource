package com.atlassian.jira.dev.reference.plugin.renderer;

import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.issue.fields.renderer.JiraRendererPlugin;
import com.atlassian.jira.plugin.renderer.JiraRendererModuleDescriptor;
import com.atlassian.jira.util.JiraKeyUtils;
import com.opensymphony.util.TextUtils;

/**
 * Upgraded reference JIRA renderer implementation. It returns raw text with a prefix
 *
 * @since 4.4
 */
public class ReferenceJiraRenderer implements JiraRendererPlugin
{
    public static final String RENDERER_TYPE = "jira-reference-renderer";

    private JiraRendererModuleDescriptor jiraRendererModuleDescriptor;

    public String render(String value, IssueRenderContext context)
    {
        return prefix() + JiraKeyUtils.linkBugKeys(TextUtils.plainTextToHtml(value));
    }

    /**
     * Prefix for rendered text, may be HTML.
     *
     * @return prefix
     */
    protected String prefix()
    {
        return "<h2>RENDERED BY !UGRADED! REFERENCE PLUGIN</h2>";
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
