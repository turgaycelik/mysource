package com.atlassian.jira.upgrade.tasks;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.UnscreenableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import org.ofbiz.core.entity.GenericValue;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public abstract class AbstractFieldScreenUpgradeTask extends AbstractUpgradeTask
{
    protected AbstractFieldScreenUpgradeTask(boolean reindexrequired)
    {
        super(reindexrequired);
    }

    protected void populateFieldScreenTab(FieldManager fieldManager, FieldLayout fieldLayout, FieldScreenTab fieldScreenTab)
    {
        // The field layout returns the field layout items sorted by name. We need to sort them by 'old' position.
        List<GenericValue> layoutItemGVs = getOfBizDelegator().findByAnd("FieldLayoutItem", EasyMap.build("fieldlayout", fieldLayout.getId()), EasyList.build("verticalposition ASC"));
        List<FieldLayoutItem> fieldLayoutItems = new LinkedList<FieldLayoutItem>(fieldLayout.getFieldLayoutItems());

        for (final GenericValue fieldLayoutItemGV : layoutItemGVs)
        {
            String fieldId = fieldLayoutItemGV.getString("fieldidentifier");
            OrderableField orderableField = fieldManager.getOrderableField(fieldId);
            if (!IssueFieldConstants.RESOLUTION.equals(fieldId) && orderableField != null && !(orderableField instanceof UnscreenableField))
            {
                if (fieldLayout.getFieldLayoutItem(orderableField) != null)
                {
                    fieldScreenTab.addFieldScreenLayoutItem(fieldId);
                    removeItemFromList(fieldLayoutItems, fieldId);
                }
            }
        }

        // Add field layout items that were not saved in the database.
        for (final FieldLayoutItem fieldLayoutItem : fieldLayoutItems)
        {
            if (fieldLayoutItem != null && fieldLayoutItem.getOrderableField() != null && !fieldLayoutItem.isHidden() && !IssueFieldConstants.RESOLUTION.equals(fieldLayoutItem.getOrderableField().getId()) && !(fieldLayoutItem.getOrderableField() instanceof UnscreenableField))
            {
                fieldScreenTab.addFieldScreenLayoutItem(fieldLayoutItem.getOrderableField().getId());
            }
        }

        // If timetracking is not part of the default field layout then add it anyway - so if timetracking is enabled
        // the timetracking field will show up straight away.
        OrderableField timtetrackingField = fieldManager.getOrderableField(IssueFieldConstants.TIMETRACKING);
        if (fieldLayout.getFieldLayoutItem(timtetrackingField) == null)
        {
            fieldScreenTab.addFieldScreenLayoutItem(timtetrackingField.getId());
        }
    }

    private void removeItemFromList(List<FieldLayoutItem> fieldLayoutItems, String fieldId)
    {
        for (Iterator<FieldLayoutItem> iterator = fieldLayoutItems.iterator(); iterator.hasNext();)
        {
            FieldLayoutItem fieldLayoutItem = iterator.next();
            if (fieldId.equals(fieldLayoutItem.getOrderableField().getId()))
            {
                iterator.remove();
                break;
            }
        }
    }
}
