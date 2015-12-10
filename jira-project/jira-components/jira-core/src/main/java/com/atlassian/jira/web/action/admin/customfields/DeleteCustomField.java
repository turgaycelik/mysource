/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.customfields;

import com.atlassian.jira.bc.customfield.CustomFieldService;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItem;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemService;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import webwork.action.Action;

@WebSudoRequired
public class DeleteCustomField extends JiraWebActionSupport
{
    private CustomField customField;
    private Long id;

    private final CustomFieldManager customFieldManager;
    private final CustomFieldService customFieldService;
    private final ManagedConfigurationItemService managedConfigurationItemService;

    public DeleteCustomField(CustomFieldService customFieldService, CustomFieldManager customFieldManager,
            ManagedConfigurationItemService managedConfigurationItemService)
    {
        this.customFieldService = customFieldService;
        this.customFieldManager = customFieldManager;
        this.managedConfigurationItemService = managedConfigurationItemService;
    }

    public String doDefault() throws Exception
    {
        if (!validateFieldLocked())
        {
            return Action.ERROR;
        }

        return super.doDefault();
    }

    public void doValidation()
    {
        if (!validateFieldLocked())
        {
            return;
        }

        customFieldService.validateDelete(getJiraServiceContext(), getId());
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        customFieldManager.removeCustomField(getCustomField());

        return getRedirect("ViewCustomFields.jspa");
    }

    public boolean isFieldManaged()
    {
        return getManagedConfigurationEntity().isManaged();
    }

    public boolean isFieldLocked()
    {
        return !managedConfigurationItemService.doesUserHavePermission(getLoggedInUser(), getManagedConfigurationEntity());
    }

    public String getManagedFieldDescriptionKey()
    {
        return getManagedConfigurationEntity().getDescriptionI18nKey();
    }

    private boolean validateFieldLocked()
    {
        if (isFieldLocked())
        {
            addErrorMessage(getText("admin.managed.configuration.items.customfield.error.cannot.delete.locked", getCustomField().getName()));
            return false;
        }
        return true;
    }

    private ManagedConfigurationItem getManagedConfigurationEntity()
    {
        return managedConfigurationItemService.getManagedCustomField(getCustomField());
    }

    public CustomField getCustomField()
    {
        if (customField == null)
        {
            customField = customFieldManager.getCustomFieldObject(getId());
        }
        return customField;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }
}
