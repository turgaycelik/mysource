package com.atlassian.jira.event.entity;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.user.ApplicationUser;

/**
 * Abstract event that captures the data relevant to issue property events.
 * @since v6.2
 */
@ExperimentalApi
public abstract class AbstractPropertyEvent
{
    private final EntityProperty entityProperty;
    private final ApplicationUser user;

    public AbstractPropertyEvent(final EntityProperty entityProperty, final ApplicationUser user)
    {
        this.entityProperty = entityProperty;
        this.user = user;
    }

    /**
     * @return the entity property on which the operation was performed.
     */
    public EntityProperty getEntityProperty()
    {
        return entityProperty;
    }

    /**
     * @return user who performed the operation.
     */
    public ApplicationUser getUser()
    {
        return user;
    }
}
