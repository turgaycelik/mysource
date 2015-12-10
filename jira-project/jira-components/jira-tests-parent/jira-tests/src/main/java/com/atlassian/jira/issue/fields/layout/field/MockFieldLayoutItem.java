package com.atlassian.jira.issue.fields.layout.field;

import com.atlassian.jira.issue.fields.OrderableField;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Simple implementation of {@link com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem}.
 *
 * @since v4.1
 */
public class MockFieldLayoutItem implements FieldLayoutItem
{
    private OrderableField orderableField;
    private String description;
    private boolean hidden;
    private boolean required;
    private String rendererType;
    private FieldLayout fieldLayout;

    public OrderableField getOrderableField()
    {
        return orderableField;
    }

    public MockFieldLayoutItem setOrderableField(OrderableField orderableField)
    {
        this.orderableField = orderableField;
        return this;
    }

    public String getFieldDescription()
    {
        return description;
    }

    @Override
    public String getRawFieldDescription()
    {
        return description;
    }

    public MockFieldLayoutItem setDescription(final String description)
    {
        this.description = description;
        return this;
    }

    public boolean isHidden()
    {
        return hidden;
    }

    public MockFieldLayoutItem setHidden(final boolean hidden)
    {
        this.hidden = hidden;
        return this;
    }

    public boolean isRequired()
    {
        return required;
    }

    public MockFieldLayoutItem setRequired(final boolean required)
    {
        this.required = required;
        return this;
    }

    public String getRendererType()
    {
        return rendererType;
    }

    public MockFieldLayoutItem setRendererType(final String type)
    {
        this.rendererType = type;
        return this;
    }

    public FieldLayout getFieldLayout()
    {
        return fieldLayout;
    }

    public MockFieldLayoutItem setFieldLayout(final FieldLayout fieldLayout)
    {
        this.fieldLayout = fieldLayout;
        return this;
    }

    public int compareTo(final FieldLayoutItem fieldLayoutItem)
    {
        if (fieldLayoutItem == null)
        {
            return 1;
        }

        if (fieldLayoutItem.getOrderableField() == null)
        {
            if (getOrderableField() == null)
            {
                return 0;
            }
            else
            {
                return 1;
            }
        }
        else
        {
            if (getOrderableField() == null)
            {
                return -1;
            }
            else
            {
                return getOrderableField().compareTo(fieldLayoutItem.getOrderableField());
            }
        }

    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
