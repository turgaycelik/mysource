/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.issue.fields.screen;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import webwork.action.Action;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractFieldScreenLayoutItem extends AbstractGVBean implements FieldScreenLayoutItem
{
    private final FieldScreenManager fieldScreenManager;
    private final FieldManager fieldManager;

    protected int position;
    protected String fieldId;
    protected FieldScreenTab fieldScreenTab;

    protected AbstractFieldScreenLayoutItem(FieldScreenManager fieldScreenManager, FieldManager fieldManager)
    {
        this.fieldScreenManager = fieldScreenManager;
        this.fieldManager = fieldManager;
    }

    public int getPosition()
    {
        return position;
    }

    public String getFieldId()
    {
        return fieldId;
    }

    public OrderableField getOrderableField()
    {
        return fieldManager.getOrderableField(fieldId);
    }

    public String getEditHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue)
    {
        return getEditHtml(fieldLayoutItem, operationContext, action, issue, new HashMap<String, Object>());
    }

    public String getCreateHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue)
    {
        return getCreateHtml(fieldLayoutItem, operationContext, action, issue, new HashMap<String, Object>());
    }

    public String getViewHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue)
    {
        return getViewHtml(fieldLayoutItem, operationContext, action, issue, new HashMap<String, Object>());
    }

    public String getViewHtml(final FieldLayoutItem fieldLayoutItem, final OperationContext operationContext, final Action action, final Issue issue, final Map<String, Object> displayParams)
    {
        if (isShown(issue))
        {
            return getOrderableField().getViewHtml(fieldLayoutItem, action, issue, displayParams);
        }
        else
        {
            return "";
        }
    }

    public String getEditHtml(final FieldLayoutItem fieldLayoutItem, final OperationContext operationContext, final Action action, final Issue issue, final Map<String, Object> displayParams)
    {
        if (isShown(issue))
        {
            return getOrderableField().getEditHtml(fieldLayoutItem, operationContext, action, issue, displayParams);
        }
        else
        {
            return "";
        }
    }

    public String getCreateHtml(final FieldLayoutItem fieldLayoutItem, final OperationContext operationContext, final Action action, final Issue issue, final Map<String, Object> displayParams)
    {
        if (isShown(issue))
        {
            return getOrderableField().getCreateHtml(fieldLayoutItem, operationContext, action, issue, displayParams);
        }
        else
        {
            return "";
        }
    }

    public boolean isShown(Issue issue)
    {
        return getOrderableField().isShown(issue);
    }

    public FieldScreenTab getFieldScreenTab()
    {
        return fieldScreenTab;
    }

    protected FieldScreenManager getFieldScreenManager()
    {
        return fieldScreenManager;
    }

    protected FieldManager getFieldManager()
    {
        return fieldManager;
    }

    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof FieldScreenLayoutItem)) return false;

        final FieldScreenLayoutItem fieldScreenLayoutItem = (FieldScreenLayoutItem) o;

        if (position != fieldScreenLayoutItem.getPosition()) return false;
        if (getOrderableField() != null ? !getOrderableField().equals(fieldScreenLayoutItem.getOrderableField()) : fieldScreenLayoutItem.getOrderableField() != null) return false;
        if (fieldScreenTab != null ? !fieldScreenTab.equals(fieldScreenLayoutItem.getFieldScreenTab()) : fieldScreenLayoutItem.getFieldScreenTab() != null) return false;

        return true;
    }

    public int hashCode()
    {
        int result;
        result = position;
        result = 29 * result + (getOrderableField() != null ? getOrderableField().hashCode() : 0);
        result = 29 * result + (fieldScreenTab != null ? fieldScreenTab.hashCode() : 0);
        return result;
    }
}
