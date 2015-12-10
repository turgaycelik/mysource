package com.atlassian.jira.event.issue.changehistory.property;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.annotations.Internal;
import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.event.entity.AbstractPropertyEvent;
import com.atlassian.jira.event.entity.EntityPropertySetEvent;
import com.atlassian.jira.user.ApplicationUser;

/**
 * Event indicating change history property value has been set.
 *
 * @since JIRA 6.3
 */
@ExperimentalApi
public class ChangeHistoryPropertySetEvent extends AbstractPropertyEvent implements EntityPropertySetEvent
{
    @Internal
    public ChangeHistoryPropertySetEvent(final EntityProperty entityProperty, final ApplicationUser user)
    {
        super(entityProperty, user);
    }
}
