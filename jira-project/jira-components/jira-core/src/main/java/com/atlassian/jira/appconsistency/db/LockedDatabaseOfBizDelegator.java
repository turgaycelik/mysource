package com.atlassian.jira.appconsistency.db;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;

import com.google.common.annotations.VisibleForTesting;

import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityFindOptions;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.entity.Transformation;
import org.ofbiz.core.entity.model.ModelReader;

/**
 * An {@link OfBizDelegator} that rejects all operations on the basis that the database is locked.
 */
public class LockedDatabaseOfBizDelegator implements OfBizDelegator
{
    @VisibleForTesting
    static final String MESSAGE = "Database is locked";

    public List<GenericValue> findByField(final String entityName, final String fieldName, final Object fieldValue)
    {
        throw new UnsupportedOperationException(MESSAGE);
    }

    public List<GenericValue> findByField(final String entityName, final String fieldName, final Object fieldValue, final String orderBy)
    {
        throw new UnsupportedOperationException(MESSAGE);
    }

    @Override
    public int removeByCondition(String entityName, EntityCondition condition) throws DataAccessException
    {
        throw new UnsupportedOperationException(MESSAGE);
    }

    public int bulkUpdateByAnd(final String entityName, final Map updateValues, final Map criteria)
    {
        throw new UnsupportedOperationException(MESSAGE);
    }

    public int bulkUpdateByPrimaryKey(final String entityName, final Map updateValues, final List keys)
    {
        throw new UnsupportedOperationException(MESSAGE);
    }

    public GenericValue createValue(final String entity, final Map params) throws DataAccessException
    {
        throw new UnsupportedOperationException(MESSAGE);
    }

    public List<GenericValue> findAll(final String s) throws DataAccessException
    {
        throw new UnsupportedOperationException(MESSAGE);
    }

    public List<GenericValue> findAll(final String s, final List sortOrder) throws DataAccessException
    {
        throw new UnsupportedOperationException(MESSAGE);
    }

    public List<GenericValue> findByAnd(final String s, final List expressions) throws DataAccessException
    {
        throw new UnsupportedOperationException(MESSAGE);
    }

    public List<GenericValue> findByAnd(final String s, final Map map) throws DataAccessException
    {
        throw new UnsupportedOperationException(MESSAGE);
    }

    public List<GenericValue> findByAnd(final String s, final Map map, final List orderClause) throws DataAccessException
    {
        throw new UnsupportedOperationException(MESSAGE);
    }

    public List<GenericValue> findByLike(final String s, final Map map) throws DataAccessException
    {
        throw new UnsupportedOperationException(MESSAGE);
    }

    public List<GenericValue> findByLike(final String s, final Map map, final List orderBy) throws DataAccessException
    {
        throw new UnsupportedOperationException(MESSAGE);
    }

    public List<GenericValue> findByOr(final String entityName, final List expressions, final List orderBy) throws DataAccessException
    {
        throw new UnsupportedOperationException(MESSAGE);
    }

    public GenericValue findByPrimaryKey(final String s, final Long id)
    {
        throw new UnsupportedOperationException(MESSAGE);
    }

    public GenericValue findByPrimaryKey(final String s, final Map map)
    {
        throw new UnsupportedOperationException(MESSAGE);
    }

    public OfBizListIterator findListIteratorByCondition(final String entityName, final EntityCondition whereEntityCondition, final EntityCondition havingEntityCondition, final Collection fieldsToSelect, final List orderBy, final EntityFindOptions entityFindOptions)
    {
        throw new UnsupportedOperationException(MESSAGE);
    }

    public OfBizListIterator findListIteratorByCondition(final String entityType, final EntityCondition condition)
    {
        throw new UnsupportedOperationException(MESSAGE);
    }

    public long getCount(final String entityName) throws DataAccessException
    {
        throw new UnsupportedOperationException(MESSAGE);
    }

    public long getCountByAnd(final String entityName, final Map<String, ?> fields)
    {
        throw new UnsupportedOperationException(MESSAGE);
    }

    public List<GenericValue> getRelated(final String relationName, final GenericValue gv)
    {
        throw new UnsupportedOperationException(MESSAGE);
    }

    public List<GenericValue> getRelated(final String relationName, final GenericValue gv, final List<String> orderBy)
            throws DataAccessException
    {
        throw new UnsupportedOperationException(MESSAGE);
    }

    public void removeAll(final List genericValues) throws DataAccessException
    {
        throw new UnsupportedOperationException(MESSAGE);
    }

    public int removeByAnd(final String s, final Map map) throws DataAccessException
    {
        throw new UnsupportedOperationException(MESSAGE);
    }

    @Override
    public int removeById(String entityName, Long id)
    {
        throw new UnsupportedOperationException(MESSAGE);
    }

    public int removeValue(final GenericValue value) throws DataAccessException
    {
        throw new UnsupportedOperationException(MESSAGE);
    }

    public GenericValue makeValue(final String entity)
    {
        throw new UnsupportedOperationException(MESSAGE);
    }

    @Override
    public GenericValue makeValue(String entityName, Map<String, Object> fields)
    {
        throw new UnsupportedOperationException(MESSAGE);
    }

    @Override
    public GenericValue findById(String entityName, Long id) throws DataAccessException
    {
        throw new UnsupportedOperationException(MESSAGE);
    }

    public void store(final GenericValue gv) throws DataAccessException
    {
        throw new UnsupportedOperationException(MESSAGE);
    }

    @Override
    public void createValueWithoutId(final String entityName, final Map<String, Object> fields)
    {
        throw new UnsupportedOperationException(MESSAGE);
    }

    public void storeAll(final List genericValues) throws DataAccessException
    {
        throw new UnsupportedOperationException(MESSAGE);
    }

    public int removeByOr(final String entityName, final String entityId, final List ids)
    {
        throw new UnsupportedOperationException(MESSAGE);
    }

    public List<GenericValue> findByCondition(final String entityName, final EntityCondition entityCondition, final Collection fieldsToSelect)
    {
        throw new UnsupportedOperationException(MESSAGE);
    }

    public List<GenericValue> findByCondition(final String entityName, final EntityCondition entityCondition, final Collection fieldsToSelect, final List orderBy)
    {
        throw new UnsupportedOperationException(MESSAGE);
    }

    public int bulkCopyColumnValuesByAnd(final String entityName, final Map updateColumns, final Map criteria)
    {
        throw new UnsupportedOperationException(MESSAGE);
    }

    public ModelReader getModelReader()
    {
        throw new UnsupportedOperationException(MESSAGE);
    }

    @Override
    public void refreshSequencer()
    {
        throw new UnsupportedOperationException(MESSAGE);
    }

    @Override
    public DelegatorInterface getDelegatorInterface()
    {
        throw new UnsupportedOperationException(MESSAGE);
    }

    @Override
    public boolean removeRelated(String relationName, GenericValue schemeGv)
    {
        throw new UnsupportedOperationException(MESSAGE);
    }

    @Override
    public List<GenericValue> transform(final String entityName, final EntityCondition entityCondition,
            final List<String> orderBy, final String lockField, final Transformation transformation)
    {
        throw new UnsupportedOperationException(MESSAGE);
    }

    @Override
    public GenericValue transformOne(final String entityName, final EntityCondition entityCondition,
            final String lockField, final Transformation transformation)
    {
        throw new UnsupportedOperationException(MESSAGE);
    }
}
