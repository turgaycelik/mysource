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
import com.atlassian.jira.project.MockProject;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestProjectLead extends AbstractNotificationTestCase
{
    private MockProject project;

    @Override
    protected void setUpTest()
    {
        project = new MockProject(1L);
        project.setLead(user);

        issue.setId(2L);
        issue.setSummary("this");
        issue.setProjectObject(project);
    }

    @Override
    public void tearDownTest()
    {
        project = null;
    }

    @Test
    public void testGetDisplayName()
    {
        final ProjectLead pl = new ProjectLead(new MockSimpleAuthenticationContext(null, Locale.ENGLISH));
        assertEquals("Project Lead", pl.getDisplayName());
    }

    @Test
    public void testGetRecipients()
    {
        final ProjectLead pl = new ProjectLead(new MockSimpleAuthenticationContext(null, Locale.ENGLISH));
        List<NotificationRecipient> recipients = pl.getRecipients(new IssueEvent(issue, null, null, null), null);
        checkRecipients(recipients, user);
    }
}
