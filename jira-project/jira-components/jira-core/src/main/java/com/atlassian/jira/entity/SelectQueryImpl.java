package com.atlassian.jira.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.Visitor;

import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityConditionList;
import org.ofbiz.core.entity.EntityFieldMap;
import org.ofbiz.core.entity.EntityFindOptions;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

/**
 * @since v5.2
 */
public class SelectQueryImpl<E> implements SelectQuery<E>
{
    private final boolean distinct;
    private final Collection<String> fieldsToSelect;
    private final String entityName;
    private final EntityBuilder<E> entityBuilder;
    private final FieldMap whereEqual;
    private final List<EntityCondition> whereCondition;
    private final List<String> orderBy;
    private final int offset;
    private final int maxResults;

    public SelectQueryImpl(Select.QueryBuilder<E> builder)
    {
        this.distinct = builder.distinct;
        this.fieldsToSelect = builder.fieldsToSelect;
        this.entityName = builder.entityName;
        this.entityBuilder = builder.entityBuilder;
        this.whereEqual = builder.whereEqual;
        this.whereCondition = builder.whereCondition;
        this.orderBy = builder.orderBy;
        this.offset = builder.offset;
        this.maxResults = builder.maxResults;
    }

    @Override
    public ExecutionContext<E> runWith(OfBizDelegator ofBizDelegator)
    {
        return new ExecutionContextImpl<E>(ofBizDelegator, entityBuilder);
    }

    @Override
    public ExecutionContext<E> runWith(EntityEngine entityEngine)
    {
        return entityEngine.run(this);
    }

    private EntityCondition getWhereEntityCondition()
    {
        if (whereCondition == null)
        {
            if (whereEqual == null)
            {
                return null;
            }
            return new EntityFieldMap(whereEqual, EntityOperator.AND);
        }

        if (whereEqual == null)
        {
            return new EntityConditionList(whereCondition, EntityOperator.AND);
        }

        final List<EntityCondition> conditions = new ArrayList<EntityCondition>(whereCondition.size() + 1);
        conditions.add(new EntityFieldMap(whereEqual, EntityOperator.AND));
        conditions.addAll(whereCondition);
        return new EntityConditionList(conditions, EntityOperator.AND);
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(final Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        final SelectQueryImpl that = (SelectQueryImpl) o;

        if (distinct != that.distinct) { return false; }
        if (maxResults != that.maxResults) { return false; }
        if (offset != that.offset) { return false; }
        if (entityBuilder != null ? !entityBuilder.equals(that.entityBuilder) : that.entityBuilder != null)
        {
            return false;
        }
        if (entityName != null ? !entityName.equals(that.entityName) : that.entityName != null) { return false; }
        if (fieldsToSelect != null ? !fieldsToSelect.equals(that.fieldsToSelect) : that.fieldsToSelect != null) { return false; }
        if (orderBy != null ? !orderBy.equals(that.orderBy) : that.orderBy != null) { return false; }
        if (whereCondition != null ? !whereCondition.equals(that.whereCondition) : that.whereCondition != null) { return false; }
        if (whereEqual != null ? !whereEqual.equals(that.whereEqual) : that.whereEqual != null) { return false; }
        return true;
    }

    @Override
    public int hashCode()
    {
        int result = (distinct ? 1 : 0);
        result = 31 * result + (fieldsToSelect != null ? fieldsToSelect.hashCode() : 0);
        result = 31 * result + (entityName != null ? entityName.hashCode() : 0);
        result = 31 * result + (entityBuilder != null ? entityBuilder.hashCode() : 0);
        result = 31 * result + (whereEqual != null ? whereEqual.hashCode() : 0);
        result = 31 * result + (whereCondition != null ? whereCondition.hashCode() : 0);
        result = 31 * result + (orderBy != null ? orderBy.hashCode() : 0);
        result = 31 * result + offset;
        result = 31 * result + maxResults;
        return result;
    }

    @Override
    public String toString()
    {
        return "SelectQueryImpl[distinct=" + distinct +
                ",fieldsToSelect=" + fieldsToSelect +
                ",entityName=" + entityName +
                ",entityBuilder=" + entityBuilder +
                ",whereEqual=" + whereEqual +
                ",whereCondition=" + whereCondition +
                ",orderBy=" + orderBy +
                ",offset=" + offset +
                ",maxResults=" + maxResults +
                ']';
    }

    @SuppressWarnings ("UnusedDeclaration")
    public final class ExecutionContextImpl<E> implements ExecutionContext<E>
    {
        private OfBizDelegator ofBizDelegator;
        private final EntityBuilder<E> entityBuilder;

        public ExecutionContextImpl(OfBizDelegator ofBizDelegator, EntityBuilder<E> entityBuilder)
        {
            this.ofBizDelegator = ofBizDelegator;
            this.entityBuilder = entityBuilder;
        }

        @Nonnull
        @Override
        public List<E> asList()
        {
            return consumeWith(new EntityListConsumer<E, List<E>>()
            {
                public List<E> list = new ArrayList<E>();

                @Override
                public void consume(E entity)
                {
                    list.add(entity);
                }

                @Override
                public List<E> result()
                {
                    return list;
                }
            });
        }

        @Nonnull
        public <R> List<R> asList(@Nonnull final Function<E,R> mappingFunction)
        {
            return consumeWith(new EntityListConsumer<E, List<R>>()
            {
                public List<R> list = new ArrayList<R>();

                @Override
                public void consume(final E entity)
                {
                    list.add(mappingFunction.get(entity));
                }

                @Override
                public List<R> result()
                {
                    return list;
                }
            });
        }

        @Nullable
        @Override
        public E singleValue() throws IllegalStateException
        {
            return consumeWith(new EntityListConsumer<E, E>()
            {
                private E value = null;
                private boolean found = false;

                @Override
                public void consume(E entity)
                {
                    if (found)
                    {
                        throw new IllegalStateException("Too many rows found for query on " + entityName +
                                "\n\trow1: " + value + "row2: \n\t" + entity);
                    }
                    value = entity;
                    found = true;
                }

                @Override
                public E result()
                {
                    return value;
                }
            });
        }

        @Override
        public <R> R consumeWith(@Nonnull final EntityListConsumer<E, R> consumer)
        {
            final EntityFindOptions entityFindOptions = new EntityFindOptions();
            entityFindOptions.setDistinct(distinct);
            entityFindOptions.setOffset(offset);
            entityFindOptions.setMaxResults(maxResults);

            // Run query
            final OfBizListIterator ofBizListIterator = ofBizDelegator.findListIteratorByCondition(entityName,
                    getWhereEntityCondition(), null, fieldsToSelect, orderBy, entityFindOptions);
            try
            {
                for (GenericValue genericValue : ofBizListIterator)
                {
                    consumer.consume(entityBuilder.build(genericValue));
                }
            }
            finally
            {
                ofBizListIterator.close();
            }
            return consumer.result();
        }

        @Override
        public void visitWith(@Nonnull final Visitor<E> visitor)
        {
            consumeWith(new EntityListConsumer<E, Void>()
            {
                @Override
                public void consume(E entity)
                {
                    visitor.visit(entity);
                }

                @Nullable
                @Override
                public Void result()
                {
                    return null;
                }
            });
        }

        @Override
        public long count()
        {
            final EntityFindOptions entityFindOptions = new EntityFindOptions();
            entityFindOptions.setDistinct(distinct);
            try
            {
                // Doesn't pay any attention to offset, maxResults, or orderBy -- they make no sense on a count, anyway
                return ofBizDelegator.getDelegatorInterface().countByCondition(entityName, null, getWhereEntityCondition(),
                        entityFindOptions);
            }
            catch (GenericEntityException gee)
            {
                throw new DataAccessException(gee);
            }
        }

        @Override
        public String toString()
        {
            return "ExecutionContextImpl[" + SelectQueryImpl.this + ']';
        }
    }
}
