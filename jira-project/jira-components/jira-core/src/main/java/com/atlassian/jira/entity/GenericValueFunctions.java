package com.atlassian.jira.entity;

import com.google.common.base.Function;

import org.ofbiz.core.entity.GenericValue;

/**
 * Helpers for transforming GenericValues
 *
 * @since v6.1.1
 */
public class GenericValueFunctions
{
    public static class GetLongFunction implements Function<GenericValue, Long>, com.atlassian.jira.util.Function<GenericValue, Long>
    {
        private final String field;

        public GetLongFunction(final String field) {this.field = field;}

        @Override
        public Long apply(final GenericValue input)
        {
            return input.getLong(field);
        }

        @Override
        public Long get(final GenericValue input)
        {
            return apply(input);
        }
    }

    public static GetLongFunction getLong(final String field)
    {
        return new GetLongFunction(field);
    }

    public static class GetStringAsLongFunction implements Function<GenericValue, Long>, com.atlassian.jira.util.Function<GenericValue, Long>
    {
        private final String field;

        public GetStringAsLongFunction(final String field) {this.field = field;}

        @Override
        public Long apply(final GenericValue input)
        {
            return Long.valueOf(input.getString(field));
        }

        @Override
        public Long get(final GenericValue input)
        {
            return apply(input);
        }
    }

    public static GetStringAsLongFunction getStringAsLong(final String field)
    {
        return new GetStringAsLongFunction(field);
    }

    /**
     * @since v6.3
     */
    public static class GetStringFunction implements Function<GenericValue, String>, com.atlassian.jira.util.Function<GenericValue, String>
    {
        private final String field;

        public GetStringFunction(final String field) {this.field = field;}

        @Override
        public String apply(final GenericValue input)
        {
            return input.getString(field);
        }

        @Override
        public String get(final GenericValue input)
        {
            return apply(input);
        }
    }

    /**
     * Get function extracting String value from GenericValue.
     * @param field field name to extract
     * @since v6.3
     */
    public static GetStringFunction getString(final String field)
    {
        return new GetStringFunction(field);
    }

}
