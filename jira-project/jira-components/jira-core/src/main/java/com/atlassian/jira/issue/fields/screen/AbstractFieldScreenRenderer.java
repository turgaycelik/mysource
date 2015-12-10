package com.atlassian.jira.issue.fields.screen;

import com.atlassian.jira.issue.fields.OrderableField;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

abstract class AbstractFieldScreenRenderer implements FieldScreenRenderer
{
    public Collection<FieldScreenRenderLayoutItem> getRequiredFieldScreenRenderItems()
    {
        final Collection<FieldScreenRenderLayoutItem> items = new LinkedList<FieldScreenRenderLayoutItem>();
        final List<FieldScreenRenderTab> tabList = getFieldScreenRenderTabs();
        if (tabList != null)
        {
            for (final FieldScreenRenderTab fieldScreenRenderTab : tabList)
            {
                for (FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem : fieldScreenRenderTab.getFieldScreenRenderLayoutItems())
                {
                    if (fieldScreenRenderLayoutItem.isRequired())
                    {
                        items.add(fieldScreenRenderLayoutItem);
                    }
                }
            }
        }

        return items;
    }

    /**
     * Return null if cannot find tab for the given fieldId
     */
    public FieldScreenRenderTab getFieldScreenRenderTabPosition(String fieldId)
    {
        final List<FieldScreenRenderTab> tabList = getFieldScreenRenderTabs();
        if (tabList != null)
        {
            for (FieldScreenRenderTab fieldScreenRenderTab : tabList)
            {
                for (FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem : fieldScreenRenderTab.getFieldScreenRenderLayoutItems())
                {
                    if (fieldScreenRenderLayoutItem.getOrderableField().getId().equals(fieldId))
                    {
                        return fieldScreenRenderTab;
                    }
                }
            }
        }

        return null;
    }

    public FieldScreenRenderLayoutItem getFieldScreenRenderLayoutItem(OrderableField orderableField)
    {
        final List<FieldScreenRenderTab> tabList = getFieldScreenRenderTabs();
        if (tabList != null)
        {
            for (FieldScreenRenderTab fieldScreenRenderTab : tabList)
            {
                for (FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem : fieldScreenRenderTab.getFieldScreenRenderLayoutItems())
                {
                    if (fieldScreenRenderLayoutItem.getOrderableField().getId().equals(orderableField.getId()))
                    {
                        return fieldScreenRenderLayoutItem;
                    }
                }
            }
        }

        return new FieldScreenRenderLayoutItemImpl(null, getFieldLayout().getFieldLayoutItem(orderableField));
    }

    public List<FieldScreenRenderLayoutItem> getAllScreenRenderItems()
    {
        final List<FieldScreenRenderLayoutItem> returnList = new ArrayList<FieldScreenRenderLayoutItem>();
        final List<FieldScreenRenderTab> tabList = getFieldScreenRenderTabs();
        if (tabList != null)
        {
            for (FieldScreenRenderTab fieldScreenRenderTab : tabList)
            {
                returnList.addAll(fieldScreenRenderTab.getFieldScreenRenderLayoutItems());
            }
        }

        return returnList;
    }
}
