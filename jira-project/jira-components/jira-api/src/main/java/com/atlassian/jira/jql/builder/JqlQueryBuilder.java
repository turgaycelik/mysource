package com.atlassian.jira.jql.builder;

import com.atlassian.jira.component.ComponentAccessor;
import javax.annotation.Nonnull;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.order.OrderBy;
import net.jcip.annotations.NotThreadSafe;

/**
 * Used to build {@link com.atlassian.query.Query}'s that can be used to perform issue searching in JIRA.
 * <p/>
 * This gives you access to a {@link com.atlassian.jira.jql.builder.JqlClauseBuilder} which can be used to build up the
 * where clause of the JQL and also a {@link com.atlassian.jira.jql.builder.JqlOrderByBuilder} which can be used to build
 * up the order by clause of the JQL.
 * <p/>
 * This object can also be used as a factory for {@link com.atlassian.jira.jql.builder.JqlClauseBuilder} and
 * {@link com.atlassian.jira.jql.builder.JqlOrderByBuilder} instances.
 *
 * @see com.atlassian.jira.jql.builder.JqlClauseBuilder
 * @see com.atlassian.jira.jql.builder.JqlOrderByBuilder
 * @since v4.0
 */
@NotThreadSafe
public class JqlQueryBuilder
{
    private final JqlOrderByBuilder jqlOrderByBuilder;
    private final JqlClauseBuilder jqlClauseBuilder;

    /**
     * @return a new builder that can be used to build a JQL query.
     */
    @Nonnull
    public static JqlQueryBuilder newBuilder()
    {
        return new JqlQueryBuilder();
    }

    /**
     * Creates a new builder that clones the state of the passed in query so that you can use the resulting builder to
     * create a slightly modified query.
     *
     * @param existingQuery the template to clone, both the where clause and order by clause will be cloned.
     * @return a new builder that clones the state of the passed in query.
     */
    @Nonnull
    public static JqlQueryBuilder newBuilder(Query existingQuery)
    {
        return new JqlQueryBuilder(existingQuery);
    }

    /**
     * Build a new {@link com.atlassian.jira.jql.builder.JqlClauseBuilder}. The returned builder will have no associated
     * {@link com.atlassian.jira.jql.builder.JqlQueryBuilder}.
     *
     * @return the new clause builder.
     */
    @Nonnull
    public static JqlClauseBuilder newClauseBuilder()
    {
        return createClauseBuilder(null, null);
    }

    /**
     * Build a new {@link com.atlassian.jira.jql.builder.JqlClauseBuilder} and initialise it with the passed clause.
     * The returned builder will have no associated {@link com.atlassian.jira.jql.builder.JqlQueryBuilder}.
     *
     * @param copy the claue to add to the new builder. Can be null.
     * @return the new clause builder.
     */
    @Nonnull
    public static JqlClauseBuilder newClauseBuilder(Clause copy)
    {
        return createClauseBuilder(null, copy);
    }

    /**
     * Build a new {@link com.atlassian.jira.jql.builder.JqlClauseBuilder} and initialise it with the clause from the
     * passed query. The returned builder will have no associated {@link com.atlassian.jira.jql.builder.JqlQueryBuilder}.
     *
     * @param query the query whose where clause will be copied into the new builder. Can be null.
     * @return the new clause builder.
     */
    @Nonnull
    public static JqlClauseBuilder newClauseBuilder(Query query)
    {
        return createClauseBuilder(null, query == null ? null : query.getWhereClause());
    }

    /**
     * Build a new {@link JqlOrderByBuilder}. The returned builder will have no associated
     * {@link com.atlassian.jira.jql.builder.JqlQueryBuilder}.
     *
     * @return the new clause builder.
     */
    @Nonnull
    public static JqlOrderByBuilder newOrderByBuilder()
    {
        return new JqlOrderByBuilder(null);
    }

    /**
     * Build a new {@link JqlOrderByBuilder} and initialise it with the passed order. The returned builder will have
     * no associated {@link com.atlassian.jira.jql.builder.JqlQueryBuilder}.
     *
     * @param copy the order to copy. Can be null.
     * @return the new clause builder.
     */
    @Nonnull
    public static JqlOrderByBuilder newOrderByBuilder(OrderBy copy)
    {
        return createOrderByBuilder(null, copy);
    }

    /**
     * Build a new {@link JqlOrderByBuilder} and initialise it with the order from the passed query. The returned builder will have
     * no associated {@link com.atlassian.jira.jql.builder.JqlQueryBuilder}.
     *
     * @param query the query whose order will be copied into the new builder. Can be null.
     * @return the new clause builder.
     */
    @Nonnull
    public static JqlOrderByBuilder newOrderByBuilder(Query query)
    {
        return createOrderByBuilder(null, query == null ? null : query.getOrderByClause());
    }

    private JqlQueryBuilder()
    {
        this.jqlOrderByBuilder = createOrderByBuilder(this, null);
        this.jqlClauseBuilder = createClauseBuilder(this, null);
    }

    private JqlQueryBuilder(Query existingQuery)
    {
        Clause exisitingClause = null;
        OrderBy exisitngOrderBy = null;
        if (existingQuery != null)
        {
            exisitingClause = existingQuery.getWhereClause();
            exisitngOrderBy = existingQuery.getOrderByClause();
        }
        this.jqlClauseBuilder = createClauseBuilder(this, exisitingClause);
        this.jqlOrderByBuilder = createOrderByBuilder(this, exisitngOrderBy);
    }

    /**
     * Creates an {@link com.atlassian.jira.jql.builder.JqlOrderByBuilder} that can be used to modify the order by
     * statements of the {@link com.atlassian.jira.jql.builder.JqlQueryBuilder} instance.
     *
     * @return an OrderBy builder associated with the {@link com.atlassian.jira.jql.builder.JqlQueryBuilder} instance.
     */
    @Nonnull
    public JqlOrderByBuilder orderBy()
    {
        return this.jqlOrderByBuilder;
    }

    /**
     * Creates an {@link com.atlassian.jira.jql.builder.JqlClauseBuilder} which is used to modify the where clause portion
     * of the {@link com.atlassian.jira.jql.builder.JqlQueryBuilder} instance.
     *
     * @return a WhereClause builder associated with the {@link com.atlassian.jira.jql.builder.JqlQueryBuilder} instance.
     */
    @Nonnull
    public JqlClauseBuilder where()
    {
        return jqlClauseBuilder;
    }

    /**
     * This will find the root of the clause tree and build a {@link com.atlassian.query.Query} whos where clause will
     * return the generated clause and Order By clause will return the generated search order.
     * <p/>
     * NOTE: Calling this method does not change the state of the builder, there are no limitations on the number of
     * times this method can be invoked.
     *
     * @return a Query whos where clause contains the built clauses and search order contains the built OrderBy clauses.
     */
    @Nonnull
    public Query buildQuery()
    {
        // Create the query from our configured data
        Clause whereClause = this.jqlClauseBuilder.buildClause();
        OrderBy orderByClause = this.jqlOrderByBuilder.buildOrderBy();
        return new QueryImpl(whereClause, orderByClause, null);
    }

    /**
     * Reset the builder to its empty state.
     *
     * @return this builder with its state cleared.
     */
    public JqlQueryBuilder clear()
    {
        where().clear();
        orderBy().clear();

        return this;
    }

    private static JqlClauseBuilder createClauseBuilder(JqlQueryBuilder parent, Clause copy)
    {
        JqlClauseBuilder jqlClauseBuilder = ComponentAccessor.getComponent(JqlClauseBuilderFactory.class).newJqlClauseBuilder(parent);
        if (copy != null)
        {
            jqlClauseBuilder.addClause(copy);
        }
        return jqlClauseBuilder;
    }

    private static JqlOrderByBuilder createOrderByBuilder(JqlQueryBuilder parent, OrderBy copy)
    {
        JqlOrderByBuilder builder = new JqlOrderByBuilder(parent);
        if (copy != null)
        {
            builder.setSorts(copy);
        }
        return builder;
    }
}
