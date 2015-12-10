package com.atlassian.jira.mail;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.issue.MockIssue;

import org.junit.Before;

import junit.framework.TestCase;

public class TestJiraMailThreader extends TestCase
{
    @Before
    public void setUp()
    {
        new MockComponentWorker().init();
    }

    public void testGetCustomMessageId() throws Exception
    {
        Issue issue = new MockIssue(10012L, 1347264545909L);
        JiraMailThreader jiraMailThreader = new JiraMailThreader(issue);
        String messageId = jiraMailThreader.getCustomMessageId(null);
        assertTrue("Wrong issue ID "  + messageId, messageId.startsWith("JIRA.10012."));
        assertTrue("Wrong issue created timestamp "  + messageId, messageId.startsWith("JIRA.10012.1347264545000."));
        assertTrue("Wrong sequence ID in " + messageId, messageId.startsWith("JIRA.10012.1347264545000.1."));

        // check sequence goes up
        messageId = jiraMailThreader.getCustomMessageId(null);
        assertTrue("Wrong sequence ID in " + messageId, messageId.startsWith("JIRA.10012.1347264545000.2."));
    }
}
