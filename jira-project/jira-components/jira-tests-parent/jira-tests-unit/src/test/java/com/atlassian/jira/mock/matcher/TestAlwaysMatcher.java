package com.atlassian.jira.mock.matcher;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Test the {@link com.atlassian.jira.mock.matcher.AlwaysMatcher}.
 *
 * @since v4.0
 */
@SuppressWarnings ({ "UnnecessaryLocalVariable" })
public class TestAlwaysMatcher
{
    @Test
    public void testMatch()
    {
        ArgumentMatcher<String> stringMatcher = AlwaysMatcher.alwaysMatcher();
        assertTrue(stringMatcher.match(null, null));
        assertTrue(stringMatcher.match("match", "match"));
        assertTrue(stringMatcher.match(null, "match"));

        ArgumentMatcher<Object> objectMatcher = AlwaysMatcher.alwaysMatcher();
        assertTrue(objectMatcher.match(null, null));
        assertTrue(objectMatcher.match("match", "match"));
        assertTrue(objectMatcher.match(null, "match"));
        assertTrue(objectMatcher.match(1, "string"));
    }

    @SuppressWarnings ({ "unchecked" })
    @Test
    public void testStaticFactory() throws Exception
    {
        final ArgumentMatcher<Object> objectArgumentMatcher = AlwaysMatcher.alwaysMatcher();
        final ArgumentMatcher<Integer> integerArgumentMatcher = AlwaysMatcher.alwaysMatcher();

        assertSame(objectArgumentMatcher, integerArgumentMatcher);

        Object obj = new Object();

        //This tests the logic behind the cast to ArgumentMatcher<T> in the AlwaysMatcher .alwaysMatcher(). We need
        //to make sure that the singleton matcher will work for all arguments.
        final ArgumentMatcher rawMatcher = integerArgumentMatcher;
        assertTrue(rawMatcher.match(null, null));
        assertTrue(rawMatcher.match(obj, obj));
        assertTrue(rawMatcher.match(obj, null));
        assertTrue(rawMatcher.match(null, null));
        assertTrue(rawMatcher.match(1948543, 1948543));
        assertTrue(rawMatcher.match(45.67, 45.67));
        assertTrue(rawMatcher.match(34.5, 122.202));
        assertTrue(rawMatcher.match(89739847, 387852984));
    }

    @Test
    public void testToString()
    {
        ArgumentMatcher<Integer> stringMatcher = AlwaysMatcher.alwaysMatcher();
        assertEquals("12345", stringMatcher.toString(12345));
        assertTrue(StringUtils.isNotBlank(stringMatcher.toString(null)));
    }
}
