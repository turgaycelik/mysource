package com.atlassian.jira.mock.matcher;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link com.atlassian.jira.mock.matcher.TestNotNullMatcher}.
 *
 * @since v4.0
 */
public class TestNotNullMatcher
{
    @Test
    public void testMatch()
    {
        ArgumentMatcher<String> stringMatcher = NotNullMatcher.notNullMatcher();
        assertFalse(stringMatcher.match(null, null));
        assertFalse(stringMatcher.match("coollio", null));
        assertTrue(stringMatcher.match(null, "match"));
        assertTrue(stringMatcher.match("me", "match"));

        ArgumentMatcher<Object> objectMatcher = NotNullMatcher.notNullMatcher();
        assertFalse(objectMatcher.match(null, null));
        assertFalse(objectMatcher.match("match", null));
        assertTrue(objectMatcher.match(null, "match"));
        assertTrue(objectMatcher.match(1, "string"));
        assertTrue(objectMatcher.match(new ArrayList<Integer>(), "string"));
    }

    @SuppressWarnings ({ "unchecked", "UnnecessaryLocalVariable" })
    @Test
    public void testStaticFactory() throws Exception
    {
        final ArgumentMatcher<Object> objectArgumentMatcher = NotNullMatcher.notNullMatcher();
        final ArgumentMatcher<Integer> integerArgumentMatcher = NotNullMatcher.notNullMatcher();

        assertSame(objectArgumentMatcher, integerArgumentMatcher);

        Object obj = new Object();

        //This tests the logic behind the cast to ArgumentMatcher<T> in the AlwaysMatcher .alwaysMatcher(). We need
        //to make sure that the singleton matcher will work for all arguments.
        final ArgumentMatcher rawMatcher = integerArgumentMatcher;
        assertFalse(rawMatcher.match(null, null));
        assertTrue(rawMatcher.match(obj, obj));
        assertFalse(rawMatcher.match(obj, null));
        assertFalse(rawMatcher.match(null, null));
        assertTrue(rawMatcher.match(1948543, 1948543));
        assertTrue(rawMatcher.match(45.67, 45.67));
        assertTrue(rawMatcher.match(34.5, 122.202));
        assertTrue(rawMatcher.match(89739847, 387852984));
    }

    @Test
    public void testToString()
    {
        ArgumentMatcher<Integer> stringMatcher = NotNullMatcher.notNullMatcher();
        assertEquals("12345", stringMatcher.toString(12345));
        assertTrue(StringUtils.isNotBlank(stringMatcher.toString(null)));
    }
}
