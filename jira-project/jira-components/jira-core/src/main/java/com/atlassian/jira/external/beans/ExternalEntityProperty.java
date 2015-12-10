package com.atlassian.jira.external.beans;

import java.sql.Timestamp;

/**
 * Represents an entity property object  At the moment this is only really used when importing from other JIRA instances.
 * @since v6.2
 */
public class ExternalEntityProperty
{
    private final Long id;
    private final String entityName;
    private final Long entityId;
    private final String key;
    private final String value;
    private final Timestamp created;
    private final Timestamp updated;

    public ExternalEntityProperty(final Long id, final String entityName, final Long entityId, final String key, final String value, final Timestamp created, final Timestamp updated)
    {
        this.id = id;
        this.entityName = entityName;
        this.entityId = entityId;
        this.key = key;
        this.value = value;
        this.created = created;
        this.updated = updated;
    }

    public Long getId()
    {
        return id;
    }

    public String getEntityName()
    {
        return entityName;
    }

    public Long getEntityId()
    {
        return entityId;
    }

    public String getKey()
    {
        return key;
    }

    public String getValue()
    {
        return value;
    }

    public Timestamp getCreated()
    {
        return created;
    }

    public Timestamp getUpdated()
    {
        return updated;
    }
}
