package com.atlassian.jira.plugin.renderer;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.fields.renderer.JiraRendererPlugin;
import com.atlassian.jira.plugin.JiraResourcedModuleDescriptor;

import java.util.Map;

/**
 * Descriptor that defines a JiraRendererModule.
 */
@PublicApi
public interface JiraRendererModuleDescriptor extends JiraResourcedModuleDescriptor<JiraRendererPlugin>
{
    public static final String TEMPLATE_NAME_CSS = "css";
    public static final String TEMPLATE_NAME_JS = "javascript";
    public static final String TEMPLATE_NAME_EDIT = "edit";

    public String getCss();

    public String getJavaScript(String contextPath);

    /**
     * This render an editable input field for the given values based on the edit template provided for the renderer.
     * @return the renderered html for the edit input
     */
    public String getEditVM(String value, String issueKey, String rendererType, String fieldId, String fieldName, Map params, boolean singleLine);

    public boolean isCSSTemplateExists();

    public boolean isJavaScriptTemplateExists();

    public boolean isEditTemplateExists();
}
