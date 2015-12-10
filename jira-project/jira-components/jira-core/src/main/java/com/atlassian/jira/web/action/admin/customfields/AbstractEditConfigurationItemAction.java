package com.atlassian.jira.web.action.admin.customfields;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItem;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemService;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigManager;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import webwork.action.Action;

public abstract class AbstractEditConfigurationItemAction extends JiraWebActionSupport
{
    private final ManagedConfigurationItemService managedConfigurationItemService;

    private Long fieldConfigId;
    private FieldConfig fieldConfig;

    private ManagedConfigurationItem managedConfigurationItem;

    protected AbstractEditConfigurationItemAction(ManagedConfigurationItemService managedConfigurationItemService)
    {
        this.managedConfigurationItemService = managedConfigurationItemService;
    }

    @Override
    public String doDefault() throws Exception
    {
        if (validateFieldLocked())
        {
            return Action.ERROR;
        }

        return super.doDefault();
    }

    public void setFieldConfigId(Long fieldConfigId)
    {
        this.fieldConfigId = fieldConfigId;
    }

    public Long getFieldConfigId()
    {
        return fieldConfigId;
    }

    public FieldConfig getFieldConfig()
    {
        if (fieldConfig == null && fieldConfigId != null)
        {
            final FieldConfigManager fieldConfigManager = ComponentAccessor.getComponent(FieldConfigManager.class);
            fieldConfig = fieldConfigManager.getFieldConfig(fieldConfigId);
        }

        return fieldConfig;
    }

    public CustomField getCustomField()
    {
        return getFieldConfig().getCustomField();
    }

    public boolean isFieldLocked()
    {
        return !managedConfigurationItemService.doesUserHavePermission(getLoggedInUser(), getManagedConfigurationItem());
    }

    public boolean isFieldManaged()
    {
        return getManagedConfigurationItem().isManaged();
    }

    public String getManagedFieldDescriptionKey()
    {
        return getManagedConfigurationItem().getDescriptionI18nKey();
    }

    protected boolean validateFieldLocked()
    {
        if (isFieldLocked())
        {
            addErrorMessage(getText("admin.managed.configuration.items.customfield.error.cannot.alter.configuration.locked", getCustomField()), Reason.FORBIDDEN);
            return true;
        }
        return false;
    }

    protected ManagedConfigurationItem getManagedConfigurationItem()
    {
        if (managedConfigurationItem == null)
        {
            managedConfigurationItem = managedConfigurationItemService.getManagedCustomField(getCustomField());
        }
        return managedConfigurationItem;
    }

}
