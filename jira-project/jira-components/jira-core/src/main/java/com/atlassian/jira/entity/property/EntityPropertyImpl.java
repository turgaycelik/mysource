package com.atlassian.jira.entity.property;

import java.sql.Timestamp;

import javax.annotation.concurrent.Immutable;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * @since v6.1
 */
@Immutable
public class EntityPropertyImpl implements EntityProperty
{
    private final Long id;
    private final String entityName;
    private final Long entityId;
    private final String key;
    private final String value;
    private final Timestamp created;
    private final Timestamp updated;

    private EntityPropertyImpl(Long id, String entityName, Long entityId, String key, String value,
            Timestamp created, Timestamp updated)
    {
        this.id = id;
        this.entityName = notNull("entityName", entityName);
        this.entityId = notNull("entityId", entityId);
        this.key = notNull("key", key);
        this.value = notNull("value", value);
        this.created = notNull("created", created);
        this.updated = notNull("updated", updated);
    }



    public static EntityProperty existing(Long id, String entityName, Long entityId, String key, String value,
            Timestamp created, Timestamp updated)
    {
        return new EntityPropertyImpl(notNull("id", id), entityName, entityId, key, value, created, updated);
    }

    public static EntityProperty forCreate(String entityName, Long entityId, String key, String value)
    {
        final Timestamp now = new Timestamp(System.currentTimeMillis());
        return new EntityPropertyImpl(null, entityName, entityId, key, value, now, now);
    }



    public Long getId()
    {
        return id;
    }

    public Long getEntityId()
    {
        return entityId;
    }

    public String getEntityName()
    {
        return entityName;
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

    @Override
    public String toString()
    {
        return "EntityPropertyImpl[id=" + id +
                ",entityName=" + entityName +
                ",entityId=" + entityId +
                ",key=" + key +
                ",value=" + ((value.length() > 70) ? (value.substring(0,64) + " ...") : value) +
                ",created=" + created +
                ",updated=" + updated +
                ']';
    }
}
