package com.atlassian.jira.mock.matcher;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * A test for {@link com.atlassian.jira.mock.matcher.NaturalMatcher}.
 *
 * @since v4.0
 */
@SuppressWarnings ({ "UnnecessaryLocalVariable" })
public class TestNaturalMatcher
{
    @SuppressWarnings ({ "UnnecessaryBoxing" })
    @Test
    public void testMatch() throws Exception
    {
        Object obj = new Object();

        final ArgumentMatcher<Object> objectArgumentMatcher = NaturalMatcher.naturalMatcher();
        assertTrue(objectArgumentMatcher.match(null, null));
        assertTrue(objectArgumentMatcher.match(obj, obj));

        assertFalse(objectArgumentMatcher.match(null, obj));
        assertFalse(objectArgumentMatcher.match(obj, null));
        assertFalse(objectArgumentMatcher.match(obj, new Object()));

        final ArgumentMatcher<Integer> integerArgumentMatcher = NaturalMatcher.naturalMatcher();
        assertTrue(integerArgumentMatcher.match(null, null));
        //Note: NO AUTOBOXING HERE. I want the explicit calls to the constructor to ensure that the Integer are
        // not cached copies.
        assertTrue(integerArgumentMatcher.match(new Integer(1), new Integer(1)));
        assertFalse(integerArgumentMatcher.match(new Integer(2), null));
        assertFalse(integerArgumentMatcher.match(null, new Integer(3)));
        assertFalse(integerArgumentMatcher.match(new Integer(4), new Integer(5)));
    }

    @SuppressWarnings ({ "unchecked" })
    @Test
    public void testStaticFactory() throws Exception
    {
        final ArgumentMatcher<Object> objectArgumentMatcher = NaturalMatcher.naturalMatcher();
        final ArgumentMatcher<Integer> integerArgumentMatcher = NaturalMatcher.naturalMatcher();

        assertSame(objectArgumentMatcher, integerArgumentMatcher);

        Object obj = new Object();

        //This tests the logic behind the cast to ArgumentMatcher<T> in the NaturalMatcher.naturalMatcher(). We need
        //to make sure that the singleton matcher will work for all arguments.
        final ArgumentMatcher rawMatcher = integerArgumentMatcher;
        assertTrue(rawMatcher.match(null, null));
        assertTrue(rawMatcher.match(obj, obj));
        assertFalse(rawMatcher.match(obj, null));
        assertTrue(rawMatcher.match(null, null));
        assertTrue(rawMatcher.match(1948543, 1948543));
        assertTrue(rawMatcher.match(45.67, 45.67));
        assertFalse(rawMatcher.match(34.5, 122.202));
        assertFalse(rawMatcher.match(89739847, 387852984));
    }

    @Test
    public void testToString() throws Exception
    {
        final ArgumentMatcher<Integer> integerArgumentMatcher = NaturalMatcher.naturalMatcher();
        assertFalse(StringUtils.isBlank(integerArgumentMatcher.toString(null)));
        assertEquals("18348", integerArgumentMatcher.toString(18348));
    }
}
