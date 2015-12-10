/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields.layout.field;

import java.util.ArrayList;
import java.util.List;

import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;

import org.apache.commons.lang3.StringUtils;
import org.ofbiz.core.entity.GenericValue;

public class EditableDefaultFieldLayoutImpl extends FieldLayoutImpl implements EditableDefaultFieldLayout
{
    public EditableDefaultFieldLayoutImpl(GenericValue genericValue, List<FieldLayoutItem> fieldLayoutItems)
    {
        super(genericValue, new ArrayList<FieldLayoutItem>(fieldLayoutItems));
    }

    protected void init()
    {
        if (getGenericValue() != null)
        {
            super.init();
        }
        else
        {
            // JIRA will start life with no explicit rows in the FieldLayout table.
            // This path sets up the implicit values for the Default Field Configuration.
            // See also FieldLayoutImpl.init()
            setId(null);
            setName(NAME);
            setDescription(DESCRIPTION);
            // type is hard-coded in the getType() method
        }
    }

    public void setDescription(final FieldLayoutItem fieldLayoutItem, final String description)
    {
        // If the description is an empty string then set it to null
        final String descriptionToSet = StringUtils.isBlank(description) ? null : description;
        final FieldLayoutItem modified = new FieldLayoutItemImpl.Builder(fieldLayoutItem)
                .setFieldDescription(descriptionToSet)
                .build();
        updateFieldLayoutItem(fieldLayoutItem, modified);
    }

    public void setRendererType(final FieldLayoutItem fieldLayoutItem, final String rendererType)
    {
        final OrderableField field = fieldLayoutItem.getOrderableField();
        if (!getHackyFieldRendererRegistry().shouldOverrideDefaultRenderers(field) && !getFieldManager().isRenderableField(field))
        {
            throw new IllegalArgumentException("Trying to set a renderer on a field that is not renderable.");
        }

        final FieldLayoutItem modified = new FieldLayoutItemImpl.Builder(fieldLayoutItem)
                .setRendererType(rendererType)
                .build();
        updateFieldLayoutItem(fieldLayoutItem, modified);
    }

    public String getType()
    {
        return FieldLayoutManager.TYPE_DEFAULT;
    }

    public void show(FieldLayoutItem fieldLayoutItem)
    {
        final OrderableField field = fieldLayoutItem.getOrderableField();
        if (!getFieldManager().isHideableField(field))
        {
            throw new IllegalArgumentException("Trying to set a field '" + field.getId() +
                    "' to be shown, but that field cannot ever be hidden.");
        }

        final FieldLayoutItem modified = new FieldLayoutItemImpl.Builder(fieldLayoutItem).setHidden(false).build();
        updateFieldLayoutItem(fieldLayoutItem, modified);
    }

    public void hide(FieldLayoutItem fieldLayoutItem)
    {
        final FieldManager fieldManager = getFieldManager();
        final OrderableField field = fieldLayoutItem.getOrderableField();
        if (!fieldManager.isHideableField(field))
        {
            throw new IllegalArgumentException("Trying to hide a field that is not hideable.");
        }

        // When hiding a field make it optional (not required) if the field is not mandatory, this should not happen
        // since it makes no sense to have a hidden required field, but if a developer wants to be dumb, let them...
        final FieldLayoutItem modified = new FieldLayoutItemImpl.Builder(fieldLayoutItem)
                .setHidden(true)
                .setRequired(fieldManager.isMandatoryField(field))
                .build();
        updateFieldLayoutItem(fieldLayoutItem, modified);
    }

    public void makeRequired(FieldLayoutItem fieldLayoutItem)
    {
        if (!getFieldManager().isRequirableField(fieldLayoutItem.getOrderableField()))
        {
            throw new IllegalArgumentException("Trying to require a field that is not requireable.");
        }

        // When requiring a field make it not hidden
        final FieldLayoutItem modified = new FieldLayoutItemImpl.Builder(fieldLayoutItem)
                .setHidden(false)
                .setRequired(true)
                .build();
        updateFieldLayoutItem(fieldLayoutItem, modified);
    }

    public void makeOptional(FieldLayoutItem fieldLayoutItem)
    {
        final OrderableField field = fieldLayoutItem.getOrderableField();
        if (!getFieldManager().isRequirableField(field))
        {
            throw new IllegalArgumentException("Trying to make field '" + field.getId() +
                    "' optional, but that field cannot ever have been required.");
        }

        final FieldLayoutItem modified = new FieldLayoutItemImpl.Builder(fieldLayoutItem)
                .setRequired(false)
                .build();
        updateFieldLayoutItem(fieldLayoutItem, modified);
    }

    private void updateFieldLayoutItem(final FieldLayoutItem fieldLayoutItem, final FieldLayoutItem modified)
    {
        final List<FieldLayoutItem> internalList = getInternalList();
        final int pos = internalList.indexOf(fieldLayoutItem);
        internalList.set(pos, modified);
        fieldLayoutItemByFieldId.put(modified.getOrderableField().getId(), modified);
    }

    public void setName(String name)
    {
        setInternalName(name);
    }

    public void setDescription(String description)
    {
        setInternalDescription(description);
    }

    public int hashCode()
    {
        return super.hashCode() + 29 * getInternalList().hashCode();
    }

    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (!(o instanceof FieldLayout))
            return false;

        final FieldLayout fieldLayout = (FieldLayout) o;

        return super.equals(o) && getFieldLayoutItems().equals(fieldLayout.getFieldLayoutItems());
    }


}
