package com.atlassian.jira.jql.resolver;

import javax.annotation.Nonnull;

import java.util.Collection;
import java.util.List;

/**
 * Looks up domain objects from the database.
 *
 * @since v4.0
 */
public interface NameResolver<T>
{
    /**
     * Returns the list of ids of T objects that have the given name. Names may be unique but often are not, hence
     * the List return type.
     *
     * @param name the name of the T.
     * @return all IDs of objects matching the name or the empty list on name lookup failure.
     */
    List<String> getIdsFromName(String name);

    /**
     * Returns true if the name would resolve to a domain object.
     *
     * @param name the addressable name.
     * @return true only if the name resolves to a domain object in the database.
     */
    boolean nameExists(String name);

    /**
     * Returns true if the id would resolve to a domain object.
     *
     * @param id the primary key.
     * @return true only if the id resolves to a domain object in the database.
     */
    boolean idExists(Long id);

    /**
     * Get by id.
     *
     * @param id the id.
     * @return the domain object or null on lookup failure.
     */
    T get(Long id);

    /**
     * Gets all domain objects of this type in the underlying database. Note that calling this may not be a good
     * idea for some domain object types.
     *
     * @return all objects of the configured type in the database (possibly empty, never null).
     */
    @Nonnull
    Collection<T> getAll();
}
