package com.atlassian.jira.rest.util;

import org.junit.Test;
import static org.junit.Assert.assertEquals;


public class TestRestStringUtils
{
    @Test
    public void escapeUnicode()
    {
        // control characters should be escaped with single unicode
        assertEquals(RestStringUtils.escapeUnicode(String.valueOf((char) 1)), "\\u0001");
        // character >= 32 and <= 127 should not be escaped
        assertEquals(RestStringUtils.escapeUnicode(String.valueOf((char) 32)), String.valueOf((char) 32));
        // characters > 127 should be escaped with single unicode
        assertEquals(RestStringUtils.escapeUnicode("\u00A9"), "\\u00A9");
        // UTF-16 characters should be escaped with double unicode
        assertEquals(RestStringUtils.escapeUnicode("\uD83D\uDCA9"), "\\uD83D\\uDCA9");

        // test example JSON string
        assertEquals(RestStringUtils.escapeUnicode("{ value: \"\uD83D\uDCA9\" }"), "{ value: \"\\uD83D\\uDCA9\" }");
    }
}