package com.atlassian.jira.issue.fields.config.manager;

import com.atlassian.jira.issue.fields.ConfigurableField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.fields.config.persistence.FieldConfigPersister;
import com.atlassian.jira.util.dbc.Null;

import java.util.Collection;
import java.util.List;

public class FieldConfigManagerImpl implements FieldConfigManager
{
    // ---------------------------------------------------------------------------------------------------- Dependencies

    private final FieldConfigPersister configPersister;
    private final FieldConfigCleanup fieldConfigCleanup;

    // ---------------------------------------------------------------------------------------------------- Constructors

    public FieldConfigManagerImpl(final FieldConfigPersister configPersister, final FieldConfigCleanup fieldConfigCleanup)
    {
        Null.not("FieldConfigPersister", configPersister);
        Null.not("FieldConfigCleanup", fieldConfigCleanup);
        this.configPersister = configPersister;
        this.fieldConfigCleanup = fieldConfigCleanup;
    }

    // ---------------------------------------------------------------------------------------- Config Interface Methods

    public FieldConfig getFieldConfig(final Long configId)
    {
        return (configId == null) ? null : configPersister.getFieldConfig(configId);
    }

    public FieldConfig createFieldConfig(final FieldConfig newConfig, final List<FieldConfigItemType> configurationItemTypes)
    {
        return configPersister.create(newConfig, configurationItemTypes);
    }

    public FieldConfig createWithDefaultValues(final ConfigurableField field)
    {
        return configPersister.createWithDefaultValues(field);
    }

    public FieldConfig updateFieldConfig(final FieldConfig newConfig)
    {
        return configPersister.update(newConfig);
    }

    public void removeConfigsForConfigScheme(final Long fieldConfigSchemeId)
    {
        Collection<FieldConfig> configs = configPersister.getConfigsExclusiveToConfigScheme(fieldConfigSchemeId);
        for (FieldConfig fieldConfig : configs)
        {
            removeFieldConfig(fieldConfig);
        }
    }

    void removeFieldConfig(final FieldConfig fieldConfig)
    {
        fieldConfigCleanup.removeAdditionalData(fieldConfig);
        configPersister.remove(fieldConfig);
    }
}