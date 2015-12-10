package com.atlassian.jira.entity;

import com.atlassian.annotations.ExperimentalApi;

/**
 * Entities implementing this interface are supposed to be uniquely identifiable by id.
 * @since v6.2
 */
@ExperimentalApi
public interface WithId
{
    /**
     * @return the unique id of the entity.
     */
    Long getId();
}
