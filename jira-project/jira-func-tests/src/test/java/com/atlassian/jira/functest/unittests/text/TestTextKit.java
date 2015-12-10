package com.atlassian.jira.functest.unittests.text;

import com.atlassian.jira.functest.framework.util.text.TextKit;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

/**
 * Test for TestKit.
 *
 * @since v3.13
 */
public class TestTextKit extends TestCase
{
    public void testCollapseWhitespace()
    {
        assertNull(TextKit.collapseWhitespace(null));
        assertEquals("", TextKit.collapseWhitespace(""));
        assertEquals("", TextKit.collapseWhitespace("     \n  \t "));
        assertEquals("Hello World.", TextKit.collapseWhitespace("     \n  \t Hello\n\n\nWorld.  "));
    }

    public void testEqualsCollapseWhiteSpace()
    {
        // Equal
        assertTrue(TextKit.equalsCollapseWhiteSpace(null, null));
        assertTrue(TextKit.equalsCollapseWhiteSpace("", ""));
        assertTrue(TextKit.equalsCollapseWhiteSpace(" hello", "hello  "));
        assertTrue(TextKit.equalsCollapseWhiteSpace("hello", " hello   "));
        assertTrue(TextKit.equalsCollapseWhiteSpace("  hello world\n", "hello\t\tworld"));
        // Not Equal
        assertFalse(TextKit.equalsCollapseWhiteSpace(null, ""));
        assertFalse(TextKit.equalsCollapseWhiteSpace("", null));
        assertFalse(TextKit.equalsCollapseWhiteSpace("helloworld", "hello world"));
    }

    public void testGetNumOccurencesIllegalArguments() throws Exception
    {
        try
        {
            assertEquals(0, TextKit.getNumOccurences("A", null));
            fail("IllegalArgumentException expected.");
        }
        catch (IllegalArgumentException e)
        {
            // Yay!
        }
        try
        {
            assertEquals(0, TextKit.getNumOccurences(null, "A"));
            fail("IllegalArgumentException expected.");
        }
        catch (IllegalArgumentException e)
        {
            // Yay!
        }
        try
        {
            assertEquals(0, TextKit.getNumOccurences("A", ""));
            fail("IllegalArgumentException expected.");
        }
        catch (IllegalArgumentException e)
        {
            // Yay!
        }
    }

    public void testGetNumOccurences() throws Exception
    {
        assertEquals(0, TextKit.getNumOccurences("", "A"));
        assertEquals(0, TextKit.getNumOccurences(" asdf asd", "A"));
        assertEquals(1, TextKit.getNumOccurences("Apple", "Apple"));
        assertEquals(2, TextKit.getNumOccurences("AppleAppleasdf", "Apple"));
        assertEquals(2, TextKit.getNumOccurences("BananaAppleApple", "Apple"));
        assertEquals(2, TextKit.getNumOccurences("fresh Apple pie fresh Apple pie", "Apple"));
        // Check for overlaps
        assertEquals(2, TextKit.getNumOccurences("babababa", "baba"));
    }

    public void testAssertContainsTextSequence()
    {
        // test illegal argument
        try
        {
            TextKit.assertContainsTextSequence(null, new String[] { "a" });
            fail("Assertion should have failed.");
        }
        catch (IllegalArgumentException ex)
        {
            // expected
        }
        try
        {
            TextKit.assertContainsTextSequence("asdf", null);
            fail("Assertion should have failed.");
        }
        catch (IllegalArgumentException ex)
        {
            // expected
        }
        // Trivially, an empty sequence should always pass
        TextKit.assertContainsTextSequence("", new String[] { });
        TextKit.assertContainsTextSequence("X", new String[] { });
        // Empty String should fail for any non-empty sequence
        try
        {
            TextKit.assertContainsTextSequence("", new String[] { "a" });
            fail("Assertion should have failed.");
        }
        catch (AssertionFailedError ex)
        {
            // expected - lets test the text.
            assertHasMessage(ex, "Sequence assertion failed on the 1st member of the expectedTextSequence: 'a'.");
        }
        TextKit.assertContainsTextSequence("a", new String[] { "a" });
        try
        {
            TextKit.assertContainsTextSequence("a", new String[] { "a", "a" });
            fail("Assertion should have failed.");
        }
        catch (AssertionFailedError ex)
        {
            // expected - lets test the text.
            assertHasMessage(ex, "Sequence assertion failed on the 2nd member of the expectedTextSequence: 'a'.");
        }
        TextKit.assertContainsTextSequence("aa", new String[] { "a", "a" });
        try
        {
            TextKit.assertContainsTextSequence("aa", new String[] { "a", "a", "a" });
            fail("Assertion should have failed.");
        }
        catch (AssertionFailedError ex)
        {
            // expected - lets test the text.
            assertHasMessage(ex, "Sequence assertion failed on the 3rd member of the expectedTextSequence: 'a'.");
        }

        TextKit.assertContainsTextSequence("cathat", new String[] { "cat", "hat" });
        TextKit.assertContainsTextSequence("cat hat", new String[] { "cat", "hat" });
        TextKit.assertContainsTextSequence("The cat that ate the mouse.", new String[] { "cat", "hat" });
        TextKit.assertContainsTextSequence("The cat that ate the mouse.", new String[] { "The", "hat", "mouse" });
        try
        {
            TextKit.assertContainsTextSequence("The cat that ate the mouse.", new String[] { "The", "mouse", "hat" });
            fail("Assertion should have failed.");
        }
        catch (AssertionFailedError ex)
        {
            // expected - lets test the text.
            assertHasMessage(ex, "Sequence assertion failed on the 3rd member of the expectedTextSequence: 'hat'.");
        }
        // this should fail as we can't reuse the same letter.
        try
        {
            TextKit.assertContainsTextSequence("cathat", new String[] { "cat", "that" });
            fail("Assertion should have failed.");
        }
        catch (AssertionFailedError ex)
        {
            // expected - lets test the text.
            assertHasMessage(ex, "Sequence assertion failed on the 2nd member of the expectedTextSequence: 'that'.");
        }

        // 3 way
        TextKit.assertContainsTextSequence("AABBCC", new String[] { "AA", "BB", "CC" });
        // One previous algorithm just check orders of consecutive pairs and would give false positive for below:
        try
        {
            TextKit.assertContainsTextSequence("BBCCAABB", new String[] { "AA", "BB", "CC" });
            fail("Assertion should have failed.");
        }
        catch (AssertionFailedError ex)
        {
            // expected - lets test the text.
            assertHasMessage(ex, "Sequence assertion failed on the 3rd member of the expectedTextSequence: 'CC'.");
        }
        // Exercise use of "4th"
        try
        {
            TextKit.assertContainsTextSequence("AA BB CC", new String[] { "AA", "BB", "CC", "AA" });
            fail("Assertion should have failed.");
        }
        catch (AssertionFailedError ex)
        {
            // expected - lets test the text.
            assertHasMessage(ex, "Sequence assertion failed on the 4th member of the expectedTextSequence: 'AA'.");
        }
        // Exercise use of "11th"
        try
        {
            TextKit.assertContainsTextSequence("the quick brown fox jumped over the lazy dog backwards",
                    new String[] { "the", "quick", "brown", "fox", "jumped", "over", "the", "lazy", "dog", "backwards",
                                   "again" });
            fail("Assertion should have failed.");
        }
        catch (AssertionFailedError ex)
        {
            // expected - lets test the text.
            assertHasMessage(ex, "Sequence assertion failed on the 11th member of the expectedTextSequence: 'again'.");
        }

    }

    private void assertHasMessage(AssertionFailedError ex, String message) {
        assertTrue("Doesnt have message : " + message , ex.getMessage().indexOf(message) == 0);
    }

    public void testContainsTextSequence() throws Exception
    {
        // test illegal argument
        try
        {
            TextKit.containsTextSequence(null, new String[] { "a" });
            fail("Should have thrown IllegalArgumentException");
        }
        catch (IllegalArgumentException iae)
        {
        }

        try
        {
            assertFalse(TextKit.containsTextSequence("asdf", null));
            fail("Should have thrown IllegalArgumentException");
        }
        catch (IllegalArgumentException iae)
        {

        }

        // Trivially, an empty sequence should always pass
        assertTrue(TextKit.containsTextSequence("", new String[] { }));
        assertTrue(TextKit.containsTextSequence("X", new String[] { }));

        assertFalse(TextKit.containsTextSequence("", new String[] { "a" }));

        assertTrue(TextKit.containsTextSequence("a", new String[] { "a" }));

        assertFalse(TextKit.containsTextSequence("a", new String[] { "a", "a" }));

        assertTrue(TextKit.containsTextSequence("aa", new String[] { "a", "a" }));

        assertFalse(TextKit.containsTextSequence("aa", new String[] { "a", "a", "a" }));

        assertTrue(TextKit.containsTextSequence("cathat", new String[] { "cat", "hat" }));

        assertTrue(TextKit.containsTextSequence("cat hat", new String[] { "cat", "hat" }));
        assertTrue(TextKit.containsTextSequence("The cat that ate the mouse.", new String[] { "cat", "hat" }));
        assertTrue(TextKit.containsTextSequence("The cat that ate the mouse.", new String[] { "The", "hat", "mouse" }));

        assertFalse(TextKit.containsTextSequence("The cat that ate the mouse.", new String[] { "The", "mouse", "hat" }));
        assertFalse(TextKit.containsTextSequence("cathat", new String[] { "cat", "that" }));

        // 3 way
        assertTrue(TextKit.containsTextSequence("AABBCC", new String[] { "AA", "BB", "CC" }));

        assertFalse(TextKit.containsTextSequence("BBCCAABB", new String[] { "AA", "BB", "CC" }));
        assertFalse(TextKit.containsTextSequence("AA BB CC", new String[] { "AA", "BB", "CC", "AA" }));

        // Exercise use of "11th"
        assertFalse(TextKit.containsTextSequence("the quick brown fox jumped over the lazy dog backwards",
                new String[] { "the", "quick", "brown", "fox", "jumped", "over", "the", "lazy", "dog", "backwards",
                               "again" }));
    }
}
