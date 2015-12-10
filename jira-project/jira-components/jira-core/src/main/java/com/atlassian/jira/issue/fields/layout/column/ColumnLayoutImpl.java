package com.atlassian.jira.issue.fields.layout.column;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.issue.fields.layout.AbstractLayout;
import com.atlassian.jira.jql.context.QueryContext;
import com.google.common.collect.Lists;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public abstract class ColumnLayoutImpl extends AbstractLayout implements ColumnLayout
{
    private ColumnConfig columnConfig;
    private List<ColumnLayoutItem> columnLayoutItems;

    public ColumnLayoutImpl(List<ColumnLayoutItem> columnLayoutItems)
    {
        this(columnLayoutItems, ColumnConfig.NONE);
    }

    public ColumnLayoutImpl(List<ColumnLayoutItem> columnLayoutItems, final ColumnConfig columnConfig)
    {
        this.columnLayoutItems = columnLayoutItems;
        this.columnConfig = columnConfig;
    }

    public List<ColumnLayoutItem> getAllVisibleColumnLayoutItems(final User user)
    {
        try
        {
            final Set<NavigableField> availableFields = getFieldManager().getAvailableNavigableFieldsWithScope(user);
            return getVisibleColumnLayoutItems(availableFields);
        }
        catch (FieldException e)
        {
            throw new DataAccessException(e);
        }
    }

    public List<ColumnLayoutItem> getVisibleColumnLayoutItems(final User user, final QueryContext queryContext)
    {
        try
        {
            final Set<NavigableField> availableFields = getFieldManager().getAvailableNavigableFieldsWithScope(user, queryContext);
            return getVisibleColumnLayoutItems(availableFields);
        }
        catch (FieldException e)
        {
            throw new DataAccessException(e);
        }
    }

    private List<ColumnLayoutItem> getVisibleColumnLayoutItems(Set<NavigableField> availableFields)
    {
        final List<ColumnLayoutItem> visibleColumns = new LinkedList<ColumnLayoutItem>();
        for (ColumnLayoutItem layoutItem : getInternalList())
        {
            if (availableFields.contains(layoutItem.getNavigableField()))
            {
                visibleColumns.add(layoutItem);
            }
        }
        return visibleColumns;
    }

    public boolean contains(NavigableField navigableField)
    {
        for (ColumnLayoutItem columnLayoutItem : getInternalList())
        {
            if (columnLayoutItem.getNavigableField().equals(navigableField))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> asFieldNames()
    {
        return Lists.transform(getInternalList(), ColumnLayoutItem.TO_ID);
    }

    @Override
    public ColumnConfig getColumnConfig()
    {
        return columnConfig;
    }

    protected List<ColumnLayoutItem> getInternalList()
    {
        return columnLayoutItems;
    }

    protected FieldManager getFieldManager()
    {
        return ComponentAccessor.getFieldManager();
    }
}
