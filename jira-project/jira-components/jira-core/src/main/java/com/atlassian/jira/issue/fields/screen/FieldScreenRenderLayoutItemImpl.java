package com.atlassian.jira.issue.fields.screen;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import webwork.action.Action;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (c) 2002-2004 All rights reserved.
 */
public class FieldScreenRenderLayoutItemImpl implements FieldScreenRenderLayoutItem
{
    private final FieldLayoutItem fieldLayoutItem;
    private final FieldScreenLayoutItem fieldScreenLayoutItem;

    public FieldScreenRenderLayoutItemImpl(FieldScreenLayoutItem fieldScreenLayoutItem, FieldLayoutItem fieldLayoutItem)
    {
        this.fieldScreenLayoutItem = fieldScreenLayoutItem;
        this.fieldLayoutItem = fieldLayoutItem;
    }

    public OrderableField getOrderableField()
    {
        return fieldLayoutItem.getOrderableField();
    }

    public String getEditHtml(Action action, OperationContext operationContext, Issue issue)
    {
        return getEditHtml(action, operationContext, issue, new HashMap<String, Object>());
    }

    public String getCreateHtml(Action action, OperationContext operationContext, Issue issue)
    {
        return getCreateHtml(action, operationContext, issue, new HashMap<String, Object>());
    }

    public String getViewHtml(Action action, OperationContext operationContext, Issue issue)
    {
        return getViewHtml(action, operationContext, issue, new HashMap<String, Object>());
    }

    public String getCreateHtml(final Action action, final OperationContext operationContext, final Issue issue, final Map<String, Object> displayParams)
    {
        if (isShow(issue))
        {
            return fieldScreenLayoutItem.getCreateHtml(fieldLayoutItem, operationContext, action, issue, displayParams);
        }
        else
        {
            return "";
        }
    }

    public String getEditHtml(final Action action, final OperationContext operationContext, final Issue issue, final Map<String, Object> displayParams)
    {
        if (isShow(issue))
        {
            return fieldScreenLayoutItem.getEditHtml(fieldLayoutItem, operationContext, action, issue, displayParams);
        }
        else
        {
            return "";
        }
    }

    public String getViewHtml(final Action action, final OperationContext operationContext, final Issue issue, final Map<String, Object> displayParams)
    {
        if (isShow(issue))
        {
            return fieldScreenLayoutItem.getViewHtml(fieldLayoutItem, operationContext, action, issue, displayParams);
        }
        else
        {
            return "";
        }
    }

    public boolean isShow(Issue issue)
    {
        return !fieldLayoutItem.isHidden() && fieldScreenLayoutItem != null && fieldScreenLayoutItem.isShown(issue);
    }

    public void populateDefaults(Map fieldValuesHolder, Issue issue)
    {
        // If the item is shown then populate the defaults
        if (isShow(issue))
        {
            getOrderableField().populateDefaults(fieldValuesHolder, issue);
        }
    }

    public boolean isRequired()
    {
        return fieldLayoutItem.isRequired();
    }

    public void populateFromIssue(Map fieldValuesHolder, Issue issue)
    {
        getOrderableField().populateFromIssue(fieldValuesHolder, issue);
    }

    public String getRendererType()
    {
        return fieldLayoutItem.getRendererType();
    }

    public FieldLayoutItem getFieldLayoutItem()
    {
        return fieldLayoutItem;
    }

    public FieldScreenLayoutItem getFieldScreenLayoutItem()
    {
        return fieldScreenLayoutItem;
    }
}
