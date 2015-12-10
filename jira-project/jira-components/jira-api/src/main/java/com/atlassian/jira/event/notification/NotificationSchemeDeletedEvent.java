package com.atlassian.jira.event.notification;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.event.scheme.AbstractSchemeDeletedEvent;

/**
 *  Event indicating a notification scheme has been deleted.
 *
 * @since v5.0
 */
public class NotificationSchemeDeletedEvent extends AbstractSchemeDeletedEvent
{
    @Internal
    public NotificationSchemeDeletedEvent(Long id, String name)
    {
        super(id, name);
    }
}
