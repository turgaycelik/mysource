
package com.atlassian.jira.propertyset;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheException;
import com.atlassian.cache.CacheLoader;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CacheSettingsBuilder;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.propertyset.OfBizPropertyTypeRegistry.TypeMapper;
import com.atlassian.jira.util.map.CacheObject;

import com.google.common.collect.ImmutableSet;
import com.opensymphony.module.propertyset.PropertyImplementationException;

import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityFieldMap;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.propertyset.OfBizPropertyTypeRegistry.mapper;
import static com.atlassian.jira.propertyset.PropertySetEntity.ENTITY_ID;
import static com.atlassian.jira.propertyset.PropertySetEntity.ENTITY_NAME;
import static com.atlassian.jira.propertyset.PropertySetEntity.ID;
import static com.atlassian.jira.propertyset.PropertySetEntity.PROPERTY_ENTRY;
import static com.atlassian.jira.propertyset.PropertySetEntity.PROPERTY_KEY;
import static com.atlassian.jira.propertyset.PropertySetEntity.SELECT_ID_AND_TYPE;
import static com.atlassian.jira.propertyset.PropertySetEntity.SELECT_ID_KEY_AND_TYPE;
import static com.atlassian.jira.propertyset.PropertySetEntity.SELECT_KEY;
import static com.atlassian.jira.propertyset.PropertySetEntity.TYPE;
import static com.atlassian.jira.propertyset.PropertySetEntity.VALUE;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static java.util.Arrays.asList;
import static org.ofbiz.core.entity.EntityOperator.AND;

/**
 * @since v6.2
 */
@EventComponent
public class CachingOfBizPropertyEntryStore implements OfBizPropertyEntryStore
{
    private final DelegatorInterface genericDelegator;

    final Cache<CacheKey,CacheObject<PropertyEntry>> entries;

    public CachingOfBizPropertyEntryStore(final DelegatorInterface genericDelegator, final CacheManager cacheManager)
    {
        this.genericDelegator = notNull("genericDelegator", genericDelegator);
        this.entries = cacheManager.getCache(
                CachingOfBizPropertyEntryStore.class.getName() + ".entries",
                new EntryLoader(),
                new CacheSettingsBuilder()
                        .expireAfterAccess(30, TimeUnit.MINUTES)
                        .build());
    }

    @Override
    public Collection<String> getKeys(final String entityName, final long entityId)
    {
        return getKeys(FieldMap.build(
                ENTITY_NAME, entityName,
                ENTITY_ID, entityId));
    }

    @Override
    public Collection<String> getKeys(final String entityName, final long entityId, final int type)
    {
        return getKeys(FieldMap.build(
                ENTITY_NAME, entityName,
                ENTITY_ID, entityId,
                TYPE, type));
    }

    private Collection<String> getKeys(final FieldMap fieldMap)
    {
        try
        {
            final EntityCondition condition = new EntityFieldMap(fieldMap, AND);
            final List<GenericValue> keyGVs = genericDelegator.findByCondition(PROPERTY_ENTRY, condition, SELECT_KEY, null);
            if (keyGVs == null || keyGVs.isEmpty())
            {
                return ImmutableSet.of();
            }
            final Set<String> keys = new HashSet<String>(keyGVs.size());
            for (GenericValue keyGV : keyGVs)
            {
                keys.add(keyGV.getString(PROPERTY_KEY));
            }
            return keys;
        }
        catch (GenericEntityException gee)
        {
            throw new PropertyImplementationException(gee);
        }
    }

    @Override
    public PropertyEntry getEntry(String entityName, long entityId, String propertyKey)
    {
        try
        {
            return entries.get(new CacheKey(entityName, entityId, propertyKey)).getValue();
        }
        catch (CacheException ex)
        {
            final Throwable cause = ex.getCause();
            if (cause instanceof RuntimeException)
            {
                throw (RuntimeException)cause;
            }
            throw new PropertyImplementationException((cause != null) ? cause : ex);
        }
    }

    @Override
    public void setEntry(final String entityName, final long entityId, final String propertyKey, final int type, final Object value)
    {
        final CacheKey cacheKey = new CacheKey(entityName, entityId, propertyKey);
        try
        {
            setEntryImpl(cacheKey, type, value);
        }
        catch (GenericEntityException gee)
        {
            throw new PropertyImplementationException(gee);
        }
        finally
        {
            // remove cache entry regardless of whether or not we succeed
            entries.remove(cacheKey);
        }
    }

    @Override
    public void removeEntry(String entityName, long entityId, String propertyKey)
    {
        final EntityCondition condition = new EntityFieldMap(FieldMap.build(
                ENTITY_NAME, entityName,
                ENTITY_ID, entityId,
                PROPERTY_KEY, propertyKey),
                AND);

        try
        {
            final List<GenericValue> list = genericDelegator.findByCondition(PROPERTY_ENTRY, condition, SELECT_ID_AND_TYPE, null);
            if (list != null)
            {
                for (GenericValue entry : list)
                {
                    removeEntryAndValue(entry);
                }
            }
        }
        catch (GenericEntityException gee)
        {
            throw new PropertyImplementationException(gee);
        }
        finally
        {
            // remove cache entry regardless of whether or not we actually find anything
            invalidateCacheEntry(entityName, entityId, propertyKey);
        }
    }

    /**
     * Removes an entire property set.
     * <p>
     * <strong>Implementation note</strong>: This is a "hard" operation to optimize due to the way the
     * tables are structured, OfBiz's poor support for joins and sub-selects, the cache's inability to
     * replicate the invalidation of a group of keys, and the probable performance cost of flushing the
     * entire entry cache.  We will make these assumptions:
     * </p>
     * <ol>
     * <li>It is rare to delete an entire property set that is also large, so going row-at-a-time is acceptable.</li>
     * <li>Another cluster node that explicitly removes a row from the property set will itself be responsible for
     *          invalidating that row's entry.</li>
     * <li>Exhaustively searching for matching keys is both expensive and unreliable (if we can have stale rows, then
     *          so can other nodes have different stale rows that we don't even know to invalidate for them).</li>
     * <li>The only real alternative would be to flush the entire cache.</li>
     * </ol>
     * <p>
     * Based on those assumptions, the decision is to invalidate only those keys for which we actually find and
     * remove an entry, exactly as if they have been removed individually.
     * </p>
     *
     * @param entityName the entity name of the property set's owner
     * @param entityId the entity ID of the property set's owner
     */
    @Override
    public void removePropertySet(String entityName, long entityId)
    {
        final EntityCondition condition = new EntityFieldMap(FieldMap.build(
                ENTITY_NAME, entityName,
                ENTITY_ID, entityId),
                AND);

        try
        {
            final List<GenericValue> list = genericDelegator.findByCondition(PROPERTY_ENTRY, condition, SELECT_ID_KEY_AND_TYPE, null);
            if (list != null && !list.isEmpty())
            {
                // We could try a separate delete for all of the entries at once after we've deleted the values, but
                // that would orphan data values that were "simultaneously" created under race conditions.  This is
                // no worse (and in some ways better) than what OFBizPropertySet does and it avoids that possibility.
                for (GenericValue entry : list)
                {
                    removeEntryAndValue(entry);
                    invalidateCacheEntry(entityName, entityId, entry.getString(PROPERTY_KEY));
                }
            }
        }
        catch (GenericEntityException gee)
        {
            throw new PropertyImplementationException(gee);
        }
    }




    private void setEntryImpl(CacheKey cacheKey, Integer type, Object unmappedValue) throws GenericEntityException
    {
        final GenericValue propertyEntry = makeUpdatedEntry(cacheKey, type);
        final TypeMapper mapper = mapper(type);
        final Object mappedValue = (unmappedValue != null) ? mapper.getHandler().processSet(type, unmappedValue) : null;
        final GenericValue propertyTypeEntry = genericDelegator.makeValue(mapper.getEntityName(), FieldMap.build(
                ID, propertyEntry.getLong(ID),
                VALUE, mappedValue));
        genericDelegator.storeAll(asList(propertyEntry, propertyTypeEntry));
    }

    private GenericValue makeUpdatedEntry(CacheKey cacheKey, Integer type) throws GenericEntityException
    {
        final FieldMap fieldMap = cacheKey.toFieldMap();
        final List<GenericValue> list = genericDelegator.findByAnd(PROPERTY_ENTRY, fieldMap);
        if (list == null || list.isEmpty())
        {
            final Long id = genericDelegator.getNextSeqId(PROPERTY_ENTRY);
            return genericDelegator.makeValue(PROPERTY_ENTRY, fieldMap
                    .add(ID, id)
                    .add(TYPE, type));
        }

        final GenericValue existingPropertyEntry = (list.size() == 1) ? list.get(0) : selectMaximumIdAndRemoveOthers(list);
        removeOrphanedValueIfTypeChanged(type, existingPropertyEntry);
        return existingPropertyEntry;
    }

    private void removeOrphanedValueIfTypeChanged(final Integer newType, final GenericValue existingPropertyEntry)
            throws GenericEntityException
    {
        final Integer oldType = existingPropertyEntry.getInteger(TYPE);
        if (oldType.equals(newType))
        {
            return;
        }

        final TypeMapper newMapper = mapper(newType);
        final TypeMapper oldMapper = mapper(oldType);
        if (!oldMapper.hasSameEntityName(newMapper))
        {
            genericDelegator.removeByAnd(oldMapper.getEntityName(), FieldMap.build(ID, existingPropertyEntry.getLong(ID)));
        }
        existingPropertyEntry.set(TYPE, newType);
        // Don't store; will be handled by transactional storeAll in setEntryImpl
    }

    private void removeEntryAndValue(GenericValue entry) throws GenericEntityException
    {
        removeEntryAndValue(entry.getLong(ID), entry.getInteger(TYPE));
    }

    private void removeEntryAndValue(Long id, Integer type) throws GenericEntityException
    {
        final FieldMap byId = FieldMap.build(ID, id);
        genericDelegator.removeByAnd(mapper(type).getEntityName(), byId);
        genericDelegator.removeByAnd(PROPERTY_ENTRY, byId);
    }

    private void invalidateCacheEntry(String entityName, long entityId, String propertyKey)
    {
        entries.remove(new CacheKey(entityName, entityId, propertyKey));
    }



    /**
     * Selects the entry from the list with the maximum value for ID, ignoring all other entries.
     * Assumptions: {@code list} is non-null and contains at least two valid entries.
     *
     * @param list the list to be scanned
     * @return the entry with the maximum value.
     */
    private static GenericValue selectMaximumId(@Nonnull List<GenericValue> list)
    {
        final Iterator<GenericValue> iter = list.iterator();
        GenericValue winner = iter.next();
        long winnerId = winner.getLong(ID);

        do
        {
            final GenericValue other = iter.next();
            final long otherId = other.getLong(ID);
            if (otherId > winnerId)
            {
                winner = other;
                winnerId = otherId;
            }
        }
        while (iter.hasNext());

        return winner;
    }

    /**
     * Selects the entry from the list with the maximum value for ID, removing the all other entries and their values.
     * Assumptions: {@code list} is non-null and contains at least two valid entries.
     *
     * @param list the list to be scanned
     * @return the entry with the maximum value.
     * @throws GenericEntityException if {@link #removeEntryAndValue(GenericValue)} does
     */
    private GenericValue selectMaximumIdAndRemoveOthers(@Nonnull List<GenericValue> list) throws GenericEntityException
    {
        final GenericValue winner = selectMaximumId(list);
        for (GenericValue other : list)
        {
            // The reference to "winner" was selected from this list, so "!=" is sane
            //noinspection ObjectEquality
            if (other != winner)
            {
                removeEntryAndValue(other);
            }
        }
        return winner;
    }



    @EventListener
    @SuppressWarnings("UnusedParameters")
    public void onClearCache(ClearCacheEvent event)
    {
        entries.removeAll();
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder(1024).append(getClass().getSimpleName()).append("[entries={");
        final Collection<CacheKey> keys = entries.getKeys();
        if (!keys.isEmpty())
        {
            for (CacheKey key : keys)
            {
                sb.append("\n\t").append(key).append(" => ").append(entries.get(key)).append(',');
            }

            // Change the last comma to a newline
            sb.setCharAt(sb.length() - 1, '\n');
        }
        return sb.append("}]").toString();
    }



    @SuppressWarnings("SerializableHasSerializationMethods")
    static class CacheKey implements Serializable
    {
        private static final long serialVersionUID = 2594159095924513835L;

        private final String entityName;
        private final long entityId;
        private final String propertyKey;

        CacheKey(String entityName, long entityId, String propertyKey)
        {
            this.entityName = entityName;
            this.entityId = entityId;
            this.propertyKey = propertyKey;
        }

        String getEntityName()
        {
            return entityName;
        }

        long getEntityId()
        {
            return entityId;
        }

        String getPropertyKey()
        {
            return propertyKey;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            final CacheKey other = (CacheKey)o;
            return entityId == other.entityId
                    && entityName.equals(other.entityName)
                    && propertyKey.equals(other.propertyKey);
        }

        @Override
        public int hashCode()
        {
            int result = entityName.hashCode();
            result = 31 * result + (int)(entityId ^ (entityId >>> 32));
            result = 31 * result + propertyKey.hashCode();
            return result;
        }

        @Override
        public String toString()
        {
            return "CacheKey[" +
                    "entityName=" + entityName +
                    ",entityId=" + entityId +
                    ",propertyKey=" + propertyKey +
                    ']';
        }

        public FieldMap toFieldMap()
        {
            return FieldMap.build(
                    ENTITY_NAME, entityName,
                    ENTITY_ID, entityId,
                    PROPERTY_KEY, propertyKey);
        }

        public EntityFieldMap toCondition()
        {
            return new EntityFieldMap(toFieldMap(), AND);
        }
    }



    static class PropertyEntryImpl implements PropertyEntry
    {
        private final int type;
        private final Object value;

        PropertyEntryImpl(int type, Object value)
        {
            this.type = type;
            this.value = value;
        }

        public int getType()
        {
            return type;
        }

        @Nullable
        public Object getValue()
        {
            if (value == null)
            {
                return null;
            }
            return mapper(type).getHandler().processGet(type, value);
        }

        @Nullable
        public Object getValue(final int type)
        {
            if (value == null)
            {
                return null;
            }
            return mapper(type).getHandler().processGet(type, value);
        }

        @Override
        public String toString()
        {
            return "PropertyEntryImpl[type=" + type + ",value=" + value + ']';
        }
    }



    class EntryLoader implements CacheLoader<CacheKey,CacheObject<PropertyEntry>>
    {
        @Override
        public CacheObject<PropertyEntry> load(@Nonnull CacheKey cacheKey)
        {
            try
            {
                final List<GenericValue> list = genericDelegator.findByCondition(PROPERTY_ENTRY, cacheKey.toCondition(), SELECT_ID_AND_TYPE, null);
                if (list == null || list.isEmpty())
                {
                    return CacheObject.NULL();
                }
                final GenericValue entry = (list.size() == 1) ? list.get(0) : selectMaximumId(list);
                return buildPropertyEntry(entry);
            }
            catch (GenericEntityException gee)
            {
                throw new PropertyImplementationException(gee);
            }
        }

        private CacheObject<PropertyEntry> buildPropertyEntry(final GenericValue entry) throws GenericEntityException
        {
            final Long id = entry.getLong(ID);
            final Integer type = entry.getInteger(TYPE);
            final GenericValue genericValue = genericDelegator.findByPrimaryKey(mapper(type).getEntityName(), FieldMap.build(ID, id));
            final Object value = (genericValue != null) ? genericValue.get(VALUE) : null;
            return CacheObject.<PropertyEntry>wrap(new PropertyEntryImpl(type, value));
        }
    }

}
