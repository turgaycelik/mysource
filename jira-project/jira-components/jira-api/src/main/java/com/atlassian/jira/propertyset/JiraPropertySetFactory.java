package com.atlassian.jira.propertyset;

import javax.annotation.Nonnull;

import com.opensymphony.module.propertyset.PropertySet;

/**
 * Provides a number of utility methods to retrieve a property set from JIRA.
 * Please note that this class's sole responsibility is to create a property set.
 * It does not cache them internally, and repeated method calls will create a new property
 * set every time, even if supplied with same arguments.  Property sets are not particularly
 * expensive to construct, but it still makes sense to avoid recreating them every time they are
 * needed, if possible.
 * <p>
 * Property sets belong to an "entity", as defined by an {@code entityName} and {@code entityId}.  Normally,
 * the entity name is one of those defined by JIRA's {@code entitymodel.xml} file, such as {@code "Issue"} or
 * {@code "CustomField"} and the entity ID is the value of its {@code "id"} field, but this is not strictly
 * required.  Plugins may store their own property sets without attaching them to any actual database entity.
 * It is recommended that the plugin's key be used as a prefix for the entity name when storing a property set
 * for this purpose.  JIRA stores its own properties for some entities, and plugins should avoid modifying
 * these values directly, as an entity's property set contents are not considered part of its API.
 * </p>
 * <p>
 * The factory methods that return a non-caching property set return an implementation that does not interact with
 * caches in any way.  Any access to them, read or write, will result in a database call.  Any caching property sets
 * for the same entity will not be notified that their cached state is invalid, potentially leaving them with stale
 * values.  For this reason, as well as for performance reasons, a caching property set should usually be preferred.
 * </p>
 * <p>
 * The factory methods that return a caching property set return an implementation that caches its properties.
 * Calls to {@link PropertySet#getKeys() getKeys} and write operations will access the database, but queries
 * like {@link PropertySet#exists(String) exists}, {@link PropertySet#getType(String) getType}, and the various
 * {@link PropertySet#getString(String) getters} to retrieve a property's value will rely on cached information
 * whenever possible.  The caching property sets are cluster-safe unless otherwise noted.
 * </p>
 * <p>
 * Plugin developers that are looking for a general storage mechanism might also want to consider one of the
 * alternatives that are available:
 * </p>
 * <ul>
 * <li>{@link com.atlassian.sal.api.pluginsettings.PluginSettings Plugin Settings} &mdash; a storage class
 *      that is suitable for use by cross-product plugins.</li>
 * <li>{@link com.atlassian.jira.entity.property.JsonEntityPropertyManager JSON Entity Properties} &mdash; similar
 *      to a property set, but using JSON as the storage format.</li>
 * <li>{@link com.atlassian.jira.issue.fields.CustomField Custom Fields} &mdash; issue-specific values that
 *      are user-visible and searchable.</li>
 * <li><a href="https://developer.atlassian.com/display/DOCS/Active+Objects">Active Objects</a> &mdash;
 *      customized object-relational mappings and persistence.</li>
 * </ul>
 *
 * @since v3.12
 */
public interface JiraPropertySetFactory
{
    /**
     * Returns a non-caching {@link PropertySet} for the given entity name and an assumed
     * entity ID of {@code 1}.
     *
     * @param entityName the entity name for the entity that owns this property set
     * @return a non-caching {@link PropertySet}
     */
    @Nonnull
    PropertySet buildNoncachingPropertySet(String entityName);

    /**
     * Returns a non-caching {@link PropertySet} for the given entity name and ID.
     *
     * @param entityName the entity name for the entity that owns this property set
     * @param entityId the entity ID for the entity that owns this property set
     * @return a non-caching {@link PropertySet}
     */
    @Nonnull
    PropertySet buildNoncachingPropertySet(String entityName, Long entityId);

    /**
     * This is the old form of {@link #buildCachingDefaultPropertySet(String)}.
     * Its {@code bulkLoad} option is no longer significant.
     *
     * @param entityName the entity name for the entity that owns this property set
     * @param bulkLoad <em>Ignored</em>
     * @return a caching {@link PropertySet}
     * @deprecated The {@code bulkLoad} flag is no longer relevant.  Use {@link #buildCachingDefaultPropertySet(String)}
     *             instead. Since v6.2.
     */
    @Deprecated
    @Nonnull
    PropertySet buildCachingDefaultPropertySet(String entityName, boolean bulkLoad);

    /**
     * This is the old form of {@link #buildCachingPropertySet(String, Long)}.
     * Its {@code bulkLoad} option is no longer significant.
     *
     * @param entityName the entity name for the entity that owns this property set
     * @param entityId the entity ID for the entity that owns this property set
     * @param bulkLoad <em>Ignored</em>
     * @return a caching {@link PropertySet}
     * @deprecated The {@code bulkLoad} flag is no longer relevant.  Use {@link #buildCachingPropertySet(String,Long)}
     *             instead. Since v6.2.
     */
    @Deprecated
    @Nonnull
    PropertySet buildCachingPropertySet(String entityName, Long entityId, boolean bulkLoad);

    /**
     * Returns a caching {@link PropertySet} for the given entity name and an assumed
     * entity ID of {@code 1}.
     *
     * @param entityName the entity name for the entity that owns this property set
     * @return a caching {@link PropertySet}
     */
    @Nonnull
    PropertySet buildCachingDefaultPropertySet(String entityName);

    /**
     * Returns a caching {@link PropertySet} for the given entity name and ID.
     *
     * @param entityName the entity name for the entity that owns this property set
     * @param entityId the entity ID for the entity that owns this property set
     * @return a caching {@link PropertySet}
     */
    @Nonnull
    PropertySet buildCachingPropertySet(String entityName, Long entityId);

    /**
     * Returns a {@link PropertySet} that caches the return values of the {@code PropertySet} that is provided.
     * This can be used to provide short-term caching around another property set implementation, but its use
     * is now discouraged.
     * <p>
     * <strong>WARNING</strong>: These property sets are <em>not cluster-safe</em> and generally not safe to hold
     * onto for extended time periods.  They do not share cache state with any other cached property set,
     * <em>even if exactly the same delegate {@code propertySet} is supplied to this method multiple times</em>.
     * </p>
     *
     * @param propertySet the {@code PropertySet} to which requests should be delegated when the request is not cached
     * @param bulkLoad If {@code true}, then all properties will be loaded into the cache during initialisation of the
     *          property set.
     * @return a {@link PropertySet} which wraps the supplied property set with a short term, non-cluster-safe cache
     * @deprecated These property sets can become stale if multiple instances are created with the same backing
     *             {@code propertySet} delegate and are also unsafe in a clustered environment.  Use
     *             {@link #buildCachingDefaultPropertySet(String)} or {@link #buildCachingPropertySet(String, Long)}
     *             instead.  Since v6.2.
     */
    @Deprecated
    @Nonnull
    PropertySet buildCachingPropertySet(PropertySet propertySet, boolean bulkLoad);

    /**
     * Returns an in-memory copy of a property set from the database.  This property set will not have its configuration
     * saved to the database on each change.  It is up to the caller of this method to manually synchronize the returned
     * property set with the database.
     *
     * @param entityName the entity name for the entity that owns this property set
     * @param entityId the entity ID for the entity that owns this property set
     * @return a {@link PropertySet} held completely in memory. Changes will not be
     *         written to the database.
     */
    @Nonnull
    PropertySet buildMemoryPropertySet(String entityName, Long entityId);
}
