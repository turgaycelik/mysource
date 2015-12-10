/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.notification.type;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.notification.NotificationRecipient;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestSingleUser extends AbstractNotificationTestCase
{
    @Test
    public void testGetDisplayName()
    {
        final SingleUser singleUser = new SingleUser(new MockSimpleAuthenticationContext(null, Locale.ENGLISH));
        assertEquals("Single User", singleUser.getDisplayName());
    }

    @Test
    public void testGetType()
    {
        final SingleUser singleUser = new SingleUser(new MockSimpleAuthenticationContext(null, Locale.ENGLISH));
        assertEquals("user", singleUser.getType());
    }

    @Test
    public void testGetRecipients()
    {
        final SingleUser singleUser = new SingleUser(new MockSimpleAuthenticationContext(null, Locale.ENGLISH));
        final List<NotificationRecipient> recipients = singleUser.getRecipients(new IssueEvent(null, null, null, null), user.getKey());
        checkRecipients(recipients, user);
    }

    @Test
    public void testDoValidation()
    {
        final SingleUser singleUser = new SingleUser(new MockSimpleAuthenticationContext(null, Locale.ENGLISH));

        final Map<String,String> params = new HashMap<String,String>();
        assertFalse("Should require the specified key to be in the param map", singleUser.doValidation("Single_User", params));

        params.put("Single_User", null);
        assertFalse("Should require the specified param to be non-null", singleUser.doValidation("Single_User", params));

        params.put("Single_User", "Non User");
        assertFalse("Should require the specified user to exist", singleUser.doValidation("Single_User", params));

        params.put("Single_User", user.getUsername());
        assertTrue("Should allow a user that exists", singleUser.doValidation("Single_User", params));
    }

    @Test
    public void testGetArgumentDisplayAndValue()
    {
        final SingleUser singleUser = new SingleUser(new MockSimpleAuthenticationContext(null, Locale.ENGLISH));
        assertEquals("Should get same arg display (name) for bad arg value (key)", "Garbage", singleUser.getArgumentDisplay("Garbage"));
        assertEquals("Should get null arg value (key) for bad display arg display (name)", null, singleUser.getArgumentValue("Garbage"));
        assertEquals("Should get user's name", user.getUsername(), singleUser.getArgumentDisplay(user.getKey()));
        assertEquals("Should get user's key", user.getKey(), singleUser.getArgumentValue(user.getUsername()));
    }
}
