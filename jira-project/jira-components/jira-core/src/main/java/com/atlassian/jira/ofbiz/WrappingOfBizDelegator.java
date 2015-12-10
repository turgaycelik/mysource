package com.atlassian.jira.ofbiz;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.exception.DataAccessException;

import com.google.common.annotations.VisibleForTesting;

import org.apache.commons.lang3.Validate;
import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityFindOptions;
import org.ofbiz.core.entity.GenericModelException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.entity.Transformation;
import org.ofbiz.core.entity.model.ModelReader;

import static com.atlassian.jira.entity.Entity.Name.ISSUE;
import static com.atlassian.jira.ofbiz.IssueGenericValueFactory.wrap;

/**
 * Wraps GenericValues returned by DefaultOfBizDelegator with IssueGenericValue
 *
 * @since v6.1
 */
public class WrappingOfBizDelegator implements OfBizDelegator
{
    final private OfBizDelegator delegate;

    @SuppressWarnings("unused")
    public WrappingOfBizDelegator(final DelegatorInterface delegatorInterface)
    {
        this(new DefaultOfBizDelegator(delegatorInterface));
    }

    @VisibleForTesting
    WrappingOfBizDelegator(final OfBizDelegator delegate)
    {
        Validate.notNull(delegate);
        this.delegate = delegate;
    }

    @Override
    public List<GenericValue> findByField(final String entityName, final String fieldName, final Object fieldValue)
    {
        final List<GenericValue> result = delegate.findByField(entityName, fieldName, fieldValue);
        if (entityName.equals(ISSUE))
        {
            return wrap(result);
        }
        else
        {
            return result;
        }
    }

    @Override
    public List<GenericValue> findByField(final String entityName, final String fieldName, final Object fieldValue, final String orderBy)
    {
        final List<GenericValue> result = delegate.findByField(entityName, fieldName, fieldValue, orderBy);
        if (entityName.equals(ISSUE))
        {
            return wrap(result);
        }
        else
        {
            return result;
        }
    }

    @Override
    public List<GenericValue> findByAnd(final String entityName, final Map<String, ?> fields) throws DataAccessException
    {
        final List<GenericValue> result = delegate.findByAnd(entityName, fields);
        if (entityName.equals(ISSUE))
        {
            return wrap(result);
        }
        else
        {
            return result;
        }
    }

    @Override
    public List<GenericValue> findByAnd(final String entityName, final Map<String, ?> fields, final List<String> orderBy)
            throws DataAccessException
    {
        final List<GenericValue> result = delegate.findByAnd(entityName, fields, orderBy);
        if (entityName.equals(ISSUE))
        {
            return wrap(result);
        }
        else
        {
            return result;
        }
    }

    @Override
    public List<GenericValue> findByAnd(final String entityName, final List<EntityCondition> expressions)
            throws DataAccessException
    {
        final List<GenericValue> result = delegate.findByAnd(entityName, expressions);
        if (entityName.equals(ISSUE))
        {
            return wrap(result);
        }
        else
        {
            return result;
        }
    }

    @Override
    public List<GenericValue> findByOr(final String entityName, final List<? extends EntityCondition> expressions, final List<String> orderBy)
            throws DataAccessException
    {
        final List<GenericValue> result = delegate.findByOr(entityName, expressions, orderBy);
        if (entityName.equals(ISSUE))
        {
            return wrap(result);
        }
        else
        {
            return result;
        }
    }

    @Override
    public List<GenericValue> findByLike(final String entityName, final Map<String, ?> map) throws DataAccessException
    {
        final List<GenericValue> result = delegate.findByLike(entityName, map);
        if (entityName.equals(ISSUE))
        {
            return wrap(result);
        }
        else
        {
            return result;
        }
    }

    @Override
    public List<GenericValue> findByLike(final String entityName, final Map<String, ?> map, final List<String> orderBy)
            throws DataAccessException
    {
        final List<GenericValue> result = delegate.findByLike(entityName, map, orderBy);
        if (entityName.equals(ISSUE))
        {
            return wrap(result);
        }
        else
        {
            return result;
        }
    }

    @Override
    public void removeAll(final List<GenericValue> genericValues) throws DataAccessException
    {
        delegate.removeAll(genericValues);
    }

    @Override
    public int removeByAnd(final String s, final Map<String, ?> map) throws DataAccessException
    {
        return delegate.removeByAnd(s, map);
    }

    @Override
    public int removeByCondition(final String entityName, final EntityCondition condition) throws DataAccessException
    {
        return delegate.removeByCondition(entityName, condition);
    }

    @Override
    public int removeById(final String entityName, final Long id)
    {
        return delegate.removeById(entityName, id);
    }

    @Override
    public int removeValue(final GenericValue value) throws DataAccessException
    {
        return delegate.removeValue(value);
    }

    @Override
    public void storeAll(final List<GenericValue> genericValues) throws DataAccessException
    {
        delegate.storeAll(genericValues);
    }

    @Override
    public List<GenericValue> findAll(final String s) throws DataAccessException
    {
        return wrap(delegate.findAll(s));
    }

    @Override
    public List<GenericValue> findAll(final String s, final List<String> orderBy) throws DataAccessException
    {
        return wrap(delegate.findAll(s, orderBy));
    }

    @Override
    public void store(final GenericValue gv) throws DataAccessException
    {
        delegate.store(gv);
    }

    @Override
    public GenericValue createValue(final String entityName, final Map<String, Object> fields)
            throws DataAccessException
    {
        return wrap(delegate.createValue(entityName, fields));
    }

    @Override
    public void createValueWithoutId(final String entityName, final Map<String, Object> fields)
    {
        delegate.createValueWithoutId(entityName, fields);
    }

    @Override
    public GenericValue makeValue(final String entityName)
    {
        return wrap(delegate.makeValue(entityName));
    }

    @Override
    public GenericValue makeValue(final String entityName, final Map<String, Object> fields)
    {
        return wrap(delegate.makeValue(entityName, fields));
    }

    @Override
    public GenericValue findById(final String entityName, final Long id) throws DataAccessException
    {
        return wrap(delegate.findById(entityName, id));
    }

    @Override
    public GenericValue findByPrimaryKey(final String entityName, final Long id) throws DataAccessException
    {
        return wrap(delegate.findByPrimaryKey(entityName, id));
    }

    @Override
    public GenericValue findByPrimaryKey(final String entityName, final Map<String, ?> fields)
            throws DataAccessException
    {
        return wrap(delegate.findByPrimaryKey(entityName, fields));
    }

    @Override
    public List<GenericValue> getRelated(final String relationName, final GenericValue gv) throws DataAccessException
    {
        return wrap(delegate.getRelated(relationName, gv));
    }

    @Override
    public List<GenericValue> getRelated(final String relationName, final GenericValue gv,  final List<String> orderBy)
            throws DataAccessException
    {
        return wrap(delegate.getRelated(relationName, gv, orderBy));
    }

    @Override
    public boolean removeRelated(final String relationName, final GenericValue schemeGv)
    {
        return delegate.removeRelated(relationName, schemeGv);
    }

    @Override
    public long getCount(final String entityName) throws DataAccessException
    {
        return delegate.getCount(entityName);
    }

    @Override
    public long getCountByAnd(final String entityName, final Map<String, ?> fields)
    {
        return delegate.getCountByAnd(entityName, fields);
    }

    @Override
    public List<GenericValue> findByCondition(final String entityName, final EntityCondition entityCondition, final Collection<String> fieldsToSelect, final List<String> orderBy)
            throws DataAccessException
    {
        final List<GenericValue> result = delegate.findByCondition(entityName, entityCondition, fieldsToSelect, orderBy);
        if (entityName.equals(ISSUE))
        {
            return wrap(result);
        }
        else
        {
            return result;
        }
    }

    @Override
    public List<GenericValue> findByCondition(final String entityName, final EntityCondition entityCondition, final Collection<String> fieldsToSelect)
            throws DataAccessException
    {
        final List<GenericValue> result = delegate.findByCondition(entityName, entityCondition, fieldsToSelect);
        if (entityName.equals(ISSUE))
        {
            return wrap(result);
        }
        else
        {
            return result;
        }
    }

    @Override
    public ModelReader getModelReader()
    {
        return delegate.getModelReader();
    }

    @Override
    public void refreshSequencer()
    {
        delegate.refreshSequencer();
    }

    @Override
    public DelegatorInterface getDelegatorInterface()
    {
        return delegate.getDelegatorInterface();
    }

    @Override
    public OfBizListIterator findListIteratorByCondition(final String entityType, final EntityCondition condition)
            throws DataAccessException
    {
        return new WrappingOfBizListIterator(delegate.findListIteratorByCondition(entityType, condition));
    }

    @Override
    public OfBizListIterator findListIteratorByCondition(final String entityName, final EntityCondition whereEntityCondition, final EntityCondition havingEntityCondition, final Collection<String> fieldsToSelect, final List<String> orderBy, final EntityFindOptions entityFindOptions)
            throws DataAccessException
    {
        return new WrappingOfBizListIterator(delegate.findListIteratorByCondition(entityName, whereEntityCondition, havingEntityCondition, fieldsToSelect, orderBy, entityFindOptions));
    }

    @Override
    public int bulkUpdateByPrimaryKey(final String entityName, final Map<String, ?> updateValues, final List<Long> keys)
            throws DataAccessException
    {
        return delegate.bulkUpdateByPrimaryKey(entityName, updateValues, keys);
    }

    @Override
    public int bulkUpdateByAnd(final String entityName, final Map<String, ?> updateValues, final Map<String, ?> criteria)
            throws DataAccessException
    {
        return delegate.bulkUpdateByAnd(entityName, updateValues, criteria);
    }

    @Override
    public int bulkCopyColumnValuesByAnd(final String entityName, final Map updateColumns, final Map criteria)
            throws DataAccessException
    {
        return delegate.bulkCopyColumnValuesByAnd(entityName, updateColumns, criteria);
    }

    @Override
    public int removeByOr(final String entityName, final String entityId, final List<Long> ids)
            throws DataAccessException, GenericModelException
    {
        return delegate.removeByOr(entityName, entityId, ids);
    }

    @Override
    public List<GenericValue> transform(final String entityName, final EntityCondition entityCondition,
            final List<String> orderBy, final String lockField, final Transformation transformation)
    {
        final List<GenericValue> result =
                delegate.transform(entityName, entityCondition, orderBy, lockField, transformation);
        if (ISSUE.equals(entityName))
        {
            return wrap(result);
        }
        else
        {
            return result;
        }
    }

    @Override
    public GenericValue transformOne(final String entityName, final EntityCondition entityCondition,
            final String lockField, final Transformation transformation)
    {
        return wrap(delegate.transformOne(entityName, entityCondition, lockField, transformation));
    }
}
