package com.atlassian.jira.event.notification;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.event.scheme.AbstractSchemeEntityEvent;
import com.atlassian.jira.scheme.SchemeEntity;

/**
 * Event indicating a notification entity has been added to a notification scheme.
 *
 * @since v5.0
 */
public class NotificationAddedEvent extends AbstractSchemeEntityEvent
{
    @Internal
    public NotificationAddedEvent(final Long schemeId, final SchemeEntity schemeEntity)
    {
        super(schemeId, schemeEntity);
    }
}
