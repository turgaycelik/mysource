package com.atlassian.jira.jelly.tag.util;

import java.sql.Timestamp;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test {@link com.atlassian.jira.jelly.tag.util.JellyTagUtils}
 *
 * @since v3.12
 */
public class TestJellyTagUtils
{
    @Test
    public void testJellyTagUtilsParseDate()
    {
        assertParseDateException("", "Timestamp format must be yyyy-mm-dd hh:mm:ss");
        assertParseDateException(null, "null string");
        assertParseDateException("invalid", "Timestamp format must be yyyy-mm-dd hh:mm:ss");
        assertParseDateException("2007-12-31", "Timestamp format must be yyyy-mm-dd hh:mm:ss");

        assertParseDate("2007-12-31 11:59:59", "2007-12-31 11:59:59.0");
        assertParseDate("2007-12-31 11:59:59.123", "2007-12-31 11:59:59.123");
        assertParseDate("2007-12-31 11:59:59.123456789", "2007-12-31 11:59:59.123456789");
    }

    //--------------------------------------------------------------------------------------------------- Helper Methods
    private void assertParseDate(String timestampStr, String expectedTime)
    {
        try
        {
            Timestamp timestamp = JellyTagUtils.parseDate(timestampStr);
            assertNotNull(timestamp);
            assertEquals(expectedTime, timestamp.toString());
        }
        catch (IllegalArgumentException e)
        {
            fail("Should not have thrown an IllegalArgumentException");
        }
    }

    private void assertParseDateException(String timestampStr, String expectedErrorMessage)
    {
        try
        {
            JellyTagUtils.parseDate(timestampStr);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            assertTrue(e.getMessage().indexOf(expectedErrorMessage) != -1);
        }
    }
}
