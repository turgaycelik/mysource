/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.event;

import java.util.Date;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.mock.event.MockEvent;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestAbstractEvent
{
    @Test
    public void testBlankConstructor()
    {
        Date before = new Date();
        MockEvent event = new MockEvent();
        Date after = new Date();

        Assert.assertEquals(0, event.getParams().size());
        assertTrue(before.before(event.getTime()) || before.equals(event.getTime()));
        assertTrue(after.after(event.getTime()) || after.equals(event.getTime()));
    }

    @Test
    public void testConstructor()
    {
        Date before = new Date();
        MockEvent event = new MockEvent(EasyMap.build("foo", "bar"));
        Date after = new Date();

        Assert.assertEquals(1, event.getParams().size());
        Assert.assertEquals("bar", event.getParams().get("foo"));
        assertTrue(before.before(event.getTime()) || before.equals(event.getTime()));
        assertTrue(after.after(event.getTime()) || after.equals(event.getTime()));
    }
}
