
package com.atlassian.jira.entity.property;

import java.util.List;

import javax.annotation.Nonnull;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.jira.util.Visitor;

/**
 * A query builder for requests to the {@link JsonEntityPropertyManager}.
 * <p/>
 * Notes:
 * <ul>
 * <li>Queries must include an entity name or key at the very least.</li>
 * <li>Although it probably doesn't make any sense to do so, searching by the entity ID without
 *      the entity name is permitted, as long as the key is specified.</li>
 * <li>Null and blank values are generally not permitted, including for the
 *      key prefix search.</li>
 * <li>The offset is ignored unless maxResults is specified, and both are ignored for
 *      {@link ExecutableQuery#count() count} queries.</li>
 * </ul>
 */
@ExperimentalApi
public interface EntityPropertyQuery<T extends EntityPropertyQuery<T>>
{
    /**
     * Restricts the search by the entity name, which can neither be {@code null} nor blank.
     * If this is not specified, then the {@link #key(String) key} is required.
     *
     * @param entityName the entity name; must not be {@code null} or blank
     * @return this query, as an {@link ExecutableQuery}, since specifying the {@code entityName}
     *      is sufficient to form a valid query
     * @throws IllegalArgumentException if {@code entityName} is {@code null} or blank
     */
    public ExecutableQuery entityName(@Nonnull String entityName);

    /**
     * Restricts the search to the given property key, which can neither be {@code null} nor blank.
     * If this is not specified, then the {@link #entityName(String) entity name} is required.
     * This restriction is not compatible with a {@link #keyPrefix(String) key prefix} restriction.
     *
     * @param key the property key
     * @return this query, as an {@link ExecutableQuery}, since specifying the {@code key}
     *      is sufficient to form a valid query
     * @throws IllegalArgumentException if {@code key} is {@code null} or blank
     * @throws IllegalStateException if {@link #keyPrefix(String)} has already been specified
     */
    public ExecutableQuery key(@Nonnull String key);

    /**
     * Restricts the search by the entity ID.  This may be specified without specifying the
     * entity name (so long as the {@link #key(String) key} has been provided), but doing
     * so probably does not make any sense.
     *
     * @param entityId the entity ID
     * @return this query
     * @throws IllegalArgumentException if {@code entityId} is {@code null}
     */
    public T entityId(@Nonnull Long entityId);

    /**
     * Restricts the search to the given property key prefix, which can neither be {@code null} nor blank.
     * The {@link #entityName(String) entity name} is required to use this restriction, which is
     * not compatible with a {@link #key(String) key} restriction.
     *
     * @param keyPrefix the property key prefix
     * @return this query
     * @throws IllegalArgumentException if {@code keyPrefix} is {@code null} or blank
     * @throws IllegalStateException if {@link #key(String)} has already been specified
     */
    public T keyPrefix(@Nonnull String keyPrefix);

    /**
     * The offset into the result list at which to begin.  You must also specify {@link #maxResults(int) max results}
     * to use this feature.
     *
     * @param offset the (0-based) index offset into the complete results.  Nonsensical values
     *      like {@code -42} are silently ignored.
     * @return this query
     */
    public T offset(int offset);

    /**
     * The maximum number of results to return.  You must specify this value to use an {@link #offset(int) offset}.
     *
     * @param maxResults the maximum results to return.  Nonsensical values like {@code -42} are
     *      silently ignored.
     * @return this query
     */
    public T maxResults(int maxResults);



    /**
     * An {@code EntityPropertyQuery} for which sufficient contraints have been provided to make
     * the query valid.
     */
    public interface ExecutableQuery extends EntityPropertyQuery<ExecutableQuery>
    {
        /**
         * Produces a list of entity property keys that satisfy this query.
         *
         * @return the list of distinct matching keys, sorted in ascending alphabetical order
         * @see #find()
         * @see #find(Visitor)
         */
        @Nonnull
        List<String> findDistinctKeys();

        /**
         * Produces a list of entity property keys that satisfy this query.
         * @return the list of matching keys, sorted in ascending alphabetical order.
         * @see #find()
         * @see #find(Visitor)
         */
        @Nonnull
        List<String> findKeys();

        /**
         * Produces a list of entity properties that satisfy this query.
         *
         * @return a list containing all of the results that were found, sorted by the key;
         *      never {@code null}
         * @see #find(Visitor)
         * @see #findDistinctKeys()
         */
        @Nonnull
        List<EntityProperty> find();

        /**
         * Produces a call to the provided visitor for each entity property that satisfies
         * this query.
         *
         * @param visitor the visitor to {@link Visitor#visit(Object) visit} for each matching entity property
         * @see #find()
         * @see #findDistinctKeys()
         */
        void find(@Nonnull Visitor<EntityProperty> visitor);

        /**
         * Counts the number of properties that match the given query.
         *
         * @return the count of matching entity properties
         */
        long count();

        /**
         * Deletes all the properties that match this query.
         */
        void delete();
    }
}
