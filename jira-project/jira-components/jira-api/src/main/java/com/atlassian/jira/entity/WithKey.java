package com.atlassian.jira.entity;

/**
 * Entities implementing this interface are supposed to be uniquely identifiable by key.
 * @since v6.2
 */
public interface WithKey
{
    /**
     * @return the entity key.
     */
    String getKey();
}
