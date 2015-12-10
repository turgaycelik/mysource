package com.atlassian.jira.web.action.admin.issuefields.screens;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.issue.fields.screen.FieldScreenService;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.sal.api.websudo.WebSudoRequired;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
@WebSudoRequired
public class EditFieldScreen extends AbstractFieldScreenAction
{
    private final FieldScreenService fieldScreenService;

    private boolean edited;

    public EditFieldScreen(FieldScreenManager fieldScreenManager, FieldScreenService fieldScreenService)
    {
        super(fieldScreenManager);
        this.fieldScreenService = fieldScreenService;
    }

    public String doDefault() throws Exception
    {
        validateId();

        if (invalidInput())
        {
            return ERROR;
        }
        else
        {
            setFieldScreenName(getFieldScreen().getName());
            setFieldScreenDescription(getFieldScreen().getDescription());
            return INPUT;
        }
    }

    protected void doValidation()
    {
        validateId();

        if (!invalidInput())
        {
            validateScreenName();
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        getFieldScreen().setName(getFieldScreenName());
        getFieldScreen().setDescription(getFieldScreenDescription());
        getFieldScreen().store();
        return redirectToView();
    }

    public void setFieldScreen(FieldScreen fieldScreen)
    {
        this.fieldScreen = fieldScreen;
    }

    @RequiresXsrfCheck
    public String doCopyFieldScreen()
    {
        validateId();

        ServiceOutcome<FieldScreen> result = fieldScreenService.copy(getFieldScreen(), getFieldScreenName(),
                getFieldScreenDescription(), getLoggedInApplicationUser());

        if (!result.isValid())
        {
            addError("fieldScreenName", result.getErrorCollection().getErrorMessages().iterator().next());

            return ERROR;
        }

        return redirectToView();
    }

    public String doViewCopyFieldScreen()
    {
        validateId();

        // Get the name and description of the new screen from the user.
        setFieldScreenName(getText("common.words.copyof",getFieldScreen().getName()));
        setFieldScreenDescription(getFieldScreen().getDescription());
        return INPUT;
    }

    public boolean isEdited()
    {
        return edited;
    }

    public void setEdited(boolean edited)
    {
        this.edited = edited;
    }
}
