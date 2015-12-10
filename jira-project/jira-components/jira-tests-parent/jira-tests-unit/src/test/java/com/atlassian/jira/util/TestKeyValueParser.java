package com.atlassian.jira.util;

import junit.framework.TestCase;

public class TestKeyValueParser extends TestCase
{
    public void testNull()
    {
        KeyValuePair<String, String> keyValuePair = KeyValueParser.parse(null);
        assertNull(keyValuePair);
    }

    public void testHappy() throws Exception
    {
        KeyValuePair<String, String> keyValuePair = KeyValueParser.parse("zed=dead");
        assertEquals("zed", keyValuePair.getKey());
        assertEquals("dead", keyValuePair.getValue());
    }

    public void testMultiEquals() throws Exception
    {
        KeyValuePair<String, String> keyValuePair = KeyValueParser.parse("zed=dead=fred");
        assertEquals("zed", keyValuePair.getKey());
        assertEquals("dead=fred", keyValuePair.getValue());
    }

    public void testIllegal()
    {
        assertIllegal("");
        assertIllegal(" ");
        assertIllegal("zed");
        assertIllegal("zed:dead");
    }

    private void assertIllegal(String s)
    {
        try
        {
            KeyValueParser.parse(s);
            fail();
        }
        catch (IllegalArgumentException ex)
        {
            // Goody
        }
    }

}
