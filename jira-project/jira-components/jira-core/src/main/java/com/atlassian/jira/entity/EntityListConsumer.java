package com.atlassian.jira.entity;

/**
 * Provide an entity list consumer to {@link com.atlassian.jira.entity.SelectQuery.ExecutionContext#consumeWith(EntityListConsumer)}
 * to accept a stream of results from the entity engine.
 * <p>
 * Note that use of this interface is not sufficient to guarantee streaming behaviour on all database types.  Some
 * implementations (MySQL and Postgres in particular) have special requirements regarding transaction state and
 * fetch size to make that work.  If you need an example of how to do this, see {@code DefaultSaxEntitiesExporter}.
 * </p>
 *
 * @since v5.2
 * @param <E> the entity type yielded by the query, as determined by which {@link Select} factory method was used
 * @param <R> the result type that the consumer will return once all entities have been accepted
 */
public interface EntityListConsumer<E, R>
{
    /**
     * Called by the entity engine so that the entity yielded by the query may be accepted and processed by the
     * consumer.
     *
     * @param entity the entity yielded by the select query
     */
    void consume(E entity);

    /**
     * Called by the entity engine to obtain the return value after all entities have been consumed.  Implementations
     * may assume that this will be called exactly once and that {@link #consume(Object)} will not be called again
     * afterwards.
     *
     * @return the result of consuming the entities
     */
    R result();
}
