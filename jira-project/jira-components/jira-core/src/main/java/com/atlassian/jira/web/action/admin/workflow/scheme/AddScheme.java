package com.atlassian.jira.web.action.admin.workflow.scheme;

import com.atlassian.jira.scheme.AbstractAddScheme;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;

@WebSudoRequired
public class AddScheme extends AbstractAddScheme
{
    private final WorkflowSchemeManager workflowSchemeManager;

    public AddScheme(final WorkflowSchemeManager workflowSchemeManager)
    {
        this.workflowSchemeManager = workflowSchemeManager;
    }

    public SchemeManager getSchemeManager()
    {
        return workflowSchemeManager;
    }

    public String getRedirectURL()
    {
        return "EditWorkflowScheme.jspa?schemeId=";
    }
}
