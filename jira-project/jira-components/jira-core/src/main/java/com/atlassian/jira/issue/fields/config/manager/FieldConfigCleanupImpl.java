package com.atlassian.jira.issue.fields.config.manager;

import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.option.OptionSetManager;
import com.atlassian.jira.util.ComponentLocator;
import com.atlassian.jira.util.dbc.Null;

public class FieldConfigCleanupImpl implements FieldConfigCleanup
{
    private final OptionSetManager optionSetManager;
    private final GenericConfigManager genericConfigManager;
    private final ComponentLocator componentLocator;

    public FieldConfigCleanupImpl(final OptionSetManager optionSetManager, final GenericConfigManager genericConfigManager, final ComponentLocator componentLocator)
    {
        Null.not("OptionSetManager", optionSetManager);
        Null.not("GenericConfigManager", genericConfigManager);
        Null.not("ComponentLocator", componentLocator);
        this.optionSetManager = optionSetManager;
        this.genericConfigManager = genericConfigManager;
        this.componentLocator = componentLocator;
    }

    public void removeAdditionalData(final FieldConfig fieldConfig)
    {
        Null.not("FieldConfig", fieldConfig);
        // not exactly sure what "OptionSets" are, but they're definitely not the Options e.g. for a Select CF
        optionSetManager.removeOptionSet(fieldConfig);
        // it seems the only records kept in the GenericConfiguration table are DefaultValues
        genericConfigManager.remove(CustomFieldType.DEFAULT_VALUE_TYPE, fieldConfig.getId().toString());
        // remove the Options e.g. for a Select CF
        getOptionsManager().removeCustomFieldConfigOptions(fieldConfig);
    }

    OptionsManager getOptionsManager()
    {
        return componentLocator.getComponentInstanceOfType(OptionsManager.class);
    }
}
