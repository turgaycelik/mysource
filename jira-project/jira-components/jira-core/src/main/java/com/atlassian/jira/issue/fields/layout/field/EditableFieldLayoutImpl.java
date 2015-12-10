/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields.layout.field;

import org.ofbiz.core.entity.GenericValue;

import java.util.List;

public class EditableFieldLayoutImpl extends EditableDefaultFieldLayoutImpl
{
    public EditableFieldLayoutImpl(GenericValue genericValue, List<FieldLayoutItem> fieldLayoutItems)
    {
        super(genericValue, fieldLayoutItems);
    }

    public String getType()
    {
        // Return null as non-default field layout items do not have a type
        return null;
    }
}
