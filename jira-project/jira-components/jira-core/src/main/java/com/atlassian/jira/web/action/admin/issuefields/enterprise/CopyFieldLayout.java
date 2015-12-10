package com.atlassian.jira.web.action.admin.issuefields.enterprise;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayoutImpl;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.sal.api.websudo.WebSudoRequired;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
@WebSudoRequired
public class CopyFieldLayout extends AbstractFieldLayoutAction
{
    private EditableFieldLayout editableFieldLayout;

    public CopyFieldLayout(FieldLayoutManager fieldLayoutManager)
    {
        super(fieldLayoutManager);
    }

    public String doDefault() throws Exception
    {
        validateFieldLayout();

        if (!invalidInput())
        {

            setFieldLayoutName(ComponentAccessor.getJiraAuthenticationContext().getI18nHelper().getText("common.words.copyof", getFieldLayout().getName()));
            setFieldLayoutDescription(getFieldLayout().getDescription());

        }

        if (!invalidInput())
            return INPUT;
        else
            return ERROR;
    }

    protected void doValidation()
    {
        validateFieldLayout();

        if (!invalidInput())
        {
            validateName();

            if (!invalidInput())
            {
                // Ensure that no other scheme with this name exists
                for (final EditableFieldLayout editableFieldLayout1 : getFieldLayouts())
                {
                    FieldLayout fieldLayout = (FieldLayout) editableFieldLayout1;
                    if ((getId() == null || !getId().equals(fieldLayout.getId())) && getFieldLayoutName().equals(fieldLayout.getName()))
                    {
                        addError("fieldLayoutName", getText("admin.errors.fieldlayout.name.exists"));
                    }
                }
            }
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        // Create a field layout with the same field properties as the default field layout
        EditableFieldLayout editableFieldLayout = new EditableFieldLayoutImpl(null, getFieldLayout().getFieldLayoutItems());
        editableFieldLayout.setName(getFieldLayoutName());
        editableFieldLayout.setDescription(getFieldLayoutDescription());
        getFieldLayoutManager().storeEditableFieldLayout(editableFieldLayout);
        return redirectToView();
    }

    public EditableFieldLayout getFieldLayout()
    {
        if (editableFieldLayout == null)
        {
            if (getId() == null)
            {
                editableFieldLayout = getFieldLayoutManager().getEditableDefaultFieldLayout();
            }
            else
            {
                editableFieldLayout = getFieldLayoutManager().getEditableFieldLayout(getId());
            }
        }

        return editableFieldLayout;
    }
}
