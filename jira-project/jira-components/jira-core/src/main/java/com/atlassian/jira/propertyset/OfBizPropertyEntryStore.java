package com.atlassian.jira.propertyset;

import java.util.Collection;
import java.util.List;

import com.opensymphony.module.propertyset.PropertySet;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

/**
 * Cached loader for property set entries and their values.  Note that this class must be used on any access to
 * the property set to ensure cache consistency; accessing one directly using {@code OFBizPropertySet} will
 * result in invalid cached state.
 *
 * @since v6.2
 */
public interface OfBizPropertyEntryStore
{
    /**
     * Retrieve all keys that are stored in the given property set.
     *
     * @param entityName the entity name of the property set's owner
     * @param entityId the entity ID of the property set's owner
     * @return all of the keys that are stored for this property set, in no particular order
     */
    Collection<String> getKeys(String entityName, long entityId);

    /**
     * Retrieve all keys that are stored in the given property set and have the specified value type.
     *
     * @param entityName the entity name of the property set's owner
     * @param entityId the entity ID of the property set's owner
     * @param type the value type, as defined by the value constants in the {@link PropertySet} interface,
     *          that the keys must have to be returned
     * @return all of the keys that are stored for this property set and have the given type, in no particular order
     */
    Collection<String> getKeys(String entityName, long entityId, int type);

    /**
     * Retrieve the type and value of the specified property.
     *
     * @param entityName the entity name of the property set's owner
     * @param entityId the entity ID of the property set's owner
     * @param propertyKey the name of the property to load
     * @return the property entry, or {@code null} if the property does not exist.  Note that even if it does "exist",
     *      the {@link PropertyEntry#getValue() value} can itself be {@code null}.
     */
    @CheckForNull
    PropertyEntry getEntry(String entityName, long entityId, String propertyKey);

    /**
     * Stores a new value for the specified property.
     *
     * @param entityName the entity name of the property set's owner
     * @param entityId the entity ID of the property set's owner
     * @param propertyKey the name of the property to store
     * @param type the value type that is to be stored, as defined by the value constants in the
     *      {@link PropertySet} interface.
     * @param value the value to be stored
     */
    void setEntry(String entityName, long entityId, String propertyKey, int type, Object value);

    /**
     * Remove a specific property from a property set.
     *
     * @param entityName the entity name of the property set's owner
     * @param entityId the entity ID of the property set's owner
     * @param propertyKey the name of the property to remove
     */
    void removeEntry(String entityName, long entityId, String propertyKey);

    /**
     * Remove all properties that belong to a property set.
     *
     * @param entityName the entity name of the property set's owner
     * @param entityId the entity ID of the property set's owner
     */
    void removePropertySet(String entityName, long entityId);




    /**
     * Holds the type and value of a property.
     */
    interface PropertyEntry
    {
        /**
         * The type of the property set entry.  See the value-type constants defined in {@link PropertySet} for
         * valid values and their meanings.
         */
        int getType();

        /**
         * Returns a safe copy of the cached value of this property entry.
         */
        @Nullable
        Object getValue();

        /**
         * Returns a safe copy of the cached value of this property entry with the requested type as opposed to
         * its actual stored type.
         *
         * @param type the type mapping to perform on the value
         */
        @Nullable
        Object getValue(int type);
    }

}

