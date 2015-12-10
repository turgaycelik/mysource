package com.atlassian.jira.issue.fields.screen;

import com.atlassian.jira.issue.operation.IssueOperation;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public class FieldScreenSchemeImpl extends AbstractGVBean implements FieldScreenScheme
{
    private final FieldScreenSchemeManager fieldScreenSchemeManager;

    private Long id;
    private String name;
    private String description;

    /**
     * lazy-load schemeItems map wrapper
     */
    private SchemeItems schemeItems = new SchemeItems();

    public FieldScreenSchemeImpl(FieldScreenSchemeManager fieldScreenSchemeManager, GenericValue genericValue)
    {
        this.fieldScreenSchemeManager = fieldScreenSchemeManager;
        setGenericValue(genericValue);
    }

    protected void init()
    {
        if (getGenericValue() != null)
        {
            id = getGenericValue().getLong("id");
            name = getGenericValue().getString("name");
            description = getGenericValue().getString("description");
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
            throw new IllegalStateException("Cannot set id of an exsiting entity.");

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

    public Collection<FieldScreenSchemeItem> getFieldScreenSchemeItems()
    {
        List<FieldScreenSchemeItem> entities = new ArrayList<FieldScreenSchemeItem>(getInternalSchemeItems().values());
        Collections.sort(entities);
        return Collections.unmodifiableCollection(entities);
    }

    /**
     * Returns a synchronized map of scheme items
     *
     * @return a synchronized map of scheme items
     */
    protected Map<IssueOperation, FieldScreenSchemeItem> getInternalSchemeItems()
    {
        if (!schemeItems.isInitialized())
        {
            schemeItems.initialize();
        }

        return schemeItems.getSchemeItemsMap();
    }

    public FieldScreenSchemeItem getFieldScreenSchemeItem(IssueOperation issueOperation)
    {
        return getInternalSchemeItems().get(issueOperation);
    }

    public void addFieldScreenSchemeItem(FieldScreenSchemeItem fieldScreenSchemeItem)
    {
        fieldScreenSchemeItem.setFieldScreenScheme(this);
        getInternalSchemeItems().put(fieldScreenSchemeItem.getIssueOperation(), fieldScreenSchemeItem);
        store();
    }

    public FieldScreenSchemeItem removeFieldScreenSchemeItem(IssueOperation issueOperation)
    {
        FieldScreenSchemeItem fieldScreenSchemeItem = getInternalSchemeItems().remove(issueOperation);
        if (fieldScreenSchemeItem != null)
        {
            fieldScreenSchemeItem.remove();
        }

        return fieldScreenSchemeItem;
    }

    public FieldScreen getFieldScreen(IssueOperation issueOperation)
    {
        FieldScreenSchemeItem fieldScreenSchemeItem = getFieldScreenSchemeItem(issueOperation);
        if (fieldScreenSchemeItem == null)
        {
            // If there is no entry for this use the default mapping
            fieldScreenSchemeItem = getFieldScreenSchemeItem(null);
            if (fieldScreenSchemeItem != null)
            {
                return fieldScreenSchemeItem.getFieldScreen();
            }
            else
            {
                throw new IllegalArgumentException("Cannot determine field screen for operation '" + (issueOperation == null ? "Default Operation" : issueOperation.getNameKey()) + "'.");
            }
        }

        return fieldScreenSchemeItem.getFieldScreen();
    }

    public void store()
    {
        if (isModified())
        {
            if (getGenericValue() == null)
            {
                fieldScreenSchemeManager.createFieldScreenScheme(this);
            }
            else
            {
                fieldScreenSchemeManager.updateFieldScreenScheme(this);
            }
        }

        // If the scheme items are loaded, see if they need to be stored
        if (schemeItems.isInitialized())
        {
            final Map<IssueOperation, FieldScreenSchemeItem> internalSchemeItems = getInternalSchemeItems();
            // Synchronize access to the map to ensure thread-safety
            synchronized (internalSchemeItems)
            {
                for (FieldScreenSchemeItem fieldScreenSchemeItem : internalSchemeItems.values())
                {
                    fieldScreenSchemeItem.store();
                }
            }
        }
    }

    public void remove()
    {
        if (id != null)
        {
            fieldScreenSchemeManager.removeFieldSchemeItems(this);
            fieldScreenSchemeManager.removeFieldScreenScheme(this);
        }
    }

    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof FieldScreenScheme)) return false;

        final FieldScreenScheme fieldScreenScheme = (FieldScreenScheme) o;

        return safeEquals(description, fieldScreenScheme.getDescription())
                && safeEquals(id, fieldScreenScheme.getId())
                && safeEquals(name, fieldScreenScheme.getName());
    }

    /**
     * Compares two given objects by calling o1.equals(o2). This method is null safe - checks for nulls.
     *
     * @param o1 object to compare
     * @param o2 object to compare
     * @return true if both objects are null or equal, false otherwise
     */
    private boolean safeEquals(Object o1, Object o2)
    {
        return o1 == null ? o2 == null : o1.equals(o2);
    }

    public int hashCode()
    {
        int result;
        result = (id != null ? id.hashCode() : 0);
        result = 29 * result + (name != null ? name.hashCode() : 0);
        result = 29 * result + (description != null ? description.hashCode() : 0);
        return result;
    }

    /**
     * This class provides the ability to synchronize the initialization of the scheme items map.
     * <p/>
     * The initialize method is synchronized on THIS OBJECT - so initialization and initialization checks should be
     * synchronized.
     */
    private class SchemeItems
    {
        protected boolean initialized = false;

        protected Map<IssueOperation, FieldScreenSchemeItem> schemeItems;

        public synchronized boolean isInitialized()
        {
            return initialized;
        }

        /**
         * Initialize the scheme items map - synchronized on THIS OBJECT
         */
        public synchronized void initialize()
        {
            schemeItems = Collections.synchronizedMap(new HashMap<IssueOperation, FieldScreenSchemeItem>());
            Collection<FieldScreenSchemeItem> fieldScreenSchemeItems = fieldScreenSchemeManager.getFieldScreenSchemeItems(FieldScreenSchemeImpl.this);
            for (FieldScreenSchemeItem fieldScreenSchemeItem : fieldScreenSchemeItems)
            {
                schemeItems.put(fieldScreenSchemeItem.getIssueOperation(), fieldScreenSchemeItem);
            }
            initialized = true;
        }

        public Map<IssueOperation, FieldScreenSchemeItem> getSchemeItemsMap()
        {
            return schemeItems;
        }
    }
}
