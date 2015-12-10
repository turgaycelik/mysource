package com.atlassian.jira.ofbiz;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.web.filters.ThreadLocalQueryProfiler;

import com.google.common.collect.ImmutableList;

import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityConditionParam;
import org.ofbiz.core.entity.EntityFieldMap;
import org.ofbiz.core.entity.EntityFindOptions;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.Transformation;
import org.ofbiz.core.entity.GenericDataSourceException;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericHelper;
import org.ofbiz.core.entity.GenericModelException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.entity.jdbc.AutoCommitSQLProcessor;
import org.ofbiz.core.entity.jdbc.SQLProcessor;
import org.ofbiz.core.entity.jdbc.SqlJdbcUtil;
import org.ofbiz.core.entity.model.ModelEntity;
import org.ofbiz.core.entity.model.ModelField;
import org.ofbiz.core.entity.model.ModelFieldTypeReader;
import org.ofbiz.core.entity.model.ModelReader;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class DefaultOfBizDelegator implements OfBizDelegator
{
    private static final Logger log = Logger.getLogger(DefaultOfBizDelegator.class);
    private static final int DEFAULT_DATABASE_QUERY_BATCH_SIZE = 100;
    private static final String COUNT_FIELD_NAME = "count";
    private static final Collection<String> UNSUPPORTED_TYPES_FOR_FINDBY = CollectionBuilder.newBuilder("very-long", "extremely-long", "text", "blob").asCollection();

    public static int getQueryBatchSize()
    {
        String size = null;
        try
        {
            size = ComponentAccessor.getApplicationProperties().getDefaultBackedString(APKeys.DATABASE_QUERY_BATCH_SIZE);
            return Integer.parseInt(size);
        }
        catch (final NumberFormatException e)
        {
            log.error("Error while converting database query batch size '" + size + "'. Using default value of " + DEFAULT_DATABASE_QUERY_BATCH_SIZE);
            return DEFAULT_DATABASE_QUERY_BATCH_SIZE;
        }
    }

    private final DelegatorInterface delegatorInterface;
    private final FieldSupportValidator findByValidator;

    public DefaultOfBizDelegator(final DelegatorInterface delegatorInterface)
    {
        this.delegatorInterface = delegatorInterface;
        findByValidator = new FieldSupportValidator("findBy", UNSUPPORTED_TYPES_FOR_FINDBY, new FieldTypeResolver());
    }

    public List<GenericValue> findByField(final String entityName, final String fieldName, final Object fieldValue)
    {
        return findByAnd(entityName, new FieldMap(fieldName, fieldValue));
    }

    public List<GenericValue> findByField(final String entityName, final String fieldName, final Object fieldValue, final String orderBy)
    {
        return findByAnd(entityName, new FieldMap(fieldName, fieldValue), CollectionBuilder.list(orderBy));
    }

    public List<GenericValue> findByAnd(final String entityName, final Map<String, ?> fields) throws DataAccessException
    {
        findByValidator.checkAll(entityName, fields.keySet());
        try
        {
            return delegatorInterface.findByAnd(entityName, fields);
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public List<GenericValue> findByAnd(final String entityName, final Map<String, ?> fields, final List<String> orderBy) throws DataAccessException
    {
        findByValidator.checkAll(entityName, fields.keySet());
        try
        {
            return delegatorInterface.findByAnd(entityName, fields, orderBy);
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public List<GenericValue> findByAnd(final String entityName, final List<EntityCondition> expressions) throws DataAccessException
    {
        // cannot check EntityCondition instances as they don't give us a where condition
        try
        {
            return delegatorInterface.findByAnd(entityName, expressions);
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public List<GenericValue> findByOr(final String entityName, final List expressions, final List orderBy) throws DataAccessException
    {
        try
        {
            if (orderBy != null)
            {
                return delegatorInterface.findByOr(entityName, expressions, orderBy);
            }
            else
            {
                return delegatorInterface.findByOr(entityName, expressions);
            }
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public List<GenericValue> findByLike(final String s, final Map<String, ?> map) throws DataAccessException
    {
        return findByLike(s, map, Collections.<String> emptyList());
    }

    public List<GenericValue> findByLike(final String s, final Map<String, ?> map, final List<String> orderBy) throws DataAccessException
    {
        try
        {
            return delegatorInterface.findByLike(s, map, orderBy);
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public void removeAll(final List<GenericValue> genericValues) throws DataAccessException
    {
        try
        {
            delegatorInterface.removeAll(genericValues, false);
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    @Override
    public int removeByCondition(String entityName, EntityCondition condition) throws DataAccessException
    {
        try
        {
            return delegatorInterface.removeByCondition(entityName, condition, false);
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public List<GenericValue> findByCondition(final String entityName, final EntityCondition entityCondition, final Collection<String> fieldsToSelect)
    {
        return findByCondition(entityName, entityCondition, fieldsToSelect, Collections.<String> emptyList());
    }

    public List<GenericValue> findByCondition(final String entityName, final EntityCondition entityCondition, final Collection<String> fieldsToSelect, final List<String> orderBy)
    {
        try
        {
            return delegatorInterface.findByCondition(entityName, entityCondition, fieldsToSelect, orderBy);
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public int removeByOr(final String entityName, final String entityId, final List<Long> ids) throws DataAccessException, GenericModelException
    {
        int result = 0;

        final ModelEntity modelEntity = delegatorInterface.getModelEntity(entityName);

        if (modelEntity == null)
        {
            throw new GenericModelException("The entityName passed in was not valid: " + entityName);
        }
        else if (!modelEntity.isField(entityId))
        {
            throw new GenericModelException("The entityId passed in was not valid for the given entity: " + entityId);
        }
        final ModelField modelField = modelEntity.getField(entityId);

        try
        {
            final GenericHelper entityHelper = delegatorInterface.getEntityHelper(entityName);
            // Generate SQL
            final StringBuilder removeSql = new StringBuilder("DELETE FROM ");

            removeSql.append(modelEntity.getTableName(entityHelper.getHelperName()));
            removeSql.append(" WHERE ");
            removeSql.append(modelField.getColName());
            removeSql.append(" IN (");

            final int idsSize = ids.size();
            final ArrayList<Long> idParams = new ArrayList<Long>();
            StringBuilder idClause = new StringBuilder();

            // batch the update
            final int batchSize = getQueryBatchSize();
            int batchIndex = 0;
            for (int i = 0; i < idsSize; i++)
            {
                idParams.add(ids.get(i));
                idClause.append("?");

                final boolean isEndOfBatch = (batchIndex == batchSize - 1);
                final boolean isEndOfIdList = (i == idsSize - 1);

                if (isEndOfBatch || isEndOfIdList)
                {
                    final SQLProcessor processor = new AutoCommitSQLProcessor(entityHelper.getHelperName());
                    // finish batch
                    idClause.append(")");
                    try
                    {
                        processor.prepareStatement(removeSql.toString() + idClause.toString());
                        for (final Long idParam : idParams)
                        {
                            processor.setValue(idParam);
                        }
                        // execute update
                        result += processor.executeUpdate();

                        // clean-up for the next batch
                        idParams.clear();
                        idClause = new StringBuilder();
                        batchIndex = 0;
                    }
                    finally
                    {
                        try
                        {
                            processor.close();
                        }
                        catch (final GenericDataSourceException e)
                        {
                            log.warn("Could not close the SQLProcessor", e);
                        }
                    }
                }
                else
                {
                    // add to this batch
                    idClause.append(", ");
                    batchIndex++;
                }
            }
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
        catch (final SQLException e)
        {
            throw new DataAccessException(e);
        }

        return result;
    }

    public int removeByAnd(final String entityName, final Map map) throws DataAccessException
    {
        try
        {
            return delegatorInterface.removeByAnd(entityName, map);
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    @Override
    public int removeById(String entityName, Long id)
    {
        return removeByAnd(entityName, FieldMap.build("id", id));
    }

    public int removeValue(final GenericValue value) throws DataAccessException
    {
        try
        {
            return delegatorInterface.removeValue(value);
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public void storeAll(final List genericValues) throws DataAccessException
    {
        try
        {
            delegatorInterface.storeAll(genericValues);
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public List<GenericValue> findAll(final String s)
    {
        try
        {
            return delegatorInterface.findAll(s);
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public List<GenericValue> findAll(final String s, final List sortOrder) throws DataAccessException
    {
        try
        {
            return delegatorInterface.findAll(s, sortOrder);
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public void store(final GenericValue gv) throws DataAccessException
    {
        try
        {
            delegatorInterface.store(gv);
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public GenericValue createValue(final String entityName, final Map<String, Object> fields)
    {
        try
        {
            final Map<String, Object> params = (fields == null) ? new HashMap<String, Object>(2) : new HashMap<String, Object>(fields);
            if (params.get("id") == null)
            {
                final Long id = delegatorInterface.getNextSeqId(entityName);
                params.put("id", id);
            }

            final GenericValue v = delegatorInterface.makeValue(entityName, params);
            v.create();
            return v;
        }
        catch (final GenericEntityException ex)
        {
            throw new DataAccessException(ex);
        }
    }

    @Override
    public void createValueWithoutId(final String entityName, final Map<String, Object> fields)
            throws DataAccessException
    {
        try
        {
            final GenericValue v = delegatorInterface.makeValue(entityName, fields);
            v.create();
        }
        catch (final GenericEntityException ex)
        {
            throw new DataAccessException(ex);
        }
    }

    public GenericValue makeValue(final String entityName)
    {
        return delegatorInterface.makeValue(entityName, null);
    }

    @Override
    public GenericValue makeValue(String entityName, Map<String, Object> fields)
    {
        return delegatorInterface.makeValue(entityName, fields);
    }

    @Override
    public GenericValue findById(String entityName, Long id) throws DataAccessException
    {
        return findByPrimaryKey(entityName, id);
    }

    public GenericValue findByPrimaryKey(final String entityName, final Long id)
    {
        // Build up the Map for the caller
        final Map<String, Object> fields = new HashMap<String, Object>(2);
        fields.put("id", id);
        // and delegate to the original findByPrimaryKey() method.
        return findByPrimaryKey(entityName, fields);
    }

    public GenericValue findByPrimaryKey(final String entityName, final Map<String,?> fields)
    {
        final long start = System.currentTimeMillis();
        try
        {
            return delegatorInterface.findByPrimaryKey(entityName, fields);
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
        finally
        {
            ThreadLocalQueryProfiler.store("OfBizDelegator", "findByPrimaryKey", System.currentTimeMillis() - start);
        }
    }

    public List<GenericValue> getRelated(final String relationName, final GenericValue gv)
    {
        try
        {
            return delegatorInterface.getRelated(relationName, gv);
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    @Override
    public List<GenericValue> getRelated(final String relationName, final GenericValue gv, final List<String> orderBy)
            throws DataAccessException
    {
        try
        {
            return delegatorInterface.getRelatedOrderBy(relationName, orderBy, gv);
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    @Override
    public long getCount(final String entityName)
    {
        // run a count with no where clause
        return getCountByAnd(entityName, new FieldMap());
    }

    @Override
    public long getCountByAnd(final String entityName, final Map<String, ?> fields)
    {
        try
        {
            final EntityCondition condition = new EntityFieldMap(fields, EntityOperator.AND);
            final GenericValue countGV = EntityUtil.getOnly(delegatorInterface.findByCondition(entityName + "Count", condition,
                    ImmutableList.of(COUNT_FIELD_NAME), null));
            return countGV.getLong(COUNT_FIELD_NAME);
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public OfBizListIterator findListIteratorByCondition(final String entityType, final EntityCondition condition)
    {
        try
        {
            return new DefaultOfBizListIterator(delegatorInterface.findListIteratorByCondition(entityType, condition, null, null));
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    /**
     * Always close the iterator returned from this method when finished.
     *
     * @return OfBizListIterator
     */
    public OfBizListIterator findListIteratorByCondition(final String entityName, final EntityCondition whereEntityCondition, final EntityCondition havingEntityCondition, final Collection fieldsToSelect, final List orderBy, final EntityFindOptions entityFindOptions)
    {
        try
        {
            return new DefaultOfBizListIterator(delegatorInterface.findListIteratorByCondition(entityName, whereEntityCondition,
                havingEntityCondition, fieldsToSelect, orderBy, entityFindOptions));
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public int bulkUpdateByPrimaryKey(final String entityName, final Map<String, ?> updateValues, final List<Long> keys)
    {
        int result = 0;

        if ((entityName == null) || (updateValues == null) || updateValues.isEmpty() || (keys == null) || keys.isEmpty())
        {
            return 0;
        }

        try
        {
            final GenericHelper entityHelper = delegatorInterface.getEntityHelper(entityName);
            final ModelEntity modelEntity = delegatorInterface.getModelEntity(entityName);

            final List<String> pks = modelEntity.getPkFieldNames();
            if (pks.size() != 1)
            {
                throw new DataAccessException("BulkUpdateByPrimaryKey only works for single column keys at this moment.");
            }
            final String pkName = pks.get(0);

            final Updater updater = new Updater(ModelFieldTypeReader.getModelFieldTypeReader(entityHelper.getHelperName()), entityName);
            final List<Updater.Value> params = new ArrayList<Updater.Value>();

            final StringBuilder updateSql = new StringBuilder("UPDATE ");

            updateSql.append(modelEntity.getTableName(entityHelper.getHelperName()));
            updateSql.append(" SET ");
            // generate the update sql
            for (final Iterator<String> iterator = updateValues.keySet().iterator(); iterator.hasNext();)
            {
                final String column = iterator.next();
                updateSql.append(" ");
                final ModelField field = modelEntity.getField(column);
                updateSql.append(field.getColName());
                updateSql.append(" = ");
                params.add(updater.create(field, updateValues.get(column)));
                updateSql.append("? ");
                if (iterator.hasNext())
                {
                    updateSql.append(", ");
                }
            }

            // generate the where clause
            updateSql.append(" WHERE ");

            // batch the update
            final int batchSize = getQueryBatchSize();

            int currentIndex = 0;

            while (currentIndex < keys.size())
            {
                int i = 0;
                final StringBuilder idClause = new StringBuilder();
                final ArrayList<Long> idParams = new ArrayList<Long>();
                for (final Iterator<Long> iterator = keys.subList(currentIndex, keys.size()).iterator(); iterator.hasNext() && (i < batchSize); i++)
                {
                    final Long key = iterator.next();
                    idClause.append(" ");
                    idClause.append(pkName);
                    idClause.append(" = ");
                    idParams.add(key);
                    idClause.append("? ");

                    if (iterator.hasNext() && ((i + 1) < batchSize))
                    {
                        idClause.append(" or ");
                    }
                }

                final SQLProcessor processor = new AutoCommitSQLProcessor(entityHelper.getHelperName());
                processor.prepareStatement(updateSql.toString() + idClause.toString());
                for (final Updater.Value param : params)
                {
                    param.setValue(processor);
                }
                for (final Long idParam : idParams)
                {
                    processor.setValue(idParam);
                }

                try
                {
                    result = processor.executeUpdate();
                }
                finally
                {
                    processor.close();
                }
                currentIndex += i;
            }
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
        catch (final SQLException e)
        {
            throw new DataAccessException(e);
        }
        catch (final NoClassDefFoundError e)
        {
            // under JDK 1.3 unit tests - javax.sql.XADataSource cannot be found.
            // this shouldn't affect runtime - application servers should ship the jar
        }
        return result;
    }

    public int bulkUpdateByAnd(final String entityName, final Map<String, ?> updateValues, final Map<String, ?> criteria)
    {
        int result = 0;

        if ((entityName == null) || (updateValues == null) || updateValues.isEmpty())
        {
            return 0;
        }

        try
        {
            final ModelEntity modelEntity = delegatorInterface.getModelEntity(entityName);

            final GenericHelper entityHelper = delegatorInterface.getEntityHelper(entityName);

            final ModelFieldTypeReader modelFieldTypeReader = ModelFieldTypeReader.getModelFieldTypeReader(entityHelper.getHelperName());

            final ArrayList<EntityConditionParam> params = new ArrayList<EntityConditionParam>();

            // generate the update sql
            final StringBuilder updateSql = new StringBuilder("UPDATE ");

            updateSql.append(modelEntity.getTableName(entityHelper.getHelperName()));
            updateSql.append(" SET ");

            if (!modelEntity.areFields(updateValues.keySet()))
            {
                throw new GenericModelException("At least one of the passed fields for update is not valid: " + updateValues.keySet().toString());
            }

            for (final Iterator<String> iterator = updateValues.keySet().iterator(); iterator.hasNext();)
            {
                final String fieldName = iterator.next();
                updateSql.append(" ");
                final ModelField modelField = modelEntity.getField(fieldName);
                updateSql.append(modelField.getColName());
                updateSql.append(" = ");
                params.add(new EntityConditionParam(modelField, updateValues.get(fieldName)));
                updateSql.append("? ");
                if (iterator.hasNext())
                {
                    updateSql.append(", ");
                }
            }

            if ((criteria != null) && !criteria.isEmpty())
            {
                if (!modelEntity.areFields(criteria.keySet()))
                {
                    throw new GenericModelException("At least one of the passed fields is not valid: " + criteria.keySet().toString());
                }

                // generate the where clause
                final EntityFieldMap entityCondition = new EntityFieldMap(criteria, EntityOperator.AND);

                final String entityCondWhereString = entityCondition.makeWhereString(modelEntity, params);

                if (entityCondWhereString.length() > 0)
                {
                    updateSql.append(" WHERE ");
                    updateSql.append(entityCondWhereString);
                }
            }

            final SQLProcessor processor = new AutoCommitSQLProcessor(entityHelper.getHelperName());
            final String sql = updateSql.toString();

            if (log.isDebugEnabled())
            {
                log.debug("Running bulk update SQL: '" + sql + "'");
            }

            processor.prepareStatement(sql);

            for (final EntityConditionParam conditionParam : params)
            {
                SqlJdbcUtil.setValue(processor, conditionParam.getModelField(), modelEntity.getEntityName(), conditionParam.getFieldValue(),
                    modelFieldTypeReader);
            }

            try
            {
                result = processor.executeUpdate();
            }
            finally
            {
                processor.close();
            }
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
        catch (final NoClassDefFoundError e)
        {
            // under JDK 1.3 unit tests - javax.sql.XADataSource cannot be found.
            // this shouldn't affect runtime - application servers should ship the jar
        }

        return result;
    }

    public int bulkCopyColumnValuesByAnd(final String entityName, final Map updateColumns, final Map criteria)
    {
        int result = 0;

        if ((entityName == null) || (updateColumns == null) || updateColumns.isEmpty())
        {
            return 0;
        }

        try
        {
            final ModelEntity modelEntity = delegatorInterface.getModelEntity(entityName);

            final GenericHelper entityHelper = delegatorInterface.getEntityHelper(entityName);

            final ModelFieldTypeReader modelFieldTypeReader = ModelFieldTypeReader.getModelFieldTypeReader(entityHelper.getHelperName());

            final ArrayList<EntityConditionParam> params = new ArrayList<EntityConditionParam>();

            // generate the update sql
            final StringBuilder updateSql = new StringBuilder("UPDATE ");

            updateSql.append(modelEntity.getTableName(entityHelper.getHelperName()));
            updateSql.append(" SET ");

            if (!modelEntity.areFields(updateColumns.keySet()))
            {
                throw new GenericModelException("At least one of the passed fields for update is not valid: " + updateColumns.keySet().toString());
            }

            if (!modelEntity.areFields(updateColumns.values()))
            {
                throw new GenericModelException("At least one of the passed fields for update is not valid: " + updateColumns.values().toString());
            }

            for (final Iterator iterator = updateColumns.keySet().iterator(); iterator.hasNext();)
            {
                final String column = (String) iterator.next();
                updateSql.append(" ");
                final ModelField toModelField = modelEntity.getField(column);
                updateSql.append(toModelField.getColName());
                updateSql.append(" = ");
                final ModelField fromModelField = modelEntity.getField((String) updateColumns.get(column));
                updateSql.append(fromModelField.getColName());
                if (iterator.hasNext())
                {
                    updateSql.append(", ");
                }
            }

            if ((criteria != null) && !criteria.isEmpty())
            {
                if (!modelEntity.areFields(criteria.keySet()))
                {
                    throw new GenericModelException("At least one of the passed fields is not valid: " + criteria.keySet().toString());
                }

                // generate the where clause
                final EntityFieldMap entityCondition = new EntityFieldMap(criteria, EntityOperator.AND);

                final String entityCondWhereString = entityCondition.makeWhereString(modelEntity, params);

                if (entityCondWhereString.length() > 0)
                {
                    updateSql.append(" WHERE ");
                    updateSql.append(entityCondWhereString);
                }
            }

            final SQLProcessor processor = new AutoCommitSQLProcessor(entityHelper.getHelperName());
            final String sql = updateSql.toString();

            if (log.isDebugEnabled())
            {
                log.debug("Running bulk update SQL: '" + sql + '\'');
            }

            processor.prepareStatement(sql);

            for (final EntityConditionParam conditionParam : params)
            {
                SqlJdbcUtil.setValue(processor, conditionParam.getModelField(), modelEntity.getEntityName(), conditionParam.getFieldValue(),
                    modelFieldTypeReader);
            }

            try
            {
                result = processor.executeUpdate();
            }
            finally
            {
                processor.close();
            }
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
        catch (final NoClassDefFoundError e)
        {
            // under JDK 1.3 unit tests - javax.sql.XADataSource cannot be found.
            // this shouldn't affect runtime - application servers should ship the jar
        }

        return result;
    }

    @Override
    public ModelReader getModelReader()
    {
        return delegatorInterface.getModelReader();
    }

    @Override
    public void refreshSequencer()
    {
        delegatorInterface.refreshSequencer();
    }

    @Override
    public boolean removeRelated(String relationName, GenericValue schemeGv)
    {
        try
        {
            return delegatorInterface.removeRelated(relationName, schemeGv) > 0;
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    @Override
    public DelegatorInterface getDelegatorInterface()
    {
        return delegatorInterface;
    }

    @Override
    public List<GenericValue> transform(final String entityName, final EntityCondition entityCondition,
            final List<String> orderBy, final String lockField, final Transformation transformation)
    {
        try
        {
            return delegatorInterface.transform(entityName, entityCondition, orderBy, lockField, transformation);
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    @Override
    public GenericValue transformOne(final String entityName, final EntityCondition entityCondition,
            final String lockField, final Transformation transformation)
    {
        final List<GenericValue> transformedValues =
                transform(entityName, entityCondition, null, lockField, transformation);
        Validate.validState(transformedValues.size() == 1, "Expected one match for %s but found %d: %s",
                entityCondition, transformedValues.size(), transformedValues);
        return transformedValues.get(0);
    }

    /**
     * Class that holds all the information necessary to update a value in a SQLProcessor.
     * Instances of {@link Value} are used to hold field values.
     */
    static final class Updater
    {
        final ModelFieldTypeReader modelFieldTypeReader;
        final String entityName;

        Updater(@Nonnull final ModelFieldTypeReader modelFieldTypeReader, @Nonnull final String entityName)
        {
            this.modelFieldTypeReader = notNull("modelFieldTypeReader", modelFieldTypeReader);
            this.entityName = notNull("entityName", entityName);
        }

        public Value create(final ModelField field, final Object value)
        {
            return new Value(field, value);
        }

        class Value
        {
            final ModelField field;
            final Object value;

            Value(final ModelField field, final Object value)
            {
                this.field = field;
                this.value = value;
            }

            void setValue(final SQLProcessor processor) throws GenericEntityException
            {
                SqlJdbcUtil.setValue(processor, field, entityName, value, modelFieldTypeReader);
            }
        }
    }

    /**
     * Get the field type given a table and field name. Returns a null function if the table can't be found, and a null type string if the entity can't be found.
     */
    class FieldTypeResolver implements Function<String, Function<String, String>>
    {
        @Nullable
        public Function<String, String> get(final String entityName)
        {
            final ModelEntity table = delegatorInterface.getModelEntity(entityName);
            return (table != null) ? new FieldTypeResolverFunction(table) : null;
        }
    }

    static class FieldTypeResolverFunction implements Function<String,String>
    {
        private final ModelEntity table;

        FieldTypeResolverFunction(ModelEntity table)
        {
            this.table = table;
        }

        @Nullable
        @Override
        public String get(final String fieldName)
        {
            final ModelField field = table.getField(fieldName);
            return (field != null) ? field.getType() : null;
        }
    }
}
