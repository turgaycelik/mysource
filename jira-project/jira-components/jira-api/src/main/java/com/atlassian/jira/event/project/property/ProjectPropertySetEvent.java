package com.atlassian.jira.event.project.property;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.event.entity.AbstractPropertyEvent;
import com.atlassian.jira.event.entity.EntityPropertySetEvent;
import com.atlassian.jira.user.ApplicationUser;

/**
 * Event indicating project property value has been set.
 *
 * @since v6.2
 */
@ExperimentalApi
public class ProjectPropertySetEvent extends AbstractPropertyEvent implements EntityPropertySetEvent
{
    public ProjectPropertySetEvent(final EntityProperty entityProperty, final ApplicationUser user)
    {
        super(entityProperty, user);
    }
}
