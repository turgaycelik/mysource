package com.atlassian.jira.action.issue.customfields;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.plugin.customfield.CustomFieldTypeModuleDescriptor;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.web.bean.BulkEditBean;

import com.google.common.collect.Lists;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Mocks a CustomFieldType
 *
 * @since v3.13
 */
public class MockCustomFieldType implements CustomFieldType
{
    private String key;
    private String name;
    private String description;
    private CustomFieldTypeModuleDescriptor moduleDescriptor;
    private List<FieldConfigItemType> fieldConfigItemTypes = Lists.newArrayList();

    public MockCustomFieldType()
    {
    }

    public MockCustomFieldType(String key, String name)
    {
        this.key = key;
        this.name = name;
    }

    public MockCustomFieldType(String key, String name, String description)
    {
        this(key, name);
        this.description = description;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(final String key)
    {
        this.key = key;
    }

    public String getName()
    {
        return name;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    public void init(CustomFieldTypeModuleDescriptor customFieldTypeModuleDescriptor)
    {
        this.moduleDescriptor = customFieldTypeModuleDescriptor;
    }

    public String getDescription()
    {
        return description;
    }

    public CustomFieldTypeModuleDescriptor getDescriptor()
    {
        return moduleDescriptor;
    }

    public String getStringFromSingularObject(Object singularObject)
    {
        return null;
    }

    public Object getSingularObjectFromString(String string) throws FieldValidationException
    {
        return null;
    }

    public Set<Long> remove(CustomField field)
    {
        return null;
    }

    public void validateFromParams(CustomFieldParams relevantParams, ErrorCollection errorCollectionToAddTo, FieldConfig config)
    {
    }

    public void createValue(CustomField field, Issue issue, @Nonnull Object value)
    {
    }

    public void updateValue(CustomField field, Issue issue, Object value)
    {
    }

    public Object getValueFromCustomFieldParams(CustomFieldParams parameters) throws FieldValidationException
    {
        return null;
    }

    public Object getStringValueFromCustomFieldParams(CustomFieldParams parameters)
    {
        return null;
    }

    public Object getValueFromIssue(CustomField field, Issue issue)
    {
        return null;
    }

    public Object getDefaultValue(FieldConfig fieldConfig)
    {
        return null;
    }

    public void setDefaultValue(FieldConfig fieldConfig, Object value)
    {
    }

    public String getChangelogValue(CustomField field, Object value)
    {
        return null;
    }

    public String getChangelogString(CustomField field, Object value)
    {
        return null;
    }

    @Nonnull
    public Map<String, Object> getVelocityParameters(Issue issue, CustomField field, FieldLayoutItem fieldLayoutItem)
    {
        return null;
    }

    @Nonnull
    public List<FieldConfigItemType> getConfigurationItemTypes()
    {
        return fieldConfigItemTypes;
    }

    public MockCustomFieldType setConfigurationItemTypes(final List<FieldConfigItemType> itemTypes)
    {
        this.fieldConfigItemTypes = itemTypes;
        return this;
    }

    public List<FieldIndexer> getRelatedIndexers(CustomField customField)
    {
        return null;
    }

    public boolean isRenderable()
    {
        return false;
    }

    public boolean valuesEqual(Object v1, Object v2)
    {
        return false;
    }

    public String availableForBulkEdit(BulkEditBean bulkEditBean)
    {
        return null;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
