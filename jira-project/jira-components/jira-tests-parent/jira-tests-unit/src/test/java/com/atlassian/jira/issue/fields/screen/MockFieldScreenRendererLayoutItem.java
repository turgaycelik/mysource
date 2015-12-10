package com.atlassian.jira.issue.fields.screen;

import java.util.Map;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import webwork.action.Action;

/**
 * A simple mock implementation of {@link com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem}.
 *
 * @since v4.1
 */
public class MockFieldScreenRendererLayoutItem implements FieldScreenRenderLayoutItem
{
    private boolean required;
    private OrderableField orderableField;
    private FieldLayoutItem layoutItem;
    private FieldScreenLayoutItem fieldScreenLayoutItem;

    public OrderableField getOrderableField()
    {
        return orderableField;
    }

    public MockFieldScreenRendererLayoutItem setOrderableField(final OrderableField orderableField)
    {
        this.orderableField = orderableField;
        return this;
    }

    public String getEditHtml(final Action action, final OperationContext operationContext, final Issue issue)
    {
        throw new UnsupportedOperationException();
    }

    public String getCreateHtml(final Action action, final OperationContext operationContext, final Issue issue)
    {
        throw new UnsupportedOperationException();
    }

    public String getViewHtml(final Action action, final OperationContext operationContext, final Issue issue)
    {
        throw new UnsupportedOperationException();
    }

    public String getEditHtml(final Action action, final OperationContext operationContext, final Issue issue, final Map<String, Object> displayParams)
    {
        throw new UnsupportedOperationException();
    }

    public String getCreateHtml(final Action action, final OperationContext operationContext, final Issue issue, final Map<String, Object> displayParams)
    {
        throw new UnsupportedOperationException();
    }

    public String getViewHtml(final Action action, final OperationContext operationContext, final Issue issue, final Map<String, Object> displayParams)
    {
        throw new UnsupportedOperationException();
    }

    public boolean isShow(final Issue issue)
    {
        throw new UnsupportedOperationException();
    }

    public void populateDefaults(final Map fieldValuesHolder, final Issue issue)
    {
        throw new UnsupportedOperationException();
    }

    public boolean isRequired()
    {
        return required;
    }

    public MockFieldScreenRendererLayoutItem setRequired(final boolean required)
    {
        this.required = required;
        return this;
    }

    public void populateFromIssue(final Map fieldValuesHolder, final Issue issue)
    {
        throw new UnsupportedOperationException();
    }

    public String getRendererType()
    {
        return layoutItem.getRendererType();
    }

    public FieldLayoutItem getFieldLayoutItem()
    {
        return layoutItem;
    }

    public MockFieldScreenRendererLayoutItem setFieldLayoutItem(FieldLayoutItem layoutItem)
    {
        this.layoutItem = layoutItem;
        return this;
    }

    public FieldScreenLayoutItem getFieldScreenLayoutItem()
    {
        return fieldScreenLayoutItem;
    }

    public MockFieldScreenRendererLayoutItem setFieldScreenLayoutItem(FieldScreenLayoutItem fieldScreenLayoutItem)
    {
        this.fieldScreenLayoutItem = fieldScreenLayoutItem;
        return this;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
