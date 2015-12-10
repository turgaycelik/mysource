package com.atlassian.jira.web.action.admin.issuefields.enterprise;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.sal.api.websudo.WebSudoRequired;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
@WebSudoRequired
public class CopyFieldLayoutScheme extends AbstractEditFieldLayoutSchemeAction
{
    public CopyFieldLayoutScheme(FieldLayoutManager fieldLayoutManager)
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
                    if (getFieldLayoutSchemeName().equals(fieldLayoutScheme.getName()))
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
        getFieldLayoutManager().copyFieldLayoutScheme(getFieldLayoutScheme(), getFieldLayoutSchemeName(), getFieldLayoutSchemeDescription());

        return redirectToView();
    }

    protected String getInitialName()
    {
        return ComponentAccessor.getJiraAuthenticationContext().getI18nHelper().getText("common.words.copyof", getFieldLayoutScheme().getName());
    }

}
