package com.atlassian.jira.mail.threading;

import java.sql.Timestamp;

import com.atlassian.jira.issue.Issue;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Unit tests for EmailHeaderBuilders class.
 */
public class TestEmailHeaderBuilders
{
    private Issue issue;
    private EmailHeaderBuilders.MessageIdBuilder messageIdBuilder;
    private EmailHeaderBuilders.InReplyToHeaderBuilder inReplyToHeaderBuilder;

    private static final String HOST_NAME = "hostname";
    private static final int SEQUENCE = 1234;
    private static final long ISSUE_ID = 1234L;
    private static final Timestamp ISSUE_CREATED = new Timestamp(123456L);
    private static final String ISSUE_CREATED_WITH_MILLIS = "123456";
    private static final String ISSUE_CREATED_MILLIS_ROUNDED = "123000";

    @Before
    public void setUp() throws Exception
    {
        issue = org.mockito.Mockito.mock(Issue.class);
        when(issue.getId()).thenReturn(ISSUE_ID);
        when(issue.getCreated()).thenReturn(ISSUE_CREATED);
        messageIdBuilder = new EmailHeaderBuilders.MessageIdBuilder(issue);
        inReplyToHeaderBuilder = new EmailHeaderBuilders.InReplyToHeaderBuilder(issue);
    }

    @Test(expected=IllegalStateException.class)
    public void testMessageIdBuilderSequenceMustBeFilled()
    {
        // messageIdBuilder should require sequence to be initialised
        messageIdBuilder.setHostName(HOST_NAME).build();
    }

    @Test(expected=NullPointerException.class)
    public void testMessageIdBuilderHostnameMustBeFilled()
    {
        // messageIdBuilder should require hostname to be initialised
        messageIdBuilder.setSequence(SEQUENCE).build();
    }

    @Test
    public void testMessageIdBuilderIssueCreatedDateIsAllowedBeNull()
    {
        // messageIdBuilder should allow issue created date to be null
        when(issue.getCreated()).thenReturn(null);
        assertEquals(issue.getCreated(), null);
        String messageId = messageIdBuilder.setSequence(SEQUENCE)
                                .setHostName(HOST_NAME)
                                .build();
        assertEquals(messageId.matches("JIRA."+ISSUE_ID+".null."+SEQUENCE+".\\d+@"+HOST_NAME), true);
    }

    @Test
    public void testMessageIdBuilderFormatting()
    {
        String messageId = messageIdBuilder.setSequence(SEQUENCE)
                .setHostName(HOST_NAME)
                .build();
        assertEquals(messageId.matches("JIRA." + ISSUE_ID + "." + ISSUE_CREATED_MILLIS_ROUNDED + "." + SEQUENCE + ".\\d+@" + HOST_NAME), true);
    }

    @Test
    public void testInReplyToHeaderBuilderIssueCreatedDateIsAllowedToBeNull()
    {
        // inReplyToHeaderBuilder should allow issue created date to be null
        when(issue.getCreated()).thenReturn(null);
        assertEquals(issue.getCreated(), null);
        String inReplyTo = inReplyToHeaderBuilder.setHostName(HOST_NAME).build();
        assertEquals(inReplyTo, "<JIRA."+ISSUE_ID+".null@"+HOST_NAME+">");
    }

    @Test(expected=NullPointerException.class)
    public void testInReplyToHeaderBuilderHostnameMustBeFilled()
    {
        // messageIdBuilder should require hostname to be initialised
        inReplyToHeaderBuilder.build();
    }

    @Test
    public void testInReplyToHeaderBuilderIssueFormattingOld()
    {
        String inReplyTo = inReplyToHeaderBuilder.setHostName(HOST_NAME).setDropMillis(false).build();
        assertEquals(inReplyTo, "<JIRA."+ISSUE_ID+"."+ISSUE_CREATED_WITH_MILLIS+"@"+HOST_NAME+">");
    }

    @Test
    public void testInReplyToHeaderBuilderIssueFormatting()
    {
        String inReplyTo = inReplyToHeaderBuilder.setHostName(HOST_NAME).build();
        assertEquals(inReplyTo, "<JIRA."+ISSUE_ID+"."+ISSUE_CREATED_MILLIS_ROUNDED+"@"+HOST_NAME+">");
    }
}
