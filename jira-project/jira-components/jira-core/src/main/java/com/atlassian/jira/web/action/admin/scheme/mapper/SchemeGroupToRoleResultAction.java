package com.atlassian.jira.web.action.admin.scheme.mapper;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.scheme.SchemeFactory;
import com.atlassian.jira.scheme.SchemeManagerFactory;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import webwork.action.ActionContext;

/**
 * This is the last step of the GroupToRoleMapping tool, this simply displays the summary results.
 */
@WebSudoRequired
public class SchemeGroupToRoleResultAction extends AbstractGroupToRoleAction
{
    public SchemeGroupToRoleResultAction(SchemeManagerFactory schemeManagerFactory, SchemeFactory schemeFactory, ApplicationProperties applicationProperties)
    {
        super(schemeManagerFactory, schemeFactory, applicationProperties);
    }

    public String doDefault() throws Exception
    {
        //Group to role mapping has finished, so cleanup the session
        resetSelectedSchemeIds();
        ActionContext.getSession().remove(AbstractGroupToRoleAction.GROUP_TO_ROLE_MAP_SESSION_KEY);
        //NOTE: do not remove the TRANSFORM_RESULTS_KEY from the session, otherwise the view will not show the results
        return SUCCESS;
    }
}
