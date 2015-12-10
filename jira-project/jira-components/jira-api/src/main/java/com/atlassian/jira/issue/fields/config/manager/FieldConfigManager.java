package com.atlassian.jira.issue.fields.config.manager;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.fields.ConfigurableField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;

import java.util.List;

/**
 * Manager for <a href="http://www.atlassian.com/software/jira/docs/latest/issuefield_configuration.html">field configurations</a>.
 * 
 * @see FieldConfig
 */
@PublicApi
public interface FieldConfigManager
{
    /**
     * Retrieve field configuration by database ID.
     * 
     * @param configId
     *            the database id
     * @return the FieldConfig object
     */
    FieldConfig getFieldConfig(Long configId);

    @SuppressWarnings ({ "UnusedDeclaration" })
    FieldConfig createFieldConfig(FieldConfig newConfig, List<FieldConfigItemType> configurationItemTypes);

    @SuppressWarnings ({ "UnusedDeclaration" })
    FieldConfig updateFieldConfig(FieldConfig newConfig);

    FieldConfig createWithDefaultValues(ConfigurableField field);

    /**
     * Removes FieldConfig objects that are only associated to the specified FieldConfigScheme. In theory, a FieldConfig should only ever be
     * associated to one FieldConfigScheme, but here we take a defensive approach.
     * <p>
     * When FieldConfig objects are removed, their associated OptionSets and GenericConfigs are also removed.
     * <p>
     * Note that the mapping from FieldConfig to FieldConfigScheme in FieldConfigSchemeIssueType is not removed until FieldConfigScheme#remove() is
     * called. Thus, if someone calls FieldConfigSchemeManager#getFieldConfigScheme() <em>after</em> this method is called but <em>before</em> the
     * FieldConfigScheme is removed, a NullPointerException will occur because the FieldConfig referenced by the mapping record no longer exists.
     * 
     * @param fieldConfigSchemeId
     *            the scheme id of the fieldConfigScheme the fieldConfigs are exclusive to.
     */
    void removeConfigsForConfigScheme(Long fieldConfigSchemeId);
}