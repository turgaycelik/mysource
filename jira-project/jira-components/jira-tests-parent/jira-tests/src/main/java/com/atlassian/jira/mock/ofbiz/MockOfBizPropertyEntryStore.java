package com.atlassian.jira.mock.ofbiz;

import java.util.Collection;
import java.util.Map;

import javax.annotation.Nullable;

import com.atlassian.jira.propertyset.OfBizPropertyEntryStore;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.google.common.collect.Maps.newHashMap;

/**
 * Mocks out the OfBizPropertyEntryStore so that you don't have to have a live GenericDelegator to use it.
 *
 * @since v6.1
 */
public class MockOfBizPropertyEntryStore implements OfBizPropertyEntryStore
{
    private final Map<EntityNameAndId,Map<String,PropertyEntry>> propertyMapForEntity = newHashMap();

    @Override
    public Collection<String> getKeys(final String entityName, final long entityId)
    {
        final Map<String,PropertyEntry> propertyMap = getPropertyMap(entityName, entityId);
        if (propertyMap == null)
        {
            return ImmutableSet.of();
        }
        return ImmutableSet.copyOf(propertyMap.keySet());
    }

    @Override
    public Collection<String> getKeys(final String entityName, final long entityId, final int type)
    {
        final Map<String,PropertyEntry> propertyMap = getPropertyMap(entityName, entityId);
        if (propertyMap == null)
        {
            return ImmutableSet.of();
        }
        return ImmutableSet.copyOf(Maps.filterValues(propertyMap, new Predicate<PropertyEntry>()
        {
            @Override
            public boolean apply(final PropertyEntry entry)
            {
                return entry.getType() == type;
            }
        }).keySet());
    }

    @Override
    public PropertyEntry getEntry(final String entityName, final long entityId, final String propertyKey)
    {
        return getPropertyMap(entityName, entityId)
                .get(propertyKey);
    }

    @Override
    public void setEntry(final String entityName, final long entityId, final String propertyKey, final int type, final Object value)
    {
        getPropertyMap(entityName, entityId)
                .put(propertyKey, new MockPropertyEntry(type, value));
    }

    @Override
    public void removeEntry(final String entityName, final long entityId, final String propertyKey)
    {
        getPropertyMap(entityName, entityId)
                .remove(propertyKey);
    }

    @Override
    public void removePropertySet(final String entityName, final long entityId)
    {
        propertyMapForEntity.remove(new EntityNameAndId(entityName, entityId));
    }

    private Map<String,PropertyEntry> getPropertyMap(final String entityName, final long entityId)
    {
        final EntityNameAndId entity = new EntityNameAndId(entityName, entityId);
        Map<String,PropertyEntry> propertyMap = propertyMapForEntity.get(entity);
        if (propertyMap == null)
        {
            propertyMap = newHashMap();
            propertyMapForEntity.put(entity, propertyMap);
        }
        return propertyMap;
    }



    static class EntityNameAndId
    {
        private final String entityName;
        private final Long entityId;

        EntityNameAndId(final String entityName, final long entityId)
        {
            this.entityName = notNull("entityName", entityName);
            this.entityId = notNull("entityId", entityId);
        }

        String getEntityName()
        {
            return entityName;
        }

        Long getEntityId()
        {
            return entityId;
        }

        @Override
        public boolean equals(final Object o)
        {
            return o instanceof EntityNameAndId && equals((EntityNameAndId)o);
        }

        private boolean equals(EntityNameAndId other)
        {
            return entityId.equals(other.entityId) && entityName.equals(other.entityName);
        }

        @Override
        public int hashCode()
        {
            return 31 * entityId.hashCode() + entityName.hashCode();
        }

        @Override
        public String toString()
        {
            return "EntityNameAndId[entityName=" + entityName + ",entityId=" + entityId + ']';
        }
    }



    static class MockPropertyEntry implements PropertyEntry
    {
        private final int type;
        private final Object value;

        MockPropertyEntry(final int type, final Object value)
        {
            this.type = type;
            this.value = value;
        }

        public int getType()
        {
            return type;
        }

        public Object getValue()
        {
            return value;
        }

        @Nullable
        @Override
        public Object getValue(final int type)
        {
            return value;
        }

        @Override
        public boolean equals(final Object o)
        {
            return o instanceof MockPropertyEntry && equals((MockPropertyEntry)o);
        }

        private boolean equals(MockPropertyEntry other)
        {
            return type == other.type && Objects.equal(value, other.value);
        }

        @Override
        public int hashCode()
        {
            return Objects.hashCode(type, value);
        }

        @Override
        public String toString()
        {
            return "MockPropertyEntry[type=" + type + ",value=" + value + ']';
        }
    }
}

