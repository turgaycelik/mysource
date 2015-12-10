package com.atlassian.jira.jql.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test for {@link com.atlassian.jira.jql.util.JqlIssueKeySupportImpl}.
 *
 * @since v4.0
 */
public class TestJqlIssueKeySupportImpl
{
    @Test
    public void testParseKeyNum() throws Exception
    {
        final JqlIssueKeySupportImpl keySupport = new JqlIssueKeySupportImpl();

        assertEquals(-1, keySupport.parseKeyNum(""));
        assertEquals(-1, keySupport.parseKeyNum(null));
        assertEquals(-1, keySupport.parseKeyNum("MKY"));
        assertEquals(1991, keySupport.parseKeyNum("MKY--1991"));
        assertEquals(1991, keySupport.parseKeyNum("MKY-1991"));
    }
}
