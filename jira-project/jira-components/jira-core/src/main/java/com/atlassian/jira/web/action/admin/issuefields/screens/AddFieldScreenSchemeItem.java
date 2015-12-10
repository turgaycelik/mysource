package com.atlassian.jira.web.action.admin.issuefields.screens;

import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItemImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.issue.operation.ScreenableIssueOperation;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.sal.api.websudo.WebSudoRequired;

@WebSudoRequired
public class AddFieldScreenSchemeItem extends AbstractFieldScreenSchemeItemAction
{
    public AddFieldScreenSchemeItem(FieldScreenSchemeManager fieldScreenSchemeManager, FieldScreenManager fieldScreenManager)
    {
        super(fieldScreenSchemeManager, fieldScreenManager);
    }

    protected void doValidation()
    {
        validateIssueOperationId();
        validateFieldScreenId();
    }

    public String doInput()
    {
        return INPUT;
    }
    
    @RequiresXsrfCheck
    protected String doExecute()
    {
        FieldScreenSchemeItem fieldScreenSchemeItem = new FieldScreenSchemeItemImpl(getFieldScreenSchemeManager(), getFieldScreenManager());
        if (getIssueOperationId() != null)
        {
            fieldScreenSchemeItem.setIssueOperation(IssueOperations.getIssueOperation(getIssueOperationId()));
        }
        else
        {
            fieldScreenSchemeItem.setIssueOperation(null);
        }

        fieldScreenSchemeItem.setFieldScreen(getFieldScreenManager().getFieldScreen(getFieldScreenId()));
        getFieldScreenScheme().addFieldScreenSchemeItem(fieldScreenSchemeItem);
        return redirectToView();
    }

    // This is here to overcome the annoying webwork hack to lookup the stack if null is returned!
    public Long getIssueOperaionId(ScreenableIssueOperation issueOperation)
    {
        return issueOperation.getId();
    }
}
