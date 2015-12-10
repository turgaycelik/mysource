package com.atlassian.jira.issue.fields.screen;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;

import java.util.Map;

/**
 * Simple implementation of a {@link FieldScreenLayoutItem}.
 *
 * @since v4.1
 */
public class MockFieldScreenLayoutItem implements FieldScreenLayoutItem
{
    private Long id;
    private int position;
    private OrderableField field;
    private String fieldId;
    private FieldScreenTab tab;

    public MockFieldScreenLayoutItem()
    {
    }

    public MockFieldScreenLayoutItem(FieldScreenLayoutItem item)
    {
        this.id = item.getId();
        this.position = item.getPosition();
        this.field = item.getOrderableField();
        this.fieldId = item.getFieldId();
        this.tab = item.getFieldScreenTab();
    }

    public Long getId()
    {
        return id;
    }

    public MockFieldScreenLayoutItem setId(Long id)
    {
        this.id = id;
        return this;
    }

    public int getPosition()
    {
        return position;
    }

    public void setPosition(final int position)
    {
        this.position = position;
    }

    public String getFieldId()
    {
        if (fieldId == null && field != null)
        {
            return field.getId();
        }
        return fieldId;
    }

    public void setFieldId(final String fieldId)
    {
        this.fieldId = fieldId;
    }

    public OrderableField getOrderableField()
    {
        return field;
    }

    public MockFieldScreenLayoutItem setOrderableField(OrderableField field)
    {
        this.field = field;
        return this;
    }

    public String getEditHtml(final FieldLayoutItem fieldLayoutItem, final OperationContext operationContext, final Action action, final Issue issue)
    {
        throw new UnsupportedOperationException();
    }

    public String getCreateHtml(final FieldLayoutItem fieldLayoutItem, final OperationContext operationContext, final Action action, final Issue issue)
    {
        throw new UnsupportedOperationException();
    }

    public String getViewHtml(final FieldLayoutItem fieldLayoutItem, final OperationContext operationContext, final Action action, final Issue issue)
    {
        throw new UnsupportedOperationException();
    }

    public String getEditHtml(final FieldLayoutItem fieldLayoutItem, final OperationContext operationContext, final Action action, final Issue issue, final Map<String, Object> displayParams)
    {
        throw new UnsupportedOperationException();
    }

    public String getCreateHtml(final FieldLayoutItem fieldLayoutItem, final OperationContext operationContext, final Action action, final Issue issue, final Map<String, Object> displayParams)
    {
        throw new UnsupportedOperationException();
    }

    public String getViewHtml(final FieldLayoutItem fieldLayoutItem, final OperationContext operationContext, final Action action, final Issue issue, final Map<String, Object> displayParams)
    {
        throw new UnsupportedOperationException();
    }

    public boolean isShown(final Issue issue)
    {
        return field.isShown(issue);
    }

    public FieldScreenTab getFieldScreenTab()
    {
        return tab;
    }

    public void setFieldScreenTab(final FieldScreenTab fieldScreenTab)
    {
        this.tab = fieldScreenTab;
    }

    public GenericValue getGenericValue()
    {
        throw new UnsupportedOperationException();
    }

    public void setGenericValue(final GenericValue genericValue)
    {
        throw new UnsupportedOperationException();
    }

    public void store()
    {
        throw new UnsupportedOperationException();
    }

    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
