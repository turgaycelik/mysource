package com.atlassian.jira.util;

import com.google.common.base.Predicate;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.collections.ComparatorUtils;

import java.util.Comparator;

/**
 * Natural Order String Comparator compares Strings in a more "human friendly" way. For instance, in a natural order
 * String comparator, "z9.txt" comes before "z10.txt". The whitespaces are also stripped so that "z9.txt" will come
 * before "z 10.txt". The comparator is not locale-dependent.
 *
 * @since v5.0
 */
public enum NaturalOrderStringComparator implements Comparator<String>
{
    CASE_SENSITIVE_ORDER(true),
    CASE_INSENSITIVE_ORDER(false);
    
    private static final Predicate<Integer> NUMERIC_PREDICATE = new Predicate<Integer>()
    {
        @Override
        public boolean apply(Integer input)
        {
            return Character.isDigit(input);
        }
    };

    private final Comparator<String> stringComparator;

    @SuppressWarnings("unchecked")
    private NaturalOrderStringComparator(boolean caseSensitive)
    {
        stringComparator = caseSensitive ? ComparatorUtils.NATURAL_COMPARATOR : String.CASE_INSENSITIVE_ORDER;
    }

    /**
     * {@inheritDoc}
     */
    public int compare(String str1, String str2)
    {
        int str1Marker = 0;
        int str2Marker = 0;
        int str1Length = str1.length();
        int str2Length = str2.length();

        while (str1Marker < str1Length && str2Marker < str2Length)
        {
            String str1Chunk = getChunk(str1, str1Marker);
            str1Marker += str1Chunk.length();

            String str2Chunk = getChunk(str2, str2Marker);
            str2Marker += str2Chunk.length();

            // if both chunks contain numeric characters, sort them numerically
            int result;
            if (Character.isDigit(str1Chunk.codePointAt(0)) && Character.isDigit(str2Chunk.codePointAt(0)))
            {
                result = compareNumerically(str1Chunk, str2Chunk);
            }
            else
            {
                str1Chunk = StringUtils.strip(str1Chunk);
                str2Chunk = StringUtils.strip(str2Chunk);
                result = stringComparator.compare(str1Chunk, str2Chunk);
            }

            if (result != 0)
            {
                return result;
            }
        }

        return str1Length - str2Length;
    }

    /**
     * Compares digit strings numerically. Leading zeroes do not affect the result.
     *
     * @param digits1 first digit string
     * @param digits2 second digit string
     * @return positive number if digits1 is bigger than digits2, 0 if they are equal, and negative number if digits1 is
     * smaller than digits2
     */
    private static int compareNumerically(String digits1, String digits2)
    {
        digits1 = StringUtils.stripStart(digits1, "0");
        digits2 = StringUtils.stripStart(digits2, "0");

        int str1ChunkLength = digits1.length();
        int result = str1ChunkLength - digits2.length();
        // if equal, the first different number is used to compare, otherwise the longer digit chunk is bigger
        if (result == 0)
        {
            for (int i = 0; i < str1ChunkLength; i++)
            {
                result = digits1.codePointAt(i) - digits2.codePointAt(i);
                if (result != 0)
                {
                    return result;
                }
            }
        }
        return result;
    }

    /**
     * Returns either a chunk of digits or a chunk of non-digits.
     *
     * @param str String
     * @param marker current position of the string being processed
     * @return a chunk of digits or a chunk of non-digits
     */
    private static String getChunk(String str, int marker)
    {
        if (Character.isDigit(str.codePointAt(marker)))
        {
            return getChunk(str, marker, NUMERIC_PREDICATE);
        }
        else
        {
            return getChunk(str, marker, com.google.common.base.Predicates.not(NUMERIC_PREDICATE));
        }
    }

    /**
     * Returns a chunk based on the given predicate.
     *
     * @param str String
     * @param marker current position of the string being processed
     * @param predicate chunk ends when the predicate returns <tt>false</tt>
     * @return chunk
     */
    private static String getChunk(String str, int marker, Predicate<Integer> predicate)
    {
        int endIndex = marker;
        int strLength = str.length();
        int codePoint;
        while (endIndex < strLength)
        {
            codePoint = str.codePointAt(endIndex);
            if (!predicate.apply(codePoint))
            {
                break;
            }
            endIndex += Character.charCount(codePoint);
        }
        return str.substring(marker, endIndex);
    }
}
