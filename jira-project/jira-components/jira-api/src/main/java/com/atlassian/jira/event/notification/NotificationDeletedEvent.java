package com.atlassian.jira.event.notification;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.event.scheme.AbstractSchemeEntityEvent;
import com.atlassian.jira.scheme.SchemeEntity;

/**
 * Event indicating a notification entity has been removed from a notification scheme.
 *
 * @since v5.0
 */
public class NotificationDeletedEvent extends AbstractSchemeEntityEvent
{
    @Internal
    public NotificationDeletedEvent(final Long schemeId, final SchemeEntity schemeEntity)
    {
        super(schemeId, schemeEntity);
    }

    public Long getId()
    {
        return getSchemeEntityId();
    }
}
