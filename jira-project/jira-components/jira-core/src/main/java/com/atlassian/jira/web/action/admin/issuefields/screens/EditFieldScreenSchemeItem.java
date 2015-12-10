package com.atlassian.jira.web.action.admin.issuefields.screens;

import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.sal.api.websudo.WebSudoRequired;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
@WebSudoRequired
public class EditFieldScreenSchemeItem extends AbstractFieldScreenSchemeItemAction
{
    public EditFieldScreenSchemeItem(FieldScreenSchemeManager fieldScreenSchemeManager, FieldScreenManager fieldScreenManager)
    {
        super(fieldScreenSchemeManager, fieldScreenManager);
    }

    public String doDefault() throws Exception
    {
        validateIssueOperationId();

        if (!invalidInput())
        {
            FieldScreenSchemeItem fieldScreenSchemeItem = getFieldScreenScheme().getFieldScreenSchemeItem(getIssueOperation());
            setFieldScreenId(fieldScreenSchemeItem.getFieldScreen().getId());
            return INPUT;
        }

        return getResult();
    }

    protected void doValidation()
    {
        validateIssueOperationId();

        // Check that the field screen scheme item for this issue operation exists
        if (!invalidInput())
        {
            validateFieldScreenId();
        }
    }

    protected void validateIssueOperationId()
    {
        super.validateIssueOperationId();

        if (!invalidInput() && getFieldScreenScheme().getFieldScreenSchemeItem(getIssueOperation()) == null)
        {
            addErrorMessage(getText("admin.errors.screens.no.scheme.item.exists.with.id","'" + getIssueOperationId() + "'"));
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        FieldScreenSchemeItem fieldScreenSchemeItem = getFieldScreenScheme().getFieldScreenSchemeItem(getIssueOperation());
        fieldScreenSchemeItem.setFieldScreen(getFieldScreenManager().getFieldScreen(getFieldScreenId()));
        fieldScreenSchemeItem.store();
        return redirectToView();
    }
}
