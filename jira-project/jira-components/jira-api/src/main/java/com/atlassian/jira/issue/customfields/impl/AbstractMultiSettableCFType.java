package com.atlassian.jira.issue.customfields.impl;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.customfields.MultipleSettableCustomFieldType;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;

import java.util.Set;

/**
 * @deprecated Implement the methods on {@link MultipleSettableCustomFieldType} yourself. Since v5.0.
 * Also, consider extending {@link AbstractSingleFieldType} or {@link AbstractMultiCFType} for other benefits
 */
@Deprecated
@PublicSpi
public abstract class AbstractMultiSettableCFType<T, S> extends AbstractCustomFieldType<T, S> implements MultipleSettableCustomFieldType<T, S>
{
    protected final OptionsManager optionsManager;
    protected final CustomFieldValuePersister customFieldValuePersister;
    protected final GenericConfigManager genericConfigManager;

    protected AbstractMultiSettableCFType(final OptionsManager optionsManager, final CustomFieldValuePersister customFieldValuePersister, final GenericConfigManager genericConfigManager)
    {
        this.optionsManager = optionsManager;
        this.customFieldValuePersister = customFieldValuePersister;
        this.genericConfigManager = genericConfigManager;
    }

    public Set<Long> remove(final CustomField field)
    {
        optionsManager.removeCustomFieldOptions(field);
        return customFieldValuePersister.removeAllValues(field.getId());
    }

    public Options getOptions(final FieldConfig fieldConfig, final JiraContextNode jiraContextNode)
    {
        return optionsManager.getOptions(fieldConfig);
    }
}
