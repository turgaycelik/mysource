package com.atlassian.jira.rest.util;

import java.io.IOException;
import java.io.Writer;

import org.apache.commons.lang3.text.translate.CharSequenceTranslator;
import org.apache.commons.lang3.text.translate.CodePointTranslator;
import org.apache.commons.lang3.text.translate.LookupTranslator;
import org.apache.commons.lang3.text.translate.UnicodeEscaper;

/**
 * JIRA REST Plugin String utilitites.
 *
 * @since v6.2
 */
public class RestStringUtils
{
    private final static CharSequenceTranslator ESCAPE_UNICODE = new LookupTranslator(new String[][] { })
            .with(new UTF16Escaper()).with(UnicodeEscaper.outsideOf(32, 0x7f));

    /**
     * Escape characters which are below 32 or above 0x7f
     *
     * @param input string to encode
     * @return string with escaped unicode characters
     */
    public static final String escapeUnicode(String input)
    {
        return ESCAPE_UNICODE.translate(input);
    }

    private static class UTF16Escaper extends CodePointTranslator
    {
        @Override
        public boolean translate(int codePoint, Writer out) throws IOException
        {
            if (codePoint > 0xffff)
            {
                int base = codePoint - Character.MIN_SUPPLEMENTARY_CODE_POINT;
                char low = (char) (Character.MIN_LOW_SURROGATE + (base & 0x3ff));
                char high = (char) (Character.MIN_HIGH_SURROGATE + ((base >> 10) & 0x3ff));

                // output as two unicodes
                out.write("\\u" + hex(high) + "\\u" + hex(low));
                return true;
            }

            return false;
        }
    }
}
