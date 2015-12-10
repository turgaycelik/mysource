package com.atlassian.jira.issue.fields.screen;

import com.atlassian.jira.util.CollectionReorderer;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public class FieldScreenTabImpl extends AbstractGVBean implements FieldScreenTab
{
    private final static CollectionReorderer<FieldScreenLayoutItem> collectionReorderer = new CollectionReorderer<FieldScreenLayoutItem>();

    private final FieldScreenManager fieldScreenManager;

    private List<FieldScreenLayoutItem> layoutItems;
    private Map<String, FieldScreenLayoutItem> layoutItemsMap;

    private Long id;
    private String name;
    private int position;
    private FieldScreen fieldScreen;

    public FieldScreenTabImpl(FieldScreenManager fieldScreenManager)
    {
        this(fieldScreenManager, null);
    }

    public FieldScreenTabImpl(FieldScreenManager fieldScreenManager, GenericValue genericValue)
    {
        this.fieldScreenManager = fieldScreenManager;
        setGenericValue(genericValue);
        this.layoutItems = null;
    }

    protected void init()
    {
        if (getGenericValue() != null)
        {
            this.id = getGenericValue().getLong("id");
            this.name = getGenericValue().getString("name");

            if (getGenericValue().getLong("sequence") != null)
                this.position = getGenericValue().getLong("sequence").intValue();

        }

        setModified(false);
    }

    public Long getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
        updateGV("name", name);
    }

    public int getPosition()
    {
        return position;
    }

    public void setPosition(int position)
    {
        this.position = position;
        updateGV("sequence", (long) position);
    }

    public void store()
    {
        if (isModified())
        {
            if (id == null)
            {
                fieldScreenManager.createFieldScreenTab(this);
            }
            else
            {
                fieldScreenManager.updateFieldScreenTab(this);
                setModified(false);
            }
        }

        // If the layout items have been loaded, see if we need to persist any of them
        // If the items have not been loaded they have not been modified and there is no need to persist them
        if (layoutItems != null)
        {
            for (FieldScreenLayoutItem fieldScreenLayoutItem : getInternalLayoutItems())
            {
                fieldScreenLayoutItem.store();
            }
        }
    }

    public void remove()
    {
        fieldScreenManager.removeFieldScreenLayoutItems(this);
        fieldScreenManager.removeFieldScreenTab(getId());
    }

    public FieldScreen getFieldScreen()
    {
        return fieldScreen;
    }

    @Override
    public void rename(String newName)
    {
        setName(newName);
        store();
    }

    public void setFieldScreen(FieldScreen fieldScreen)
    {
        this.fieldScreen = fieldScreen;
        if (fieldScreen == null)
            updateGV("fieldscreen", null);
        else
            updateGV("fieldscreen", fieldScreen.getId());
    }

    public List<FieldScreenLayoutItem> getFieldScreenLayoutItems()
    {
        return Collections.unmodifiableList(getInternalLayoutItems());
    }

    private List<FieldScreenLayoutItem> getInternalLayoutItems()
    {
        if (layoutItems == null)
        {
            initInternalItems();
        }

        return layoutItems;
    }

    private Map<String, FieldScreenLayoutItem> getInternalLayoutItemsMap()
    {
        if (layoutItemsMap == null)
        {
            initInternalItems();
        }

        return layoutItemsMap;
    }

    private void initInternalItems()
    {
        layoutItems = fieldScreenManager.getFieldScreenLayoutItems(this);
        // Initialise the map to speed up access using field id.
        layoutItemsMap = new HashMap<String, FieldScreenLayoutItem>();
        for (FieldScreenLayoutItem fieldScreenLayoutItem : layoutItems)
        {
            layoutItemsMap.put(fieldScreenLayoutItem.getFieldId(), fieldScreenLayoutItem);
        }
    }

    public FieldScreenLayoutItem getFieldScreenLayoutItem(int poistion)
    {
        return getInternalLayoutItems().get(poistion);
    }

    public void addFieldScreenLayoutItem(String orderableFieldId)
    {
        addFieldScreenLayoutItem(orderableFieldId, getInternalLayoutItems().size());
    }

    public void addFieldScreenLayoutItem(String orderableFieldId, int position)
    {
        FieldScreenLayoutItem fieldScreenLayoutItem = fieldScreenManager.buildNewFieldScreenLayoutItem(orderableFieldId);
        fieldScreenLayoutItem.setFieldScreenTab(this);
        getInternalLayoutItems().add(position, fieldScreenLayoutItem);
        getInternalLayoutItemsMap().put(fieldScreenLayoutItem.getFieldId(), fieldScreenLayoutItem);
        resequence(position);
        store();
    }

    public void moveFieldScreenLayoutItemFirst(int fieldPosition)
    {
        collectionReorderer.moveToStart(getInternalLayoutItems(), getInternalLayoutItems().get(fieldPosition));
        resequence();
        store();
    }

    public void moveFieldScreenLayoutItemUp(int fieldPosition)
    {
        collectionReorderer.increasePosition(getInternalLayoutItems(), getInternalLayoutItems().get(fieldPosition));
        resequence();
        store();
    }

    public void moveFieldScreenLayoutItemDown(int fieldPosition)
    {
        collectionReorderer.decreasePosition(getInternalLayoutItems(), getInternalLayoutItems().get(fieldPosition));
        resequence();
        store();
    }

    public void moveFieldScreenLayoutItemLast(int fieldPosition)
    {
        collectionReorderer.moveToEnd(getInternalLayoutItems(), getInternalLayoutItems().get(fieldPosition));
        resequence();
        store();
    }

    public FieldScreenLayoutItem removeFieldScreenLayoutItem(int fieldPosition)
    {
        FieldScreenLayoutItem fieldScreenLayoutItem = getInternalLayoutItems().remove(fieldPosition);
        getInternalLayoutItemsMap().remove(fieldScreenLayoutItem.getFieldId());
        fieldScreenLayoutItem.remove();
        resequence();
        store();
        return fieldScreenLayoutItem;
    }

    public void moveFieldScreenLayoutItemToPosition(Map<Integer, FieldScreenLayoutItem> positionsToFields)
    {
        if (positionsToFields.isEmpty())
            return;

        collectionReorderer.moveToPosition(getInternalLayoutItems(), positionsToFields);
        resequence();
        store();
    }

    public FieldScreenLayoutItem getFieldScreenLayoutItem(String fieldId)
    {
        return getInternalLayoutItemsMap().get(fieldId);
    }

    public boolean isContainsField(String fieldId)
    {
        return getInternalLayoutItemsMap().containsKey(fieldId);
    }

    private void resequence()
    {
        resequence(0);
    }

    private void resequence(int startIndex)
    {
        for (int i = startIndex; i < getInternalLayoutItems().size(); i++)
        {
            FieldScreenLayoutItem fieldScreenLayoutItem = getInternalLayoutItems().get(i);
            fieldScreenLayoutItem.setPosition(i);
        }
    }

    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof FieldScreenTab)) return false;

        final FieldScreenTab fieldScreenTab = (FieldScreenTab) o;

        if (position != fieldScreenTab.getPosition()) return false;
        if (id != null ? !id.equals(fieldScreenTab.getId()) : fieldScreenTab.getId() != null) return false;
        if (name != null ? !name.equals(fieldScreenTab.getName()) : fieldScreenTab.getName() != null) return false;

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (id != null ? id.hashCode() : 0);
        result = 29 * result + (name != null ? name.hashCode() : 0);
        result = 29 * result + position;
        return result;
    }
}
