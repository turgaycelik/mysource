package com.atlassian.jira.web.action.admin.issuefields.enterprise;

import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.opensymphony.util.TextUtils;

import java.util.Collection;
import java.util.List;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public class AbstractFieldLayoutAction extends JiraWebActionSupport
{
    private final FieldLayoutManager fieldLayoutManager;

    private Long id;
    private String fieldLayoutName;
    private String fieldLayoutDescription;
    private List<EditableFieldLayout> editableFieldLayouts;
    private EditableFieldLayout editableFieldLayout;

    public AbstractFieldLayoutAction(FieldLayoutManager fieldLayoutManager)
    {
        this.fieldLayoutManager = fieldLayoutManager;
    }

    public Collection<EditableFieldLayout> getFieldLayouts()
    {
        if (editableFieldLayouts == null)
        {
            editableFieldLayouts = fieldLayoutManager.getEditableFieldLayouts();
        }

        return editableFieldLayouts;
    }

    protected String redirectToView()
    {
        return getRedirect("ViewFieldLayouts.jspa");
    }

    public String getFieldLayoutName()
    {
        return fieldLayoutName;
    }

    public void setFieldLayoutName(String fieldLayoutName)
    {
        this.fieldLayoutName = fieldLayoutName;
    }

    public String getFieldLayoutDescription()
    {
        return fieldLayoutDescription;
    }

    public void setFieldLayoutDescription(String fieldLayoutDescription)
    {
        this.fieldLayoutDescription = fieldLayoutDescription;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public EditableFieldLayout getFieldLayout()
    {
        if (editableFieldLayout == null)
        {
            editableFieldLayout = getFieldLayoutManager().getEditableFieldLayout(getId());
        }

        return editableFieldLayout;
    }

    protected void validateId()
    {
        if (getId() == null)
        {
            addErrorMessage(getText("admin.errors.id.required"));
        }
    }

    protected void validateFieldLayout()
    {
        if (getFieldLayout() == null)
        {
            addErrorMessage(getText("admin.errors.fieldlayout.invalid.field.config.id","'" + getId() + "'"));
        }
    }

    protected FieldLayoutManager getFieldLayoutManager()
    {
        return fieldLayoutManager;
    }

    protected void validateName()
    {
        if (!TextUtils.stringSet(getFieldLayoutName()))
        {
            addError("fieldLayoutName", getText("admin.common.errors.validname"));
            //addError("fieldLayoutName", "Please specify a name.");
        }
    }
}
