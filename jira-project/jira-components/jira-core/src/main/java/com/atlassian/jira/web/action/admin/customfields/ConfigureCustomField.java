package com.atlassian.jira.web.action.admin.customfields;

import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItem;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemService;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import java.util.List;

@WebSudoRequired
public class ConfigureCustomField extends JiraWebActionSupport
{
    private Long customFieldId;
    private ManagedConfigurationItem managedConfigurationItem;

    private final FieldConfigSchemeManager schemeManager;
    private final CustomFieldManager customFieldManager;
    private final ManagedConfigurationItemService managedConfigurationItemService;

    public ConfigureCustomField(FieldConfigSchemeManager schemeManager, CustomFieldManager customFieldManager,
            ManagedConfigurationItemService managedConfigurationItemService)
    {
        this.schemeManager = schemeManager;
        this.customFieldManager = customFieldManager;
        this.managedConfigurationItemService = managedConfigurationItemService;
    }

    protected String doExecute() throws Exception
    {
        return SUCCESS;
    }

    public List<FieldConfigScheme> getConfigs()
    {
        if (getCustomFieldId() != null)
        {
            CustomField customField = getCustomField();
            return schemeManager.getConfigSchemesForField(customField);
        }
        else
        {
            return null;
        }
    }

    public CustomField getCustomField()
    {
        return customFieldManager.getCustomFieldObject(getCustomFieldId());
    }

    public Long getCustomFieldId()
    {
        return customFieldId;
    }

    public void setCustomFieldId(Long customFieldId)
    {
        this.customFieldId = customFieldId;
    }

    public boolean isFieldManaged()
    {
        return getManagedConfigurationItem().isManaged();
    }

    public boolean isFieldLocked()
    {
        return !managedConfigurationItemService.doesUserHavePermission(getLoggedInUser(), getManagedConfigurationItem());
    }

    public String getManagedFieldDescriptionKey()
    {
        return getManagedConfigurationItem().getDescriptionI18nKey();
    }

    public ManagedConfigurationItem getManagedConfigurationItem()
    {
        if (managedConfigurationItem == null)
        {
            managedConfigurationItem = managedConfigurationItemService.getManagedCustomField(getCustomField());
        }
        return managedConfigurationItem;
    }
}
