/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields.layout.column;

import java.util.ArrayList;
import java.util.List;

public class EditableDefaultColumnLayoutImpl extends EditableColumnLayoutImpl implements EditableDefaultColumnLayout
{
    public EditableDefaultColumnLayoutImpl(List columnLayoutItems)
    {
        super(new ArrayList(columnLayoutItems), ColumnConfig.SYSTEM);
    }

    public List getColumnLayoutItems()
    {
        return getInternalList();
    }
}
