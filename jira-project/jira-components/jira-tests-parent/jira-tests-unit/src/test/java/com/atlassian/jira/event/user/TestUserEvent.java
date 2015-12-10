/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.event.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.security.MockAuthenticationContext;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockUser;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class TestUserEvent
{
    public TestUserEvent()
    {
        new MockComponentWorker().init().addMock(JiraAuthenticationContext.class, new MockAuthenticationContext(null));
    }

    @Test
    public void testBlankConstructor()
    {
        User bob = new MockUser("bob");

        UserEvent event = new UserEvent(bob, UserEventType.USER_SIGNUP);
        assertEquals(0, event.getParams().size());
        assertEquals(bob, event.getUser());
    }

    @Test
    public void testFullConstructor()
    {
        User bob = new MockUser("bob");

        UserEvent event = new UserEvent(FieldMap.build("foo", "bar"), bob, UserEventType.USER_SIGNUP);
        assertEquals("bar", event.getParams().get("foo"));
        assertEquals(bob, event.getUser());
    }
}
