package com.atlassian.jira.entity;

import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.Visitor;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.GenericDelegator;

/**
 * @since v5.2
 */
public interface SelectQuery<E>
{
    ExecutionContext<E> runWith(OfBizDelegator ofBizDelegator);
    ExecutionContext<E> runWith(EntityEngine entityEngine);

    /**
     * The parser context for entity engine {@code Select} queries that is available after calling
     * {@link com.atlassian.jira.entity.Select.ExecutableContext#runWith(EntityEngine)} or
     * {@link com.atlassian.jira.entity.Select.ExecutableContext#runWith(com.atlassian.jira.ofbiz.OfBizDelegator)}.
     * <p>
     * The methods available through the execution context specify how the selected entities should be returned
     * to the caller.  For example, {@link #asList()} will return a list of values, or {@link #count()} will return
     * only a count of them.
     * </p>
     *
     * @param <E> the type of value yielded by this execution context, as determined by which {@link Select} factory
     *      method was used to begin the query.  For example, {@link Select#from(String)} yields {@code GenericValue}
     *      and {@link Select#id()} yields {@code Long}.
     */
    interface ExecutionContext<E>
    {
        /**
         * Returns the selected values in a list.
         * @return the selected values in a list.
         */
        @Nonnull
        List<E> asList();

        /**
         * Returns the selected values in a list with the mapping function applied to each of them.  For example,
         * if {@code mappingFunction} is a {@code Function&lt;GenericValue,String&gt;}, then each value is returned
         * as the {@code String} produced by the function instead of the {@code GenericValue} itself.
         *
         * @param <R> the type of value returned by the mapping function
         * @return the selected values as a list of the yielded values.
         */
        @Nonnull
        <R> List<R> asList(@Nonnull Function<E,R> mappingFunction);

        /**
         * Returns the single Entity that is the result of this query.
         * <p>
         * Will return {@code null} if no rows were returned by the DB query.
         *
         * @return the single matching value, or {@code null} if no match is found.
         * @throws IllegalStateException if more than one row is found.
         */
        @Nullable
        E singleValue();

        /**
         * Apply an {@link EntityListConsumer} to the returned results.
         * <p>
         * This is equivalent to calling {@link #visitWith(Visitor)}, except that the entity list consumer can
         * return a value.
         * </p>
         *
         * @param consumer the entity list consumer that will consume the query results
         * @param <R> the return value of the consumer
         * @return the result returned by the consumer's {@link EntityListConsumer#result()} method.
         */
        <R> R consumeWith(@Nonnull EntityListConsumer<E, R> consumer);

        /**
         * Visits each entity returned by the query.
         * <p>
         * This is equivalent to calling {@link #consumeWith(EntityListConsumer)} and ignoring the return value.
         * </p>
         *
         * @param visitor the visitor to call with each entity that the query returns
         */
        void visitWith(@Nonnull final Visitor<E> visitor);

        /**
         * Returns a count of matching items.  Note that this does <strong>not</strong> go through the
         * the {@code entityName + "Count"} pseudo-view stuff in the entity model, but rather generates
         * a count function directly in the query that it executes, so you should be able to use it with
         * arbitrary entities.
         *
         * @return count of matching items
         * @since 6.1
         */
        long count();
    }
}
