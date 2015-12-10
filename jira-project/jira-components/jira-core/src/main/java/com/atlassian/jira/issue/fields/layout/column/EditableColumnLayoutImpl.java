/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields.layout.column;

import com.atlassian.jira.issue.fields.NavigableField;

import java.util.ArrayList;
import java.util.List;

public abstract class EditableColumnLayoutImpl extends ColumnLayoutImpl implements EditableColumnLayout
{
    public EditableColumnLayoutImpl(List<ColumnLayoutItem> columnLayoutItems, ColumnConfig columnConfig)
    {
        // Ensure that the list IS modifiable
        super(new ArrayList(columnLayoutItems), columnConfig);
    }

    public EditableColumnLayoutImpl(List<ColumnLayoutItem> columnLayoutItems)
    {
        this(columnLayoutItems, ColumnConfig.NONE);
    }

    public void addColumn(NavigableField navigableField)
    {
        // Insert the column at the end of the list
        ColumnLayoutItem columnLayoutItem = new ColumnLayoutItemImpl(navigableField, getInternalList().size());
        getInternalList().add(columnLayoutItem);
    }

    public void removeColumn(ColumnLayoutItem columnLayoutItem)
    {
        getInternalList().remove(columnLayoutItem);
    }

    public void moveColumnLeft(ColumnLayoutItem columnLayoutItem)
    {
        if (columnLayoutItem.getPosition() > 0)
        {
            List internalList = getInternalList();
            int moveLeftPosition = internalList.indexOf(columnLayoutItem);
            ColumnLayoutItem moveLeft = (ColumnLayoutItem) internalList.get(moveLeftPosition);
            ColumnLayoutItem moveRight = (ColumnLayoutItem) internalList.get(moveLeftPosition - 1);

            moveLeft = new ColumnLayoutItemImpl(moveLeft.getNavigableField(), (moveLeft.getPosition() - 1));
            moveRight = new ColumnLayoutItemImpl(moveRight.getNavigableField(), (moveRight.getPosition() + 1));

            internalList.set(moveLeftPosition - 1, moveLeft);
            internalList.set(moveLeftPosition, moveRight);
        }
        else
        {
            throw new IllegalArgumentException("Trying to move left-most column left.");
        }
    }

    public void setColumns(List<NavigableField> selectedFields)
    {
        getInternalList().clear();
        for (NavigableField field : selectedFields)
        {
            addColumn(field);
        }
    }

    public void moveColumnRight(ColumnLayoutItem columnLayoutItem)
    {
        List internalList = getInternalList();
        int moveRightPosition = internalList.indexOf(columnLayoutItem);
        if (moveRightPosition < (internalList.size() - 1))
        {
            ColumnLayoutItem moveRight = (ColumnLayoutItem) internalList.get(moveRightPosition);
            ColumnLayoutItem moveLeft = (ColumnLayoutItem) internalList.get(moveRightPosition + 1);

            moveRight = new ColumnLayoutItemImpl(moveRight.getNavigableField(), (moveRight.getPosition() + 1));
            moveLeft = new ColumnLayoutItemImpl(moveLeft.getNavigableField(), (moveLeft.getPosition() - 1));

            internalList.set(moveRightPosition + 1, moveRight);
            internalList.set(moveRightPosition, moveLeft);
        }
        else
        {
            throw new IllegalArgumentException("Trying to move right-most column right.");
        }
    }
}
