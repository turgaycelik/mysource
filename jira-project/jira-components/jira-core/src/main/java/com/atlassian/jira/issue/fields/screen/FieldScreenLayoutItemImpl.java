package com.atlassian.jira.issue.fields.screen;

import com.atlassian.jira.issue.fields.FieldManager;
import org.ofbiz.core.entity.GenericValue;

/**
 * Copyright (c) 2002-2004 All rights reserved.
 */
public class FieldScreenLayoutItemImpl extends AbstractFieldScreenLayoutItem
{
    private Long id;

    public FieldScreenLayoutItemImpl(FieldScreenManager fieldScreenManager, FieldManager fieldManager)
    {
        this(fieldScreenManager, fieldManager, null);
    }

    public FieldScreenLayoutItemImpl(FieldScreenManager fieldScreenManager, FieldManager fieldManager, GenericValue genericValue)
    {
        super(fieldScreenManager, fieldManager);
        setGenericValue(genericValue);
    }

    protected void init()
    {
        if (getGenericValue() != null)
        {
            this.id = getGenericValue().getLong("id");
            this.fieldId = getGenericValue().getString("fieldidentifier");
            if (getGenericValue().getLong("sequence") != null)
            {
                this.position = getGenericValue().getLong("sequence").intValue();
            }
        }

        setModified(false);
    }

    public Long getId()
    {
        return id;
    }

    public void setPosition(int position)
    {
        this.position = position;
        updateGV("sequence", (long) position);
    }

    public void setFieldId(String fieldId)
    {
        this.fieldId = fieldId;
        updateGV("fieldidentifier", fieldId);
    }

    public void setFieldScreenTab(FieldScreenTab fieldScreenTab)
    {
        this.fieldScreenTab = fieldScreenTab;

        if (fieldScreenTab == null)
        {
            updateGV("fieldscreentab", null);
        }
        else
        {
            updateGV("fieldscreentab", fieldScreenTab.getId());
        }
    }

    public void store()
    {
        if (isModified())
        {
            if (id == null)
            {
                getFieldScreenManager().createFieldScreenLayoutItem(this);
            }
            else
            {
                getFieldScreenManager().updateFieldScreenLayoutItem(this);
                setModified(false);
            }
        }
    }

    public void remove()
    {
        getFieldScreenManager().removeFieldScreenLayoutItem(this);
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof FieldScreenLayoutItem))
        {
            return false;
        }

        final FieldScreenLayoutItem fieldScreenLayoutItem = (FieldScreenLayoutItem) o;

        if (id != null ? !id.equals(fieldScreenLayoutItem.getId()) : fieldScreenLayoutItem.getId() != null)
        {
            return false;
        }

        return super.equals(o);
    }

    public int hashCode()
    {
        int result;
        result = (id != null ? id.hashCode() : 0);
        result = 29 * result + super.hashCode();
        return result;
    }
}
