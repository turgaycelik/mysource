package com.atlassian.jira.mail;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.mail.MailException;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.mail.server.SMTPMailServer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test helper methods in MailUtils
 */
public class TestJiraMailUtils
{
    @Mock private MailServerManager mockMailServerManager;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        new MockComponentWorker().init().addMock(MailServerManager.class, mockMailServerManager);
    }

    @After
    public void tearDownWorker()
    {
        ComponentAccessor.initialiseWorker(null);
    }

    @Test
    public void isHasMailServerShouldReturnFalseWhenNoMailServerIsAvailable() throws Exception
    {
        // Invoke and check
        assertFalse(JiraMailUtils.isHasMailServer());
    }

    @Test
    public void testIsHasMailServer() throws MailException
    {
        // Set up
        final SMTPMailServer mockSmtpMailServer = mock(SMTPMailServer.class);
        when(mockMailServerManager.getDefaultSMTPMailServer()).thenReturn(mockSmtpMailServer);

        // Invoke and check
        assertTrue(JiraMailUtils.isHasMailServer());
    }
}
