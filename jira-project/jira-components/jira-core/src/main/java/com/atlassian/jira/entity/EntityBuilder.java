package com.atlassian.jira.entity;

import org.ofbiz.core.entity.GenericValue;

/**
 * @since v5.2
 */
public interface EntityBuilder<E>
{
    /**
     * Builds an instance of this Entity from the given GenericValue.
     *
     * @param genericValue GenericValue for the entity
     * @return the entity Object
     */
    E build(GenericValue genericValue);
}
