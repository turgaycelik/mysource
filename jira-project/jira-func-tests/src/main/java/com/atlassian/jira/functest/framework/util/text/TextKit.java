package com.atlassian.jira.functest.framework.util.text;

import com.opensymphony.util.TextUtils;
import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import java.util.StringTokenizer;

/**
 * A class that handles text assertions and searches.
 *
 * @since v3.13
 */
public class TextKit
{
    /**
     * Collapses repeated white space ( \n\t) in the string into a single space.
     *
     * @param text      the text to collapse the white space in
     * @return a copy of the string with white space collapased to single spaces
     */
    public static String collapseWhitespace(String text)
    {
        if (text == null)
        {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        StringTokenizer st = new StringTokenizer(text);
        while (st.hasMoreTokens())
        {
            sb.append(st.nextToken());
            if (st.hasMoreTokens())
            {
                sb.append(' ');
            }
        }
        return sb.toString();
    }

    /**
     * Tests if two strings are equal after repeated white space sequences
     * are collapased into single spaces.
     *
     * @param string1   the first string to test
     * @param string2   the second string to test
     * @return  true iff string1 and string2 are equal after white space has been collapased
     *
     * @see #collapseWhitespace
     */
    public static boolean equalsCollapseWhiteSpace(final String string1, final String string2)
    {
        if (string1 == null)
        {
            return string2 == null;
        }
        String collapsed1 = TextKit.collapseWhitespace(string1);
        String collapsed2 = TextKit.collapseWhitespace(string2);
        return collapsed1.equals(collapsed2);
    }

    /**
     * Tests if two strings are equal after repeated white space sequences
     * are collapased into single spaces.
     *
     * @param needle   the first string to test
     * @param haystack   the second string to test
     * @return  true iff string1 and string2 are equal after white space has been collapased
     *
     * @see #collapseWhitespace
     */
    public static boolean containsCollapseWhiteSpace(final String needle, final String haystack)
    {
        if (haystack == null)
        {
            return needle == null;
        }
        else if (needle == null)
        {
            return false;
        }
        String collapsedNeedle = TextKit.collapseWhitespace(needle);
        String collapsedHaystack = TextKit.collapseWhitespace(haystack);
        return collapsedHaystack.contains(collapsedNeedle);
    }

    /**
     * Counts the number of times that subString occurs within the given text.
     * 
     * @param text The text to search.
     * @param subString The subString that we are searching for.
     * @return the number of times that subString occurs within the given text.
     */
    public static int getNumOccurences(final String text, final String subString)
    {
        if (text == null || subString == null)
        {
            throw new IllegalArgumentException("Parameters cannot be null.");
        }
        if (subString.length() == 0)
        {
            throw new IllegalArgumentException("subString cannot be empty.");
        }

        // we begin searching the srcText from the beginning.
        int fromIndex = 0;
        int count = 0;

        int index;
        do
        {
            index = text.indexOf(subString, fromIndex);
            if (index > -1)
            {
                count++;
                fromIndex = index + subString.length();
            }
        } while (index > -1);

        return count;
    }

    /**
     * Returns true if the 'srcText' contains the given sequence of text.
     *
     * @param srcText              the text to search
     * @param expectedTextSequence the expected text sequence
     * @return true if the 'srcText' contains the given sequence of text.
     */
    public static boolean containsTextSequence(String srcText, String[] expectedTextSequence)
    {
        try
        {
            assertContainsTextSequence(srcText, expectedTextSequence);
            return true;
        }
        catch (AssertionFailedError ex)
        {
            return false;
        }
    }

    /**
     * Returns true if the given expectedTextSequence of Strings all occur within the given srcText in the order given.
     *
     * @param srcText              the text to search
     * @param expectedTextSequence the expected text sequence
     */
    public static void assertContainsTextSequence(String srcText, String[] expectedTextSequence)
    {
        if (srcText == null || expectedTextSequence == null)
        {
            throw new IllegalArgumentException("Parameters cannot be null.");
        }

        // we begin searching the srcText from the beginning.
        int fromIndex = 0;
        // Loop through the Strings in the srcText expectedTextSequence:
        String remainingText = srcText;
        for (int i = 0; i < expectedTextSequence.length; i++)
        {
            String expected = expectedTextSequence[i];
            int index = srcText.indexOf(expected, fromIndex);
            if (index < 0)
            {
                // show a roundabout here in the string string
                if (remainingText.length() > 40) {
                    remainingText = remainingText.substring(0,40);
                }
                Assert.fail("Sequence assertion failed on the " + getOrdinalFor(i + 1) +
                                         " member of the expectedTextSequence: '" + expected + "'." +
                                         " around about here : ....'" + remainingText + "'...");
            }
            // we have this String. Now we update the fromIndex to search from the end of where we found it.
            fromIndex = index + expected.length();
            remainingText = srcText.substring(fromIndex);
        }
    }

    /**
     * Returns the ordinal abbreviation for the given number.
     * <p/>
     * ie 1st, 2nd, 3rd, etc
     * </p>
     *
     * @param n Number we want an ordinal String for .
     * @return the ordinal abbreviation for the given number.
     */
    private static String getOrdinalFor(int n)
    {
        // Normally everything ends in "th", except for endings of first, second, third.
        // The exception is when it is eleventh, twelth, thirteenth
        if (n > 10 && n < 20)
        {
            return String.valueOf(n) + "th";
        }
        if (n % 10 == 1)
        {
            return String.valueOf(n) + "st";
        }
        if (n % 10 == 2)
        {
            return String.valueOf(n) + "nd";
        }
        if (n % 10 == 3)
        {
            return String.valueOf(n) + "rd";
        }
        return String.valueOf(n) + "th";
    }

    public static String htmlEncode(final String text)
    {
        return TextUtils.htmlEncode(text, false);
    }
}
