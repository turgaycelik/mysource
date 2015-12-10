package com.atlassian.jira.issue.fields.layout.field;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.issue.fields.screen.AbstractGVBean;

import org.ofbiz.core.entity.GenericValue;

/**
 * Thread safety is mostly achieved (there are still some issues with the {@link #setGenericValue(GenericValue)} method)
 * by two mechanisms.
 * <ol>
 * <li> all mutative operations to the map are done under a write-lock. For extra safety all stores/loads from the database
 * are also done under the lock, even though this is not ideal.
 * <li> iteration over the map are protected by a read-lock.
 */
public class FieldLayoutSchemeImpl extends AbstractGVBean implements FieldLayoutScheme
{
    private Long id;
    private String name;
    private String description;
    private final FieldLayoutManager fieldLayoutManager;

    private volatile Map<String, FieldLayoutSchemeEntity> schemeEntities;
    @ClusterSafe("Guards in-memory changes to the GenericValue")
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public FieldLayoutSchemeImpl(final FieldLayoutManager fieldLayoutManager, final GenericValue genericValue)
    {
        this.fieldLayoutManager = fieldLayoutManager;
        // Note that AbstractGVBean.setGenericValue(genericValue) calls init()
        setGenericValue(genericValue);
    }

    @Override
    protected void init()
    {
        lock.writeLock().lock();
        try
        {
            final GenericValue gv = getGenericValue();
            if (gv != null)
            {
                id = gv.getLong("id");
                name = gv.getString("name");
                description = gv.getString("description");
            }
            setModified(false);
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    public Long getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(final String name)
    {
        lock.writeLock().lock();
        try
        {
            this.name = name;
            updateGV("name", name);
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(final String description)
    {
        lock.writeLock().lock();
        try
        {
            this.description = description;
            updateGV("description", description);
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    public void store()
    {
        lock.writeLock().lock();
        try
        {
            if (getGenericValue() == null)
            {
                fieldLayoutManager.createFieldLayoutScheme(this);
            }
            else
            {
                fieldLayoutManager.updateFieldLayoutScheme(this);
            }
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    public Long getFieldLayoutId(final String issueTypeId)
    {
        // not atomic, hold the lock
        lock.writeLock().lock();
        try
        {
            FieldLayoutSchemeEntity fieldLayoutSchemeEntity = getInternalSchemeEntities().get(issueTypeId);
            if (fieldLayoutSchemeEntity == null)
            {
                // There is no specific entry for the given issueTypeId - use the default mapping
                fieldLayoutSchemeEntity = getInternalSchemeEntities().get(null);
            }
            return fieldLayoutSchemeEntity.getFieldLayoutId();
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    public FieldLayoutSchemeEntity getEntity(final String issueTypeId)
    {
        return getInternalSchemeEntities().get(issueTypeId);
    }

    public FieldLayoutSchemeEntity getEntity(final EditableFieldLayout editableFieldLayout)
    {
        // this getter does a write-lock, cannot do this under read lock
        final Collection<FieldLayoutSchemeEntity> values = getInternalSchemeEntities().values();
        lock.readLock().lock();
        try
        {
            for (final FieldLayoutSchemeEntity fieldLayoutSchemeEntity : values)
            {
                if (editableFieldLayout.getType() == null)
                {
                    if (editableFieldLayout.getId().equals(fieldLayoutSchemeEntity.getFieldLayoutId()))
                    {
                        return fieldLayoutSchemeEntity;
                    }
                }
                else
                {
                    // If the passed in editableFieldLayout is a default field layout - do not compare ids, as the
                    // default field layout has id of null. So check for null.
                    if (fieldLayoutSchemeEntity.getFieldLayoutId() == null)
                    {
                        return fieldLayoutSchemeEntity;
                    }
                }
            }
        }
        finally
        {
            lock.readLock().unlock();
        }

        return null;
    }

    public Collection<GenericValue> getProjects()
    {
        return fieldLayoutManager.getProjects(this);
    }

    public boolean containsEntity(final String issueTypeId)
    {
        return getInternalSchemeEntities().containsKey(issueTypeId);
    }

    private Map<String, FieldLayoutSchemeEntity> getInternalSchemeEntities()
    {
        lock.readLock().lock();
        try
        {
            if (schemeEntities != null)
            {
                return schemeEntities;
            }
        }
        finally
        {
            lock.readLock().unlock();
        }

        lock.writeLock().lock();
        try
        {
            if (schemeEntities == null)
            {
                schemeEntities = new HashMap<String, FieldLayoutSchemeEntity>();
                final Collection<FieldLayoutSchemeEntity> fieldLayoutSchemeEntities = fieldLayoutManager.getFieldLayoutSchemeEntities(this);
                for (final FieldLayoutSchemeEntity fieldLayoutSchemeEntity : fieldLayoutSchemeEntities)
                {
                    recordEntity(fieldLayoutSchemeEntity, schemeEntities);
                }
            }
        }
        finally
        {
            lock.writeLock().unlock();
        }

        return schemeEntities;
    }

    public void addEntity(final FieldLayoutSchemeEntity fieldLayoutSchemeEntity)
    {
        lock.writeLock().lock();
        try
        {
            fieldLayoutManager.createFieldLayoutSchemeEntity(this,
                    fieldLayoutSchemeEntity.getIssueTypeId(), fieldLayoutSchemeEntity.getFieldLayoutId());
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    public void removeEntity(final String issueTypeId)
    {
        lock.writeLock().lock();
        try
        {
            if (containsEntity(issueTypeId))
            {
                fieldLayoutManager.removeFieldLayoutSchemeEntity(getInternalSchemeEntities().get(issueTypeId));
            }
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    public Collection<FieldLayoutSchemeEntity> getEntities()
    {
        final List<FieldLayoutSchemeEntity> entities = new LinkedList<FieldLayoutSchemeEntity>(getInternalSchemeEntities().values());
        Collections.sort(entities);
        return Collections.unmodifiableCollection(entities);
    }

    public void remove()
    {
        if (getGenericValue() != null)
        {
            lock.writeLock().lock();
            try
            {
                fieldLayoutManager.removeFieldLayoutScheme(this);
            }
            finally
            {
                lock.writeLock().unlock();
            }
        }
    }

    protected void cacheEntity(final FieldLayoutSchemeEntity fieldLayoutSchemeEntity)
    {
        recordEntity(fieldLayoutSchemeEntity, getInternalSchemeEntities());
    }

    protected void flushEntity(final FieldLayoutSchemeEntity fieldLayoutSchemeEntity)
    {
        getInternalSchemeEntities().remove(fieldLayoutSchemeEntity.getIssueTypeId());
    }

    private void recordEntity(final FieldLayoutSchemeEntity fieldLayoutSchemeEntity, final Map<String, FieldLayoutSchemeEntity> entities)
    {
        final String issueTypeId = fieldLayoutSchemeEntity.getIssueTypeId();
        entities.put(issueTypeId, fieldLayoutSchemeEntity);
    }
}
