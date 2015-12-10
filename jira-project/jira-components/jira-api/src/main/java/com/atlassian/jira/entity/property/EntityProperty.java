package com.atlassian.jira.entity.property;

import java.sql.Timestamp;

import com.atlassian.annotations.ExperimentalApi;

/**
 * Container for arbitrary JSON data attached to an entity.
 *
 * @since v6.1
 */
@ExperimentalApi
public interface EntityProperty
{
    public static final String ID = "id";
    public static final String ENTITY_NAME = "entityName";
    public static final String ENTITY_ID = "entityId";
    public static final String KEY = "propertyKey";
    public static final String VALUE = "value";
    public static final String CREATED = "created";
    public static final String UPDATED = "updated";

    public Long getId();
    public Long getEntityId();
    public String getEntityName();
    public String getKey();
    public String getValue();
    public Timestamp getCreated();
    public Timestamp getUpdated();
}
