package com.atlassian.jira.web.action.admin.issuefields.enterprise;

import java.util.Collection;

import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.sal.api.websudo.WebSudoRequired;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
@WebSudoRequired
public class EditFieldLayoutScheme extends AbstractEditFieldLayoutSchemeAction
{
    private String confirm;

    public EditFieldLayoutScheme(FieldLayoutManager fieldLayoutManager)
    {
        super(fieldLayoutManager);
    }

    protected void doValidation()
    {
        validateName();

        if (!invalidInput())
        {
            validateId();

            if (!invalidInput())
            {
                for (final FieldLayoutScheme fieldLayoutScheme : getFieldLayoutSchemes())
                {
                    if (!getId().equals(fieldLayoutScheme.getId()) && getFieldLayoutSchemeName().equals(fieldLayoutScheme.getName()))
                    {
                        addError("fieldLayoutSchemeName", getText("admin.errors.fieldlayout.scheme.name.exists"));
                    }
                }

                if (!invalidInput())
                {
                    validateFieldLayoutScheme();
                }
            }
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        getFieldLayoutScheme().setName(getFieldLayoutSchemeName());
        getFieldLayoutScheme().setDescription(getFieldLayoutSchemeDescription());
        getFieldLayoutScheme().store();

        return redirectToView();
    }

    @RequiresXsrfCheck
    public String doDeleteScheme()
    {
        validateId();

        if (!invalidInput())
        {
            validateFieldLayoutScheme();

            if (!invalidInput())
            {
                Collection schemeProjects = getSchemeProjects(getFieldLayoutScheme());
                if (schemeProjects != null && !schemeProjects.isEmpty())
                {
                    addErrorMessage(getText("admin.errors.fieldlayout.cannot.delete"));
                }

                if (!invalidInput())
                {
                    if (!Boolean.valueOf(getConfirm()))
                    {
                        return "confirm";
                    }
                    getFieldLayoutScheme().remove();
                    return redirectToView();
                }
            }
        }

        return getResult();
    }

    public String getConfirm()
    {
        return confirm;
    }

    public void setConfirm(String confirm)
    {
        this.confirm = confirm;
    }

    protected String getInitialName()
    {
        return getFieldLayoutScheme().getName();
    }
}
