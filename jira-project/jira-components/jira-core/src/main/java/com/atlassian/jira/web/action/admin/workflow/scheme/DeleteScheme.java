package com.atlassian.jira.web.action.admin.workflow.scheme;

import com.atlassian.jira.scheme.AbstractDeleteScheme;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.workflow.AssignableWorkflowScheme;
import com.atlassian.jira.workflow.SchemeIsBeingMigratedException;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.jira.workflow.migration.WorkflowSchemeMigrationTaskAccessor;
import com.atlassian.sal.api.websudo.WebSudoRequired;

@WebSudoRequired
public class DeleteScheme extends AbstractDeleteScheme
{
    private final WorkflowSchemeManager workflowSchemeManager;
    private final WorkflowSchemeMigrationTaskAccessor taskAccessor;

    public DeleteScheme(final WorkflowSchemeManager workflowSchemeManager, WorkflowSchemeMigrationTaskAccessor taskAccessor)
    {
        this.workflowSchemeManager = workflowSchemeManager;
        this.taskAccessor = taskAccessor;
    }

    @Override
    public SchemeManager getSchemeManager()
    {
        return workflowSchemeManager;
    }

    @Override
    public String getRedirectURL()
    {
        return "ViewWorkflowSchemes.jspa";
    }

    @Override
    protected void doValidation()
    {
        super.doValidation();

        AssignableWorkflowScheme workflowScheme = workflowSchemeManager.getWorkflowSchemeObj(getSchemeId());

        if (workflowScheme != null && taskAccessor.getActive(workflowScheme) != null)
        {
            addMigrationError();
        }
    }

    @Override
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        try
        {
            return super.doExecute();
        }
        catch (SchemeIsBeingMigratedException e)
        {
            addMigrationError();

            return ERROR;
        }
    }

    private void addMigrationError()
    {
        addErrorMessage(getText("admin.errors.deletescheme.cannot.delete.while.migrating"));
    }
}
