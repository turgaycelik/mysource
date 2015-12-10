package com.atlassian.jira.web.action.func;

import org.apache.commons.collections.keyvalue.MultiKey;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class EventTypeManagerImpl implements EventTypeManager
{
    private static final Map<MultiKey, EventType> EVENTS = new LinkedHashMap<MultiKey, EventType>();
    static
    {
        for (EventType e : Arrays.asList(new ClickOnAnchor(), new RadioEvent(), new SelectEvent()))
        {
            EVENTS.put(new MultiKey(e.getTagName(), e.getEventType()), e);
        }
    }

    public Collection<EventType> getAllEventTypes()
    {
        return EVENTS.values();
    }

    public EventType getEventType(String tagName, String eventType)
    {
        return EVENTS.get(new MultiKey(tagName, eventType));
    }
}
