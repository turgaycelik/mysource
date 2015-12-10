package com.atlassian.jira.functest.config.sharing;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Simple implementation of {@link ConfigSharedEntity} for use in
 * the tests.
 *
 * @since v4.2
 */
public class ConfigSharedEntityId implements ConfigSharedEntity
{
    private final String entityType;
    private final Long id;

    public ConfigSharedEntityId(final Long id, final String entityType)
    {
        this.entityType = entityType;
        this.id = id;
    }

    public String getEntityType()
    {
        return entityType;
    }

    public Long getId()
    {
        return id;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final ConfigSharedEntityId id1 = (ConfigSharedEntityId) o;

        if (entityType != null ? !entityType.equals(id1.entityType) : id1.entityType != null)
        {
            return false;
        }
        if (id != null ? !id.equals(id1.id) : id1.id != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = entityType != null ? entityType.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
