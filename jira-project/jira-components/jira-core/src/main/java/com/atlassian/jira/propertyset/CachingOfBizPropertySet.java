package com.atlassian.jira.propertyset;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.propertyset.OfBizPropertyEntryStore.PropertyEntry;
import com.atlassian.util.concurrent.Supplier;
import com.atlassian.util.concurrent.Suppliers;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.opensymphony.module.propertyset.AbstractPropertySet;
import com.opensymphony.module.propertyset.PropertyException;
import com.opensymphony.module.propertyset.PropertyImplementationException;
import com.opensymphony.module.propertyset.PropertySet;

import static com.atlassian.jira.propertyset.PropertySetEntity.ENTITY_ID;
import static com.atlassian.jira.propertyset.PropertySetEntity.ENTITY_NAME;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;

/**
 * Reimplementation of {@code OFBizPropertySet} that caches property entries and values to provide
 * more efficient access.
 * <p>
 * Note that {@code JiraCachingPropertySet} is just a decorator around an arbitrary supplied
 * property set and thus has uncertain behaviour both in a clustered environment and when there
 * are multiple instances of the caching property set, as the individual caches have no way to
 * share state.
 * </p>
 * <p>
 * {@link CachingOfBizPropertySet} is different.  It is a flyweight around the
 * {@link OfBizPropertyEntryStore}, which caches the operations that it can (such as the
 * existence, type, and value of an entry) and delegates those it can't cache to the database
 * directly.  The implementation deviates from that of {@code OFBizPropertySet} only in ways
 * that make it more fault-tolerant, such as by not throwing an exception when removing a
 * nonexistent property and not throwing an exception when setting a property to a new type
 * without removing it first.
 * </p>
 * <p>
 * The only known disadvantage to using this class is that it will eagerly load the value on
 * a cache miss.  This seems to be an acceptable loss, because the main reasons why code
 * would bother to call {@link #exists(String)} or {@link #getType(String)} seem to be
 * </p>
 * <ol>
 * <li>To know whether or not the property exists and/or the appropriate getter type to use,
 *     both of which suggest that the value will eventually be retrieved anyway and that
 *     loading it eagerly is not wasteful.</li>
 * <li>To guard against the exception that {@code OFBizPropertySet} throws if you attempt
 *     to remove a property that does not exist of change the type of an existing property.
 *     This implementation silently ignores a spurious remove and gracefully updates the type
 *     if it is changed, so these guards are not necessary.</li>
 * <li>To use the existence or absence of the key as a {@code boolean} value, where the actual
 *     stored value is a meaningless sentinel.  This is a strange thing to do, and it makes
 *     more sense to store an actual {@code boolean} value for the property, instead.</li>
 * </ol>
 * <p>
 * This class is <em>thread-safe</em>, in spite of the difficulties that the {@code PropertySet}
 * contract imposes upon it.  Its only mutable state is the entity definition provided through
 * {@link PropertySet#init(Map,Map)}.  This is stored in an immutable holder class whose reference
 * is marked as {@code volatile} to ensure safe publishing.
 * </p>
 * <p>
 * Although {@code OFBizPropertySet} itself allows for multiple generic delegators to be used,
 * this implementation ignores this setting.  JIRA always uses the {@code "default"} delegator.
 * </p>
 *
 * @since v6.2
 */
public class CachingOfBizPropertySet extends AbstractPropertySet implements Serializable
{
    private static final long serialVersionUID = -5171154172922195519L;

    private transient Supplier<OfBizPropertyEntryStore> entryStoreRef;
    private volatile Entity entity;

    @SuppressWarnings("UnusedDeclaration")  // Used by PropertySetManager
    public CachingOfBizPropertySet()
    {
        this.entryStoreRef = ComponentAccessor.getComponentReference(OfBizPropertyEntryStore.class);
        // this.entity to be initialized by init(Map,Map)
    }

    CachingOfBizPropertySet(final OfBizPropertyEntryStore entryStore, final String entityName, final Long entityId)
    {
        this.entryStoreRef = Suppliers.memoize(entryStore);
        this.entity = new Entity(entityName, entityId);
    }


    public List<String> getKeys()
    {
        final Entity entity = this.entity;  // volatile read
        return sorted(getEntryStore().getKeys(entity.entityName, entity.entityId));
    }

    public List<String> getKeys(final String prefix)
    {
        final Entity entity = this.entity;  // volatile read
        return sorted(filter(getEntryStore().getKeys(entity.entityName, entity.entityId), new StartsWith(prefix)));
    }

    public List<String> getKeys(int type)
    {
        final Entity entity = this.entity;  // volatile read
        return sorted(getEntryStore().getKeys(entity.entityName, entity.entityId, type));
    }

    public List<String> getKeys(String prefix, int type)
    {
        final Entity entity = this.entity;  // volatile read
        return sorted(filter(getEntryStore().getKeys(entity.entityName, entity.entityId, type), new StartsWith(prefix)));
    }

    private static List<String> sorted(final Iterable<String> keys)
    {
        final List<String> sortedKeys = newArrayList(keys);
        Collections.sort(sortedKeys);
        return sortedKeys;
    }




    public int getType(String key) throws PropertyException
    {
        final Entity entity = this.entity;  // volatile read
        final PropertyEntry entry = getEntryStore().getEntry(entity.entityName, entity.entityId, key);
        if (entry == null)
        {
            throw new PropertyImplementationException("Property '" + key + "' not found");
        }
        return entry.getType();
    }

    public boolean exists(String key) throws PropertyException
    {
        final Entity entity = this.entity;  // volatile read
        return getEntryStore().getEntry(entity.entityName, entity.entityId, key) != null;
    }

    @SuppressWarnings("rawtypes")  // Forced by interface definition
    public void init(Map config, Map args)
    {
        final String entityName = (String)args.get(ENTITY_NAME);
        final Long entityId = (Long)args.get(ENTITY_ID);

        // volatile write
        this.entity = new Entity(entityName, entityId);
    }

    public void remove() throws PropertyException
    {
        final Entity entity = this.entity;  // volatile read
        getEntryStore().removePropertySet(entity.entityName, entity.entityId);
    }

    /**
     * Implementation note: Unlike {@code OFBizPropertySet}, this implementation will <em>not</em> throw
     * an exception if the property does not exist.
     *
     * @param key the key of the property to be removed
     * @throws PropertyException if the attempt to remove an existing property fails at the persistence layer
     */
    public void remove(String key)
    {
        final Entity entity = this.entity;  // volatile read
        getEntryStore().removeEntry(entity.entityName, entity.entityId, key);
    }

    public boolean supportsType(int type)
    {
        return true;
    }

    protected void setImpl(int type, String key, Object obj)
    {
        final Entity entity = this.entity;  // volatile read
        getEntryStore().setEntry(entity.entityName, entity.entityId, key, type, obj);
    }

    @Nullable
    protected Object get(int type, String key)
    {
        final Entity entity = this.entity;  // volatile read
        final PropertyEntry entry = getEntryStore().getEntry(entity.entityName, entity.entityId, key);
        return (entry != null) ? entry.getValue(type) : null;
    }

    @VisibleForTesting
    OfBizPropertyEntryStore getEntryStore()
    {
        return entryStoreRef.get();
    }



    @SuppressWarnings("NonFinalFieldReferenceInEquals")  // Forced by two-stage construction
    @Override
    public boolean equals(final Object o)
    {
        return o instanceof CachingOfBizPropertySet
                && Objects.equal(entity, ((CachingOfBizPropertySet)o).entity);
    }

    @SuppressWarnings("NonFinalFieldReferencedInHashCode")  // Forced by two-stage construction
    @Override
    public int hashCode()
    {
        final Entity entity = this.entity;  // volatile read
        return (entity != null) ? entity.hashCode() : 0;
    }

    private void writeObject(ObjectOutputStream out) throws IOException
    {
        out.writeObject(entity);
    }

    @SuppressWarnings("CastToConcreteClass")  // Well, that's how serialization works...
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        entryStoreRef = ComponentAccessor.getComponentReference(OfBizPropertyEntryStore.class);
        entity = (Entity)in.readObject();
    }



    @SuppressWarnings("SerializableHasSerializationMethods")  // Unnecessary
    static class Entity implements Serializable
    {
        private static final long serialVersionUID = -9043112541417402302L;

        final String entityName;
        final long entityId;

        Entity(final String entityName, final Long entityId)
        {
            this.entityName = notNull("entityName", entityName);
            this.entityId = notNull("entityId", entityId);
        }

        @Override
        public boolean equals(final Object o)
        {
            return o instanceof Entity && equals((Entity)o);
        }

        private boolean equals(final Entity other)
        {
            return entityId == other.entityId
                    && entityName.equals(other.entityName);
        }

        @Override
        public int hashCode()
        {
            return 31 * entityName.hashCode() + (int)(entityId ^ (entityId >>> 32));
        }
    }


    static class StartsWith implements Predicate<String>
    {
        private final String prefix;

        StartsWith(String prefix)
        {
            this.prefix = (prefix != null) ? prefix : "";
        }

        @Override
        public boolean apply(@Nullable String s)
        {
            return s != null && s.startsWith(prefix);
        }
    }
}
