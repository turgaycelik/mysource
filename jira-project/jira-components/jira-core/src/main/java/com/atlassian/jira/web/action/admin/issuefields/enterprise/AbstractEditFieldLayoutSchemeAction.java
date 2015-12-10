package com.atlassian.jira.web.action.admin.issuefields.enterprise;

import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.opensymphony.util.TextUtils;

import java.util.Collection;
import java.util.List;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public abstract class AbstractEditFieldLayoutSchemeAction extends JiraWebActionSupport
{
    private final FieldLayoutManager fieldLayoutManager;
    private Long id;
    private String fieldLayoutSchemeName;
    private String fieldLayoutSchemeDescription;
    private FieldLayoutScheme fieldLayoutScheme;
    private List<FieldLayoutScheme> fieldLayoutSchemes;

    public AbstractEditFieldLayoutSchemeAction(FieldLayoutManager fieldLayoutManager)
    {
        this.fieldLayoutManager = fieldLayoutManager;
    }

    protected String redirectToView()
    {
        return getRedirect("ViewFieldLayoutSchemes.jspa");

    }

    protected Collection<FieldLayoutScheme> getFieldLayoutSchemes()
    {
        if (fieldLayoutSchemes == null)
        {
            fieldLayoutSchemes = fieldLayoutManager.getFieldLayoutSchemes();
        }

        return fieldLayoutSchemes;
    }

    protected void validateName()
    {
        if (!TextUtils.stringSet(getFieldLayoutSchemeName()))
        {
            addError("fieldLayoutSchemeName", getText("admin.common.errors.validname"));
            //addError("fieldLayoutSchemeName", "Please specify a name.");
        }
    }

    protected void validateFieldLayoutScheme()
    {
        if (getFieldLayoutScheme() == null)
        {
            addErrorMessage(getText("admin.errors.fieldlayout.invalid.id","'" + getId() + "'"));
        }
    }

    public FieldLayoutScheme getFieldLayoutScheme()
    {
        if (fieldLayoutScheme == null)
        {
            fieldLayoutScheme = fieldLayoutManager.getMutableFieldLayoutScheme(getId());
        }

        return fieldLayoutScheme;
    }

    protected void validateId()
    {
        if (getId() == null)
        {
            addErrorMessage(getText("admin.errors.id.required"));
        }
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getFieldLayoutSchemeName()
    {
        return fieldLayoutSchemeName;
    }

    public void setFieldLayoutSchemeName(String fieldLayoutSchemeName)
    {
        this.fieldLayoutSchemeName = fieldLayoutSchemeName;
    }

    public String getFieldLayoutSchemeDescription()
    {
        return fieldLayoutSchemeDescription;
    }

    public void setFieldLayoutSchemeDescription(String fieldLayoutSchemeDescription)
    {
        this.fieldLayoutSchemeDescription = fieldLayoutSchemeDescription;
    }

    public Collection getSchemeProjects(FieldLayoutScheme fieldLayoutScheme)
    {
        return fieldLayoutManager.getProjects(fieldLayoutScheme);
    }

    protected FieldLayoutManager getFieldLayoutManager()
    {
        return fieldLayoutManager;
    }

    public String doDefault() throws Exception
    {
        validateId();

        if (!invalidInput())
        {
            validateFieldLayoutScheme();

            if (!invalidInput())
            {
                setFieldLayoutSchemeName(getInitialName());
                setFieldLayoutSchemeDescription(getFieldLayoutScheme().getDescription());
            }

        }

        return INPUT;
    }

    protected abstract String getInitialName();
}
