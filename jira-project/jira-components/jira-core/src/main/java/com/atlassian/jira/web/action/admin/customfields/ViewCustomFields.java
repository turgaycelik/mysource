/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.customfields;

import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItem;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemService;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.MultipleSettableCustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebSudoRequired
public class ViewCustomFields extends JiraWebActionSupport
{
    private final CustomFieldManager customFieldManager;
    private final FieldScreenManager fieldScreenManager;
    private final ManagedConfigurationItemService managedConfigurationItemService;
    private final FeatureManager featureManager;
    private final TranslationManager translationManager;

    private Map fieldScreenTabMap;

    public ViewCustomFields(CustomFieldManager customFieldManager, FieldScreenManager fieldScreenManager,
            ManagedConfigurationItemService managedConfigurationItemService, FeatureManager featureManager, final TranslationManager translationManager)
    {
        this.customFieldManager = customFieldManager;
        this.fieldScreenManager = fieldScreenManager;
        this.managedConfigurationItemService = managedConfigurationItemService;
        this.featureManager = featureManager;
        this.translationManager = translationManager;
        fieldScreenTabMap = new HashMap();
    }

    public String doDefault() throws Exception
    {
        return super.doDefault();
    }

    public String doReset() throws Exception
    {
        customFieldManager.refresh();
        return super.doDefault();
    }


    public List<CustomField> getCustomFields() throws Exception
    {
        return customFieldManager.getCustomFieldObjects();
    }

    public boolean isCustomFieldTypesExist()
    {
        Collection fieldTypes = customFieldManager.getCustomFieldTypes();
        return fieldTypes != null && !fieldTypes.isEmpty();
    }

    public boolean isHasConfigurableOptions(CustomField customField)
    {
        return customField.getCustomFieldType() instanceof MultipleSettableCustomFieldType;  
    }

    
    public Collection getFieldScreenTabs(OrderableField orderableField)
    {
        String fieldId = orderableField.getId();
        if (!fieldScreenTabMap.containsKey(fieldId))
        {
            fieldScreenTabMap.put(fieldId, fieldScreenManager.getFieldScreenTabs(orderableField.getId()));
        }

        return (Collection) fieldScreenTabMap.get(fieldId);
    }

    public boolean isFieldManaged(final CustomField field)
    {
        ManagedConfigurationItem item = managedConfigurationItemService.getManagedCustomField(field);
        return item.isManaged();
    }

    public boolean isMultiLingual()
    {
        Map installedLocales = translationManager.getInstalledLocales();
        return installedLocales.size() > 1;
    }

    public boolean isFieldLocked(final CustomField field)
    {
        ManagedConfigurationItem item = managedConfigurationItemService.getManagedCustomField(field);
        if (!item.isManaged())
        {
            return false;
        }
        return !managedConfigurationItemService.doesUserHavePermission(getLoggedInUser(), item);
    }

    public String getManagedFieldDescriptionKey(final CustomField field)
    {
        ManagedConfigurationItem item = managedConfigurationItemService.getManagedCustomField(field);
        String descriptionI18nKey = item.getDescriptionI18nKey();
        if (StringUtils.isBlank(descriptionI18nKey))
        {
            return "";
        }
        return descriptionI18nKey;
    }

    public boolean isOnDemand()
    {
        return featureManager.isOnDemand();
    }
}
