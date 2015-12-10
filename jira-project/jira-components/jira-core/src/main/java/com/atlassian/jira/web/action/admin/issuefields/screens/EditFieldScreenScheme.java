package com.atlassian.jira.web.action.admin.issuefields.screens;

import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItemImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.sal.api.websudo.WebSudoRequired;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
@WebSudoRequired
public class EditFieldScreenScheme extends AbstractFieldScreenSchemeAction
{
    private boolean edited;
    private final FieldScreenManager fieldScreenManager;

    public EditFieldScreenScheme(FieldScreenSchemeManager fieldScreenSchemeManager, FieldScreenManager fieldScreenManager)
    {
        super(fieldScreenSchemeManager);
        this.fieldScreenManager = fieldScreenManager;
    }

    public String doDefault() throws Exception
    {
        validateId();

        if (!invalidInput())
        {
            setFieldScreenSchemeName(getFieldScreenScheme().getName());
            setFieldScreenSchemeDescription(getFieldScreenScheme().getDescription());
            return INPUT;
        }

        return getResult();
    }

    protected void doValidation()
    {
        validateId();

        if (!invalidInput())
            validateName(true);
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        FieldScreenScheme fieldScreenScheme = getFieldScreenScheme();
        fieldScreenScheme.setName(getFieldScreenSchemeName());
        fieldScreenScheme.setDescription(getFieldScreenSchemeDescription());
        fieldScreenScheme.store();

        return redirectToView();
    }

    public String doViewCopyFieldScreenScheme()
    {
        validateId();

        if (!invalidInput())
        {
            setFieldScreenSchemeName(getText("common.words.copyof",getFieldScreenScheme().getName()));
            setFieldScreenSchemeDescription(getFieldScreenScheme().getDescription());
            return INPUT;
        }

        return getResult();
    }

    @RequiresXsrfCheck
    public String doCopyFieldScreenScheme()
    {
        validateId();

        if (!invalidInput())
        {
            // Create a copy of field screen
            validateName(false);

            if (!invalidInput())
            {
                FieldScreenScheme fieldScreenScheme = new FieldScreenSchemeImpl(getFieldScreenSchemeManager(), null);
                fieldScreenScheme.setName(getFieldScreenSchemeName());
                fieldScreenScheme.setDescription(getFieldScreenSchemeDescription());
                fieldScreenScheme.store();

                // Copy all teh scheme entities
                for (FieldScreenSchemeItem fieldScreenSchemeItem : getFieldScreenScheme().getFieldScreenSchemeItems())
                {
                    FieldScreenSchemeItem copyFieldScreenSchemeItem = new FieldScreenSchemeItemImpl(getFieldScreenSchemeManager(), fieldScreenSchemeItem, fieldScreenManager);
                    fieldScreenScheme.addFieldScreenSchemeItem(copyFieldScreenSchemeItem);
                }
            }

            return redirectToView();
        }

        return getResult();
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
