package com.atlassian.jira.event;

import java.util.ArrayList;
import java.util.List;

import com.atlassian.event.api.EventPublisher;

import static org.junit.Assert.assertEquals;

/**
* @since v5.0
*/
public class MockEventPublisher implements EventPublisher
{
    private final List<Object> events = new ArrayList<Object>(5);

    @Override
    public void publish(Object event)
    {
        events.add(event);
    }

    @Override
    public void register(Object listener)
    {
    }

    @Override
    public void unregister(Object listener)
    {
    }

    @Override
    public void unregisterAll()
    {
    }

    /**
     * Returns the single event published in this mock EventPublisher.
     * @return the single event published in this mock EventPublisher.
     */
    public Object getSingleEvent()
    {
        assertEquals("Expected exactly one event to be published but found " + events, 1, events.size());
        return events.get(0);
    }

    /**
     * Returns the list of events published in this mock.
     * @return the list of events published in this mock.
     */
    public List<Object> getEvents()
    {
        return events;
    }
}
