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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TestSingleEmailAddress extends AbstractNotificationTestCase
{
    @Test
    public void testGetDisplayName()
    {
        SingleEmailAddress singleEmailAddress = new SingleEmailAddress(new MockSimpleAuthenticationContext(null, Locale.ENGLISH));
        assertEquals("Single Email Address", singleEmailAddress.getDisplayName());
    }

    @Test
    public void testGetType()
    {
        SingleEmailAddress singleEmailAddress = new SingleEmailAddress(new MockSimpleAuthenticationContext(null, Locale.ENGLISH));
        assertEquals("email", singleEmailAddress.getType());
    }

    @Test
    public void testGetRecipients()
    {
        SingleEmailAddress singleEmailAddress = new SingleEmailAddress(new MockSimpleAuthenticationContext(null, Locale.ENGLISH));
        List<NotificationRecipient> recipients = singleEmailAddress.getRecipients(new IssueEvent(null, null, null, null), "user@atlassian.com");
        assertEquals(1, recipients.size());
        final NotificationRecipient nr = recipients.get(0);
        assertNull("This kind of notification type targets a specific e-mail address; there should be no user", nr.getUser());
        assertEquals("user@atlassian.com", nr.getEmail());
        assertTrue("We always use HTML format when there is no user to check preferences for", nr.isHtml());
    }

    @Test
    public void testDoValidation()
    {
        SingleEmailAddress singleEmailAddress = new SingleEmailAddress(new MockSimpleAuthenticationContext(null, Locale.ENGLISH));

        final Map<String,String> params = new HashMap<String,String>();
        assertFalse("Should require the specified key to be in the param map", singleEmailAddress.doValidation("Single_Email_Address", params));

        params.put("Single_Email_Address", null);
        assertFalse("Should require the specified param to be non-null", singleEmailAddress.doValidation("Single_Email_Address", params));

        params.put("Single_Email_Address", "Not an e-mail address");
        assertFalse("Should require the specified value to look like a valid e-mail address", singleEmailAddress.doValidation("Single_Email_Address", params));

        params.put("Single_Email_Address", user.getEmailAddress());
        assertTrue("Should allow a valid e-mail address", singleEmailAddress.doValidation("Single_Email_Address", params));
    }
}
