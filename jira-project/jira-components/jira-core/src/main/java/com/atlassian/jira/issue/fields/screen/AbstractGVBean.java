package com.atlassian.jira.issue.fields.screen;

import com.google.common.base.Objects;

import org.ofbiz.core.entity.GenericValue;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public abstract class AbstractGVBean
{
    private GenericValue genericValue;
    private boolean modified;

    public GenericValue getGenericValue()
    {
        return genericValue;
    }

    public void setGenericValue(GenericValue genericValue)
    {
        this.genericValue = genericValue;
        init();
    }

    protected abstract void init();

    protected void updateGV(String fieldName, Object value)
    {
        if (genericValue != null)
        {
            if (!Objects.equal(value, genericValue.get(fieldName)))
            {
                genericValue.set(fieldName, value);
                modified = true;
            }
        }
        else
        {
            modified = true;
        }
    }

    public boolean isModified()
    {
        return modified;
    }

    protected void setModified(boolean modified)
    {
        this.modified = modified;
    }
}
