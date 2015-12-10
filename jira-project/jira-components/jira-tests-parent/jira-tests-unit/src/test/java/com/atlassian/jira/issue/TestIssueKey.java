package com.atlassian.jira.issue;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestIssueKey
{
    @Test
    public void testParse() throws Exception
    {
        // Simple Key
        IssueKey issueKey = IssueKey.from("ABC-123");
        assertEquals("ABC", issueKey.getProjectKey());
        assertEquals(123L, issueKey.getIssueNumber());
        assertEquals("ABC-123", issueKey.toString());

        // Numbers and underscore
        issueKey = IssueKey.from("AB_93-56");
        assertEquals("AB_93", issueKey.getProjectKey());
        assertEquals(56, issueKey.getIssueNumber());
        assertEquals("AB_93-56", issueKey.toString());

        // Numeric project key
        // Not officially supported
        issueKey = IssueKey.from("222-56");
        assertEquals("222", issueKey.getProjectKey());
        assertEquals(56, issueKey.getIssueNumber());
        assertEquals("222-56", issueKey.toString());

        // Two dashes
        // (Not officially supported, but we may as well handle it anyway)
        issueKey = IssueKey.from("AB-93-56");
        assertEquals("AB-93", issueKey.getProjectKey());
        assertEquals(56, issueKey.getIssueNumber());
        assertEquals("AB-93-56", issueKey.toString());

        // Two dashes and weird
        // (Not officially supported, but we may as well handle it anyway)
        issueKey = IssueKey.from("--56");
        assertEquals("-", issueKey.getProjectKey());
        assertEquals(56, issueKey.getIssueNumber());
        assertEquals("--56", issueKey.toString());
    }

    @Test
    public void testParseErrors() throws Exception
    {
        try
        {
            IssueKey.from(null);
            fail("Exception expected");
        }
        catch (NullPointerException ex)
        {
            // expected
        }

        try
        {
            IssueKey.from("");
            fail("Exception expected");
        }
        catch (IllegalArgumentException ex)
        {
            // expected
        }

        try
        {
            IssueKey.from("ABC123");
            fail("Exception expected");
        }
        catch (IllegalArgumentException ex)
        {
            // expected
        }

        try
        {
            IssueKey.from("ABC-");
            fail("Exception expected");
        }
        catch (NumberFormatException ex)
        {
            // expected
        }

        try
        {
            IssueKey.from("ABC-1x3");
            fail("Exception expected");
        }
        catch (NumberFormatException ex)
        {
            // expected
        }

        try
        {
            IssueKey.from("-123");
            fail("Exception expected");
        }
        catch (IllegalArgumentException ex)
        {
            // expected
        }
    }
}
