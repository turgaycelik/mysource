package com.atlassian.jira.issue.fields.screen;

import com.atlassian.jira.issue.IssueFieldConstants;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public class FieldScreenRenderTabImpl implements FieldScreenRenderTab
{
    private final String name;
    private final List<FieldScreenRenderLayoutItem> fieldScreenRenderLayoutItems;
    private final int position;

    public FieldScreenRenderTabImpl(String name, int position, List<FieldScreenRenderLayoutItem> fieldScreenRenderLayoutItems)
    {
        this.name = name;
        this.position = position;
        this.fieldScreenRenderLayoutItems = Collections.unmodifiableList(fieldScreenRenderLayoutItems);
    }

    public String getName()
    {
        return name;
    }

    public int getPosition()
    {
        return position;
    }

    public List<FieldScreenRenderLayoutItem> getFieldScreenRenderLayoutItems()
    {
        return fieldScreenRenderLayoutItems;
    }

    public List<FieldScreenRenderLayoutItem> getFieldScreenRenderLayoutItemsForProcessing()
    {
        // Ensure assignee is processed after components, so component assignees are resolved correctly. Do this
        // by placing the assignee item last in the list.
        final List<FieldScreenRenderLayoutItem> items = new LinkedList<FieldScreenRenderLayoutItem>(fieldScreenRenderLayoutItems);
        for (final Iterator<FieldScreenRenderLayoutItem> iterator = items.iterator(); iterator.hasNext();)
        {
            final FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem = iterator.next();
            if (IssueFieldConstants.ASSIGNEE.equals(fieldScreenRenderLayoutItem.getOrderableField().getId()))
            {
                iterator.remove();
                items.add(fieldScreenRenderLayoutItem);
                break;
            }
        }

        return Collections.unmodifiableList(items);
    }

    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof FieldScreenRenderTabImpl)) return false;

        final FieldScreenRenderTabImpl fieldScreenRenderTab = (FieldScreenRenderTabImpl) o;

        if (position != fieldScreenRenderTab.position) return false;
        if (name != null ? !name.equals(fieldScreenRenderTab.name) : fieldScreenRenderTab.name != null) return false;

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (name != null ? name.hashCode() : 0);
        result = 29 * result + position;
        return result;
    }

    public int compareTo(FieldScreenRenderTab o)
    {
        if (o == null)
            return 1;

        if (o.getPosition() > position)
            return -1;
        else if (o.getPosition() < position)
            return 1;
        else
            return 0;
    }
}
