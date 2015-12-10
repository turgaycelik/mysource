package com.atlassian.jira.entity;

import java.util.Collection;
import java.util.List;

/**
 * Provides methods for working with the DB via Atlassian EntityEngine.
 * <p>
 * These methods are considered a higher level alternative to the OfBizDelegator and provide two main advantages:
 * <ul>
 *     <li>They provide a fluent interface that is considered easier to read and understand than the OfBizDelegator methods.</li>
 *     <li>They allow the developer to ignore GenericValues and deal directly with proper Data Objects.<br>
 *         (Provided that an EntityFactory exists for the given Entity)</li>
 * </ul>
 *
 * This interface is still experimental at this stage.
 *
 * @since v4.4
 */
public interface EntityEngine
{
    /**
     * Starts a dialog to run a SELECT query against EntityEngine.
     * <p>
     * e.g. to run "SELECT * FROM remotelink WHERE id = ?" (and return a single entity value) you could write:
     * <pre>
     *   RemoteIssueLink link = entityEngine.selectFrom(Entity.REMOTE_ISSUE_LINK)
     *                                 .whereEqual("id", remoteIssueLinkId)
     *                                 .singleValue();
     * </pre>
     * e.g. to run "SELECT * FROM remotelink WHERE issueid = ? AND app = ? ORDER BY type" you could write:
     * <pre>
     *   List<RemoteIssueLink> remoteIssueLinks =
     *           entityEngine.selectFrom(Entity.REMOTE_ISSUE_LINK)
     *                       .whereEqual("issueid", issueId)
     *                       .andEqual("app", app)
     *                       .orderBy("type");
     * <pre>
     *
     * @param entityFactory that can convert GenericValues into Entity data objects. See {@link Entity} for existing factories.
     * @param <E> Entity Data Object type.
     * @return The context that begins a fluent dialog to run a SELECT query.
     *
     * @see Entity
     */
    <E> SelectFromContext<E> selectFrom(EntityFactory<E> entityFactory);

    /**
     * Creates a new Entity and auto populates the ID if no ID is explicitly set.
     *
     * Use this for entities that include an ID column (most of them).
     *
     * @param entityFactory the EntityFactory
     * @param newValue the entity to be created.
     * @param <E> entity type
     *
     * @return the newly created value (with the newly populated ID in it).
     *
     * @see #createValueWithoutId(EntityFactory, Object)
     */
    <E> E createValue(EntityFactory<E> entityFactory, E newValue);

    /**
     * Creates a new Entity without trying to automatically populate the ID column.
     *
     * Use this for entities that don't have a numeric ID column.
     *
     * @param entityFactory the EntityFactory
     * @param newValue the entity to be created.
     * @param <E> entity type
     *
     * @see #createValue(EntityFactory, Object)
     */
    <E> void createValueWithoutId(EntityFactory<E> entityFactory, E newValue);

    <E> void updateValue(EntityFactory<E> entityFactory, E newValue);

    /**
     * Remove the given entity from the DB.
     *
     * @param entityFactory represents the entity type (ie TABLE)
     * @param id the id of the row to delete.
     *
     * @return number of rows effected by this operation
     */
    <E> int removeValue(EntityFactory<E> entityFactory, Long id);

    /**
     * Allows you to execute an UPDATE statement using a fluent interface.
     * <p>
     * See the {@link Update} class for an example.
     *
     * @param updateContext build up a fluent UPDATE statement here. Should start with <code>Update.into(</code>
     * @return the number of entities / DB rows deleted.
     */
    int execute(Update.WhereContext updateContext);

    /**
     * Allows you to execute an SQL DELETE using a fluent interface.
     * <p>
     * You should call this using code that looks like:
     * <pre>
     *     entityEngine.delete(Delete.from(Entity.ISSUE_SECURITY_LEVEL).whereIdEquals(securityLevelId));
     * </pre>
     * or:
     * <pre>
     *     entityEngine.delete(
     *         Delete.from(Entity.ISSUE_SECURITY_LEVEL)
     *               .whereEqual("scheme", schemeId)
     *               .andEqual("name", name)
     *     );
     * </pre>
     *
     * @param deleteContext build up a fluent DELETE statement here. Should start with <code>Delete.from(</code>
     * @return the number of entities / DB rows deleted.
     */
    int delete(Delete.DeleteWhereContext deleteContext);

    <E> SelectQuery.ExecutionContext<E> run(SelectQuery<E> selectQuery);

    interface SelectFromContext<E>
    {
        WhereContext<E> findAll();

        E findById(Long id);

        @SuppressWarnings ( { "UnusedDeclaration" })
        WhereEqualContext<E> whereEqual(String fieldName, String value);

        WhereEqualContext<E> whereEqual(String fieldName, Long value);

        /**
         * Supports IN operator.
         * <p>
         * NOTE: No use cases now, but the current design does not allow us to easily mix different types
         *       of contexts, e.g., (f1 = v1 and f2 in (v2, v3)).
         */
        <V> WhereInContext<E> whereIn(String fieldName, Collection<V> values);
    }

    interface WhereContext<E>
    {
        List<E> orderBy(String... orderByColumn);

        List<E> list();

        /**
         * Returns the single Entity that is the result of this query.
         * <p>
         * Will return null if no rows were returned by the DB query.
         * Throws {@link com.atlassian.jira.jql.builder.PrecedenceSimpleClauseBuilder.IllegalState} if more than one row is found.
         *
         * @return the Entity found or null of none found.
         */
        E singleValue();
    }

    interface WhereEqualContext<E> extends WhereEqualAndContext<E>//, WhereEqualOrContext<E>
    {
    }

    interface WhereEqualAndContext<E> extends WhereContext<E>
    {
        WhereEqualAndContext<E> andEqual(String fieldName, String value);

        @SuppressWarnings ( { "UnusedDeclaration" })
        WhereEqualAndContext<E> andEqual(String fieldName, Long value);
    }

//    interface WhereEqualOrContext<E> extends WhereContext<E>
//    {
//        WhereEqualOrContext<E> orEqual(String fieldName, String value);
//
//        WhereEqualOrContext<E> orEqual(String fieldName, Long value);
//    }

    interface WhereInContext<E> extends WhereContext<E>
    {
    }
}
