package com.atlassian.jira.issue.fields.layout.column;

import com.atlassian.jira.issue.fields.NavigableField;

public class ExcelColumnLayoutItem extends ColumnLayoutItemImpl
{

    public ExcelColumnLayoutItem(NavigableField navigableField, int position)
    {
        super(navigableField, position);
    }

    public ExcelColumnLayoutItem(ColumnLayoutItem columnLayoutItem)
    {
        super(columnLayoutItem.getNavigableField(), columnLayoutItem.getPosition());
    }

    // for excel, we want the _full_ field name, rather than the short version
    public String getColumnHeadingKey()
    {
        return getNavigableField().getNameKey();
    }
}
