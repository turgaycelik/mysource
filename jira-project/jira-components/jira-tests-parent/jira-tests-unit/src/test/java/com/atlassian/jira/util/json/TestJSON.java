package com.atlassian.jira.util.json;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * JSON tests
 * <p/>
 * These tend to mess with you mind because the Java source representation has quoting as does the JSON string values
 * so take time to read it and enter a ZEN like escaping state!
 *
 * @since v3.13
 */
public class TestJSON
{
    @Test
    public void testEscapeNull() throws Exception
    {
        String actual = JSONObject.quote(null);
        assertNotNull(actual);
        assertEquals("\"\"", actual);
    }

    @Test
    public void testEscapeEmptyString() throws Exception
    {
        String actual = JSONObject.quote("");
        assertNotNull(actual);
        assertEquals("\"\"", actual);
    }

    @Test
    public void testJSONObjectEscape() throws Exception
    {
        assertEscapedIsSameAddQuotes(" a simple string");
        assertEscapedIsSameAddQuotes(" a 'simple' string");

        assertEscapedAddQuotes(" a \"simple\" string", " a \\\"simple\\\" string");
        assertEscapedAddQuotes(" a '\"simple\"' string", " a '\\\"simple\\\"' string");

        assertEscapedAddQuotes(" a simple string with \nwhitespace", " a simple string with \\nwhitespace");
        assertEscapedAddQuotes(" a simple string with \bwhitespace", " a simple string with \\bwhitespace");
        assertEscapedAddQuotes(" a simple string with \twhitespace", " a simple string with \\twhitespace");

        assertEscapedAddQuotes("json\"quotes\"here", "json\\\"quotes\\\"here");

    }

    @Test
    public void testJSONEscaper() throws Exception
    {
        assertEscaper(null, "");
        assertEscaper("", "");
        assertEscaper("\"\"", "\\\"\\\"");

        assertEscaper("some json\\", "some json\\\\");
        assertEscaper("some json\nover some lines", "some json\\nover some lines");
        assertEscaper("some json\bover some lines", "some json\\bover some lines");
        assertEscaper("json\"quotes\"here", "json\\\"quotes\\\"here");
    }

    /**
     *
     */
    private void assertEscaper(String source, String expected)
    {
        String actual = JSONEscaper.escape(source);
        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    /**
     * Adds " and " characterss around the end of the expected string so DONT add them yourself otherwise you hed will exploded with nested quoting!
     */
    private void assertEscapedAddQuotes(String source, String expected)
    {
        String actual = JSONObject.quote(source);
        assertNotNull(actual);
        assertEquals("\"" + expected + "\"", actual);
    }

    private void assertEscapedIsSameAddQuotes(String source)
    {
        assertEscapedAddQuotes(source, source);
    }
}
