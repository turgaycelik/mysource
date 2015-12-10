package com.atlassian.jira.entity;

import org.ofbiz.core.entity.GenericValue;

/**
 * @since v5.2
 */
public class EntityBuilders
{
    public static final EntityBuilder<GenericValue> NO_OP_BUILDER = new NoOpBuilder();

    private EntityBuilders() {}

    private static class NoOpBuilder implements EntityBuilder<GenericValue>
    {
        @Override
        public GenericValue build(GenericValue genericValue)
        {
            return genericValue;
        }
    }
}
