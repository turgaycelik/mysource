package com.atlassian.jira.jql.parser;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Test for {@link com.atlassian.jira.jql.parser.TestJqlParseErrorMessage}.
 *
 * @since v4.0
 */
public class TestJqlParseErrorMessage
{
    @Test
    public void testCotrVarargs() throws Exception
    {
        JqlParseErrorMessage errorMessage = new JqlParseErrorMessage("key", 0, 10, 1111);
        assertEquals(-1, errorMessage.getLineNumber());
        assertEquals(10, errorMessage.getColumnNumber());
        assertEquals("key", errorMessage.getKey());
        assertEquals(Collections.singletonList(1111), errorMessage.getArguments());

        errorMessage = new JqlParseErrorMessage("qwertyt", 1, -220, "hello", "world");
        assertEquals(1, errorMessage.getLineNumber());
        assertEquals(-1, errorMessage.getColumnNumber());
        assertEquals("qwertyt", errorMessage.getKey());
        assertEquals(Arrays.asList("hello", "world"), errorMessage.getArguments());

        try
        {
            new JqlParseErrorMessage("", 10, 1, 10011);
            fail("Expected IAE.");
        }
        catch (IllegalArgumentException expected)
        {
            //expected.
        }

        try
        {
            new JqlParseErrorMessage(null, 10, 1, 10011);
            fail("Expected IAE.");
        }
        catch (IllegalArgumentException expected)
        {
            //expected.
        }

        try
        {
            new JqlParseErrorMessage("qwerty", 10, 1, "qwerty", null);
            fail("Expected IAE.");
        }
        catch (IllegalArgumentException expected)
        {
            //expected.
        }
    }

    @Test
    public void testCotrCollections() throws Exception
    {
        JqlParseErrorMessage errorMessage = new JqlParseErrorMessage("key", 0, 10, Collections.singleton(18829));
        assertEquals(-1, errorMessage.getLineNumber());
        assertEquals(10, errorMessage.getColumnNumber());
        assertEquals("key", errorMessage.getKey());
        assertEquals(Collections.singletonList(18829), errorMessage.getArguments());

        errorMessage = new JqlParseErrorMessage("qwertyt", 1, -220, Arrays.asList("hello", "world"));
        assertEquals(1, errorMessage.getLineNumber());
        assertEquals(-1, errorMessage.getColumnNumber());
        assertEquals("qwertyt", errorMessage.getKey());
        assertEquals(Arrays.asList("hello", "world"), errorMessage.getArguments());

        try
        {
            new JqlParseErrorMessage("", 10, 1, Collections.emptyList());
            fail("Expected IAE.");
        }
        catch (IllegalArgumentException expected)
        {
            //expected.
        }

        try
        {
            new JqlParseErrorMessage(null, 10, 1, Collections.emptyList());
            fail("Expected IAE.");
        }
        catch (IllegalArgumentException expected)
        {
            //expected.
        }

        try
        {
            new JqlParseErrorMessage("qwerty", 10, 1, Arrays.asList(null, "a"));
            fail("Expected IAE.");
        }
        catch (IllegalArgumentException expected)
        {
            //expected.
        }

        try
        {
            new JqlParseErrorMessage("qwerty", 10, 1, (Collection <?>)null);
            fail("Expected IAE.");
        }
        catch (IllegalArgumentException expected)
        {
            //expected.
        }
    }
}
