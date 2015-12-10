package com.atlassian.jira.ofbiz;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.exception.DataAccessException;

import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityFindOptions;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericModelException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.entity.Transformation;
import org.ofbiz.core.entity.model.ModelReader;

/**
 * A wrapper around {@link org.ofbiz.core.entity.DelegatorInterface} that does not throw {@link GenericEntityException}.
 */
@PublicApi
public interface OfBizDelegator
{

    public static final String VERSION = "Version";
    public static final String ISSUE_LINK = "IssueLink";
    public static final String ISSUE_LINK_TYPE = "IssueLinkType";
    public static final String PROJECT_COMPONENT = "Component";

    /**
     * Finds GenericValue records by the specified field value.
     *
     * @param entityName The Name of the Entity as defined in the entity XML file
     * @param fieldName The field to do filtering by.
     * @param fieldValue The desired value for the filtering field.
     * @return List of GenericValue instances that match the query
     * @throws com.atlassian.jira.exception.DataAccessException If an error occurs in the persistence layer.
     */
    List<GenericValue> findByField(String entityName, String fieldName, Object fieldValue);

    /**
     * Finds GenericValue records by the specified field value.
     *
     * @param entityName The Name of the Entity as defined in the entity XML file
     * @param fieldName The field to do filtering by.
     * @param fieldValue The desired value for the filtering field.
     * @param orderBy Single field to order by.
     * @return List of GenericValue instances that match the query
     * @throws com.atlassian.jira.exception.DataAccessException If an error occurs in the persistence layer.
     */
    List<GenericValue> findByField(String entityName, String fieldName, Object fieldValue, String orderBy);

    /**
     * Finds GenericValue records by all of the specified fields (ie: combined using AND).
     *
     * @param entityName The Name of the Entity as defined in the entity XML file
     * @param fields The fields of the named entity to query by with their corresponding values
     * @return List of GenericValue instances that match the query
     * @throws com.atlassian.jira.exception.DataAccessException If an error occurs in the persistence layer.
     */
    List<GenericValue> findByAnd(String entityName, Map<String, ?> fields) throws DataAccessException;

    List<GenericValue> findByAnd(String entityName, Map<String, ?> fields, List<String> orderBy) throws DataAccessException;

    List<GenericValue> findByAnd(String entityName, List<EntityCondition> expressions) throws DataAccessException;

    List<GenericValue> findByOr(String entityName, List<? extends EntityCondition> expressions, List<String> orderBy) throws DataAccessException;

    List<GenericValue> findByLike(String entityName, Map<String, ?> map) throws DataAccessException;

    List<GenericValue> findByLike(String entityName, Map<String, ?> map, List<String> orderBy) throws DataAccessException;

    void removeAll(List<GenericValue> genericValues) throws DataAccessException;

    int removeByAnd(String s, Map<String, ?> map) throws DataAccessException;

    int removeByCondition(String entityName, EntityCondition condition) throws DataAccessException;

    /**
     * Remove the given entity from the DB.
     *
     * @param entityName the entity type (ie TABLE)
     * @param id the id of the row to delete.
     *
     * @return number of rows effected by this operation
     */
    int removeById(String entityName, Long id);

    int removeValue(GenericValue value) throws DataAccessException;

    void storeAll(List<GenericValue> genericValues) throws DataAccessException;

    List<GenericValue> findAll(String s) throws DataAccessException;

    List<GenericValue> findAll(String s, List<String> orderBy) throws DataAccessException;

    void store(GenericValue gv) throws DataAccessException;

    /**
     * Creates a new GenericValue, and persists it.
     * <p>
     *   If there is no "id" in the field values, one is created using the entity sequence.
     * </p>
     *
     * @param entityName the entity name.
     * @param fields field values
     * @return The new GenericValue.
     * @throws DataAccessException if an error occurs in the Database layer
     *
     * @see #createValueWithoutId(String, java.util.Map)
     * @see #makeValue(String)
     */
    GenericValue createValue(String entityName, Map<String, Object> fields) throws DataAccessException;

    /**
     * Creates a new GenericValue, and persists it without trying to automatically populate the ID column.
     * <p>
     *   Use this for entities that don't have a numeric ID column.
     * </p>
     *
     * @param entityName the entity name.
     * @param fields field values
     *
     * @throws DataAccessException if an error occurs in the Database layer
     *
     * @see #createValue(String, java.util.Map)
     * @see #makeValue(String)
     */
    void createValueWithoutId(String entityName, Map<String, Object> fields) throws DataAccessException;

    /**
     * Creates an Entity in the form of a GenericValue without persisting it.
     *
     * @param entityName the entity name.
     * @return The new GenericValue.
     *
     * @see #makeValue(String, java.util.Map)
     * @see #createValue(String, java.util.Map)
     */
    GenericValue makeValue(String entityName);

    /**
     * Creates an Entity in the form of a GenericValue without persisting it.
     *
     * @param entityName the entity name.
     * @param fields initial field values
     * @return The new GenericValue.
     *
     * @see #makeValue(String)
     * @see #createValue(String, java.util.Map)
     */
    GenericValue makeValue(String entityName, Map<String, Object> fields);

    /**
     * Find a Generic Entity by its numeric ID.
     *
     * <p> This method is a synonym for {@link #findByPrimaryKey(String, Long)}
     *
     * @param entityName The Name of the Entity as defined in the entity XML file
     * @param id The numeric "id" field value that is the primary key of this entity.
     * @return The GenericValue corresponding to the ID
     *
     * @throws com.atlassian.jira.exception.DataAccessException DataAccessException
     */
    GenericValue findById(String entityName, Long id) throws DataAccessException;

    /**
     * Find a Generic Entity by its single numeric Primary Key.
     *
     * <p> This method is a convenience for entities with a numeric primary key on single field called "id".
     * This is the case for most JIRA entities.
     *
     * @param entityName The Name of the Entity as defined in the entity XML file
     * @param id The numeric "id" field value that is the primary key of this entity.
     * @return The GenericValue corresponding to the primary key
     *
     * @throws com.atlassian.jira.exception.DataAccessException DataAccessException
     * @see #findByPrimaryKey(String, java.util.Map)
     */
    GenericValue findByPrimaryKey(String entityName, Long id) throws DataAccessException;

    /**
     * Find a Generic Entity by its Primary Key.
     *
     * @param entityName The Name of the Entity as defined in the entity XML file
     * @param fields The field/value pairs of the primary key (in JIRA, mostly just a single field "id")
     * @return The GenericValue corresponding to the primary key
     *
     * @throws com.atlassian.jira.exception.DataAccessException DataAccessException
     * @see #findByPrimaryKey(String, Long)
     */
    GenericValue findByPrimaryKey(String entityName, Map<String, ?> fields) throws DataAccessException;

    List<GenericValue> getRelated(String relationName, GenericValue gv) throws DataAccessException;

    List<GenericValue> getRelated(String relationName, GenericValue gv, List<String> orderBy) throws DataAccessException;

    /**
     * Remove all the entities related to the passed generic value via the passed relationship name.
     *
     * @param relationName the name of the relationship.
     * @param schemeGv the gv at the start of the relationship.
     * @return true the database was changed, false otherwise.
     */
    boolean removeRelated(String relationName, GenericValue schemeGv);

    /**
     * Runs a {@code COUNT *} query over the given entity.
     * <p>
     * Ensure that there is a view-entity defined in {@code entitymodel.xml} and {@code entitygroup.xml} for the entity
     * you are calling this method with, and that the view-entity is named correctly!  The view-entity must be named
     * the name of the normal entity with {@code "Count"} appended.  For example, for the {@code "Issue"} entity,
     * the view-entity must be called {@code "IssueCount"}.  Otherwise an exception will be thrown.
     * </p>
     * <p>
     * For JIRA core developers, see {@code EntityEngine}'s {@code Select} factory for a more flexible alternative,
     * including the ability to terminate queries with {@code .count()} instead of {@code .asList()} to accomplish
     * a count without creating a special view entity for that purpose.  For add-on developers, this can only be
     * done directly through the lower level {@code DelegatorInterface} at this time.
     * </p>
     *
     * @param entityName entity name
     * @return count
     * @throws DataAccessException if data access problems occur
     */
    long getCount(String entityName) throws DataAccessException;

    /**
     * Runs a {@code COUNT *} query over the given entity with some {@code WHERE} conditions.
     * <p>
     * In addition to the restrictions given for {@link #getCount(String)}, the view-entity will need to define
     * any columns that you wish to use in the where clause.
     * </p>
     *
     * @param entityName entity name
     * @param fields The fields of the named entity to query by with their corresponding values
     * @return count
     * @see #getCount(String)
     * @throws DataAccessException if data access problems occur
     */
    long getCountByAnd(final String entityName, final Map<String, ?> fields);

    /**
     * Returns a new OfBizListIterator.
     * <p/>
     * <b>IMPORTANT</b>: the returned iterator needs to be properly closed in a {@code finally} block to avoid connection
     * leaks.
     */
    OfBizListIterator findListIteratorByCondition(String entityType, EntityCondition condition) throws DataAccessException;

    /**
     * Returns a new OfBizListIterator.
     * <p/>
     * <b>IMPORTANT</b>: the returned iterator needs to be properly closed in a {@code finally} block to avoid connection
     * leaks.
     */
    OfBizListIterator findListIteratorByCondition(String entityName, EntityCondition whereEntityCondition,
            EntityCondition havingEntityCondition, Collection<String> fieldsToSelect, List<String> orderBy,
            EntityFindOptions entityFindOptions) throws DataAccessException;

    /**
     * This can be used to perform an update on the entityName of all the rows
     * identified by the keys with the values stored in the updateValues.
     *
     * @param entityName   identifies the table to perform the update on.
     * @param updateValues is a map where the key is the fieldName and the value
     *                     is the value to update the column to.
     * @param keys         is a list of Long values that represent the primary keys of the
     *                     the where clause.
     * @return the number of rows updated
     * @throws com.atlassian.jira.exception.DataAccessException If an error occurs in the persistence layer.
     */
    int bulkUpdateByPrimaryKey(String entityName, Map<String, ?> updateValues, List<Long> keys) throws DataAccessException;

    /**
     * This can be used to perform an update on the entityName of all the rows
     * identified by AND criteria of the fields specified by the criteria map.
     *
     * @param entityName   identifies the table to perform the update on.
     * @param updateValues is a map where the key is the fieldName and the value
     *                     is the value to update the column to.
     * @param criteria     map of field to value mapping that will be used to generate the
     *                     where clause of the update SQL statement. Multiple entries in the map are joined using the
     *                     AND operator.
     * @return the number of rows updated
     * @throws com.atlassian.jira.exception.DataAccessException If an error occurs in the persistence layer.
     */
    int bulkUpdateByAnd(String entityName, Map<String, ?> updateValues, Map<String, ?> criteria) throws DataAccessException;

    /**
     * This can be used to perform an update on the entityName of all the rows
     * identified by AND criteria of the fields specified by the criteria map.
     *
     * @param entityName    table na,e
     * @param updateColumns map of update to - update from columns
     * @param criteria      map of column names and their values that will create WHERE clause
     * @return the number of rows updated
     * @throws com.atlassian.jira.exception.DataAccessException If an error occurs in the persistence layer.
     */
    int bulkCopyColumnValuesByAnd(String entityName, Map updateColumns, Map criteria) throws DataAccessException;

    /**
     * This can be used to remove rows for a given entity based on <code>entityName</code>
     * and where <code>entityId</q
     *
     * @param entityName identifies the table to perform the remove on.
     * @param entityId   the Ofbiz fieldName to be used for the identifier, eg WHERE fieldName IN (...). Must be the
     *                   same case as that found in entitymodel.xml.
     * @param ids        a list of entity IDs of the rows to be removed
     * @return number of rows removed
     * @throws GenericModelException if the given entityId is not valid for the given entity
     * @throws DataAccessException   if there are problems executing/accessing the data store
     */
    int removeByOr(String entityName, String entityId, List<Long> ids) throws DataAccessException, GenericModelException;

    /**
     * Finds GenericValues by the conditions specified in the EntityCondition object.
     *
     * @param entityName The Name of the Entity as defined in the entity model XML file
     * @param entityCondition The EntityCondition object that specifies how to constrain this query
     * @param fieldsToSelect The fields of the named entity to get from the database; if empty or null all fields will be retreived
     * @param orderBy The fields of the named entity to order the query by; optionally add a " ASC" for ascending or " DESC" for descending
     * @return List of GenericValue objects representing the search results
     *
     * @since v3.12
     * @throws com.atlassian.jira.exception.DataAccessException If an error occurs in the persistence layer.
     */
    List<GenericValue> findByCondition(String entityName, EntityCondition entityCondition, Collection<String> fieldsToSelect, List<String> orderBy) throws DataAccessException;

    /**
     * Finds GenericValues by the conditions specified in the EntityCondition object with no specified order.
     * <p>
     * Convenience method for calling {@link #findByCondition(String, EntityCondition, Collection, List)} with 
     * an empty orderBy list.
     *
     * @param entityName The Name of the Entity as defined in the entity model XML file
     * @param entityCondition The EntityCondition object that specifies how to constrain this query
     * @param fieldsToSelect The fields of the named entity to get from the database; if empty or null all fields will be retreived
     * @return List of GenericValue objects representing the search results
     *
     * @since v4.1
     * @throws com.atlassian.jira.exception.DataAccessException If an error occurs in the persistence layer.
     */
    List<GenericValue> findByCondition(String entityName, EntityCondition entityCondition, Collection<String> fieldsToSelect) throws DataAccessException;

    /**
     * Returns a model reader that can be used to retrieve all the different entitynames configured in the
     * entitymodel.
     *
     * @return a {@link ModelReader}
     * @since 4.4
     */
    ModelReader getModelReader();

    /**
     * Refreshes the sequencer that is used to retrieve unique IDs in the database.
     *
     * @since 4.4
     */
    void refreshSequencer();

    /**
     * Returns the underlying raw Entity Engine DelegatorInterface.
     *
     * @return the underlying raw Entity Engine DelegatorInterface.
     */
    DelegatorInterface getDelegatorInterface();

    /**
     * Applies the given transformation to any entities matching the given condition.
     *
     * @param entityName      the type of entity to transform (required)
     * @param entityCondition the condition that selects the entities to transform (null means transform all)
     * @param orderBy         the order in which the entities should be selected for updating (null means no ordering)
     * @param lockField       the entity field to use for optimistic locking; the value of this field will be read
     * between the SELECT and the UPDATE to determine whether another process has updated one of the target records in
     * the meantime; if so, the transformation will be reapplied and another UPDATE attempted
     * @param transformation  the transformation to apply (required)
     * @return the transformed entities in the order they were selected (never null)
     * @since 6.2
     */
    List<GenericValue> transform(
            String entityName, EntityCondition entityCondition, List<String> orderBy, String lockField, Transformation transformation);

    /**
     * Applies the given transformation to the entity matching the given condition.
     *
     * @param entityName      the type of entity to transform (required)
     * @param entityCondition the condition that selects the entity to transform (must select one entity)
     * @param lockField       the entity field to use for optimistic locking; the value of this field will be read
     * between the SELECT and the UPDATE to determine whether another process has updated one of the target records in
     * the meantime; if so, the transformation will be reapplied and another UPDATE attempted
     * @param transformation  the transformation to apply (required)
     * @return the transformed entity (never null)
     * @throws IllegalArgumentException if the given condition selects more than one entity
     * @since 6.2
     */
    GenericValue transformOne(String entityName, EntityCondition entityCondition, String lockField, Transformation transformation);
}
