package com.atlassian.jira.web.action.admin.workflow.scheme;

import com.atlassian.jira.scheme.AbstractCopyScheme;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;

@WebSudoRequired
public class CopyScheme extends AbstractCopyScheme
{
    private final WorkflowSchemeManager workflowSchemeManager;

    public CopyScheme(final WorkflowSchemeManager workflowSchemeManager)
    {
        this.workflowSchemeManager = workflowSchemeManager;
    }

    public SchemeManager getSchemeManager()
    {
        return workflowSchemeManager;
    }

    public String getRedirectURL()
    {
        return "ViewWorkflowSchemes.jspa";
    }
}
