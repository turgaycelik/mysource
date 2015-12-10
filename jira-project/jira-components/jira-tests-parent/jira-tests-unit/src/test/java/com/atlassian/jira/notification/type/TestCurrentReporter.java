/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.notification.type;

import java.util.Locale;

import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;

import org.junit.Test;

import static com.google.common.collect.Maps.newHashMap;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class TestCurrentReporter extends AbstractNotificationTestCase
{
    @Test
    public void testGetDisplayName()
    {
        CurrentReporter cr = new CurrentReporter(new MockSimpleAuthenticationContext(null, Locale.ENGLISH));
        assertEquals("Reporter", cr.getDisplayName());
    }

    @Test
    public void testGetRecipientsWithNoLevelSet() throws Exception
    {
        issue.setReporterId(user.getKey());

        final IssueEvent event = new IssueEvent(issue, newHashMap(), null, null);
        final CurrentReporter cr = new CurrentReporter(new MockSimpleAuthenticationContext(null, Locale.ENGLISH));
        checkRecipients(cr.getRecipients(event, null), user);
    }

    @Test
    public void testGetRecipientsWithLevelSatisfied() throws Exception
    {
        issue.setReporterId(user.getKey());
        when(groupManager.isUserInGroup(user.getUsername(), "group1")).thenReturn(true);

        final IssueEvent event = new IssueEvent(issue, paramsWithLevel(), null, null);
        final CurrentReporter cr = new CurrentReporter(new MockSimpleAuthenticationContext(null, Locale.ENGLISH));
        checkRecipients(cr.getRecipients(event, null), user);
    }

    @Test
    public void testGetRecipientsWithLevelNotSatisfied() throws Exception
    {
        issue.setReporterId(user.getKey());

        final IssueEvent event = new IssueEvent(issue, paramsWithLevel(), null, null);
        final CurrentReporter cr = new CurrentReporter(new MockSimpleAuthenticationContext(null, Locale.ENGLISH));
        checkRecipients(cr.getRecipients(event, null));
    }

}
