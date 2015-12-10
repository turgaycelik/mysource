/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields.layout.column;

import java.util.List;

public class DefaultColumnLayoutImpl extends ColumnLayoutImpl
{
    public DefaultColumnLayoutImpl(List<ColumnLayoutItem> columnLayoutItems)
    {
        super(columnLayoutItems);
    }

    public List<ColumnLayoutItem> getColumnLayoutItems()
    {
        return getInternalList();
    }
}
