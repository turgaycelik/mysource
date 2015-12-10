package com.atlassian.jira.entity;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.google.common.collect.ImmutableSet;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityFieldMap;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.EntityWhereString;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EntityEngineImpl implements  EntityEngine
{
    private OfBizDelegator ofBizDelegator;

    public EntityEngineImpl(OfBizDelegator ofBizDelegator)
    {
        this.ofBizDelegator = ofBizDelegator;
    }

    @Override
    public <E> E createValue(EntityFactory<E> entityFactory, E value)
    {
        Map<String, Object> fieldMap = entityFactory.fieldMapFrom(value);
        GenericValue gv = ofBizDelegator.createValue(entityFactory.getEntityName(), fieldMap);
        // rebuild the entity to get the new ID
        return entityFactory.build(gv);
    }

    @Override
    public <E> void createValueWithoutId(EntityFactory<E> entityFactory, E value)
    {
        Map<String, Object> fieldMap = entityFactory.fieldMapFrom(value);
        ofBizDelegator.createValueWithoutId(entityFactory.getEntityName(), fieldMap);
    }

    @Override
    public <E> void updateValue(EntityFactory<E> entityFactory, E newValue)
    {
        // Create a generic value from the passed entity
        GenericValue genericValue = ofBizDelegator.makeValue(entityFactory.getEntityName(), entityFactory.fieldMapFrom(newValue));
        // store it
        ofBizDelegator.store(genericValue);
    }

    @Override
    public int execute(Update.WhereContext updateContext)
    {
        return updateContext.execute(ofBizDelegator);
    }

    @Override
    public int delete(Delete.DeleteWhereContext deleteContext)
    {
        return deleteContext.execute(ofBizDelegator);
    }

    @Override
    public <E> SelectQuery.ExecutionContext<E> run(SelectQuery<E> selectQuery)
    {
        return selectQuery.runWith(ofBizDelegator);
    }

    @Override
    public <E> int removeValue(EntityFactory<E> entityFactory, Long id)
    {
        return ofBizDelegator.removeById(entityFactory.getEntityName(), id);
    }

    @Override
    public <E> SelectFromContext<E> selectFrom(EntityFactory<E> entity)
    {
        return new SelectFromContextImpl<E>(entity);
    }

    private class SelectFromContextImpl<E> implements SelectFromContext<E>
    {
        private final EntityFactory<E> entity;

        public SelectFromContextImpl(EntityFactory<E> entity)
        {
            this.entity = entity;
        }

        @Override
        public WhereContext<E> findAll()
        {
            return new WhereContextImpl<E>(entity, "");
        }

        @Override
        public E findById(Long id)
        {
            return whereEqual("id", id).singleValue();
        }

        @Override
        public WhereEqualContext<E> whereEqual(String fieldName, String value)
        {
            return new WhereEqualContextImpl<E>(entity, fieldName, value);
        }

        @Override
        public WhereEqualContext<E> whereEqual(String fieldName, Long value)
        {
            return new WhereEqualContextImpl<E>(entity, fieldName, value);
        }

        @Override
        public <V> WhereInContext<E> whereIn(final String fieldName, final Collection<V> values)
        {
            return new WhereInContextImpl<E, V>(entity, fieldName, values);
        }
    }

    private class WhereEqualContextImpl<E> extends AbstractWhereContext<E> implements WhereEqualContext<E>
    {
        private final FieldMap fieldMap;

        public WhereEqualContextImpl(EntityFactory<E> entity, String fieldName, Object value)
        {
            super(entity);
            this.fieldMap = new FieldMap(fieldName, value);
        }

        EntityCondition getEntityCondition()
        {
            return new EntityFieldMap(fieldMap, EntityOperator.AND);
        }

        @Override
        public WhereEqualAndContext<E> andEqual(String fieldName, String value)
        {
            fieldMap.add(fieldName, value);
            return new WhereEqualAndContextImpl<E>(entity, fieldMap);
        }

        @Override
        public WhereEqualAndContext<E> andEqual(String fieldName, Long value)
        {
            fieldMap.add(fieldName, value);
            return new WhereEqualAndContextImpl<E>(entity, fieldMap);
        }
    }

    private class WhereEqualAndContextImpl<E> extends AbstractWhereContext<E> implements WhereEqualContext<E>
    {
        private final FieldMap fieldMap;

        public WhereEqualAndContextImpl(EntityFactory<E> entity, FieldMap fieldMap)
        {
            super(entity);
            this.fieldMap = fieldMap;
        }

        EntityCondition getEntityCondition()
        {
            return new EntityFieldMap(fieldMap, EntityOperator.AND);
        }

        @Override
        public WhereEqualAndContext<E> andEqual(String fieldName, String value)
        {
            fieldMap.add(fieldName, value);
            return this;
        }

        @Override
        public WhereEqualAndContext<E> andEqual(String fieldName, Long value)
        {
            fieldMap.add(fieldName, value);
            return this;
        }
    }

    private class WhereInContextImpl<E, V> extends AbstractWhereContext<E> implements WhereInContext<E>
    {
        private final String fieldName;
        private final Set<V> values;

        public WhereInContextImpl(final EntityFactory<E> entity, final String fieldName, final Collection<V> values)
        {
            super(entity);
            this.fieldName = fieldName;
            this.values = values == null ? ImmutableSet.<V>of() : ImmutableSet.copyOf(values);
        }

        @Override
        EntityCondition getEntityCondition()
        {
            return new EntityExpr(fieldName, EntityOperator.IN, values);
        }
    }

    private class WhereContextImpl<E> extends AbstractWhereContext<E> implements WhereContext<E>
    {
        private String whereClause;

        public WhereContextImpl(EntityFactory<E> entity, String whereClause)
        {
            super(entity);
            this.whereClause = whereClause;
        }

        EntityCondition getEntityCondition()
        {
            return new EntityWhereString(whereClause);
        }
    }

    private abstract class AbstractWhereContext<E> implements WhereContext<E>
    {
        final EntityFactory<E> entity;

        public AbstractWhereContext(EntityFactory<E> entity)
        {
            this.entity = entity;
        }

        @Override
        public List<E> orderBy(String... orderByColumn)
        {
            List<GenericValue> gvList = ofBizDelegator.findByCondition(entity.getEntityName(), getEntityCondition(), null, Arrays.asList(orderByColumn));
            return entity.buildList(gvList);
        }

        @Override
        public List<E> list()
        {
            List<GenericValue> gvList = ofBizDelegator.findByCondition(entity.getEntityName(), getEntityCondition(), null, null);
            return entity.buildList(gvList);
        }

        @Override
        public E singleValue()
        {
            List<GenericValue> gvList = runQuery();
            switch (gvList.size())
            {
                case 0:
                    return null;
                case 1:
                    return entity.build(gvList.get(0));
            }
            throw new IllegalStateException("Too many rows returned for query on entity '" + entity.getEntityName() +
                    "' condition: " + getEntityCondition());
        }

        private List<GenericValue> runQuery()
        {
            return ofBizDelegator.findByCondition(entity.getEntityName(), getEntityCondition(), null, null);
        }

        abstract EntityCondition getEntityCondition();
    }
}
