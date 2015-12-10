package com.atlassian.jira.issue.fields.config;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.persistence.FieldConfigPersister;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FieldConfigImpl implements FieldConfig, Comparable
{
    private final Long id;
    private final String name;
    private final String description;
    private final List<FieldConfigItem> configItems;
    private final String fieldId;

    public FieldConfigImpl(final Long id, final String name, final String description, List<FieldConfigItemType> configItemTypes, final String fieldId)
    {
        this.id = id;
        this.name = StringUtils.abbreviate(name, FieldConfigPersister.ENTITY_LONG_TEXT_LENGTH);
        this.description = description;
        this.fieldId = fieldId;

        if (configItemTypes == null)
        {
            configItemTypes = Collections.emptyList();
        }
        final List<FieldConfigItem> configItems = new ArrayList<FieldConfigItem>(configItemTypes.size());
        for (final FieldConfigItemType type : configItemTypes)
        {
            configItems.add(new FieldConfigItemImpl(type, this));
        }

        this.configItems = Collections.unmodifiableList(configItems);
    }

    public String getDescription()
    {
        return description;
    }

    public Long getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public CustomField getCustomField()
    {
        // Load the custom field statically.... hmmm...
        final CustomField customField = ComponentAccessor.getCustomFieldManager().getCustomFieldObject(fieldId);
        if (customField == null)
        {
            throw new DataAccessException("No custom field for " + fieldId + ". This should not happen. Data is likely to be corrupt.");
        }
        return customField;
    }

    public String getFieldId()
    {
        return fieldId;
    }

    public List<FieldConfigItem> getConfigItems()
    {
        return configItems;
    }

    public boolean equals(final Object o)
    {
        if (!(o instanceof FieldConfigImpl))
        {
            return false;
        }
        final FieldConfigImpl rhs = (FieldConfigImpl) o;
        return new EqualsBuilder().append(getName(), rhs.getName()).append(getId(), rhs.getId()).isEquals();
    }

    public int compareTo(final Object obj)
    {
        final FieldConfigImpl o = (FieldConfigImpl) obj;
        return new CompareToBuilder().append(getName(), o.getName()).append(getId(), o.getId()).toComparison();
    }

    public int hashCode()
    {
        return new HashCodeBuilder(15, 175).append(getName()).append(getId()).toHashCode();
    }
}
