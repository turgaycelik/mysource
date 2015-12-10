package com.atlassian.jira.issue.fields.layout.field;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.project.Project;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple mock implementation of {@link FieldLayout}.
 *
 * @since v4.1
 */
public class MockFieldLayout implements FieldLayout
{
    private Map<String, FieldLayoutItem> fields = new HashMap<String, FieldLayoutItem>();
    private String name;
    private String description;
    private Long id;
    private boolean defaultLayout;

    public Long getId()
    {
        return id;
    }

    public MockFieldLayout setId(final Long id)
    {
        this.id = id;
        return this;
    }

    public String getName()
    {
        return name;
    }

    public MockFieldLayout setName(String name)
    {
        this.name = name;
        return this;
    }

    public String getDescription()
    {
        return description;
    }

    public MockFieldLayout setDescription(String description)
    {
        this.description = description;
        return this;
    }

    public List<FieldLayoutItem> getFieldLayoutItems()
    {
        return new ArrayList<FieldLayoutItem>(fields.values());
    }

    public GenericValue getGenericValue()
    {
        throw new UnsupportedOperationException();
    }

    public FieldLayoutItem getFieldLayoutItem(final OrderableField orderableField)
    {
        return getFieldLayoutItem(orderableField.getId());
    }

    public FieldLayoutItem getFieldLayoutItem(final String fieldId)
    {
        return fields.get(fieldId);
    }

    public MockFieldLayoutItem addFieldLayoutItem(final OrderableField orderableField)
    {
        MockFieldLayoutItem fieldLayoutItem = new MockFieldLayoutItem();
        fieldLayoutItem.setOrderableField(orderableField);

        fields.put(orderableField.getId(), fieldLayoutItem);

        return fieldLayoutItem;
    }

    @Override
    public List<FieldLayoutItem> getVisibleLayoutItems(final User remoteUser, final Project project, final List<String> issueTypes)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<FieldLayoutItem> getVisibleLayoutItems(final Project project, final List<String> issueTypes)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<FieldLayoutItem> getVisibleCustomFieldLayoutItems(final Project project, final List<String> issueTypes)
    {
        throw new UnsupportedOperationException();
    }

    public List<Field> getHiddenFields(final Project project, final List<String> issueTypeIds)
    {
        throw new UnsupportedOperationException();
    }

    public List<Field> getHiddenFields(final User remoteUser, final GenericValue project, final List<String> issueTypeIds)
    {
        throw new UnsupportedOperationException();
    }

    public List<Field> getHiddenFields(final User remoteUser, final Project project, final List<String> issueTypeIds)
    {
        throw new UnsupportedOperationException();
    }

    public List<FieldLayoutItem> getRequiredFieldLayoutItems(final Project project, final List<String> issueTypes)
    {
        throw new UnsupportedOperationException();
    }

    public boolean isFieldHidden(final String fieldId)
    {
        return false;
    }

    public String getRendererTypeForField(final String fieldId)
    {
        FieldLayoutItem fieldLayoutItem = getFieldLayoutItem(fieldId);
        return fieldLayoutItem == null ? null : fieldLayoutItem.getRendererType();
    }

    public boolean isDefault()
    {
        return defaultLayout;
    }

    public MockFieldLayout setDefault(boolean defaultLayout)
    {
        this.defaultLayout = defaultLayout;
        return this;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
