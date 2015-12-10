package com.atlassian.jira.task.context;

import com.atlassian.johnson.event.Event;
import com.atlassian.johnson.event.EventType;

class MockJohnsonEvent extends Event
{
    public MockJohnsonEvent()
    {
        super(new EventType("test", "test"), "testing");
    }
}
