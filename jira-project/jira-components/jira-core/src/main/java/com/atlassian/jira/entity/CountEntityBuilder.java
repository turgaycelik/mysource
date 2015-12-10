package com.atlassian.jira.entity;

import org.ofbiz.core.entity.GenericValue;

/**
 * Implementation of EntityBuilder to return a count.
 * That is, it returns a long value as returned in a SELECT COUNT(*) FROM ... query.
 *
 * @since v6.1
 */
public class CountEntityBuilder implements EntityBuilder<Long>
{
    static final String COUNT_FIELD_NAME = "count";

    @Override
    public Long build(final GenericValue genericValue)
    {
        return genericValue.getLong(COUNT_FIELD_NAME);
    }
}
