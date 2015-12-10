package com.atlassian.jira.mail;

import java.util.List;
import java.util.Map;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;

import com.google.common.collect.Lists;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestMailThreadManagerImpl
{
    @Test
    public void testGetAssociatedIssue() throws Exception
    {
        IssueManager im = mock(IssueManager.class);
        final MockIssue mockIssue = new MockIssue(123L, 1347264545909L);
        mockIssue.setKey("ABC-123");
        when(im.getIssueObject(123L)).thenReturn(mockIssue);

        MailThreadManagerImpl mailThreadManager = new MailThreadManagerImpl(null, im, null);
        Message message = new MimeMessage((Session) null);
        message.setHeader("In-Reply-To", "<JIRA.123.1347264545909.74.1347264545999@localhost>");
        Issue issue = mailThreadManager.getAssociatedIssueObject(message);

        //noinspection ConstantConditions
        assertEquals("ABC-123", issue.getKey());

        // What if the created date is wrong
        message.setHeader("In-Reply-To", "<JIRA.123.1347264333111.74.1347264545999@localhost>");
        issue = mailThreadManager.getAssociatedIssueObject(message);

        assertEquals(null, issue);

        // However - we truncate milliseconds because of JDBC conversions dropping millseconds sometimes
        message.setHeader("In-Reply-To", "<JIRA.123.1347264545908.74.1347264545999@localhost>");
        issue = mailThreadManager.getAssociatedIssueObject(message);

        //noinspection ConstantConditions
        assertEquals("ABC-123", issue.getKey());
    }

    @SuppressWarnings ("ConstantConditions")
    @Test
    public void testGetAssociatedIssueFromReferences() throws Exception
    {
        IssueManager im = mock(IssueManager.class);
        final MockIssue mockIssue = new MockIssue(123L, 1347264545909L);
        mockIssue.setKey("ABC-123");
        when(im.getIssueObject(123L)).thenReturn(mockIssue);

        MailThreadManagerImpl mailThreadManager = new MailThreadManagerImpl(new MockOfBizDelegator(), im, null);
        Message message = new MimeMessage((Session) null);
        message.setHeader("In-Reply-To", "<50512597.808@atlassian.com>");
        // References can have multiple Message-IDs with whitespace between
        message.setHeader("References", "<JIRA.123.1347264545909.4.1347495168235@marky2>\r\n <50512531.80500@atlassian.com> <50512597.808@atlassian.com>");
        Issue issue = mailThreadManager.getAssociatedIssueObject(message);

        //noinspection ConstantConditions
        assertEquals("ABC-123", issue.getKey());

        // put References in middle
        message.setHeader("References", " <50512531.80500@atlassian.com> <JIRA.123.1347264545909.4.1347495168235@marky2>\r\n <50512597.808@atlassian.com>   ");
        issue = mailThreadManager.getAssociatedIssueObject(message);

        assertEquals("ABC-123", issue.getKey());
    }

    @Test
    public void testShouldAllowModificationsToIssuesWithoutCreationDate() throws Exception
    {
        IssueManager im = mock(IssueManager.class);
        final MockIssue mockIssue = new MockIssue(123L); // no create-date passed to constructor so issue.getCreated() will return null
        mockIssue.setKey("ABC-123");
        when(im.getIssueObject(123L)).thenReturn(mockIssue);

        MailThreadManagerImpl mailThreadManager = new MailThreadManagerImpl(new MockOfBizDelegator(), im, null);
        Message message = new MimeMessage((Session) null);
        message.setHeader("In-Reply-To", "<50512597.808@atlassian.com>");
        // References can have multiple Message-IDs with whitespace between
        message.setHeader("References", "<JIRA.123.1347264545909.4.1347495168235@marky2>\r\n <50512531.80500@atlassian.com> <50512597.808@atlassian.com>");
        Issue issue = mailThreadManager.getAssociatedIssueObject(message);

        assertEquals(issue.getKey(), mockIssue.getKey());
    }

    @Test
    public void testShouldAllowModificationsToIssuesWhenInReplyToContainsNullStringForCreatedDateMillis()
            throws Exception
    {
        IssueManager im = mock(IssueManager.class);
        final MockIssue mockIssue = new MockIssue(123L, 1347264545909L);
        mockIssue.setKey("ABC-123");
        when(im.getIssueObject(123L)).thenReturn(mockIssue);

        MailThreadManagerImpl mailThreadManager = new MailThreadManagerImpl(new MockOfBizDelegator(), im, null);
        Message message = new MimeMessage((Session) null);

        // if at some point the issue with id=123 had its created-date set to null then this will have resulted
        // in email notifications containing a message-id header similar to this: if this happens we treat the
        // email as valid even if the issue has subsequently had a created-date added again, which this test is
        // simulating
        message.setHeader("In-Reply-To", "<JIRA.123.null.4.1347495168235@atlassian.com>");
        Issue issue = mailThreadManager.getAssociatedIssueObject(message);

        assertEquals(issue.getKey(), mockIssue.getKey());
    }

    @Test
    public void testGetAssociatedIssueFromReferencesBadMessageId() throws Exception
    {
        IssueManager im = mock(IssueManager.class);
        final MockIssue mockIssue = new MockIssue(123L, 1347264545909L);
        mockIssue.setKey("ABC-123");
        when(im.getIssueObject(123L)).thenReturn(mockIssue);

        MailThreadManagerImpl mailThreadManager = new MailThreadManagerImpl(new MockOfBizDelegator(), im, null);
        Message message = new MimeMessage((Session) null);
        message.setHeader("In-Reply-To", "<50512597.808@atlassian.com>");
        // References can have multiple Message-IDs with whitespace between
        // Missing right bracket can cause drama - previously it was an OOME
        message.setHeader("References", "<20130120014306.G");
        Issue issue = mailThreadManager.getAssociatedIssueObject(message);

        //noinspection ConstantConditions
        assertEquals(null, issue);

        // Bad value but with legit Message-ID
        message.setHeader("References", "<JIRA.123.1347264545909.4.1347495168235@marky2");
        issue = mailThreadManager.getAssociatedIssueObject(message);

        // We jsut ignore this as it is invalid.
        assertEquals(null, issue);


        // Now test missing '<'
        message.setHeader("References", "JIRA.123.1347264545909.4.1347495168235@marky2>");
        issue = mailThreadManager.getAssociatedIssueObject(message);

        assertEquals(null, issue);

        // Both angle-brackets missing
        message.setHeader("References", "JIRA.123.1347264545909.4.1347495168235@marky2");
        issue = mailThreadManager.getAssociatedIssueObject(message);

        assertEquals(null, issue);
    }

    @Test
    public void findIssueFromMessageIdIssueExists() throws Exception
    {
        final long source = 101L;
        OfBizDelegator mockOfBizDelegator = mock(OfBizDelegator.class);
        IssueManager im = mock(IssueManager.class);
        MutableIssue issue = mock(MutableIssue.class);
        when(im.getIssueObject(source)).thenReturn(issue);

        MailThreadManagerImpl mailThreadManager = new MailThreadManagerImpl(mockOfBizDelegator, im, null);

        final String messageId = "<50512597.808@atlassian.com>";
        final List<GenericValue> list = Lists.newArrayList();
        GenericValue gv = mock(GenericValue.class);
        when(gv.getLong("source")).thenReturn(source);
        list.add(gv);

        when(mockOfBizDelegator.findByAnd(eq("NotificationInstance"), Matchers.<Map<String, ?>>any())).thenReturn(list);

        assertNotNull("Associated issue exists", mailThreadManager.findIssueFromMessageId(messageId));

        ArgumentCaptor<Map> captor = (ArgumentCaptor.forClass(Map.class));
        verify(mockOfBizDelegator).findByAnd(eq("NotificationInstance"), captor.capture());
        assertEquals("Passes messageId to OfBizDelegator", messageId, captor.getValue().get("messageid"));
    }

    @Test
    public void findIssueFromMessageIdIssueNotExists() throws Exception
    {
        OfBizDelegator mockOfBizDelegator = mock(OfBizDelegator.class);

        MailThreadManagerImpl mailThreadManager = new MailThreadManagerImpl(mockOfBizDelegator, null, null);

        List<GenericValue> list = Lists.newArrayList();
        final String messageId = "<50512597.808@atlassian.com>";
        when(mockOfBizDelegator.findByAnd(eq("NotificationInstance"), Matchers.<Map<String, ?>>any())).thenReturn(list);

        assertNull("Associated does not exist", mailThreadManager.findIssueFromMessageId(messageId));

        ArgumentCaptor<Map> captor = (ArgumentCaptor.forClass(Map.class));
        verify(mockOfBizDelegator).findByAnd(eq("NotificationInstance"), captor.capture());
        assertEquals("Passes messageId to OfBizDelegator", messageId, captor.getValue().get("messageid"));
    }

}
