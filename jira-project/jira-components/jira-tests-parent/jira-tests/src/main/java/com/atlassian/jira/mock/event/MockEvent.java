/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.mock.event;

import com.atlassian.jira.event.AbstractEvent;

import java.util.Map;

public class MockEvent extends AbstractEvent
{
    public MockEvent()
    {
    }

    public MockEvent(Map params)
    {
        super(params);
    }
}
