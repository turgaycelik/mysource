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
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItemImpl;
import com.atlassian.jira.issue.fields.renderer.HackyFieldRendererRegistry;
import com.atlassian.jira.issue.fields.renderer.text.DefaultTextRenderer;
import webwork.action.Action;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * BulkFiedScreenRenderLayoutItem
 * - encapsulates a fieldScreenLayoutItem and the associated fieldlayouts for a collection of issues
 */
public class BulkFieldScreenRenderLayoutItemImpl implements FieldScreenRenderLayoutItem
{
    private final Collection<FieldLayoutItem> fieldLayoutItems;
    private final FieldManager fieldManager;
    private final HackyFieldRendererRegistry hackyFieldRendererRegistry;
    private final FieldScreenLayoutItem fieldScreenLayoutItem;

    BulkFieldScreenRenderLayoutItemImpl(FieldManager fieldManager, HackyFieldRendererRegistry hackyFieldRendererRegistry, FieldScreenLayoutItem fieldScreenLayoutItem, Collection<FieldLayoutItem> fieldLayoutItems)
    {
        this.fieldManager = fieldManager;
        this.hackyFieldRendererRegistry = hackyFieldRendererRegistry;
        this.fieldScreenLayoutItem = fieldScreenLayoutItem;
        this.fieldLayoutItems = fieldLayoutItems;
    }

    public FieldScreenLayoutItem getFieldScreenLayoutItem()
    {
        return fieldScreenLayoutItem;
    }

    public Collection<FieldLayoutItem> getFieldLayoutItems()
    {
        return fieldLayoutItems;
    }

    public String getEditHtml(Action action, OperationContext operationContext, Collection<Issue> issues, Map displayParameters)
    {
        Issue issue = null;

        // Iterate over the issues to make sure we can show them for editing
        //noinspection ForLoopReplaceableByForEach
        for (final Iterator<Issue> issueIterator = issues.iterator(); issueIterator.hasNext();)
        {
            issue = issueIterator.next();
            if (!isShow(issue))
            {
                return "";
            }

        }
        return fieldScreenLayoutItem.getOrderableField().getEditHtml(createBulkFieldLayoutItem(), operationContext, action, issue, displayParameters);
    }

    public String getViewHtml(Action action, OperationContext operationContext, Collection<Issue> issues, Map<String, ?> displayParameters)
    {
        Issue issue = null;

        // Iterate over the issues to make sure we can show them for editing
        //noinspection ForLoopReplaceableByForEach
        for (final Iterator<Issue> issueIterator = issues.iterator(); issueIterator.hasNext();)
        {
            issue = issueIterator.next();
            if (!isShow(issue))
            {
                return "";
            }
        }

        return fieldScreenLayoutItem.getOrderableField().getViewHtml(createBulkFieldLayoutItem(), action, issue, displayParameters);
    }

    public OrderableField getOrderableField()
    {
        return fieldScreenLayoutItem.getOrderableField();
    }

    // Determine if the fieldlayout item is required in at least one of the fieldlayouts.
    public boolean isRequired()
    {
        for (FieldLayoutItem fieldLayoutItem : fieldLayoutItems)
        {
            if (fieldLayoutItem.isRequired())
            {
                return true;
            }
        }
        return false;
    }

    // Determine if the fieldlayout item is hidden in at least one of the fieldlayouts.
    public boolean isShow(Issue issue)
    {
        if (fieldScreenLayoutItem != null && !fieldScreenLayoutItem.isShown(issue))
        {
            return false;
        }

        for (FieldLayoutItem fieldLayoutItem : fieldLayoutItems)
        {
            if (fieldLayoutItem.isHidden())
            {
                return false;
            }
        }
        return true;
    }

    // Creates a 'bulk' fieldlayoutitem - aggregation of isRequired and isShow of all fieldlayoutitems
    private FieldLayoutItem createBulkFieldLayoutItem()
    {
        final FieldLayoutItem fieldLayoutItem = fieldLayoutItems.iterator().next();

        // Assume isShow is true from getViewHTML
        final OrderableField orderableField = fieldLayoutItem.getOrderableField();
        FieldLayoutItemImpl.Builder builder = new FieldLayoutItemImpl.Builder(fieldLayoutItem).setFieldDescription(null).setRequired(isRequired());
        if (!hackyFieldRendererRegistry.shouldOverrideDefaultRenderers(orderableField) && !fieldManager.isRenderableField(orderableField))
        {
            builder.setRendererType(DefaultTextRenderer.RENDERER_TYPE);
        }
        return builder.build();
    }

    // INTERFACE METHODS NOT USED --------------------------------------------------------------------------------------

    public void populateDefaults(Map fieldValuesHolder, Issue issue)
    {
        throw new UnsupportedOperationException("This method is not available for BulkFieldScreenRenderLayoutItem");
    }

    public void populateFromIssue(Map fieldValuesHolder, Issue issue)
    {
        throw new UnsupportedOperationException("This method is not available for BulkFieldScreenRenderLayoutItem");
    }

    public String getRendererType()
    {
        throw new UnsupportedOperationException("This method is not available for BulkFieldScreenRenderLayoutItem");
    }

    public FieldLayoutItem getFieldLayoutItem()
    {
        throw new UnsupportedOperationException("This method is not available for BulkFieldScreenRenderLayoutItem");
    }

    public String getEditHtml(Action action, OperationContext operationContext, Issue issue)
    {
        throw new UnsupportedOperationException("This method is not available for BulkFieldScreenRenderLayoutItem");
    }

    public String getCreateHtml(Action action, OperationContext operationContext, Issue issue)
    {
        throw new UnsupportedOperationException("This method is not available for BulkFieldScreenRenderLayoutItem");
    }

    public String getViewHtml(Action action, OperationContext operationContext, Issue issue)
    {
        throw new UnsupportedOperationException("This method is not available for BulkFieldScreenRenderLayoutItem");
    }

    public String getCreateHtml(final Action action, final OperationContext operationContext, final Issue issue, final Map<String, Object> displayParams)
    {
        throw new UnsupportedOperationException("This method is not available for BulkFieldScreenRenderLayoutItem");
    }

    public String getEditHtml(final Action action, final OperationContext operationContext, final Issue issue, final Map<String, Object> displayParams)
    {
        throw new UnsupportedOperationException("This method is not available for BulkFieldScreenRenderLayoutItem");
    }

    public String getViewHtml(final Action action, final OperationContext operationContext, final Issue issue, final Map<String, Object> displayParams)
    {
        throw new UnsupportedOperationException("This method is not available for BulkFieldScreenRenderLayoutItem");
    }
}
