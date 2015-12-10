package com.atlassian.jira.entity.property;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.fugue.Function2;
import com.atlassian.jira.event.entity.EntityPropertySetEvent;
import com.atlassian.jira.user.ApplicationUser;

/**
 * Low-level database API for accessing JSON entity properties.
 * <p>
 * JSON entity properties are similar to {@link com.opensymphony.module.propertyset.PropertySet PropertySet}s
 * in that they are permitted to hold more or less arbitrary collections of data.  There are, however, several
 * important differences:
 * </p>
 * <ul>
 * <li>Property sets support a wider range of data types, including things like XML documents, Properties,
 *      and any serializable object.  JSON entity properties support only those data types that JSON itself
 *      supports.</li>
 * <li>Property sets are stored as an individual database row for each property while JSON Entity Properties
 *      are stored as a single row containing the entire data structure.</li>
 * <li>Property sets are expected to be directly associated with an OfBiz entity of some kind, and only one
 *      such property set can exist for any given entity.  JSON entity properties are more flexible, in
 *      that they are keyed by an entity name, entity ID, and a key.  This allows multiple kinds of metadata
 *      to be attached to the entity, such as both issue links and issue properties, without the data
 *      colliding.</li>
 * <li>Property sets are loaded by direct reference using the owning entity name and entity ID.  JSON entity
 *      properties are more flexible in this regard as there is a fluent {@link #query() query service}
 *      for locating them.</li>
 * </ul>
 * <p>
 * Note: The {@code entityName} used here <strong>MUST</strong> be unique to the service that maintains
 * the properties.  To reduce the risk of collision, it <strong>SHOULD NOT</strong> be the same as the
 * name of the owning entity itself, as there may be other services that also which to attach properties
 * to that same entity.  For example, the
 * {@link com.atlassian.jira.bc.project.version.remotelink.RemoteVersionLinkService RemoteVersionLinkService}
 * uses {@code "RemoteVersionLink"} as its {@code entityName}, not {@code "Version"}.
 *
 * @since v6.1
 */
@ExperimentalApi
public interface JsonEntityPropertyManager
{
    /**
     * Look up an entity property by the entity name, entity id, and property key.
     *
     * @param entityName the entity name of the property
     * @param entityId the entity ID of the property
     * @param key the key of the property
     * @return the matching property, or {@code null} if the property does not exist.
     */
    @Nullable
    EntityProperty get(String entityName, Long entityId, String key);

    /**
     * Set the value for an entity property, creating, updating, or deleting it as necessary.
     *
     * @param entityName the entity name for the property (maximum length {@code 255}).  As explained in the
     *      {@link JsonEntityPropertyManager class documentation}, this value should be unique to the service
     *      creating the properties and will generally <strong>not</strong> be the same as the entity to
     *      which the properties are attached.
     * @param entityId the entity ID for the property.  In general, this will be the same as the ID of the
     *      owning entity; for example, the
     *      {@link com.atlassian.jira.bc.project.version.remotelink.RemoteVersionLinkService RemoteVersionLinkService}
     *      uses the {@link com.atlassian.jira.project.version.Version#getId() version ID} for this value.
     * @param key the key for the property (maximum length {@code 255}).  This value should generally be suitable
     *      for a reverse lookup when the same data might be associated with multiple entities.  For example, the
     *      {@link com.atlassian.jira.bc.project.version.remotelink.RemoteVersionLinkService RemoteVersionLinkService}
     *      uses the {@code global ID} of the remote object, which is the same identifier used by applinks to identify
     *      a Confluence page, Bamboo project, etc.
     * @param json the new value for the property, or {@code null} to delete it (maximum length
     *      is available by calling {@link #getMaximumValueLength()})
     *
     * @throws FieldTooLongJsonPropertyException if any of the values exceed the maximum permitted length
     * @throws InvalidJsonPropertyException if {@code json} is malformed
     * @throws IllegalArgumentException if {@code entityId} is {@code null}, or if {@code entityName} or
     *          {@code key} is either {@code null} or blank.
     * @see #putDryRun(String, String, String)
     * @deprecated This method does not properly throw events. Use {@link com.atlassian.jira.entity.property.JsonEntityPropertyManager#put(com.atlassian.jira.user.ApplicationUser, String, Long, String, String, com.atlassian.fugue.Function2, boolean)} instead.
     */
    void put(@Nonnull String entityName, @Nonnull Long entityId, @Nonnull String key, @Nullable String json);

    void put(ApplicationUser user, @Nonnull String entityName, @Nonnull Long entityId, @Nonnull String key, @Nullable String json, Function2<ApplicationUser, EntityProperty, ? extends EntityPropertySetEvent> eventFunction, boolean dispatchEvent);

    /**
     * Performs all of the validation that would be performed during a {@link #put(String, Long, String, String)}
     * request, but does not actually store the value.
     *
     * @param entityName as for {@link #put(String, Long, String, String)}
     * @param key  as for {@link #put(String, Long, String, String)}
     * @param json  as for {@link #put(String, Long, String, String)}
     * @throws FieldTooLongJsonPropertyException  as for {@link #put(String, Long, String, String)}
     * @throws InvalidJsonPropertyException as for {@link #put(String, Long, String, String)}
     * @throws IllegalArgumentException as for {@link #put(String, Long, String, String)}
     * @see #put(String, Long, String, String)
     */
    void putDryRun(@Nonnull String entityName, @Nonnull String key, @Nullable String json);

    /**
     * Deletes the stored value for an entity property, if it exists.
     *
     * @param entityName the entity name of the property to be deleted
     * @param entityId the entity ID of the property to be deleted
     * @param key the key of the property to be deleted
     */
    void delete(@Nonnull String entityName, @Nonnull Long entityId, @Nonnull String key);

    /**
     * The maximum allowed length (in characters) permitted for the {@code String json} value
     * when calling {@link #put(String, Long, String, String)} (or {@link #putDryRun(String, String, String)}).
     * This value is currently {@code 32,768}, but this may change in the future.
     */
    int getMaximumValueLength();

    /**
     * Returns a query object for finding, counting, or deleting properties with various restrictions.
     * See {@link EntityPropertyQuery} for usage and minimum requirements.
     *
     * @return the query object
     */
    EntityPropertyQuery<?> query();



    /**
     * Produces a list of entity property keys that match the provided entity name and key prefix.
     * This convenience method is exactly equivalent to:
     * <code><pre>
     *     {@link #query()}.{@link EntityPropertyQuery#entityName(String) entityName(entityName)}
     *          .{@link EntityPropertyQuery#keyPrefix(String) keyPrefix(keyPrefix)}
     *          .{@link EntityPropertyQuery.ExecutableQuery#findDistinctKeys() findDistinctKeys()}
     * </pre></code>
     *
     * @param entityName the entity name for the entities that are associated with the properties.
     * @param keyPrefix the prefix to apply for limiting which keys are returned; must not be
     *      {@code null} or a blank string
     * @return the list of distinct matching keys, sorted in ascending alphabetical order
     * @throws IllegalArgumentException if either {@code entityName} or {@code keyPrefix} is {@code null} or blank
     */
    @Nonnull
    List<String> findKeys(@Nonnull String entityName, @Nonnull String keyPrefix);

    /**
     * Produces a list of entity property keys that match the provided entity name and entity id.
     * This convenience method is exactly equivalent to:
     * <code><pre>
     *     {@link #query()}.{@link EntityPropertyQuery#entityName(String) entityName(entityName)}
     *          .{@link EntityPropertyQuery#entityId(Long) entityId(entityId)}
     *          .{@link EntityPropertyQuery.ExecutableQuery#findKeys()}
     * </pre></code>
     *
     * @param entityName the entity name for the entities that are associated with the properties.
     * @param entityId the id of the entity.
     * @return the list of distinct matching keys, sorted in ascending alphabetical order
     * @throws IllegalArgumentException if either {@code entityName} or {@code entityId} is {@code null} or blank
     */
    @Nonnull
    List<String> findKeys(@Nonnull String entityName, @Nonnull Long entityId);

    /**
     * Returns whether or not a given property exists.  This convenience method is equivalent
     * to {@link #get(String, Long, String)} {@code != null}, but does not actually retrieve
     * the JSON data from the database, so it may have better performance characteristics when
     * the JSON content is not needed.
     *
     * @param entityName the entity name for the property
     * @param entityId the entity ID for the property
     * @param key the key for the property
     */
    boolean exists(@Nonnull String entityName, @Nonnull Long entityId, @Nonnull String key);

    /**
     * Counts the number of properties that match the given entity.
     * This convenience method is exactly equivalent to:
     * <code><pre>
     *     {@link #query()}.{@link EntityPropertyQuery#entityName(String) entityName(entityName)}
     *          .{@link EntityPropertyQuery#entityId(Long) entityId(entityId)}
     *          .{@link EntityPropertyQuery.ExecutableQuery#count() count()}
     * </pre></code>
     *
     * @param entityName the entity name of the owning entity
     * @param entityId the entity ID of the owning entity
     * @return the count of properties assigned to that entity
     * @throws IllegalArgumentException if either {@code entityName} is {@code null} or blank or {@code entityId}
     *      is {@code null}.
     */
    long countByEntity(@Nonnull String entityName, @Nonnull Long entityId);

    /**
     * Counts the number of properties that match the given entity name and property key.
     * This convenience method is exactly equivalent to:
     * <code><pre>
     *     {@link #query()}.{@link EntityPropertyQuery#entityName(String) entityName(entityName)}
     *          .{@link EntityPropertyQuery#key(String) key(key)}
     *          .{@link EntityPropertyQuery.ExecutableQuery#count() count()}
     * </pre></code>
     *
     * @param entityName the entity name of the owning entities
     * @param key the property key to look for
     * @return the count of properties with that key which are assigned to entities with the given name
     * @throws IllegalArgumentException if either {@code entityName} or {@code key} is {@code null} or blank
     */
    long countByEntityNameAndPropertyKey(@Nonnull String entityName, @Nonnull String key);

    /**
     * Deletes all properties that are associated with the provided entity.
     * This convenience method is exactly equivalent to:
     * <code><pre>
     *     {@link #query()}.{@link EntityPropertyQuery#entityName(String) entityName(entityName)}
     *          .{@link EntityPropertyQuery#entityId(Long) entityId(entityId)}
     *          .{@link EntityPropertyQuery.ExecutableQuery#delete() delete()}
     * </pre></code>
     *
     * @param entityName the entity name for the properties
     * @param entityId the entity ID for the properties
     */
    void deleteByEntity(@Nonnull String entityName, @Nonnull Long entityId);

    /**
     * Deletes all properties that are associated with the provided entity name and
     * property key.
     * This convenience method is exactly equivalent to:
     * <code><pre>
     *     {@link #query()}.{@link EntityPropertyQuery#entityName(String) entityName(entityName)}
     *          .{@link EntityPropertyQuery#key(String) key(key)}
     *          .{@link EntityPropertyQuery.ExecutableQuery#delete() delete()}
     * </pre></code>
     *
     * @param entityName the entity name for the properties
     * @param key the property key for the properties
     */
    void deleteByEntityNameAndPropertyKey(@Nonnull String entityName, @Nonnull String key);
}



