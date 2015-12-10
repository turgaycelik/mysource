package com.atlassian.jira.web.action.func;

import java.util.Collection;

public interface EventTypeManager
{
    Collection getAllEventTypes();
    EventType getEventType(String tagName, String eventType);
}
