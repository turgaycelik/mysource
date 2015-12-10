package com.atlassian.jira.entity;

import org.ofbiz.core.entity.GenericValue;

/**
 * No-op implementation of EntityBuilder such that you can just select GenericValues.
 *
 * @since v6.0
 */
public class NoopEntityBuilder implements EntityBuilder<GenericValue>
{
    @Override
    public GenericValue build(GenericValue genericValue)
    {
        return genericValue;
    }
}
