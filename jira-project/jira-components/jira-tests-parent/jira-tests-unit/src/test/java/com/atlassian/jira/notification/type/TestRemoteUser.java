/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.notification.type;

import java.util.List;
import java.util.Locale;

import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.notification.NotificationRecipient;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestRemoteUser extends AbstractNotificationTestCase
{
    @Test
    public void testGetDisplayName()
    {
        RemoteUser remoteUser = new RemoteUser(new MockSimpleAuthenticationContext(null, Locale.ENGLISH));
        assertEquals("Current User", remoteUser.getDisplayName());
    }

    @Test
    public void testGetRecipients()
    {
        RemoteUser remoteUser = new RemoteUser(new MockSimpleAuthenticationContext(null, Locale.ENGLISH));
        List<NotificationRecipient> recipients = remoteUser.getRecipients(new IssueEvent(null, user.getDirectoryUser(), null, null, null, null, null), null);
        checkRecipients(recipients, user);
    }
}
