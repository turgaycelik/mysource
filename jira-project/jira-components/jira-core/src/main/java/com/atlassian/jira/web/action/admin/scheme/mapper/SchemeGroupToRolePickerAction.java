package com.atlassian.jira.web.action.admin.scheme.mapper;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.scheme.SchemeFactory;
import com.atlassian.jira.scheme.SchemeManagerFactory;
import com.atlassian.jira.web.action.admin.scheme.AbstractSchemePickerAction;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import webwork.action.ActionContext;

/**
 * This is the first step in the GroupToRoleMapping tool, it collects the selected scheme information for the tool.
 */
@WebSudoRequired
public class SchemeGroupToRolePickerAction extends AbstractSchemePickerAction
{
    public SchemeGroupToRolePickerAction(SchemeManagerFactory schemeManagerFactory, SchemeFactory schemeFactory, ApplicationProperties applicationProperties)
    {
        super(schemeManagerFactory, schemeFactory, applicationProperties);
    }

    public String doDefault()
    {
        // When starting the wizard make sure we are not using any old sessions
        resetSelectedSchemeIds();
        ActionContext.getSession().remove(AbstractGroupToRoleAction.GROUP_TO_ROLE_MAP_SESSION_KEY);
        ActionContext.getSession().remove(AbstractGroupToRoleAction.TRANSFORM_RESULTS_KEY);

        return super.doDefault();
    }

    public String getRedirectPage()
    {
        return "SchemeGroupToRoleMapper!default.jspa";
    }

    public String getToolName()
    {
        return AbstractGroupToRoleAction.SCHEME_TOOL_NAME;
    }
}
