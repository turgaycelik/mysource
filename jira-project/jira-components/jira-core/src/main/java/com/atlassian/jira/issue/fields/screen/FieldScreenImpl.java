package com.atlassian.jira.issue.fields.screen;

import com.atlassian.jira.util.CollectionReorderer;
import org.ofbiz.core.entity.GenericValue;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public class FieldScreenImpl extends AbstractGVBean implements FieldScreen
{
    private final static CollectionReorderer<FieldScreenTab> collectionReorderer = new CollectionReorderer<FieldScreenTab>();
    private final FieldScreenManager fieldScreenManager;

    private List<FieldScreenTab> tabs;
    private String description;
    private String name;
    private Long id;

    public FieldScreenImpl(FieldScreenManager fieldScreenManager)
    {
        this(fieldScreenManager, null);
    }

    public FieldScreenImpl(FieldScreenManager fieldScreenManager, GenericValue genericValue)
    {
        this.fieldScreenManager = fieldScreenManager;
        this.tabs = null;
        setGenericValue(genericValue);
    }

    public FieldScreenImpl deepCopy()
    {
        return new FieldScreenImpl(fieldScreenManager, getGenericValue());
    }

    protected void init()
    {
        if (getGenericValue() != null)
        {
            this.id = getGenericValue().getLong("id");
            this.name = getGenericValue().getString("name");
            this.description = getGenericValue().getString("description");
        }

        setModified(false);
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        if (getGenericValue() != null)
            throw new IllegalStateException("Cannot change id of an existing entity.");

        this.id = id;
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

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
        updateGV("description", description);
    }

    public List<FieldScreenTab> getTabs()
    {
        return Collections.unmodifiableList(getInternalTabs());
    }

    private List<FieldScreenTab> getInternalTabs()
    {
        if (tabs == null)
        {
            tabs = fieldScreenManager.getFieldScreenTabs(this);
        }

        return tabs;
    }

    public FieldScreenTab getTab(int tabPosition)
    {
        return getInternalTabs().get(tabPosition);
    }

    public FieldScreenTab addTab(String tabName)
    {
        FieldScreenTab tab = new FieldScreenTabImpl(fieldScreenManager);
        tab.setName(tabName);
        tab.setPosition(getInternalTabs().size());
        tab.setFieldScreen(this);
        getInternalTabs().add(tab);
        resequence();
        store();
        return tab;
    }

    public void removeTab(int tabPosition)
    {
        FieldScreenTab fieldScreenTab = getInternalTabs().remove(tabPosition);
        if (fieldScreenTab != null)
        {
            // Remove aasociation to this screen from the tab
            fieldScreenTab.setFieldScreen(null);
            fieldScreenTab.remove();

            resequence();
            store();
        }
    }

    public void moveFieldScreenTabToPosition(int tabPosition, int newPosition)
    {
        collectionReorderer.moveToPosition(getInternalTabs(), tabPosition, newPosition);
        resequence();
    }

    public void moveFieldScreenTabLeft(int tabPosition)
    {
        collectionReorderer.increasePosition(getInternalTabs(), getInternalTabs().get(tabPosition));
        resequence();
    }

    public void moveFieldScreenTabRight(int tabPosition)
    {
        collectionReorderer.decreasePosition(getInternalTabs(), getInternalTabs().get(tabPosition));
        resequence();
    }

    public void resequence()
    {
        for (int i = 0; i < getInternalTabs().size(); i++)
        {
            FieldScreenTab fieldScreenTab = getInternalTabs().get(i);
            fieldScreenTab.setPosition(i);
        }
    }

    public boolean containsField(String fieldId)
    {
        return getFieldScreenLayoutItem(fieldId) != null;
    }

    public void removeFieldScreenLayoutItem(String fieldId)
    {
        FieldScreenLayoutItem fieldScreenLayoutItem = getFieldScreenLayoutItem(fieldId);
        if (fieldScreenLayoutItem != null)
        {
            fieldScreenLayoutItem.getFieldScreenTab().removeFieldScreenLayoutItem(fieldScreenLayoutItem.getPosition());
        }
        else
        {
            throw new IllegalArgumentException("Cannot find field screen layout item for field with id '" + fieldId + "'.");
        }
    }

    private FieldScreenLayoutItem getFieldScreenLayoutItem(String fieldId)
    {
        for (FieldScreenTab fieldScreenTab : getInternalTabs())
        {
            FieldScreenLayoutItem fieldScreenLayoutItem = fieldScreenTab.getFieldScreenLayoutItem(fieldId);
            if (fieldScreenLayoutItem != null)
            {
                return fieldScreenLayoutItem;
            }
        }

        return null;
    }

    public void store()
    {
        if (isModified())
        {
            if (getGenericValue() == null)
            {
                fieldScreenManager.createFieldScreen(this);
            }
            else
            {
                fieldScreenManager.updateFieldScreen(this);
                setModified(false);
            }
        }

        // See if the tabs collection has been initialised, and if so if we neew to persist any of the tabs
        // If the collection has not been initialised the tabs have not been modified
        if (tabs != null)
        {
            for (FieldScreenTab fieldScreenTab : getInternalTabs())
            {
                fieldScreenTab.store();
            }
        }
    }

    public void remove()
    {
        for (FieldScreenTab fieldScreenTab : getInternalTabs())
        {
            fieldScreenManager.removeFieldScreenLayoutItems(fieldScreenTab);
        }

        fieldScreenManager.removeFieldScreenTabs(this);
        fieldScreenManager.removeFieldScreen(id);
    }

    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof FieldScreenImpl)) return false;

        final FieldScreen fieldScreen = (FieldScreen) o;

        if (description != null ? !description.equals(fieldScreen.getDescription()) : fieldScreen.getDescription() != null) return false;
        if (id != null ? !id.equals(fieldScreen.getId()) : fieldScreen.getId() != null) return false;
        if (name != null ? !name.equals(fieldScreen.getName()) : fieldScreen.getName() != null) return false;

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (description != null ? description.hashCode() : 0);
        result = 29 * result + (name != null ? name.hashCode() : 0);
        result = 29 * result + (id != null ? id.hashCode() : 0);
        return result;
    }

    public String toString()
    {
        return "[FieldScreenImpl " + getName() + "]";
    }
}
