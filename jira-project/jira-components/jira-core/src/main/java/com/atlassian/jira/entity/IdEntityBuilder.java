package com.atlassian.jira.entity;

import org.ofbiz.core.entity.GenericValue;

/**
 *
 * @since v6.1
 */
public class IdEntityBuilder implements EntityBuilder<Long>
{
    public static final String ID = "id";
    private static final IdEntityBuilder INSTANCE = new IdEntityBuilder();

    public static IdEntityBuilder getInstance()
    {
        return INSTANCE;
    }

    private IdEntityBuilder() {}

    @Override
    public Long build(final GenericValue genericValue)
    {
        return genericValue.getLong(ID);
    }
}
