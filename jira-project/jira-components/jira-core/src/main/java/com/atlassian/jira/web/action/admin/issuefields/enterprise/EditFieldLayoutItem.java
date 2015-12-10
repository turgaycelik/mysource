/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuefields.enterprise;

import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemService;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.web.action.admin.issuefields.AbstractEditFieldLayoutItem;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericValue;

@WebSudoRequired
public class EditFieldLayoutItem extends AbstractEditFieldLayoutItem
{
    private Long id;
    private EditableFieldLayout editableFieldLayout;
    private FieldLayoutManager fieldLayoutManager;

    public EditFieldLayoutItem(final FieldLayoutManager fieldLayoutManager, final FieldManager fieldManager, final ManagedConfigurationItemService managedConfigurationItemService)
    {
        super(managedConfigurationItemService, fieldManager);
        this.fieldLayoutManager = fieldLayoutManager;
    }

    public String doDefault() throws Exception
    {
        doValidation();
        if (!invalidInput())
        {
            return super.doDefault();
        }
        return ERROR;
    }

    protected void doValidation()
    {
        // Check the scheme with specified id exists
        if (getId() != null)
        {
            if (getFieldLayout() == null)
            {
                addErrorMessage(getText("admin.errors.fieldlayout.no.field.config.id"));
            }
            else
            {
                super.doValidation();
            }
        }
        else
        {
            addErrorMessage(getText("admin.errors.fieldlayout.no.field.config.id"));
        }
    }

    protected String getRedirectURI()
    {
        return "ConfigureFieldLayout.jspa?id=" + getId();
    }

    protected EditableFieldLayout getFieldLayout()
    {
        if (editableFieldLayout == null)
        {
            editableFieldLayout = getFieldLayoutManager().getEditableFieldLayout(getId());
        }

        return editableFieldLayout;
    }

    protected void store()
    {
        try
        {
            getFieldLayoutManager().storeEditableFieldLayout(getFieldLayout());
        }
        catch (DataAccessException e)
        {
            log.error(ACCESS_EXCEPTION, e);
            addErrorMessage(ACCESS_EXCEPTION);
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

    public FieldLayoutManager getFieldLayoutManager()
    {
        return fieldLayoutManager;
    }

    public GenericValue getFieldLayoutScheme(Long schemeId)
    {
        return null;
    }
}
