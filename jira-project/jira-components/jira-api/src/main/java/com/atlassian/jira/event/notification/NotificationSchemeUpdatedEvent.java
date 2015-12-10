package com.atlassian.jira.event.notification;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.event.scheme.AbstractSchemeUpdatedEvent;
import com.atlassian.jira.scheme.Scheme;

/**
 *  Event indicating a notification scheme has been updated.
 *
 * @since v5.0
 */
public class NotificationSchemeUpdatedEvent extends AbstractSchemeUpdatedEvent
{
    @Internal
    public NotificationSchemeUpdatedEvent(Scheme scheme, Scheme originalScheme)
    {
        super(scheme, originalScheme);
    }
}
