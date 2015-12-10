package com.atlassian.jira.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;

import com.google.common.collect.ImmutableList;

import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Select is the entry point to building up a {@link SelectQuery} which can be run in OfBiz Entity Engine.
 * <p/>
 * eg
 * <pre>
 * {@literal
 * SelectQuery<GenericValue> query = Select.columns().from("FilterSubscription")
 *      .whereEqual("group", (String) null)
 *      .andEqual("username", username)
 *      .orderBy("id desc");
 * }
 * </pre>
 *
 * <p/>
 * If you are selecting a single column, then you can it can return String objects instead of GenericValues like
 * <pre>
 * {@literal
 * SelectQuery<String> query = Select.distinctString("username").from("FilterSubscription");
 * List<String> vals = query.runWith(delegator).asList();
 * }
 * </pre>
 *
 * <p/>
 * You can also use an {@link EntityFactory} to automatically convert the GenericValues to other entity objects.
 * <pre>
 * {@literal
 * List<ProjectCategory> categories = Select.from(Entity.PROJECT_CATEGORY).runWith(delegator).asList();
 * }
 * </pre>
 *
 * @since v5.2
 */
@SuppressWarnings("UnusedDeclaration")
public class Select
{
    private static final Collection<String> COUNT_FIELD_LIST = Collections.singleton(CountEntityBuilder.COUNT_FIELD_NAME);

    /**
     * Begins a {@code SELECT} query that will only retrieve the specified columns.
     *
     * @param columns the list of fields to retrieve; must not be {@code null} or contain any {@code null} values
     * @return a partially constructed query; use {@code .from(entityName)} to continue building it
     */
    public static SelectColumnsContext columns(List<String> columns)
    {
        return new SelectColumnsContext(columns, false);
    }

    /**
     * Begins a {@code SELECT} query that will only retrieve the specified columns.
     *
     * @param columns the fields to retrieve; must not be {@code null} or contain any {@code null} values
     * @return a partially constructed query; use {@code .from(entityName)} to continue building it
     */
    public static SelectColumnsContext columns(String... columns)
    {
        return new SelectColumnsContext(columns, false);
    }

    /**
     * Begins a {@code SELECT DISTICT} query that will only retrieve the specified columns.
     *
     * @param columns the fields to retrieve; must not be {@code null} or contain any {@code null} values
     * @return a partially constructed query; use {@code .from(entityName)} to continue building it
     */
    public static SelectColumnsContext distinct(String... columns)
    {
        return new SelectColumnsContext(columns, true);
    }

    /**
     * Begins a {@code SELECT} query that will only retrieve the {@code "id"} field, which must use {@code Long}
     * values.
     * <p>
     * <em>WARNING</em>: Not suitable for use with {@code "id"} columns that return a {@code String}, such as
     * those used for issue constants.
     * </p>
     *
     * @return a partially constructed query; use {@code .from(entityName)} to continue building it
     */
    public static SelectSingleColumnContext<Long> id()
    {
        return new SelectSingleColumnContext<Long>(IdEntityBuilder.ID, true, IdEntityBuilder.getInstance());
    }

    /**
     * Begins a {@code SELECT DISTICT} query that will only retrieve the specified {@code String} column.
     *
     * @param columnName the field to query
     * @return a partially constructed query; use {@code .from(entityName)} to continue building it
     */
    public static SelectSingleColumnContext<String> distinctString(String columnName)
    {
        EntityBuilder<String> entityBuilder = new StringEntityBuilder(columnName);
        return new SelectSingleColumnContext<String>(columnName, true, entityBuilder);
    }

    /**
     * Begins a {@code SELECT} query that will only retrieve the specified {@code String} column.
     *
     * @param columnName the field to query
     * @return a partially constructed query; use {@code .from(entityName)} to continue building it
     */
    public static SelectSingleColumnContext<String> stringColumn(String columnName)
    {
        EntityBuilder<String> entityBuilder = new StringEntityBuilder(columnName);
        return new SelectSingleColumnContext<String>(columnName, false, entityBuilder);
    }

    /**
     * Begins a {@code SELECT *} query for the specified entity factory.
     * <p>
     * As the query returns results, {@link NamedEntityBuilder#build(GenericValue)} is used on each
     * value to yield its corresponding entity value.
     * </p>
     *
     * @param entityFactory the entity factory to use for coverting {@code GenericValue} to the
     *      desired return type
     * @param <E> the entity type that this query will yield, as inferred from its factory
     * @return a partially constructed query; use one of the {@code .where...} constraints, an {@code orderBy},
     *      or a {@code .runWith} method to continue building it
     */
    public static <E> SelectColumnsFromContext<E> from(NamedEntityBuilder<E> entityFactory)
    {
        final QueryBuilder<E> queryBuilder = new QueryBuilder<E>();
        queryBuilder.fieldsToSelect = null;
        queryBuilder.distinct = false;
        queryBuilder.entityBuilder = entityFactory;
        queryBuilder.entityName = entityFactory.getEntityName();

        return new SelectColumnsFromContext<E>(queryBuilder);
    }

    /**
     * Begins a {@code SELECT *} query for the specified entity.
     *
     * @param entityName the name of the entity to query
     * @return a partially constructed query; use one of the {@code .where...} constraints, an {@code orderBy},
     *      or a {@code .runWith} method to continue building it
     */
    public static SelectColumnsFromContext<GenericValue> from(String entityName)
    {
        final QueryBuilder<GenericValue> queryBuilder = new QueryBuilder<GenericValue>();
        queryBuilder.fieldsToSelect = null;
        queryBuilder.distinct = false;
        queryBuilder.entityBuilder = new NoopEntityBuilder();
        queryBuilder.entityName = entityName;

        return new SelectColumnsFromContext<GenericValue>(queryBuilder);
    }

    /**
     * Builds a "SELECT COUNT(*) FROM ..." query for the given entity.
     * <p>
     * For example:
     * </p>
     * <code><pre>
     * long count = Select.countFrom(Entity.Name.COMMENT)
     *                    .whereEqual("author", userKey)
     *                    .runWith(ofBizDelegator)
     *                    .singleValue();
     * </pre></code>
     * <p>
     * This method requires the existence of a {@code "Count"} view in the entity model, which has not been defined
     * for all entities.  Consider building a normal query with {@link SelectQuery.ExecutionContext#count() .count()}
     * as the finishing action for a more flexible alternative.
     * </p>
     *
     * @param entityName the Entity
     *
     * @return a query builder for a "SELECT COUNT(*) FROM ..." query
     */
    public static SelectColumnsFromContext<Long> countFrom(String entityName)
    {
        final QueryBuilder<Long> queryBuilder = new QueryBuilder<Long>();
        queryBuilder.fieldsToSelect = COUNT_FIELD_LIST;
        queryBuilder.distinct = false;
        queryBuilder.entityBuilder = new CountEntityBuilder();
        queryBuilder.entityName = entityName + "Count";

        return new SelectColumnsFromContext<Long>(queryBuilder);
    }

    /**
     * Partially constructed query that has a column list (with a single target column) but no entity name, yet.
     */
    public static class SelectSingleColumnContext<E>
    {
        private final QueryBuilder<E> queryBuilder = new QueryBuilder<E>();

        private SelectSingleColumnContext(String columnName, boolean distinct, EntityBuilder<E> entityBuilder)
        {
            queryBuilder.fieldsToSelect = Collections.singletonList(columnName);
            queryBuilder.distinct = distinct;
            queryBuilder.entityBuilder = entityBuilder;
        }

        public SelectColumnsFromContext<E> from(String entityName)
        {
            queryBuilder.entityName = entityName;
            return new SelectColumnsFromContext<E>(queryBuilder);
        }

        public SelectColumnsFromContext<E> from(NamedEntityBuilder<?> entityBuilder)
        {
            return from(entityBuilder.getEntityName());
        }
    }

    /**
     * Partially constructed query that has a column list but no entity name, yet.
     */
    public static class SelectColumnsContext
    {
        private final QueryBuilder<GenericValue> queryBuilder = new QueryBuilder<GenericValue>();

        private SelectColumnsContext(List<String> columns, boolean distinct)
        {
            queryBuilder.fieldsToSelect = ImmutableList.copyOf(columns);
            queryBuilder.distinct = distinct;
        }

        private SelectColumnsContext(String[] columns, boolean distinct)
        {
            queryBuilder.fieldsToSelect = Arrays.asList(columns);
            queryBuilder.distinct = distinct;
        }

        public SelectColumnsFromContext<GenericValue> from(String entityName)
        {
            queryBuilder.entityName = entityName;
            queryBuilder.entityBuilder = EntityBuilders.NO_OP_BUILDER;
            return new SelectColumnsFromContext<GenericValue>(queryBuilder);
        }
    }

    /**
     * Partially constructed query that has enough information supplied to form a complete query.
     * <p>
     * If no further modifications are needed, use {@link #runWith(EntityEngine)} or {@link #runWith(OfBizDelegator)}
     * to specify the execution method, then use one of the return methods from {@link SelectQuery.ExecutionContext}
     * to collect the results.  When both components are available, {@code EntityEngine} should be preferred, but the
     * two mechanisms are functionally equivalent.
     * </p>
     */
    public static abstract class ExecutableContext<E> implements SelectQuery<E>
    {
        protected final QueryBuilder<E> queryBuilder;

        protected ExecutableContext(QueryBuilder<E> queryBuilder)
        {
            this.queryBuilder = queryBuilder;
        }

        /**
         * Specifies the entity engine implementation to use for running the query.
         *
         * @param ofBizDelegator the {@code OfBizDelegator} to use for running the query
         * @return the fully constructed query, complete with execution context.  Use one of the return methods
         *          specified by {@link SelectQuery.ExecutionContext} to collect the results.
         */
        public final SelectQueryImpl.ExecutionContext<E> runWith(OfBizDelegator ofBizDelegator)
        {
            return queryBuilder.toQuery().runWith(ofBizDelegator);
        }

        /**
         * Specifies the entity engine implementation to use for running the query.
         *
         * @param entityEngine the {@code EntityEngine} to use for running the query
         * @return the fully constructed query, complete with execution context.  Use one of the return methods
         *          specified by {@link SelectQuery.ExecutionContext} to collect the results.
         */
        public final SelectQueryImpl.ExecutionContext<E> runWith(EntityEngine entityEngine)
        {
            return queryBuilder.toQuery().runWith(entityEngine);
        }
    }

    /**
     * Marker for contexts that can accept a where clause.
     * <p>
     * Where clauses can be accepted until the condition chain is terminated by a {@code .orderBy} or {@code .runWith}.
     * </p>
     */
    public static abstract class WhereClauseAwareContext<E> extends ExecutableContext<E>
    {
        protected WhereClauseAwareContext(QueryBuilder<E> queryBuilder)
        {
            super(queryBuilder);
        }

        public abstract WhereContext<E> whereEqual(String field, String value);
        public abstract WhereContext<E> whereEqual(String field, Long value);
        public abstract WhereContext<E> whereCondition(EntityCondition condition);
        public abstract WhereContext<E> where(String fieldName, EntityOperator operator, Long value);
        public abstract OrderByContext<E> orderBy(String... orderByColumn);

        public final WhereContext<E> byId(@Nonnull Long id)
        {
            return whereEqual("id", notNull("id", id));
        }

        public final WhereContext<E> whereNull(String field)
        {
            return whereEqual(field, (String)null);  // Cast is not significant, here
        }

        public final WhereContext<E> whereLike(String field, String pattern)
        {
            return whereCondition(new EntityExpr(field, EntityOperator.LIKE, pattern));
        }
    }

    /**
     * A partially constructed query that may accept {@code .where} and {@code .orderBy} clauses.
     *
     * @param <E> the type of value that the query yields, which is either implied by the factory method used or
     *          inferred from the {@link NamedEntityBuilder} that was supplied to it
     */
    public static class SelectColumnsFromContext<E> extends WhereClauseAwareContext<E>
    {
        private SelectColumnsFromContext(QueryBuilder<E> queryBuilder)
        {
            super(queryBuilder);
        }

        public WhereContext<E> whereEqual(String fieldName, String value)
        {
            queryBuilder.addWhereEqual(fieldName, value);
            return new WhereContext<E>(queryBuilder);
        }

        public WhereContext<E> whereEqual(String fieldName, Long value)
        {
            queryBuilder.addWhereEqual(fieldName, value);
            return new WhereContext<E>(queryBuilder);
        }

        public WhereContext<E> where(String fieldName, EntityOperator operator, Long value)
        {
            return whereCondition(new EntityExpr(fieldName, operator, value));
        }

        @Override
        public WhereContext<E> whereCondition(final EntityCondition condition)
        {
            queryBuilder.addWhereCondition(condition);
            return new WhereContext<E>(queryBuilder);
        }

        public OrderByContext<E> orderBy(String... orderByColumn)
        {
            queryBuilder.orderBy = Arrays.asList(orderByColumn);
            return new OrderByContext<E>(queryBuilder);
        }
    }

    /**
     * A partially constructed query that may accept {@code .where} and {@code .orderBy} clauses.
     * <p>
     * As a readability aid, most {@code .whereX} clauses my also be specified with a {.andX} clause after the
     * first one has been specified.
     * </p>
     *
     * @param <E> the type of value that the query yields, which is either implied by the factory method used or
     *          inferred from the {@link NamedEntityBuilder} that was supplied to it
     * @return a partially constructed query.  Use {@code .whereX}, {@code .andX} or {@code .orderBy} to continue
     *      building it or {@code .runWith} to complete it by specifying an execution method.
     */
    public static class WhereContext<E> extends WhereClauseAwareContext<E>
    {
        private WhereContext(QueryBuilder<E> queryBuilder)
        {
            super(queryBuilder);
        }

        public WhereContext<E> whereEqual(String fieldName, String value)
        {
            queryBuilder.addWhereEqual(fieldName, value);
            return this;
        }
        public WhereContext<E> whereEqual(String fieldName, Long value)
        {
            queryBuilder.addWhereEqual(fieldName, value);
            return this;
        }
        public WhereContext<E> where(String fieldName, EntityOperator operator, Long value)
        {
            return whereCondition(new EntityExpr(fieldName, operator, value));
        }
        public WhereContext<E> whereCondition(EntityCondition condition)
        {
            queryBuilder.addWhereCondition(condition);
            return this;
        }

        public WhereContext<E> andEqual(String field, String value)
        {
            return whereEqual(field, value);
        }
        public WhereContext<E> andEqual(String field, Long value)
        {
            return whereEqual(field, value);
        }
        public WhereContext<E> andLike(String field, String pattern)
        {
            return whereLike(field, pattern);
        }
        public WhereContext<E> andCondition(EntityCondition condition)
        {
            return whereCondition(condition);
        }

        /**
         * Specifies the ordering for the return values.  This is required if you wish to specify a {@code LIMIT}
         * for the query, as some databases cannot enumerate the returned rows except in the context of a well
         * defined ordering (and which rows get returned would not be reliable without it, anyway).
         *
         * @param orderByColumns one or more columns by which to order the results
         * @return a partially constructed query.  Use {@code .limit} to continue building the query or
         *      {@code .runWith} to complete it by specifying an execution method.
         */
        public OrderByContext<E> orderBy(String... orderByColumns)
        {
            queryBuilder.orderBy = Arrays.asList(orderByColumns);
            return new OrderByContext<E>(queryBuilder);
        }
    }

    /**
     * A partially constructed query with completed column, entity, where condition, and ordering information
     * already specified.  Use {@code .limit} to specify pagination for the results or {@code .runWith} to
     * complete the query by specifying an execution method.
     *
     * @param <E> the type of value that the query yields, which is either implied by the factory method used or
     *          inferred from the {@link NamedEntityBuilder} that was supplied to it
     */
    public static class OrderByContext<E> extends ExecutableContext<E>
    {
        private OrderByContext(QueryBuilder<E> queryBuilder)
        {
            super(queryBuilder);
        }

        public LimitContext<E> limit(int maxResults)
        {
            queryBuilder.maxResults = maxResults;
            return new LimitContext<E>(queryBuilder);
        }

        public LimitContext<E> limit(int offset, int maxResults)
        {
            queryBuilder.offset = offset;
            queryBuilder.maxResults = maxResults;
            return new LimitContext<E>(queryBuilder);
        }
    }

    /**
     * A fully specified query that is ready to be executed.
     * <p>
     * Use {@code .runWith} to complete the query by specifying an execution method.
     * </p>
     *
     * @param <E> the type of value that the query yields, which is either implied by the factory method used or
     *          inferred from the {@link NamedEntityBuilder} that was supplied to it
     */
    public static class LimitContext<E> extends ExecutableContext<E>
    {
        LimitContext(QueryBuilder<E> queryBuilder)
        {
            super(queryBuilder);
        }
    }



    static class QueryBuilder<E>
    {
        boolean distinct;
        Collection<String> fieldsToSelect;
        String entityName;
        FieldMap whereEqual;
        List<EntityCondition> whereCondition;
        List<String> orderBy;
        int offset;
        int maxResults;
        EntityBuilder<E> entityBuilder;

        void addWhereEqual(String field, Object value)
        {
            if (whereEqual == null)
            {
                whereEqual = new FieldMap();
            }
            whereEqual.add(field, value);
        }

        void addWhereCondition(EntityCondition condition)
        {
            if (whereCondition == null)
            {
                whereCondition = new ArrayList<EntityCondition>(8);
            }
            whereCondition.add(condition);
        }

        public SelectQuery<E> toQuery()
        {
            return new SelectQueryImpl<E>(this);
        }
    }
}
