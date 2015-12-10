package com.atlassian.jira.event.notification;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.event.scheme.AbstractSchemeCopiedEvent;
import com.atlassian.jira.scheme.Scheme;

/**
 * Event indicating a notification scheme has been copied.
 *
 * @since v5.0
 */
public class NotificationSchemeCopiedEvent extends AbstractSchemeCopiedEvent
{
    @Internal
    public NotificationSchemeCopiedEvent(Scheme fromScheme, Scheme toScheme)
    {
        super(fromScheme, toScheme);
    }
}
