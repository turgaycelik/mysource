package com.atlassian.jira.functest.unittests.config;

import com.atlassian.jira.functest.config.CheckMessage;
import junit.framework.TestCase;

/**
 * Test for {@link com.atlassian.jira.functest.unittests.config.TestCheckMessage}.
 *
 * @since v4.1
 */
public class TestCheckMessage extends TestCase
{
    public void testOnlyMessage() throws Exception
    {
        final String messageStr = "message";
        final CheckMessage message = new CheckMessage(messageStr);
        assertEquals(messageStr, message.getMessage());
        assertEquals(messageStr, message.getFormattedMessage());
        assertNull(message.getCheckId());
    }

    public void testNullMessage() throws Exception
    {
        final String msg = "<unknown>";
        final CheckMessage message = new CheckMessage(null);
        
        assertEquals(msg, message.getMessage());
        assertEquals(msg, message.getFormattedMessage());
        assertNull(message.getCheckId());
    }

    public void testCheckId() throws Exception
    {
        final String messageStr = "message";
        final String checkId = "checkId";
        final CheckMessage message = new CheckMessage(messageStr, checkId);
        assertEquals(messageStr, message.getMessage());
        assertEquals("[checkId] - message", message.getFormattedMessage());
        assertEquals(checkId, message.getCheckId());
    }

    public void testCheckIdNull() throws Exception
    {
        final String messageStr = "message";
        final CheckMessage message = new CheckMessage(messageStr, null);
        assertEquals(messageStr, message.getMessage());
        assertEquals(messageStr, message.getFormattedMessage());
        assertNull(message.getCheckId());
    }
}