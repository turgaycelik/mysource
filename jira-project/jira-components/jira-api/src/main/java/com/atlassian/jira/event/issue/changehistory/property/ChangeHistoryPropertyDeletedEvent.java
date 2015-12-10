package com.atlassian.jira.event.issue.changehistory.property;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.annotations.Internal;
import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.event.entity.AbstractPropertyEvent;
import com.atlassian.jira.event.entity.EntityPropertyDeletedEvent;
import com.atlassian.jira.user.ApplicationUser;

/**
 * Event indicating change history property was deleted.
 *
 * @since JIRA 6.3
 */
@ExperimentalApi
public class ChangeHistoryPropertyDeletedEvent extends AbstractPropertyEvent implements EntityPropertyDeletedEvent
{
    @Internal
    public ChangeHistoryPropertyDeletedEvent(final EntityProperty entityProperty, final ApplicationUser user)
    {
        super(entityProperty, user);
    }
}
