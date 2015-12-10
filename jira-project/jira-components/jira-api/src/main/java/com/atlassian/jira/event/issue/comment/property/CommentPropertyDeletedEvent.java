package com.atlassian.jira.event.issue.comment.property;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.event.entity.AbstractPropertyEvent;
import com.atlassian.jira.event.entity.EntityPropertyDeletedEvent;
import com.atlassian.jira.user.ApplicationUser;

/**
 * Event indicating comment property was deleted.
 *
 * @since v6.2
 */
@ExperimentalApi
public class CommentPropertyDeletedEvent  extends AbstractPropertyEvent implements EntityPropertyDeletedEvent
{
    public CommentPropertyDeletedEvent(EntityProperty entityProperty, ApplicationUser user)
    {
        super(entityProperty, user);
    }
}
