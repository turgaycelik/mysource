package com.atlassian.jira.event.issue.property;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.event.entity.AbstractPropertyEvent;
import com.atlassian.jira.event.entity.EntityPropertySetEvent;
import com.atlassian.jira.user.ApplicationUser;

/**
 * Event indicating issue property value has been set.
 *
 * @since v6.2
 */
@ExperimentalApi
public class IssuePropertySetEvent extends AbstractPropertyEvent implements EntityPropertySetEvent
{
    public IssuePropertySetEvent(final EntityProperty entityProperty, final ApplicationUser user)
    {
        super(entityProperty, user);
    }
}
