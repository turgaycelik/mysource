package com.atlassian.jira.util.dbc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.atlassian.jira.util.collect.CollectionBuilder;

import org.junit.Test;

import static com.atlassian.jira.util.dbc.Assertions.stripNotBlank;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestAssertions
{
    @Test
    public void testNotNull() throws Exception
    {
        final String input = "aThing";
        final Object result = Assertions.notNull("theThing", input);

        assertSame(input, result);
    }

    @Test
    public void testIsNull() throws Exception
    {
        try
        {
            Assertions.notNull("theThing", null);
            fail("NullArgumentException expected");
        }
        catch (final Assertions.NullArgumentException yay)
        {
            assertTrue(yay.getMessage().contains("theThing"));
        }
    }

    @Test
    public void testNotEmpty() throws Exception
    {
        final Collection input = Collections.singleton("aThing");
        final Object result = Assertions.notEmpty("theThing", input);

        assertSame(input, result);
    }

    @Test
    public void testNotEmptyIsNullOrEmpty() throws Exception
    {
        try
        {
            Assertions.notEmpty("theThing", null);
            fail("NullArgumentException expected");
        }
        catch (final Assertions.NullArgumentException yay)
        {
            assertTrue(yay.getMessage().contains("theThing"));
        }

        try
        {
            Assertions.notEmpty("theThing", Collections.emptyList());
            fail("EmptyArgumentException expected");
        }
        catch (final Assertions.EmptyArgumentException yay)
        {
            assertTrue(yay.getMessage().contains("theThing"));
        }
    }

    @Test
    public void testNotBlank() throws Exception
    {
        final String input = "a string";
        final String result = Assertions.notBlank("theThing", input);

        assertSame(input, result);
    }

    @Test
    public void testBlankNull() throws Exception
    {
        try
        {
            Assertions.notBlank("theThing", null);
            fail("NullArgumentException expected");
        }
        catch (final Assertions.NullArgumentException yay)
        {
            assertTrue(yay.getMessage().contains("theThing"));
        }
    }

    @Test
    public void testBlankEmpty() throws Exception
    {
        try
        {
            Assertions.notBlank("theThing", "");
            fail("BlankStringArgumentException expected");
        }
        catch (final Assertions.BlankStringArgumentException yay)
        {
            assertTrue(yay.getMessage().contains("theThing"));
        }
    }

    @Test
    public void testBlankSpaces() throws Exception
    {
        try
        {
            Assertions.notBlank("theThing", "   ");
            fail("BlankStringArgumentException expected");
        }
        catch (final Assertions.BlankStringArgumentException yay)
        {
            assertTrue(yay.getMessage().contains("theThing"));
        }
    }

    @Test
    public void testBlankTabs() throws Exception
    {
        try
        {
            Assertions.notBlank("theThing", "\t\t\t");
            fail("BlankStringArgumentException expected");
        }
        catch (final Assertions.BlankStringArgumentException yay)
        {
            assertTrue(yay.getMessage().contains("theThing"));
        }
    }

    @Test
    public void testBlankNewlines() throws Exception
    {
        try
        {
            Assertions.notBlank("theThing", "\n\n\n");
            fail("BlankStringArgumentException expected");
        }
        catch (final Assertions.BlankStringArgumentException yay)
        {
            assertTrue(yay.getMessage().contains("theThing"));
        }
    }

    @Test
    public void testBlankReturns() throws Exception
    {
        try
        {
            Assertions.notBlank("theThing", "\r\r\r");
            fail("BlankStringArgumentException expected");
        }
        catch (final Assertions.BlankStringArgumentException yay)
        {
            assertTrue(yay.getMessage().contains("theThing"));
        }
    }

    @Test
    public void testBlankCombination() throws Exception
    {
        try
        {
            Assertions.notBlank("theThing", " \t\r\r\n \t\r");
            fail("BlankStringArgumentException expected");
        }
        catch (final Assertions.BlankStringArgumentException yay)
        {
            assertTrue(yay.getMessage().contains("theThing"));
        }
    }

    @Test
    public void testBlankI18nBlanks() throws Exception
    {
        try
        {
            Assertions.notBlank("theThing", "\u2009");
            fail("BlankStringArgumentException expected");
        }
        catch (final Assertions.BlankStringArgumentException yay)
        {
            assertTrue(yay.getMessage().contains("theThing"));
        }
    }

    @Test
    public void testStripNotBlankBlanks() throws Exception
    {
        //Unicode blank. Not normally considered blank in ASCII.
        assertStripNotBlankFailsOnBlank("theThing", "\u2009");
        assertStripNotBlankFailsOnBlank("theThing", "      ");
        assertStripNotBlankFailsOnBlank("theThing", "\r\n   \t");
    }

    @Test
    public void testStripNotBlankActuallyDoesStrip() throws Exception
    {
        String name = "good";

        assertThat(stripNotBlank(name, "simple"), equalTo("simple"));
        assertThat(stripNotBlank(name, "\t\rprefix"), equalTo("prefix"));
        assertThat(stripNotBlank(name, "suffix\u2009"), equalTo("suffix"));
        assertThat(stripNotBlank(name, "        \r\n\tprefixAndSuffix\u2009"), equalTo("prefixAndSuffix"));
    }


    @Test
    public void testContainsNoNullsWithNoNulls()
    {
        final List<String> strs = CollectionBuilder.newBuilder("one", "two", "three").asList();
        final List<String> copy = Assertions.containsNoNulls("strs", strs);
        assertNotNull("copy", copy);
    }

    @Test
    public void testContainsNoNullsWithNulls()
    {
        final List<String> strs = CollectionBuilder.newBuilder("one", null, "three").asList();
        try
        {
            Assertions.containsNoNulls("strs", strs);
            fail("IllegalArgumentException expected");
        }
        catch (final IllegalArgumentException expected)
        {
            assertTrue(expected.getMessage(), expected.getMessage().contains("[1]"));
        }
    }

    @Test
    public void testContainsNoNullsWithNullsArray()
    {
        final String[] strs = new String[] {"one", null, "three"};
        try
        {
            Assertions.containsNoNulls("strs", strs);
            fail("IllegalArgumentException expected");
        }
        catch (final IllegalArgumentException expected)
        {
            assertTrue(expected.getMessage(), expected.getMessage().contains("[1]"));
        }
    }
    
    @Test
    public void testContainsNoNullsNullArray()
    {
        try
        {
            Assertions.containsNoNulls("strs", (String[])null);
            fail("IllegalArgumentException expected");
        }
        catch (final IllegalArgumentException expected)
        {
            //expected.
        }
    }

    @Test
    public void testContainsNoNullsWithNoNullsArray()
    {
        final String[] strs = new String[] {"one", "two", "three"};
        final String[] copy = Assertions.containsNoNulls("strs", strs);
        assertSame(strs, copy);
    }

    @Test
    public void testContainsNoBlanks()
    {
        final List<String> strs = CollectionBuilder.newBuilder("one", "two", "three").asList();
        final List<String> copy = Assertions.containsNoBlanks("strs", strs);
        assertNotNull("copy", copy);
        assertSame(strs, copy);
    }

    @Test
    public void testContainsNoBlanksWithBlanks()
    {
        final List<String> strs = CollectionBuilder.newBuilder("one", "two", "").asList();
        try
        {
            Assertions.containsNoBlanks("strs", strs);
            fail("IllegalArgumentException expected");
        }
        catch (final IllegalArgumentException expected)
        {
            assertTrue(expected.getMessage(), expected.getMessage().contains("[2]"));
        }
    }

    private void assertStripNotBlankFailsOnBlank(final String name, final String argument)
    {
        try
        {
            stripNotBlank(name, argument);
            fail("BlankStringArgumentException expected");
        }
        catch (final Assertions.BlankStringArgumentException yay)
        {
            assertTrue(yay.getMessage().contains(name));
        }
    }
}
