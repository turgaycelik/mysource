/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuefields;

import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItem;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemService;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import webwork.action.Action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractEditFieldLayoutItem extends JiraWebActionSupport
{
    private Integer position;
    private String description;
    private String fieldName;
    protected static final String ACCESS_EXCEPTION = "Error while accessing field layouts.";

    final private ManagedConfigurationItemService managedConfigurationItemService;
    private final FieldManager fieldManager;

    private ManagedConfigurationItem managedCustomField;
    private FieldLayoutItem fieldLayoutItem;

    protected AbstractEditFieldLayoutItem(ManagedConfigurationItemService managedConfigurationItemService, final FieldManager fieldManager)
    {
        this.managedConfigurationItemService = managedConfigurationItemService;
        this.fieldManager = fieldManager;
    }

    public String doDefault() throws Exception
    {
        // Retrieve the field's current description
        FieldLayoutItem fieldLayoutItem = getFieldLayoutItem();
        if (fieldLayoutItem != null)
        {
            if (isFieldLocked())
            {
                addErrorMessage(getText("admin.managed.configuration.items.customfield.error.cannot.alter.configuration.locked", fieldLayoutItem.getOrderableField().getName()), Reason.FORBIDDEN);
                return Action.ERROR;
            }

            setDescription(fieldLayoutItem.getRawFieldDescription());
            if (fieldManager.isCustomField(fieldLayoutItem.getOrderableField()))
            {
                CustomField customField = fieldManager.getCustomField(fieldLayoutItem.getOrderableField().getId());
                setFieldName(customField.getName());
            }
            else
            {
                setFieldName(fieldLayoutItem.getOrderableField().getName());
            }

            return super.doDefault();
        }
        return ERROR;
    }

    @Override
    protected void doValidation()
    {
        // first, validate Position
        final FieldLayoutItem fieldLayoutItem1 = getFieldLayoutItem();
        if (hasAnyErrors())
        {
            return;
        }

        if (isFieldLocked())
        {
            addErrorMessage(getText("admin.managed.configuration.items.customfield.error.cannot.alter.configuration.locked", fieldLayoutItem1.getOrderableField().getName()), Reason.FORBIDDEN);
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        FieldLayoutItem fieldLayoutItem = getFieldLayoutItem();
        if (fieldLayoutItem != null)
        {
            // Update the field layout item's description
            getFieldLayout().setDescription(fieldLayoutItem, getDescription());

            // Store the field layout item's description
            store();
        }

        return getRedirect(getRedirectURI());
    }

    protected abstract String getRedirectURI();

    private FieldLayoutItem getFieldLayoutItem()
    {
        if (fieldLayoutItem != null)
        {
            return fieldLayoutItem;
        }

        if (getPosition() != null)
        {
            List<FieldLayoutItem> fieldLayoutItems = new ArrayList<FieldLayoutItem>(getFieldLayout().getFieldLayoutItems());
            // Need to sort here, as the order depends on the name of the fields which are i18n'ed.
            Collections.sort(fieldLayoutItems);
            if (getPosition() >= 0 && (getPosition() < fieldLayoutItems.size()))
            {
                fieldLayoutItem = fieldLayoutItems.get(getPosition());
            }
            else
            {
                log.error("The field layout item at position '" + getPosition() + "' does not exist.");
                addErrorMessage(getText("admin.errors.fieldlayout.field.does.not.exist", "'" + getPosition() + "'"));
            }
        }
        return fieldLayoutItem;
    }

    private void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    /** Zero-relative position of the field */
    public Integer getPosition()
    {
        return position;
    }

    public void setPosition(Integer position)
    {
        this.position = position;
    }

    protected abstract EditableFieldLayout getFieldLayout();

    protected abstract void store();

    public String getFieldName()
    {
        return fieldName;
    }

    public boolean isFieldLocked()
    {
        final ManagedConfigurationItem item = getManagedCustomField();
        if (item == null || !item.isManaged())
        {
            return false;
        }
        
        return !managedConfigurationItemService.doesUserHavePermission(getLoggedInUser(), item);
    }

    private ManagedConfigurationItem getManagedCustomField()
    {
        if (managedCustomField != null)
        {
            return managedCustomField;
        }

        final FieldLayoutItem fieldLayoutItem = getFieldLayoutItem();
        if (fieldLayoutItem == null)
        {
            return null;
        }
        
        Field field = fieldLayoutItem.getOrderableField();
        if (field == null)
        {
            return null;
        }

        if (field instanceof CustomField)
        {
            final CustomField customField = (CustomField) field;
            managedCustomField = managedConfigurationItemService.getManagedCustomField(customField);
        }

        return managedCustomField;
    }
}
