package com.atlassian.jira.event.issue.comment.property;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.event.entity.AbstractPropertyEvent;
import com.atlassian.jira.event.entity.EntityPropertySetEvent;
import com.atlassian.jira.user.ApplicationUser;

/**
 * Event indicating comment property value has been set.
 *
 * @since v6.2
 */
@ExperimentalApi
public class CommentPropertySetEvent extends AbstractPropertyEvent implements EntityPropertySetEvent
{
    public CommentPropertySetEvent(EntityProperty entityProperty, ApplicationUser user)
    {
        super(entityProperty, user);
    }
}
