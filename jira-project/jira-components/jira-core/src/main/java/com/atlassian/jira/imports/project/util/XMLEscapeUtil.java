package com.atlassian.jira.imports.project.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Replaces all characters that are illegal in XML with a escape sequence '\u2603[0-9][0-9][0-9][0-9]'.
 * Additionally \u2603 character is also escaped to ensure that decoding encoded text will ne the same.
 *
 * @since v6.0
 */
public class XMLEscapeUtil
{
    public static final char ESCAPING_CHAR = '\u2603'; //SNOWMAN :)

    private static final BiMap<Character, String> charToUnicodeString; //maps character to escaping string
    private static final Map<Character, List<Character>> charToUnicodeCollection; //maps character to escaping list of characters
    private static final Pattern decoderPattern = Pattern.compile("\\"+ESCAPING_CHAR + "([0-9ABCDEF]{4})");

    static
    {
        ImmutableBiMap.Builder<Character, String> stringBiMapBuilder = ImmutableBiMap.builder();
        ImmutableMap.Builder<Character, List<Character>> listMapBuilder = ImmutableMap.builder();
        for (int i = 0; i < 0x10000 ; i++)
        {
            if (!shouldEscape((char)i))
            {
                continue;
            }

            stringBiMapBuilder.put((char)i, String.format(ESCAPING_CHAR + "%04X", i));
            listMapBuilder.put((char)i, Lists.charactersOf(String.format(ESCAPING_CHAR + "%04X", i)));
        }
        charToUnicodeString = stringBiMapBuilder.build();
        charToUnicodeCollection = listMapBuilder.build();
    }

    /**
     * Replaces all characters that are illegal in XML with a Java-like unicode escape sequence
     * '\u2603[0-9][0-9][0-9][0-9]'. When <code>null</code> is passed into this method, <code>null</code> is returned.
     */
    public static String unicodeEncode(final String string)
    {
        if (string == null)
        {
            return null;
        }

        //I choose to preserve some memory for some speed
        //If no escaping is needed will just return original string
        //I don't use Guava Predicators to avoid char boxing
        boolean escapingNeeded = false;
        for (char character : string.toCharArray()) {
            if (shouldEscape(character)) {
                escapingNeeded = true;
                break;
            }
        }
        if (!escapingNeeded) {
            return string;
        }


        StringBuilder escapedCopy = new StringBuilder();
        for (char character : string.toCharArray())
        {
            if (shouldEscape(character)) {
                escapedCopy.append(charToUnicodeString.get(character));

            } else {
                escapedCopy.append(character);
            }
        }
        return escapedCopy.toString();
    }

    /**
     * Substitutes all occurrences of '\u2603[0-9][0-9][0-9][0-9]' with their corresponding character codes. When
     * <code>null</code> is passed into this method, <code>null</code> is returned.
     */
    public static String unicodeDecode(String string)
    {
        if (string == null)
        {
            return null;
        }

        boolean copied = false;
        StringBuffer copy = new StringBuffer();
        Matcher matcher = decoderPattern.matcher(string);
        while (matcher.find())
        {
            copied = true;

            if (charToUnicodeString.inverse().containsKey(matcher.group())) {
                matcher.appendReplacement(copy,  charToUnicodeString.inverse().get(matcher.group()).toString());
            } else {
                int codeInt = Integer.parseInt(matcher.group(1), 16);
                //codeInt is char because decoderPattern format guarantees it
                matcher.appendReplacement(copy, Character.toString((char)codeInt));
            }
        }

        if (copied)
        {
            matcher.appendTail(copy);
            return copy.toString();
        }
        else
        {
            return string;
        }
    }

    /**
     * Escaping characters in place in given buffer. Because escaped character is encoded with more than one character
     * it is quite possible that it won't fit in buffer. Additional characters in escaped form if needed will be added
     * to overflow queue. Buffer is analyzed from offset for len characters. Buffer may be longer but not filled -
     * maxLen is maximum buffer length that can be used.
     * @param cbuf buffer with data - it will be overwritten with escaped data
     * @param off offset of characters in buffer to be analyzed and escaped if needed
     * @param len number of characters to be analyzed
     * @param maxLen maximum number of characters that can be put into buffer (counting from offset)
     * @param overflow should be empty when called, used to return overflow data
     * @return number of characters in buffer starting from offset at the end of function
     */
    public static int unicodeInPlaceEncode(final char[] cbuf, final int off, final int len, final int maxLen, final Queue<Character> overflow)
    {
        if (len <= 0)
        {
            throw new IllegalStateException("Reader in inconsistent state: if nothing more can be read it shouldn't come here len is: " + len);
        }

        int usedLen = 0;

        //traverse all buffer positions to write
        for (int pos = off; pos < maxLen + off; pos++)
        {
            //if there is something to read from buffer in this position - read it
            if (pos < len + off)
            {
                char currentChar = cbuf[pos];

                //escape character if needed
                if (!unicodeInPlaceEncode(currentChar, overflow))
                {
                    //small optimization to not put unescaped character to queue if overflow is empty anyway
                    if (!overflow.isEmpty())
                    {
                        overflow.add(currentChar);
                    }
                    else
                    {
                        //when overflow is empty and character doesn't need escaping
                        //it's already in buffer so we don't have to do anything
                        usedLen++;
                        continue;
                    }
                }
            }

            //if overflow is not empty replace current character with first from overflow
            //and put current character escaped if needed at the end of overflow
            if (!overflow.isEmpty() && pos < maxLen + off)
            {
                cbuf[pos] = overflow.remove();
                usedLen++;
            }
            else
            {
                //if we are here it means that either overflow is empty or we've reached maxLen
                //so we can escape anyway
                break;
            }
        }

        return usedLen;
    }

    /**
     * Checks given character. If it needs escaping return true and adding it's escaping to the overflow.
     * It does nothing if character doesn't need escaping
     * @param c character to escape
     * @param overflow queue to be updated with escaped data
     * @return true if character was escaped, false if it doesn't need escaping
     */
    private static boolean unicodeInPlaceEncode(final char c, final Queue<Character> overflow)
    {
        if (shouldEscape(c))
        {
            //escape
            overflow.addAll(charToUnicodeCollection.get(c));

            return true;
        }
        else
        {
            //no need to escape
            return false;
        }
    }

    /**
     * @param c character to check
     * @return true if character should be escaped
     */
    private static boolean shouldEscape(final char c)
    {
        return (c <= 0x1F && c != 0x09 && c != 0x0A && c != 0x0D) ||
                (c >= 0xD800 && c <= 0xDFFF) ||
                (c >= 0xFFFE) ||
                (c == ESCAPING_CHAR);
    }
}
