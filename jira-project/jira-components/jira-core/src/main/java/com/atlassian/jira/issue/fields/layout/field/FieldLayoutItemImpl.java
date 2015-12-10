/*
 * Copyright (c) 2002-2006
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields.layout.field;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.renderer.HackyFieldRendererRegistry;
import com.atlassian.jira.issue.fields.renderer.text.DefaultTextRenderer;

import com.google.common.base.Objects;

import org.apache.commons.lang.builder.ToStringBuilder;

public class FieldLayoutItemImpl implements FieldLayoutItem
{
    private final OrderableField orderableField;
    private final String fieldDescription;
    private final boolean hidden;
    private final boolean required;
    private final String rendererType;
    private final FieldLayout fieldLayout;
    private final FieldManager fieldManager;
    private final FieldDescriptionHelper fieldDescriptionHelper;

    FieldLayoutItemImpl(OrderableField orderableField, String description, boolean hidden, boolean required, String rendererType, FieldLayout fieldLayout, FieldManager fieldManager, FieldDescriptionHelper fieldDescriptionHelper)
    {
        if (hidden && !fieldManager.isHideableField(orderableField))
        {
            throw new IllegalArgumentException("Trying to create a Hidden Field Layout Item for a Field that is not hideable. '" + orderableField + "'.");
        }

        this.orderableField = orderableField;
        this.fieldDescription = description;
        this.hidden = hidden;
        this.required = required;
        this.rendererType = rendererType;
        this.fieldLayout = fieldLayout;
        this.fieldManager = fieldManager;
        this.fieldDescriptionHelper = fieldDescriptionHelper;
    }

    public OrderableField getOrderableField()
    {
        return orderableField;
    }

    /**
     *
     * @return  a rendered view of the description for OnDemand only
     */
    public String getFieldDescription()
    {
        String description = null;
        // JRA-10427 if we have a field description, prefer it over the default custom field description
        if (fieldDescription != null)
        {
            description = fieldDescription;
            if (fieldDescriptionHelper != null)
            {
                description = fieldDescriptionHelper.getDescription(fieldDescription);
            }
        }
        else
        {
            if (fieldManager.isCustomField(orderableField))
            {
                CustomField customField = fieldManager.getCustomField(orderableField.getId());
                description = customField.getDescriptionProperty().getViewHtml();
            }
        }
        return description;
    }

    @Override
    public String getRawFieldDescription()
    {
        String description = fieldDescription;
        if (description == null)
        {
            if (fieldManager.isCustomField(orderableField))
            {
                CustomField customField = fieldManager.getCustomField(orderableField.getId());
                description = customField.getDescription();
            }
        }
        return description;
    }

    public boolean isHidden()
    {
        return hidden;
    }

    public boolean isRequired()
    {
        return required;
    }

    public String getRendererType()
    {
        return rendererType;
    }

    public FieldLayout getFieldLayout()
    {
        return fieldLayout;
    }

    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof FieldLayoutItem))
        {
            return false;
        }

        final FieldLayoutItem fieldLayoutItem = (FieldLayoutItem) o;
        return hidden == fieldLayoutItem.isHidden()
                && required == fieldLayoutItem.isRequired()
                && Objects.equal(orderableField, fieldLayoutItem.getOrderableField());
    }

    public int hashCode()
    {
        int result;
        result = (orderableField != null ? orderableField.hashCode() : 0);
        result = 29 * result + (hidden ? 1 : 0);
        result = 29 * result + (required ? 1 : 0);
        return result;
    }

    public int compareTo(FieldLayoutItem fieldLayoutItem)
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

    /**
     * Useful for instantiating {@link FieldLayoutItemImpl} objects from scratch or copying them.
     * 
     * @since v4.2
     */
    public static class Builder
    {
        private OrderableField orderableField = null;
        private String fieldDescription = null;
        private boolean hidden = false;
        private boolean required = false;
        private String rendererType = null;
        private FieldLayout fieldLayout = null;
        private FieldManager fieldManager = null;
        private FieldDescriptionHelper fieldDescriptionHelper = null;

        public Builder()
        {
        }

        /**
         * @param orig the FieldLayoutItem instance to copy
         */
        public Builder(FieldLayoutItem orig)
        {
            this.orderableField = orig.getOrderableField();
            this.fieldDescription = orig.getRawFieldDescription();
            this.hidden = orig.isHidden();
            this.required = orig.isRequired();
            this.rendererType = orig.getRendererType();
            this.fieldLayout = orig.getFieldLayout();
        }

        /**
         * @return a new {@link FieldLayoutItemImpl} instance. If a {@link FieldManager} was not set, the result of
         * {@link ComponentAccessor#getFieldManager()} will be used.
         */
        public FieldLayoutItemImpl build()
        {
            // Initialize default values for items that were not provided to the builder
            if (fieldManager == null)
            {
                fieldManager = ComponentAccessor.getFieldManager();
            }
            if (fieldDescriptionHelper == null)
            {
                fieldDescriptionHelper = ComponentAccessor.getComponent(FieldDescriptionHelper.class);
            }
            if (rendererType == null)
            {
                rendererType = getDefaultRendererType();
            }
            else if (rendererType.equals(DefaultTextRenderer.RENDERER_TYPE))
            {
                // A little interning here saves 60K
                rendererType = DefaultTextRenderer.RENDERER_TYPE;
            }
            return new FieldLayoutItemImpl(orderableField, fieldDescription, hidden, required, rendererType, fieldLayout, fieldManager, fieldDescriptionHelper);
        }

        private String getDefaultRendererType()
        {
            if (orderableField != null)
            {
                final HackyFieldRendererRegistry hackyFieldRendererRegistry = ComponentAccessor.getComponentOfType(HackyFieldRendererRegistry.class);
                if (hackyFieldRendererRegistry.shouldOverrideDefaultRenderers(orderableField))
                {
                    return hackyFieldRendererRegistry.getDefaultRendererType(orderableField).getKey();
                }
            }
            return DefaultTextRenderer.RENDERER_TYPE;
        }

        public Builder setFieldDescription(final String fieldDescription)
        {
            this.fieldDescription = fieldDescription;
            return this;
        }

        public Builder setFieldLayout(final FieldLayout fieldLayout)
        {
            this.fieldLayout = fieldLayout;
            return this;
        }

        public Builder setHidden(final boolean hidden)
        {
            this.hidden = hidden;
            return this;
        }

        public Builder setOrderableField(final OrderableField orderableField)
        {
            this.orderableField = orderableField;
            return this;
        }

        public Builder setRendererType(final String rendererType)
        {
            this.rendererType = rendererType;
            return this;
        }

        public Builder setRequired(final boolean required)
        {
            this.required = required;
            return this;
        }

        public Builder setFieldManager(final FieldManager fieldManager)
        {
            this.fieldManager = fieldManager;
            return this;
        }

        public Builder setFieldDescriptionHelper(final FieldDescriptionHelper fieldDescriptionHelper)
        {
            this.fieldDescriptionHelper = fieldDescriptionHelper;
            return this;
        }
    }
}
