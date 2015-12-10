package com.atlassian.jira.issue.fields.config.persistence;

import com.atlassian.jira.issue.fields.ConfigurableField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.option.OptionSet;

import java.util.Collection;
import java.util.List;

public interface FieldConfigPersister
{
    int ENTITY_LONG_TEXT_LENGTH = 254;

    // ----------------------------------------------------------------------------------------------- Interface Methods
    FieldConfig create(FieldConfig config, List<FieldConfigItemType> configurationItemTypes);

    List getConfigForField(ConfigurableField field);

    FieldConfig getFieldConfig(Long configId);

    FieldConfig update(FieldConfig config);

    /**
     * Remove a {@link FieldConfig} and all associated additional data.
     * 
     * @param fieldConfig the fieldConfig to remove.
     */
    void remove(FieldConfig fieldConfig);

    /**
     * Gets the {@link FieldConfig} objects that are only associated to the specified {@link FieldConfigScheme}. In theory, a {@link FieldConfig}
     * should only ever be associated to one {@link FieldConfigScheme}, but the database schema allows sharing of configs between schemes.
     * <p>
     * When FieldConfig objects are removed, their associated {@link OptionSet OptionsSets} and GenericConfigurations are also removed. Note that the
     * mapping from {@link FieldConfig} to {@link FieldConfigScheme} in BackupOverviewBuilderImpl.FieldConfigSchemeIssueType is not removed until a
     * {@link FieldConfigScheme} is removed.
     * 
     * @param fieldConfigSchemeId
     *            the id of the field config scheme
     * @return the {@link Collection} of FieldConfig objects
     */
    Collection<FieldConfig> getConfigsExclusiveToConfigScheme(Long fieldConfigSchemeId);

    FieldConfig createWithDefaultValues(ConfigurableField field);

    FieldConfig getFieldConfig(Long configId, ConfigurableField field);
}
