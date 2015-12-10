package com.atlassian.jira.web.action.admin.issuefields.enterprise;

import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.sal.api.websudo.WebSudoRequired;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
@WebSudoRequired
public class EditFieldLayout extends AbstractFieldLayoutAction
{
    public EditFieldLayout(FieldLayoutManager fieldLayoutManager)
    {
        super(fieldLayoutManager);
    }

    public String doDefault() throws Exception
    {
        validateId();

        if (!invalidInput())
        {
            validateFieldLayout();

            if (!invalidInput())
            {
                setFieldLayoutName(getFieldLayout().getName());
                setFieldLayoutDescription(getFieldLayout().getDescription());
            }
        }

        if (!invalidInput())
            return INPUT;
        else
            return ERROR;
    }

    protected void doValidation()
    {
        validateId();

        if (!invalidInput())
        {
            validateFieldLayout();

            if (!invalidInput())
            {
                validateName();

                if (!invalidInput())
                {
                    // Ensure that no other scheme with this name exists
                    for (final EditableFieldLayout editableFieldLayout : getFieldLayouts())
                    {
                        FieldLayout fieldLayout = (FieldLayout) editableFieldLayout;
                        if (!getId().equals(fieldLayout.getId()) && getFieldLayoutName().equals(fieldLayout.getName()))
                        {
                            addError("fieldLayoutName", getText("admin.errors.fieldlayout.name.exists"));
                        }
                    }
                }
            }
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        getFieldLayout().setName(getFieldLayoutName());
        getFieldLayout().setDescription(getFieldLayoutDescription());
        getFieldLayoutManager().storeEditableFieldLayout(getFieldLayout());

        return redirectToView();
    }
}
