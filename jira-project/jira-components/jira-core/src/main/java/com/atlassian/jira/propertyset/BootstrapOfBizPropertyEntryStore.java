package com.atlassian.jira.propertyset;

import java.util.Collection;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.ofbiz.FieldMap;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.opensymphony.module.propertyset.PropertyImplementationException;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;

import org.w3c.dom.Document;

import static com.atlassian.jira.propertyset.PropertySetEntity.ENTITY_ID;
import static com.atlassian.jira.propertyset.PropertySetEntity.ENTITY_NAME;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Used during bootstrap, only.  This simply delegates to "ofbiz" + "cached" PropertySet
 * combinations instead of implementing the low-level database access that "ofbiz-cached"
 * PropertySet would normally get.
 *
 * @since v6.2
 */
public class BootstrapOfBizPropertyEntryStore implements OfBizPropertyEntryStore
{
    @ClusterSafe("Only used during bootstrap; this component is discarded afterwards")
    private final Cache<CacheKey,PropertySet> propertySets = CacheBuilder.newBuilder()
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .build(new PropertySetLoader());

    @SuppressWarnings("unchecked")
    @Override
    public Collection<String> getKeys(final String entityName, final long entityId)
    {
        return getPropertySet(entityName, entityId).getKeys();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<String> getKeys(final String entityName, final long entityId, final int type)
    {
        return getPropertySet(entityName, entityId).getKeys(type);
    }

    @Nullable
    @Override
    public PropertyEntry getEntry(final String entityName, final long entityId, final String propertyKey)
    {
        final PropertySet ps = getPropertySet(entityName, entityId);
        return ps.exists(propertyKey) ? new PropertyEntryImpl(ps, propertyKey) : null;
    }

    @Override
    public void setEntry(final String entityName, final long entityId, final String propertyKey, final int type, final Object value)
    {
        final PropertySet ps = getPropertySet(entityName, entityId);
        switch (type)
        {
            case PropertySet.BOOLEAN:
                ps.setBoolean(propertyKey, (Boolean)value);
                return;
            case PropertySet.DATE:
                ps.setDate(propertyKey, (Date)value);
                return;
            case PropertySet.DATA:
                ps.setData(propertyKey, (byte[])value);
                return;
            case PropertySet.LONG:
                ps.setLong(propertyKey, (Long)value);
                return;
            case PropertySet.INT:
                ps.setInt(propertyKey, (Integer)value);
                return;
            case PropertySet.DOUBLE:
                ps.setDouble(propertyKey, (Double) value);
                return;
            case PropertySet.OBJECT:
                ps.setObject(propertyKey, value);
                return;
            case PropertySet.XML:
                ps.setXML(propertyKey, (Document)value);
                return;
            case PropertySet.PROPERTIES:
                ps.setProperties(propertyKey, (Properties)value);
                return;
            case PropertySet.STRING:
                ps.setString(propertyKey, (String)value);
                return;
            case PropertySet.TEXT:
                ps.setText(propertyKey, (String) value);
                return;
        }
        throw new PropertyImplementationException("Unrecognized property type: " + type);
    }

    @Override
    public void removeEntry(final String entityName, final long entityId, final String propertyKey)
    {
        final PropertySet ps = getPropertySet(entityName, entityId);
        if (ps.exists(propertyKey))
        {
            ps.remove(propertyKey);
        }
    }

    @Override
    public void removePropertySet(final String entityName, final long entityId)
    {
        getPropertySet(entityName, entityId).remove();
    }

    static class CacheKey
    {
        private final String entityName;
        private final long entityId;

        CacheKey(final String entityName, final long entityId)
        {
            this.entityName = notNull("entityName", entityName);
            this.entityId = entityId;
        }

        String getEntityName()
        {
            return entityName;
        }

        long getEntityId()
        {
            return entityId;
        }

        @Override
        public boolean equals(final Object o)
        {
            return o instanceof CacheKey && equals((CacheKey)o);
        }

        private boolean equals(@Nonnull final CacheKey other)
        {
            return entityId == other.entityId && entityName.equals(other.entityName);
        }

        @Override
        public int hashCode()
        {
            return 31 * entityName.hashCode() + (int)(entityId ^ (entityId >>> 32));
        }
    }

    private PropertySet getPropertySet(String entityName, long entityId)
    {
        return propertySets.getUnchecked(new CacheKey(entityName, entityId));
    }

    static class PropertySetLoader extends CacheLoader<CacheKey,PropertySet>
    {
        @Override
        public PropertySet load(@Nonnull final CacheKey key) throws Exception
        {
            final PropertySet uncached = PropertySetManager.getInstance("ofbiz", FieldMap.build(
                    ENTITY_NAME, key.getEntityName(),
                    ENTITY_ID, key.getEntityId() ));
            return PropertySetManager.getInstance("cached", FieldMap.build("PropertySet", uncached));
        }
    }

    static class PropertyEntryImpl implements PropertyEntry
    {
        private final PropertySet propertySet;
        private final String propertyKey;

        PropertyEntryImpl(final PropertySet propertySet, final String propertyKey)
        {
            this.propertySet = propertySet;
            this.propertyKey = propertyKey;
        }

        @Override
        public int getType()
        {
            return propertySet.getType(propertyKey);
        }

        @Nullable
        @Override
        public Object getValue()
        {
            return propertySet.getAsActualType(propertyKey);
        }

        @Nullable
        @Override
        public Object getValue(final int type)
        {
            switch (type)
            {
                case PropertySet.STRING:
                    return propertySet.getString(propertyKey);
                case PropertySet.TEXT:
                    return propertySet.getText(propertyKey);
                case PropertySet.LONG:
                    return propertySet.getLong(propertyKey);
                case PropertySet.INT:
                    return propertySet.getInt(propertyKey);
                case PropertySet.BOOLEAN:
                    return propertySet.getBoolean(propertyKey);
                case PropertySet.DATE:
                    return propertySet.getDate(propertyKey);
                case PropertySet.DOUBLE:
                    return propertySet.getDouble(propertyKey);
                case PropertySet.PROPERTIES:
                    return propertySet.getProperties(propertyKey);
                case PropertySet.DATA:
                    return propertySet.getData(propertyKey);
                case PropertySet.XML:
                    return propertySet.getXML(propertyKey);
                case PropertySet.OBJECT:
                    return propertySet.getObject(propertyKey);
            }
            throw new PropertyImplementationException("Unsupported property type: " + type);
        }
    }
}

