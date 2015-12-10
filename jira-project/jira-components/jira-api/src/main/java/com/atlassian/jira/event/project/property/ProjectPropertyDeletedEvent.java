package com.atlassian.jira.event.project.property;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.event.entity.AbstractPropertyEvent;
import com.atlassian.jira.event.entity.EntityPropertyDeletedEvent;
import com.atlassian.jira.user.ApplicationUser;

/**
 * Event indicating project property was deleted.
 *
 * @since v6.2
 */
@ExperimentalApi
public class ProjectPropertyDeletedEvent extends AbstractPropertyEvent implements EntityPropertyDeletedEvent
{
    public ProjectPropertyDeletedEvent(final EntityProperty entityProperty, final ApplicationUser user)
    {
        super(entityProperty, user);
    }
}
