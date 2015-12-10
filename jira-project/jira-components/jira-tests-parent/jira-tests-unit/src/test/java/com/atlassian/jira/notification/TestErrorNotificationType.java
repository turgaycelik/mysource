package com.atlassian.jira.notification;

import com.atlassian.jira.event.issue.IssueEvent;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TestErrorNotificationType
{
    private ErrorNotificationType errorNotificationType;

    @Before
    public void setUp()
    {
        this.errorNotificationType = new ErrorNotificationType("Test Error");
    }

    @Test
    public void testDisplayName()
    {
        assertEquals("Test Error", errorNotificationType.getDisplayName());
    }

    @Test
    public void testRecipients()
    {
        assertNull(errorNotificationType.getRecipients(new IssueEvent(null, null, null, null), null));
    }

    @Test
    public void testValidationIsFalse()
    {
        assertTrue(!errorNotificationType.doValidation(null, null));
    }
}
