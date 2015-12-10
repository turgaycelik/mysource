package com.atlassian.jira.plugin.renderer;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.LookAndFeelBean;
import com.atlassian.jira.issue.fields.renderer.JiraRendererPlugin;
import com.atlassian.jira.plugin.AbstractJiraModuleDescriptor;
import com.atlassian.jira.plugin.JiraResourcedModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.module.ModuleFactory;
import com.opensymphony.util.TextUtils;
import webwork.action.ServletActionContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Descriptor that defines a JiraRendererModule.
 */
public class JiraRendererModuleDescriptorImpl extends AbstractJiraModuleDescriptor<JiraRendererPlugin> implements JiraRendererModuleDescriptor
{
    public static final String TEMPLATE_NAME_CSS = "css";
    public static final String TEMPLATE_NAME_JS = "javascript";
    public static final String TEMPLATE_NAME_EDIT = "edit";

    private ApplicationProperties applicationProperties;

    public JiraRendererModuleDescriptorImpl(JiraAuthenticationContext authenticationContext, ApplicationProperties applicationProperties, final ModuleFactory moduleFactory)
    {
        super(authenticationContext, moduleFactory);
        this.applicationProperties = applicationProperties;
    }

    public String getCss()
    {
        Map<String, Object> params = new HashMap<String, Object>();
        LookAndFeelBean lookAndFeelBean = LookAndFeelBean.getInstance(applicationProperties);
        params.put("menuTxtColour", lookAndFeelBean.getMenuTxtColour());
        params.put("textHighlightColour", lookAndFeelBean.getTextHeadingColour());
        params.put("menuBackgroundColour", lookAndFeelBean.getMenuBackgroundColour());
        params.put("req", ServletActionContext.getRequest());
        return getHtml(TEMPLATE_NAME_CSS, params);
    }

    public String getJavaScript(String contextPath)
    {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("i18n", getI18nBean());
        params.put("contextPath", contextPath);
        return getHtml(TEMPLATE_NAME_JS, params);
    }

    /**
     * This render an editable input field for the given values based on the edit template provided for the renderer.
     * @return the renderered html for the edit input
     */
    public String getEditVM(String value, String issueKey, String rendererType, String fieldId, String fieldName, Map params, boolean singleLine)
    {
        if(!isResourceExist(TEMPLATE_NAME_EDIT))
            throw new IllegalStateException("An edit velocity template is a required resource for a renderer module.");

        if(params == null)
        {
            params = new HashMap();
        }
        JiraRendererPlugin renderer = getModule();
        params.put("req", ServletActionContext.getRequest());
        params.put("fieldId", fieldId);
        params.put("fieldName", fieldName);
        params.put("issueKey", issueKey);
        params.put("rendererType", renderer.getRendererType());
        params.put("value", renderer.transformForEdit(value));
        params.put("textutils", new TextUtils());
        params.put("i18n", getI18nBean());
        if (singleLine)
        {
            params.put("singleLine", Boolean.TRUE);
        }

        String editHtml = getHtml(TEMPLATE_NAME_EDIT, params);
        if(!renderer.getRendererType().equals(rendererType) && rendererType != null && !"null".equals(rendererType))
        {
            editHtml = getI18nBean().getText("renderer.not.available.message", rendererType, getName()) + editHtml;
        }
        return editHtml;
    }

    public boolean isCSSTemplateExists()
    {
        return isResourceExist(TEMPLATE_NAME_CSS);
    }

    public boolean isJavaScriptTemplateExists()
    {
        return isResourceExist(TEMPLATE_NAME_JS);
    }

    public boolean isEditTemplateExists()
    {
        return isResourceExist(TEMPLATE_NAME_EDIT);
    }

}
